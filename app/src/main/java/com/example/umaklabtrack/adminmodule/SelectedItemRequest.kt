package com.example.umaklabtrack.adminmodule

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.umaklabtrack.ui.theme.poppins // Assuming poppins is available

// --- Mock Data Structure (for the items inside the request) ---
data class SelectedItem(
    val id: String,
    val name: String,
    val type: String,
    val quantity: Int
)

// =======================================================================
// NEW COMPOSABLE: REJECTION REASON DIALOG
// =======================================================================

@Composable
fun RejectionReasonDialog(
    onDismiss: () -> Unit,
    onRejectConfirmed: (String) -> Unit // Passes the rejection reason back
) {
    var rejectionReason by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Please state the reason for rejecting the request.",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Input Field
                OutlinedTextField(
                    value = rejectionReason,
                    onValueChange = { rejectionReason = it },
                    singleLine = false,
                    placeholder = { Text("Reason...", fontFamily = poppins) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF182C55),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Adjusted height for multi-line input visibility
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Button (Dark Blue)
                Button(
                    onClick = { onRejectConfirmed(rejectionReason.text) },
                    enabled = rejectionReason.text.isNotBlank(), // Disable if reason is empty
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm Rejection", fontFamily = poppins, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel Button (Red Outlined)
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                    border = BorderStroke(1.dp, Color(0xFFE53935)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel Rejection", fontFamily = poppins, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


// =======================================================================
// UPDATED COMPOSABLE: SelectedItemsRequestDialog
// =======================================================================

@Composable
fun SelectedItemsRequestDialog(
    requestorName: String,
    selectedItems: List<SelectedItem>,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onRejectConfirmed: (String) -> Unit // Changed signature to handle confirmation
) {
    // State to control the visibility of the Rejection Reason Dialog
    var showReasonDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- Dialog Header ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Selected Items",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                        Text(
                            text = "Borrowing Request by $requestorName",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onDismiss),
                        tint = Color.Black
                    )
                }

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // --- Scrollable Item List ---
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedItems) { item ->
                        ItemDetailCard(item = item)
                        Divider()
                    }
                }

                // --- Action Buttons ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Approve", fontFamily = poppins, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showReasonDialog = true }, // SHOW REJECTION DIALOG
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)), // Red text color
                        border = BorderStroke(1.dp, Color(0xFFE53935)), // Red border
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reject", fontFamily = poppins, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // --- Conditional Rejection Reason Dialog ---
    if (showReasonDialog) {
        RejectionReasonDialog(
            onDismiss = { showReasonDialog = false },
            onRejectConfirmed = { reason ->
                showReasonDialog = false
                onRejectConfirmed(reason) // Pass the reason up to the main RequestsAdminPage
            }
        )
    }
}

// --- Helper Composable for Item Card (No change) ---
@Composable
fun ItemDetailCard(item: SelectedItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder for Item Image
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Item Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = "Type: ${item.type}",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Quantity
        Text(
            text = "Qty: ${item.quantity}",
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RejectionReasonDialogPreview() {
    // Note: The onRejectConfirmed is a placeholder. In a real app, it would send a network request.
    RejectionReasonDialog(
        onDismiss = {},
        onRejectConfirmed = {}
    )

}

@Preview(showBackground = true)
@Composable
fun SelectedItemsRequestDialogPreview() {
    val mockItems = listOf(
        SelectedItem(id = "1", name = "Microscope", type = "Equipment", quantity = 2),
        SelectedItem(id = "2", name = "Beaker Set", type = "Labware", quantity = 5),
        SelectedItem(id = "3", name = "Safety Goggles", type = "Protective Gear", quantity = 10)
    )

    SelectedItemsRequestDialog(
        requestorName = "Juan Dela Cruz",
        selectedItems = mockItems,
        onDismiss = {},
        onApprove = {},
        onRejectConfirmed = {} // Placeholder function
    )
}