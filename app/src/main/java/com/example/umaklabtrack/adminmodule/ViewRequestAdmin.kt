    package com.example.umaklabtrack.adminmodule
    // NAG HAHANDLE NUNG VIEW REQUEST SA ADMIN
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
        val transactionType: String, // "Borrow", "Reserve", or "Loan"
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
            RequestData("1", "Prof. Susan Guevarra", "Borrow", "11/10/25 • 2:00PM", "Requesting"),
            RequestData("2", "Prof. John Doe", "Reserve", "11/11/25 • 1:00PM", "Pending"),
            RequestData("3", "Prof. Jane Smith", "Loan", "11/12/25 • 9:00AM", "Approved")

        )
    ) {
        // --- STATE VARIABLES ---
        var currentSelectedIndex by remember { mutableStateOf(1) }
        var showNewRequestBanner by remember { mutableStateOf(requests.isNotEmpty()) }

        var showRequestDialog by remember { mutableStateOf(false) }
        var selectedRequest by remember { mutableStateOf<RequestData?>(null) }

        // --- FILTER STATE ---
        var isFilterExpanded by remember { mutableStateOf(false) }
        var selectedFilter by remember { mutableStateOf("All") }
        val filterOptions = listOf("All", "Borrow", "Reserve", "Loan")

        // --- FILTER LOGIC ---
        val filteredRequests = remember(requests, selectedFilter) {
            if (selectedFilter == "All") {
                requests
            } else {
                requests.filter { it.transactionType.equals(selectedFilter, ignoreCase = true) }
            }
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
                        // --- CHANGED: Updated padding to 16.dp (matches History Page) ---
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(110.dp)) // Space for Header

                    // --- HEADER ROW WITH SORT ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Adjusted bottom padding slightly if needed, keeping alignment
                            .padding(start = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title
                        Text(
                            text = "Requests",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )

                        // --- SORT UI (UPDATED to 50.dp fixed width) ---
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

                            // Box for Dropdown (Compact size)
                            Box(
                                modifier = Modifier
                                    .width(75.dp)
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isFilterExpanded = true }
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedFilter,
                                        fontFamily = poppins,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Sort",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(12.dp)
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
                    // --- END HEADER ROW ---

                    if (filteredRequests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (selectedFilter == "All") "You have no pending requests." else "No $selectedFilter requests found.",
                                color = Color.Gray,
                                fontFamily = poppins
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(filteredRequests) { request ->
                                // Original card
                                RequestCard(
                                    data = request,
                                    onViewClick = {
                                        selectedRequest = request
                                        showRequestDialog = true
                                    }
                                )

                                // Add extra "Scan" card if it's a Borrow transaction
                                if (request.transactionType.equals("Borrow", ignoreCase = true)) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    RequestCard(
                                        data = request.copy(status = "Waiting for Return"),
                                        onViewClick = {
                                            // Action for Scan button
                                            selectedRequest = request
                                            showRequestDialog = true
                                        },
                                        buttonText = "Scan",
                                        buttonColor = Color(0xFFFFD600) // Yellow
                                    )
                                }
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

                // --- Layer 4: THE POP-UP DIALOG (DETAILED DATA) ---
                if (showRequestDialog && selectedRequest != null) {

                    // --- MOCKING DETAILED DATA BASED ON SELECTION ---
                    val borrowData = BorrowingDetails(
                        professorName = selectedRequest!!.requestorName,
                        items = listOf(
                            "(1) Microscope",
                            "(2) Beakers (500ml)",
                            "(3) Test Tubes",
                            "(1) Bunsen Burner",
                            "(1) Graduated Cylinder"
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

                    BorrowingInfoSlipDialog(
                        data = borrowData,
                        onDismiss = { showRequestDialog = false },
                        onMarkAsPrepared = { showRequestDialog = false },
                        onMarkAsClaimed = { showRequestDialog = false }
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
    fun RequestCard(
        data: RequestData,
        onViewClick: () -> Unit,
        buttonText: String = "View",
        buttonColor: Color = PrimaryBlueColor
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(data.requestorName, fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Transaction: ${data.transactionType}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                    Text("Date & Time: ${data.dateTime}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)

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
                Button(
                    onClick = onViewClick,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(buttonText, fontFamily = poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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