package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.umaklabtrack.R
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.entityManagement.ItemManage
import com.example.umaklabtrack.entityManagement.ItemManage.BorrowInfoWithId
import com.example.umaklabtrack.entityManagement.ItemManage.TransactedItem
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import com.example.umaklabtrack.utils.TimeUtils
import kotlinx.coroutines.delay

private val itemManage = ItemManage()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogsScreen(
    showToast: Boolean = false,
    onNavSelected: (String) -> Unit = {}
) {
    var selectedReservation by remember { mutableStateOf<BorrowInfoWithId?>(null) }
    var visible by remember { mutableStateOf(showToast) }
    var toastMessage by remember { mutableStateOf("Your request is now pending approval.") }
    var toastUser by remember { mutableStateOf("System") }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog States
    var showReturnedItemsDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    var reservationsWithItems by remember { mutableStateOf<Map<BorrowInfoWithId, List<TransactedItem>>>(emptyMap()) }

    // Load reservations
    LaunchedEffect(Unit) {
        val data = itemManage.getReservationsWithItems()
        reservationsWithItems = data
        isLoading = false
    }

    if (visible) {
        LaunchedEffect(visible, toastMessage) {
            delay(4000L)
            visible = false
        }
    }

    Scaffold(
        // Reverted to your original component name
        bottomBar = { BottomNavigationBar(selectedRoute = "logs", onNavSelected = onNavSelected) },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(110.dp))
                Text(
                    text = "Transaction History",
                    style = TextStyle(
                        fontSize = 24.sp, fontFamily = poppins, fontWeight = FontWeight.Bold,
                        color = AppColors.TextDark.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        reservationsWithItems.forEach { (reservation, items) ->
                            item {
                                ActivityLogItemCardDynamic(
                                    name = UserSession.name ?: "Unknown",
                                    reservation = reservation,
                                    items = items,
                                    onAction = { _ ->
                                        selectedReservation = reservation
                                        visible = true

                                        // Logic for opening dialogs based on status
                                        if (reservation.status == "Pending" || reservation.status == "Preparing") {
                                            showReturnedItemsDialog = true
                                        }
                                        if (reservation.status == "Preparing") {
                                            toastMessage = "Please wait while we prepare your items!"
                                            toastUser = "Admin"
                                            visible = true
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }

            // Reverted to your original component name
            TopHeaderBar(modifier = Modifier.align(Alignment.TopCenter))

            if (visible) {
                SystemNotificationToast(
                    message = toastMessage,
                    user = toastUser,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                        .zIndex(1f)
                )
            }

            // Dialog Logic
            if (showReturnedItemsDialog && selectedReservation != null) {
                ReturnedItemsDialog(
                    onDismiss = {
                        showReturnedItemsDialog = false
                        selectedReservation = null
                    },
                    onNext = {
                        showReturnedItemsDialog = false
                        showDetailsDialog = true
                    },
                    reservation = selectedReservation!!,
                    items = reservationsWithItems[selectedReservation] ?: emptyList()
                )
            }

            if (showDetailsDialog && selectedReservation != null) {
                val reservation = selectedReservation!!
                TransactionDetailsDialog(
                    reservation = reservation,
                    onDismiss = { showDetailsDialog = false },
                    onGoBack = {
                        showDetailsDialog = false
                        showReturnedItemsDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun ActivityLogItemCardDynamic(
    name: String,
    reservation: BorrowInfoWithId,
    items: List<TransactedItem>,
    onAction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    "Transaction: ${reservation.type}",
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    "Date & Time: ${TimeUtils.formatTimestamp(reservation.created_at)}",
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Black)) {
                            append("Status: ")
                        }

                        val statusColor = when (reservation.status) {
                            "toClaim", "claimed", "Pending" -> Color(0xFFFFA500)
                            "Preparing" -> Color(0xFFFFCE3D)
                            "Approved" -> Color(0xFF43A047)
                            "Rejected" -> Color(0xFFD32F2F)
                            "Returned" -> Color(0xFF6A1B9A)
                            else -> Color.Black
                        }

                        withStyle(style = SpanStyle(color = statusColor)) {
                            append(reservation.status)
                        }
                    },
                    fontFamily = poppins,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = { onAction(reservation.status) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("View", fontFamily = poppins, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ReturnedItemsDialog(
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    reservation: BorrowInfoWithId,
    items: List<TransactedItem>
) {
    var returnedItems by remember { mutableStateOf<List<Triple<String, String, Int>>>(emptyList()) }
    val primaryBlue = Color(0xFF182C55)

    LaunchedEffect(items) {
        val list = items.map { transItem ->
            val category = itemManage.getCategory(transItem.item_id, transItem.item_name)
            Triple(
                transItem.item_name,
                category ?: "Unknown",
                transItem.quantity
            )
        }
        returnedItems = list
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // --- HEADER ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selected Items",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppins,
                        color = primaryBlue
                    )
                    // Subtitle
                    Text(
                        text = reservation.type,
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontFamily = poppins
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Top Divider
                Divider(color = primaryBlue, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                // --- END HEADER ---

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(returnedItems) { (name, type, qty) ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Image Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = poppins,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Type: $type",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontFamily = poppins
                                    )
                                }
                                Text(
                                    text = "Qty: $qty",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = poppins,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BOTTOM DIVIDER & BUTTON ---
                Divider(color = primaryBlue, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Next",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsDialog(
    reservation: BorrowInfoWithId,
    onDismiss: () -> Unit,
    onGoBack: () -> Unit
) {
    val primaryBlue = Color(0xFF182C55)
    val closeRed = Color(0xFFFF5252)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- HEADER ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Prof. ${UserSession.name ?: "Unknown"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = poppins,
                        color = primaryBlue
                    )
                    // Subtitle
                    Text(
                        text = reservation.type,
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontFamily = poppins
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = primaryBlue, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                // --- END HEADER ---

                // Subject
                FormSection1(label = "Subject:") {
                    Text(reservation.subject, style = TextStyle(fontSize = 14.sp, fontFamily = poppins))
                }

                // College
                FormSection1(label = "College:") {
                    Text(reservation.college, style = TextStyle(fontSize = 14.sp, fontFamily = poppins))
                }

                // Year & Section
                FormSection1(label = "Year & Section:") {
                    Text(reservation.yr_section, style = TextStyle(fontSize = 14.sp, fontFamily = poppins))
                }

                // Student Representatives
                FormSection1(label = "Student Representatives Name:") {
                    if (reservation.student_representative_names.isEmpty()) {
                        Text("No students added.", color = Color.Gray, fontSize = 14.sp, fontFamily = poppins)
                    } else {
                        Column {
                            reservation.student_representative_names.forEach { student ->
                                Text(student, fontSize = 14.sp, fontFamily = poppins)
                            }
                        }
                    }
                }

                // Borrowing Date & Time
                FormSection1(label = "Borrowing Date & Time:") {
                    Text(TimeUtils.formatTimestamp(reservation.created_at), fontSize = 14.sp, fontFamily = poppins)
                }

                // Return Time
                FormSection1(label = "Return Time:") {
                    Text(TimeUtils.formatTimestamp(reservation.return_by), fontSize = 14.sp, fontFamily = poppins)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BOTTOM DIVIDER & BUTTON ---
                Divider(color = primaryBlue, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, closeRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Close",
                        color = closeRed,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppins,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FormSection1(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = poppins
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

// --- Reuse helpers ---
@Composable
fun SystemNotificationToast(message: String, user: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(user, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text(
                    message,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun TopHeaderBarHistory(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.PrimaryDarkBlue)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "UMak LabTrack",
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = poppins,
                fontWeight = FontWeight(600),
                color = Color.White
            )
        )
    }
}

@Composable
fun BottomNavigationBarHistory(selectedRoute: String, onNavSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountCircle, contentDescription = "Logs", tint = Color.Blue)
            Text("Logs", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ActivityLogsScreenPreview() {
    ActivityLogsScreen(showToast = true)
}