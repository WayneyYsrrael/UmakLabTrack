package com.example.umaklabtrack.borrowermodule

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
fun LoanScreen(
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
    var showInfoSlipDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = { LoanTopBar(onBackClicked = onBackClicked) },
        bottomBar = { BottomNavigationBar(selectedRoute = "catalog", onNavSelected = onNavSelected) },
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
                LoanBanner(title = "Loan")
                Spacer(modifier = Modifier.height(24.dp))

                when (category) {
                    "apparatus" -> {
                        ApparatusHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicLoanList(
                            items = ItemRepository.apparatusItemsL,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true
                        )
                    }
                    "chemicals" -> {
                        ChemicalsHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicLoanList(
                            items = ItemRepository.chemicalItemsL,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true
                        )
                    }
                    "slides" -> {
                        SlidesHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicLoanList(
                            items = ItemRepository.slidesItemsL,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = false
                        )
                    }
                    else -> {
                        CategoriesSection(
                            onApparatusClicked = { onNavSelected("loan/apparatus") },
                            onChemicalsClicked = { onNavSelected("loan/chemicals") },
                            onSlidesClicked = { onNavSelected("loan/slides") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Frequently Loaned Items",
                            style = TextStyle(
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                color = AppColors.TextDark
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        DynamicLoanList(
                            items = ItemRepository.frequentlyBorrowedItems,
                            sortOption = "Name A-Z",
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true
                        )
                    }
                }
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
                        style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    )
                }
            }

            if (selectedItemDialog != null) {
                BorrowItemDetailsDialog(
                    item = selectedItemDialog!!,
                    isSelected = selectedItems.containsKey(selectedItemDialog!!.name),
                    onToggleSelect = { onToggleSelect(selectedItemDialog!!.name) },
                    onDismiss = { selectedItemDialog = null }
                )
            }

            if (showSelectedItemsDialog) {
                val itemsInCart = ItemRepository.allItemsL.filter { selectedItems.containsKey(it.name) }

                if (itemsInCart.isEmpty()) {
                    LaunchedEffect(Unit) {
                        showSelectedItemsDialog = false
                    }
                } else {
                    SelectedItemsDialog(
                        selectedItems = itemsInCart,
                        itemQuantities = selectedItems,
                        headerSubtitle = "Loan",
                        onDismiss = { showSelectedItemsDialog = false },
                        onRemoveItem = onRemoveItem,
                        onIncreaseQuantity = { itemName ->
                            val currentQty = selectedItems[itemName] ?: 0
                            if (currentQty < 20) {
                                onIncreaseQuantity(itemName)
                            } else {
                                Toast.makeText(context, "Maximum quantity is 20", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDecreaseQuantity = onDecreaseQuantity,
                        onNext = {
                            showSelectedItemsDialog = false
                            showInfoSlipDialog = true
                        }
                    )
                }
            }

            // --- DIALOG INTEGRATION ---
            if (showInfoSlipDialog) {
                LoanInformationSlipDialog(
                    onDismiss = {
                        showInfoSlipDialog = false
                    },
                    onGoBack = {
                        showInfoSlipDialog = false
                        showSelectedItemsDialog = true
                    },
                    // --- CHANGED: Only receiving subject and college now ---
                    onConfirm = { subject, college ->
                        CoroutineScope(Dispatchers.IO).launch {
                            itemManager.insertBorrowerInfo(
                                subject,
                                college,
                                "", // Section Removed (Empty String)
                                selectedItems,
                                "Loan",
                                "Pending"
                            )

                            withContext(Dispatchers.Main) {
                                selectedItems.keys.forEach { itemName ->
                                    onRemoveItem(itemName)
                                }
                            }
                        }
                        println("Confirmed: $subject, $college, Room: ${UserSession.room}")
                        showInfoSlipDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun DynamicLoanList(
    items: List<ItemDetails>,
    sortOption: String,
    selectedItems: Set<String>,
    onToggleSelect: (String) -> Unit,
    onItemClick: (ItemDetails) -> Unit,
    showImage: Boolean
) {
    val sortedList = when (sortOption) {
        "Name A-Z" -> items.sortedBy { it.name }
        "Name Z-A" -> items.sortedByDescending { it.name }
        "Available" -> items.filter { it.isAvailable }.sortedBy { it.name }
        "Unavailable" -> items.filter { !it.isAvailable }.sortedBy { it.name }
        "Available First" -> items.sortedByDescending { it.isAvailable }
        else -> items.sortedBy { it.name }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sortedList.forEach { item ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanTopBar(onBackClicked: () -> Unit) {
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
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
                .height(50.dp)
                .padding(horizontal = 8.dp),
            singleLine = true
        )
    }
}

@Composable
fun LoanBanner(title: String) {
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
fun LoanScreenPreview() {
    val sampleSelectedItems = remember { mutableStateMapOf<String, Int>("Beaker" to 1) }

    LoanScreen(
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
            if (current > 1) sampleSelectedItems[itemName] = current - 1 else sampleSelectedItems.remove(itemName)
        },
        onRemoveItem = { itemName -> sampleSelectedItems.remove(itemName) },
        onNavSelected = {},
        onBackClicked = {},
        onViewSelectedClicked = {}
    )
}