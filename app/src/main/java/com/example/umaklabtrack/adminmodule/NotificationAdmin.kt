package com.example.umaklabtrack.adminmodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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

// Admin Notification Data
data class NotificationAdminItem(
    val sender: String,
    val message: String
)

// NEW: For dropdown expand/collapse
data class NotificationSection(
    val date: String,
    val notifications: List<NotificationAdminItem>,
    val expanded: Boolean = true
)

@Composable
fun NotificationAdminPage(
    onNavSelected: (String) -> Unit = {}
) {
    // --- STATE ---
    var selectedIndex by remember { mutableStateOf(2) }

    val nov11AdminExact = listOf(
        NotificationAdminItem("System", "“Professor’s Name” submitted a borrow request. Review it now."),
        NotificationAdminItem("System", "“Professor’s Name” borrowed item is due soon."),
        NotificationAdminItem("System", "“Professor’s Name” borrowed item is overdue."),
        NotificationAdminItem("System", "An item is ready for scanning upon return.")
    )

    val nov10AdminExact = listOf(
        NotificationAdminItem("System", "“Professor’s Name” borrowed item is due soon."),
        NotificationAdminItem("System", "“Professor’s Name” borrowed item is overdue."),
        NotificationAdminItem("System", "An item is ready for scanning upon return."),
        NotificationAdminItem("System", "“Professor’s Name” transaction has been marked as returned.")
    )

    val groupedAdminNotifications = mapOf(
        "November 11, 2025" to nov11AdminExact,
        "November 10, 2025" to nov10AdminExact
    )

    // NEW: Convert to section states
    val adminSections = remember {
        groupedAdminNotifications.map { (date, list) ->
            mutableStateOf(NotificationSection(date, list, true))
        }
    }

    Scaffold(
        topBar = { AdminHeaderBar() },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notifications",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                adminSections.forEach { sectionState ->
                    val section = sectionState.value

                    // HEADER (CLICKABLE)
                    item {
                        AdminDateHeader(
                            date = section.date,
                            expanded = section.expanded,
                            onClick = {
                                sectionState.value = section.copy(expanded = !section.expanded)
                            }
                        )
                    }

                    // SHOW ONLY IF EXPANDED
                    if (section.expanded) {
                        items(section.notifications) { notif ->
                            AdminNotificationCardItem(notif)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@Composable
fun AdminDateHeader(
    date: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Expand",
            tint = Color.Black,
            modifier = Modifier
                .size(24.dp)
                .rotate(if (expanded) 0f else -90f) // NEW ROTATION
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = date,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        )
    }
}

@Composable
fun AdminNotificationCardItem(notification: NotificationAdminItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = notification.sender,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF333333),
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationAdminPreview() {
    NotificationAdminPage()
}
