package io.github.orioneee.data

import io.github.orioneee.models.release.GitResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteRepositoryImpl(
    private val client: HttpClient
) : RemoteRepository {
    override suspend fun getLatestGitTag(): Result<String> {
        val url = "https://api.github.com/repos/orioneee/axer/releases/latest"
        return try {
            val response: GitResponse = client.get(url).body()
            Result.success(response.tag_name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}