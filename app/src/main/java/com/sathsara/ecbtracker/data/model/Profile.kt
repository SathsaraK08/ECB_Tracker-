package com.sathsara.ecbtracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = "",
    val username: String? = null,
    val mobile: String? = null,
    val ceb_account: String? = null,
)
