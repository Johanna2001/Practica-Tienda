/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve, update and delete an item from the [ProductsRepository]'s data source.
 */
class ProductDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val productsRepository: ProductsRepository,
) : ViewModel() {

    private val productId: Int = checkNotNull(savedStateHandle[ProductDetailsDestination.productIdArg])

    /**
     * Holds the item details ui state. The data is retrieved from [ProductsRepository] and mapped to
     * the UI state.
     */
    val uiState: StateFlow<ProductDetailsUiState> =
        productsRepository.getProductStream(productId)
            .filterNotNull()
            .map {
                ProductDetailsUiState(outOfStock = it.quantity <= 0, productDetails = it.toProductDetails())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ProductDetailsUiState()
            )

    /**
     * Deletes the item from the [ProductsRepository]'s data source.
     */
    suspend fun deleteProduct() {
        productsRepository.deleteProduct(uiState.value.productDetails.toProduct())
    }

    /**
     * Reduces the item quantity by one and update the [ProductsRepository]'s data source.
     */
    fun reduceQuantityByOne() {
        viewModelScope.launch {
            val currentProduct = uiState.value.productDetails.toProduct()
            if (currentProduct.quantity > 0) {
                productsRepository.updateProduct(currentProduct.copy(quantity = currentProduct.quantity - 1))
            }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * UI state for ProductDetailsScreen
 */
data class ProductDetailsUiState(
    val outOfStock: Boolean = true,
    val productDetails: ProductDetails = ProductDetails()
)
