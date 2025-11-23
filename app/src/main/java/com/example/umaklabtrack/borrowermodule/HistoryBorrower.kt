package com.example.umaklabtrack.borrowermodule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private val itemManage = ItemManage()

// --- Constants for the Wheel Picker ---
private val ITEM_HEIGHT = 48.dp
private val VISIBLE_ITEMS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogsScreen(
    showToast: Boolean = false,
    onNavSelected: (String) -> Unit = {}
) {
    var selectedReservation by remember { mutableStateOf<BorrowInfoWithId?>(null) }
    var visible by remember { mutableStateOf(showToast) }

    // Initial Toast State
    var toastMessage by remember { mutableStateOf("Your request is now pending approval.") }
    var toastUser by remember { mutableStateOf("System") }

    var isLoading by remember { mutableStateOf(true) }

    // --- SORT / FILTER STATES ---
    var showSortMenu by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf("All") }
    val sortOptions = listOf("All", "Borrow", "Reserve", "Loan")

    // Dialog States
    var showDetailsDialog by remember { mutableStateOf(false) }
    var isRescheduling by remember { mutableStateOf(false) }

    var reservationsWithItems by remember { mutableStateOf<Map<BorrowInfoWithId, List<TransactedItem>>>(emptyMap()) }

    // Load reservations
    LaunchedEffect(Unit) {
        val data = itemManage.getReservationsWithItems()
        reservationsWithItems = data
        isLoading = false
    }

    // --- FILTER LOGIC ---
    val filteredReservations = remember(reservationsWithItems, currentFilter) {
        if (currentFilter == "All") {
            reservationsWithItems
        } else {
            reservationsWithItems.filterKeys {
                it.type.contains(currentFilter, ignoreCase = true)
            }
        }
    }

    // Auto-hide toast logic
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

                // --- HEADER ROW ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction History",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextDark.copy(alpha = 0.9f)
                        )
                    )

                    // --- SORT UI ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category",
                            fontFamily = poppins,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSortMenu = true }
                                .padding(horizontal = 7.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = currentFilter,
                                    fontFamily = poppins,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Sort",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                sortOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = poppins,
                                                fontWeight = if (currentFilter == option) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            currentFilter = option
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        filteredReservations.forEach { (reservation, items) ->
                            item {
                                LogItemCard(
                                    name = UserSession.name ?: "Unknown",
                                    reservation = reservation,
                                    items = items,
                                    onAction = { _ ->
                                        selectedReservation = reservation
                                        isRescheduling = false // Reset to view mode initially

                                        if (reservation.status.equals("Pending", ignoreCase = true)) {
                                            toastMessage = "Your request is now pending approval."
                                            toastUser = "System"
                                            visible = true
                                        } else if (reservation.status.equals("Preparing", ignoreCase = true)) {
                                            toastMessage = "Please wait while we prepare your items.."
                                            toastUser = "System"
                                            visible = true
                                        } else {
                                            visible = false
                                        }

                                        showDetailsDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }

                        if (filteredReservations.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(top = 50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No ${if (currentFilter == "All") "" else currentFilter} transactions found.",
                                        style = TextStyle(color = Color.Gray, fontFamily = poppins, fontSize = 14.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            TopHeaderBarHistory(modifier = Modifier.align(Alignment.TopCenter))

            if (visible) {
                LogSystemToast(
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

            // --- DIALOG NAVIGATION LOGIC ---
            // ... inside ActivityLogsScreen ...

            // ... inside ActivityLogsScreen ...

            // --- DIALOG NAVIGATION LOGIC ---
            if (showDetailsDialog && selectedReservation != null) {
                if (isRescheduling) {
                    // *** SHOW RESCHEDULE SCREEN ***
                    LogRescheduleDialog(
                        reservation = selectedReservation!!,
                        onDismiss = {
                            showDetailsDialog = false
                            selectedReservation = null
                            isRescheduling = false
                        },
                        onBack = {
                            isRescheduling = false // Go back to View Slip
                        },
                        onConfirm = {
                            // Save logic here
                            showDetailsDialog = false
                            selectedReservation = null
                            isRescheduling = false
                            toastMessage = "Reservation updated successfully."
                            visible = true
                        }
                    )
                } else {
                    // *** SHOW VIEW SLIP ***

                    // 1. Check types
                    val isLoan = selectedReservation!!.type.contains("Loan", ignoreCase = true)
                    val isReservation = selectedReservation!!.type.contains("Reserve", ignoreCase = true) ||
                            selectedReservation!!.type.contains("Reservation", ignoreCase = true)

                    if (isLoan) {
                        // 2. LOAN DIALOG (New)
                        LogLoanDetailsDialog(
                            reservation = selectedReservation!!,
                            items = reservationsWithItems[selectedReservation] ?: emptyList(),
                            onDismiss = {
                                showDetailsDialog = false
                                selectedReservation = null
                            },
                            onReschedule = {
                                isRescheduling = true // Switch to Edit Mode
                            }
                        )
                    } else if (isReservation) {
                        // 3. RESERVATION DIALOG (Existing)
                        LogReservationDetailsDialog(
                            reservation = selectedReservation!!,
                            items = reservationsWithItems[selectedReservation] ?: emptyList(),
                            onDismiss = {
                                showDetailsDialog = false
                                selectedReservation = null
                            },
                            onReschedule = {
                                isRescheduling = true // Switch to Edit Mode
                            }
                        )
                    } else {
                        // 4. GENERIC/HISTORY DIALOG (Read-only)
                        LogTransactionDetailsDialog(
                            reservation = selectedReservation!!,
                            items = reservationsWithItems[selectedReservation] ?: emptyList(),
                            onDismiss = {
                                showDetailsDialog = false
                                selectedReservation = null
                            }
                        )
                    }
                }
            }
        }
    }
}

// ======================= 1. ORIGINAL DIALOG (For Borrow) =======================
@Composable
fun LogTransactionDetailsDialog(
    reservation: BorrowInfoWithId,
    items: List<TransactedItem>,
    onDismiss: () -> Unit
) {
    val primaryBlue = Color(0xFF182C55)
    val closeRed = Color(0xFFFF5252)
    val textBlack = Color(0xFF1D1B20)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textBlack, modifier = Modifier.align(Alignment.CenterStart).size(24.dp).clickable { onDismiss() })
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Information Slip", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryBlue, fontFamily = poppins))
                        Text(reservation.type, style = TextStyle(fontSize = 12.sp, color = Color.Gray, fontFamily = poppins))
                    }
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textBlack, modifier = Modifier.align(Alignment.CenterEnd).size(24.dp).clickable { onDismiss() })
                }
                Divider(color = primaryBlue, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    items.forEach { item ->
                        Text("(${item.quantity}) ${item.item_name}", style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textBlack), modifier = Modifier.padding(bottom = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailRow("Subject:", reservation.subject)
                    LogDetailRow("College:", reservation.college)
                    LogDetailRow("Year & Section:", reservation.yr_section)

                    Text("Student Representatives Name:", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = textBlack), modifier = Modifier.padding(top = 4.dp))
                    if (reservation.student_representative_names.isEmpty()) {
                        Text("N/A", style = TextStyle(fontSize = 14.sp, fontFamily = poppins))
                    } else {
                        reservation.student_representative_names.forEach { name ->
                            Text(text = name, style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textBlack))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LogDetailRow("Borrowing Date & Time:", TimeUtils.formatTimestamp(reservation.created_at))
                    LogDetailRow("Returned:", TimeUtils.formatTimestamp(reservation.return_by))
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E2E5), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text("Condition Update: Items has been successfully returned in good condition. Thank you!", style = TextStyle(fontSize = 12.sp, fontFamily = poppins, color = textBlack), modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = textBlack, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = primaryBlue, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = BorderStroke(1.dp, closeRed), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text(text = "Close", color = closeRed, fontWeight = FontWeight.Bold, fontFamily = poppins, fontSize = 16.sp)
                }
            }
        }
    }
}

// ======================= 2. RESERVATION VIEW DIALOG =======================
@Composable
fun LogReservationDetailsDialog(
    reservation: BorrowInfoWithId,
    items: List<TransactedItem>,
    onDismiss: () -> Unit,
    onReschedule: () -> Unit
) {
    val primaryBlue = Color(0xFF182C55)
    val closeRed = Color(0xFFFF5252)
    val textBlack = Color(0xFF1D1B20)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(max = 750.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textBlack, modifier = Modifier.align(Alignment.CenterStart).size(24.dp).clickable { onDismiss() })
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Information Slip", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryBlue, fontFamily = poppins))
                        Text(text = reservation.type, style = TextStyle(fontSize = 12.sp, color = Color.Gray, fontFamily = poppins))
                    }
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textBlack, modifier = Modifier.align(Alignment.CenterEnd).size(24.dp).clickable { onDismiss() })
                }
                Divider(color = primaryBlue, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    Text(text = "Selected Items:", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = textBlack), modifier = Modifier.padding(bottom = 4.dp))
                    items.forEach { item ->
                        Text(text = "(${item.quantity}) ${item.item_name}", style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textBlack), modifier = Modifier.padding(bottom = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailRow("Subject:", reservation.subject)
                    LogDetailRow("College:", reservation.college)
                    LogDetailRow("Year & Section:", reservation.yr_section)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Student Representatives Name:", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = textBlack))
                    if (reservation.student_representative_names.isEmpty()) {
                        Text("N/A", style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textBlack))
                    } else {
                        reservation.student_representative_names.forEach { name ->
                            Text(text = name, style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textBlack))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailRow("Borrowing Date & Time:", TimeUtils.formatTimestamp(reservation.created_at))
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = primaryBlue, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onReschedule, colors = ButtonDefaults.buttonColors(containerColor = primaryBlue), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(text = "Re-schedule", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = poppins, fontSize = 16.sp)
                    }
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = BorderStroke(1.dp, closeRed), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(text = "Close", color = closeRed, fontWeight = FontWeight.Bold, fontFamily = poppins, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
@Composable
fun LogLoanDetailsDialog(
    reservation: BorrowInfoWithId,
    items: List<TransactedItem>,
    onDismiss: () -> Unit,
    onReschedule: () -> Unit
) {
    val primaryBlue = Color(0xFF182C55)
    val closeRed = Color(0xFFFF5252)
    val textBlack = Color(0xFF1D1B20)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(max = 750.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // --- Header ---
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textBlack, modifier = Modifier.align(Alignment.CenterStart).size(24.dp).clickable { onDismiss() })
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Information Slip", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryBlue, fontFamily = poppins))
                        Text(text = "Loan", style = TextStyle(fontSize = 12.sp, color = Color.Gray, fontFamily = poppins))
                    }
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textBlack, modifier = Modifier.align(Alignment.CenterEnd).size(24.dp).clickable { onDismiss() })
                }
                Divider(color = primaryBlue, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                // --- Content ---
                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    Text(text = "Selected Items:", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = textBlack), modifier = Modifier.padding(bottom = 4.dp))
                    items.forEach { item ->
                        Text(text = "(${item.quantity}) ${item.item_name}", style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textBlack), modifier = Modifier.padding(bottom = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    LogDetailRow("Subject:", reservation.subject)
                    LogDetailRow("College:", reservation.college)

                    // Added Room Number as per your image.
                    // Note: If your database has a 'room' variable, replace "HPSB 901" with 'reservation.room'
                    LogDetailRow("Room Number:", "HPSB 901")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Student Representatives Name:", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = textBlack))
                    if (reservation.student_representative_names.isEmpty()) {
                        Text("N/A", style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textBlack))
                    } else {
                        reservation.student_representative_names.forEach { name ->
                            Text(text = name, style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textBlack))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Specific Date Label for Loan
                    LogDetailRow("Should be Returned by:", TimeUtils.formatTimestamp(reservation.return_by))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = primaryBlue, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // --- Buttons ---
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onReschedule, colors = ButtonDefaults.buttonColors(containerColor = primaryBlue), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(text = "Re-schedule", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = poppins, fontSize = 16.sp)
                    }
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = BorderStroke(1.dp, closeRed), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(text = "Close", color = closeRed, fontWeight = FontWeight.Bold, fontFamily = poppins, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
// ======================= 3. RESCHEDULE DIALOG (UPDATED LAYOUT) =======================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogRescheduleDialog(
    reservation: BorrowInfoWithId,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun showToast(message: String) {
        toastMessage = message
        showToast = true
        coroutineScope.launch {
            delay(3000)
            showToast = false
        }
    }

    // Data
    val studentList = remember { mutableStateListOf<String>().apply { addAll(reservation.student_representative_names) } }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showRemoveStudentDialog by remember { mutableStateOf(false) }
    var studentToRemove by remember { mutableStateOf("") }

    var pickupDateTimeStr by remember { mutableStateOf("November 11, 2025 - 2:00 PM") }
    var returnDateTimeStr by remember { mutableStateOf("November 11, 2025 - 5:00 PM") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isSelectingPickup by remember { mutableStateOf(true) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isChecked by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .shadow(4.dp)
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 750.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- Header ---
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { if (!showToast) onBack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF202020))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Information Slip", style = TextStyle(fontSize = 16.sp, fontFamily = poppins, fontWeight = FontWeight(700), color = Color(0xFF182C55)))
                            Text("Reservation", style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = Color.Gray))
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                            Icon(Icons.Default.Close, "Close", tint = Color(0xFF202020))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(Modifier.height(16.dp))

                    // --- STUDENT LIST (Directly under header) ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        studentList.forEach { studentName ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircle,
                                    contentDescription = "Remove",
                                    tint = Color(0xFFDC3545),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            studentToRemove = studentName
                                            showRemoveStudentDialog = true
                                        }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = studentName,
                                    style = TextStyle(
                                        fontFamily = poppins,
                                        fontSize = 14.sp,
                                        color = Color(0xFF202020)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Add Student Button ---
                    Button(
                        onClick = { showAddStudentDialog = true },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF425275)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = "Add student", style = TextStyle(fontFamily = poppins, color = Color.White, fontSize = 14.sp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Scheduled Pick-up ---
                    LogFormSection(label = "Scheduled Pick-up:") {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pickupDateTimeStr, onValueChange = {}, readOnly = true, enabled = false, modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { Icon(Icons.Default.DateRange, "Calendar", tint = Color.Black) },
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledContainerColor = Color.White, disabledBorderColor = Color(0xFFE0E0E0), disabledTrailingIconColor = Color.Black),
                                textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp), shape = RoundedCornerShape(8.dp)
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { if (!showToast) { isSelectingPickup = true; showDatePicker = true } })
                        }
                    }

                    // --- Should be Returned by ---
                    LogFormSection(label = "Should be Returned by:") {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = returnDateTimeStr, onValueChange = {}, readOnly = true, enabled = false, modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { Icon(Icons.Default.Schedule, "Schedule", tint = Color.Black) },
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledContainerColor = Color.White, disabledBorderColor = Color(0xFFE0E0E0), disabledTrailingIconColor = Color.Black),
                                textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp), shape = RoundedCornerShape(8.dp)
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { if (!showToast) { isSelectingPickup = false; showDatePicker = true } })
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Reminders ---
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFE0E0E0)), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Reminders:", style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. Requests must be sent at least 3 days before experiment.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                            Text("2. Double-check all items and information.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                            Text("3. Reagent containers, bottles, and other apparatus must be returned to the Central Laboratory clean and dry.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                            Text("4. Replace damaged items ASAP.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start, modifier = Modifier.padding(12.dp)) {
                            Checkbox(checked = isChecked, onCheckedChange = { isChecked = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF182C55)))
                            Text(text = "I have read and understood all reminders.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(Modifier.height(16.dp))

                    // --- Save Changes Button ---
                    AnimatedVisibility(visible = !showToast, enter = fadeIn(animationSpec = tween(300, delayMillis = 300)), exit = fadeOut(animationSpec = tween(300))) {
                        Button(
                            onClick = {
                                showToast("Reservation updated."); coroutineScope.launch { delay(2000); onConfirm() }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = isChecked,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isChecked) Color(0xFF182C55) else Color(0xFF182C55).copy(alpha = 0.9f), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Changes", style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                    }

                    AnimatedVisibility(visible = showToast, enter = fadeIn(animationSpec = tween(300)), exit = fadeOut(animationSpec = tween(300, delayMillis = 300))) {
                        LogSystemToast(message = toastMessage, user = "System")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false; showTimePicker = true }) { Text("OK", color = Color(0xFF182C55)) } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Red) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        LogTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute, amPm ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                val hourInt = hour.toInt()
                cal.set(Calendar.HOUR, if (hourInt == 12) 0 else hourInt)
                cal.set(Calendar.MINUTE, minute.toInt())
                cal.set(Calendar.AM_PM, if (amPm == "AM") Calendar.AM else Calendar.PM)
                val formattedTime = SimpleDateFormat("MMMM dd, yyyy - h:mm a", Locale.getDefault()).format(cal.time)
                if (isSelectingPickup) pickupDateTimeStr = formattedTime else returnDateTimeStr = formattedTime
                showTimePicker = false
            }
        )
    }

    if (showAddStudentDialog) LogAddStudentDialog(onDismiss = { showAddStudentDialog = false }, onAdd = { name -> if (name.isNotBlank()) studentList.add(name); showAddStudentDialog = false })
    if (showRemoveStudentDialog) LogRemoveStudentDialog(studentName = studentToRemove, onDismiss = { showRemoveStudentDialog = false }, onRemove = { studentList.remove(studentToRemove); showRemoveStudentDialog = false })
}

