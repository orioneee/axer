package io.github.orioneee.models.release

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Asset(
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("content_type")
    val contentType: String,
    @SerialName("created_at")
    val createdAt: String,
    val digest: String,
    @SerialName("download_count")
    val downloadCount: Int,
    val id: Int,
    val label: String? = null,
    val name: String,
    @SerialName("node_id")
    val nodeId: String,
    val size: Int,
    val state: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val uploader: Uploader,
    val url: String
)
