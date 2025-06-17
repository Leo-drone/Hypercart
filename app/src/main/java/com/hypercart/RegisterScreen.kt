package com.hypercart


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hypercart.ui.theme.black
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.darkGray
import com.hypercart.ui.theme.night
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun RegisterScreen(navController: NavController?) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Un seul état pour tous les messages d’erreur
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(black)
                .padding(padding)
        ) {
            GradientEarthScreen()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RegisterHeader()
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
                        value = emailValue,
                        onValueChange = { emailValue = it },
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

                Spacer(modifier = Modifier.height(16.dp))

                // Mot de passe
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.password),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = passwordValue,
                        onValueChange = { passwordValue = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.password_placeholder),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
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

                // Bouton inscription
                Button(
                    onClick = {
                        keyboardController?.hide()
                        when {
                            emailValue.isBlank() -> {
                                dialogMessage = "Email requis"
                            }
                            !isValidEmail(emailValue) -> {
                                dialogMessage = "Email invalide"
                            }
                            passwordValue.isBlank() -> {
                                dialogMessage = "Mot de passe requis"
                            }
                            passwordValue.length < 8 -> {
                                dialogMessage = "Mot de passe trop court"
                            }
                            else -> {
                                authManager.signUpWithEmail(emailValue, passwordValue)
                                    .onEach { result ->
                                        when (result) {
                                            is AuthResponse.Success -> {
                                                showSuccessDialog = true
                                                emailValue = ""
                                                passwordValue = ""
                                            }
                                            is AuthResponse.Error -> {
                                                val msg = result.message ?: "Erreur inconnue"
                                                dialogMessage = msg
                                                passwordValue = ""
                                            }
                                        }
                                    }
                                    .launchIn(coroutineScope)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.sign_up),
                        color = black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Lien vers connexion
                TextButton(onClick = {
                    navController?.navigate("login")
                }) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Light,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            ) {
                                append(stringResource(R.string.already_have_account))
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            ) {
                                append(" ${stringResource(R.string.log_in)}")
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialog succès inscription
    SuccessDialog(
        showDialog = showSuccessDialog,
        onDismiss = {
            showSuccessDialog = false
            navController?.navigate("login")
        }
    )

    // Dialog pour toutes les erreurs
    ErrorDialog(
        message = dialogMessage,
        onDismiss = { dialogMessage = null }
    )
}

@Composable
private fun RegisterHeader() {
    Text(
        text = stringResource(R.string.register_title),
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun GradientEarthScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        night,
                        blueSkye
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        StarField(
            numberOfStars = 250,
            modifier = Modifier.fillMaxSize()
        )

        EarthImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .align(Alignment.BottomCenter)
        )
    }
}


@Composable
fun EarthImage(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = canvasWidth * 1.2f

        val center = Offset(x = canvasWidth / 2, y = canvasHeight * 1.2f)

        drawCircle(
            color = Color(0xFF4FC3F7),
            radius = radius,
            center = center
        )

        val continentPath = Path().apply {
            moveTo(center.x - radius * 0.6f, center.y - radius * 0.8f)
            cubicTo(
                center.x - radius * 0.4f, center.y - radius * 0.9f,
                center.x - radius * 0.2f, center.y - radius * 0.7f,
                center.x - radius * 0.1f, center.y - radius * 0.5f
            )
            cubicTo(
                center.x - radius * 0.3f, center.y - radius * 0.3f,
                center.x - radius * 0.5f, center.y - radius * 0.2f,
                center.x - radius * 0.6f, center.y - radius * 0.5f
            )
            close()

            moveTo(center.x + radius * 0.3f, center.y - radius * 0.9f)
            cubicTo(
                center.x + radius * 0.4f, center.y - radius * 0.85f,
                center.x + radius * 0.35f, center.y - radius * 0.75f,
                center.x + radius * 0.25f, center.y - radius * 0.7f
            )
            cubicTo(
                center.x + radius * 0.2f, center.y - radius * 0.65f,
                center.x + radius * 0.2f, center.y - radius * 0.6f,
                center.x + radius * 0.3f, center.y - radius * 0.6f
            )
            close()

            moveTo(center.x - radius * 0.2f, center.y - radius * 0.4f)
            cubicTo(
                center.x - radius * 0.1f, center.y - radius * 0.35f,
                center.x - radius * 0.05f, center.y - radius * 0.3f,
                center.x - radius * 0.1f, center.y - radius * 0.2f
            )
            cubicTo(
                center.x - radius * 0.2f, center.y - radius * 0.2f,
                center.x - radius * 0.3f, center.y - radius * 0.3f,
                center.x - radius * 0.2f, center.y - radius * 0.4f
            )
            close()
        }

        drawPath(
            path = continentPath,
            color = Color(0xFF81C784)
        )
    }
}

@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(navController = null)
}



