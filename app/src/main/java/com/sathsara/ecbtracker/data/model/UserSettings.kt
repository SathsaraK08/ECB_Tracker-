package com.sathsara.ecbtracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    @SerialName("user_id") val userId: String = "",
    @SerialName("lkr_per_unit") val lkrPerUnit: Double = 0.0,
    @SerialName("account_number") val accountNumber: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
)
