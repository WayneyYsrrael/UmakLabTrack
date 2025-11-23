package com.example.umaklabtrack.borrowermodule

// Imports para sa Toast Animation

// import androidx.compose.material.icons.filled.Menu // <-- REMOVED
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.umaklabtrack.dataClasses.AvailabilityStatus
import com.example.umaklabtrack.dataClasses.Items
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.entityManagement.ItemManage
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// --- Import for the slip (This now matches your file) ---
private val itemManager = ItemManage()

object ItemRepository {


    var apparatusItems: List<ItemDetails> = emptyList()
        private set
    var chemicalItems: List<ItemDetails> = emptyList()
        private set
    var slidesItems: List<ItemDetails> = emptyList()


        private set

    var apparatusItemsL: List<ItemDetails> = emptyList()
        private set
    var chemicalItemsL: List<ItemDetails> = emptyList()
        private set
    var slidesItemsL: List<ItemDetails> = emptyList()


        private set
    val frequentlyBorrowedItems = listOf(
        ItemDetails(1,"Beaker, 250ml", true, "Glass beaker for experiments.", "Apparatus",false),
        ItemDetails(2,"Microscope, Compound", false, "High power microscope.", "Apparatus",false),
        ItemDetails(3,"Hydrochloric Acid, 1L", true, "1M solution in water.", "Chemical",false),
        ItemDetails(4,"Onion Cell, l.s.", true, "Longitudinal section of an onion cell.", "Slide",false)
    )
    val allItems: List<ItemDetails>
        get() = (apparatusItems + chemicalItems + slidesItems + frequentlyBorrowedItems).distinctBy { it.name }

    val allItemsL: List<ItemDetails>
        get() = (apparatusItemsL + chemicalItemsL + slidesItemsL + frequentlyBorrowedItems).distinctBy { it.name }

