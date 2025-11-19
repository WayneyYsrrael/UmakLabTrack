package com.example.umaklabtrack.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// --- GLOBAL COLOR VARIABLES ---
object AppColors {
    // Colors used in LandingPage
    val BackgroundLight = Color(0xFFF6F6F6)   // Faint white/light color
    val SecondaryGold = Color(0xFFFFDE59)     // Gold/Yellow accent
    val TextDark = Color(0xFF202020)          // Very Dark Text (Fixes Unresolved reference 'TextDark')

    // Colors used in HomePage
    val PrimaryDarkBlue = Color(0xFF182C55)   // Dark Blue for buttons/icons
    val GoldAccent = Color(0xFFFFCC00)        // Gold/Yellow for Home tab highlight
    val HeaderDarkBlue = Color(0xFF0B1E46)    // Dark Blue for the Header/Top Bar
}

// --- GLOBAL FONT FAMILY ---
val poppins = FontFamily(
    // FIX: Using the fully qualified path resolves the R.font Unresolved reference error.
    Font(com.example.umaklabtrack.R.font.poppins_regular, weight = FontWeight.Normal),
    Font(com.example.umaklabtrack.R.font.poppins_medium, weight = FontWeight.Medium),
    Font(com.example.umaklabtrack.R.font.poppins_semibold, weight = FontWeight.SemiBold),
    Font(com.example.umaklabtrack.R.font.poppins_bold, weight = FontWeight.Bold)
)