package com.hypercart

import android.os.Bundle
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.hypercart.config.SecureConfig
import com.hypercart.navigation.NavRoutes
import com.hypercart.ui.screens.AddStoreScreen
import com.hypercart.ui.screens.CartScreen
import com.hypercart.ui.screens.HomeScreen
import com.hypercart.ui.screens.LoginScreen
import com.hypercart.ui.screens.RegisterScreen
import com.hypercart.ui.screens.StoreDetailScreen
import com.hypercart.ui.screens.StoreProductsScreen
import com.hypercart.ui.theme.HypercartTheme
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.darkGray
import com.hypercart.ui.theme.night
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random

data class Star(val x: Float, val y: Float, val radius: Float)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize secure configuration
        SecureConfig.initialize(this)

        val token = intent?.data?.getQueryParameter("token")
        val email = intent?.data?.getQueryParameter("email")
        
        setContent {
            HypercartApp(startToken = token, email = email)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HypercartApp(startToken: String? = null, email: String? = null) {
    val navController: NavHostController = rememberNavController()

    // Handle deep link for password reset
    androidx.compose.runtime.LaunchedEffect(startToken) {
        if (!startToken.isNullOrEmpty() && !email.isNullOrEmpty()) {
            navController.navigate(NavRoutes.NewPassword.createRoute(startToken, email)) {
                popUpTo(NavRoutes.Login.route) { inclusive = true }
            }
        }
    }

    HypercartTheme {
        AnimatedNavHost(
            navController = navController,
            startDestination = NavRoutes.Login.route
        ) {
            composable(route = NavRoutes.Login.route) {
                LoginScreen(navController)
            }

            composable(route = NavRoutes.Home.route) {
                HomeScreen(navController)
            }

            composable(route = NavRoutes.AddStore.route) {
                AddStoreScreen(navController)
            }

            composable(
                route = NavRoutes.StoreDetail.route,
                arguments = listOf(navArgument("storeId") { defaultValue = "" })
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
                StoreDetailScreen(navController, storeId)
            }

            composable(
                route = NavRoutes.StoreProducts.route,
                arguments = listOf(navArgument("storeId") { defaultValue = "" })
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
                StoreProductsScreen(navController, storeId)
            }

            composable(
                route = NavRoutes.Cart.route,
                arguments = listOf(navArgument("storeId") { defaultValue = "" })
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
                CartScreen(navController, storeId)
            }

            composable(route = NavRoutes.ResetPassword.route) {
                ResetPasswordScreen(
                    onBack = { navController.popBackStack() },
                    navController = navController,
                    onRegisterClick = { navController.navigate(NavRoutes.Register.route) }
                )
            }

            composable(
                route = NavRoutes.Register.route,
                enterTransition = { fadeIn(animationSpec = tween(1000)) },
                exitTransition = { fadeOut(animationSpec = tween(700)) },
                popEnterTransition = { fadeIn(animationSpec = tween(800)) },
                popExitTransition = { fadeOut(animationSpec = tween(500)) }
            ) {
                RegisterScreen(navController)
            }

            composable(
                route = NavRoutes.NewPassword.route + "?token={token}&email={email}",
                arguments = listOf(
                    navArgument(NavRoutes.NewPassword.TOKEN_ARG) { defaultValue = "" },
                    navArgument(NavRoutes.NewPassword.EMAIL_ARG) { defaultValue = "" }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://hypercart.com/reset-password?token={token}&email={email}"
                    }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString(NavRoutes.NewPassword.TOKEN_ARG) ?: ""
                val emailAddress = backStackEntry.arguments?.getString(NavRoutes.NewPassword.EMAIL_ARG) ?: ""
                NewPasswordScreen(
                    token = token,
                    onPasswordReset = { navController.navigate(NavRoutes.Login.route) },
                    email = emailAddress
                )
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

@Composable
fun GoogleSignInButtonWithLogic(
    authManager: AuthManager,
    coroutineScope: CoroutineScope,
    navController: NavController?,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {},
    keyboardController: SoftwareKeyboardController? = null
) {
    GoogleSignInButton(
        onClick = {
            keyboardController?.hide()
            authManager.loginGoogleUser()
                .onEach { result ->
                    when (result) {
                        is AuthResponse.Success -> {
                            navController?.navigate(NavRoutes.Home.route)
                            onSuccess()
                        }
                        is AuthResponse.Error -> {
                            val msg = result.message ?: ""
                            onError(msg)
                        }
                    }
                }
                .launchIn(coroutineScope)
        }
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
    val stars = androidx.compose.runtime.remember {
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

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
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



