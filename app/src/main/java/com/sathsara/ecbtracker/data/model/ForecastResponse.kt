package com.sathsara.ecbtracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    val projected_bill: Double = 0.0,
    val efficiency_rating: String = "Medium",
    val peak_hours: String = "",
    val tips: List<ForecastTip> = emptyList(),
)

@Serializable
data class ForecastTip(
    val title: String = "",
    val description: String = "",
    val saving_lkr: Double = 0.0,
)
