package com.sathsara.ecbtracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Entry(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val date: String = "",
    val time: String = "",
    val unit: Double = 0.0,
    val used: Double = 0.0,
    val note: String? = null,
    val appliances: List<String>? = null,
    @SerialName("img_url") val imgUrl: String? = null,
)
