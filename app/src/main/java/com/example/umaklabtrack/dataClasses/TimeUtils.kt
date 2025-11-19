package com.example.umaklabtrack.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    fun formatTime(time: LocalDateTime, addHours: Boolean = false): String {
        val displayTime = if (addHours) time.plusHours(3) else time
        return displayTime.format(formatter)
    }
}
