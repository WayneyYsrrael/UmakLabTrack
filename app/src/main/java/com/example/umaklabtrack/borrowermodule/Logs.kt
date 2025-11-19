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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogsScreen(
    showToast: Boolean = false,
    onNavSelected: (String) -> Unit = {}
) {
    var visible by remember { mutableStateOf(showToast) }

    // --- STATE VARIABLES FOR DIALOGS ---
    var showEditDialog by remember { mutableStateOf(false) } // Controls the "Cart"
    var showSlipDialog by remember { mutableStateOf(false) } // Controls the "Slip"

    // --- MOCK DATA ---
    var editItems by remember { mutableStateOf(ItemRepository.frequentlyBorrowedItems.take(2)) }
    var editQuantities by remember {
        mutableStateOf(editItems.associate { it.name to 1 })
    }

    if (visible) {
        LaunchedEffect(Unit) {
            delay(3000L)
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

            // --- Layer 1: The Scrollable Column ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(110.dp))

                Text(
                    text = "Activity Logs",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextDark.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Activity Card ---
                ActivityLogItemCard(
                    name = "Prof. Susan Guevarra",
                    transactionType = "Borrow",
                    dateTime = "11/10/25 • 2:00PM",
                    status = "Pending Approval",
                    // 1. On Edit Click -> Open Edit Dialog
                    onEdit = { showEditDialog = true }
                )
            }

            // --- Layer 2: The TopHeaderBar ---
            TopHeaderBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )

            // --- Layer 3: The Toast ---
            if (visible) {
                SystemNotificationToast(
                    message = "Your request is now pending approval.",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp)
                        .zIndex(1f)
                )
            }

            // =========================================
            //             DIALOG LOGIC
            // =========================================

            // --- 1. SELECTED ITEMS (CART) DIALOG ---
            if (showEditDialog) {
                val currentItems = editItems.filter { editQuantities.containsKey(it.name) }

                if (currentItems.isEmpty()) {
                    showEditDialog = false
                } else {
                    SelectedItemsDialog(
                        selectedItems = currentItems,
                        itemQuantities = editQuantities,
                        headerTitle = "Edit Request",
                        headerSubtitle = "Modify items",
                        onDismiss = { showEditDialog = false },
                        onRemoveItem = { itemName ->
                            editQuantities = editQuantities - itemName
                        },
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
                        onNext = {
                            // 2. On Next Click -> Close Cart, Open Slip
                            showEditDialog = false
                            showSlipDialog = true
                        }
                    )
                }
            }

            // --- 2. INFORMATION SLIP DIALOG ---
            if (showSlipDialog) {
                BorrowerInformationSlipDialog(
                    onDismiss = { showSlipDialog = false },
                    onGoBack = {
                        // 3. On Back Click -> Return to Cart
                        showSlipDialog = false
                        showEditDialog = true
                    },
                    onConfirm = { subject, college, section ->
                        // 4. On Confirm -> Close Slip, Show Toast
                        showSlipDialog = false
                        visible = true // Trigger success toast
                    }
                )
            }
        }
    }
}

// --- Helper Composables (Standard) ---

@Composable
fun SystemNotificationToast(
    message: String,
    user: String = "System",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "System Notification",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = user,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = message,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun ActivityLogItemCard(
    name: String,
    transactionType: String,
    dateTime: String,
    status: String,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Transaction: $transactionType",
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
                    text = "Status: $status",
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    color = Color(0xFF1288BF)
                )
            }
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Edit", fontFamily = poppins, fontSize = 12.sp)
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showSystemUi = true)
@Composable
fun ActivityLogsScreenPreview() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopHeaderBar(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(AppColors.PrimaryDarkBlue)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
    @Composable
    fun BottomNavigationBar(selectedRoute: String, onNavSelected: (String) -> Unit) {
        Row(modifier = Modifier
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

    ActivityLogsScreen(showToast = true)
}