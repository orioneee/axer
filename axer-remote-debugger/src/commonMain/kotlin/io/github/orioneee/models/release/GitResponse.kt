package io.github.orioneee.models.release

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GitResponse(
    val assets: List<Asset>,
    @SerialName("assets_url")
    val assetsUrl: String,
    val author: Author,
    val body: String,
    @SerialName("created_at")
    val createdAt: String,
    val draft: Boolean,
    @SerialName("html_url")
    val htmlUrl: String,
    val id: Int,
    val immutable: Boolean,
    @SerialName("node_id")
    val nodeId: String,
    val prerelease: Boolean,
    @SerialName("published_at")
    val publishedAt: String,
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("tarball_url")
    val tarballUrl: String,
    @SerialName("target_commitish")
    val targetCommitish: String,
    @SerialName("upload_url")
    val uploadUrl: String,
    val url: String,
    @SerialName("zipball_url")
    val zipballUrl: String
)
