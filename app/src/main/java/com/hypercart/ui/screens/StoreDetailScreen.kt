package com.hypercart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hypercart.DialogAlert
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.darkGray
import com.hypercart.ui.theme.night
import com.hypercart.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    navController: NavController,
    storeId: String,
    storeViewModel: StoreViewModel = viewModel()
) {
    val selectedStore by storeViewModel.selectedStore.collectAsState()
    val isLoading by storeViewModel.isLoading.collectAsState()
    val error by storeViewModel.error.collectAsState()

    // Charger le magasin au démarrage
    LaunchedEffect(storeId) {
        storeViewModel.loadStoreById(storeId.toLongOrNull() ?: 0L)
    }

    // Gestion des erreurs
    error?.let { errorMessage ->
        DialogAlert(
            message = errorMessage,
            onConfirm = { 
                storeViewModel.clearError()
                navController.popBackStack()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(night)
    ) {
        // Top Bar
        TopAppBar(
            title = { 
                Text(
                    text = selectedStore?.name ?: "Détails du magasin",
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
        } else if (selectedStore == null) {
            // Magasin non trouvé
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = darkGray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Magasin non trouvé",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = blueSkye)
                ) {
                    Text("Retour")
                }
            }
        } else {
            // Affichage des détails du magasin
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Carte principale du magasin
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = darkGray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = blueSkye
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = selectedStore!!.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Informations du magasin",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }

                // Informations supplémentaires
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = darkGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Informations",
                            style = MaterialTheme.typography.titleMedium,
                            color = blueSkye,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        InfoRow("ID du magasin", selectedStore!!.id.toString())
                        InfoRow("Nom", selectedStore!!.name)
                    }
                }

                // Section de test (pour tester les fonctionnalités futures)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = darkGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Zone de test",
                            style = MaterialTheme.typography.titleMedium,
                            color = blueSkye,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Cette section sera utilisée pour tester les futures fonctionnalités du magasin (produits, commandes, etc.)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { /* TODO: Ajouter produits */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = blueSkye
                                )
                            ) {
                                Text("Produits", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                            }
                            
                            OutlinedButton(
                                onClick = { /* TODO: Gérer commandes */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = blueSkye
                                )
                            ) {
                                Text("Commandes", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
} 