// ======================= HELPER COMPOSABLES =======================

@Composable
fun LogAddStudentDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // OutlinedTextField with custom styling
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Last Name, First Name", color = Color.Gray, fontSize = 14.sp, fontFamily = poppins) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        cursorColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Stacked Buttons: Add Student (Blue)
                Button(
                    onClick = { onAdd(name) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Student", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = poppins)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cancel (Outlined with Red Text)
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontFamily = poppins)
                }
            }
        }
    }
}

@Composable
fun LogRemoveStudentDialog(studentName: String, onDismiss: () -> Unit, onRemove: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildAnnotatedString {
                        append("Do you really want to\nremove ")
                        withStyle(style = SpanStyle(color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)) { append(studentName) }
                        append("?")
                    },
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 18.sp, fontFamily = poppins, fontWeight = FontWeight.Bold, color = Color.Black)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Stacked Buttons: Remove (Red)
                Button(
                    onClick = onRemove,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Remove", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = poppins)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Keep (Outlined Blue)
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, Color(0xFF182C55)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF182C55)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Keep", color = Color(0xFF182C55), fontWeight = FontWeight.Bold, fontFamily = poppins)
                }
            }
        }
    }
}

@Composable
fun LogFormSection(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = Color(0xFF1D1B20)))
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
fun LogDropdownInput(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit, placeholder: String, isError: Boolean = false, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption.ifEmpty { "" },
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = { Text(placeholder, color = Color.Gray, fontFamily = poppins, fontSize = 14.sp) },
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { expanded = true },
            shape = RoundedCornerShape(8.dp),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color(0xFFE0E0E0), disabledTrailingIconColor = Color.Gray, errorBorderColor = Color.Red),
            textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp)
        )
        Box(modifier = Modifier.matchParentSize().clickable(enabled = enabled) { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option, fontFamily = poppins, fontSize = 14.sp) }, onClick = { onOptionSelected(option); expanded = false }) }
        }
    }
}

