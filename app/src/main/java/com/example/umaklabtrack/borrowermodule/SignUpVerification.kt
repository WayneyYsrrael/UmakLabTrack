package com.example.umaklabtrack.borrowermodule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.ui.theme.Poppins
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.umaklabtrack.entityManagement.CredentialsValidation
import com.example.umaklabtrack.dataClasses.User


private val credentialsValidation = CredentialsValidation()

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun SignUpVerificationScreen(
    onExitClick: () -> Unit = {},
    onVerificationSuccess: () -> Unit = {},
    showInitialToast: Boolean = true,
    previewMode: Boolean = false

) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showContent by remember { mutableStateOf(previewMode) }
    if (!previewMode) {
        LaunchedEffect(Unit) { showContent = true }
    }

    var codes by remember { mutableStateOf(List(6) { "" }) }
    var toastMessage by remember { mutableStateOf("") }
    var toastTitle by remember { mutableStateOf("") }
    var toastType by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    var resendAvailable by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(90) }

    var isError by remember { mutableStateOf(false) }

    fun showVerificationToast(title: String, message: String, type: String) {
        toastTitle = title
        toastMessage = message
        toastType = type
        showToast = true
        coroutineScope.launch {
            delay(3000)
            showToast = false
        }
    }

    var initialToastShown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        resendAvailable = true
    }


    LaunchedEffect(showInitialToast) {
        if (showInitialToast && !initialToastShown) {
            initialToastShown = true
            delay(300)
            showVerificationToast(
                "Info",
                "Please check your email. A 6-digit verification code has been sent to you.",
                "info"
            )
        }
    }

    fun getCredentialsValidation(): CredentialsValidation = CredentialsValidation()
    fun createUserFromSession(): User {
        return User(
            name = UserSession.name ?: "",
            email = UserSession.email ?: "",
            contact = UserSession.cNum ?: "",
            hashedPassword = UserSession.hashedPassword ?: "",
           role = UserSession.ROLE ?: ""
        )
    }
    fun handleVerification() {
        val code = codes.joinToString("")

        if (code.isBlank() || code.length < 6) {
            isError = true
            showVerificationToast("Warning!", "Please fill all the required fields.", "warning")
            return
        }

        coroutineScope.launch {
            val credentialsValidation = getCredentialsValidation()
            val email = UserSession.email
            val newUser = createUserFromSession()

            if (email != null) {
                val isOtpValid = credentialsValidation.verifyEmailOtp(email, code)
                if (isOtpValid) {
                    isError = false
                    showVerificationToast("Success!", "Verification Successful!", "success")
                    delay(1500)
                    credentialsValidation.insertUser(newUser)
                    onVerificationSuccess()
                    UserSession.clear()
                } else {
                    isError = true
                    showVerificationToast("Oops!", "The verification code is incorrect.", "error")
                }
            } else {
                println("Email is null — cannot verify OTP")
            }
        }
    }
    fun handleResend() {
        if (!resendAvailable) return
        resendAvailable = false
        countdown = 90

        coroutineScope.launch {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            resendAvailable = true
        }

        val credentialsValidation = CredentialsValidation()
        val newUser = createUserFromSession()
        val email = UserSession.email

        if (email != null) {
            coroutineScope.launch {
                try {
      // Send OTP after countdown started
                    val isSent = credentialsValidation.signInWithEmailOtp(newUser)

                    if (isSent) {
                        showVerificationToast("Info", "Code sent! Check your inbox.", "info")
                    } else {
                        showVerificationToast("Oops!", "Failed to send code. Try again.", "error")
                    }

                } catch (e: Exception) {
                    println("Error sending OTP: ${e.message}")
                    e.printStackTrace()

                }
            }
        } else {
            println("Email is null — cannot send OTP")
        }
    }



    val focusRequesters = List(6) { FocusRequester() }

    AnimatedVisibility(
        visible = showContent,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(400)),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(animationSpec = tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1E46))
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 1.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = onExitClick,
                        modifier = Modifier.offset(x = (-10).dp, y = 28.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.exitbutton),
                            contentDescription = "Exit",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Image(
                    painter = painterResource(id = R.drawable.labtracklogo),
                    contentDescription = "UMak Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(150.dp)
                        .align(Alignment.CenterHorizontally)
                        .offset(y = (-15).dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "UMak LabTrack",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.offset(y = (-35).dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .background(
                        Color(0xFFF9F9F9),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SIGN UP", fontSize = 40.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Text("Let's get you set up!", color = Color(0xFF182C55), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Verification Code:",
                    color = Color.Black,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 17.dp),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    codes.forEachIndexed { index, value ->
                        VerificationBox(
                            value = value,
                            focusRequester = focusRequesters[index],
                            onValueChange = { newValue ->
                                val updated = codes.toMutableList()
                                updated[index] = newValue
                                codes = updated
                                if (isError) isError = false
                                if (newValue.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                } else if (newValue.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            },
                            isError = isError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { handleVerification() },
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
                    Text("VERIFY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (resendAvailable) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = "Didn’t get your code? ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Resend",
                            color = Color(0xFF0066CC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { handleResend() }
                        )
                    }
                } else {
                    Text(
                        text = "Didn’t get your code? Try again in ${countdown}s.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedVisibility(
                    visible = showToast,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(400)),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(400))
                ) {
                    VerificationToastBox(toastType, toastTitle, toastMessage)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun VerificationBox(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    isError: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = {
            if (it.length <= 1 && it.all { ch -> ch.isDigit() }) {
                onValueChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(42.dp)
            .height(50.dp)
            .border(
                width = 1.dp,
                color = when {
                    isError -> Color(0xFFE53935)
                    isFocused -> Color(0xFF182C55)
                    else -> Color(0xFF9CA3AF)
                },
                shape = RoundedCornerShape(5.dp)
            )
            .background(Color.White, RoundedCornerShape(5.dp))
            .onFocusChanged { focusState -> isFocused = focusState.isFocused }
            .focusRequester(focusRequester),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 24.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF182C55),
            textAlign = TextAlign.Center
        ),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { innerTextField() }
        },
        visualTransformation = VisualTransformation.None,
        interactionSource = remember { MutableInteractionSource() }
    )
}

@Composable
fun VerificationToastBox(type: String, title: String, message: String) {
    val (icon, backgroundColor) = when (type.lowercase()) {
        "success" -> Pair(R.drawable.check, Color(0xFF4CAF50))
        "error" -> Pair(R.drawable.xcircle, Color(0xFFE53935))
        "info" -> Pair(R.drawable.alertcircle, Color(0xFF2196F3))
        "warning" -> Pair(R.drawable.alerttriangle, Color(0xFFFFCE3D))
        else -> Pair(R.drawable.alertcircle, Color.Gray)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(4.dp, backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(backgroundColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Toast Icon",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(backgroundColor)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(title, color = backgroundColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(message, color = Color.Black, fontSize = 13.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpVerificationPreview() {
    SignUpVerificationScreen(previewMode = true)
}
