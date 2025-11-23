package com.example.umaklabtrack.borrowermodule

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins

// 1. Data Model
data class NotificationItem(
    val sender: String,
    val message: String,
    val isSystem: Boolean = false
)

@Composable
fun NotificationPage(
    onNavSelected: (String) -> Unit = {}
) {
    // 2. DATA PREPARATION
    val nov11Notifications = listOf(
        NotificationItem("AdminName", "Your request has been approved. Please proceed to the laboratory to claim your items."),
        NotificationItem("AdminName", "Your reservation request has been approved."),
        NotificationItem("AdminName", "Your loan request has been approved."),
        NotificationItem("AdminName", "Your borrow request has been rejected."),
        // UPDATED: Use the pending message
        NotificationItem("System", "Your request is now pending approval.")
    )

    val nov10Notifications = listOf(
        NotificationItem("AdminName", "Your borrow request has been rejected."),
        NotificationItem("AdminName", "Your reservation request has been rejected."),
        NotificationItem("AdminName", "Your loan request has been rejected."),
        NotificationItem("System", "Your request is now pending approval.")
    )

    val groupedNotifications = mapOf(
        "November 11, 2025" to nov11Notifications,
        "November 10, 2025" to nov10Notifications,
    )

    // 3. STATE FOR COLLAPSING/EXPANDING
    // We use a Map to track which date strings are expanded.
    // True = Open, False = Closed.
    val expandedState = remember {
        // Initialize all dates to TRUE (Open) by default
        mutableStateMapOf<String, Boolean>().apply {
            groupedNotifications.keys.forEach { put(it, true) }
        }
    }

    Scaffold(
        topBar = { TopHeaderBar() },
        bottomBar = {
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

            Text(
                text = "Notifications",
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = AppColors.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedNotifications.forEach { (date, notifications) ->

                    // Get current state (default to true if not found)
                    val isExpanded = expandedState[date] ?: true

                    // A. Header (Clickable)
                    item {
                        DateHeader(
                            date = date,
                            isExpanded = isExpanded,
                            onToggle = {
                                // Toggle the boolean value for this date
                                expandedState[date] = !isExpanded
                            }
                        )
                    }

                    // B. Render items ONLY if expanded
                    if (isExpanded) {
                        items(notifications) { notification ->
                            NotificationCardItem(notification)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // C. Spacer between groups
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(50.dp)) }
            }
        }
    }
}

@Composable
fun DateHeader(
    date: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    // Simple animation to rotate the arrow
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            // Make the whole row clickable, removing the ripple effect if you want it cleaner
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Removes the gray click splash (optional)
            ) { onToggle() }
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Expand/Collapse",
            tint = Color.Black,
            modifier = Modifier
                .size(24.dp)
                .rotate(rotationState) // Apply rotation animation
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = date,
            fontFamily = poppins,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NotificationCardItem(notification: NotificationItem) {
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
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = notification.sender,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AppColors.TextDark
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF555555),
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