@Composable
fun LogDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = poppins, color = Color(0xFF1D1B20)))
        Text(text = value, style = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = Color(0xFF1D1B20)))
    }
}

@Composable
fun LogItemCard(name: String, reservation: BorrowInfoWithId, items: List<TransactedItem>, onAction: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), elevation = CardDefaults.cardElevation(2.dp), border = BorderStroke(1.dp, Color.LightGray)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Transaction: ${reservation.type}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                Text("Date & Time: ${TimeUtils.formatTimestamp(reservation.created_at)}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black)) { append("Status: ") }
                    val statusColor = when (reservation.status) { "toClaim", "claimed", "Pending" -> Color(0xFFFFA500); "Preparing" -> Color(0xFFFFCE3D); "Approved" -> Color(0xFF43A047); "Rejected" -> Color(0xFFD32F2F); "Returned" -> Color(0xFF6A1B9A); else -> Color.Black }
                    withStyle(style = SpanStyle(color = statusColor)) { append(reservation.status) }
                }, fontFamily = poppins, fontSize = 12.sp)
            }
            Button(onClick = { onAction(reservation.status) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)), shape = RoundedCornerShape(10.dp)) { Text("View", fontFamily = poppins, fontSize = 12.sp, color = Color.White) }
        }
    }
}

