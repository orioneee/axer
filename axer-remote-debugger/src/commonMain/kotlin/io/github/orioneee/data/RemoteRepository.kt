package io.github.orioneee.data

interface RemoteRepository {
    suspend fun getLatestGitTag(): Result<String>
}