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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve and update an item from the [ProductsRepository]'s data source.
 */
class ProductEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val productsRepository: ProductsRepository
) : ViewModel() {

    /**
     * Holds current product ui state
     */
    var productUiState by mutableStateOf(ProductUiState())
        private set

    private val productId: Int = checkNotNull(savedStateHandle[ProductEditDestination.productIdArg])

    init {
        viewModelScope.launch {
            productUiState = productsRepository.getProductStream(productId)
                .filterNotNull()
                .first()
                .toProductUiState(true)
        }
    }

    /**
     * Update the product in the [ProductsRepository]'s data source
     */
    suspend fun updateProduct() {
        if (validateInput(productUiState.productDetails)) {
            productsRepository.updateProduct(productUiState.productDetails.toProduct())
        }
    }

    /**
     * Updates the [productUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(productDetails: ProductDetails) {
        productUiState =
            ProductUiState(productDetails = productDetails, isEntryValid = validateInput(productDetails))
    }

    private fun validateInput(uiState: ProductDetails = productUiState.productDetails): Boolean {
        return with(uiState) {
            producto.isNotBlank()&& material.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
        }
    }
}
