package com.example.umaklabtrack.borrowermodule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.umaklabtrack.R
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.entityManagement.ItemManage
import com.example.umaklabtrack.preferences.SessionPreferences
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

// --- Constants for the Wheel Picker ---
private val ITEM_HEIGHT = 48.dp
private val VISIBLE_ITEMS = 3

val itmLoan = ItemManage()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanInformationSlipDialog(
    onDismiss: () -> Unit,
    onGoBack: () -> Unit,
    onConfirm: (subject: String, college: String) -> Unit // Removed section parameter
) {
    // --- Toast logic ---
    var toastMessage by remember { mutableStateOf("") }
    var toastTitle by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var toastType by remember { mutableStateOf("") }

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
    var room by remember { mutableStateOf("") } // Added Room State

    // --- Student List Management ---
    val studentList = remember { mutableStateListOf<String>() }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showRemoveStudentDialog by remember { mutableStateOf(false) }
    var studentToRemove by remember { mutableStateOf("") }

    // --- DATE & TIME STATE ---
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())

    // Initial Loan time = Now
    var loanDateTimeStr by remember { mutableStateOf(dateFormat.format(calendar.time)) }
    // Return time = Empty (User must select)
    var returnDateTimeStr by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }

    // Flag to know which field is being edited (Loan Time vs Return Time)
    var isSelectingLoanTime by remember { mutableStateOf(true) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    // --- Error States ---
    var subjectError by remember { mutableStateOf(false) }
    var collegeError by remember { mutableStateOf(false) }
    var roomError by remember { mutableStateOf(false) }
    var returnDateError by remember { mutableStateOf(false) }
    var studentListError by remember { mutableStateOf(false) }

    // --- Dropdown Data ---
    val allColleges = listOf(
        "College of Computing and Information Sciences (CCIS)",
        "Institute of Pharmacy (IOP)",
        "Institute of Imaging Health Science (IIHS)",
        "College of Construction Sciences and Engineering (CCSE)"
    )

    val allRooms = listOf(
        "HPSB 701", "HPSB 702", "HPSB 703", "HPSB 704", "HPSB 705", "HPSB 901", "HPSB 902", "HPSB 903", "HPSB 904", "HPSB 905"
    )

    val filteredColleges = when (subject) {
        "Biology", "Chemistry Lab" -> listOf("Institute of Pharmacy (IOP)", "Institute of Imaging Health Science (IIHS)")
        "Modern Physics" -> allColleges
        else -> allColleges
    }

    // --- MAIN DIALOG ---
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
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .shadow(4.dp)
                    .fillMaxWidth(0.95f) // <--- This makes it take 95% of the screen width
                    .heightIn(max = 750.dp)
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
                                text = "Loan",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = poppins,
                                    color = Color.Gray
                                )
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                            Icon(Icons.Default.Close, "Close", tint = Color(0xFF202020))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(Modifier.height(16.dp))

                    // --- Form Fields ---
                    FormSection(label = "Professor Name:") {
                        Text(text = "Prof. $professorName", style = TextStyle(fontFamily = poppins, fontSize = 14.sp))
                    }

                    FormSection(label = "Subject:") {
                        DropdownInput(
                            options = listOf("Modern Physics", "Biology", "Chemistry Lab"),
                            selectedOption = subject,
                            onOptionSelected = {
                                subject = it
                                subjectError = false
                                college = ""
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
                            },
                            placeholder = "Select college",
                            isError = collegeError,
                            enabled = !showToast
                        )
                    }

                    // --- ROOM FIELD (Renamed label to Room Number as implied by list) ---
                    FormSection(label = "Room Number:") {
                        DropdownInput(
                            options = allRooms,
                            selectedOption = room,
                            onOptionSelected = {
                                room = it
                                roomError = false
                            },
                            placeholder = "Select room",
                            isError = roomError,
                            enabled = !showToast
                        )
                    }

                    // --- STUDENT LIST SECTION ---
                    FormSection(label = "Student Representatives:") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (studentListError) 2.dp else 0.dp,
                                    color = if (studentListError) Color.Red else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(if (studentListError) 8.dp else 0.dp)
                        ) {
                            if (studentList.isEmpty()) {
                                Text("Names will appear once added.", style = TextStyle(fontFamily = poppins, fontSize = 14.sp, color = Color.Gray))
                            } else {
                                studentList.forEach { studentName ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = "Remove", tint = Color(0xFFDC3545), modifier = Modifier.size(24.dp).clickable { studentToRemove = studentName; showRemoveStudentDialog = true })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = studentName, style = TextStyle(fontFamily = poppins, fontSize = 14.sp, color = Color(0xFF202020)))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddStudentDialog = true }, modifier = Modifier.fillMaxWidth().height(45.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF425275)), shape = RoundedCornerShape(6.dp)) {
                        Text(text = "Add student", style = TextStyle(fontFamily = poppins, color = Color.White, fontSize = 14.sp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- DATE & TIME OF LOAN (Calendar Icon) ---
                    FormSection(label = "Date & Time of Loan:") {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = loanDateTimeStr,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                // CALENDAR ICON
                                trailingIcon = { Icon(Icons.Default.DateRange, "Calendar", tint = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledContainerColor = Color.White,
                                    disabledBorderColor = Color(0xFFE0E0E0),
                                    disabledTrailingIconColor = Color.Gray
                                ),
                                textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        if (!showToast) {
                                            isSelectingLoanTime = true
                                            showDatePicker = true
                                        }
                                    }
                            )
                        }
                    }

                    // --- SHOULD BE RETURNED BY (Calendar Icon + Validation) ---
                    FormSection(label = "Should be Returned by:") {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = returnDateTimeStr,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                placeholder = { Text("Should be returned by", color = Color.Gray, fontFamily = poppins, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // RED BORDER IF ERROR
                                    .border(
                                        width = if (returnDateError) 2.dp else 0.dp,
                                        color = if (returnDateError) Color.Red else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                // CALENDAR ICON
                                trailingIcon = { Icon(Icons.Default.DateRange, "Calendar", tint = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledContainerColor = Color.White,
                                    disabledBorderColor = Color(0xFFE0E0E0),
                                    disabledPlaceholderColor = Color.Gray,
                                    disabledTrailingIconColor = Color.Gray
                                ),
                                textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            // MANUAL SELECTION CLICKABLE
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        if (!showToast) {
                                            isSelectingLoanTime = false
                                            showDatePicker = true
                                        }
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- REMINDERS ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Reminders:",
                                style = TextStyle(
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. You’ll be keeping this for the whole current semester. Please handle with care.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                            Text("2. Double-check all items and information before confirming.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                            Text("3. Reagent containers, bottles, and other apparatus must be returned to the Central Laboratory clean and dry.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                            Text("4. Replace any damaged or broken apparatus as soon as possible.", style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { isChecked = it }
                            )
                            Text(
                                text = "I have read and understood all reminders.",
                                style = TextStyle(fontFamily = poppins, fontSize = 12.sp, color = Color.Gray)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(Modifier.height(16.dp))

                    // --- CONFIRM BUTTON ---
                    AnimatedVisibility(
                        visible = !showToast,
                        modifier = Modifier.fillMaxWidth(),
                        enter = fadeIn(animationSpec = tween(300, delayMillis = 300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Button(
                            onClick = {
                                subjectError = subject.isEmpty()
                                collegeError = college.isEmpty()
                                roomError = room.isEmpty() // Validate Room
                                returnDateError = returnDateTimeStr.isEmpty()
                                studentListError = studentList.isEmpty()

                                val isFormValid = !subjectError && !collegeError && !roomError && !returnDateError && !studentListError

                                if (isFormValid) {
                                    UserSession.college = college
                                    UserSession.subject = subject
                                    UserSession.room = room
                                    UserSession.listStud=studentList


                                    showToast("Info!", "Your loan request has been sent.", "info")

                                    coroutineScope.launch {
                                        delay(2000)
                                        onConfirm(subject, college)
                                    }
                                } else {
                                    showToast("Warning!", "Please fill all the required fields.", "warning")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = isChecked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isChecked) Color(0xFF182C55) else Color(0xFF182C55).copy(alpha = 0.9f)
                                , // active vs inactive color
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Confirm", style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                    }

                    AnimatedVisibility(
                        visible = showToast,
                        modifier = Modifier.fillMaxWidth(),
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300, delayMillis = 300))
                    ) {
                        ToastBoxesLoan(
                            type = toastType,
                            title = toastTitle,
                            message = toastMessage
                        )
                    }
                }
            }
        }
    }

    // --- DATE PICKER ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("OK", color = Color(0xFF182C55))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.Red)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- CUSTOM TIME PICKER ---
    if (showTimePicker) {
        SpinnerTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute, amPm ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

                val hourInt = hour.toInt()
                cal.set(Calendar.HOUR, if (hourInt == 12) 0 else hourInt)
                cal.set(Calendar.MINUTE, minute.toInt())
                cal.set(Calendar.AM_PM, if (amPm == "AM") Calendar.AM else Calendar.PM)

                val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                val formattedTime = sdf.format(cal.time)

                if (isSelectingLoanTime) {
                    loanDateTimeStr = formattedTime
                } else {
                    returnDateTimeStr = formattedTime
                    returnDateError = false // Clear error
                }
                showTimePicker = false
            }
        )
    }

    // --- Add/Remove Student Dialogs ---
    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { showAddStudentDialog = false },
            onAdd = { name ->
                if (name.isNotBlank()) {
                    studentList.add(name)
                    studentListError = false // Clear error
                }
                showAddStudentDialog = false
            }
        )
    }
    if (showRemoveStudentDialog) {
        RemoveStudentDialog(studentName = studentToRemove, onDismiss = { showRemoveStudentDialog = false }, onRemove = { studentList.remove(studentToRemove); showRemoveStudentDialog = false })
    }
}

// ==========================================
// --- CUSTOM SPINNER TIME PICKER COMPONENTS ---
// ==========================================

@Composable
private fun SpinnerTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: String, minute: String, amPm: String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF2C2C2C) else Color.White
    val contentColor = if (isDark) Color.White else Color.Black
    val highlightColor = if (isDark) Color(0xFFFFCE3D) else Color(0xFF182C55)
    val unselectedColor = if (isDark) Color.Gray else Color.Gray
    val buttonColor = if (isDark) Color(0xFFFFCE3D) else Color(0xFF182C55)

    val hours = (1..12).map { "%02d".format(it) }
    val minutes = (0..59).map { "%02d".format(it) }
    val amPm = listOf("AM", "PM")

    val currentHour = Calendar.getInstance().get(Calendar.HOUR).let { if (it == 0) 12 else it }
    var selectedHour by remember { mutableStateOf("%02d".format(currentHour)) }
    var selectedMinute by remember { mutableStateOf("%02d".format(Calendar.getInstance().get(Calendar.MINUTE))) }
    var selectedAmPm by remember { mutableStateOf(if (Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM") }

    val itemHeight = ITEM_HEIGHT
    val pickerHeight = itemHeight * VISIBLE_ITEMS
    val verticalPadding = (pickerHeight / 2) - (itemHeight / 2)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Set start time",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                )
                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pickerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WheelPicker(
                            items = hours,
                            initialItem = selectedHour,
                            onItemSelected = { selectedHour = it },
                            highlightColor = highlightColor,
                            textColor = unselectedColor,
                            modifier = Modifier.width(80.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            ":",
                            color = highlightColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        WheelPicker(
                            items = minutes,
                            initialItem = selectedMinute,
                            onItemSelected = { selectedMinute = it },
                            highlightColor = highlightColor,
                            textColor = unselectedColor,
                            modifier = Modifier.width(80.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        WheelPicker(
                            items = amPm,
                            initialItem = selectedAmPm,
                            onItemSelected = { selectedAmPm = it },
                            highlightColor = highlightColor,
                            textColor = unselectedColor,
                            modifier = Modifier.width(80.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().height(pickerHeight),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(verticalPadding))
                        Divider(color = highlightColor.copy(alpha = 0.5f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(itemHeight - 2.dp))
                        Divider(color = highlightColor.copy(alpha = 0.5f), thickness = 1.dp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = buttonColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    TextButton(onClick = { onConfirm(selectedHour, selectedMinute, selectedAmPm) }) {
                        Text("OK", color = buttonColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WheelPicker(
    items: List<String>,
    initialItem: String,
    onItemSelected: (String) -> Unit,
    highlightColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    itemHeight: Dp = ITEM_HEIGHT,
    visibleItems: Int = VISIBLE_ITEMS,
) {
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val pickerHeight = itemHeight * visibleItems
    val verticalPadding = (pickerHeight / 2) - (itemHeight / 2)

    val initialIndex = items.indexOf(initialItem).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val selectedIndex by remember {
        derivedStateOf {
            if (listState.isScrollInProgress) -1
            else (listState.firstVisibleItemIndex + (listState.firstVisibleItemScrollOffset / itemHeightPx).roundToInt()).coerceIn(0, items.lastIndex)
        }
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex != -1) {
            onItemSelected(items[selectedIndex])
        }
    }

    Box(modifier = modifier.height(pickerHeight)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = verticalPadding),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = (index == selectedIndex)
                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = TextStyle(
                            fontFamily = poppins,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isSelected) 22.sp else 20.sp,
                            color = if (isSelected) highlightColor else textColor
                        )
                    )
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
private fun FormSection(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(label, style = TextStyle(fontSize = 14.sp, fontFamily = poppins, fontWeight = FontWeight(700), color = Color(0xFF202020)))
        Spacer(Modifier.height(4.dp))
        content()
    }
}

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
    LaunchedEffect(isError) { animateError = isError }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = { Text(placeholder, fontFamily = poppins, fontSize = 14.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF5F5F5), unfocusedBorderColor = Color(0xFFE0E0E0), disabledContainerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.fillMaxWidth().menuAnchor().border(width = if (animateError) 2.dp else 0.dp, color = if (animateError) Color.Red else Color.Transparent, shape = RoundedCornerShape(8.dp)),
            textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp, color = Color.Black),
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option, fontFamily = poppins, fontSize = 14.sp) }, onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
}

@Composable
fun ToastBoxesLoan(type: String, title: String, message: String) {
    val (bgColor, iconRes) = when (type) {
        "info" -> Pair(Color(0xFF1288BF), R.drawable.alertcircle)
        "warning" -> Pair(Color(0xFFFFCE3D), R.drawable.alerttriangle)
        else -> Pair(Color(0xFFDC3545), R.drawable.xcircle)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
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

// --- ADD/REMOVE STUDENT DIALOGS ---
@Composable
fun AddStudentLoanDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Last Name, First Name", color = Color.Gray, fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedBorderColor = Color(0xFFE0E0E0)), shape = RoundedCornerShape(8.dp))
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { onAdd(name) }, modifier = Modifier.fillMaxWidth().height(45.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182C55)), shape = RoundedCornerShape(8.dp)) { Text("Add Student", fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(45.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFDC3545)), shape = RoundedCornerShape(8.dp)) { Text("Cancel", color = Color(0xFFDC3545), fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun RemoveStudentLoanDialog(studentName: String, onDismiss: () -> Unit, onRemove: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = buildAnnotatedString { append("Do you really want to\nremove "); withStyle(style = SpanStyle(color = Color(0xFFDC3545))) { append("$studentName?") } }, textAlign = TextAlign.Center, style = TextStyle(fontSize = 16.sp, fontFamily = poppins, fontWeight = FontWeight.Medium, color = Color(0xFF202020)))
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRemove, modifier = Modifier.fillMaxWidth().height(45.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)), shape = RoundedCornerShape(8.dp)) { Text("Remove", fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(45.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFF182C55)), shape = RoundedCornerShape(8.dp)) { Text("Keep", color = Color(0xFF182C55), fontWeight = FontWeight.Bold) }
            }
        }
    }
}