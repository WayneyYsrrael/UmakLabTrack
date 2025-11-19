package com.example.umaklabtrack.borrowermodule

import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
// --- NEW IMPORTS ---
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
// --- END NEW IMPORTS ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
// --- NEW IMPORTS ---
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
// --- END NEW IMPORTS ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
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
import androidx.compose.material3.TextButton
// --- REMOVED IMPORTS ---
// import androidx.compose.material3.TimePicker
// import androidx.compose.material3.TimePickerLayoutType
// import androidx.compose.material3.TimePickerState
// import androidx.compose.material3.rememberTimePickerState
// --- END REMOVED IMPORTS ---
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
// --- NEW IMPORTS ---
import androidx.compose.runtime.derivedStateOf
// --- END NEW IMPORTS ---
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
// --- NEW IMPORTS ---
import androidx.compose.ui.platform.LocalDensity
// --- END NEW IMPORTS ---
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.umaklabtrack.R
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.ui.theme.poppins
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// --- NEW IMPORTS ---
import kotlin.math.roundToInt
// --- END NEW IMPORTS ---

// --- NEW: Define constants for the picker geometry ---
private val ITEM_HEIGHT = 48.dp
private val VISIBLE_ITEMS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanInformationSlipDialog(
    onDismiss: () -> Unit,
    onGoBack: () -> Unit,
    onConfirm: (subject: String, room: String, college: String, section: String) -> Unit
) {
    // --- Toast logic (Omitted for brevity, kept as is) ---
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
            delay(2000)
            if (toastType == "warning") {
                showToast = false
            }
        }
    }

    // --- State Management (Omitted for brevity, kept as is) ---
    var subject by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var yearAndSection by remember { mutableStateOf("") }

    // --- Date/Time State ---
    var scheduledPickUp by remember { mutableStateOf("") }
    var returnBy by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    // --- REMOVED: val timePickerState = rememberTimePickerState(is24Hour = false) ---
    // --- End Date/Time State ---

    // --- Other states (Omitted for brevity, kept as is) ---
    val requestDateTime = remember {
        Calendar.getInstance().formatDateTime("MMMM dd, yyyy - hh:mm a")
    }
    var subjectError by remember { mutableStateOf(false) }
    var roomError by remember { mutableStateOf(false) }
    var collegeError by remember { mutableStateOf(false) }
    var yearAndSectionError by remember { mutableStateOf(false) }
    var scheduledPickUpError by remember { mutableStateOf(false) }
    val allColleges = listOf(
        "College of Computing and Information Sciences (CCIS)",
        "Institute of Pharmacy (IOP)",
        "Institute of Imaging Health Science (IIHS)",
        "College of Construction Sciences and Engineering (CCSE)"
    )
    val allRooms = listOf(
        "HPSB 701", "HPSB 702", "HPSB 703", "HPSB 704", "HPSB 705", "HPSB 901", "HPSB 902", "HPSB 903", "HPSB 904", "HPSB 905"
    )
    val allSections = listOf(
        "II - ACSAD", "II - BCSAD", "II - CCSAD", "II - DCSAD", "II - BCOMP", "III - APHYS", "III - ABINS"
    )
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

    // --- MAIN DIALOG (Omitted for brevity, kept as is) ---
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
                    .heightIn(max = 620.dp) // Max height is fine
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

                    // --- Form Fields (Omitted for brevity, kept as is) ---
                    FormSection(label = "Professor Name:") {
                        Text("Prof. Susan Guevarra", style = TextStyle(fontFamily = poppins, fontSize = 14.sp))
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

                    FormSection(label = "Room:") {
                        DropdownInput(
                            options = allRooms,
                            selectedOption = room,
                            onOptionSelected = {
                                room = it
                                roomError = false
                            },
                            placeholder = "Select room number",
                            isError = roomError,
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

                    FormSection(label = "Date & Time of Request:") {
                        Text(requestDateTime, style = TextStyle(fontFamily = poppins, fontSize = 14.sp))
                    }

                    FormSection(label = "Scheduled Pick-Up:") {
                        DateTimePickerInput(
                            text = scheduledPickUp,
                            placeholder = "mm/dd/yy - 00:00 AM",
                            onClick = {
                                if (!showToast) showDatePicker = true // Show date picker
                            },
                            enabled = !showToast,
                            isError = scheduledPickUpError
                        )
                    }

                    FormSection(label = "Should be Returned by:") {
                        val returnText = if (returnBy.isEmpty()) "Please select pick up schedule first." else returnBy
                        val returnColor = if (returnBy.isEmpty()) Color.Gray else Color.Black
                        Text(
                            returnText,
                            style = TextStyle(
                                fontFamily = poppins,
                                fontSize = 14.sp,
                                color = returnColor
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(Modifier.height(16.dp))

                    // --- Confirm Button / Toast ---
                    AnimatedVisibility(
                        visible = !showToast,
                        modifier = Modifier.fillMaxWidth(),
                        enter = fadeIn(animationSpec = tween(300, delayMillis = 300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Button(
                            onClick = {
                                // --- Validation ---
                                subjectError = subject.isEmpty()
                                roomError = room.isEmpty()
                                collegeError = college.isEmpty()
                                yearAndSectionError = yearAndSection.isEmpty()
                                scheduledPickUpError = scheduledPickUp.isEmpty()

                                val isFormValid = subject.isNotEmpty() &&
                                        room.isNotEmpty() &&
                                        college.isNotEmpty() &&
                                        yearAndSection.isNotEmpty() &&
                                        scheduledPickUp.isNotEmpty()

                                if (isFormValid) {
                                    UserSession.college=college
                                    UserSession.yearSection=yearAndSection
                                    UserSession.subject=subject
                                    UserSession.room=room

                                    showToast("Info!", "Your loan request has been sent. Please wait for approval.", "info")
                                    coroutineScope.launch {
                                        delay(2000)
                                        onConfirm(subject, room, college, yearAndSection)
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
    } // <-- END OF MAIN DIALOG

    // --- Date/Time pickers ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true // Show Time Picker next
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    // --- REPLACED: This now calls our new SpinnerTimePickerDialog ---
    if (showTimePicker) {
        SpinnerTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute, amPm ->
                val cal = Calendar.getInstance()
                // Get the selected date
                cal.timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

                // Set the selected time
                val hourInt = hour.toInt()
                // Calendar.HOUR is for 12-hour format. 12 AM/PM is 0.
                cal.set(Calendar.HOUR, if (hourInt == 12) 0 else hourInt)
                cal.set(Calendar.MINUTE, minute.toInt())
                cal.set(Calendar.AM_PM, if (amPm == "AM") Calendar.AM else Calendar.PM)

                // Set the final values
                scheduledPickUp = cal.formatDateTime("MMM dd, yyyy - hh:mm a")
                scheduledPickUpError = false // Clear error

                // Calculate return time
                cal.add(Calendar.HOUR_OF_DAY, 3)
                returnBy = cal.formatDateTime("MMM dd, yyyy - hh:mm a")

                showTimePicker = false
            }
        )
    }
}

// --- NEW COMPOSABLE: SpinnerTimePickerDialog ---
@Composable
private fun SpinnerTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: String, minute: String, amPm: String) -> Unit
) {
    // --- Define Colors based on theme ---
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF2C2C2C) else Color.White // Darker gray for dialog
    val contentColor = if (isDark) Color.White else Color.Black
    val highlightColor = if (isDark) Color(0xFFFFCE3D) else Color(0xFF182C55)
    val unselectedColor = if (isDark) Color.Gray else Color.Gray
    val buttonColor = if (isDark) Color(0xFFFFCE3D) else Color(0xFF182C55)

    // --- Define Time Options ---
    val hours = (1..12).map { "%02d".format(it) }
    val minutes = (0..59).map { "%02d".format(it) }
    val amPm = listOf("AM", "PM")

    // --- State for selected items ---
    val currentHour = Calendar.getInstance().get(Calendar.HOUR).let { if (it == 0) 12 else it }
    var selectedHour by remember { mutableStateOf("%02d".format(currentHour)) }
    var selectedMinute by remember { mutableStateOf("%02d".format(Calendar.getInstance().get(Calendar.MINUTE))) }
    var selectedAmPm by remember { mutableStateOf(if (Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM") }

    // --- Picker Geometry ---
    val itemHeight = ITEM_HEIGHT
    val pickerHeight = itemHeight * VISIBLE_ITEMS
    val verticalPadding = (pickerHeight / 2) - (itemHeight / 2) // This is 48.dp

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
                    "Select time",
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
                    // --- The 3 Wheel Pickers ---
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
                            modifier = Modifier.width(80.dp),
                            itemHeight = itemHeight,
                            visibleItems = VISIBLE_ITEMS
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            ":",
                            color = highlightColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                            // fontFamily = poppins // <-- REMOVED
                        )
                        Spacer(Modifier.width(8.dp))
                        WheelPicker(
                            items = minutes,
                            initialItem = selectedMinute,
                            onItemSelected = { selectedMinute = it },
                            highlightColor = highlightColor,
                            textColor = unselectedColor,
                            modifier = Modifier.width(80.dp),
                            itemHeight = itemHeight,
                            visibleItems = VISIBLE_ITEMS
                        )
                        Spacer(Modifier.width(16.dp))
                        WheelPicker(
                            items = amPm,
                            initialItem = selectedAmPm,
                            onItemSelected = { selectedAmPm = it },
                            highlightColor = highlightColor,
                            textColor = unselectedColor,
                            modifier = Modifier.width(80.dp),
                            itemHeight = itemHeight,
                            visibleItems = VISIBLE_ITEMS
                        )
                    }

                    // --- Divider Overlay ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(pickerHeight),
                        verticalArrangement = Arrangement.Top // <-- Use Top
                    ) {
                        // Spacer to push the first divider down to the top of the centered item
                        Spacer(modifier = Modifier.height(verticalPadding)) // 48.dp
                        Divider(color = highlightColor.copy(alpha = 0.5f), thickness = 1.dp)

                        // Spacer between the dividers, equal to the item height minus the dividers
                        Spacer(modifier = Modifier.height(itemHeight - 2.dp)) // 46.dp
                        Divider(color = highlightColor.copy(alpha = 0.5f), thickness = 1.dp)
                    }
                }
                // --- End of Box ---

                Spacer(Modifier.height(24.dp))

                // --- Action Buttons ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "CANCEL",
                            color = buttonColor,
                            fontWeight = FontWeight.Bold
                            // fontFamily = poppins // <-- REMOVED
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    TextButton(onClick = { onConfirm(selectedHour, selectedMinute, selectedAmPm) }) {
                        Text(
                            "OK",
                            color = buttonColor,
                            fontWeight = FontWeight.Bold
                            // fontFamily = poppins // <-- REMOVED
                        )
                    }
                }
            }
        }
    }
}

// --- NEW COMPOSABLE: WheelPicker ---
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

    // Find the initial index
    val initialIndex = items.indexOf(initialItem).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Get the selected index based on scroll
    val selectedIndex by remember {
        derivedStateOf {
            if (listState.isScrollInProgress) {
                -1 // Don't update while scrolling
            } else {
                (listState.firstVisibleItemIndex + (listState.firstVisibleItemScrollOffset / itemHeightPx).roundToInt())
                    .coerceIn(0, items.lastIndex)
            }
        }
    }

    // Report selection change when scrolling stops
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
                // Check if this item is the one in the center
                val isSelected = (index == selectedIndex)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center // --- Set to Center ---
                ) {
                    Text(
                        text = item,
                        style = TextStyle(
                            fontFamily = poppins,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isSelected) 22.sp else 20.sp, // <-- INCREASED FONT SIZE
                            color = if (isSelected) highlightColor else textColor
                        )
                    )
                }
            }
        }
    }
}


