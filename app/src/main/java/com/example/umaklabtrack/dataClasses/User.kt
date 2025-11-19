package com.example.umaklabtrack.dataClasses

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
@Serializable
data class User(
    val id: String? = null,
    val name: String,
    val email: String,
    val contact:String,
    @SerialName("hashed_password") val hashedPassword: String
)