package com.example.umaklabtrack.borrowermodule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.umaklabtrack.borrowermodule.ItemRepository
import com.example.umaklabtrack.borrowermodule.ItemDetails

class HomeViewModel : ViewModel() {
    var isLoading by mutableStateOf(true)
        private set

    var apparatusItems by mutableStateOf<List<ItemDetails>>(emptyList())
        private set
    var chemicalItems by mutableStateOf<List<ItemDetails>>(emptyList())
        private set
    var slidesItems by mutableStateOf<List<ItemDetails>>(emptyList())
        private set

    init {
        loadItems()
    }
    fun refreshItems(){
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            isLoading = true

            ItemRepository.loadAllItemsFromDb()

            isLoading = false
        }
    }
}
