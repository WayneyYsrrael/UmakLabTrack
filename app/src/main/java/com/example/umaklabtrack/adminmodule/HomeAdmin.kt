package com.example.umaklabtrack.adminmodule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAdminPage(
    adminName: String = "Prof. FirstName",
    onNavSelected: (String) -> Unit = {}
) {
    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { AdminTopHeaderBar() },
        bottomBar = {
            AdminBottomNavigationBar(
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    selectedIndex = index
                    when (index) {
                        0 -> onNavSelected("dashboard")
                        1 -> onNavSelected("requests")
                        2 -> onNavSelected("notifications")
                        3 -> onNavSelected("logs")
                        4 -> onNavSelected("profile")
                    }
                }

            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.labtracklogo),
                    contentDescription = "UMak Logo",
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Welcome, $adminName!",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "How can we help you today?",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AdminHeaderBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.PrimaryDarkBlue)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "UMak LabTrack (Admin)",
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

// NOTE: This AdminBottomNavigationBar implementation is now present
// in *both* HomeAdminPage.kt and RequestsAdminPage.kt for consistency.
@Composable
fun AdminBottomNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit  // Remove @Composable here
) {
    val items = listOf(
        AdminNavItem("Dashboard", R.drawable.dashboard),
        AdminNavItem("Requests", R.drawable.requestcheck),
        AdminNavItem("Notifications", R.drawable.notif),
        AdminNavItem("Logs", R.drawable.logs),
        AdminNavItem("Profile", R.drawable.profilenav)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedIndex == index
            val backgroundColor = if (isSelected) Color(0xFFFFD600) else Color.White

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(backgroundColor)
                    .clickable { onItemSelected(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconResId),
                    contentDescription = item.label,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.label,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontFamily = poppins,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color.Black
                    )
                )
            }
        }
    }
}

// NOTE: This data class is now present in *both* files.
data class AdminHomeNavItem(val label: String, val iconResId: Int)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeAdminPreview() {
    HomeAdminPage()
}