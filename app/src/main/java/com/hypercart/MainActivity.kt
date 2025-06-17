package com.hypercart

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.hypercart.ui.theme.HypercartTheme
import com.hypercart.ui.theme.black
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.darkGray
import com.hypercart.ui.theme.night
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Star(val x: Float, val y: Float, val radius: Float)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val token = intent?.data?.getQueryParameter("token")
        setContent {
            HypercartApp(startToken = token)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HypercartApp(startToken: String? = null) {
    val navController: NavHostController = rememberNavController()

    LaunchedEffect(startToken) {
        if (!startToken.isNullOrEmpty()) {
            navController.navigate("new_password?token=$startToken")
        }
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(
            route = "login?message={message}",
            arguments = listOf(navArgument("message") { defaultValue = "" })
        ) { backStackEntry ->
            LoginScreen(navController)
        }

        composable("products") {
            ProductListScreen()
        }

        composable("reset_password") {
            ResetPasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "register",
            enterTransition = { fadeIn(animationSpec = tween(1000)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) },
            popEnterTransition = { fadeIn(animationSpec = tween(800)) },
            popExitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            RegisterScreen(navController)
        }

        composable(
            route = "new_password?token={token}",
            arguments = listOf(navArgument("token") { defaultValue = "" })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            Log.i("debug", "Token received: $token")
            NewPasswordScreen(
                token = token,
                onPasswordReset = { navController.navigate("login") },
                onError = { /* Affiche une erreur */ }
            )
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

@Composable
fun LoginScreen(navController: NavController?) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Un seul état pour tous les messages d’erreur
    var dialogMessage by remember { mutableStateOf<String?>(null) }

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
                    TextButton(
                        onClick = { navController?.navigate("reset_password") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Mot de passe oublié ?",
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bouton Connexion Email
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
                            else -> {
                                authManager.signInWithEmail(emailValue, passwordValue)
                                    .onEach { result ->
                                        when (result) {
                                            is AuthResponse.Success -> {
                                                navController?.navigate("products")
                                                emailValue = ""
                                                passwordValue = ""
                                            }
                                            is AuthResponse.Error -> {
                                                val msg = result.message ?: "Erreur inconnue"
                                                dialogMessage = when {
                                                    msg.contains("non vérifié", ignoreCase = true) -> "Compte non vérifié"
                                                    msg.contains("identifiants", ignoreCase = true) ||
                                                            msg.contains("incorrect", ignoreCase = true) ||
                                                            msg.contains("échoué", ignoreCase = true) -> "Email ou mot de passe incorrect"
                                                    else -> msg
                                                }
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
                        text = stringResource(R.string.log_in),
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
                GoogleSignInButton(
                    onClick = {
                        keyboardController?.hide()
                        authManager.loginGoogleUser()
                            .onEach { result ->
                                when (result) {
                                    is AuthResponse.Success -> {
                                        navController?.navigate("products")
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
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Lien vers inscription
                TextButton(onClick = {
                    navController?.navigate("register")
                }) {
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
            }
        }
    }

    // Dialog pour toutes les erreurs
    ErrorDialog(
        message = dialogMessage,
        onDismiss = { dialogMessage = null }
    )
}


@Composable
fun GradientScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight(0.35f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(darkGray, night, blueSkye)
                )
            )
    ) {
        StarField(
            numberOfStars = 250,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun StarField(numberOfStars: Int, modifier: Modifier = Modifier) {
    val stars = remember {
        List(numberOfStars) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 2f + 0.5f
            )
        }
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        stars.forEach { star ->
            drawCircle(
                color = Color.White,
                radius = star.radius,
                center = Offset(star.x * canvasWidth, star.y * canvasHeight)
            )
        }
    }
}

@Composable
private fun ConnectionHeader() {
    Text(
        text = stringResource(R.string.login_subtitle),
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    HypercartTheme {
        LoginScreen(navController = null)
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(R.string.sign_in_with_google),
            color = Color.White,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Réinitialiser le mot de passe", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    resetPasswordWithSupabase(email)
                    successMessage = "Un email de réinitialisation a été envoyé."
                    errorMessage = null

                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Envoyer l'email")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Retour")
        }
        if (successMessage != null) {
            Text(successMessage!!, color = MaterialTheme.colorScheme.primary)
        }
    }
    ErrorDialog(message = errorMessage, onDismiss = { errorMessage = null })
}

@Composable
fun NewPasswordScreen(
    token: String,
    onPasswordReset: () -> Unit,
    onError: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nouveau mot de passe", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nouveau mot de passe") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmer le mot de passe") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Les mots de passe ne correspondent pas."
                    return@Button
                }
                loading = true
                coroutineScope.launch {
                    val error = updatePasswordWithSupabase(newPassword = password, accessToken = token)
                    loading = false
                    if (error == null) {
                        successMessage = "Mot de passe mis à jour avec succès."
                        onPasswordReset()
                    } else {
                        errorMessage = when {
                            error.contains("length", ignoreCase = true) -> "Le mot de passe est trop court (au moins 8 caractères)."
                            error.contains("unknown", ignoreCase = true) -> "Erreur inconnue, réessayez."
                            error.contains("token", ignoreCase = true) -> "Lien de réinitialisation invalide ou expiré."
                            else -> error
                        }
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Chargement..." else "Valider")
        }

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        if (successMessage != null) {
            Text(successMessage!!, color = MaterialTheme.colorScheme.primary)
        }
    }
}