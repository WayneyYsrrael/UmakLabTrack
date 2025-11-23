package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.umaklabtrack.R
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.preferences.SessionPreferences
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavSelected: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // --- State for User Data ---
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // --- State for Dialogs ---
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sessionPrefs = remember { SessionPreferences(context) }
    val scope = rememberCoroutineScope()

    // Load Session Data
    LaunchedEffect(Unit) {
        val user = sessionPrefs.loadSession()
        val names = user.name?.split(" ") ?: listOf("User", "")
        firstName = names.firstOrNull() ?: ""
        lastName = names.drop(1).joinToString(" ")
        email = user.email ?: ""
        phoneNumber = user.cNum ?: ""
    }

    Scaffold(
        topBar = { TopHeaderBar(modifier = Modifier.fillMaxWidth()) },
        bottomBar = { BottomNavigationBar(selectedRoute = "profile", onNavSelected = onNavSelected) },
        containerColor = Color.White
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // --- 1. Profile Header (Picture + Name) ---
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, AppColors.PrimaryDarkBlue, CircleShape)
                        .background(Color.LightGray)
                )
                // Camera Icon Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AppColors.PrimaryDarkBlue, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { /* TODO: Open Image Picker */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Edit Photo", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$firstName $lastName",
                style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextDark)
            )
            Text(
                text = email,
                style = TextStyle(fontFamily = poppins, fontSize = 14.sp, color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- 2. Menu Options List ---

            // Edit Profile
            ProfileMenuOption(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                onClick = { showEditProfileDialog = true }
            )
            Divider(color = Color(0xFFF0F0F0))

            // Terms
            ProfileMenuOption(
                icon = Icons.Default.Description,
                title = "Terms and Conditions",
                onClick = { showTermsDialog = true }
            )
            Divider(color = Color(0xFFF0F0F0))

            // Privacy Policy
            ProfileMenuOption(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                onClick = { showPrivacyDialog = true }
            )
            Divider(color = Color(0xFFF0F0F0))

            // Log Out
            ProfileMenuOption(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                onClick = {
                    scope.launch {
                        sessionPrefs.clearSession()
                        UserSession.clear()
                        onLogout()
                    }
                },
                textColor = Color.Red,
                iconColor = Color.Red,
                showArrow = false
            )

            Spacer(modifier = Modifier.height(50.dp))
        }

        // --- DIALOGS ---

        // 1. Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                initialFirstName = firstName,
                initialLastName = lastName,
                initialPhone = phoneNumber,
                initialEmail = email,
                onDismiss = { showEditProfileDialog = false },
                onSave = { newFirst, newLast, newPhone, newEmail ->
                    firstName = newFirst
                    lastName = newLast
                    phoneNumber = newPhone
                    email = newEmail
                    showEditProfileDialog = false
                    // TODO: Save logic here
                }
            )
        }

        // 2. Terms Dialog (No Buttons, Read Only)
        if (showTermsDialog) {
            SimpleScrollableDialog(
                title = "Terms and Conditions",
                onDismiss = { showTermsDialog = false },
                content = { TermsContent() } // Injecting the Terms composable
            )
        }

        // 3. Privacy Dialog (No Buttons, Read Only)
        if (showPrivacyDialog) {
            SimpleScrollableDialog(
                title = "Privacy Policy",
                onDismiss = { showPrivacyDialog = false },
                content = { PrivacyContent() } // Injecting the Privacy composable
            )
        }
    }
}

// ==========================================
//              HELPER COMPOSABLES
// ==========================================

@Composable
fun ProfileMenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: Color = AppColors.TextDark,
    iconColor: Color = AppColors.PrimaryDarkBlue,
    showArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = TextStyle(
                fontFamily = poppins,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            ),
            modifier = Modifier.weight(1f)
        )
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    initialFirstName: String,
    initialLastName: String,
    initialPhone: String,
    initialEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var fName by remember { mutableStateOf(initialFirstName) }
    var lName by remember { mutableStateOf(initialLastName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var mail by remember { mutableStateOf(initialEmail) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- CENTERED TITLE HEADER ---
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Edit Profile",
                        style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextDark),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd) // Pushes Close button to the right
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(value = fName, onValueChange = { fName = it }, label = "First Name")
                Spacer(modifier = Modifier.height(12.dp))
                ProfileTextField(value = lName, onValueChange = { lName = it }, label = "Last Name")
                Spacer(modifier = Modifier.height(12.dp))
                ProfileTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 11 && it.all { char -> char.isDigit() }) phone = it },
                    label = "Phone Number",
                    keyboardType = KeyboardType.Phone
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProfileTextField(value = mail, onValueChange = { mail = it }, label = "Email", keyboardType = KeyboardType.Email)
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSave(fName, lName, phone, mail) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDarkBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes", style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                }
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.PrimaryDarkBlue,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedLabelColor = AppColors.PrimaryDarkBlue
            ),
            textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp, color = AppColors.TextDark),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

