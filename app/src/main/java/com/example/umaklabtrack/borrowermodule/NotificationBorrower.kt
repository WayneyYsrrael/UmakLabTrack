package com.example.umaklabtrack.borrowermodule

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins

// 1. Data Model
data class NotificationItem(
    val sender: String, // e.g., "AdminName" or "System"
    val message: String,
    val isSystem: Boolean = false // You can use this to change icon logic if needed later
)

@Composable
fun NotificationPage(
    onNavSelected: (String) -> Unit = {}
) {
    // 2. PREPARING THE DATA
    // I extracted these texts exactly from your second image to match the design.

    val nov11Notifications = listOf(
        NotificationItem("AdminName", "Your request has been approved. Please proceed to the laboratory to claim your items."),
        NotificationItem("AdminName", "Your reservation request has been approved."),
        NotificationItem("AdminName", "Your loan request has been approved."),
        NotificationItem("AdminName", "Your borrow request has been rejected.")
    )

    val nov10Notifications = listOf(
        NotificationItem("AdminName", "Your borrow request has been rejected."),
        NotificationItem("AdminName", "Your reservation request has been rejected."),
        NotificationItem("AdminName", "Your loan request has been rejected."),
        NotificationItem("System", "Your request is now pending approval.")
    )

    // Extra examples from your second image (Available for use)
    val otherExamples = listOf(
        NotificationItem("System", "Your borrowed equipment is due later today. Please return on time."),
        NotificationItem("System", "Overdue Alert: You still have items that need to be returned."),
        NotificationItem("AdminName", "Items has been successfully returned in good condition. Thank you!"),
        NotificationItem("AdminName", "Condition Update: One of your returned items was marked as damaged."),
        NotificationItem("AdminName", "A borrowed item is reported lost or missing. Please coordinate with the laboratory."),
        NotificationItem("AdminName", "Damage Report: Please settle the replacement for the damaged item."),
        NotificationItem("System", "Reminder: You have a scheduled reservation today."),
        NotificationItem("AdminName", "Your rescheduling request has been approved."),
        NotificationItem("AdminName", "Your rescheduling request has been rejected.")
    )

    // 3. GROUPING LOGIC
    // This Map prevents "doubling". Key is the Date, Value is the List of notifications for that date.
    val groupedNotifications = mapOf(
        "November 11, 2025" to nov11Notifications,
        "November 10, 2025" to nov10Notifications,
        // You can add "Earlier" here with the 'otherExamples' list if you want
    )

    Scaffold(
        topBar = { TopHeaderBar() }, // Reuses the Header from Home.kt
        bottomBar = {
            // "notifications" route triggers the yellow active state
            BottomNavigationBar(selectedRoute = "notifications", onNavSelected = onNavSelected)
        },
        containerColor = Color.White
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Page Title
            Text(
                text = "Notifications",
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp, // Slightly larger title
                color = AppColors.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. LIST RENDERING
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items
            ) {
                // Iterate through the Map.
                // key = Date String, value = List of items
                groupedNotifications.forEach { (date, notifications) ->

                    // A. Render the Date Header ONCE per group
                    item {
                        DateHeader(date = date)
                    }

                    // B. Render the cards for that date
                    items(notifications) { notification ->
                        NotificationCardItem(notification)
                        Spacer(modifier = Modifier.height(4.dp)) // Small gap between cards
                    }

                    // C. Add a little extra space after a group finishes
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(50.dp)) // Bottom padding for scrolling
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* Optional: Collapse logic could go here */ }
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Expand",
            tint = Color.Black, // Darker arrow as per screenshot
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = date,
            fontFamily = poppins,
            fontSize = 14.sp,
            color = Color.Black, // Dark text for date
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NotificationCardItem(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), // Very light gray/white background
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)), // Subtle border
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Image(
                painter = painterResource(id = R.drawable.profile), // Ensure this drawable exists
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Content
            Column {
                Text(
                    text = notification.sender,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold, // Bold Name
                    fontSize = 14.sp,
                    color = AppColors.TextDark
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF555555), // Dark Gray text
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun NotificationPagePreview() {
    NotificationPage()
}