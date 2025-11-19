package com.example.umaklabtrack.borrowermodule

// --- FUNCTIONAL IMPORTS ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Menu
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

// --- Main Chemicals Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChemicalScreen(
    onNavSelected: (String) -> Unit = {},
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClicked: () -> Unit = {}
) {
    var selectedItem: ItemDetails? by remember { mutableStateOf(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Name A-Z") }
    var showActionMenu by remember { mutableStateOf(false) }

    // --- Chemicals List ---
    val chemicalItems= ItemRepository.chemicalItems
    val filteredList = chemicalItems.filter { it.name.contains(searchQuery, ignoreCase = true) }

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
            ChemicalsTopBar(
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
                ChemicalsHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                Spacer(modifier = Modifier.height(16.dp))
                ChemicalsList(sortedList, onItemClick = { selectedItem = it })
            }

            if (selectedItem != null) {
                ItemDetailsDialog(item = selectedItem!!, onDismiss = { selectedItem = null })
            }

            if (showActionMenu) {
                ActionMenuDialog(
                    onDismiss = { showActionMenu = false },
                    onBorrowClick = { onNavSelected("borrow/chemicals"); showActionMenu = false },
                    onReserveClick = { onNavSelected("reserve/chemicals"); showActionMenu = false },
                    onLoanClick = { onNavSelected("loan/chemicals"); showActionMenu = false },
                    onReturnClick = { onNavSelected("return/chemicals"); showActionMenu = false }
                )
            }

        }
    }
}

// --- Top Bar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChemicalsTopBar(
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
                unfocusedIndicatorColor = Color.Transparent,
                focusedPlaceholderColor = placeholderColor,
                unfocusedPlaceholderColor = placeholderColor,
                disabledPlaceholderColor = placeholderColor
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(onClick = onMenuClicked) {
            Icon(Icons.Default.Menu, "Menu", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

// --- Header with Sort Dropdown ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChemicalsHeader(
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
            "Chemicals",
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

// --- Chemicals List ---
@Composable
fun ChemicalsList(
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
fun ChemicalScreenPreview() {
    ChemicalScreen()
}
