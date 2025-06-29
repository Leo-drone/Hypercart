package com.hypercart.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.darkGray
import com.hypercart.ui.theme.night

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAddItem: (String, Int, String?) -> Unit, // nom, quantité, catégorie (si nécessaire)
    onSearchProducts: (String) -> Unit = {},
    onSelectProduct: (com.hypercart.data.Product) -> Unit = {},
    productSuggestions: List<com.hypercart.data.Product> = emptyList(),
    needsCategoryInput: Pair<String, Int>? = null // produit et quantité qui ont besoin d'une catégorie
) {
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1") }
    var categoryName by remember { mutableStateOf("") }
    var showCategoryInput by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<com.hypercart.data.Product?>(null) }
    
    // Réagir aux changements de needsCategoryInput
    LaunchedEffect(needsCategoryInput) {
        if (needsCategoryInput != null) {
            // Un produit a besoin d'une catégorie
            itemName = needsCategoryInput.first
            itemQuantity = needsCategoryInput.second.toString()
            showCategoryInput = true
            categoryName = ""
            isLoading = false // Importante : débloquer les champs
        } else {
            showCategoryInput = false
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(300)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(300)
                    ),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(top = 60.dp),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            night.copy(alpha = 0.95f),
                                            darkGray.copy(alpha = 0.95f)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, Float.POSITIVE_INFINITY)
                                    ),
                                    shape = RoundedCornerShape(
                                        bottomStart = 20.dp,
                                        bottomEnd = 20.dp
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (showCategoryInput) "Nouveau produit" else "Ajouter un produit",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    IconButton(onClick = {
                                        itemName = ""
                                        itemQuantity = "1"
                                        categoryName = ""
                                        showCategoryInput = false
                                        selectedProduct = null
                                        onDismiss()
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Fermer",
                                            tint = Color.White
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Champs principaux - toujours visibles
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Nom du produit
                                    Column(modifier = Modifier.weight(2f)) {
                                        Text(
                                            text = if (selectedProduct != null) "Produit sélectionné ✓" else "Nom du produit",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (selectedProduct != null) blueSkye else Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        OutlinedTextField(
                                            value = itemName,
                                            onValueChange = { newValue ->
                                                itemName = newValue
                                                // Réinitialiser le produit sélectionné si on tape manuellement
                                                selectedProduct = null
                                                // Déclencher la recherche d'autocomplétion
                                                if (!showCategoryInput && newValue.isNotBlank()) {
                                                    onSearchProducts(newValue)
                                                }
                                            },
                                            placeholder = { 
                                                Text(
                                                    "Ex: Pommes, Pain, Lait...",
                                                    color = Color.White.copy(alpha = 0.6f)
                                                ) 
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                                focusedIndicatorColor = blueSkye,
                                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)
                                            ),
                                            enabled = !isLoading
                                        )
                                    }
                                    
                                    // Quantité
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Quantité",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        OutlinedTextField(
                                            value = itemQuantity,
                                            onValueChange = { newValue ->
                                                if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                                                    itemQuantity = newValue
                                                }
                                            },
                                            placeholder = { 
                                                Text(
                                                    "1",
                                                    color = Color.White.copy(alpha = 0.6f)
                                                ) 
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                                focusedIndicatorColor = blueSkye,
                                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)
                                            ),
                                            enabled = !isLoading
                                        )
                                    }
                                }
                                
                                // Suggestions d'autocomplétion
                                if (productSuggestions.isNotEmpty() && !showCategoryInput) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text(
                                        text = "Produits suggérés :",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    LazyColumn(
                                        modifier = Modifier.height(200.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(productSuggestions) { product ->
                                                                                         Card(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .clickable {
                                                         // Remplir les champs avec le produit sélectionné
                                                         itemName = product.name
                                                         itemQuantity = "1"
                                                         selectedProduct = product
                                                         onSelectProduct(product)
                                                     },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color.White.copy(alpha = 0.1f)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = product.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.White,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Ajouter",
                                                        tint = blueSkye,
                                                        modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Champ catégorie - visible seulement si le produit n'existe pas
                                AnimatedVisibility(visible = showCategoryInput) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "Ce produit n'existe pas encore.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "Veuillez spécifier une catégorie :",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        OutlinedTextField(
                                            value = categoryName,
                                            onValueChange = { categoryName = it },
                                            placeholder = { 
                                                Text(
                                                    "Ex: Électronique, Alimentation...",
                                                    color = Color.White.copy(alpha = 0.6f)
                                                ) 
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                                focusedIndicatorColor = blueSkye,
                                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)
                                            ),
                                            enabled = !isLoading
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                // Boutons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    GlassButton(
                                        onClick = {
                                            itemName = ""
                                            itemQuantity = "1"
                                            categoryName = ""
                                            showCategoryInput = false
                                            selectedProduct = null
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Annuler", color = Color.White)
                                    }
                                    
                                    GlassButton(
                                        onClick = { 
                                            // Vérifier les conditions avant d'exécuter
                                            val isEnabled = !isLoading && itemName.isNotBlank() && 
                                                    itemQuantity.isNotBlank() && 
                                                    (!showCategoryInput || categoryName.isNotBlank())
                                            
                                            if (isEnabled) {
                                                val quantity = itemQuantity.toIntOrNull() ?: 1
                                                
                                                if (showCategoryInput) {
                                                    // Confirmer avec catégorie
                                                    if (categoryName.isNotBlank()) {
                                                        isLoading = true
                                                        onAddItem(itemName.trim(), quantity, categoryName.trim())
                                                        // Reset des champs
                                                        itemName = ""
                                                        itemQuantity = "1"
                                                        categoryName = ""
                                                        showCategoryInput = false
                                                        isLoading = false
                                                        onDismiss()
                                                    }
                                                } else {
                                                    // Vérifier si un produit a été sélectionné depuis les suggestions
                                                    if (selectedProduct != null) {
                                                        // Produit existant sélectionné, l'ajouter directement
                                                        isLoading = true
                                                        // Utiliser un callback spécial pour les produits existants
                                                        onAddItem("EXISTING_PRODUCT_${selectedProduct!!.id}", quantity, null)
                                                        // Reset des champs
                                                        itemName = ""
                                                        itemQuantity = "1"
                                                        selectedProduct = null
                                                        isLoading = false
                                                        onDismiss()
                                                    } else {
                                                        // Premier ajout - vérifier si le produit existe
                                                        isLoading = true
                                                        onAddItem(itemName.trim(), quantity, null)
                                                        // Note: Le reset se fera selon le résultat dans la logique parent
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when {
                                                showCategoryInput -> "Confirmer"
                                                selectedProduct != null -> "Ajouter"
                                                else -> "Ajouter"
                                            },
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fonction utilitaire pour que AddItemModal puisse demander d'afficher le champ catégorie
@Composable
fun rememberAddItemModalState() = remember { mutableStateOf(false) } 