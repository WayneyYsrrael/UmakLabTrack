package com.example.umaklabtrack.adminmodule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import com.example.umaklabtrack.ui.theme.poppins

// --- COLORS ---
val StatusGreen = Color(0xFF4CAF50)
val PrimaryBlueColors = Color(0xFF0D47A1) // Added this to prevent errors in Dialog

// --- DATA CLASS ---
data class LogItem(
    val id: String,
    val name: String,
    val transactionType: String,
    val dateTime: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminActivityLogsPage(
    showToast: Boolean = true,
    onNavSelected: (String) -> Unit = {}
) {
    var selectedIndex by remember { mutableStateOf(3) }

    // Dialog State
    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedLogItem by remember { mutableStateOf<LogItem?>(null) }

    // --- FILTER STATES ---
    var isFilterExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Borrow", "Reserve", "Loan")

    // Sample Data
    val logs = listOf(
        LogItem("1", "Prof. Susan Guevarra", "Borrow", "11/10/25 - 2:00PM", "Returned"),
        LogItem("2", "Prof. Era Marie", "Reserve", "11/09/25 - 10:00AM", "Approved"),
        LogItem("3", "Prof. CM Mansueto", "Loan", "11/08/25 - 4:30PM", "Returned")
    )

    // --- FILTER LOGIC ---
    val filteredLogs = remember(logs, selectedFilter) {
        if (selectedFilter == "All") {
            logs
        } else {
            logs.filter { it.transactionType.equals(selectedFilter, ignoreCase = true) }
        }
    }

    // Banner Logic
    var showBanner by remember { mutableStateOf(showToast) }
    LaunchedEffect(showBanner) {
        if (showBanner) {
            delay(5000)
            showBanner = false
        }
    }

    Scaffold(
        topBar = { null },
        bottomBar = {
            AdminBottomReqNavigationBar(
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    selectedIndex = index
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
            // --- LAYER 1: MAIN CONTENT ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(110.dp))

                // --- HEADER ROW (Title + Sort) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activity Logs",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
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

                        // Box Wrapping the Trigger Row and the DropdownMenu
                        Box(
                            modifier = Modifier
                                .width(75.dp) // INCREASED WIDTH TO FIT TEXT
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isFilterExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // --- FIX: SINGLE TEXT COMPONENT ---
                                Text(
                                    text = selectedFilter,
                                    fontFamily = poppins,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Sort",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isFilterExpanded,
                                onDismissRequest = { isFilterExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                filterOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = poppins,
                                                fontWeight = if (selectedFilter == option) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            selectedFilter = option
                                            isFilterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    // --- END SORT UI ---
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No ${if (selectedFilter == "All") "" else selectedFilter} logs found.",
                            color = Color.Gray,
                            fontFamily = poppins
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredLogs) { log ->
                            ActivityLogCard(
                                log = log,
                                onDetailsClick = {
                                    selectedLogItem = log
                                    showDetailsDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // --- LAYER 2: HEADER ---
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
                AdminTopHeaderBar()
            }

            // --- LAYER 3: NOTIFICATION ---
            AnimatedVisibility(
                visible = showBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                    .zIndex(1f)
            ) {
                if (logs.isNotEmpty()) {
                    SystemNotificationBanner(
                        message = "\"${logs.first().name}\" transaction has been marked as returned."
                    )
                }
            }

            // --- LAYER 4: DETAILS DIALOG (CUSTOM) ---
            if (showDetailsDialog && selectedLogItem != null) {
                LogDetailsDialog(
                    logItem = selectedLogItem!!,
                    onDismiss = { showDetailsDialog = false }
                )
            }
        }
    }
}

@Composable
fun ActivityLogCard(log: LogItem, onDetailsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.name, fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Transaction: ${log.transactionType}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                Text("Date & Time: ${log.dateTime}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Gray)) { append("Status: ") }
                        withStyle(style = SpanStyle(color = StatusGreen, fontWeight = FontWeight.Medium)) { append(log.status) }
                    },
                    fontFamily = poppins, fontSize = 12.sp
                )
            }
            Button(
                onClick = onDetailsClick,
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Details", fontFamily = poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// --- WIDE LOG DETAILS DIALOG ---
@Composable
fun LogDetailsDialog(
    logItem: LogItem,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Header with Close Icon
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.align(Alignment.Center)) {
                        Text(
                            text = logItem.name,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryBlueColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = when(logItem.transactionType) {
                                "Borrow" -> "Borrowing"
                                "Reserve" -> "Reservation"
                                else -> logItem.transactionType
                            },
                            fontFamily = poppins,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = PrimaryBlueColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Content
                Text("(1) Microscope", fontFamily = poppins, fontSize = 14.sp)
                Text("(2) Beakers", fontFamily = poppins, fontSize = 14.sp)
                Text("(3) Test Tubes", fontFamily = poppins, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                AdminInfoRow("Subject:", "Chemistry")
                AdminInfoRow("College:", "Institute of Pharmacy (IOP)")
                AdminInfoRow("Year & Section:", "II - A BSP")

                Text("Student Representatives Name:", fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Canlas, Emerson B.", fontFamily = poppins, fontSize = 14.sp)
                Text("Elado, Wayne Ysrael C.", fontFamily = poppins, fontSize = 14.sp)
                Text("Villarica, Amrafel Marcus B.", fontFamily = poppins, fontSize = 14.sp)
                Text("Picao, Mark Kevin .", fontFamily = poppins, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                AdminInfoRow("Borrowing Date & Time:", "November 10, 2025 - 2:00 PM")
                AdminInfoRow("Return Date:", "November 10, 2025 - 5:00 PM")

                // Success Message Box
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Condition Update: Items has been successfully returned in good condition. Thank you!",
                        fontFamily = poppins,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = PrimaryBlueColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Close", fontFamily = poppins, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AdminInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(label, fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(value, fontFamily = poppins, fontSize = 14.sp)
    }
}

@Preview(showSystemUi = true)
@Composable
fun AdminActivityLogsPreview() {
    AdminActivityLogsPage(showToast = true)
}