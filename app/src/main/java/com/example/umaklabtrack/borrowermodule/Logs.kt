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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
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
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.delay

// --- 1. SIMULATION ENUM ---
enum class TransactionStatus {
    PENDING, TO_CLAIM, IN_USE, IN_USE_OVERDUE, SCANNING, RETURNED
}

// --- 2. MOCK ITEM DATA ---
data class Item(val name: String, val type: String = "Apparatus")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogsScreen(
    showToast: Boolean = false,
    onNavSelected: (String) -> Unit = {}
) {
    // --- SIMULATION STATE ---
    var currentStatus by remember { mutableStateOf(TransactionStatus.PENDING) }

    // --- TOAST STATE ---
    var visible by remember { mutableStateOf(showToast) }
    var toastMessage by remember { mutableStateOf("Your request is now pending approval.") }
    var toastUser by remember { mutableStateOf("System") }

    // --- DIALOG STATES ---
    var showEditDialog by remember { mutableStateOf(false) }
    var showSlipDialog by remember { mutableStateOf(false) }
    var showReturnedItemsDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    // --- CART DATA ---
    val initialItems = listOf(Item("Beaker, 250ml", "Apparatus"), Item("Microscope", "Apparatus"))
    var editItems by remember { mutableStateOf(initialItems) }
    var editQuantities by remember { mutableStateOf(editItems.associate { it.name to 1 }) }

    // Auto-hide toast
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

                // --- DYNAMIC TITLE ---
                Text(
                    text = if (currentStatus == TransactionStatus.PENDING) "Activity Logs" else "Transaction History",
                    style = TextStyle(
                        fontSize = 24.sp, fontFamily = poppins, fontWeight = FontWeight.Bold,
                        color = AppColors.TextDark.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- DYNAMIC CARD ---
                val dateDisplay = when(currentStatus) {
                    TransactionStatus.RETURNED -> "11/10/2025 - 4:55PM"
                    TransactionStatus.IN_USE,
                    TransactionStatus.IN_USE_OVERDUE,
                    TransactionStatus.SCANNING -> "11/10/2025 - 5:00PM"
                    else -> "11/10/25 - 2:00PM"
                }

                ActivityLogItemCard(
                    name = "Prof. Susan Guevarra",
                    transactionType = "Borrow",
                    dateTime = dateDisplay,
                    statusEnum = currentStatus,
                    onAction = {
                        when(currentStatus) {
                            TransactionStatus.PENDING -> showEditDialog = true
                            TransactionStatus.IN_USE, TransactionStatus.IN_USE_OVERDUE -> {
                                currentStatus = TransactionStatus.SCANNING
                                visible = true; toastUser = "System"; toastMessage = "Scanning items..."
                            }
                            TransactionStatus.RETURNED -> showReturnedItemsDialog = true
                            else -> {}
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- DEV SIMULATION BUTTON ---
                Button(
                    onClick = {
                        visible = true
                        when(currentStatus) {
                            TransactionStatus.PENDING -> {
                                currentStatus = TransactionStatus.TO_CLAIM
                                toastUser = "AdminName"
                                toastMessage = "Your request has been approved."
                            }
                            TransactionStatus.TO_CLAIM -> {
                                currentStatus = TransactionStatus.IN_USE
                                toastUser = "System"
                                toastMessage = "Your borrowed equipment is due later today."
                            }
                            TransactionStatus.IN_USE -> {
                                currentStatus = TransactionStatus.IN_USE_OVERDUE
                                toastUser = "System"
                                toastMessage = "Overdue Alert!"
                            }
                            TransactionStatus.IN_USE_OVERDUE -> {
                                currentStatus = TransactionStatus.SCANNING
                                toastUser = "System"
                                toastMessage = "Scanning items..."
                            }
                            TransactionStatus.SCANNING -> {
                                currentStatus = TransactionStatus.RETURNED
                                toastUser = "System"
                                toastMessage = "Items returned successfully."
                            }
                            TransactionStatus.RETURNED -> {
                                currentStatus = TransactionStatus.PENDING
                                toastUser = "System"
                                toastMessage = "Request reset to pending."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DEV: Simulate Next Step", color = Color.Black)
                }

                Text("Pending > Claim > InUse > Overdue > Scanning > Returned", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp))
            }

            // --- HEADERS & TOAST ---
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

            // =========================================
            //             DIALOG LOGIC
            // =========================================

            // 1. PENDING: EDIT ITEMS
            if (showEditDialog) {
                val currentItems = editItems.filter { editQuantities.containsKey(it.name) }
                if (currentItems.isNotEmpty()) {
                    SelectedItemsDialog(
                        selectedItems = currentItems,
                        itemQuantities = editQuantities,
                        onDismiss = { showEditDialog = false },
                        onRemoveItem = { itemName -> editQuantities = editQuantities - itemName },
                        onIncreaseQuantity = { itemName ->
                            val currentQty = editQuantities[itemName] ?: 0
                            editQuantities = editQuantities + (itemName to currentQty + 1)
                        },
                        onDecreaseQuantity = { itemName ->
                            val currentQty = editQuantities[itemName] ?: 0
                            if (currentQty > 1) {
                                editQuantities = editQuantities + (itemName to currentQty - 1)
                            }
                        },
                        onNext = { showEditDialog = false; showSlipDialog = true }
                    )
                } else {
                    editQuantities = editItems.associate { it.name to 1 }
                    showEditDialog = false
                }
            }

            // 2. PENDING: INFORMATION SLIP
            if (showSlipDialog) {
                BorrowerInformationSlipDialog(
                    onDismiss = { showSlipDialog = false },
                    onGoBack = { showSlipDialog = false; showEditDialog = true },
                    onConfirm = { _, _, _ ->
                        showSlipDialog = false
                        visible = true; toastUser = "System"; toastMessage = "Your request has been updated."
                    }
                )
            }

            // 3. RETURNED: ITEMS LIST (Read Only)
            if (showReturnedItemsDialog) {
                ReturnedItemsDialog(
                    onDismiss = { showReturnedItemsDialog = false },
                    onNext = {
                        showReturnedItemsDialog = false
                        showDetailsDialog = true
                    }
                )
            }

            // 4. RETURNED: FINAL DETAILS (Fixed)
            if (showDetailsDialog) {
                TransactionDetailsDialog(onDismiss = { showDetailsDialog = false })
            }
        }
    }
}

// ============================================================
//   DIALOG IMPLEMENTATIONS
// ============================================================

// --- 1. EDITABLE ITEMS (Pending Status) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedItemsDialog(
    selectedItems: List<Item>,
    itemQuantities: Map<String, Int>,
    onDismiss: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onNext: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
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

                Text("Note: Each item has a maximum quantity of 'n'.", fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = poppins, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(selectedItems) { item ->
                        val qty = itemQuantities[item.name] ?: 1
                        Card(
                            shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(60.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = Color.Black)
                                        Text("Type: ${item.type}", fontSize = 12.sp, color = Color.Gray, fontFamily = poppins)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(30.dp).background(Color(0xFF757575), RoundedCornerShape(6.dp)).clickable { onDecreaseQuantity(item.name) }, contentAlignment = Alignment.Center) { Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                        Text(text = "$qty", modifier = Modifier.padding(horizontal = 12.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = poppins)
                                        Box(modifier = Modifier.size(30.dp).background(Color(0xFF424242), RoundedCornerShape(6.dp)).clickable { onIncreaseQuantity(item.name) }, contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                    }
                                }
                                Icon(Icons.Outlined.RemoveCircle, contentDescription = "Remove", tint = Color(0xFFFF5252), modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp).size(24.dp).clickable { onRemoveItem(item.name) })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Divider(modifier = Modifier.padding(bottom = 16.dp), color = Color(0xFFEEEEEE))
                Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Next", fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        }
    }
}

// --- 2. READ-ONLY ITEMS (Returned Status) ---
@Composable
fun ReturnedItemsDialog(
    onDismiss: () -> Unit,
    onNext: () -> Unit
) {
    val returnedItems = listOf(
        Triple("Beaker, 250ml", "Apparatus", 1),
        Triple("Microscope", "Apparatus", 1)
    )

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

// --- 3. INFORMATION SLIP (Pending Status) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerInformationSlipDialogHISTORY(
    onDismiss: () -> Unit,
    onGoBack: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var studentToRemove by remember { mutableStateOf<String?>(null) }
    var students by remember { mutableStateOf(listOf("Villarica, Amrafel Marcus B.", "Canlas, Emerson B.", "Elado, Wayne Ysrael C.", "Picao, Mark Kevin N.")) }

    Dialog(onDismissRequest = onDismiss) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().heightIn(max = 650.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.align(Alignment.CenterStart).clickable { onGoBack() })
                        Column(modifier = Modifier.align(Alignment.Center)) {
                            Text("Information Slip", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Text("Borrowing", fontSize = 12.sp, color = Color.Gray, fontFamily = poppins, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.align(Alignment.CenterEnd).clickable { onDismiss() })
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    // ... (Rest of Information Slip Logic is unchanged) ...
                    Text("Student Representatives Name:", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins)
                    Spacer(modifier = Modifier.height(4.dp))
                    students.forEach { name ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.RemoveCircle, contentDescription = "Remove", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp).clickable { studentToRemove = name })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, fontSize = 13.sp, fontFamily = poppins)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(40.dp)) { Text("Add student", fontSize = 12.sp, fontFamily = poppins) }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Borrowing Date & Time:", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = poppins)
                    OutlinedTextField(value = "November 10, 2025 - 2:00 PM", onValueChange = {}, readOnly = true, trailingIcon = { Icon(Icons.Outlined.AccessTime, contentDescription = null) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(fontSize = 13.sp, fontFamily = poppins))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Should be Returned by:", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = poppins)
                    OutlinedTextField(value = "November 10, 2025 - 5:00 PM", onValueChange = {}, readOnly = true, trailingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(fontSize = 13.sp, fontFamily = poppins))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onConfirm("Subject", "College", "Section") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) { Text("Save Changes", fontFamily = poppins) }
                }
            }
            if (studentToRemove != null) {
                Dialog(onDismissRequest = { studentToRemove = null }) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Remove student?", fontWeight = FontWeight.Bold, fontFamily = poppins)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { students = students - studentToRemove!!; studentToRemove = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }
}

// --- 4. FINAL DETAILS (Returned Status) - FIXED HEADER AND DIVIDER ---
@Composable
fun TransactionDetailsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {

                // --- HEADER START (Centered Title + Thick Blue Line) ---
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Prof. Susan Guevarra", fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = poppins, color = Color(0xFF182C55))
                    Text("Borrowing", fontSize = 12.sp, color = Color.Gray, fontFamily = poppins)
                }

                // Thick Dark Blue Divider BELOW HEADER
                Divider(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp), color = Color(0xFF182C55), thickness = 2.dp)
                // --- HEADER END ---

                // Content
                DetailRow("Subject:", "Modern Physics")
                DetailRow("College:", "College of Computing and Information Sciences (CCIS)")
                DetailRow("Year & Section:", "II-DCSAD")
                DetailRow("Student Representatives Name:", "Villarica, Amrafel Marcus B.\nCanlas, Emerson B.\nElado, Wayne Ysrael C.\nPicao, Mark Kevin N.")

                DetailRow("Borrowing Date & Time:", "November 10, 2025 - 2:00 PM")
                DetailRow("Return Time:", "November 10, 2025 - 4:57 PM")

                // Thick Dark Blue Divider ABOVE BUTTON
                Divider(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp), color = Color(0xFF182C55), thickness = 2.dp)

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

