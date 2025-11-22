package com.example.umaklabtrack.borrowermodule

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
//                                        toastUser = "System"
//                                        toastMessage = "Action executed for ${reservation.type}"
                                        if (reservation.status == "Pending") showEditDialog = true
                                        if (reservation.status == "RETURNED") showReturnedItemsDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
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

            if (showEditDialog) {
                ReturnedItemsDialog(

                    onDismiss = { showReturnedItemsDialog = false },
                    onNext = {
                        showReturnedItemsDialog = false
                        showDetailsDialog = true
                    },
                    reservation = selectedReservation!!,
                    items = reservationsWithItems[selectedReservation] ?: emptyList(),
                )
            }

            if (showReturnedItemsDialog) {
                ReturnedItemsDialogDynamic(onDismiss = { showReturnedItemsDialog = false })
            }
            if (showDetailsDialog) {
                TransactionDetailsDialog(onDismiss = { showDetailsDialog = false })
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
                    "Date & Time: ${reservation.created_at}",
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    "Status: ${reservation.status}",
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    color = Color.Blue
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
                Text("Action", fontFamily = poppins, fontSize = 12.sp, color = Color.White)
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
fun TransactionDetailsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text("Transaction Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                // Add your transaction details here using BorrowInfo and TransactedItem
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
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

    LaunchedEffect(items) {
        val list = items.map { transItem ->
            val category = itemManage.getCategory(transItem.item_id) // suspend call
            Triple(transItem.item_name, "Apparatus", transItem.quantity)
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
                        text = "Selected Items",
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
                    text = "Borrowing", fontSize = 14.sp, color = Color.Gray, fontFamily = poppins,
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