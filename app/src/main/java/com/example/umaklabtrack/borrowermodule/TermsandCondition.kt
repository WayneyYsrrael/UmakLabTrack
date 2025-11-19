// TermsandCondition.kt
package com.example.umaklabtrack.borrowermodule // Using your package name

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState // For LazyColumn scroll state
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A composable function that displays the Terms and Conditions dialog,
 * matching the style of the BorrowerInformationSlipDialog.
 *
 * @param onAgreeClicked A lambda function to be invoked when the "I AGREE" button is clicked.
 * @param onDismiss A lambda function to be invoked when the dialog is dismissed (e.g., back press).
 */
@Composable
fun TermsAndConditionsDialog(
    onNextClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false // Fills the entire screen
        )
    ) {
        // Semi-transparent background dim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            // Main content card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White // White background
                ),
                modifier = Modifier
                    .fillMaxWidth(0.95f) // Use 95% of the screen width
                    .fillMaxHeight(0.9f)  // Use 90% of the screen height
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- Header ---
                    Text(
                        text = "Terms and Conditions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Divider
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    // --- Scrollable Content Area ---
                    val lazyListState = rememberLazyListState()

                    // Derived state to check if the user has scrolled to the end
                    val hasScrolledToEnd by remember {
                        derivedStateOf {
                            val layoutInfo = lazyListState.layoutInfo
                            if (layoutInfo.totalItemsCount == 0) return@derivedStateOf true
                            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val totalItemsCount = layoutInfo.totalItemsCount
                            (lastVisibleItemIndex == totalItemsCount - 1) && !lazyListState.canScrollForward
                        }
                    }

                    LazyColumn(
                        state = lazyListState, // Assign the state to the LazyColumn
                        modifier = Modifier
                            .weight(1f) // Takes up all available space
                            .padding(vertical = 8.dp)
                    ) {
                        // Preamble
                        item {
                            Text(
                                text = "By using this application, you agree to the following:",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)
                            )
                        }

                        // --- FULL UPDATED TEXT ---
                        item { SectionTitle("1. Account & Identity") }
                        item { BulletPoint("Only authorized UMak users (STEM professors, Lab Staff, Lab Head) may access the system.") }
                        item { BulletPoint("Your COR information may be scanned and stored for verification and accountability.") }
                        item { BulletPoint("You are responsible for keeping your login credentials secure.") }

                        item { SectionTitle("2. Borrowing & Reservations") }
                        item { BulletPoint("All borrow, loan, and reservation requests must be made through the app.") }
                        item { BulletPoint("Some items require Staff or Lab Head approval, especially restricted or specialized equipment.") }
                        item { BulletPoint("Approved items must be claimed and returned on time.") }

                        item { SectionTitle("3. Use, Care, and Return of Items") }
                        item { BulletPoint("Inspect all items confirming the request.") }
                        item { BulletPoint("All items must be returned clean, dry, and in good condition.") }
                        item { BulletPoint("Returns require scanning to record condition (Good / Damaged / Lost).") }
                        item { BulletPoint("You are responsible for any item damaged, lost, or mishandled.") }

                        item { SectionTitle("4. Damage, Loss & Replacement") }
                        item { BulletPoint("Any damaged or missing item must be reported immediately.") }
                        item { BulletPoint("Users may be required to replace or compensate for broken or lost items, following UMak policies.") }
                        item { BulletPoint("Damaged or lost items will be logged and archived for proper disposal or replacement.") }

                        item { SectionTitle("5. Scheduling & Restrictions") }
                        item { BulletPoint("Reservations follow class-based scheduling to avoid conflicts.") }
                        item { BulletPoint("Overlapping or double reservations are not allowed.") }
                        item { BulletPoint("Schedule changes, extensions, or loan renewals require approval.") }

                        item { SectionTitle("6. Notifications & Compliance") }
                        item { BulletPoint("You agree to receive reminders for due dates, approvals, expirations, and overdue items.") }
                        item { BulletPoint("Users must follow all usage guidelines, laboratory safety rules, and system alerts.") }

                        item { SectionTitle("7. Activity Monitoring") }
                        item { BulletPoint("All scans, requests, approvals, borrowing actions, and returns are recorded for accountability.") }
                        item { BulletPoint("The laboratory may review logs and restrict access for any misuse or violations.") }
                    }
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    
                    Spacer(modifier = Modifier.height(16.dp)) // Spacer between content and button

                    // --- "I AGREE" Button ---
                    // --- "NEXT" Button ---
                    // logic: Button is grey/disabled until scrolled to bottom, then turns Yellow
                    val buttonBackgroundColor = if (hasScrolledToEnd) Color(0xFFFFC107) else Color(0xFFAAAAAA)
                    val buttonTextColor = if (hasScrolledToEnd) Color.Black else Color.White

                    Button(
                        onClick = onNextClicked, // This will trigger navigation to Privacy Policy
                        enabled = hasScrolledToEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBackgroundColor,
                            disabledContainerColor = Color(0xFFAAAAAA)
                        )
                    ) {
                        Text(
                            text = "NEXT", // Changed from "I AGREE"
                            color = buttonTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper composable for a section title
 */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color.Black,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

/**
 * Helper composable for a bullet point item
 */
@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, start = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 14.sp,
            lineHeight = 20.sp // Added for better readability
        )
    }
}

// --- Preview ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermsAndConditionsDialogPreview() {
    TermsAndConditionsDialog(
        onNextClicked = {},
        onDismiss = {}
    )
}