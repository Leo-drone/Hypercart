package com.hypercart.ui.screens

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hypercart.R
import com.hypercart.data.AuthRepository
import com.hypercart.navigation.NavRoutes
import com.hypercart.ui.components.HypercartButton
import com.hypercart.ui.components.HypercartTextField
import com.hypercart.ui.theme.blueSkye
import com.hypercart.ui.theme.night
import com.hypercart.ui.viewmodel.RegisterViewModel
import com.hypercart.AuthManager
import com.hypercart.ErrorDialog
import com.hypercart.SuccessDialog

@Composable
fun RegisterScreen(
    navController: NavController?
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val repository = remember { AuthRepository(authManager) }
    
    val viewModel: RegisterViewModel = viewModel {
        RegisterViewModel(repository)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Navigation effects
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            navController?.navigate(NavRoutes.Login.route)
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
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

                Spacer(modifier = Modifier.height(28.dp))

                // Register button
                HypercartButton(
                    text = stringResource(R.string.sign_up),
                    onClick = {
                        keyboardController?.hide()
                        viewModel.signUpWithEmail()
                    },
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login link
                TextButton(onClick = { navController?.navigate(NavRoutes.Login.route) }) {
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

    // Success dialog
    SuccessDialog(
        message = stringResource(R.string.successful_registration),
        onDismiss = {
            viewModel.resetSuccess()
            navController?.navigate(NavRoutes.Login.route)
        }
    )

    // Error dialog
    ErrorDialog(
        message = uiState.errorMessage,
        onDismiss = viewModel::clearError
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
                    colors = listOf(night, blueSkye),
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
fun StarField(numberOfStars: Int, modifier: Modifier = Modifier) {
    val stars = androidx.compose.runtime.remember {
        List(numberOfStars) {
            com.hypercart.Star(
                x = kotlin.random.Random.nextFloat(),
                y = kotlin.random.Random.nextFloat(),
                radius = kotlin.random.Random.nextFloat() * 2f + 0.5f
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