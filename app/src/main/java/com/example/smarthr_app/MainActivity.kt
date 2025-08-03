package com.example.smarthr_app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.data.repository.ChatRepository
import com.example.smarthr_app.presentation.navigation.NavGraph
import com.example.smarthr_app.presentation.navigation.Screen
import com.example.smarthr_app.presentation.theme.SmartHRTheme
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.utils.notification.createNotificationChannel
import com.example.smarthr_app.utils.notification.showNotification
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(applicationContext)
        requestNotificationPermissionIfNeeded()
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
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
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
    val chatRepository = ChatRepository(dataStoreManager)
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
    val chatViewModel : ChatViewModel = viewModel { ChatViewModel(chatRepository) }
    var startDestination by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }


    val notificationEvent by chatViewModel.notificationEvent.collectAsState()

    // Show notification
    LaunchedEffect(notificationEvent) {
        notificationEvent?.let { (title, message) ->
            showNotification(context, title, message)
            chatViewModel.clearNotificationEvent() // prevent repeat
        }
    }

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