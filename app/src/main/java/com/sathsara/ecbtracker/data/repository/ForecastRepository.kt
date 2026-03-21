package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.model.ForecastResponse
import com.sathsara.ecbtracker.data.service.GeminiService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForecastRepository @Inject constructor(
    private val geminiService: GeminiService,
    private val entryRepository: EntryRepository,
    private val settingsRepository: SettingsRepository
) {
    private val json = Json { prettyPrint = false }

    suspend fun getMonthlyForecast(yearMonth: String): Result<ForecastResponse> {
        return Result.runCatching {
            // Get entries for current month
            val entriesResult = entryRepository.getEntriesForMonth(yearMonth)
            val entries = entriesResult.getOrNull() ?: emptyList()
            
            // Get rate
            val settingsResult = settingsRepository.getSettings()
            val lkrPerUnit = settingsResult.getOrNull()?.lkrPerUnit ?: 32.0 // Fallback

            // We only need a simplified version to send to the prompt to save tokens
            val simplifiedEntries = entries.map {
                mapOf(
                    "date" to it.date,
                    "time" to it.time,
                    "used_kwh" to it.used,
                    "appliances" to (it.appliances ?: emptyList())
                )
            }
            
            val entriesJson = json.encodeToString(simplifiedEntries)
            
            // Call Gemini
            geminiService.getForecast(entriesJson, lkrPerUnit).getOrThrow()
        }
    }
}
