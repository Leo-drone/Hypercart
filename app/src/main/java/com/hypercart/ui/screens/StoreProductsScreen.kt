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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.hypercart.data.Category
import com.hypercart.data.Product
import com.hypercart.navigation.NavRoutes
import com.hypercart.ui.components.AddItemModal
import com.hypercart.ui.components.GlassButton
import com.hypercart.ui.components.GlassIcon
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.darkGray
import com.hypercart.ui.theme.night
import com.hypercart.ui.viewmodel.ItemViewModel
import com.hypercart.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreProductsScreen(
    navController: NavController,
    storeId: String,
    itemViewModel: ItemViewModel = viewModel(),
    storeViewModel: StoreViewModel = viewModel()
) {
    val products by itemViewModel.products.collectAsState()
    val categories by itemViewModel.categories.collectAsState()
    val selectedCategory by itemViewModel.selectedCategory.collectAsState()
    val cartItemCount by itemViewModel.cartItemCount.collectAsState()
    val isLoading by itemViewModel.isLoading.collectAsState()
    val error by itemViewModel.error.collectAsState()
    val successMessage by itemViewModel.successMessage.collectAsState()
    val selectedStore by storeViewModel.selectedStore.collectAsState()
    
    // État pour la modal d'ajout d'item
    var showAddItemModal by remember { mutableStateOf(false) }
    
    // Initialiser les données au chargement
    LaunchedEffect(storeId) {
        val storeIdLong = storeId.toLongOrNull() ?: 0L
        storeViewModel.loadStoreById(storeIdLong)
        itemViewModel.initializeCartForStore(storeIdLong)
        itemViewModel.loadProductsAndCategories(storeIdLong)
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
            // Top Bar avec boutons paramètres et ajout
            TopAppBar(
                title = { 
                    Text(
                        text = selectedStore?.name ?: "Produits",
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
                    // Bouton paramètres
                    IconButton(onClick = { 
                        navController.navigate(NavRoutes.StoreDetail.createRoute(storeId))
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = Color.White
                        )
                    }
                    
                    // Bouton d'ajout d'item
                    IconButton(onClick = { showAddItemModal = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Ajouter un article",
                            tint = blueSkye
                        )
                    }
                    
                    // Icône du panier avec badge
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(
                                    containerColor = blueSkye,
                                    contentColor = Color.White
                                ) {
                                    Text(cartItemCount.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { 
                            navController.navigate(NavRoutes.Cart.createRoute(storeId))
                        }) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Panier",
                                tint = Color.White
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
                    // Filtres par catégorie
                    if (categories.isNotEmpty()) {
                        Text(
                            text = "Catégories",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { 
                                        val storeIdLong = storeId.toLongOrNull() ?: 0L
                                        itemViewModel.loadAllProducts(storeIdLong) 
                                    },
                                    label = { Text("Tous") },
                                    selected = selectedCategory == null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = blueSkye,
                                        selectedLabelColor = Color.White,
                                        containerColor = darkGray,
                                        labelColor = Color.White
                                    )
                                )
                            }
                            items(categories) { category ->
                                CategoryChip(
                                    category = category,
                                    isSelected = selectedCategory == category.id,
                                    onClick = { 
                                        val storeIdLong = storeId.toLongOrNull() ?: 0L
                                        itemViewModel.loadProductsByCategory(category.id, storeIdLong) 
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Liste des produits
                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GlassIcon(
                                    imageVector = Icons.Default.Category,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Aucun produit",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                
                                Text(
                                    text = "Aucun produit trouvé dans cette catégorie",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(products) { product ->
                                ProductCard(
                                    product = product,
                                    onAddToCart = { 
                                        itemViewModel.addToCart(product.id, 1)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Modal d'ajout d'item
    AddItemModal(
        isVisible = showAddItemModal,
        onDismiss = { showAddItemModal = false },
        onAddItem = { itemName, itemDescription ->
            // Créer le produit avec la catégorie sélectionnée ou catégorie par défaut
            val categoryId = selectedCategory ?: 1L
            val storeIdLong = storeId.toLongOrNull() ?: 0L
            itemViewModel.createProduct(itemName, itemDescription, categoryId, storeIdLong)
        }
    )
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { Text(category.name) },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = blueSkye,
            selectedLabelColor = Color.White,
            containerColor = darkGray,
            labelColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Catégorie: ${product.categoryId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                GlassButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Ajouter au panier",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ajouter",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
} 