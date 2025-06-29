package com.hypercart.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hypercart.DialogAlert
import com.hypercart.GradientScreen
import com.hypercart.SuccessDialog
import com.hypercart.data.CartItem
import com.hypercart.data.Product
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
    val needsCategoryInput by itemViewModel.needsCategoryInput.collectAsState()
    val categories by itemViewModel.categories.collectAsState()
    val products by itemViewModel.products.collectAsState()
    val productSuggestions by itemViewModel.productSuggestions.collectAsState()
    
    var showAddItemModal by remember { mutableStateOf(false) }
    var previousCartItemCount by remember { mutableStateOf(0) }
    var checkedItems by remember { mutableStateOf(setOf<Long>()) }
    var anyFieldFocused by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Fonction pour supprimer les items cochés
    fun deleteCheckedItems() {
        if (checkedItems.isNotEmpty()) {
            checkedItems.forEach { itemId ->
                itemViewModel.removeFromCart(itemId)
            }
            checkedItems = emptySet()
        }
    }
    
    // Gérer le cycle de vie pour supprimer les items cochés quand l'app va en arrière-plan
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // L'app va en arrière-plan, supprimer les items cochés
                    deleteCheckedItems()
                }
                Lifecycle.Event.ON_STOP -> {
                    // L'app se ferme, supprimer les items cochés
                    deleteCheckedItems()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        // Nettoyer l'observateur quand le composable est détruit
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Gérer le bouton Back pour supprimer les items cochés (seulement si aucun champ n'est en focus)
    BackHandler(enabled = !anyFieldFocused) {
        // Supprimer tous les items cochés avant de naviguer
        deleteCheckedItems()
        // Puis naviguer vers la page précédente
        navController.popBackStack()
    }
    
    // Initialiser les données au chargement
    LaunchedEffect(storeId) {
        val storeIdLong = storeId.toLongOrNull() ?: 0L
        storeViewModel.loadStoreById(storeIdLong)
        itemViewModel.initializeCartForStore(storeIdLong)
        itemViewModel.loadAllProducts(storeIdLong) // Pour les recherches
        itemViewModel.loadCategories() // Pour le dropdown
    }
    
    // Fermer la modal quand un produit est ajouté avec succès
    LaunchedEffect(currentCart?.items?.size) {
        val currentItemCount = currentCart?.items?.size ?: 0
        if (showAddItemModal && currentItemCount > previousCartItemCount) {
            // Si le nombre d'items uniques a augmenté, fermer la modal
            kotlinx.coroutines.delay(500)
            showAddItemModal = false
            itemViewModel.clearNeedsCategoryInput()
        }
        previousCartItemCount = currentItemCount
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Liste de courses - ${selectedStore?.name ?: "Magasin"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        // Supprimer tous les items cochés avant de naviguer
                        deleteCheckedItems()
                        navController.popBackStack() 
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Bouton Paramètres
                    IconButton(onClick = { /* TODO: Ouvrir paramètres */ }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkGray
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemModal = true },
                containerColor = blueSkye,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter produit"
                )
            }
        },
        containerColor = night
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GradientScreen()
            
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
                ) {

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
                                    text = "Liste vide",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                
                                Text(
                                    text = "Ajoutez des produits à votre liste de courses",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                GlassButton(
                                    onClick = { showAddItemModal = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ajouter un produit", color = Color.White)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentCart!!.items) { cartItem ->
                                CartItemCard(
                                    cartItem = cartItem,
                                    products = products,
                                    isChecked = checkedItems.contains(cartItem.id),
                                    onCheckedChange = { isChecked ->
                                        checkedItems = if (isChecked) {
                                            checkedItems + cartItem.id
                                        } else {
                                            checkedItems - cartItem.id
                                        }
                                    },
                                    onUpdateQuantity = { newQuantity ->
                                        itemViewModel.updateCartItemQuantity(cartItem.id, newQuantity)
                                    },
                                    onFocusChange = { isFocused ->
                                        anyFieldFocused = isFocused
                                    }
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Modal d'ajout d'item
    if (showAddItemModal) {
        AddItemModal(
            isVisible = showAddItemModal,
            onDismiss = { 
                showAddItemModal = false
                itemViewModel.clearNeedsCategoryInput()
                itemViewModel.clearProductSuggestions()
            },
            onAddItem = { productName, quantity, categoryName ->
                val storeIdLong = storeId.toLongOrNull() ?: 0L
                if (categoryName != null) {
                    // Créer le produit avec la catégorie
                    itemViewModel.createProductWithCategory(productName, quantity, categoryName, storeIdLong)
                } else if (productName.startsWith("EXISTING_PRODUCT_")) {
                    // Produit existant sélectionné depuis les suggestions
                    val productId = productName.removePrefix("EXISTING_PRODUCT_").toLongOrNull()
                    if (productId != null) {
                        itemViewModel.addToCart(productId, quantity)
                    }
                } else {
                    // Chercher le produit existant
                    itemViewModel.checkAndAddProduct(productName, quantity, storeIdLong)
                }
            },
            onSearchProducts = { query ->
                val storeIdLong = storeId.toLongOrNull() ?: 0L
                itemViewModel.searchProductSuggestions(query, storeIdLong)
            },
            onSelectProduct = { product ->
                // Remplir les champs avec le produit sélectionné




                itemViewModel.clearProductSuggestions()
            },
            productSuggestions = productSuggestions,
            needsCategoryInput = needsCategoryInput
        )
    }
}

@Composable
fun QuantityTextField(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onFocusChange: (Boolean) -> Unit = {}
) {
    var quantityText by remember { mutableStateOf(quantity.toString()) }
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    
    // Gérer le bouton Back quand le champ est en focus
    BackHandler(enabled = isFocused) {
        focusManager.clearFocus()
    }
    
    // Synchroniser avec la quantité externe
    LaunchedEffect(quantity) {
        quantityText = quantity.toString()
    }
    
    fun handleDoneEditing(clearFocusAfter: Boolean = true) {
        if (quantityText.isEmpty() || quantityText.toIntOrNull() == null || quantityText.toInt() <= 0) {
            // Si le champ est vide ou invalide, remettre la valeur originale
            quantityText = quantity.toString()
        } else {
            val newQuantity = quantityText.toInt()
            if (newQuantity != quantity && newQuantity > 0) {
                onQuantityChange(newQuantity)
            }
        }
        if (clearFocusAfter) {
            focusManager.clearFocus()
        }
    }
    
    OutlinedTextField(
        value = quantityText,
        onValueChange = { newValue ->
            if (newValue.isEmpty()) {
                quantityText = newValue
            } else if (newValue.toIntOrNull() != null && newValue.toInt() > 0) {
                quantityText = newValue
            }
        },
        placeholder = { 
            Text("1", color = Color.White.copy(alpha = 0.5f)) 
        },
        modifier = Modifier
            .width(70.dp)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChange(focusState.isFocused)
                if (!focusState.isFocused) {
                    // Le champ a perdu le focus, sauvegarder sans clear focus
                    handleDoneEditing(clearFocusAfter = false)
                }
            },
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { handleDoneEditing() }
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedIndicatorColor = blueSkye,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    products: List<Product>,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    val product = products.find { it.id == cartItem.productId }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = onCheckedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = blueSkye,
                            uncheckedColor = Color.White.copy(alpha = 0.7f),
                            checkmarkColor = Color.White
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = product?.name ?: "Produit #${cartItem.productId}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isChecked) Color.White.copy(alpha = 0.6f) else Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                QuantityTextField(
                    quantity = cartItem.quantity,
                    onQuantityChange = { newQuantity ->
                        if (newQuantity > 0) {
                            onUpdateQuantity(newQuantity)
                        }
                    },
                    onFocusChange = onFocusChange
                )
            }
        }
    }
} 