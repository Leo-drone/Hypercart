package com.hypercart.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hypercart.R
import com.hypercart.data.AuthRepository
import com.hypercart.data.SessionManager
import com.hypercart.navigation.NavRoutes
import com.hypercart.ui.components.HypercartButton
import com.hypercart.ui.components.HypercartTextField
import com.hypercart.ui.components.Separator
import com.hypercart.ui.theme.black
import com.hypercart.ui.viewmodel.LoginViewModel
import com.hypercart.AuthManager
import com.hypercart.ErrorDialog
import com.hypercart.GradientScreen

@Composable
fun LoginScreen(
    navController: NavController?
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val repository = remember { AuthRepository(authManager) }
    val sessionManager = remember { SessionManager(context) }
    
    val viewModel: LoginViewModel = viewModel {
        LoginViewModel(repository, sessionManager)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Navigation effects
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController?.navigate(NavRoutes.Home.route) {
                popUpTo(NavRoutes.Login.route) { inclusive = true }
            }
            viewModel.resetSuccess()
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(black)
                .padding(padding)
        ) {
            GradientScreen()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ConnectionHeader()
                Spacer(modifier = Modifier.height(32.dp))

                // Email field
                HypercartTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = stringResource(R.string.email),
                    placeholder = stringResource(R.string.email_placeholder),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                HypercartTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = stringResource(R.string.password),
                    placeholder = stringResource(R.string.password_placeholder),
                    isPassword = true,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                )

                // Forgot password link
                TextButton(
                    onClick = { navController?.navigate(NavRoutes.ResetPassword.route) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Mot de passe oublié ?",
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Login button
                HypercartButton(
                    text = stringResource(R.string.log_in),
                    onClick = {
                        keyboardController?.hide()
                        viewModel.signInWithEmail()
                    },
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Separator
                Separator(text = stringResource(R.string.or))

                Spacer(modifier = Modifier.height(24.dp))

                // Google Sign In button
                OutlinedButton(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.signInWithGoogle()
                    },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_google),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.sign_in_with_google),
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Register link
                TextButton(onClick = { navController?.navigate(NavRoutes.Register.route) }) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Light,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                                )
                            ) {
                                append(stringResource(R.string.no_account))
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            ) {
                                append(stringResource(R.string.sign_up))
                            }
                        }
                    )
                }
            }
        }
    }

    // Error dialog
    ErrorDialog(
        message = uiState.errorMessage,
        onDismiss = viewModel::clearError
    )
}

@Composable
private fun ConnectionHeader() {
    Text(
        text = stringResource(R.string.login_subtitle),
        style = MaterialTheme.typography.titleLarge,
        color = androidx.compose.ui.graphics.Color.White,
        fontWeight = FontWeight.Bold
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginScreen(navController = null)
} 