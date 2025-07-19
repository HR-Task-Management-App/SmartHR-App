package com.example.smarthr_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.data.repository.CompanyRepository
import com.example.smarthr_app.presentation.screen.auth.*
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeDashboardScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeProfileScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeCompanyManagementScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRDashboardScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRProfileScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.EmployeeManagementScreen
import com.example.smarthr_app.presentation.screen.profile.EditProfileScreen
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.CompanyViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.smarthr_app.data.repository.TaskRepository
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeTaskDetailScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.CreateTaskScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRTaskManagementScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.TaskDetailScreen
import com.example.smarthr_app.presentation.viewmodel.TaskViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    val authRepository = AuthRepository(dataStoreManager)
    val companyRepository = CompanyRepository(dataStoreManager)
    val taskRepository = TaskRepository(dataStoreManager)

    val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
    val companyViewModel: CompanyViewModel = viewModel { CompanyViewModel(companyRepository) }
    val taskViewModel: TaskViewModel = viewModel { TaskViewModel(taskRepository) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHRDashboard = {
                    navController.navigate(Screen.HRDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToEmployeeDashboard = {
                    navController.navigate(Screen.EmployeeDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHRDashboard = {
                    navController.navigate(Screen.HRDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToEmployeeDashboard = {
                    navController.navigate(Screen.EmployeeDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.HRDashboard.route) {
            HRDashboardScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.HRDashboard.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToEmployees = {
                    navController.navigate(Screen.EmployeeManagement.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.HRProfile.route)
                },
                onNavigateToTasks = {
                    navController.navigate(Screen.HRTaskManagement.route)
                }
            )
        }

        // HR Task Management Routes
        composable(Screen.HRTaskManagement.route) {
            HRTaskManagementScreen(
                taskViewModel = taskViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                }
            )
        }

        composable(Screen.CreateTask.route) {
            CreateTaskScreen(
                taskViewModel = taskViewModel,
                companyViewModel = companyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = Screen.TaskDetail.arguments
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                taskViewModel = taskViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = Screen.EditTask.arguments
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            CreateTaskScreen(
                taskViewModel = taskViewModel,
                companyViewModel = companyViewModel,
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Employee Task Routes
        composable(
            route = Screen.EmployeeTaskDetail.route,
            arguments = Screen.EmployeeTaskDetail.arguments
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            EmployeeTaskDetailScreen(
                taskId = taskId,
                taskViewModel = taskViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.HRProfile.route) {
            HRProfileScreen(
                authViewModel = authViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.HRDashboard.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.EmployeeManagement.route) {
            EmployeeManagementScreen(
                companyViewModel = companyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EmployeeDashboard.route) {
            EmployeeDashboardScreen(
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.EmployeeDashboard.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.EmployeeProfile.route)
                },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.EmployeeTaskDetail.createRoute(taskId))
                }
            )
        }

        composable(Screen.EmployeeProfile.route) {
            EmployeeProfileScreen(
                authViewModel = authViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToCompanyManagement = {
                    navController.navigate(Screen.CompanyManagement.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.EmployeeDashboard.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CompanyManagement.route) {
            EmployeeCompanyManagementScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object Register : Screen("register")
    object Login : Screen("login")
    object HRDashboard : Screen("hr_dashboard")
    object HRProfile : Screen("hr_profile")
    object EmployeeManagement : Screen("employee_management")
    object EmployeeDashboard : Screen("employee_dashboard")
    object EmployeeProfile : Screen("employee_profile")
    object EditProfile : Screen("edit_profile")
    object CompanyManagement : Screen("company_management")

    // Task Management Routes
    object HRTaskManagement : Screen("hr_task_management")
    object CreateTask : Screen("create_task")

    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
        val arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    }

    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
        val arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    }

    object EmployeeTaskDetail : Screen("employee_task_detail/{taskId}") {
        fun createRoute(taskId: String) = "employee_task_detail/$taskId"
        val arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    }
}