// ==========================================
//         UPDATED SCROLLABLE DIALOG
// ==========================================

@Composable
fun SimpleScrollableDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit // Changed from String to Composable
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp) // Slightly taller
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // --- CENTERED TITLE HEADER ---
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextDark),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd) // Pushes Close button to the right
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Divider(color = Color.LightGray, modifier = Modifier.padding(top = 8.dp))

                // Content Area
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false) // Allow it to shrink if content is small
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

// ==========================================
//         TERMS & PRIVACY CONTENT
// ==========================================

@Composable
fun TermsContent() {
    Column {
        Text(
            text = "By using this application, you agree to the following:",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        SectionTitle("1. Account & Identity")
        BulletPoint("Only authorized UMak users (STEM professors, Lab Staff, Lab Head) may access the system.")
        BulletPoint("Your COR information may be scanned and stored for verification and accountability.")
        BulletPoint("You are responsible for keeping your login credentials secure.")

        SectionTitle("2. Borrowing & Reservations")
        BulletPoint("All borrow, loan, and reservation requests must be made through the app.")
        BulletPoint("Some items require Staff or Lab Head approval, especially restricted or specialized equipment.")
        BulletPoint("Approved items must be claimed and returned on time.")

        SectionTitle("3. Use, Care, and Return of Items")
        BulletPoint("Inspect all items confirming the request.")
        BulletPoint("All items must be returned clean, dry, and in good condition.")
        BulletPoint("Returns require scanning to record condition (Good / Damaged / Lost).")
        BulletPoint("You are responsible for any item damaged, lost, or mishandled.")

        SectionTitle("4. Damage, Loss & Replacement")
        BulletPoint("Any damaged or missing item must be reported immediately.")
        BulletPoint("Users may be required to replace or compensate for broken or lost items, following UMak policies.")
        BulletPoint("Damaged or lost items will be logged and archived for proper disposal or replacement.")

        SectionTitle("5. Scheduling & Restrictions")
        BulletPoint("Reservations follow class-based scheduling to avoid conflicts.")
        BulletPoint("Overlapping or double reservations are not allowed.")
        BulletPoint("Schedule changes, extensions, or loan renewals require approval.")

        SectionTitle("6. Notifications & Compliance")
        BulletPoint("You agree to receive reminders for due dates, approvals, expirations, and overdue items.")
        BulletPoint("Users must follow all usage guidelines, laboratory safety rules, and system alerts.")

        SectionTitle("7. Activity Monitoring")
        BulletPoint("All scans, requests, approvals, borrowing actions, and returns are recorded for accountability.")
        BulletPoint("The laboratory may review logs and restrict access for any misuse or violations.")

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PrivacyContent() {
    Column {
        Text(
            text = "Your privacy is important to us. This policy outlines how UMak LabTrack collects, uses, and protects your data.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        SectionTitle("1. Information We Collect")
        BulletPoint("Personal Identification: Name, Student/Employee ID, and Department/College.")
        BulletPoint("Academic Records: Certificate of Registration (COR) data for validation purposes.")
        BulletPoint("System Activity: Login timestamps, item reservation history, and return status logs.")

        SectionTitle("2. How We Use Your Information")
        BulletPoint("To verify your identity as an authorized UMak STEM student or staff member.")
        BulletPoint("To process and track equipment borrowing, reservations, and returns.")
        BulletPoint("To generate accountability reports for damaged or lost laboratory items.")
        BulletPoint("To send notifications regarding schedules, approvals, and overdue alerts.")

        SectionTitle("3. Data Storage & Security")
        BulletPoint("Your data is stored securely within the UMak LabTrack local server/database.")
        BulletPoint("Access is restricted to authorized Laboratory Heads and System Administrators.")
        BulletPoint("We implement security measures to prevent unauthorized access, alteration, or disclosure.")

        SectionTitle("4. Data Sharing & Disclosure")
        BulletPoint("We do not sell or trade your personal information to third parties.")
        BulletPoint("Data may be shared with University Administration for disciplinary actions regarding lost or damaged equipment.")

        SectionTitle("5. Data Retention")
        BulletPoint("Transaction logs (borrowing history) are retained for the duration of the academic year for auditing.")
        BulletPoint("Inactive accounts may be archived or deleted per University data retention policies.")

        SectionTitle("6. User Responsibilities")
        BulletPoint("You consent to the scanning and storage of your COR for account verification.")
        BulletPoint("You are responsible for ensuring your registered contact information is accurate.")

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==========================================
//         STYLED TEXT HELPERS
// ==========================================

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp, // Slightly smaller than the full screen version for better dialog fit
        color = AppColors.TextDark,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp)
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, start = 4.dp),
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
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    // Dummy Composables for Preview
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable fun TopHeaderBar(modifier: Modifier = Modifier) {}
    @Composable fun BottomNavigationBar(selectedRoute: String, onNavSelected: (String) -> Unit) {}

    ProfileScreen()
}