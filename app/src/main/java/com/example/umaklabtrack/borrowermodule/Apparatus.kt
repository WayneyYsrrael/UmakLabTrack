package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext



// --- FUNCTIONAL IMPORTS ---
import com.example.umaklabtrack.borrowermodule.BottomNavigationBar
import com.example.umaklabtrack.borrowermodule.CatalogBanner
import com.example.umaklabtrack.borrowermodule.ItemDetails
import com.example.umaklabtrack.borrowermodule.BorrowedItemCard
import com.example.umaklabtrack.borrowermodule.ItemDetailsDialog
import com.example.umaklabtrack.borrowermodule.ActionMenuDialog
import kotlinx.coroutines.coroutineScope

// --- Main Apparatus Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApparatusScreen(
    onNavSelected: (String) -> Unit = {},
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClicked: () -> Unit = {}
) {

    var selectedItem: ItemDetails? by remember { mutableStateOf(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Name A-Z") }
    var showActionMenu by remember { mutableStateOf(false) }

    // --- Chemicals List ---
    val apparatusItems= ItemRepository.apparatusItems
    val filteredList = apparatusItems.filter { it.name.contains(searchQuery, ignoreCase = true) }

    // --- Sorting Logic ---
    val sortedList = when (sortOption) {
        "Name A-Z" -> filteredList.sortedBy { it.name }
        "Name Z-A" -> filteredList.sortedByDescending { it.name }

        // --- THIS IS THE FIX ---
        // Use .filter{} to show ONLY matching items,
        // then .sortedBy{} to keep the filtered list organized.

        "Available" -> filteredList.filter { it.isAvailable }
            .sortedBy { it.name }

        "Unavailable" -> filteredList.filter { !it.isAvailable }
            .sortedBy { it.name }
        // -------------------------

        else -> filteredList.sortedBy { it.name } // Default to A-Z sorting
    }
    Scaffold(
        topBar = {
            ApparatusTopBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBackClicked = onBackClicked,
                onMenuClicked = { /* TODO */ }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 50.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { CatalogBanner(title = "Catalog") }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { ApparatusHeader(sortOption = sortOption, onSortChange = { sortOption = it }) }
                items(sortedList) { item ->
                    BorrowedItemCard(item, onClick = { selectedItem = item })
                }

            }


            if (selectedItem != null) {
                ItemDetailsDialog(item = selectedItem!!, onDismiss = { selectedItem = null })
            }

            if (showActionMenu) {
                ActionMenuDialog(
                    onDismiss = { showActionMenu = false },
                    onBorrowClick = { onNavSelected("borrow/apparatus"); showActionMenu = false },
                    onReserveClick = { onNavSelected("reserve/apparatus"); showActionMenu = false },
                    onLoanClick = { onNavSelected("loan/apparatus"); showActionMenu = false },
                    onReturnClick = { onNavSelected("return/apparatus"); showActionMenu = false }
                )
            }

        }
    }
}

// --- Top Bar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApparatusTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBackClicked: () -> Unit,
    onMenuClicked: () -> Unit
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


    }
}

// --- Header with Sort Dropdown ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApparatusHeader(
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
            "Apparatus",
            style = TextStyle(fontSize = 20.sp, fontFamily = poppins, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
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
                        unfocusedTextColor = Color.Black,
                        cursorColor = AppColors.PrimaryDarkBlue
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

// --- Apparatus List ---
@Composable
fun ApparatusList(
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
            BorrowedItemCard(item, onClick = { onItemClick(item) })
        }
    }
}

// --- Preview ---
@Preview(showSystemUi = true)
@Composable
fun ApparatusScreenPreview() {
    ApparatusScreen()
}
