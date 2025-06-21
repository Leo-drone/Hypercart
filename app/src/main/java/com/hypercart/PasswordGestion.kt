package com.hypercart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hypercart.ui.theme.black
import com.hypercart.ui.theme.darkGray
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit = {},
    navController: NavController?,
    onRegisterClick: () -> Unit = {}
) {

    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val invalid_email = stringResource(R.string.invalid_email)
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(black)
    ) {
        GradientScreen()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.reset_password),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Email
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.email),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.email_placeholder),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bouton principal
            Button(
                onClick = {
                    coroutineScope.launch {
                        keyboardController?.hide()
                        if (email.isBlank()) {
                            errorMessage = "Email requis"
                            return@launch
                        }
                        if (!isValidEmail(email)) {
                            errorMessage = invalid_email
                            return@launch
                        }
                        try {
                            resetPasswordWithSupabase(email)
                            errorMessage = null
                            showSuccessDialog = true
                        } catch (e: Exception) {
                            errorMessage = "Erreur lors de l'envoi de l'email"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.send_reset_link),
                    color = black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Séparateur
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                Text(
                    text = stringResource(R.string.or),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Connexion Google
            GoogleSignInButtonWithLogic(
                authManager = authManager,
                coroutineScope = coroutineScope,
                navController = navController,
                onSuccess = {
                    email = ""
                },
                onError = { msg ->
                    dialogMessage = msg
                    email = ""
                },
                keyboardController = keyboardController
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Lien vers inscription
            TextButton(onClick = onRegisterClick) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            append(stringResource(R.string.no_account))
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        ) {
                            append(stringResource(R.string.sign_up))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton retour
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = darkGray),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.back), color = Color.White)
            }
        }
        ErrorDialog(message = errorMessage, onDismiss = { errorMessage = null })
    }

    if (showSuccessDialog) {
        SuccessDialog(
            message  = stringResource(R.string.reset_link_sent),
            onDismiss = { showSuccessDialog = false }
        )
    }
}
@Composable
fun NewPasswordScreen(
    token: String,
    onPasswordReset: () -> Unit,
    email:String) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val password_not_match = stringResource(R.string.password_not_match)
    val password_length_error = stringResource(R.string.password_length_error)
    val unknownError = stringResource(R.string.unknown_error)
    val invalid_link = stringResource(R.string.invalid_link)
    val needed_password = stringResource(R.string.password_needed)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(black)
    ) {
        GradientScreen()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.new_password),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Nouveau mot de passe
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.new_password),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.password_placeholder),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmation
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.confirm_password),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.password_placeholder),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (password.isBlank()) {
                        errorMessage = needed_password
                        return@Button
                    }
                    if (password.length < 8) {
                        errorMessage = password_length_error
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = password_not_match
                        return@Button
                    }
                    loading = true
                    coroutineScope.launch {
                        val error = updatePasswordWithSupabase(
                            newPassword = password,
                            accessToken = token,
                            emailAdress = email
                        )
                        loading = false
                        if (error == null) {
                            errorMessage = null
                            showSuccessDialog = true
                        } else {
                            errorMessage = when {
                                error.contains("length", ignoreCase = true) -> password_length_error
                                error.contains("unknown", ignoreCase = true) -> unknownError
                                error.contains("token", ignoreCase = true) -> invalid_link
                                else -> error
                            }
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.new_password),
                    color = black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        ErrorDialog(message = errorMessage, onDismiss = { errorMessage = null })
    }

    if (showSuccessDialog) {
        SuccessDialog(
            message = stringResource(R.string.password_updated),
            onDismiss = {
                showSuccessDialog = false
                onPasswordReset()
            }
        )
    }
}
