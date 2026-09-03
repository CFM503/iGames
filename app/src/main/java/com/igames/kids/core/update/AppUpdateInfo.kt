package com.igames.kids.core.update

data class AppUpdateInfo(
    val hasUpdate: Boolean = false,
    val latestVersionName: String = "",
    val latestTag: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val apkFileName: String = "iGames-release.apk",
    val publishedAt: String = "",
    val isChecking: Boolean = false,
    val errorMessage: String? = null
)

sealed class DownloadStatus {
    data object Idle : DownloadStatus()
    data class Downloading(val progress: Int) : DownloadStatus() // 0 to 100
    data class Completed(val apkPath: String) : DownloadStatus()
    data class Failed(val reason: String) : DownloadStatus()
}
