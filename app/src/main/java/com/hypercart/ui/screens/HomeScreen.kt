package com.hypercart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.hypercart.ConfirmDeleteDialog
import com.hypercart.DialogAlert
import com.hypercart.GradientScreen
import com.hypercart.data.Store
import com.hypercart.navigation.NavRoutes
import com.hypercart.ui.components.GlassButton
import com.hypercart.ui.components.GlassIcon
import com.hypercart.ui.components.GlassIconButton
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.night
import com.hypercart.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    storeViewModel: StoreViewModel = viewModel()
) {
    val stores by storeViewModel.stores.collectAsState()
    val isLoading by storeViewModel.isLoading.collectAsState()
    val error by storeViewModel.error.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<Store?>(null) }

    // Rafraîchir les données quand on revient sur cet écran
    LaunchedEffect(Unit) {
        storeViewModel.refreshStores()
    }

    // Gestion des erreurs
    error?.let { errorMessage ->
        DialogAlert(
            message = errorMessage,
            onConfirm = { storeViewModel.clearError() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(night)
    ) {
        GradientScreen()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 32.dp)
        ) {
            // Header avec nouveau bouton glass
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mes Magasins",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                // Remplacement du FloatingActionButton par GlassIconButton
                GlassIconButton(
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate(NavRoutes.AddStore.route) },
                    modifier = Modifier.size(56.dp),
                    tint = blueSkye
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = blueSkye)
                }
            } else if (stores.isEmpty()) {
                // État vide avec nouveau bouton glass
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GlassIcon(
                        imageVector = Icons.Default.Store,
                        modifier = Modifier.size(80.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Aucun magasin",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Text(
                        text = "Créez votre premier magasin pour commencer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Remplacement du Button standard par GlassButton
                    GlassButton(
                        onClick = { navController.navigate(NavRoutes.AddStore.route) }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Créer un magasin", color = Color.White)
                    }
                }
            } else {
                // Liste des magasins
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stores) { store ->
                        StoreCard(
                            store = store,
                            onStoreClick = { 
                                navController.navigate(NavRoutes.StoreProducts.createRoute(store.id.toString()))
                            },
                            onDeleteClick = { showDeleteDialog = store }
                        )
                    }
                }
            }
        }
    }

    // Dialog de confirmation de suppression avec style glass
    showDeleteDialog?.let { store ->
        ConfirmDeleteDialog(
            storeName = store.name,
            onConfirm = {
                storeViewModel.deleteStore(store.id)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreCard(
    store: Store,
    onStoreClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onStoreClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            blueSkye.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GlassIcon(Icons.Default.Store)

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                GlassIconButton(
                    icon = Icons.Default.Delete,
                    tint = Color.Red.copy(alpha = 0.9f),
                    onClick = onDeleteClick
                )
            }
        }
    }
} 