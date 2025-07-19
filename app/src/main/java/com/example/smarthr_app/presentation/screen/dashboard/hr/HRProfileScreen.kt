package com.example.smarthr_app.presentation.screen.dashboard.hr

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val user by authViewModel.user.collectAsState(initial = null)
    val uploadImageState by authViewModel.uploadImageState.collectAsState(initial = null)

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            authViewModel.uploadProfileImage(context, it)
        }
    }

    // Handle image upload response
    LaunchedEffect(uploadImageState) {
        when (val state = uploadImageState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Profile image updated successfully!")
                authViewModel.clearUploadImageState()
                authViewModel.refreshProfile()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, "Failed to upload image: ${state.message}")
                authViewModel.clearUploadImageState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Bar with Back Button
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
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { showLogoutDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }
        }

        // Profile Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Avatar and Basic Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture with upload option
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user?.imageUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(50.dp),
                                    tint = PrimaryPurple
                                )
                            }
                        }

                        // Camera icon for upload
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            FloatingActionButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.size(32.dp),
                                containerColor = PrimaryPurple,
                                contentColor = Color.White
                            ) {
                                if (uploadImageState is Resource.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Upload Photo",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    Text(
                        text = user?.name ?: "HR User",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Role Badge
                    Surface(
                        modifier = Modifier.padding(top = 8.dp),
                        color = PrimaryPurple.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "HR Manager",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit Profile Button
                    OutlinedButton(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryPurple
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }

            // Contact Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = user?.email ?: "Not available"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone
                    ProfileInfoRow(
                        icon = Icons.Default.Phone,
                        label = "Phone",
                        value = user?.phone ?: "Not available"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gender
                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        label = "Gender",
                        value = when (user?.gender) {
                            "M" -> "Male"
                            "F" -> "Female"
                            else -> "Not specified"
                        }
                    )
                }
            }

            // Company Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Company Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Company Code with Copy functionality
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Company Code",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = user?.companyCode ?: "Not available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Copy Button
                        if (!user?.companyCode.isNullOrBlank()) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(user?.companyCode ?: ""))
                                    ToastHelper.showSuccessToast(context, "Company code copied to clipboard!")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Company Code",
                                    tint = PrimaryPurple
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Position
                    ProfileInfoRow(
                        icon = Icons.Default.Work,
                        label = "Position",
                        value = user?.position ?: "Not specified"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Department
                    ProfileInfoRow(
                        icon = Icons.Default.Group,
                        label = "Department",
                        value = user?.department ?: "Not specified"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to logout?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}