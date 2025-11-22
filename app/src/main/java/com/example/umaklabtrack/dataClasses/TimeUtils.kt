package com.example.umaklabtrack.utils

import java.util.Locale

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField



object TimeUtils {

    private val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy - h:mm a")

    @Composable
    fun rememberCurrentTime(addHours: Boolean = false): String {
        var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

        LaunchedEffect(Unit) {
            while (true) {
                currentTime = LocalDateTime.now()
                delay(1000L) // Updates every second, but format ignores seconds
            }
        }

        val displayTime = if (addHours) currentTime.plusHours(3) else currentTime
        return displayTime.format(formatter)
    }

    fun formatTimestamp(input: String): String {
        // Parse the ISO-8601 string
        val parsedDate = LocalDateTime.parse(input) // works directly with "2025-11-22T16:19:52.287165"

        // Format to desired output
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yy h:mma")
        return parsedDate.format(formatter)
    }





}
