/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.client.ui.screens

import PaymentDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.client.PaymentProvider
import com.example.client.data.MenuItem
import com.example.client.data.OrderItem
import com.example.client.ui.components.MenuItemRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantMenuScreen(
    restaurantName: String,
    menuItems: List<MenuItem>,
    paymentProvider: PaymentProvider,
    lifecycleScope: LifecycleCoroutineScope
) {
    var orderItems by remember { mutableStateOf(mapOf<String, OrderItem>()) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val totalAmount = orderItems.values.sumOf { it.menuItem.price * it.quantity }

    val handlePaymentSuccess = {
        showPaymentDialog = false
        // In a real app, you would fulfill the order here.
        // We'll just show a message instead.
        Toast.makeText(
            context,
            "Your ${orderItems.size} item(s) are on the way!",
            Toast.LENGTH_LONG
        ).show()
        // Reset order after "payment"
        orderItems = emptyMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(restaurantName) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (totalAmount > 0) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: $${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    paymentProvider.initialize(
                                        totalAmount = totalAmount,
                                        onPaymentSuccess = handlePaymentSuccess,
                                    )
                                    showPaymentDialog = true
                                }
                            }
                        ) {
                            Text("Pay Now")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { // For a little spacing at the top
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(menuItems) { menuItem ->
                MenuItemRow(
                    menuItem = menuItem,
                    orderItem = orderItems[menuItem.id],
                    onQuantityChange = { newItem ->
                        orderItems = orderItems.toMutableMap().apply {
                            if (newItem.quantity > 0) {
                                put(newItem.menuItem.id, newItem)
                            } else {
                                remove(newItem.menuItem.id)
                            }
                        }
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(if (totalAmount > 0) 70.dp else 8.dp))
            }
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            onDismiss = { showPaymentDialog = false },
            paymentProvider = paymentProvider
        )
    }
}