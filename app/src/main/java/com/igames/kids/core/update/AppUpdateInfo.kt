package com.igames.kids.core.update

enum class UpdateChannel(val title: String, val desc: String) {
    AUTO("🚀 智能高速（推荐）", "优先尝试国内主流镜像，失败自动平滑重试"),
    GHPROXY("⚡ GHProxy 镜像", "通过 ghproxy.net 国内反向代理加速"),
    DIRECT("🌐 官方原版直连", "适合身处海外或开启了网络加速工具的用户"),
    CUSTOM("🛠️ 自定义代理", "使用自行配置的加速镜像前缀")
}

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
