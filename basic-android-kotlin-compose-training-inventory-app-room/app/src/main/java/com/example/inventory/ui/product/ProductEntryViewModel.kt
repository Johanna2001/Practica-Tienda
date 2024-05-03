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
import androidx.lifecycle.ViewModel
import com.example.inventory.data.Product
import com.example.inventory.data.ProductsRepository
import java.text.NumberFormat

/**
 * ViewModel to validate and insert items in the Room database.
 */
class ProductEntryViewModel(private val productsRepository: ProductsRepository) : ViewModel() {

    /**
     * Holds current product ui state
     */
    var productUiState by mutableStateOf(ProductUiState())
        private set

    /**
     * Updates the [productUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(productDetails: ProductDetails) {
        productUiState =
            ProductUiState(productDetails = productDetails, isEntryValid = validateInput(productDetails))
    }

    /**
     * Inserts an [Product] in the Room database
     */
    suspend fun saveProduct() {
        if (validateInput()) {
            productsRepository.insertProduct(productUiState.productDetails.toProduct())
        }
    }

    private fun validateInput(uiState: ProductDetails = productUiState.productDetails): Boolean {
        return with(uiState) {
            producto.isNotBlank() && material.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
        }
    }
}

/**
 * Represents Ui State for an Product.
 */
data class ProductUiState(
    val productDetails: ProductDetails = ProductDetails(),
    val isEntryValid: Boolean = false
)

data class ProductDetails(
    val id: Int = 0,
    val producto: String = "",
    val material: String = "",
    val price: String = "",
    val quantity: String = "",
)

/**
 * Extension function to convert [ProductUiState] to [Product]. If the value of [ProductDetails.price] is
 * not a valid [Double], then the price will be set to 0.0. Similarly if the value of
 * [ProductUiState] is not a valid [Int], then the quantity will be set to 0
 */
fun ProductDetails.toProduct(): Product = Product(
    id = id,
    producto = producto,
    material = material,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toIntOrNull() ?: 0
)

fun Product.formatedPrice(): String {
    return NumberFormat.getCurrencyInstance().format(price)
}

/**
 * Extension function to convert [Product] to [ProductUiState]
 */
fun Product.toProductUiState(isEntryValid: Boolean = false): ProductUiState = ProductUiState(
    productDetails = this.toProductDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [Product] to [ProductDetails]
 */
fun Product.toProductDetails(): ProductDetails = ProductDetails(
    id = id,
    producto = producto,
    material = material,
    price = price.toString(),
    quantity = quantity.toString()
)