@Composable
fun LogSystemToast(message: String, user: String, modifier: Modifier = Modifier) {
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

@Composable
fun BottomNavigationBarhistory(selectedRoute: String, onNavSelected: (String) -> Unit) {
    BottomNavigationBarHistory(selectedRoute, onNavSelected)
}

@Preview(showSystemUi = true)
@Composable
fun ActivityLogsScreenPreview() { ActivityLogsScreen(showToast = true) }

// ======================= WHEEL TIME PICKER (DARK THEME) =======================
@Composable
private fun LogTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: String, minute: String, amPm: String) -> Unit
) {
    val bgColor = Color(0xFF2D2D2D) // Dark Grey
    val contentColor = Color.White
    val highlightColor = Color(0xFFFFC400) // Yellow/Gold
    val unselectedColor = Color.Gray

    val hours = (1..12).map { "%02d".format(it) }
    val minutes = (0..59).map { "%02d".format(it) }
    val amPm = listOf("AM", "PM")

    var selectedHour by remember { mutableStateOf("08") }
    var selectedMinute by remember { mutableStateOf("49") }
    var selectedAmPm by remember { mutableStateOf("PM") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bgColor)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Set start time", style = TextStyle(fontSize = 18.sp, fontFamily = poppins, fontWeight = FontWeight.SemiBold, color = contentColor))
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().height(ITEM_HEIGHT * VISIBLE_ITEMS), contentAlignment = Alignment.Center) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        LogWheelPicker(items = hours, initialItem = selectedHour, onItemSelected = { selectedHour = it }, highlightColor = highlightColor, textColor = unselectedColor, modifier = Modifier.width(60.dp))
                        Text(":", color = highlightColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        LogWheelPicker(items = minutes, initialItem = selectedMinute, onItemSelected = { selectedMinute = it }, highlightColor = highlightColor, textColor = unselectedColor, modifier = Modifier.width(60.dp))
                        Spacer(Modifier.width(16.dp))
                        LogWheelPicker(items = amPm, initialItem = selectedAmPm, onItemSelected = { selectedAmPm = it }, highlightColor = highlightColor, textColor = unselectedColor, modifier = Modifier.width(60.dp))
                    }
                    Column(modifier = Modifier.fillMaxWidth().height(ITEM_HEIGHT * VISIBLE_ITEMS), verticalArrangement = Arrangement.Top) {
                        Spacer(modifier = Modifier.height((ITEM_HEIGHT * VISIBLE_ITEMS / 2) - (ITEM_HEIGHT / 2)))
                        Divider(color = highlightColor.copy(alpha = 0.3f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(ITEM_HEIGHT - 2.dp))
                        Divider(color = highlightColor.copy(alpha = 0.3f), thickness = 1.dp)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = highlightColor, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(16.dp))
                    TextButton(onClick = { onConfirm(selectedHour, selectedMinute, selectedAmPm) }) { Text("OK", color = highlightColor, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun LogWheelPicker(
    items: List<String>,
    initialItem: String,
    onItemSelected: (String) -> Unit,
    highlightColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    itemHeight: Dp = ITEM_HEIGHT,
    visibleItems: Int = VISIBLE_ITEMS,
) {
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val initialIndex = items.indexOf(initialItem).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val selectedIndex by remember {
        derivedStateOf {
            if (listState.isScrollInProgress) -1
            else (listState.firstVisibleItemIndex + (listState.firstVisibleItemScrollOffset / itemHeightPx).roundToInt()).coerceIn(0, items.lastIndex)
        }
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex != -1) onItemSelected(items[selectedIndex])
    }

    Box(modifier = modifier.height(itemHeight * visibleItems)) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItems / 2)),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items.size) { index ->
                Box(modifier = Modifier.height(itemHeight).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = items[index],
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = poppins,
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == selectedIndex) highlightColor else textColor
                        )
                    )
                }
            }
        }
    }
}