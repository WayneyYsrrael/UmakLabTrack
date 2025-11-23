package com.example.umaklabtrack.adminmodule
// eto yung nag tatawag ng info sa viewrequestDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.umaklabtrack.ui.theme.poppins

// --- SHARED COLORS ---
val DialogHeaderBlue = Color(0xFF182C55)
val DialogFieldGray = Color(0xFFEAEAEA)

// --- DATA CLASSES ---
data class TransactionItem(
    val name: String,
    val type: String,
    val qty: Int
)

data class TransactionSlipData(
    val transactionType: String, // "Borrowing", "Reservation", "Loan"
    val professorName: String,
    val items: List<TransactionItem>,
    val subject: String,
    val college: String,
    val yearSection: String,
    val studentReps: List<String>,
    val dateLabel: String, // e.g. "Borrowing Date & Time"
    val startDateTime: String,
    val returnDateTime: String
)

@Composable
fun TransactionInfoSlipDialog(
    data: TransactionSlipData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f) // 95% Screen Width
                .heightIn(max = 800.dp) // Max height
                .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- HEADER ---
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // CENTER TITLE
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Transaction Details",
                            style = TextStyle(
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DialogHeaderBlue
                            )
                        )
                        // DYNAMIC SUBTITLE
                        Text(
                            text = data.transactionType,
                            style = TextStyle(
                                fontFamily = poppins,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        )
                    }

                    // Close Button
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp)
                            .clickable { onDismiss() },
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DialogHeaderBlue, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // --- SINGLE SCROLLABLE CONTENT (Merged) ---
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Selected Items Section
                    SelectedItemsContent(data.items)

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 2. Info Details Section
                    InfoSlipContent(data)
                }

                // --- FOOTER BUTTONS (Only Close) ---
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", fontFamily = poppins, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
        }
    }
}

// --- EXTENSION FUNCTIONS FOR LAZY LIST CONTENT ---

fun LazyListScope.SelectedItemsContent(items: List<TransactionItem>) {
    item {
        Text(
            text = "Selected Items",
            fontFamily = poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = DialogHeaderBlue,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
    items(items) { item ->
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // IMAGE PLACEHOLDER
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.LightGray, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Type: ${item.type}", fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                }
                Text("Qty: ${item.qty}", fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

fun LazyListScope.InfoSlipContent(data: TransactionSlipData) {
    item {
        Text(
            text = "Information Slip",
            fontFamily = poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = DialogHeaderBlue,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
    item {
        LabelText("Professor Name:")
        Text(data.professorName, fontFamily = poppins, fontSize = 14.sp, color = Color.Black)
    }
    item {
        LabelText("Subject:")
        GrayReadOnlyField(data.subject)
    }
    item {
        LabelText("College:")
        GrayReadOnlyField(data.college)
    }
    item {
        LabelText("Year & Section:")
        GrayReadOnlyField(data.yearSection)
    }
    item {
        LabelText("Student Representatives Name:")
        data.studentReps.forEach {
            Text(it, fontFamily = poppins, fontSize = 14.sp, color = Color.Black)
        }
    }
    item {
        LabelText("${data.dateLabel}:")
        GrayReadOnlyField(data.startDateTime)
    }
    item {
        LabelText("Should be Returned by:")
        GrayReadOnlyField(data.returnDateTime)
    }
}

// --- HELPERS ---
@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        fontFamily = poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun GrayReadOnlyField(value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DialogFieldGray, RoundedCornerShape(6.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(value, fontFamily = poppins, fontSize = 14.sp, color = Color.Black)
    }
}