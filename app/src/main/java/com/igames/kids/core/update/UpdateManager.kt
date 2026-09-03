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
    var repoOwner: String = "CFM503",
    var repoName: String = "iGames"
) {
    companion object {
        const val CURRENT_VERSION_NAME = "1.0.4"
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

        fun buildMirrorUrls(rawUrl: String, channel: UpdateChannel, customProxy: String): List<String> {
            val cleanRaw = rawUrl.trim()
            if (cleanRaw.isBlank()) return emptyList()

            val jsDelivrUrl = "https://fastly.jsdelivr.net/gh/CFM503/iGames@apk/iGames-release.apk"

            return when (channel) {
                UpdateChannel.AUTO -> listOf(
                    "https://gh-proxy.com/$cleanRaw",
                    "https://ghfast.top/$cleanRaw",
                    jsDelivrUrl,
                    "https://gh.h233.eu.org/$cleanRaw",
                    "https://ghproxy.net/$cleanRaw",
                    cleanRaw
                )
                UpdateChannel.GHPROXY -> listOf(
                    "https://gh-proxy.com/$cleanRaw",
                    "https://ghfast.top/$cleanRaw",
                    jsDelivrUrl,
                    "https://ghproxy.net/$cleanRaw"
                )
                UpdateChannel.DIRECT -> listOf(cleanRaw)
                UpdateChannel.CUSTOM -> {
                    val prefix = if (customProxy.endsWith("/")) customProxy else "$customProxy/"
                    listOf("$prefix$cleanRaw", cleanRaw)
                }
            }
        }
    }

    private val _updateInfo = MutableStateFlow(AppUpdateInfo())
    val updateInfo: StateFlow<AppUpdateInfo> = _updateInfo.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()

    var currentChannel: UpdateChannel = UpdateChannel.AUTO
    var customProxyPrefix: String = "https://ghproxy.net/"

    /**
     * Checks for updates using a multi-channel fallback strategy:
     * 1. jsDelivr Fastly CDN (version.json) - lightning fast in China
     * 2. jsDelivr Cloudflare CDN (version.json)
     * 3. Official GitHub Releases REST API (fallback)
     */
    suspend fun checkForUpdates(isManualCheck: Boolean = false) {
        _updateInfo.value = _updateInfo.value.copy(isChecking = true, errorMessage = null)
        withContext(Dispatchers.IO) {
            val candidateEndpoints = listOf(
                "https://gh-proxy.com/https://raw.githubusercontent.com/$repoOwner/$repoName/main/version.json",
                "https://ghfast.top/https://raw.githubusercontent.com/$repoOwner/$repoName/main/version.json",
                "https://fastly.jsdelivr.net/gh/$repoOwner/$repoName@main/version.json",
                "https://cdn.jsdelivr.net/gh/$repoOwner/$repoName@main/version.json",
                "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            )

            var parsedInfo: AppUpdateInfo? = null
            var lastError: String? = null

            for (endpoint in candidateEndpoints) {
                try {
                    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 6000
                        readTimeout = 6000
                        setRequestProperty("User-Agent", "iGames-Android-App")
                        setRequestProperty("Accept", "application/json")
                    }

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(responseText)

                        if (endpoint.contains("version.json")) {
                            // Format of version.json
                            val tagName = json.optString("tagName", "")
                            val releaseNotes = json.optString("releaseNotes", "新版本日常习惯小游戏与体验优化")
                            val rawDownloadUrl = json.optString("downloadUrl", "")
                            val downloadUrl = if (rawDownloadUrl.isNotBlank()) {
                                rawDownloadUrl
                            } else {
                                "https://github.com/$repoOwner/$repoName/releases/download/$tagName/iGames-release.apk"
                            }
                            val apkFileName = json.optString("apkFileName", "iGames-release.apk")
                            val publishedAt = json.optString("publishedAt", "")
                            val hasNewer = isNewerVersion(CURRENT_VERSION_NAME, tagName)

                            parsedInfo = AppUpdateInfo(
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
                            break
                        } else {
                            // Format of GitHub API
                            val tagName = json.optString("tag_name", "")
                            val releaseNotes = json.optString("body", "暂无更新说明")
                            val publishedAt = json.optString("published_at", "")
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
                                        if (name.contains("release")) break
                                    }
                                }
                            }

                            val hasNewer = isNewerVersion(CURRENT_VERSION_NAME, tagName)
                            parsedInfo = AppUpdateInfo(
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
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Endpoint failed: $endpoint, error: ${e.message}")
                    lastError = e.message
                }
            }

            if (parsedInfo != null) {
                _updateInfo.value = parsedInfo
            } else {
                _updateInfo.value = _updateInfo.value.copy(
                    isChecking = false,
                    errorMessage = if (isManualCheck) "连接更新服务器超时，请检查网络设置 ($lastError)" else null
                )
            }
        }
    }

    /**
     * Downloads APK using multi-mirror fallback (GHProxy -> Alt Mirror -> Direct)
     */
    suspend fun downloadApk(rawDownloadUrl: String, fileName: String = "iGames-update.apk") {
        if (rawDownloadUrl.isBlank()) {
            _downloadStatus.value = DownloadStatus.Failed("下载链接无效")
            return
        }

        val candidateUrls = buildMirrorUrls(rawDownloadUrl, currentChannel, customProxyPrefix)
        _downloadStatus.value = DownloadStatus.Downloading(0)

        withContext(Dispatchers.IO) {
            var downloadSuccess = false
            var finalErrorMessage: String? = null

            for ((index, candidateUrl) in candidateUrls.withIndex()) {
                Log.i(TAG, "Attempting download route [${index + 1}/${candidateUrls.size}]: $candidateUrl")
                var connection: HttpURLConnection? = null
                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    var currentUrl = candidateUrl
                    var redirectCount = 0
                    while (redirectCount < 6) {
                        val url = URL(currentUrl)
                        connection = (url.openConnection() as HttpURLConnection).apply {
                            instanceFollowRedirects = false
                            connectTimeout = 6000
                            readTimeout = 20000
                            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) iGames-App")
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
                        Log.w(TAG, "Route returned non-200 code: ${connection?.responseCode}, trying next...")
                        continue
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

                    val buffer = ByteArray(64 * 1024)
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

                    if (apkFile.length() > 500_000) { // Ensure file isn't an error HTML page
                        _downloadStatus.value = DownloadStatus.Completed(apkFile.absolutePath)
                        downloadSuccess = true
                        break
                    } else {
                        Log.w(TAG, "Downloaded file too small (${apkFile.length()} bytes), likely an error page. Trying next...")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Route failed: $candidateUrl, ${e.message}")
                    finalErrorMessage = e.message
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

            if (!downloadSuccess) {
                _downloadStatus.value = DownloadStatus.Failed("所有加速通道均下载失败，请检查网络或切换代理 ($finalErrorMessage)")
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
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // Explicitly grant URI read permission to package installers for OEM compatibility (Xiaomi, Huawei, Oppo, Vivo)
            try {
                val resInfoList = context.packageManager.queryIntentActivities(installIntent, 0)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (e: Exception) {
                Log.w(TAG, "grantUriPermission warning: ${e.message}")
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
