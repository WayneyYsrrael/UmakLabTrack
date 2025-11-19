package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
// In HomePage.kt and LandingPage.kt:
import com.example.umaklabtrack.ui.theme.poppins
object Variables {
    val BACKGROUND = Color(0xFFF6F6F6)
    val SECONDARY = Color(0xFFFFDE59)
    val TEXTPRIMARY = Color(0xFF202020)
}

val poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun LandingPage(
    onJoinClick: () -> Unit = {},    onLoginClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF182C55)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.labtracklogo),
                contentDescription = "LabTrack Logo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .width(217.dp)
                    .height(217.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "UMak LabTrack",
                style = TextStyle(
                    fontSize = 40.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Variables.BACKGROUND,
                    textAlign = TextAlign.Center
                )
            )

            Box(
                modifier = Modifier
                    .shadow(4.dp)
                    .width(297.dp)
                    .height(1.dp)
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Simplifying Lab Life",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Variables.SECONDARY,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Know what’s available, reserve what you need, and get\nback to doing science.",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.width(260.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))


            Box(
                modifier = Modifier
                    .width(297.dp)
                    .height(50.dp)
                    .background(
                        color = Variables.SECONDARY,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onJoinClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JOIN THE LAB!",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Variables.TEXTPRIMARY,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.64.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onLoginClick() },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Variables.SECONDARY),
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "I ALREADY HAVE AN ACCOUNT",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Variables.BACKGROUND,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.3.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingPagePreview() {
    LandingPage()
}
