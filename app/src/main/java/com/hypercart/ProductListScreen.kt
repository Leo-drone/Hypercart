package com.hypercart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProductListScreen() {
    val products = listOf("iPhone 15", "Samsung Galaxy S24", "Pixel 8 Pro", "Xiaomi 13", "OnePlus 12")

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(16.dp)) {

        Text(
            text = "Liste des produits",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        products.forEach { product ->
            Text(
                text = "• $product",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
