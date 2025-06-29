package com.hypercart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hypercart.DialogAlert
import com.hypercart.GradientScreen
import com.hypercart.SuccessDialog
import com.hypercart.data.CartItem
import com.hypercart.ui.components.GlassButton
import com.hypercart.ui.components.GlassIcon
import com.hypercart.ui.components.GlassIconButton
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.darkGray
import com.hypercart.ui.theme.night
import com.hypercart.ui.viewmodel.ItemViewModel
import com.hypercart.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    storeId: String,
    itemViewModel: ItemViewModel = viewModel(),
    storeViewModel: StoreViewModel = viewModel()
) {
    val currentCart by itemViewModel.currentCart.collectAsState()
    val cartItemCount by itemViewModel.cartItemCount.collectAsState()
    val isLoading by itemViewModel.isLoading.collectAsState()
    val error by itemViewModel.error.collectAsState()
    val successMessage by itemViewModel.successMessage.collectAsState()
    val selectedStore by storeViewModel.selectedStore.collectAsState()
    
    // Initialiser les données au chargement
    LaunchedEffect(storeId) {
        val storeIdLong = storeId.toLongOrNull() ?: 0L
        storeViewModel.loadStoreById(storeIdLong)
        itemViewModel.initializeCartForStore(storeIdLong)
    }

    // Gestion des erreurs
    error?.let { errorMessage ->
        DialogAlert(
            message = errorMessage,
            onConfirm = { itemViewModel.clearError() }
        )
    }

    // Gestion des messages de succès
    successMessage?.let { message ->
        SuccessDialog(
            message = message,
            onDismiss = { itemViewModel.clearSuccessMessage() },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(night)
    ) {
        GradientScreen()
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = { 
                    Text(
                        text = "Panier - ${selectedStore?.name ?: "Magasin"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (currentCart?.items?.isNotEmpty() == true) {
                        IconButton(onClick = { itemViewModel.clearCart() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Vider le panier",
                                tint = Color.Red.copy(alpha = 0.9f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkGray
                )
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = blueSkye)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top = 16.dp)
                ) {
                    // Résumé du panier
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            blueSkye.copy(alpha = 0.2f),
                                            blueSkye.copy(alpha = 0.1f)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlassIcon(Icons.Default.ShoppingCart)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Articles dans le panier",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Text(
                                    text = cartItemCount.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = blueSkye,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Liste des items du panier
                    if (currentCart?.items?.isEmpty() != false) {
                        // Panier vide
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GlassIcon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Panier vide",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                
                                Text(
                                    text = "Ajoutez des produits à votre panier",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                GlassButton(
                                    onClick = { navController.popBackStack() }
                                ) {
                                    Text("Continuer les achats", color = Color.White)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentCart!!.items) { cartItem ->
                                CartItemCard(
                                    cartItem = cartItem,
                                    onUpdateQuantity = { newQuantity ->
                                        itemViewModel.updateCartItemQuantity(cartItem.id, newQuantity)
                                    },
                                    onRemove = {
                                        itemViewModel.removeFromCart(cartItem.id)
                                    }
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Actions du panier
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    GlassButton(
                                        onClick = { /* TODO: Passer commande */ },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "Passer la commande",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    GlassButton(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Continuer les achats", color = Color.White)
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

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Produit #${cartItem.productId}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (cartItem.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cartItem.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    GlassIconButton(
                        icon = Icons.Default.Delete,
                        onClick = onRemove,
                        tint = Color.Red.copy(alpha = 0.9f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Contrôles de quantité
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantité:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassIconButton(
                            icon = Icons.Default.Remove,
                            onClick = { 
                                if (cartItem.quantity > 1) {
                                    onUpdateQuantity(cartItem.quantity - 1)
                                } else {
                                    onRemove()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Text(
                            text = cartItem.quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        GlassIconButton(
                            icon = Icons.Default.Add,
                            onClick = { onUpdateQuantity(cartItem.quantity + 1) },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
} 