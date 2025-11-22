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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.poppins
import androidx.compose.foundation.layout.navigationBarsPadding

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

// Define SelectedItem which was missing in the original file
data class SelectedRequestItem(
    val id: String,
    val name: String,
    val type: String,
    val quantity: Int
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
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<RequestData?>(null) }

    // Mock items to show inside the dialog
    val mockItems = remember {
        listOf(
            SelectedItem("a", "Item Name", "Apparatus", 1),
            SelectedItem("b", "Item Name", "Chemicals", 2),
            SelectedItem("c", "Item Name", "Slides", 3)
        )
    }

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
                SelectedItemsRequestDialog(
                    selectedItems = mockItems,
                    onDismiss = { showRequestDialog = false },
                    onApprove = { showRequestDialog = false },
                    onReject = {
                        // Open the reject reason dialog instead of just closing
                        showRejectDialog = true
                    }
                )
            }

            // --- Layer 5: REJECT REASON DIALOG ---
            if (showRejectDialog) {
                RejectReasonDialog(
                    onDismiss = { showRejectDialog = false },
                    onConfirm = { reason ->
                        // Handle the rejection reason here
                        println("Rejected because: $reason") // Replace with API call or state update
                        showRejectDialog = false
                        showRequestDialog = false
                    }
                )
            }
        }
    }
}


// ==========================================
//           2. NEW DIALOG DESIGN
// ==========================================
@Composable
fun SelectedItemsRequestDialog(
    selectedItems: List<SelectedItem>,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val RedColor = Color(0xFFE53935)
    val DividerColor = PrimaryBlueColor

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected Items",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueColor
                            )
                        )
                        Text(
                            text = "Borrowing",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp)
                            .size(24.dp)
                            .clickable { onDismiss() },
                        tint = Color.Black
                    )
                }

                HorizontalDivider(thickness = 1.5.dp, color = DividerColor, modifier = Modifier.padding(bottom = 8.dp))

                // LIST
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedItems) { item ->
                        ItemRequestDetailCard(item = item)
                    }
                }

                // FOOTER
                HorizontalDivider(thickness = 1.5.dp, color = DividerColor, modifier = Modifier.padding(top = 8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Approve", fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }

                    Button(
                        onClick = onReject,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, RedColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reject", fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RedColor)
                    }
                }
            }
        }
    }
}

// --- ITEM CARD HELPER ---
@Composable
fun ItemRequestDetailCard(item: SelectedItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                Text(text = "Type: ${item.type}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = "Qty: ${item.quantity}",
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

// ==========================================
//           3. OTHER COMPONENTS
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
                Text("Status: ${data.status}", fontFamily = poppins, fontSize = 12.sp, color = Color(0xFF1288BF), fontWeight = FontWeight.SemiBold)
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

// NOTE: This data class is now present in *both* files.
data class AdminNavItem(val label: String, val iconResId: Int)

// NOTE: This AdminBottomNavigationBar implementation is now present
// in *both* HomeAdminPage.kt and RequestsAdminPage.kt for consistency.
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

// ⬆️ CONSISTENT IMPLEMENTATION ENDS HERE ⬆️
@Composable
fun RejectReasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            // 1. The "White Box" Dialog Dimensions
            modifier = Modifier
                .width(336.dp)
                .height(279.dp),
            // Using light gray so the white input field is visible (matches your screenshot)
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F4))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp), // Vertical padding to space things out
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // Distributes items evenly
            ) {
                // 2. Header Text (16sp)
                Text(
                    text = "Please state the reason for\nrejecting the request.",
                    style = TextStyle(
                        fontSize = 16.sp, // Requested size
                        fontFamily = poppins,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                )

                // 3. Text Input for Rejection (299.dp x 34.dp)
                Box(
                    modifier = Modifier
                        .width(299.dp)
                        .height(50        .dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(8.dp),
                            clip = false
                        )
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp), // Inner padding for text
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = poppins,
                            fontSize = 14.sp, // Input text size fits better at 14sp inside a 34dp box
                            color = Color.Black
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Black)
                    )
                }

                // 4. Buttons (300.dp x 50.dp, Text 16sp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Confirm Button
                    Button(
                        onClick = { onConfirm(reason) },
                        modifier = Modifier
                            .width(300.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Confirm Rejection",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, // Requested size
                            color = Color.White
                        )
                    }

                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .width(300.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEF5350)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel Rejection",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, // Requested size
                            color = Color(0xFFEF5350)
                        )
                    }
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