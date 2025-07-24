package com.example.smarthr_app

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.data.repository.ChatRepository
import com.example.smarthr_app.data.repository.TaskRepository
import com.example.smarthr_app.presentation.navigation.NavGraph
import com.example.smarthr_app.presentation.navigation.Screen
import com.example.smarthr_app.presentation.theme.SmartHRTheme
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.utils.createNotificationChannel
import com.example.smarthr_app.utils.showNotification
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
    val user = authViewModel.user.collectAsState(initial = null).value

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
                        com.example.smarthr_app.data.model.UserRole.ROLE_HR -> Screen.ChatList.route
                        com.example.smarthr_app.data.model.UserRole.ROLE_USER -> Screen.ChatList.route
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

