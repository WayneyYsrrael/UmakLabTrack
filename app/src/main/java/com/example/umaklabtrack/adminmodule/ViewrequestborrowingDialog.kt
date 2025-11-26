package com.example.umaklabtrack.adminmodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties // --- IMPORT ADDED ---
import com.example.umaklabtrack.ui.theme.poppins

// --- DATA CLASS ---
data class BorrowingDetails(
    val professorName: String,
    val items: List<String>,
    val subject: String,
    val college: String,
    val yearSection: String,
    val studentReps: List<String>,
    val borrowDate: String,
    val returnDate: String
)

@Composable
fun BorrowingInfoSlipDialog(
    data: BorrowingDetails,
    onDismiss: () -> Unit,
    onMarkAsPrepared: () -> Unit,
    onMarkAsClaimed: () -> Unit
) {
    val PrimaryBlueColor = Color(0xFF182C55)
    val TextBlack = Color.Black
    val LabelColor = Color.Black
    val SubTextColor = Color(0xFF4A4A4A)

    Dialog(
        onDismissRequest = onDismiss,
        // --- THIS FIXES THE WIDTH ISSUE ---
        // It tells the dialog to ignore the default system margins
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .padding(0.dp)
                // Now this will actually take up 95% of the TOTAL screen width
                .fillMaxWidth(0.95f)
                .height(580.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // ================= HEADER =================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp)
                ) {


                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = data.professorName,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueColor
                            )
                        )
                        Text(
                            text = "Borrowing",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Normal,
                                color = TextBlack
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .size(24.dp)
                            .clickable { onDismiss() },
                        tint = TextBlack
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = PrimaryBlueColor.copy(alpha = 0.5f))

                // ================= SCROLLABLE CONTENT =================
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Selected Items:",
                            style = TextStyle(
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = LabelColor
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(data.items) { itemStr ->
                        Text(
                            text = itemStr,
                            style = TextStyle(
                                fontFamily = poppins,
                                fontSize = 14.sp,
                                color = SubTextColor
                            ),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        @Composable
                        fun InfoBlock(label: String, value: String) {
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = LabelColor
                                    )
                                )
                                Text(
                                    text = value,
                                    style = TextStyle(
                                        fontFamily = poppins,
                                        fontSize = 14.sp,
                                        color = SubTextColor
                                    )
                                )
                            }
                        }

                        InfoBlock("Subject:", data.subject)
                        InfoBlock("College:", data.college)
                        InfoBlock("Year & Section:", data.yearSection)

                        Text(
                            text = "Student Representatives Name:",
                            style = TextStyle(
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = LabelColor
                            )
                        )
                        data.studentReps.forEach { name ->
                            Text(
                                text = name,
                                style = TextStyle(
                                    fontFamily = poppins,
                                    fontSize = 14.sp,
                                    color = SubTextColor
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        InfoBlock("Borrowing Date & Time:", data.borrowDate)
                        InfoBlock("Should be Returned by:", data.returnDate)

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = PrimaryBlueColor.copy(alpha = 0.5f))

                // ================= FOOTER BUTTONS =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onMarkAsPrepared,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, PrimaryBlueColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Mark As Prepared",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryBlueColor
                        )
                    }

                    Button(
                        onClick = onMarkAsClaimed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Mark as Claimed",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ================= PREVIEW =================
@Preview(showBackground = true)
@Composable
fun PreviewBorrowingInfoSlipDialog() {
    val dummyData = BorrowingDetails(
        professorName = "Prof. Susan Guevarra",
        items = listOf(
            "(1) ItemName1",
            "(2) ItemName2",
            "(3) ItemName3",
            "(2) ItemName4",
            "(1) ItemName5",
            "(2) ItemName6"
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        BorrowingInfoSlipDialog(
            data = dummyData,
            onDismiss = {},
            onMarkAsPrepared = {},
            onMarkAsClaimed = {}
        )
    }
}