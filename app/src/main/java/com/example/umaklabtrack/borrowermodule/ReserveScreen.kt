package com.example.umaklabtrack.borrowermodule

// import androidx.compose.material.icons.filled.Menu // <-- Import no longer needed

// Import for the auto-close fix
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umaklabtrack.R
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.entityManagement.ItemManage
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
private val itemManager = ItemManage()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveScreen(
    category: String,
    selectedItems: Map<String, Int>,
    onToggleSelect: (String) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onNavSelected: (String) -> Unit = {},
    onBackClicked: () -> Unit = {},
    onViewSelectedClicked: () -> Unit = {}
) {
    var selectedItemDialog: ItemDetails? by remember { mutableStateOf(null) }
    var sortOption by remember { mutableStateOf("Name A-Z") }
    var showSelectedItemsDialog by remember { mutableStateOf(false) }
    var showInfoSlipDialog by remember { mutableStateOf(false) } // <-- 1. ADDED THIS STATE

    Scaffold(
        topBar = {
            ReserveTopBar(
                onBackClicked = onBackClicked
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedRoute = "catalog", onNavSelected = onNavSelected)
        },
        containerColor = Color(0xFFF4F4F4)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 50.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ReserveBanner(title = "Reserve")
                Spacer(modifier = Modifier.height(24.dp))

                // --- MODIFICATION: Passing showImage parameter ---
                when (category) {
                    "apparatus" -> {
                        ApparatusHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicReserveList(
                            items = ItemRepository.apparatusItems,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true // <-- ADDED
                        )
                    }
                    "chemicals" -> {
                        ChemicalsHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicReserveList(
                            items = ItemRepository.chemicalItems,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true // <-- ADDED
                        )
                    }
                    "slides" -> {
                        SlidesHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicReserveList(
                            items = ItemRepository.slidesItems,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = false // <-- ADDED (set to false)
                        )
                    }
                    else -> {
                        CategoriesSection(
                            onApparatusClicked = { onNavSelected("reserve/apparatus") },
                            onChemicalsClicked = { onNavSelected("reserve/chemicals") },
                            onSlidesClicked = { onNavSelected("reserve/slides") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Frequently Reserved Items",
                            style = TextStyle(
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                color = AppColors.TextDark
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        DynamicReserveList(
                            items = ItemRepository.frequentlyBorrowedItems,
                            sortOption = "Name A-Z",
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true // <-- ADDED
                        )
                    }
                }
                // --- END OF MODIFICATION ---
            }

            if (selectedItems.isNotEmpty()) {
                Button(
                    onClick = { showSelectedItemsDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .height(50.dp)
                        .align(Alignment.BottomCenter),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDarkBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "View All Selected Items",
                        style = TextStyle(
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            if (selectedItemDialog != null) {
                // This dialog is defined in Borrower.txt, so it will hide the box for slides
                BorrowItemDetailsDialog(
                    item = selectedItemDialog!!,
                    isSelected = selectedItems.containsKey(selectedItemDialog!!.name),
                    onToggleSelect = { onToggleSelect(selectedItemDialog!!.name) },
                    onDismiss = { selectedItemDialog = null }
                )
            }

            if (showSelectedItemsDialog) {
                val itemsInCart = ItemRepository.allItems.filter { selectedItems.containsKey(it.name) }

                if (itemsInCart.isEmpty()) {
                    LaunchedEffect(Unit) {
                        showSelectedItemsDialog = false
                    }
                } else {
                    // This dialog is defined in Borrower.txt, so it will hide the box for slides
                    SelectedItemsDialog(
                        selectedItems = itemsInCart,
                        itemQuantities = selectedItems,
                        headerSubtitle = "Reservation",
                        onDismiss = { showSelectedItemsDialog = false },
                        onRemoveItem = onRemoveItem,
                        onIncreaseQuantity = onIncreaseQuantity,
                        onDecreaseQuantity = onDecreaseQuantity,
                        // --- 2. UPDATED THIS LAMBDA ---
                        onNext = {
                            showSelectedItemsDialog = false // Close cart
                            showInfoSlipDialog = true       // Open slip
                        }
                        // --- END OF UPDATE ---
                    )
                }
            }

            // --- 3. ADDED THIS BLOCK TO SHOW THE SLIP ---
            if (showInfoSlipDialog) {
                ReservationInformationSlipDialog(
                    onDismiss = {
                        showInfoSlipDialog = false
                    },
                    onGoBack = {
                        showInfoSlipDialog = false
                        showSelectedItemsDialog = true // Re-open cart
                    },
                    onConfirm = { subject, college, section ->
                        CoroutineScope(Dispatchers.IO).launch {
                            itemManager.insertBorrowerInfo(
                                UserSession.subject!!,
                                UserSession.college!!,
                                UserSession.yearSection!!,
                                selectedItems,
                                "Reserve",
                                UserSession.room!!
                            )
                            // Remove all items after insertion
                            withContext(Dispatchers.Main) {
                                selectedItems.keys.forEach { itemName ->
                                    onRemoveItem(itemName)
                                }
                            }
                        }
                        showInfoSlipDialog = false
                    }
                )
            }
            // --- END OF NEW BLOCK ---
        }
    }
}

// --- DynamicReserveList (MODIFIED) ---
@Composable
fun DynamicReserveList(
    items: List<ItemDetails>,
    sortOption: String,
    selectedItems: Set<String>,
    onToggleSelect: (String) -> Unit,
    onItemClick: (ItemDetails) -> Unit,
    showImage: Boolean // <-- ADDED PARAMETER
) {
    val sortedList = when (sortOption) {
        "Name A-Z" -> items.sortedBy { it.name }
        "Name Z-A" -> items.sortedByDescending { it.name }
        "Available" -> items.filter { it.isAvailable }
            .sortedBy { it.name }
        "Unavailable" -> items.filter { !it.isAvailable }
            .sortedBy { it.name }
        "Available First" -> items.sortedByDescending { it.isAvailable }
        else -> items.sortedBy { it.name } // Default to A-Z
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sortedList.forEach { item ->
            // This function is defined in Borrower.txt
            // It will check the item.type and hide the box for slides
            BorrowSelectItemCard(
                item = item,
                isSelected = selectedItems.contains(item.name),
                onToggleSelect = { onToggleSelect(item.name) },
                onCardClick = { onItemClick(item) },
                showImage = if (showImage) item.type != "Slide" else false
            )
        }
    }
}
// --- END OF MODIFICATION ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveTopBar(
    onBackClicked: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchBarColor = Color(0xFF4A6582)
    val placeholderColor = Color(0xFFC0C0C0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.PrimaryDarkBlue)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search", color = placeholderColor, fontSize = 15.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = searchBarColor,
                unfocusedContainerColor = searchBarColor,
                disabledContainerColor = searchBarColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            singleLine = true
        )
    }
}
@Composable
fun ReserveBanner(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.catalogheader),
                contentDescription = "Catalog Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    title,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    "Note: Requests must be submitted at least \n" +
                            "3 days in advance.",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ReserveScreenPreview() {
    val sampleSelectedItems = remember { mutableStateMapOf<String, Int>("Microscope" to 1) }

    ReserveScreen(
        category = "apparatus",
        selectedItems = sampleSelectedItems,
        onToggleSelect = { itemName ->
            if (sampleSelectedItems.containsKey(itemName)) {
                sampleSelectedItems.remove(itemName)
            } else {
                sampleSelectedItems[itemName] = 1
            }
        },
        onIncreaseQuantity = { itemName ->
            sampleSelectedItems[itemName] = (sampleSelectedItems[itemName] ?: 0) + 1
        },
        onDecreaseQuantity = { itemName ->
            val current = sampleSelectedItems[itemName] ?: 0
            if (current > 1) sampleSelectedItems[itemName] = current - 1
            else sampleSelectedItems.remove(itemName)
        },
        onRemoveItem = { sampleSelectedItems.remove(it) },
        onNavSelected = {},
        onBackClicked = {},
        onViewSelectedClicked = {}
    )
}