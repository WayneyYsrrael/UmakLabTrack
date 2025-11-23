package com.example.umaklabtrack.adminmodule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.poppins

// If AppColors is missing in your theme, we define the specific blue here
val PrimaryBlueColor = Color(0xFF182C55)

// --- DATA CLASSES ---
data class RequestData(
    val id: String,
    val requestorName: String,
    val transactionType: String,
    val dateTime: String,
    val status: String
)

// ==========================================
//           1. MAIN SCREEN LOGIC
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsAdminPage(
    onNavSelected: (String) -> Unit = {},
    requests: List<RequestData> = listOf(
        RequestData("1", "Prof. Susan Guevarra", "Borrow", "11/10/25 • 2:00PM", "Requesting Approval")
    )
) {
    // --- STATE VARIABLES ---
    var currentSelectedIndex by remember { mutableStateOf(1) }
    var showNewRequestBanner by remember { mutableStateOf(requests.isNotEmpty()) }

    var showRequestDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<RequestData?>(null) }

    // Auto-hide banner logic
    LaunchedEffect(Unit) {
        if (showNewRequestBanner) {
            kotlinx.coroutines.delay(5000)
            showNewRequestBanner = false
        }
    }

    Scaffold(
        topBar = { null },
        bottomBar = {
            AdminBottomReqNavigationBar(
                selectedIndex = currentSelectedIndex,
                onItemSelected = { index ->
                    currentSelectedIndex = index
                    when (index) {
                        0 -> onNavSelected("dashboard")
                        1 -> onNavSelected("requests")
                        2 -> onNavSelected("notifications")
                        3 -> onNavSelected("history")
                        4 -> onNavSelected("profile")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            // --- Layer 1: Main Content ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(110.dp)) // Space for Header

                Text(
                    text = "Requests",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (requests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You have no pending requests.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(requests) { request ->
                            RequestCard(
                                data = request,
                                onViewClick = {
                                    selectedRequest = request
                                    showRequestDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // --- Layer 2: Header Bar ---
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
                AdminTopHeaderBar()
            }

            // --- Layer 3: Notification Banner ---
            AnimatedVisibility(
                visible = showNewRequestBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                    .zIndex(1f)
            ) {
                if (requests.isNotEmpty()) {
                    SystemNotificationBanner(
                        message = "${requests.first().requestorName} submitted a ${requests.first().transactionType.lowercase()} request. Review it now."
                    )
                }
            }

            // --- Layer 4: THE POP-UP DIALOG ---
            if (showRequestDialog && selectedRequest != null) {

                // 1. UPDATED DATA with exact names requested
                val borrowData = BorrowingDetails(
                    professorName = selectedRequest!!.requestorName,
                    items = listOf(
                        "(1) ItemName1",
                        "(2) ItemName2",
                        "(3) ItemName3",
                        "(2) ItemName4",
                        "(1) ItemName5",
                        "(2) ItemName6"
                    ),
                    subject = "Chemistry",
                    college = "Institute of Pharmacy (IOP)",
                    yearSection = "II - A BSP",
                    studentReps = listOf(
                        "Villarica, Amrafel Marcus B.",
                        "Canlas, Emerson B.",
                        "Elado, Wayne Ysrael C.",
                        "Picao, Mark Kevin N."
                    ),
                    borrowDate = "November 10, 2025 - 2:00 PM",
                    returnDate = "November 10, 2025 - 5:00 PM"
                )

                // 2. Call the Dialog
                BorrowingInfoSlipDialog(
                    data = borrowData,
                    onDismiss = { showRequestDialog = false },
                    onMarkAsPrepared = {
                        // TODO: Add logic for 'Prepared'
                        showRequestDialog = false
                    },
                    onMarkAsClaimed = {
                        // TODO: Add logic for 'Claimed'
                        showRequestDialog = false
                    }
                )
            }
        }
    }
}

// ==========================================
//           2. OTHER COMPONENTS
// ==========================================

@Composable
fun SystemNotificationBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "System",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.LightGray, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("System", fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(message, fontFamily = poppins, fontSize = 13.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun RequestCard(data: RequestData, onViewClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(data.requestorName, fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Transaction: ${data.transactionType}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                Text("Date & Time: ${data.dateTime}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)

                // --- UPDATED TEXT WITH MIXED COLORS ---
                // "Status:" is Gray, Value is Blue
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Gray)) {
                            append("Status: ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF1288BF), fontWeight = FontWeight.SemiBold)) {
                            append(data.status)
                        }
                    },
                    fontFamily = poppins,
                    fontSize = 12.sp
                )
            }
            Button(onClick = onViewClick, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor), shape = RoundedCornerShape(10.dp)) {
                Text("View", fontFamily = poppins, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopHeaderBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().background(PrimaryBlueColor).statusBarsPadding().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("UMak LabTrack (Admin)", style = TextStyle(fontSize = 20.sp, fontFamily = poppins, fontWeight = FontWeight(600), color = Color.White))
    }
}

// ⬇️ CONSISTENT IMPLEMENTATION STARTS HERE ⬇️

data class AdminNavItem(val label: String, val iconResId: Int)

@Composable
fun AdminBottomReqNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        AdminNavItem("Dashboard", R.drawable.dashboard),
        AdminNavItem("Requests", R.drawable.requestcheck),
        AdminNavItem("Notifications", R.drawable.notif),
        AdminNavItem("History", R.drawable.logs),
        AdminNavItem("Profile", R.drawable.profilenav)
    )

    // Using Column + Row structure to fix disappearance
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp)
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
}

@Preview(showSystemUi = true)
@Composable
fun RequestsAdminPagePreview() {
    RequestsAdminPage()
}