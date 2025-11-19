package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.umaklabtrack.entityManagement.PasswordForgot
import com.example.umaklabtrack.dataClasses.UserSession
private val pwd= PasswordForgot()
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ForgotPasswordConfirmationScreen(
    onBackClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onYesClick: () -> Unit = {},
    previewMode: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showContent by remember { mutableStateOf(previewMode) }
    if (!previewMode) {
        LaunchedEffect(Unit) { showContent = true }
    }

    var contactNumber by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    var toastTitle by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf("") }
    var toastType by remember { mutableStateOf("") }
    var verifiedAccount by remember { mutableStateOf<String?>(null) }

    var showError by remember { mutableStateOf(false) }
    var showRequiredText by remember { mutableStateOf(false) }

    var confirmClicked by remember { mutableStateOf(false) }

    fun showToast(title: String, message: String, type: String) {
        toastTitle = title
        toastMessage = message
        toastType = type
        showToast = true
        coroutineScope.launch {
            delay(2000)
            showToast = false
        }
    }

    fun verifyContact() {
        when {
            contactNumber.isBlank() -> {
                showError = true
                showRequiredText = true
                showToast("Warning!", "Please fill all the required fields.", "warning")
            }

            contactNumber.length != 11 -> {
                showError = true
                showRequiredText = false
                showToast("Warning!", "Contact number must contain 11 digits.", "warning")
            }

            else -> {
                coroutineScope.launch {
                    val email = pwd.getEmailByPhone(contactNumber)
                    if (email==null) {
                        showError = true
                        showRequiredText = false
                        showToast(
                            "Oops!",
                            "We couldn’t find an account linked to that contact number.",
                            "error"
                        )
                    } else {
                        showToast("Success!", "Phone number verified!", "success")

                        showError = false
                        showRequiredText = false
                        confirmClicked = true
                        delay(2700)
                        confirmClicked = false
                        verifiedAccount =email
                        UserSession.email=email
                        UserSession.cNum=contactNumber
                    }
                }
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
                    Text(
                        "FORGOT PASSWORD",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text("Recover your account", color = Color(0xFF182C55), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (verifiedAccount == null) {
                        OutlinedTextField(
                            value = contactNumber,
                            onValueChange = {
                                if (it.length <= 11 && it.all { c -> c.isDigit() }) {
                                    contactNumber = it
                                    if (contactNumber.isNotBlank()) {
                                        showError = false
                                        showRequiredText = false
                                    }
                                }
                            },
                            placeholder = { Text("Type your contact number") },
                            isError = showError,
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(56.dp)
                        )

                        AnimatedVisibility(visible = showRequiredText) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 30.dp, top = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "* This field is required.",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            "Enter your phone number to help us verify your email address.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 30.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { verifyContact() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD740),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(46.dp)
                                .graphicsLayer {

                                }
                        ) {
                            Text(
                                "CONFIRM",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onCancelClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, SolidColor(Color.Red)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(46.dp)
                        ) {
                            Text(
                                "CANCEL",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }


                    AnimatedVisibility(
                        visible = verifiedAccount != null,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(400)),
                        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(animationSpec = tween(400))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                "Is this your account?",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = verifiedAccount ?: "",
                                onValueChange = {},
                                enabled = false,
                                visualTransformation = VisualTransformation.None,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(56.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    verifiedAccount?.let { email ->
                                        coroutineScope.launch {
                                            onYesClick() // Navigate first
                                            launch {
                                                val otpSent = pwd.signUpWithEmailOtp(email)
                                                if (otpSent) {
                                                    showToast("Success!", "OTP sent to $email", "success")
                                                } else {
                                                    showToast("Error!", "Failed to send OTP. Try again.", "error")
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD740)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(46.dp)
                            ) {
                                Text("YES", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }


                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { verifiedAccount = null },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, SolidColor(Color.Red)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(46.dp)
                            ) {
                                Text("NO", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedVisibility(
                        visible = showToast,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(400)),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(400))
                    ) {
                        ToastBox(toastType, toastTitle, toastMessage)
                    }
                }
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    ForgotPasswordConfirmationScreen(previewMode = true)
}
