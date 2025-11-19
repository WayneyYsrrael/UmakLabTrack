package com.example.umaklabtrack.borrowermodule

// --- FUNCTIONAL IMPORTS ---
import androidx.compose.foundation.BorderStroke // <-- ADDED IMPORT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // <-- ADDED IMPORT
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu // <-- Import no longer needed
import androidx.compose.material3.Card // <-- ADDED IMPORT
import androidx.compose.material3.CardDefaults // <-- ADDED IMPORT
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins

// --- Main Slides Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidesScreen(
    onNavSelected: (String) -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    var selectedItem: ItemDetails? by remember { mutableStateOf(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Name A-Z") }
    var showActionMenu by remember { mutableStateOf(false) }

    // --- Slides Data ---
    val slidesItems = ItemRepository.slidesItems

    val filteredSlides = slidesItems.filter { it.name.contains(searchQuery, ignoreCase = true) }

    // --- Sorting Logic ---
    val sortedSlides = when (sortOption) {
        "Name A-Z" -> filteredSlides.sortedBy { it.name }
        "Name Z-A" -> filteredSlides.sortedByDescending { it.name }

        // --- THIS IS THE FIX ---
        // We use .filter{} to show ONLY the matching items,
        // then .sortedBy{} to keep the filtered list organized.

        "Available" -> filteredSlides.filter { it.isAvailable }
            .sortedBy { it.name }

        "Unavailable" -> filteredSlides.filter { !it.isAvailable }
            .sortedBy { it.name }
        // -------------------------

        else -> filteredSlides.sortedBy { it.name } // Default to A-Z sorting
    }

    Scaffold(
        topBar = {
            SlidesTopBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBackClicked = onBackClicked
                // onMenuClicked = { /* TODO */ } // <-- REMOVED
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedRoute = "catalog", onNavSelected = onNavSelected)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showActionMenu = true },
                containerColor = AppColors.PrimaryDarkBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(1.dp)
                    .size(60.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clipboard),
                    contentDescription = "View Requests",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        containerColor = Color(0xFFF4F4F4)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 50.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                CatalogBanner(title = "Catalog")
                Spacer(modifier = Modifier.height(24.dp))
                SlidesHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                Spacer(modifier = Modifier.height(16.dp))
                SlidesList(sortedSlides, onItemClick = { selectedItem = it })
            }

            if (selectedItem != null) {
                ItemDetailsDialog(item = selectedItem!!, onDismiss = { selectedItem = null })
            }

            if (showActionMenu) {
                ActionMenuDialog(
                    onDismiss = { showActionMenu = false },
                    onBorrowClick = { onNavSelected("borrow/slides"); showActionMenu = false },
                    onReserveClick = { onNavSelected("reserve/slides"); showActionMenu = false },
                    onLoanClick = { onNavSelected("loan/slides"); showActionMenu = false },
                    onReturnClick = { onNavSelected("return/slides"); showActionMenu = false }
                )
            }

        }
    }
}

// --- Top Bar (MODIFIED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidesTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBackClicked: () -> Unit
    // onMenuClicked: () -> Unit // <-- REMOVED
) {
    val searchBarColor = Color(0xFF4A6582)
    val placeholderColor = Color(0xFFC0C0C0)
    val textColor = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.PrimaryDarkBlue)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
        }

        TextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search", color = placeholderColor, fontSize = 15.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = searchBarColor,
                unfocusedContainerColor = searchBarColor,
                disabledContainerColor = searchBarColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textColor)
        )

        // --- MODIFICATION: Removed Spacer and Menu Button ---
        /*
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(onClick = onMenuClicked) {
            Icon(Icons.Default.Menu, "Menu", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        */
        // --- END OF MODIFICATION ---
    }
}

// --- Header with Sort Dropdown ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidesHeader(
    sortOption: String,
    onSortChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Name A-Z", "Name Z-A", "Available", "Unavailable")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Slides",
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextDark
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Sort by:",
                style = TextStyle(fontSize = 12.sp, fontFamily = poppins, color = Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.width(140.dp)
            ) {
                TextField(
                    value = sortOption,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .height(48.dp),
                    textStyle = TextStyle(fontSize = 10.sp, fontFamily = poppins, color = Color.Black),
                    singleLine = true,
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontFamily = poppins, fontSize = 14.sp) },
                            onClick = {
                                onSortChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- Slides List (MODIFIED) ---
@Composable
fun SlidesList(
    items: List<ItemDetails>,
    onItemClick: (ItemDetails) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            // --- MODIFICATION: Using new SlideItemCard ---
            SlideItemCard(item, onClick = { onItemClick(item) })
        }
    }
}

// --- NEW COMPOSABLE: SlideItemCard ---
// This is a copy of BorrowedItemCard but WITHOUT the image box
@Composable
fun SlideItemCard(
    item: ItemDetails,
    onClick: () -> Unit
) {
    val statusText = if (item.isAvailable) "Available" else "Unavailable"
    val statusColor = if (item.isAvailable) Color(0xFF28A745) else Color(0xFFDC3545)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp), // Increased padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- NO IMAGE BOX ---

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = AppColors.TextDark
                    ),
                    maxLines = 1
                )
                Row {
                    Text(
                        "Status: ",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    )
                    Text(
                        statusText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap to view details",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3F60A4)
                    )
                )
            }
        }
    }
}
// --- END OF NEW COMPOSABLE ---


// --- Preview ---
@Preview(showSystemUi = true)
@Composable
fun SlidesScreenPreview() {
    SlidesScreen()
}