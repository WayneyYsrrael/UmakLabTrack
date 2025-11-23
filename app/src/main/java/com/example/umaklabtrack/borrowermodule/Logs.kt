package com.example.umaklabtrack.borrowermodule
import androidx.compose.material3.Text
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle

import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.unit.dp

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.shadow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.umaklabtrack.R
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.delay
import com.example.umaklabtrack.entityManagement.ItemManage
import kotlin.collections.map
import com.example.umaklabtrack.entityManagement.ItemManage.BorrowInfoWithId
import com.example.umaklabtrack.entityManagement.ItemManage.TransactedItem
import com.example.umaklabtrack.entityManagement.ItemManage.ItemL
import com.example.umaklabtrack.utils.TimeUtils


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

    var showEditDialog by remember { mutableStateOf(false) }
    var showSlipDialog by remember { mutableStateOf(false) }
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
                                    name=UserSession.name!!,
                                    reservation = reservation,
                                    items = items,
                                    onAction = { action ->
                                        selectedReservation = reservation
                                        visible = true
//
                                        if (reservation.status == "Pending"||reservation.status == "Preparing") showReturnedItemsDialog = true
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
            if (showSlipDialog && selectedReservation != null) {

            }

            if (showDetailsDialog) {
                val reservation = selectedReservation!!
                TransactionDetailsDialog(
                    reservation = reservation,
                    onDismiss = { showDetailsDialog = false},
                    onGoBack = {showDetailsDialog = false;showReturnedItemsDialog=true}
                )
            }
        }
    }
}

@Composable
fun ActivityLogItemCardDynamic(
    name:String,
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
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                            "toClaim" -> Color(0xFFFFA500)
                            "claimed" -> Color(0xFFFFA500)
                            "Pending" -> Color(0xFFFFA500)
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

//            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                items.forEach { item ->
//                    Text("${item.item_name} x${item.quantity}", fontFamily = poppins, fontSize = 12.sp)
//                }
//            }


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
fun EditReservationDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Edit Reservation", fontWeight = FontWeight.Bold, fontFamily = poppins, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
            }
        }
    }
}

@Composable
fun ReturnedItemsDialogDynamic(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Returned Items", fontWeight = FontWeight.Bold, fontFamily = poppins, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
            }
        }
    }
}

// --- Reuse helpers ---
@Composable
fun SystemNotificationToast(message: String, user: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Image(painter = painterResource(id = R.drawable.profile), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(user, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text(message, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun TopHeaderBarHistory(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().background(AppColors.PrimaryDarkBlue).statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("UMak LabTrack", style = TextStyle(fontSize = 20.sp, fontFamily = poppins, fontWeight = FontWeight(600), color = Color.White))
    }
}

@Composable
fun BottomNavigationBarHistory(selectedRoute: String, onNavSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
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



@Composable
fun ReturnedItemsDialog(
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    reservation: BorrowInfoWithId,
    items: List<TransactedItem>
) {
    var returnedItems by remember { mutableStateOf<List<Triple<String, String, Int>>>(emptyList()) }

    LaunchedEffect(items) {
        val list = items.map { transItem ->

            println("DEBUG: Fetching category for item_id=${transItem.item_id}, name=${transItem.item_name}")

            val category = itemManage.getCategory(transItem.item_id, transItem.item_name)

            println("DEBUG: Category result = $category")

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
            modifier = Modifier.fillMaxWidth().heightIn(max = 650.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // HEADER
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Transacted Items",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = poppins,
                        textAlign = TextAlign.Center, color = Color(0xFF182C55),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        Icons.Default.Close, contentDescription = "Close", tint = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterEnd).clickable { onDismiss() }
                    )
                }
                Text(
                    text = "Current Status: ${reservation.status}", fontSize = 14.sp, color = Color.Gray, fontFamily = poppins,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
                Divider(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp), color = Color(0xFF182C55), thickness = 2.dp)
                // END HEADER

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(returnedItems) { (name, type, qty) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = Color.Black)
                                    Text("Type: $type", fontSize = 12.sp, color = Color.Gray, fontFamily = poppins)
                                }
                                Text("Qty: $qty", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = Color.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Next", fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsDialog(
    reservation: BorrowInfoWithId,
    onDismiss: () -> Unit,
    onGoBack:()-> Unit// optional go back action
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    val statusText = when (reservation.status.lowercase()) {
                        "preparing" -> "Preparing"
                        "borrowed" -> "Borrowed"
                        "returned" -> "Returned"
                        "pending" -> "Pending"
                        else -> "Unknown"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .height(56.dp) // optional, gives top bar height
                    ) {
                        // Back arrow aligned to start
                        IconButton(
                            onClick = { onGoBack() },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF202020)
                            )
                        }


                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                "Prof. ${UserSession.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = poppins,
                                color = Color(0xFF182C55)
                            )
                            Text(
                                statusText,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontFamily = poppins
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp), color = Color(0xFF182C55), thickness = 2.dp)

                    // Subject
                    FormSection1(label = "Subject:") {
                        Text(reservation.subject, style = TextStyle(fontSize = 14.sp))
                    }

                    // College
                    FormSection1(label = "College:") {
                        Text(reservation.college, style = TextStyle(fontSize = 14.sp))
                    }

                    // Year & Section
                    FormSection1(label = "Year & Section:") {
                        Text(reservation.yr_section, style = TextStyle(fontSize = 14.sp))
                    }

                    // Student Representatives
                    FormSection1(label = "Student Representatives:") {
                        if (reservation.student_representative_names.isEmpty()) {
                            Text("No students added.", color = Color.Gray, fontSize = 14.sp)
                        } else {
                            Column {
                                reservation.student_representative_names.forEach { student ->
                                    Text(student, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // Borrowing Date & Time
                    FormSection1(label = "Borrowing Date & Time:") {
                        Text(TimeUtils.formatTimestamp(reservation.created_at), fontSize = 14.sp)
                    }

                    // Return Date & Time
                    FormSection1(label = "Return Date & Time:") {
                        Text(TimeUtils.formatTimestamp(reservation.return_by), fontSize = 14.sp)
                    }

                    Divider(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp), color = Color(0xFF182C55), thickness = 2.dp)

                    // Close button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFFF5252)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Close", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontFamily = poppins)
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
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
