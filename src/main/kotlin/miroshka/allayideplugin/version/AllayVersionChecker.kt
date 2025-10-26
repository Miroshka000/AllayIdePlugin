package miroshka.allayideplugin.version

import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object AllayVersionChecker {
    private val LOG = Logger.getInstance(AllayVersionChecker::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    
    private const val MAVEN_CENTRAL_SEARCH_URL = "https://search.maven.org/solrsearch/select?q=g:org.allaymc.allay+AND+a:api&rows=1&wt=json"
    
    fun getLatestVersion(): String? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(MAVEN_CENTRAL_SEARCH_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build()
            
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            
            if (response.statusCode() == 200) {
                val searchResponse = json.decodeFromString<MavenSearchResponse>(response.body())
                searchResponse.response.docs.firstOrNull()?.latestVersion
            } else {
                LOG.warn("Failed to fetch latest Allay version: HTTP ${response.statusCode()}")
                null
            }
        } catch (e: Exception) {
            LOG.warn("Failed to fetch latest Allay version", e)
            null
        }
    }
    
    fun compareVersions(current: String, latest: String): VersionComparison {
        val currentParts = parseVersion(current)
        val latestParts = parseVersion(latest)
        
        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val currentPart = currentParts.getOrNull(i) ?: 0
            val latestPart = latestParts.getOrNull(i) ?: 0
            
            when {
                currentPart < latestPart -> return VersionComparison.OUTDATED
                currentPart > latestPart -> return VersionComparison.NEWER
            }
        }
        
        return VersionComparison.SAME
    }
    
    private fun parseVersion(version: String): List<Int> {
        return version
            .removeSuffix("-SNAPSHOT")
            .split(".", "-")
            .mapNotNull { it.toIntOrNull() }
    }
    
    enum class VersionComparison {
        OUTDATED,
        SAME,
        NEWER
    }
    
    @Serializable
    private data class MavenSearchResponse(
        val response: Response
    )
    
    @Serializable
    private data class Response(
        val docs: List<Doc>
    )
    
    @Serializable
    private data class Doc(
        val latestVersion: String
    )
}