// --- HELPERS ---
@Composable
fun ActivityLogItemCard(name: String, transactionType: String, dateTime: String, statusEnum: TransactionStatus, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(2.dp), border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(Modifier.height(4.dp))
                Text("Transaction: $transactionType", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)

                val dateLabel = when(statusEnum) {
                    TransactionStatus.IN_USE, TransactionStatus.IN_USE_OVERDUE, TransactionStatus.SCANNING -> "Return by:"
                    TransactionStatus.RETURNED -> "Returned:"
                    else -> "Date & Time:"
                }
                Text("$dateLabel $dateTime", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)

                val (statusColor, statusText) = when(statusEnum) {
                    TransactionStatus.PENDING -> Color(0xFF1288BF) to "Pending Request"
                    TransactionStatus.TO_CLAIM -> Color(0xFF1288BF) to "To claim"
                    TransactionStatus.IN_USE -> Color(0xFFD4A017) to "Currently In Use"
                    TransactionStatus.IN_USE_OVERDUE -> Color(0xFFD4A017) to "Currently In Use"
                    TransactionStatus.SCANNING -> Color(0xFF1288BF) to "Scanning in progress"
                    TransactionStatus.RETURNED -> Color(0xFF4CAF50) to "Returned"
                }
                Text("Status: $statusText", fontFamily = poppins, fontSize = 12.sp, color = statusColor)
            }

            when(statusEnum) {
                TransactionStatus.PENDING -> Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)), shape = RoundedCornerShape(10.dp), modifier = Modifier.padding(start = 8.dp)) { Text("Edit", fontFamily = poppins, fontSize = 12.sp) }
                TransactionStatus.TO_CLAIM -> {}
                TransactionStatus.IN_USE, TransactionStatus.IN_USE_OVERDUE -> Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDD835)), shape = RoundedCornerShape(10.dp), modifier = Modifier.padding(start = 8.dp)) { Text("Return", fontFamily = poppins, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold) }
                TransactionStatus.SCANNING -> Button(onClick = {}, enabled = false, colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFF808080), disabledContentColor = Color.White), shape = RoundedCornerShape(10.dp), modifier = Modifier.padding(start = 8.dp)) { Text("Return", fontFamily = poppins, fontSize = 12.sp) }
                TransactionStatus.RETURNED -> Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(10.dp), modifier = Modifier.padding(start = 8.dp)) { Text("Details", fontFamily = poppins, fontSize = 12.sp, color = Color.White) }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = poppins, color = Color.Black)
        Text(value, fontSize = 13.sp, fontFamily = poppins, color = Color(0xFF424242))
    }
}

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
fun TopHeaderBarHISTORY(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().background(AppColors.PrimaryDarkBlue).statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("UMak LabTrack", style = TextStyle(fontSize = 20.sp, fontFamily = poppins, fontWeight = FontWeight(600), color = Color.White))
    }
}

@Composable
fun BottomNavigationBarHISTORY(selectedRoute: String, onNavSelected: (String) -> Unit) {
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