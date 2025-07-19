package com.example.smarthr_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.presentation.navigation.NavGraph
import com.example.smarthr_app.presentation.navigation.Screen
import com.example.smarthr_app.presentation.theme.SmartHRTheme
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHRTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartHRApp()
                }
            }
        }
    }
}

@Composable
fun SmartHRApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    val authRepository = AuthRepository(dataStoreManager)
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }

    var startDestination by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    // Determine start destination based on auth state with delay to check persistence
    LaunchedEffect(Unit) {
        // Add small delay to ensure DataStore is properly loaded
        delay(100)

        authRepository.isLoggedIn.collect { isLoggedIn ->
            if (isLoggedIn) {
                authRepository.user.collect { user ->
                    startDestination = when (user?.role) {
                        com.example.smarthr_app.data.model.UserRole.ROLE_HR -> Screen.HRDashboard.route
                        com.example.smarthr_app.data.model.UserRole.ROLE_USER -> Screen.EmployeeDashboard.route
                        else -> Screen.RoleSelection.route
                    }
                    isInitialized = true
                    return@collect
                }
            } else {
                startDestination = Screen.RoleSelection.route
                isInitialized = true
            }
        }
    }

    // Only show NavGraph after determining the correct start destination
    if (isInitialized && startDestination != null) {
        NavGraph(
            navController = navController,
            startDestination = startDestination!!
        )
    }
}