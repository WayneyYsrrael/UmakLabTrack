package com.example.umaklabtrack.dataClasses
import kotlinx.serialization.Serializable

@Serializable
enum class AvailabilityStatus {
    Available,
    InUse,
    Damaged,
    Lost,
    Archived
}


@Serializable
data class Items(
    val item_id:Int,
    val name: String,
    val status: AvailabilityStatus,
    val quantity: Int,
    val description: String,
    val category: String,
    val isForLoan: Boolean
)

@Serializable
data class SelectedItem(
    val id: Int,
    val name: String,
    val quantity: Int
)

