package com.example.umaklabtrack.borrowermodule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu // <-- This import is no longer needed
import androidx.compose.material.icons.filled.Search
// Added explicit M3 imports to prevent ambiguity errors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
// ------------------
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
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

// --- CRITICAL IMPORTS ---
// (Assuming these are in your project)
import com.example.umaklabtrack.ui.theme.AppColors
import com.example.umaklabtrack.ui.theme.poppins
// ------------------------

/**
 * Holds the details for an item to be displayed in the dialog.
 */
data class ItemDetails(
    val id:Int,
    val name: String,
    val isAvailable: Boolean,
    val description: String,
    val type: String,
    val imageResId: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(

    onNavSelected: (String) -> Unit = {},
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onApparatusClicked: () -> Unit = {},
    onChemicalsClicked: () -> Unit = {},
    onSlidesClicked: () -> Unit = {}
) {
    var selectedItem: ItemDetails? by remember { mutableStateOf(null) }
    var showActionMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CatalogTopBar() },
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
                    .width(60.dp)
                    .height(60.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clipboard),
                    contentDescription = "View Requests",
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
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
                CategoriesSection(
                    onApparatusClicked = onApparatusClicked,
                    onChemicalsClicked = onChemicalsClicked,
                    onSlidesClicked = onSlidesClicked
                )
                Spacer(modifier = Modifier.height(24.dp))
                FrequentlyBorrowedSection(
                    onItemClick = { item ->
                        selectedItem = item
                    }
                )
            }

            if (selectedItem != null) {
                ItemDetailsDialog(
                    item = selectedItem!!,
                    onDismiss = {
                        selectedItem = null
                    }
                )
            }

            if (showActionMenu) {
                ActionMenuDialog(
                    onDismiss = { showActionMenu = false },
                    onBorrowClick = { onNavSelected("borrow/all"); showActionMenu = false },
                    onReserveClick = { onNavSelected("reserve/all"); showActionMenu = false },
                    onLoanClick = { onNavSelected("loan/all"); showActionMenu = false },
                    onReturnClick = { /* TODO */ ; showActionMenu = false }
                )
            }
        }
    }
}


// --- CATALOG TOP BAR (MODIFIED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTopBar() {
    var searchQuery by remember { mutableStateOf("") }
    val searchBarColor = Color.White.copy(alpha = 0.2f)
    val placeholderColor = Color.White.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.PrimaryDarkBlue)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search", color = placeholderColor, fontSize = 15.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = placeholderColor) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = searchBarColor,
                unfocusedContainerColor = searchBarColor,
                disabledContainerColor = searchBarColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f) // This will now make the search bar fill the whole row
                .height(50.dp),
            textStyle = TextStyle(fontSize = 14.sp, fontFamily = poppins, color = Color.White),
            singleLine = true
        )
    }
}

// --- CATALOG BANNER (Unchanged) ---
@Composable
fun CatalogBanner(title: String) {
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
                    "Find what you need for your next lab work.",
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

// --- CATEGORIES SECTION (Unchanged) ---
@Composable
fun CategoriesSection(
    onApparatusClicked: () -> Unit = {},
    onChemicalsClicked: () -> Unit = {},
    onSlidesClicked: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            "Categories",
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextDark
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryCard(
                title = "Apparatus",
                imageRes = R.drawable.apparatus,
                modifier = Modifier.weight(1f),
                onClick = onApparatusClicked
            )
            Spacer(modifier = Modifier.width(12.dp))
            CategoryCard(
                title = "Chemicals",
                imageRes = R.drawable.chemicals,
                modifier = Modifier.weight(1f),
                onClick = onChemicalsClicked
            )
            Spacer(modifier = Modifier.width(12.dp))
            CategoryCard(
                title = "Slides",
                imageRes = R.drawable.slides,
                modifier = Modifier.weight(1f),
                onClick = onSlidesClicked
            )
        }
    }
}

// --- CategoryCard Function (Unchanged) ---
@Composable
fun CategoryCard(
    title: String,
    imageRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.aspectRatio(0.9f),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)

            )
            Text(
                title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}


