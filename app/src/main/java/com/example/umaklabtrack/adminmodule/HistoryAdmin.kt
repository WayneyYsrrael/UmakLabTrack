package com.example.umaklabtrack.adminmodule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import com.example.umaklabtrack.ui.theme.poppins

// --- COLORS ---
val StatusGreen = Color(0xFF4CAF50)

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

    // Sample Data
    val logs = listOf(
        LogItem("1", "Prof. Susan Guevarra", "Borrow", "11/10/25 - 2:00PM", "Returned"),
        LogItem("2", "Prof. Era Marie", "Reserve", "11/09/25 - 10:00AM", "Approved"),
        LogItem("3", "Prof. CM Mansueto", "Loan", "11/08/25 - 4:30PM", "Returned")
    )

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
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(110.dp))

                Text(
                    text = "Activity Logs",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(logs) { log ->
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

            // --- LAYER 4: DETAILS DIALOG ---
            if (showDetailsDialog && selectedLogItem != null) {

                // 1. DETERMINE TYPE & LABELS
                val titleType = when(selectedLogItem!!.transactionType) {
                    "Borrow" -> "Borrowing"
                    "Reserve" -> "Reservation"
                    "Loan" -> "Loan"
                    else -> "Transaction"
                }

                val dateLabelText = when(selectedLogItem!!.transactionType) {
                    "Borrow" -> "Borrowing Date & Time"
                    "Reserve" -> "Reservation Date & Time"
                    "Loan" -> "Loan Date & Time"
                    else -> "Date & Time"
                }

                // 2. COLLEGE & SECTION LOGIC (Integrated Here)
                val allColleges = listOf(
                    "College of Computing and Information Sciences (CCIS)",
                    "Institute of Pharmacy (IOP)",
                    "Institute of Imaging Health Science (IIHS)",
                    "College of Construction Sciences and Engineering (CCSE)"
                )

                val allSections = listOf(
                    "II - ACSAD", "II - BCSAD", "II - CCSAD", "II - DCSAD", "II - BCOMP", "III - APHYS", "III - ABINS"
                )

                // Simple subject logic for demo purposes
                val subject = if(selectedLogItem!!.name.contains("Susan")) "Chemistry Lab" else "Modern Physics"

                val filteredColleges = when (subject) {
                    "Biology", "Chemistry Lab" -> listOf("Institute of Pharmacy (IOP)", "Institute of Imaging Health Science (IIHS)")
                    "Modern Physics" -> allColleges
                    else -> allColleges
                }

                val selectedCollege = filteredColleges.firstOrNull() ?: allColleges.first()

                val filteredSections = when (selectedCollege) {
                    "College of Computing and Information Sciences (CCIS)" -> listOf("II - ACSAD", "II - BCSAD", "II - CCSAD", "II - DCSAD")
                    "Institute of Pharmacy (IOP)" -> listOf("III - APHYS")
                    "Institute of Imaging Health Science (IIHS)" -> listOf("III - ABINS")
                    "College of Construction Sciences and Engineering (CCSE)" -> listOf("II - BCOMP")
                    else -> allSections
                }

                val selectedSection = filteredSections.firstOrNull() ?: allSections.first()

                // 3. PREPARE DIALOG DATA
                val dialogData = TransactionSlipData(
                    transactionType = titleType,
                    professorName = selectedLogItem!!.name,
                    items = listOf(
                        TransactionItem("Item Name", "Apparatus", 1),
                        TransactionItem("Item Name", "Chemicals", 2),
                        TransactionItem("Item Name", "Slides", 3)
                    ),
                    subject = subject,
                    college = selectedCollege,
                    yearSection = selectedSection,
                    studentReps = listOf(
                        "Villarica, Amrafel Marcus B.",
                        "Canlas, Emerson B.",
                        "Elado,Wayne Ysrael C.",
                        "Picao Mark Kevin"
                    ),
                    dateLabel = dateLabelText,
                    // Format: November 10, 2025 – 2:00 PM
                    startDateTime = "November 10, 2025 - 2:00 PM",
                    returnDateTime = "November 10, 2025 - 5:00 PM"
                )

                TransactionInfoSlipDialog(
                    data = dialogData,
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
                .fillMaxWidth(0.95f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.name, fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Transaction: ${log.transactionType}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                Text("Date & Time: ${log.dateTime}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
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

@Preview(showSystemUi = true)
@Composable
fun AdminActivityLogsPreview() {
    AdminActivityLogsPage(showToast = true)
}