// --- Helper function to format date/time ---
private fun Calendar.formatDateTime(pattern: String): String {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.US)
        sdf.format(this.time)
    } catch (e: Exception) {
        "Invalid Date"
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

// --- Dropdown Input ---
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

// --- DateTimePickerInput (Copied from previous file) ---
// --- DateTimePickerInput (Copied from previous file) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerInput(
    text: String,
    placeholder: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isError: Boolean
) {
    var animateError by remember { mutableStateOf(false) }
    LaunchedEffect(isError) {
        animateError = isError
    }

    // --- THIS IS THE FIX ---
    // We wrap the TextField in a Box to correctly handle the click.
    // Clicks on a disabled TextField are unreliable.
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {},
            readOnly = true,
            enabled = enabled, // Pass 'enabled' to control the visual state (grayed out)
            placeholder = { Text(placeholder, fontFamily = poppins, fontSize = 14.sp) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFF5F5F5), // Ensure disabled state looks right
                disabledTrailingIconColor = Color.Gray // Gray out icon when disabled
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (animateError) 2.dp else 0.dp,
                    color = if (animateError) Color.Red else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            textStyle = TextStyle(fontFamily = poppins, fontSize = 14.sp, color = Color.Black),
            shape = RoundedCornerShape(8.dp)
        )

        // Add a transparent, clickable overlay.
        // This Box will receive the clicks, controlled by the 'enabled' flag.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled, onClick = onClick)
        )
    }
}

// --- ToastBox Composable (New for this file) ---
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

// --- Preview ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoanInformationSlipDialogPreview() {
    LoanInformationSlipDialog(onDismiss = {}, onGoBack = {}, onConfirm = { s, r, c, sec ->
        println("Confirm: $s, $r, $c, $sec")
    })
}