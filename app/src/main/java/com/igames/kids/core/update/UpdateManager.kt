package com.igames.kids.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(
    private val context: Context,
    var repoOwner: String = "pzeus",
    var repoName: String = "iGames"
) {
    companion object {
        const val CURRENT_VERSION_NAME = "1.0.0"
        private const val TAG = "UpdateManager"

        /**
         * Checks if latestTag is strictly newer than currentVersion.
         * Examples:
         * isNewerVersion("1.0.0", "v1.0.1") -> true
         * isNewerVersion("1.0.0", "v1.0.0") -> false
         * isNewerVersion("1.2.0", "v1.1.9") -> false
         */
        fun isNewerVersion(currentVersion: String, latestTag: String): Boolean {
            try {
                val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")
                val cleanLatest = latestTag.trim().removePrefix("v").removePrefix("V")

                val currentParts = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
                val latestParts = cleanLatest.split(".").map { it.toIntOrNull() ?: 0 }

                val maxLen = maxOf(currentParts.size, latestParts.size)
                for (i in 0 until maxLen) {
                    val c = currentParts.getOrElse(i) { 0 }
                    val l = latestParts.getOrElse(i) { 0 }
                    if (l > c) return true
                    if (l < c) return false
                }
                return false
            } catch (e: Exception) {
                return false
            }
        }
    }

    fun isNewerVersion(currentVersion: String, latestTag: String): Boolean =
        Companion.isNewerVersion(currentVersion, latestTag)

    private val _updateInfo = MutableStateFlow(AppUpdateInfo())
    val updateInfo: StateFlow<AppUpdateInfo> = _updateInfo.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()

    suspend fun checkForUpdates(isManualCheck: Boolean = false) {
        _updateInfo.value = _updateInfo.value.copy(isChecking = true, errorMessage = null)
        withContext(Dispatchers.IO) {
            try {
                val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
                val url = URL(apiUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "iGames-Android-App")
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    val tagName = json.optString("tag_name", "")
                    val releaseNotes = json.optString("body", "暂无更新说明")
                    val publishedAt = json.optString("published_at", "")

                    // Look for APK in assets
                    val assets = json.optJSONArray("assets")
                    var downloadUrl = ""
                    var apkFileName = "iGames-release.apk"

                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk")) {
                                downloadUrl = asset.optString("browser_download_url", "")
                                apkFileName = name
                                if (name.contains("release")) {
                                    break
                                }
                            }
                        }
                    }

                    val hasNewer = isNewerVersion(CURRENT_VERSION_NAME, tagName)

                    _updateInfo.value = AppUpdateInfo(
                        hasUpdate = hasNewer,
                        latestVersionName = tagName.removePrefix("v").removePrefix("V"),
                        latestTag = tagName,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        apkFileName = apkFileName,
                        publishedAt = publishedAt,
                        isChecking = false,
                        errorMessage = if (!hasNewer && isManualCheck) "当前已是最新版本 ($CURRENT_VERSION_NAME)" else null
                    )
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    _updateInfo.value = _updateInfo.value.copy(
                        isChecking = false,
                        errorMessage = if (isManualCheck) "尚未发布 Release 版本" else null
                    )
                } else {
                    _updateInfo.value = _updateInfo.value.copy(
                        isChecking = false,
                        errorMessage = if (isManualCheck) "检查更新失败 (HTTP $responseCode)" else null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check update", e)
                _updateInfo.value = _updateInfo.value.copy(
                    isChecking = false,
                    errorMessage = if (isManualCheck) "网络连接异常，请检查网络" else null
                )
            }
        }
    }

    suspend fun downloadApk(downloadUrl: String, fileName: String = "iGames-update.apk") {
        if (downloadUrl.isBlank()) {
            _downloadStatus.value = DownloadStatus.Failed("下载链接无效")
            return
        }

        _downloadStatus.value = DownloadStatus.Downloading(0)

        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            try {
                var currentUrl = downloadUrl
                // Handle GitHub redirects (302)
                var redirectCount = 0
                while (redirectCount < 5) {
                    val url = URL(currentUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        connectTimeout = 15000
                        readTimeout = 15000
                        setRequestProperty("User-Agent", "iGames-Android-App")
                    }
                    val code = connection.responseCode
                    if (code == HttpURLConnection.HTTP_MOVED_TEMP ||
                        code == HttpURLConnection.HTTP_MOVED_PERM ||
                        code == 307 || code == 308) {
                        currentUrl = connection.getHeaderField("Location")
                        connection.disconnect()
                        redirectCount++
                    } else {
                        break
                    }
                }

                if (connection?.responseCode != HttpURLConnection.HTTP_OK) {
                    _downloadStatus.value = DownloadStatus.Failed("下载服务器响应失败: ${connection?.responseCode}")
                    return@withContext
                }

                val contentLength = connection.contentLength
                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.cacheDir
                val apkFile = File(downloadDir, fileName)
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                inputStream = connection.inputStream
                outputStream = FileOutputStream(apkFile)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes: Long = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                    if (contentLength > 0) {
                        val progress = ((totalBytes * 100) / contentLength).toInt().coerceIn(0, 100)
                        _downloadStatus.value = DownloadStatus.Downloading(progress)
                    }
                }
                outputStream.flush()

                _downloadStatus.value = DownloadStatus.Completed(apkFile.absolutePath)
            } catch (e: Exception) {
                Log.e(TAG, "Download APK failed", e)
                _downloadStatus.value = DownloadStatus.Failed("下载中断: ${e.message}")
            } finally {
                try {
                    outputStream?.close()
                    inputStream?.close()
                    connection?.disconnect()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun installApk(apkPath: String) {
        val file = File(apkPath)
        if (!file.exists()) {
            _downloadStatus.value = DownloadStatus.Failed("安装包文件不存在")
            return
        }

        try {
            // Check unknown sources installation permission on Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val hasInstallPermission = context.packageManager.canRequestPackageInstalls()
                if (!hasInstallPermission) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Install APK failed", e)
            _downloadStatus.value = DownloadStatus.Failed("唤起安装失败: ${e.message}")
        }
    }

    fun dismissUpdate() {
        _updateInfo.value = _updateInfo.value.copy(hasUpdate = false, errorMessage = null)
        _downloadStatus.value = DownloadStatus.Idle
    }
}
