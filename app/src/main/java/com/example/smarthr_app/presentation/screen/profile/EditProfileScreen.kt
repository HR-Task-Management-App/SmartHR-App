package com.example.smarthr_app.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smarthr_app.data.model.Department
import com.example.smarthr_app.data.model.Gender
import com.example.smarthr_app.data.model.Position
import com.example.smarthr_app.data.model.UpdateProfileRequest
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import com.example.smarthr_app.utils.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val user by authViewModel.user.collectAsState(initial = null)
    val updateProfileState by authViewModel.updateProfileState.collectAsState(initial = null)

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var selectedPosition by remember { mutableStateOf<Position?>(null) }
    var selectedDepartment by remember { mutableStateOf<Department?>(null) }

    var expandedGender by remember { mutableStateOf(false) }
    var expandedPosition by remember { mutableStateOf(false) }
    var expandedDepartment by remember { mutableStateOf(false) }

    // Initialize fields with current user data
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            phone = it.phone?.replace("+91", "") ?: ""
            selectedGender = when (it.gender) {
                "M" -> Gender.M
                "F" -> Gender.F
                else -> null
            }
            selectedPosition = try {
                it.position?.let { pos -> Position.valueOf(pos) }
            } catch (e: Exception) { null }

            selectedDepartment = try {
                it.department?.let { dept -> Department.valueOf(dept) }
            } catch (e: Exception) { null }
        }
    }

    // Handle update response
    LaunchedEffect(updateProfileState) {
        when (val state = updateProfileState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Profile updated successfully!")
                authViewModel.clearUpdateProfileState()
                onNavigateBack()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                authViewModel.clearUpdateProfileState()
            }
            else -> {}
        }
    }

    fun saveProfile() {
        val nameValidation = ValidationUtils.validateName(name)
        if (!nameValidation.isValid) {
            ToastHelper.showErrorToast(context, nameValidation.errorMessage)
            return
        }

        val cleanPhone = phone.filter { it.isDigit() }
        if (cleanPhone.isNotBlank()) {
            val phoneValidation = ValidationUtils.validatePhone(cleanPhone)
            if (!phoneValidation.isValid) {
                ToastHelper.showErrorToast(context, phoneValidation.errorMessage)
                return
            }
        }

        val formattedPhone = if (cleanPhone.isNotBlank()) {
            ValidationUtils.formatPhoneNumber(cleanPhone)
        } else null

        val updateRequest = UpdateProfileRequest(
            name = name.trim(),
            phone = formattedPhone,
            gender = selectedGender?.name,
            position = selectedPosition?.name,
            department = selectedDepartment?.name
        )

        authViewModel.updateProfile(updateRequest)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PrimaryPurple),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save Button
                TextButton(
                    onClick = { saveProfile() },
                    enabled = updateProfileState !is Resource.Loading
                ) {
                    if (updateProfileState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Save",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple,
                            focusedLeadingIconColor = PrimaryPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { newValue ->
                            val digits = newValue.filter { it.isDigit() }
                            if (digits.length <= 10) {
                                phone = digits
                            }
                        },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null
                            )
                        },
                        prefix = { Text("+91 ") },
                        supportingText = { Text("10 digits only") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple,
                            focusedLeadingIconColor = PrimaryPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gender Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedGender,
                        onExpandedChange = { expandedGender = !expandedGender }
                    ) {
                        OutlinedTextField(
                            value = when (selectedGender) {
                                Gender.M -> "Male"
                                Gender.F -> "Female"
                                null -> ""
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedGender
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple,
                                focusedLeadingIconColor = PrimaryPurple
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedGender,
                            onDismissRequest = { expandedGender = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Male") },
                                onClick = {
                                    selectedGender = Gender.M
                                    expandedGender = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Female") },
                                onClick = {
                                    selectedGender = Gender.F
                                    expandedGender = false
                                }
                            )
                        }
                    }
                }
            }

            // Professional Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Professional Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Position Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedPosition,
                        onExpandedChange = { expandedPosition = !expandedPosition }
                    ) {
                        OutlinedTextField(
                            value = selectedPosition?.name?.replace("_", " ") ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Position") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedPosition
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple,
                                focusedLeadingIconColor = PrimaryPurple
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedPosition,
                            onDismissRequest = { expandedPosition = false }
                        ) {
                            Position.values().forEach { position ->
                                DropdownMenuItem(
                                    text = { Text(position.name.replace("_", " ")) },
                                    onClick = {
                                        selectedPosition = position
                                        expandedPosition = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Department Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedDepartment,
                        onExpandedChange = { expandedDepartment = !expandedDepartment }
                    ) {
                        OutlinedTextField(
                            value = selectedDepartment?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Department") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedDepartment
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple,
                                focusedLeadingIconColor = PrimaryPurple
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDepartment,
                            onDismissRequest = { expandedDepartment = false }
                        ) {
                            Department.values().forEach { department ->
                                DropdownMenuItem(
                                    text = { Text(department.name) },
                                    onClick = {
                                        selectedDepartment = department
                                        expandedDepartment = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}