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
package com.example.client.data

// Sample Data (In a real app, this would come from a ViewModel or repository)
val sampleMenuItems = listOf(
    MenuItem(
        "1",
        "Margherita Pizza",
        "Classic cheese and tomato pizza",
        12.99,
        drawableResId = null // Replace with actual R.drawable.margherita_pizza if you have it
    ),
    MenuItem(
        "2",
        "Pepperoni Pizza",
        "Pizza with spicy pepperoni slices",
        14.99,
        drawableResId = null // Replace with actual R.drawable.pepperoni_pizza
    ),
    MenuItem(
        "3",
        "Caesar Salad",
        "Fresh romaine lettuce with Caesar dressing",
        8.50,
        drawableResId = null // Replace with actual R.drawable.caesar_salad
    ),
    MenuItem(
        "4",
        "Cheeseburger",
        "Beef patty with cheese, lettuce, and tomato",
        10.75,
        drawableResId = null // Replace with actual R.drawable.cheeseburger
    ),
    MenuItem(
        "5",
        "Fries",
        "Crispy golden french fries",
        4.00,
        drawableResId = null // Replace with actual R.drawable.fries
    ),
    MenuItem(
        "6",
        "Cola",
        "Refreshing cola drink",
        2.50,
        drawableResId = null // Replace with actual R.drawable.cola
    )
)