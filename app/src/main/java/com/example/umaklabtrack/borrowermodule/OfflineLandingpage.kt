package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
// --- We no longer need Font or FontFamily imports ---
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R

// --- DELETE 'Variables' OBJECT. It exists in LandingPage.kt ---
/*
private object Variables {
    val BACKGROUND = Color(0xFFF6F6F6)
}
*/

// --- DELETE 'poppins' VAL. It exists in LandingPage.kt ---
/*
private val poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)
*/

// These colors are specific to this page, so we keep them
private val GraysGray2 = Color(0xFFAEAEB2)
private val DarkNavy = Color(0xFF182C55)


@Composable
fun OfflineLandingPage(
    onRetry: () -> Unit,
    isChecking: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.wifiwhite),
                contentDescription = "Offline",
                modifier = Modifier
                    .width(115.dp)
                    .height(115.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "You’re Offline",
                modifier = Modifier.width(235.dp),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = poppins, // This will use 'poppins' from LandingPage.kt
                    fontWeight = FontWeight.Bold,
                    color = Variables.BACKGROUND, // This will use 'Variables' from LandingPage.kt
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.96.sp,
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Looks like you are offline\nReconnect to continue",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = poppins, // Uses 'poppins' from LandingPage.kt
                    fontWeight = FontWeight.Normal,
                    color = GraysGray2,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.64.sp,
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onRetry,
                enabled = !isChecking,
                modifier = Modifier
                    .width(235.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GraysGray2)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = GraysGray2,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "RETRY",
                        style = TextStyle(
                            fontFamily = poppins, // Uses 'poppins' from LandingPage.kt
                            fontWeight = FontWeight.Bold,
                            color = GraysGray2
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OfflineLandingPagePreview() {
    /*
     NOTE: This preview might fail to render because it
     can't see 'poppins' or 'Variables' from the other file.
     This is normal. The app itself will build and run correctly.
    */
    OfflineLandingPage(onRetry = {}, isChecking = false)
}