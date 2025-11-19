package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
// --- 1. ADD these imports ---
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
// --- (End of new imports) ---
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineHomePage(
    currentRoute: String,
    onRetry: () -> Unit,
    onNavSelected: (String) -> Unit,
    isChecking: Boolean = false // --- 2. ADD this parameter ---
) {
    Scaffold(
        topBar = { TopHeaderBar() },
        bottomBar = { BottomNavigationBar(selectedRoute = currentRoute, onNavSelected = onNavSelected) },
        containerColor = Color.White
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.wifiblack),
                contentDescription = "Offline",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "You're Offline",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight(700),
                    color = AppColors.TextDark
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Looks like you are offline\nReconnect to continue",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight(400),
                    color = Color.Gray
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. REPLACE ActionButton with a custom Button ---
            Button(
                onClick = onRetry,
                enabled = !isChecking, // Disable button while checking
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDarkBlue),
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(size = 10.dp)
            ) {
                if (isChecking) {
                    // Show spinner
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White, // Spinner color
                        strokeWidth = 2.dp
                    )
                } else {
                    // Show text
                    Text(
                        "RETRY",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        fontFamily = poppins
                    )
                }
            }
        }
    }
}

// (TopHeaderBar and ActionButton composables are still in HomePage.kt)
// (We deleted them from this file in a previous step)

@Preview(showSystemUi = true)
@Composable
fun OfflineHomePagePreview() {
    OfflineHomePage(
        currentRoute = "home",
        onRetry = {},
        onNavSelected = {},
        isChecking = false // Add isChecking for preview
    )
}