    suspend fun loadAllItemsFromDb() {
        val itemsFromDb = kotlinx.coroutines.suspendCancellableCoroutine<List<Items>> { cont ->
            itemManager.fetchAllItems { items ->
                cont.resume(items, onCancellation = null)
            }
        }

        // Map Items → ItemDetails
        val allItemDetails = itemsFromDb.map { item ->
            ItemDetails(
                id=item.item_id,
                name = item.name,
                isAvailable = if (item.status == AvailabilityStatus.Available) true else false,

                        description = item.description,
                type = item.category,
                imageResId = null,
                isForLoan=item.isForLoan

            )

        }

        val categorized = allItemDetails.groupBy { it.type }

        apparatusItems = categorized["Apparatus"] ?.filter { !it.isForLoan }?: emptyList()
        chemicalItems = categorized["Chemical"] ?.filter { !it.isForLoan }?: emptyList()
        slidesItems = categorized["Slide"] ?.filter { !it.isForLoan }?: emptyList()
        slidesItemsL = categorized["Slide"]?.filter { it.isForLoan } ?: emptyList()
        chemicalItemsL = categorized["Chemical"]?.filter { it.isForLoan } ?: emptyList()
        apparatusItemsL = categorized["Apparatus"]?.filter { it.isForLoan } ?: emptyList()

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowScreen(
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
    var showInfoSlipDialog by remember { mutableStateOf(false) } // State to control the slip
    var availabilityFilter by remember { mutableStateOf("All") } // All | Available | Unavailable
    val context = LocalContext.current

    Scaffold(
        topBar = {
            BorrowTopBar(onBackClicked = onBackClicked)
        },
        bottomBar = {
            BottomNavigationBar(selectedRoute = "catalog", onNavSelected = onNavSelected)
        },
        containerColor = Color(0xFFF4F4F4)
    ) { innerPadding ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 50.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                BorrowerBanner(title = "Borrow")
                Spacer(modifier = Modifier.height(24.dp))

                // --- MODIFICATION: Passing showImage parameter ---
                when (category) {
                    "apparatus" -> {
                        ApparatusHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicBorrowList(
                            items = ItemRepository.apparatusItems,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true // <-- Show image
                        )
                    }
                    "chemicals" -> {
                        ChemicalsHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicBorrowList(
                            items = ItemRepository.chemicalItems,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true // <-- Show image
                        )
                    }
                    "slides" -> {
                        SlidesHeader(sortOption = sortOption, onSortChange = { sortOption = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        DynamicBorrowList(
                            items = ItemRepository.slidesItems,
                            sortOption = sortOption,
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = false // <-- HIDE image for slides
                        )
                    }
                    else -> { // "all"
                        CategoriesSection(
                            onApparatusClicked = { onNavSelected("borrow/apparatus") },
                            onChemicalsClicked = { onNavSelected("borrow/chemicals") },
                            onSlidesClicked = { onNavSelected("borrow/slides") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))


                        Text(
                            text = "Frequently Borrowed Items",
                            style = TextStyle(
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                color = AppColors.TextDark
                            ),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        )
                        DynamicBorrowList(
                            items = ItemRepository.frequentlyBorrowedItems,
                            sortOption = "Name A-Z",
                            selectedItems = selectedItems.keys,
                            onToggleSelect = onToggleSelect,
                            onItemClick = { selectedItemDialog = it },
                            showImage = true // <-- THIS WAS THE MISSING LINE
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
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
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
                    onToggleSelect = {
                        onToggleSelect(selectedItemDialog!!.name)
                    },
                    onDismiss = {
                        selectedItemDialog = null
                    }
                )
            }

            if (showSelectedItemsDialog) {
                val itemsInCart = ItemRepository.allItems
                    .filter { selectedItems.containsKey(it.name) }

                if (itemsInCart.isEmpty()) {
                    LaunchedEffect(Unit) {
                        showSelectedItemsDialog = false
                    }
                } else {
                    SelectedItemsDialog(
                        selectedItems = itemsInCart,
                        itemQuantities = selectedItems,
                        onDismiss = { showSelectedItemsDialog = false },
                        onRemoveItem = onRemoveItem,
                        onIncreaseQuantity = onIncreaseQuantity,
                        onDecreaseQuantity = onDecreaseQuantity,
                        onNext = {
                            showSelectedItemsDialog = false // Close current dialog
                            showInfoSlipDialog = true       // Open the new one
                        }
                    )
                }
            }

            if (showInfoSlipDialog) {
                BorrowerInformationSlipDialog(
                    onDismiss = {
                        showInfoSlipDialog = false
                        showSelectedItemsDialog = false
                    },
                    onGoBack = {
                        showInfoSlipDialog = false
                        showSelectedItemsDialog = true // <-- Re-opens the cart
                    },
                    onConfirm = { subject, college, section ->
                        CoroutineScope(Dispatchers.IO).launch {
                            itemManager.insertBorrowerInfo(
                                UserSession.subject!!,
                                UserSession.college!!,
                                UserSession.yearSection!!,
                                selectedItems,
                                "Borrow",
                                "Preparing"
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
        }
    }
}

// --- DynamicBorrowList (MODIFIED) ---
@Composable
fun DynamicBorrowList(
    items: List<ItemDetails>,
    sortOption: String,
    selectedItems: Set<String>,
    onToggleSelect: (String) -> Unit,
    onItemClick: (ItemDetails) -> Unit,
    showImage: Boolean
) {
    val processedList = when (sortOption) {
        "Name A-Z" -> items.sortedBy { it.name }
        "Name Z-A" -> items.sortedByDescending { it.name }
        "Available" -> items.filter { it.isAvailable }
            .sortedBy { it.name }
        "Unavailable" -> items.filter { !it.isAvailable }
            .sortedBy { it.name }
        "Available First" -> items.sortedWith(
            compareByDescending<ItemDetails> { it.isAvailable }.thenBy { it.name }
        )
        "Unavailable First" -> items.sortedWith(
            compareBy<ItemDetails> { it.isAvailable }.thenBy { it.name }
        )
        else -> items.sortedBy { it.name }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        processedList.forEach { item ->
            BorrowSelectItemCard(
                item = item,
                isSelected = selectedItems.contains(item.name),
                onToggleSelect = { onToggleSelect(item.name) },
                onCardClick = { onItemClick(item) },
                // MODIFIED: Pass parameter, but also check item type
                // This hides image for slides even in "Frequently Used"
                showImage = if (showImage) item.type != "Slide" else false
            )
        }
    }
}
// --- END OF MODIFICATION ---

// --- Top Bar for Borrow Screen (MODIFIED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowTopBar(onBackClicked: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(28.dp))
        }

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search", color = placeholderColor, fontSize = 15.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = placeholderColor) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = searchBarColor,
                unfocusedContainerColor = searchBarColor,
                disabledContainerColor = searchBarColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedLeadingIconColor = placeholderColor,
                unfocusedLeadingIconColor = placeholderColor,
                focusedPlaceholderColor = placeholderColor,
                unfocusedPlaceholderColor = placeholderColor,
                disabledPlaceholderColor = placeholderColor
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            textStyle = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = textColor),
            singleLine = true
        )

        // --- MODIFICATION: Removed Spacer and Menu Icon ---
        /*
        Spacer(modifier = Modifier.width(10.dp))

        IconButton(onClick = { /* TODO: Handle menu click */ }) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        */
        // --- END OF MODIFICATION ---
    }
}
// --- END OF MODIFICATION ---


// --- BorrowSelectItemCard (MODIFIED) ---
@Composable
fun BorrowSelectItemCard(
    item: ItemDetails,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onCardClick: () -> Unit,
    showImage: Boolean // <-- ADDED PARAMETER
) {
    val statusText = if (item.isAvailable) "Available" else "Unavailable"
    val statusColor = if (item.isAvailable) Color(0xFF28A745) else Color(0xFFDC3545)
    val tapColor = Color(0xFF3F60A4)

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
                .clickable { onCardClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- MODIFICATION: Image is now conditional ---
            if (showImage) {
                Card(
                    modifier = Modifier
                        .size(70.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)) // Gray fallback
                ) {
                    if (item.imageResId != null) {
                        Image(
                            painter = painterResource(id = item.imageResId),
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // If null, the gray box shows
                }
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                // Add a small spacer so text doesn't touch the edge
                Spacer(modifier = Modifier.width(4.dp))
            }
            // --- END OF MODIFICATION ---

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
                    "Tap to view details",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = tapColor
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (item.isAvailable) {
                Button(
                    onClick = onToggleSelect,
                    modifier = Modifier.size(width = 80.dp, height = 40.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = if (isSelected) {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF28A745), // Green
                            contentColor = Color.White
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A6582), // Dark blue-gray
                            contentColor = Color.White
                        )
                    }
                ) {
                    Text(
                        if (isSelected) "Selected" else "Select",
                        style = TextStyle(fontFamily = poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(width = 80.dp, height = 40.dp))
            }
        }
    }
}
// --- END OF MODIFICATION ---

// --- BorrowItemDetailsDialog (MODIFIED) ---
@Composable
fun BorrowItemDetailsDialog(
    item: ItemDetails,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 70.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .width(300.dp)
                    .clickable(
                        onClick = {},
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // --- MODIFICATION: Conditional Image Box ---
                    // It only shows the image box if the item type is NOT "Slide"
                    if (item.type != "Slide") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)) // Gray fallback
                        ) {
                            if (item.imageResId != null) {
                                Image(
                                    painter = painterResource(id = item.imageResId),
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // If null, the gray card shows
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // If it is a slide, just add a small spacer at the top
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // --- END OF MODIFICATION ---

                    Text(
                        text = item.name,
                        style = TextStyle(
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AppColors.TextDark
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val statusText = if (item.isAvailable) "Available" else "Unavailable"
                    val statusColor = if (item.isAvailable) Color(0xFF28A745) else Color(0xFFDC3545)
                    Row {
                        Text(
                            "Status: ",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        )
                        Text(
                            statusText,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                color = statusColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Description:",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        item.description,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (item.isAvailable) {
                        Button(
                            onClick = onToggleSelect,
                            modifier = Modifier.size(width = 120.dp, height = 40.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = if (isSelected) {
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF28A745), // Green
                                    contentColor = Color.White
                                )
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4A6582), // Dark blue-gray
                                    contentColor = Color.White
                                )
                            }
                        ) {
                            Text(
                                if (isSelected)
                                    "Selected" else "Select",
                                style = TextStyle(fontFamily = poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                }
            }
        }
    }
}
// --- END OF MODIFICATION ---
// --- CATALOG BANNER (Unchanged) ---
@Composable
fun BorrowerBanner(title: String) {
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
                    "Borrow your items right away!",
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
// --- This matches your screenshot, so it should be correct. ---
@Composable
fun SelectedItemsDialog(
    selectedItems: List<ItemDetails>, // <--- FIXED: Changed Item to ItemDetails
    itemQuantities: Map<String, Int>,
    headerTitle: String = "Selected Items",
    headerSubtitle: String = "Borrowing",
    onDismiss: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onNext: () -> Unit
) {
    // --- Toast logic ---
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun showInfoToast(message: String) {
        toastMessage = message
        showToast = true
        coroutineScope.launch {
            delay(3000)
            showToast = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            // Main Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center)
                    .clickable(
                        onClick = {},
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // --- Header with RED 'X' button ---
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                headerTitle,
                                style = TextStyle(
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF182C55)
                                )
                            )
                            Text(
                                headerSubtitle,
                                style = TextStyle(
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            )
                        }

                        // --- RED 'X' button ---
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Note: ")
                            }
                            append("Each item has a maximum quantity of ‘n’.")
                        },
                        style = TextStyle(
                            fontFamily = poppins,
                            fontSize = 12.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- Scrollable list of selected items ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 'item' here is now correctly inferred as 'ItemDetails'
                        items(selectedItems, key = { it.name }) { item ->
                            val currentQuantity = itemQuantities[item.name] ?: 1

                            SelectedItemCard(
                                item = item,
                                quantity = currentQuantity,
                                onRemove = {
                                    onRemoveItem(item.name)
                                    showInfoToast("'${item.name}' has been removed.")
                                },
                                onIncrease = {
                                    if ((itemQuantities[item.name] ?: 1) >= 20) {
                                        Toast.makeText(context, "Maximum quantity is 20", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onIncreaseQuantity(item.name)
                                    }
                                },
                                onDecrease = {
                                    if (currentQuantity > 1) {
                                        onDecreaseQuantity(item.name)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = showToast,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(300)),
                        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(animationSpec = tween(300))
                    ) {
                        InfoToastBox(message = toastMessage)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color(0xFF182C55))
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedItems.isNotEmpty()) {
                                onNext()
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDarkBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Next",
                            style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }
}

// --- USE THIS FUNCTION ---
// --- It has the larger 28.dp remove button ---
// --- SelectedItemCard (MODIFIED) ---
// ... (Your other code, like SelectedItemsDialog, remains the same) ...

// --- NEW COMPOSABLE: RemoveCircleButton ---
@Composable
fun RemoveCircleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(20.dp) // Maintain the same clickable area
    ) {
        Box(
            modifier = Modifier
                .size(24.dp) // Size of the actual circle (slightly smaller than IconButton)
                .background(Color.Red, CircleShape), // Red circle
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove, // White minus icon
                contentDescription = "Remove item",
                tint = Color.White,
                modifier = Modifier.size(16.dp) // Size of the minus icon inside the circle
            )
        }
    }
}
// --- END NEW COMPOSABLE ---


// --- ITEM CARD (MODIFIED) ---
// --- Now uses the new RemoveCircleButton composable ---
@Composable
fun SelectedItemCard(
    item: ItemDetails,
    quantity: Int,
    onRemove: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp, start = 12.dp, end = 8.dp),
            contentAlignment = Alignment.Center
        ) {

            // --- MODIFIED: Using the new RemoveCircleButton ---
            RemoveCircleButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Row(
                modifier = Modifier.heightIn(min = 60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // --- Conditional Image Box ---
                if (item.type != "Slide") {
                    Card(
                        modifier = Modifier
                            .size(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)) // Gray fallback
                    ) {
                        if (item.imageResId != null) {
                            Image(
                                painter = painterResource(id = item.imageResId),
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                // --- END OF MODIFICATION ---

                // Name and Type
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                        maxLines = 1
                    )
                    Text(
                        "Type: ${item.type}",
                        style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // --- Quantity Controls ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuantityControlButton(icon = Icons.Default.Remove, onClick = onDecrease)
                    Text(
                        "$quantity",
                        style = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    QuantityControlButton(icon = Icons.Default.Add, onClick = onIncrease)
                }

                // Empty spacer to push quantity controls left
                Spacer(modifier = Modifier.width(24.dp))
            }
        }
    }
}

// --- END OF MODIFICATION ---

// --- QuantityControlButton (MODIFIED) ---
@Composable
private fun QuantityControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(Color(0xFF6E6E6E), RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White, // <-- MODIFIED: Changed tint to White for visibility
            modifier = Modifier.size(16.dp)
        )
    }
}
// --- END OF MODIFICATION ---


// --- PREVIEW ---
@Preview(showSystemUi = true)
@Composable
fun BorrowScreenPreview() {
    BorrowScreen(
        category = "all",
        selectedItems = mapOf(),
        onToggleSelect = {},
        onIncreaseQuantity = {},
        onDecreaseQuantity = {},
        onRemoveItem = {}
    )
}

@Composable
fun InfoToastBox(message: String) {
    val backgroundColor = Color(0xFF2196F3) // Info Blue
    val title = "Info"

    // Assuming you have this drawable
    val iconPainter = painterResource(id = R.drawable.alertcircle)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(backgroundColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = iconPainter,
                contentDescription = "Toast Icon",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(backgroundColor)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(title, color = backgroundColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = poppins)
            Text(message, color = Color.Black, fontSize = 13.sp, fontFamily = poppins)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectedItemsDialogPreview() {
    val previewItems = ItemRepository.apparatusItems.take(3)
    val quantities = mapOf(
        previewItems[0].name to 1,
        previewItems[1].name to 2,
        previewItems[2].name to 5
    )

    SelectedItemsDialog(
        selectedItems = previewItems,
        itemQuantities = quantities,
        onDismiss = {},
        onRemoveItem = {},
        onIncreaseQuantity = {},
        onDecreaseQuantity = {},
        onNext = {}
    )
}