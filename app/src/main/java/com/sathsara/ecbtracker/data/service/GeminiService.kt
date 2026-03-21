package com.sathsara.ecbtracker.data.service

import com.sathsara.ecbtracker.BuildConfig
import com.sathsara.ecbtracker.data.model.ForecastResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }
    
    // We intentionally build the client fresh or use a simple singleton to handle Ktor
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"

    suspend fun getForecast(entriesJson: String, lkrPerUnit: Double): Result<ForecastResponse> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val prompt = """
                You are an energy analyst. The user has logged the following electricity 
                readings this month in Sri Lanka (CEB utility): $entriesJson.
                Their rate is LKR $lkrPerUnit per kWh. 
                Respond ONLY in JSON with this structure:
                {
                  "projected_bill": number,
                  "efficiency_rating": "Low"|"Medium"|"High",
                  "peak_hours": "string",
                  "tips": [
                    { "title": "string", "description": "string", "saving_lkr": number }
                  ]
                }
            """.trimIndent()

            val requestBody = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json"
                )
            )

            val response: GeminiResponse = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val textResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from Gemini")
                
            // Clean up backticks if Gemini includes them
            val cleanJson = textResponse.removePrefix("```json").removeSuffix("```").trim()

            json.decodeFromString<ForecastResponse>(cleanJson)
        }
    }
}

// Gemini specific models for the request/response

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig
)

@Serializable
private data class GenerationConfig(
    val responseMimeType: String
)

@Serializable
private data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiPart(
    val text: String
)

@Serializable
private data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
private data class Candidate(
    val content: GeminiContentResponse
)

@Serializable
private data class GeminiContentResponse(
    val parts: List<GeminiPart>
)