// --- FrequentlyBorrowedSection (MODIFIED) ---
// --- FrequentlyBorrowedSection (MODIFIED) ---
@Composable
fun FrequentlyBorrowedSection(
    onItemClick: (ItemDetails) -> Unit
) {
    // --- MODIFICATION: Removed imageResId to default to gray boxes ---
    val frequentlyBorrowedItems = listOf(
        ItemDetails(1,"Beaker, 250ml", true, "Glass beaker for experiments.", "Apparatus"),
        ItemDetails(2,"Microscope, Compound", false, "High power microscope.", "Apparatus"),
        ItemDetails(3,"Hydrochloric Acid, 1L", true, "1M solution in water.", "Chemical"),
        ItemDetails(4,"Onion Cell, l.s.", true, "Longitudinal section of an onion cell.", "Slide") // No imageId or box for slide
    )
    // --- END OF MODIFICATION ---


    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            "Frequently Used Items",
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextDark
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            frequentlyBorrowedItems.forEach { item ->
                BorrowedItemCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

// --- BorrowedItemCard (MODIFIED) ---
@Composable
fun BorrowedItemCard(
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- MODIFICATION: Conditional Image Box ---
            // Only show this block if the item is NOT a slide
            if (item.type != "Slide") {
                Card(
                    modifier = Modifier.size(70.dp),
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
                    // If imageResId is null, the gray Card bg shows
                }
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                // If it IS a slide, just add a little padding
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


// --- ItemDetailsDialog (MODIFIED) ---
@Composable
fun ItemDetailsDialog(
    item: ItemDetails,
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
                )
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 70.dp, end = 15.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .width(280.dp)
                    .align(Alignment.Center)
                    .clickable(
                        onClick = {},
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    // --- MODIFICATION: Conditional Image Box ---
                    // Only show the image box if the item type is NOT "Slide"
                    if (item.type != "Slide") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
                        ) {
                            if (item.imageResId != null) {
                                Image(
                                    painter = painterResource(id = item.imageResId),
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // If imageResId is null, the gray Card bg shows
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // If it is a slide, just add a small spacer
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
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
                        )
                    )
                    Text(
                        item.description,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    )
                }
            }
        }
    }
}


// --- BottomNavigationBar (Unchanged) ---
@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onNavSelected: (String) -> Unit
) {

    val navItems = listOf(
        Triple("Home", R.drawable.home, "home"),
        Triple("Catalog", R.drawable.catalog, "catalog"),
        Triple("Notifications", R.drawable.notif, "notifications"),
        Triple("Logs", R.drawable.logs, "logs"),
        Triple("Profile", R.drawable.profile, "profile")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        navItems.forEach { (label, iconResId, route) ->
            val isSelected = route == selectedRoute

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSelected) AppColors.GoldAccent else Color.White)
                    .clickable { onNavSelected(route) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = label,
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    label,
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontFamily = poppins
                )
            }
        }
    }
}


// --- ACTION MENU DIALOG (Unchanged) ---
@Composable
fun ActionMenuDialog(
    onDismiss: () -> Unit,
    onBorrowClick: () -> Unit,
    onReserveClick: () -> Unit,
    onLoanClick: () -> Unit,
    onReturnClick: () -> Unit
) {
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
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
            ) {
                FabMenuButton(text = "BORROW", onClick = { onBorrowClick(); onDismiss() })
                FabMenuButton(text = "RESERVE", onClick = { onReserveClick(); onDismiss() })
                FabMenuButton(text = "LOAN", onClick = { onLoanClick(); onDismiss() })
                FabMenuButton(text = "RETURN", onClick = { onReturnClick(); onDismiss() })
            }
        }
    }
}

// --- FabMenuButton (Unchanged) ---
@Composable
private fun FabMenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(300.dp)
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF101D36),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
    }
}


// --- PREVIEWS (Unchanged) ---

@Preview(showSystemUi = true)
@Composable
fun CatalogScreenPreview() {
    CatalogScreen()
}

@Preview(showBackground = true)
@Composable
fun ItemDetailsDialog_Available_Preview() {
    ItemDetailsDialog(
        item = ItemDetails(
            id=1,
            name = "Beaker, 250ml",
            isAvailable = true,
            description = "Glass beaker for experiments.",
            type = "Apparatus",
            imageResId = R.drawable.apparatus // Added for preview
        ),
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ItemDetailsDialog_Unavailable_Preview() {
    ItemDetailsDialog(
        item = ItemDetails(
            id=1,
            name = "Microscope, Compound",
            isAvailable = false,
            description = "High power microscope.",
            type = "Apparatus",
            imageResId = R.drawable.apparatus // Added for preview
        ),
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ItemDetailsDialog_Slide_Preview() { // <-- ADDED SLIDE PREVIEW
    ItemDetailsDialog(
        item = ItemDetails(
            id=1,
            name = "Onion Cell, l.s.",
            isAvailable = true,
            description = "Longitudinal section of an onion cell.",
            type = "Slide" // Type is "Slide"
        ),
        onDismiss = {}
    )
}