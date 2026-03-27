package com.sathsara.ecbtracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    @SerialName("projected_bill")
    val projectedBill: Double = 0.0,
    @SerialName("efficiency_rating")
    val efficiencyRating: String = "Medium",
    @SerialName("peak_hours")
    val peakHours: String = "",
    val tips: List<ForecastTip> = emptyList(),
)

@Serializable
data class ForecastTip(
    val title: String = "",
    val description: String = "",
    @SerialName("saving_lkr")
    val savingLkr: Double = 0.0,
)
