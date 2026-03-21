package com.sathsara.ecbtracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val month: String = "",
    @SerialName("last_units") val lastUnits: Double = 0.0,
    @SerialName("bill_amount") val billAmount: Double = 0.0,
    @SerialName("paid_amount") val paidAmount: Double = 0.0,
    val paid: Boolean = false,
    val bank: String? = null,
    @SerialName("payee_name") val payeeName: String? = null,
    @SerialName("payee_account") val payeeAccount: String? = null,
    val note: String? = null,
)
