package com.example.umaklabtrack.borrowermodule

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.VisualTransformation
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.umaklabtrack.entityManagement.PasswordForgot
import com.example.umaklabtrack.dataClasses.UserSession

private val pwd = PasswordForgot()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
    onResetSuccess: () -> Unit = {},
    previewMode: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showContent by remember { mutableStateOf(previewMode) }
    if (!previewMode) {
        LaunchedEffect(Unit) { showContent = true }
    }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var passwordsMismatch by remember { mutableStateOf(false) }

    var showToast by remember { mutableStateOf(false) }
    var toastTitle by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf("") }
    var toastType by remember { mutableStateOf("") }

    val isLengthValid = newPassword.length >= 8
    val hasNumber = newPassword.any { it.isDigit() }

    // 🔴 Highlight trigger when that specific warning toast is active
    val highlightPasswordBoxes =
        showToast && toastType == "warning" &&
                toastMessage == "Password should have at least 8 characters and a number."

    // Update mismatch dynamically if both fields are non-empty
    LaunchedEffect(newPassword, confirmPassword) {
        passwordsMismatch =
            newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword
    }

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

    fun validateReset() {
        // Step 1: Check empty fields
        newPasswordError = newPassword.isBlank()
        confirmPasswordError = confirmPassword.isBlank()
        passwordsMismatch = newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword

        if (newPasswordError || confirmPasswordError) {
            showToast("Warning!", "Please fill all required fields.", "warning")
            return
        }

        // Step 2: Password rules
        if (!isLengthValid || !hasNumber) {
            showToast("Warning!", "Password should have at least 8 characters and a number.", "warning")
            return
        }

        // Step 3: Password mismatch
        if (passwordsMismatch) {
            showToast("Oops!", "Passwords do not match!", "error")
            return
        }

        // Step 4: Update password
        coroutineScope.launch {
            try {
                val email = UserSession.email ?: ""
                val success = pwd.updatePassword(identifier = email, newPassword = newPassword)
                if (success) {
                    showToast("Success!", "Reset Password Successful!", "success")
                    delay(1500)
                    onResetSuccess()
                    UserSession.clear()
                } else {
                    showToast("Oops!", "Reset password failed!", "error")
                }
            } catch (e: Exception) {
                showToast("Oops!", "Something went wrong: ${e.message}", "error")
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
                .background(Color(0xFF0B1E46))
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.offset(x = (-10).dp, y = 28.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.exitbutton),
                            contentDescription = "Exit",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.White)
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
                    modifier = Modifier.offset(y = (-30).dp)
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
                Text("FORGOT PASSWORD", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Text("Enter your new password", color = Color(0xFF182C55), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Reset Password",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // 🔴 Highlight border if toast warning appears
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                if (newPasswordError && newPassword.isNotBlank()) newPasswordError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Type your new password") },
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = newPasswordError || passwordsMismatch || highlightPasswordBoxes,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (highlightPasswordBoxes) Color.Red else Color(0xFF182C55),
                                unfocusedBorderColor = if (highlightPasswordBoxes) Color.Red else Color.Gray,
                                errorBorderColor = Color.Red
                            )
                        )

                        if (newPasswordError) {
                            Text(
                                text = "This field is required",
                                color = Color(0xFFE53935),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                if (confirmPasswordError && confirmPassword.isNotBlank()) confirmPasswordError = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            placeholder = { Text("Confirm new password") },
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = confirmPasswordError || passwordsMismatch || highlightPasswordBoxes,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (highlightPasswordBoxes) Color.Red else Color(0xFF182C55),
                                unfocusedBorderColor = if (highlightPasswordBoxes) Color.Red else Color.Gray,
                                errorBorderColor = Color.Red
                            )
                        )

                        if (confirmPasswordError) {
                            Text(
                                text = "This field is required",
                                color = Color(0xFFE53935),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        } else if (passwordsMismatch) {
                            Text(
                                text = "Passwords do not match",
                                color = Color(0xFFE53935),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Password must contain at least 8 characters.",
                            color = if (isLengthValid) Color(0xFF388E3C) else Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Password must contain a number.",
                            color = if (hasNumber) Color(0xFF388E3C) else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { validateReset() },
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
                    Text("CONFIRM", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(previewMode = true)
}
