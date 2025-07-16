package com.example.smarthr_app.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.smarthr_app.data.model.UserRegisterRequest
import com.example.smarthr_app.data.model.UserRole
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.theme.SecondaryPurple
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import com.example.smarthr_app.utils.ValidationUtils
import com.example.smarthr_app.utils.ValidationResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHRDashboard: () -> Unit,
    onNavigateToEmployeeDashboard: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.ROLE_USER) }
    var companyCode by remember { mutableStateOf("") }

    // Validation states
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var companyCodeError by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.collectAsState(initial = null)

    // Handle registration result
    LaunchedEffect(registerState) {
        when (val currentState = registerState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Account created successfully!")
                delay(500)
                if (currentState.data.user.role == "ROLE_HR") {
                    onNavigateToHRDashboard()
                } else {
                    onNavigateToEmployeeDashboard()
                }
                viewModel.clearRegisterState()
            }
            is Resource.Error -> {
                // Show specific error messages
                when {
                    currentState.message.contains("email already exists", ignoreCase = true) ||
                            currentState.message.contains("account with this email", ignoreCase = true) -> {
                        ToastHelper.showErrorToast(context, "Account with this email already exists")
                    }
                    currentState.message.contains("company code", ignoreCase = true) -> {
                        ToastHelper.showErrorToast(context, "Company code does not exist. Please check with your HR.")
                    }
                    currentState.message.contains("network", ignoreCase = true) -> {
                        ToastHelper.showErrorToast(context, "Network error. Please check your internet connection.")
                    }
                    else -> {
                        ToastHelper.showErrorToast(context, currentState.message)
                    }
                }
            }
            else -> {}
        }
    }

    // Real-time validation
    LaunchedEffect(name) {
        if (name.isNotBlank()) {
            val validation = ValidationUtils.validateName(name)
            nameError = if (validation.isValid) "" else validation.errorMessage
        } else {
            nameError = ""
        }
    }

    LaunchedEffect(email) {
        if (email.isNotBlank()) {
            val validation = ValidationUtils.validateEmail(email)
            emailError = if (validation.isValid) "" else validation.errorMessage
        } else {
            emailError = ""
        }
    }

    LaunchedEffect(phone) {
        if (phone.isNotBlank()) {
            val validation = ValidationUtils.validatePhone(phone)
            phoneError = if (validation.isValid) "" else validation.errorMessage
        } else {
            phoneError = ""
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

    LaunchedEffect(companyCode) {
        if (companyCode.isNotBlank() && selectedRole == UserRole.ROLE_USER) {
            val validation = ValidationUtils.validateCompanyCode(companyCode)
            companyCodeError = if (validation.isValid) "" else validation.errorMessage
        } else {
            companyCodeError = ""
        }
    }

    fun validateAllFields(): Boolean {
        val nameValidation = ValidationUtils.validateName(name)
        val emailValidation = ValidationUtils.validateEmail(email)
        val phoneValidation = ValidationUtils.validatePhone(phone)
        val passwordValidation = ValidationUtils.validatePassword(password)
        val companyCodeValidation = if (selectedRole == UserRole.ROLE_USER && companyCode.isNotBlank()) {
            ValidationUtils.validateCompanyCode(companyCode)
        } else ValidationResult(true, "")

        nameError = if (nameValidation.isValid) "" else nameValidation.errorMessage
        emailError = if (emailValidation.isValid) "" else emailValidation.errorMessage
        phoneError = if (phoneValidation.isValid) "" else phoneValidation.errorMessage
        passwordError = if (passwordValidation.isValid) "" else passwordValidation.errorMessage
        companyCodeError = if (companyCodeValidation.isValid) "" else companyCodeValidation.errorMessage

        val hasErrors = !nameValidation.isValid || !emailValidation.isValid ||
                !phoneValidation.isValid || !passwordValidation.isValid ||
                !companyCodeValidation.isValid

        if (hasErrors) {
            val firstError = listOfNotNull(
                nameValidation.errorMessage.takeIf { !nameValidation.isValid },
                emailValidation.errorMessage.takeIf { !emailValidation.isValid },
                phoneValidation.errorMessage.takeIf { !phoneValidation.isValid },
                passwordValidation.errorMessage.takeIf { !passwordValidation.isValid },
                companyCodeValidation.errorMessage.takeIf { !companyCodeValidation.isValid }
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Registration Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Join SmartHR",
                        style = MaterialTheme.typography.headlineSmall,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Role Selection
                    Text(
                        text = "Select Role",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilterChip(
                            selected = selectedRole == UserRole.ROLE_HR,
                            onClick = { selectedRole = UserRole.ROLE_HR },
                            label = { Text("HR") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryPurple
                            )
                        )
                        FilterChip(
                            selected = selectedRole == UserRole.ROLE_USER,
                            onClick = { selectedRole = UserRole.ROLE_USER },
                            label = { Text("Employee") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError.isNotEmpty(),
                        supportingText = if (nameError.isNotEmpty()) {
                            { Text(nameError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { newValue ->
                            // Only allow digits and limit to 10
                            val digits = newValue.filter { it.isDigit() }
                            if (digits.length <= 10) {
                                phone = digits
                            }
                        },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneError.isNotEmpty(),
                        supportingText = if (phoneError.isNotEmpty()) {
                            { Text(phoneError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        prefix = { Text("+91 ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Company Code Field (only for employees)
                    if (selectedRole == UserRole.ROLE_USER) {
                        OutlinedTextField(
                            value = companyCode,
                            onValueChange = { companyCode = it },
                            label = { Text("Company Code (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = companyCodeError.isNotEmpty(),
                            supportingText = if (companyCodeError.isNotEmpty()) {
                                { Text(companyCodeError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

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
                        } else {
                            { Text("Min 8 chars: A-Z, a-z, 0-9, special char") }
                        },
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

                    val currentRegisterState = registerState

                    // Register Button
                    Button(
                        onClick = {
                            if (validateAllFields()) {
                                val formattedPhone = ValidationUtils.formatPhoneNumber(phone)
                                viewModel.registerUser(
                                    UserRegisterRequest(
                                        name = name.trim(),
                                        email = email.trim().lowercase(),
                                        phone = formattedPhone,
                                        password = password,
                                        gender = "M",
                                        role = if (selectedRole == UserRole.ROLE_HR) "ROLE_HR" else "ROLE_USER",
                                        companyCode = if (selectedRole == UserRole.ROLE_USER && companyCode.isNotBlank()) companyCode.trim() else null
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
                        enabled = currentRegisterState !is Resource.Loading
                    ) {
                        if (currentRegisterState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                }
            }

        }
    }
}