package com.example.smarthr_app.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smarthr_app.data.model.LoginRequest
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.theme.SecondaryPurple
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import com.example.smarthr_app.utils.ValidationUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    chatViewModel: ChatViewModel,
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChatScreen:()->Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation states
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState(initial = null)

    // Handle login result
    LaunchedEffect(authState) {
        when (val currentState = authState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Login successful!")
                delay(500)
                if (currentState.data.user.role == "ROLE_HR") {
                    chatViewModel.initSocket(currentState.data.user.userId)
                    onNavigateToChatScreen()
                } else {
                    chatViewModel.initSocket(currentState.data.user.userId)
                    onNavigateToChatScreen()
                }
                viewModel.clearAuthState()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, currentState.message)
            }
            else -> {}
        }
    }

    // Real-time validation
    LaunchedEffect(email) {
        if (email.isNotBlank()) {
            val validation = ValidationUtils.validateEmail(email)
            emailError = if (validation.isValid) "" else validation.errorMessage
        } else {
            emailError = ""
        }
    }

    LaunchedEffect(password) {
        if (password.isNotBlank()) {
            val validation = ValidationUtils.validatePassword(password)
            passwordError = if (validation.isValid) "" else validation.errorMessage
        } else {
            passwordError = ""
        }
    }

    fun validateLoginFields(): Boolean {
        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = ValidationUtils.validatePassword(password)

        emailError = if (emailValidation.isValid) "" else emailValidation.errorMessage
        passwordError = if (passwordValidation.isValid) "" else passwordValidation.errorMessage

        val hasErrors = !emailValidation.isValid || !passwordValidation.isValid

        if (hasErrors) {
            val firstError = listOfNotNull(
                emailValidation.errorMessage.takeIf { !emailValidation.isValid },
                passwordValidation.errorMessage.takeIf { !passwordValidation.isValid }
            ).firstOrNull()

            firstError?.let { ToastHelper.showErrorToast(context, it) }
            return false
        }
        return true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryPurple, SecondaryPurple)
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Login Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Welcome Back!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Sign in to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = emailError.isNotEmpty(),
                            supportingText = if (emailError.isNotEmpty()) {
                                { Text(emailError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = passwordError.isNotEmpty(),
                            supportingText = if (passwordError.isNotEmpty()) {
                                { Text(passwordError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        val currentAuthState = authState

                        // Login Button
                        Button(
                            onClick = {
                                if (validateLoginFields()) {
                                    viewModel.login(
                                        LoginRequest(
                                            email = email.trim().lowercase(),
                                            password = password
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = currentAuthState !is Resource.Loading
                        ) {
                            if (currentAuthState is Resource.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Login",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}