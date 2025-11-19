package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.umaklabtrack.entityManagement.CredentialsValidation
import com.example.umaklabtrack.R

private val crdtuser = CredentialsValidation()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    credentialsValidation: CredentialsValidation = CredentialsValidation(),
    onBackClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},      // For Regular Users
    onAdminLoginSuccess: () -> Unit = {}, // ✅ NEW: For Admin
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    previewMode: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showContent by remember { mutableStateOf(previewMode) }

    if (!previewMode) {
        LaunchedEffect(Unit) { showContent = true }
    }

    var rememberMe by remember { mutableStateOf(crdtuser.isRememberMe(context)) }

    var email by remember { mutableStateOf(if (rememberMe) crdtuser.getSavedEmail(context) else "") }
    var password by remember { mutableStateOf(if (rememberMe) crdtuser.getSavedPassword(context) else "") }

    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

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

    fun validateLogin() {
        emailError = false
        passwordError = false

        var hasError = false

        if (email.isBlank()) {
            emailError = true
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = true
            hasError = true
        }

        if (hasError) {
            showToast("Warning!", "Please fill all required fields.", "warning")
            return
        }

        // ------------------------------------------------
        // ✅ HARD CODED ADMIN LOGIN LOGIC
        // ------------------------------------------------
        if (email == "admin@umak.edu.ph" && password == "admin213") {
            showToast("Success!", "Admin login successful!", "success")
            coroutineScope.launch {
                delay(1500)
                // ✅ Call the ADMIN specific callback
                onAdminLoginSuccess()
            }
            return
        }
        // ------------------------------------------------

        if (!email.endsWith("@umak.edu.ph")) {
            emailError = true
            showToast("Oops!", "Account does not exist.", "error")
            return
        }

        coroutineScope.launch {
            val isLoggedIn = crdtuser.loginUser(
                email,
                crdtuser.hashPassword(password),
                rememberMe,
                context,
                password
            )

            if (isLoggedIn) {
                showToast("Success!", "Login successful!", "success")
                delay(1500)
                onLoginSuccess()
            } else {
                emailError = true
                passwordError = true
                showToast("Oops!", "Incorrect email or password!", "error")
            }
        }
    }

    AnimatedVisibility(
        visible = showContent,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(400)),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(animationSpec = tween(400))
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
                        .background(
                            Color(0xFFF9F9F9),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 28.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("LOGIN", fontSize = 40.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Text("Glad you're back!", color = Color(0xFF182C55), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    InputField(
                        email,
                        "Type your UMak email",
                        {
                            email = it
                            if (emailError && email.isNotBlank()) emailError = false
                        },
                        emailError
                    )

                    PasswordField(
                        password,
                        "Type your password",
                        showPassword,
                        passwordError,
                        { showPassword = !showPassword },
                        {
                            password = it
                            if (passwordError && password.isNotBlank()) passwordError = false
                        }
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .offset(x = (-10).dp, y = (-8).dp)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0B1E46))
                            )
                            Text("Remember me", fontSize = 14.sp, color = Color.Black)
                        }

                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFF0066CC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onForgotPasswordClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                delay(500)
                                validateLogin()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD740),
                            contentColor = Color.Black
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
                        Text("LOGIN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    AnimatedVisibility(
                        visible = showToast,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(400)),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(400))
                    ) {
                        ToastBox(toastType, toastTitle, toastMessage)
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "Doesn’t have an account? Sign Up",
                        color = Color(0xFF0066CC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .clickable { onSignUpClick() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    LoginScreen(previewMode = true)
}