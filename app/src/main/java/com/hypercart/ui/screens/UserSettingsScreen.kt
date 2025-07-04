package com.hypercart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hypercart.DialogAlert
import com.hypercart.GradientScreen
import com.hypercart.navigation.NavRoutes
import com.hypercart.ui.components.GlassButton
import com.hypercart.ui.components.GlassIconButton
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.night
import com.hypercart.ui.viewmodel.UserSettingsViewModel

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.3f)
        )
    ) {
        content()
    }
}

@Composable
fun GlassTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.7f)
            )
        },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = blueSkye.copy(alpha = 0.7f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    navController: NavController,
    viewModel: UserSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isEditingName by viewModel.isEditingName.collectAsState()
    val tempName by viewModel.tempName.collectAsState()
    val isSignedOut by viewModel.isSignedOut.collectAsState()
    
    // Initialiser l'AuthManager au début - se fait immédiatement
    LaunchedEffect(Unit) {
        android.util.Log.i("UserSettingsScreen", "Initialisation de l'écran UserSettings")
        viewModel.initializeAuthManager(context)
    }
    
    // Forcer un rechargement périodique si les infos sont vides
    LaunchedEffect(userEmail, userName) {
        android.util.Log.i("UserSettingsScreen", "États actuels - Email: '$userEmail', Nom: '$userName'")
        
        // Si après 2 secondes on n'a toujours pas les infos, forcer un rechargement
        if ((userEmail.isEmpty() || userEmail == "Email non disponible" || userEmail == "Chargement...") && 
            (userName.isEmpty() || userName == "Chargement...")) {
            android.util.Log.w("UserSettingsScreen", "Informations vides détectées, rechargement forcé dans 2s")
            kotlinx.coroutines.delay(2000)
            viewModel.refreshUserInfo()
        }
    }
    
    // Rafraîchir les infos quand le nom change
    LaunchedEffect(userName) {
        if (userName.isNotEmpty() && !isEditingName) {
            viewModel.updateTempName(userName)
        }
    }
    
    // Rediriger vers l'écran de connexion si l'utilisateur s'est déconnecté
    LaunchedEffect(isSignedOut) {
        if (isSignedOut) {
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    // Gestion des erreurs
    error?.let { errorMessage ->
        DialogAlert(
            message = errorMessage,
            onConfirm = { viewModel.clearError() }
        )
    }
    
    // Gestion des messages de succès
    successMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000) // Afficher pendant 3 secondes
            viewModel.clearSuccessMessage()
        }
    }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.Default.ArrowBack,
                    onClick = { navController.popBackStack() },
                    tint = Color.White
                )
                
                Text(
                    text = "Paramètres utilisateur",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                // Bouton de rafraîchissement
                GlassIconButton(
                    icon = Icons.Default.Refresh,
                    onClick = { viewModel.refreshUserInfo() },
                    tint = blueSkye,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // Affichage des messages de succès
            successMessage?.let { message ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = message,
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Section informations utilisateur
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = blueSkye,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Informations personnelles",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Email (lecture seule)
                    Column(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (userEmail.isNotEmpty()) userEmail else "Email non disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (userEmail.isNotEmpty()) Color.White else Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Nom (modifiable)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nom",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            
                            if (!isEditingName) {
                                GlassIconButton(
                                    icon = Icons.Default.Edit,
                                    onClick = { viewModel.startEditingName() },
                                    tint = blueSkye,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        if (isEditingName) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlassTextInput(
                                    value = tempName,
                                    onValueChange = { viewModel.updateTempName(it) },
                                    placeholder = if (userName.isEmpty()) "Entrez votre nom" else userName,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { viewModel.saveUserName() }
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                GlassIconButton(
                                    icon = Icons.Default.Close,
                                    onClick = { viewModel.cancelEditingName() },
                                    tint = Color.Gray,
                                    modifier = Modifier.size(40.dp)
                                )
                                
                                GlassIconButton(
                                    icon = Icons.Default.Save,
                                    onClick = { viewModel.saveUserName() },
                                    tint = blueSkye,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else {
                            Text(
                                text = userName.ifEmpty { "Nom non défini" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (userName.isEmpty()) Color.Gray else Color.White,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Section déconnexion
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Déconnexion",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    GlassButton(
                        onClick = { if (!isLoading) showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Se déconnecter",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    
    // Dialog de confirmation de déconnexion
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Confirmer la déconnexion")
            },
            text = {
                Text("Êtes-vous sûr de vouloir vous déconnecter ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.signOut()
                    }
                ) {
                    Text("Déconnexion", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
} 