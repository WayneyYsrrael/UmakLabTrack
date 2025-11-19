package com.example.umaklabtrack.borrowermodule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.umaklabtrack.R
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.umaklabtrack.entityManagement.ItemManage
import com.example.umaklabtrack.utils.TimeUtils

import androidx.compose.ui.platform.LocalContext

import com.example.umaklabtrack.preferences.SessionPreferences




val itm= ItemManage()
@Composable
fun BorrowerInformationSlipDialog(
    onDismiss: () -> Unit,
    onGoBack: () -> Unit,
    onConfirm: (subject: String, college: String, section: String) -> Unit
) {
    // --- Toast logic ---
    var toastMessage by remember { mutableStateOf("") }
    var toastTitle by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var toastType by remember { mutableStateOf("") }
    val scope=rememberCoroutineScope()

    fun showToast(title: String, message: String, type: String) {
        toastTitle = title
        toastMessage = message
        toastType = type
        showToast = true
        coroutineScope.launch {
            delay(3000)
            if (toastType == "warning") {
                showToast = false
            }
        }
    }

    val context = LocalContext.current
    val sessionPrefs = remember { SessionPreferences(context) }
    var professorName by remember { mutableStateOf("Prof. Name") }

    LaunchedEffect(Unit) {
        val user = sessionPrefs.loadSession()
        professorName = user.name ?: "Prof. Name"
    }


    // --- State Management ---
    var subject by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var yearAndSection by remember { mutableStateOf("") }

    // --- Error states ---
    var subjectError by remember { mutableStateOf(false) }
    var collegeError by remember { mutableStateOf(false) }
    var yearAndSectionError by remember { mutableStateOf(false) }

    val allColleges = listOf(
        "College of Computing and Information Sciences (CCIS)",
        "Institute of Pharmacy (IOP)",
        "Institute of Imaging Health Science (IIHS)",
        "College of Construction Sciences and Engineering (CCSE)"
    )

    val allSections = listOf(
        "II - ACSAD", "II - BCSAD", "II - CCSAD", "II - DCSAD", "II - BCOMP", "III - APHYS", "III - ABINS"
    )

    // --- Dynamic Filtering ---
    val filteredColleges = when (subject) {
        "Biology", "Chemistry Lab" -> listOf("Institute of Pharmacy (IOP)", "Institute of Imaging Health Science (IIHS)")
        "Modern Physics" -> allColleges
        else -> allColleges
    }

    val filteredSections = when (college) {
        "College of Computing and Information Sciences (CCIS)" -> listOf("II - ACSAD", "II - BCSAD", "II - CCSAD", "II - DCSAD")
        "Institute of Pharmacy (IOP)" -> listOf("III - APHYS")
        "Institute of Imaging Health Science (IIHS)" -> listOf("III - ABINS")
        "College of Construction Sciences and Engineering (CCSE)" -> listOf("II - BCOMP")
        else -> allSections
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            // --- CLOSE BUTTON REMOVED FROM HERE ---

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .shadow(4.dp)
                    .width(350.dp)
                    .heightIn(max = 620.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // --- Header ---
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { if (!showToast) onGoBack() },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF202020))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Information Slip",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight(700),
                                    color = Color(0xFF182C55),
                                    textAlign = TextAlign.Center,
                                )
                            )
                            Text(
                                text = "Borrowing",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = poppins,
                                    color = Color.Gray
                                )
                            )
                        }
                        // --- CLOSE BUTTON ADDED HERE ---
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF202020) // Changed color
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(Modifier.height(16.dp))

                    // --- Form Fields ---
                    FormSection(label = "Professor Name:") {
                        Text(text="Prof. $professorName",
                            style = TextStyle(fontFamily = poppins, fontSize = 14.sp))
                    }

                    FormSection(label = "Subject:") {
                        DropdownInput(
                            options = listOf("Modern Physics", "Biology", "Chemistry Lab"),
                            selectedOption = subject,
                            onOptionSelected = {
                                subject = it
                                subjectError = false
                                college = ""
                                yearAndSection = ""
                            },
                            placeholder = "Select subject",
                            isError = subjectError,
                            enabled = !showToast
                        )
                    }

                    FormSection(label = "College:") {
                        DropdownInput(
                            options = filteredColleges,
                            selectedOption = college,
                            onOptionSelected = {
                                college = it
                                collegeError = false
                                yearAndSection = ""
                            },
                            placeholder = "Select college",
                            isError = collegeError,
                            enabled = !showToast
                        )
                    }

                    FormSection(label = "Year & Section:") {
                        DropdownInput(
                            options = filteredSections,
                            selectedOption = yearAndSection,
                            onOptionSelected = {
                                yearAndSection = it
                                yearAndSectionError = false
                            },
                            placeholder = "Select year and section",
                            isError = yearAndSectionError,
                            enabled = !showToast
                        )
                    }

                    // --- MODIFIED SECTION ---
                    FormSection(label = "Student Representatives Name:") {
                        Text(
                            text = "Names will appear once the COR is scanned.",
                            style = TextStyle(
                                fontFamily = poppins,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                lineHeight = 24.sp
                            )
                        )
                    }
                    // --- END OF MODIFICATION ---

                    FormSection(label = "Borrowing Date & Time:") {
                        Text(text= TimeUtils.rememberCurrentTime(addHours = false),
                            style = TextStyle(fontFamily = poppins, fontSize = 14.sp))
                    }

                    FormSection(label = "Should be Returned by:") {
                        Text(text=TimeUtils.rememberCurrentTime(addHours = true),
                            style = TextStyle(fontFamily = poppins, fontSize = 14.sp))
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(Modifier.height(16.dp))

                    // --- START OF UI SWAP (Confirm Button / Toast) ---
                    AnimatedVisibility(
                        visible = !showToast,
                        modifier = Modifier.fillMaxWidth(), // Added modifier
                        enter = fadeIn(animationSpec = tween(300, delayMillis = 300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Button(
                            onClick = {
                                subjectError = subject.isEmpty()
                                collegeError = college.isEmpty()
                                yearAndSectionError = yearAndSection.isEmpty()

                                val isFormValid = subject.isNotEmpty() && college.isNotEmpty() && yearAndSection.isNotEmpty()

                                if (isFormValid) {
//
                                    UserSession.college=college
                                    UserSession.yearSection=yearAndSection
                                    UserSession.subject=subject
                                    showToast("Info!", "Your request has been sent. Please wait for the approval.", "info")
                                    coroutineScope.launch {
                                        delay(2000)
                                        onConfirm(subject, college, yearAndSection)
                                    }
                                } else {
                                    showToast("Warning!", "Please fill all the required fields.", "warning")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF182C55),
                                disabledContainerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Confirm", style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                    }

                    AnimatedVisibility(
                        visible = showToast,
                        modifier = Modifier.fillMaxWidth(), // Added modifier
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300, delayMillis = 300))
                    ) {
                        ToastBoxes(
                            type = toastType,
                            title = toastTitle,
                            message = toastMessage
                        )
                    }
                    // --- END OF UI SWAP ---
                }
            }
        }
    }
}

