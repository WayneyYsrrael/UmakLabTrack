package com.example.umaklabtrack.adminmodule

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
// --- COIL IMPORTS ---
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.umaklabtrack.R
import com.example.umaklabtrack.preferences.SessionPreferences
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    onNavSelected: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // --- Hardcoded Admin Data (Default State) ---
    var firstName by remember { mutableStateOf("Rai") }
    var lastName by remember { mutableStateOf("Sibulo") }
    var phoneNumber by remember { mutableStateOf("09123456789") }
    var email by remember { mutableStateOf("rsibulo.a12345632@umak.edu.ph") }

    // --- State for Profile Image ---
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // --- State for Dialogs ---
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sessionPrefs = remember { SessionPreferences(context) }
    val scope = rememberCoroutineScope()

    // --- PHOTO PICKER LAUNCHER ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // 1. Update UI
                imageUri = uri

                // 2. Grant Permanent Permission
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                try {
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 3. Save to Preferences (Async)
                scope.launch {
                    sessionPrefs.saveProfileImage(uri.toString())
                }
            }
        }
    )

    // --- LOAD SAVED IMAGE ON START ---
    LaunchedEffect(Unit) {
        val savedUriString = sessionPrefs.getProfileImage()
        if (!savedUriString.isNullOrEmpty()) {
            try {
                imageUri = Uri.parse(savedUriString)
            } catch (e: Exception) {
                imageUri = null
            }
        }
    }

    Scaffold(
        // 1. Use the REAL Blue Header from HomeAdminPage.kt
        topBar = { AdminHeaderBar() },

        // 2. Use the REAL Bottom Navigation from HomeAdminPage.kt
        bottomBar = {
            AdminBottomNavigationBar(
                selectedIndex = 4, // 4 corresponds to the "Profile" tab
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavSelected("dashboard")
                        1 -> onNavSelected("requests")
                        2 -> onNavSelected("notifications")
                        3 -> onNavSelected("logs") // History
                        4 -> { /* Already on Profile */ }
                    }
                }
            )
        },
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

                // --- IMAGE DISPLAY LOGIC ---
                if (imageUri != null) {
                    // Show Selected Image
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, AppColors.PrimaryDarkBlue, CircleShape)
                            .background(Color.LightGray)
                    )
                } else {
                    // Show Default Resource
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
                }

                // Camera Icon Badge (Clickable)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AppColors.PrimaryDarkBlue, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable {
                            // Launch Photo Picker
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
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
                text = "Admin Account",
                style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = AppColors.PrimaryDarkBlue, fontWeight = FontWeight.Bold)
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

            // Log Out
            ProfileMenuOption(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                onClick = {
                    scope.launch {
                        sessionPrefs.clearSession()
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
                }
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
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Edit Admin Profile",
                        style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextDark),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
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