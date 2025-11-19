// PrivacyPolicy.kt
package com.example.umaklabtrack.borrowermodule

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
import androidx.compose.foundation.lazy.rememberLazyListState
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
 * Privacy Policy Dialog.
 * This is the second step after Terms and Conditions.
 *
 * @param onAgreeClicked Called when the user accepts the Privacy Policy.
 * @param onDismiss Called if the dialog is dismissed.
 */
@Composable
fun PrivacyPolicyDialog(
    onAgreeClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Semi-transparent background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            // Main content card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- Header ---
                    Text(
                        text = "Privacy Policy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    // --- Scrollable Content Area ---
                    val lazyListState = rememberLazyListState()

                    // Logic: Check if user scrolled to the bottom
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
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        item {
                            Text(
                                text = "Your privacy is important to us. This policy outlines how UMak LabTrack collects, uses, and protects your data.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)
                            )
                        }

                        // --- PRIVACY CONTENT BASED ON UMak CONTEXT ---

                        item { SectionTitle("1. Information We Collect") }
                        item { BulletPoint("Personal Identification: Name, Student/Employee ID, and Department/College.") }
                        item { BulletPoint("Academic Records: Certificate of Registration (COR) data for validation purposes.") }
                        item { BulletPoint("System Activity: Login timestamps, item reservation history, and return status logs.") }

                        item { SectionTitle("2. How We Use Your Information") }
                        item { BulletPoint("To verify your identity as an authorized UMak STEM student or staff member.") }
                        item { BulletPoint("To process and track equipment borrowing, reservations, and returns.") }
                        item { BulletPoint("To generate accountability reports for damaged or lost laboratory items.") }
                        item { BulletPoint("To send notifications regarding schedules, approvals, and overdue alerts.") }

                        item { SectionTitle("3. Data Storage & Security") }
                        item { BulletPoint("Your data is stored securely within the UMak LabTrack local server/database.") }
                        item { BulletPoint("Access is restricted to authorized Laboratory Heads and System Administrators.") }
                        item { BulletPoint("We implement security measures to prevent unauthorized access, alteration, or disclosure.") }

                        item { SectionTitle("4. Data Sharing & Disclosure") }
                        item { BulletPoint("We do not sell or trade your personal information to third parties.") }
                        item { BulletPoint("Data may be shared with University Administration for disciplinary actions regarding lost or damaged equipment.") }

                        item { SectionTitle("5. Data Retention") }
                        item { BulletPoint("Transaction logs (borrowing history) are retained for the duration of the academic year for auditing.") }
                        item { BulletPoint("Inactive accounts may be archived or deleted per University data retention policies.") }

                        item { SectionTitle("6. User Responsibilities") }
                        item { BulletPoint("You consent to the scanning and storage of your COR for account verification.") }
                        item { BulletPoint("You are responsible for ensuring your registered contact information is accurate.") }
                    }

                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- "I AGREE" Button ---
                    val buttonBackgroundColor = if (hasScrolledToEnd) Color(0xFFFFC107) else Color(0xFFAAAAAA)
                    val buttonTextColor = if (hasScrolledToEnd) Color.Black else Color.White

                    Button(
                        onClick = onAgreeClicked,
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
                            text = "I AGREE",
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

// --- Reusable Composable Helpers (Same as T&C to ensure design consistency) ---

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
            lineHeight = 20.sp
        )
    }
}

// --- Preview ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PrivacyPolicyDialogPreview() {
    PrivacyPolicyDialog(
        onAgreeClicked = {},
        onDismiss = {}
    )
}