// --- Form Section ---
@Composable
private fun FormSection(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(label, style = TextStyle(fontSize = 14.sp, fontFamily = poppins, fontWeight = FontWeight(700), color = Color(0xFF202020)))
        Spacer(Modifier.height(4.dp))
        content()
    }
}

// --- Dropdown Input (UPDATED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownInput(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    var animateError by remember { mutableStateOf(false) }
    LaunchedEffect(isError) {
        animateError = isError
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = { Text(placeholder, fontFamily = poppins, fontSize = 14.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFF5F5F5)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .border(
                    width = if (animateError) 2.dp else 0.dp,
                    color = if (animateError) Color.Red else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp, color = Color.Black),
            shape = RoundedCornerShape(8.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = poppins, fontSize = 14.sp) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BorrowerInformationSlipDialogPreview() {
    BorrowerInformationSlipDialog(onDismiss = {}, onGoBack = {}, onConfirm = { s, c, sec ->
        println("Confirm: $s, $c, $sec")
    })
}


// --- ToastBox Composable ---
@Composable
fun ToastBoxes(type: String, title: String, message: String) {
    val (bgColor, iconRes) = when (type) {
        "info" -> Pair(Color(0xFF1288BF), R.drawable.alertcircle)
        "warning" -> Pair(Color(0xFFFFCE3D), R.drawable.alerttriangle)
        else -> Pair(Color(0xFFDC3545), R.drawable.xcircle)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth() // Fills width of the Column's center alignment
            .background(Color.White, RoundedCornerShape(6.dp))
            .border(2.dp, bgColor, RoundedCornerShape(6.dp))
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