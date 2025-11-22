package com.example.umaklabtrack.borrowermodule

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.umaklabtrack.dataClasses.User
import com.example.umaklabtrack.entityManagement.CredentialsValidation
import com.example.umaklabtrack.dataClasses.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    previewMode: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showContent by remember { mutableStateOf(previewMode) }
    if (!previewMode) {
        LaunchedEffect(Unit) { showContent = true }
    }

    // --- NEW: Loading State ---
    var isLoading by remember { mutableStateOf(false) }
    // --------------------------

    // --- DIALOG STATES ---
    var showTermsAndConditionsDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    // ---------------------

    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var lastNameError by remember { mutableStateOf(false) }
    var firstNameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var contactError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    var toastMessage by remember { mutableStateOf("") }
    var toastTitle by remember { mutableStateOf("") }
    var toastType by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    fun showToast(title: String, message: String, type: String) {
        toastTitle = title
        toastMessage = message
        toastType = type
        showToast = true
        coroutineScope.launch {
            delay(3000)
            showToast = false
        }
    }

    fun validateForm() {
        // Prevent running if already loading
        if (isLoading) return

        lastNameError = lastName.isBlank()
        firstNameError = firstName.isBlank()
        emailError = email.isBlank() || !email.endsWith("@umak.edu.ph", ignoreCase = false)
        contactError = contactNumber.isBlank() || contactNumber.length != 11
        passwordError = password.isBlank() || password.length < 8 || !password.any { it.isDigit() }
        confirmPasswordError = confirmPassword.isBlank() || confirmPassword != password

        val hasError = listOf(
            lastNameError, firstNameError, emailError,
            contactError, passwordError, confirmPasswordError
        ).any { it }

        if (hasError) {
            when {
                lastName.isBlank() || firstName.isBlank() ||
                        email.isBlank() || contactNumber.isBlank() ||
                        password.isBlank() || confirmPassword.isBlank() -> {
                    showToast("Warning!", "Please fill all required fields.", "warning")
                }
                !email.endsWith("@umak.edu.ph", ignoreCase = false) -> {
                    showToast("Warning!", "Please use your UMak email.", "warning")
                }
                contactNumber.length != 11 -> {
                    showToast("Warning!", "Contact number must be 11 digits.", "warning")
                }
                password.length < 8 || !password.any { it.isDigit() } -> {
                    showToast("Warning!", "Password must contain 8 chars & a number.", "warning")
                }
                password != confirmPassword -> {
                    showToast("Oops!", "Passwords do not match!", "error")
                }
            }
            return // Stop here if there are local validation errors
        }

        // --- START LOADING ---
        isLoading = true

        val validator = CredentialsValidation()
        val newUser = User(
            name = "$firstName $lastName",
            email = email,
            contact=contactNumber,
            hashedPassword = validator.hashPassword(password)
        )
        UserSession.name = newUser.name
        UserSession.email = newUser.email
        UserSession.hashedPassword=newUser.hashedPassword
        UserSession.cNum=newUser.contact

        coroutineScope.launch {
            try {
                val emailDuplicate = validator.isEmailDuplicate(newUser)

                if (emailDuplicate) {
                    emailError = true
                    isLoading = false // Stop loading
                    showToast("Oops!", "Email Already taken!", "error")
                } else {
                    if(validator.signInWithEmailOtp(newUser)) {
                        isLoading = false // Stop loading on success
                        showTermsAndConditionsDialog = true
                    } else {
                        isLoading = false // Stop loading on failure
                        showToast("Error", "Failed to initiate sign up.", "error")
                    }
                }
            } catch (e: Exception) {
                isLoading = false // Stop loading on crash
                println("Error sending OTP: ${e.message}")
                e.printStackTrace()
                showToast("Error", "An unexpected error occurred.", "error")
            }
        }
    }

    val passwordHasMinLength = password.length >= 8
    val passwordHasDigit = password.any { it.isDigit() }

    AnimatedVisibility(
        visible = showContent,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(400)),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF0B1E46))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(35.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B1E46))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                Image(
                    painter = painterResource(id = R.drawable.labtracklogo),
                    contentDescription = "UMak Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(150.dp)
                        .align(Alignment.CenterHorizontally)
                        .offset(y = (-45).dp)
                )

                Text(
                    text = "UMak LabTrack",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.offset(y = (-55).dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-50).dp)
                        .background(Color(0xFFF9F9F9), RoundedCornerShape(topStart = 24.dp, topEnd = 28.dp))
                        .padding(horizontal = 10.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SIGN UP", fontSize = 40.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Text("Let's get you set up!", color = Color(0xFF182C55), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    InputField(lastName, "Type your last name", { lastName = it; lastNameError = false }, lastNameError)
                    InputField(firstName, "Type your first name", { firstName = it; firstNameError = false }, firstNameError)
                    InputField(email, "Type your UMak email", { email = it; emailError = false }, emailError)
                    InputField(
                        contactNumber,
                        "Type your contact number",
                        { contactNumber = it.filter { ch -> ch.isDigit() }.take(11); contactError = false },
                        contactError,
                        KeyboardType.Number
                    )

                    PasswordField(password, "Type your password", showPassword, passwordError, { showPassword = !showPassword }, {
                        password = it; passwordError = false
                    })
                    PasswordField(confirmPassword, "Confirm password", showConfirmPassword, confirmPasswordError, { showConfirmPassword = !showConfirmPassword }, {
                        confirmPassword = it; confirmPasswordError = false
                    })

                    Spacer(modifier = Modifier.height(6.dp))
                    Column(modifier = Modifier.fillMaxWidth(0.9f), horizontalAlignment = Alignment.Start) {
                        val okColor = Color(0xFF14AE5C)
                        val neutralColor = Color(0xFF6E6E6E)
                        Text("Password must contain at least 8 characters.", color = if (passwordHasMinLength) okColor else neutralColor, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Password must contain a number.", color = if (passwordHasDigit) okColor else neutralColor, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- MODIFIED BUTTON WITH LOADING ---
                    Button(
                        onClick = { validateForm() },
                        enabled = !isLoading, // Disable button when loading
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD740),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFFFFD740).copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(46.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text("SIGN UP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    // -----------------------------------

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedVisibility(
                        visible = showToast,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(400)),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(400))
                    ) {
                        ToastBox(toastType, toastTitle, toastMessage)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Already have an account? Login",
                        color = Color(0xFF0066CC),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .clickable { onLoginClick() }
                    )
                }
            }

            // --- DIALOGS SECTION ---

            // 1. Terms and Conditions Dialog
            if (showTermsAndConditionsDialog) {
                TermsAndConditionsDialog(
                    onNextClicked = {
                        showTermsAndConditionsDialog = false
                        showPrivacyPolicyDialog = true // Proceed to Privacy Policy
                    },
                    onDismiss = {
                        showTermsAndConditionsDialog = false
                    }
                )
            }

            // 2. Privacy Policy Dialog
            if (showPrivacyPolicyDialog) {
                PrivacyPolicyDialog(
                    onAgreeClicked = {
                        showPrivacyPolicyDialog = false
                        // SUCCESS: Now show toast and navigate
                        showToast("Success!", "Successfully Registered!", "success")
                        coroutineScope.launch {
                            delay(2000)
                            onSignUpSuccess()
                        }
                    },
                    onDismiss = {
                        showPrivacyPolicyDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun InputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var animateError by remember { mutableStateOf(false) }

    LaunchedEffect(showError) {
        animateError = showError
    }

    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                animateError = false
            },
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = if (animateError) 2.dp else 0.dp,
                    color = if (animateError) Color.Red else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        AnimatedVisibility(
            visible = showError && value.isBlank(),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(100))
        ) {
            Text("* This field is required.", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun PasswordField(
    value: String,
    placeholder: String,
    showPassword: Boolean,
    showError: Boolean,
    onToggleVisibility: () -> Unit,
    onValueChange: (String) -> Unit
) {
    var animateError by remember { mutableStateOf(false) }

    LaunchedEffect(showError) {
        animateError = showError
    }

    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                animateError = false
            },
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val iconRes = if (showPassword) R.drawable.eyeopen else R.drawable.eyeoff
                IconButton(onClick = onToggleVisibility) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Toggle password",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = if (animateError) 2.dp else 0.dp,
                    color = if (animateError) Color.Red else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        AnimatedVisibility(
            visible = showError && value.isBlank(),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(100))
        ) {
            Text("* This field is required.", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ToastBox(type: String, title: String, message: String) {
    val (bgColor, iconRes) = when (type) {
        "success" -> Pair(Color(0xFF14AE5C), R.drawable.check)
        "warning" -> Pair(Color(0xFFFFCE3D), R.drawable.alerttriangle)
        else -> Pair(Color(0xFFDC3545), R.drawable.xcircle)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(Color.White, RoundedCornerShape(6.dp))
            .border(3.dp, bgColor, RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(bgColor, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = iconRes), contentDescription = title)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = bgColor, fontSize = 12.sp)
            Text(message, fontSize = 12.sp, color = Color.Black)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpPreview() {
    SignUpScreen(previewMode = true)
}