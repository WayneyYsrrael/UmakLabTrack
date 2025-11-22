package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding // <-- IMPORT ADDED
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import com.example.umaklabtrack.entityManagement.ItemManage.BorrowInfo
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.umaklabtrack.entityManagement.ItemManage
import com.example.umaklabtrack.utils.TimeUtils
import com.example.umaklabtrack.dataClasses.UserSession

private val spb= ItemManage()


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    userName: String = "FirstName",
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBorrowClick: () -> Unit = {},
    onReserveClick: () -> Unit = {},
    onLoanClick: () -> Unit = {},
    onReturnClick: () -> Unit = {},
    onBrowseCatalogClick: () -> Unit = {},
    onNavSelected: (String) -> Unit = {}
) {
    var latestReservation by remember { mutableStateOf<BorrowInfo?>(null) }
    var isCheck by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        latestReservation = spb.getLatestReservation()
    }
    Scaffold(
        topBar = { TopHeaderBar() }, // This is fine, it calls the REAL TopHeaderBar
        bottomBar = { BottomNavigationBar(selectedRoute = "home", onNavSelected = onNavSelected) },
        containerColor = Color.White
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_home),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.labtracklogo),
                        contentDescription = "UMak Logo",
                        modifier = Modifier
                            .width(125.dp)
                            .height(125.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    // This is your fixed flexible layout
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome, $userName!",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight(700),
                                color = AppColors.TextDark.copy(alpha = 0.9f),
                            )
                        )

                        Text(
                            text = "How can we help you today?",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight(400),
                                color = AppColors.TextDark.copy(alpha = 0.8f),
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Your fixed flexible buttons
                ActionButton("BORROW") { onNavSelected("borrow/all") }
                ActionButton("RESERVE") { onNavSelected("reserve/all") }
                ActionButton("LOAN") { onNavSelected("loan/all") }
                ActionButton("RETURN", onReturnClick)
                ActionButton("BROWSE CATALOG") { onNavSelected("catalog") }

                Spacer(modifier = Modifier.height(8.dp)) // Adjusted space

                // Your card, which now correctly sends the toast command
                if (latestReservation != null) {
                    UserSession.trdate= TimeUtils.formatTimestamp(latestReservation!!.created_at)
                    UserSession.trstatus=if (latestReservation!!.status == "Pending" && latestReservation!!.type == "Borrow") {
                        "preparing items"
                    } else {
                        latestReservation!!.type
                    }
                    UserSession.trtype = latestReservation!!.type

                    // Display the card only if there’s a reservation
                    OngoingRequestCard(
                        name = UserSession.name!!,
                        transactionType = UserSession.trtype!!,
                        dateTime = UserSession.trdate!!,
                        status = UserSession.trstatus!!,
                        onEdit = { onNavSelected("logs?showToast=true") }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// --- ⬇️ THIS IS THE REAL FUNCTION YOU MUST FIX ⬇️ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeaderBar(modifier: Modifier = Modifier) { // <-- 1. ADD 'modifier'
    Row(
        modifier = modifier // <-- 2. APPLY 'modifier'
            .fillMaxWidth()
            .background(AppColors.PrimaryDarkBlue)
            .statusBarsPadding() // This now works because of the import
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "UMak LabTrack",
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = poppins,
                fontWeight = FontWeight(600),
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.size(48.dp))
    }
}
// --- ⬆️ THIS IS THE REAL FUNCTION YOU MUST FIX ⬆️ ---

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDarkBlue),
        modifier = Modifier
            .fillMaxWidth(0.9f) // Use 90% of width
            .height(50.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, fontFamily = poppins)
    }
}

@Composable
fun OngoingRequestCard(
    name: String,
    transactionType: String,
    dateTime: String,
    status: String,
    onEdit: () -> Unit
) {
    var latestReservation by remember { mutableStateOf<BorrowInfo?>(null) }
    var isCheck by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        latestReservation = spb.getLatestReservation()
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFFFD740),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 14.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "Ongoing Requests",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF182C55),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Card(
                modifier = Modifier.padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically // Centered
                ) {
                    if (latestReservation != null) {

                        val r = latestReservation!!   // shorter reference

                        // ===== SHOW LATEST RESERVATION =====
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = UserSession.name!!,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Transaction: ${r.type}",
                                fontFamily = poppins,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = "Date & Time: $dateTime",
                                fontFamily = poppins,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = "Status: ${r.status}",
                                fontFamily = poppins,
                                fontSize = 12.sp,
                                color = Color(0xFF1288BF)
                            )
                        }

                        Button(
                            onClick = onEdit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("View", fontFamily = poppins, fontSize = 12.sp)
                        }

                    } else {

                        // ===== WHEN NO LATEST RESERVATION =====
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No ongoing request",
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }

                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("View", fontFamily = poppins, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomePagePreview() {
    HomePage()
}


