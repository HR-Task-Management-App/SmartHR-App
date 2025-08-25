package com.example.smarthr_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.repository.AttendanceRepository
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.data.repository.ChatRepository
import com.example.smarthr_app.data.repository.CompanyRepository
import com.example.smarthr_app.data.repository.LeaveRepository
import com.example.smarthr_app.data.repository.MeetingRepository
import com.example.smarthr_app.data.repository.TaskRepository
import com.example.smarthr_app.presentation.screen.auth.LoginScreen
import com.example.smarthr_app.presentation.screen.auth.RegisterScreen
import com.example.smarthr_app.presentation.screen.auth.RoleSelectionScreen
import com.example.smarthr_app.presentation.screen.chat.AllUserListScreen
import com.example.smarthr_app.presentation.screen.chat.ChatListScreen
import com.example.smarthr_app.presentation.screen.chat.ChatScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeAttendanceScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeCompanyManagementScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeDashboardScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeLeaveScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeMeetingScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeProfileScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeTaskDetailScreen
import com.example.smarthr_app.presentation.screen.dashboard.employee.EmployeeTaskScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.CreateMeetingScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.CreateTaskScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.EmployeeManagementScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRCompanyAttendanceScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRDashboardScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRLeaveManagementScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRMeetingManagementScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HROfficeLocationScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRProfileScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.HRTaskManagementScreen
import com.example.smarthr_app.presentation.screen.dashboard.hr.TaskDetailScreen
import com.example.smarthr_app.presentation.screen.profile.EditProfileScreen
import com.example.smarthr_app.presentation.viewmodel.AttendanceViewModel
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.presentation.viewmodel.CompanyViewModel
import com.example.smarthr_app.presentation.viewmodel.LeaveViewModel
import com.example.smarthr_app.presentation.viewmodel.MeetingViewModel
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
    val chatRepository = ChatRepository(dataStoreManager)

    val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
    val companyViewModel: CompanyViewModel = viewModel { CompanyViewModel(companyRepository) }
    val taskViewModel: TaskViewModel = viewModel { TaskViewModel(taskRepository) }

    val leaveRepository = LeaveRepository(dataStoreManager)
    val leaveViewModel: LeaveViewModel = viewModel { LeaveViewModel(leaveRepository) }

    val attendanceRepository = AttendanceRepository(dataStoreManager)
    val attendanceViewModel: AttendanceViewModel = viewModel { AttendanceViewModel(attendanceRepository) }

    val chatViewModel: ChatViewModel = viewModel { ChatViewModel(chatRepository) }
    val meetingRepository = MeetingRepository(dataStoreManager)
    val meetingViewModel: MeetingViewModel = viewModel { MeetingViewModel(meetingRepository) }

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
                chatViewModel = chatViewModel,
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
                },
                onNavigateToLeaves = {
                    navController.navigate(Screen.HRLeaveManagement.route)
                },
                onNavigateToOfficeLocation = {
                    navController.navigate(Screen.HROfficeLocation.route)
                },
                onNavigateToCompanyAttendance = {
                    navController.navigate(Screen.HRCompanyAttendance.route)
                },
                onNavigateToChatList = {
                    navController.navigate(Screen.ChatList.route)
                },
                onNavigateToMeetings = {
                    navController.navigate(Screen.HRMeetingManagement.route)
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

        // Employee Dashboard with updated screens - KEEP BOTTOM NAV VISIBLE
        composable(Screen.EmployeeDashboard.route) {
            EmployeeDashboardScreen(
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                leaveViewModel = leaveViewModel,
                attendanceViewModel = attendanceViewModel,
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
                },
                onNavigateToChatList = {
                    navController.navigate(Screen.ChatList.route)
                },
                onNavigateToMeetings = {
                    navController.navigate(Screen.EmployeeMeeting.route)
                },
                onNavigateToCompanyManagement = {
                    navController.navigate(Screen.CompanyManagement.route)
                }
            )
        }

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

        // Employee Meeting Route with AuthViewModel
        composable(Screen.EmployeeMeeting.route) {
            EmployeeMeetingScreen(
                meetingViewModel = meetingViewModel,
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCompanyManagement = {
                    navController.navigate(Screen.CompanyManagement.route)
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

        composable(Screen.HRLeaveManagement.route) {
            HRLeaveManagementScreen(
                leaveViewModel = leaveViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.HROfficeLocation.route) {
            HROfficeLocationScreen(
                attendanceViewModel = attendanceViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.HRCompanyAttendance.route) {
            HRCompanyAttendanceScreen(
                attendanceViewModel = attendanceViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Chat Routes with company verification
        composable(Screen.ChatList.route) {
            ChatListScreen(
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                onNavigateToUserListScreen = {
                    navController.navigate(Screen.AllUserListScreen.route)
                },
                onNavigateChatScreen = { otherUserId, imageUrl, name ->
                    navController.navigate("${Screen.ChatScreen.route}/$otherUserId/$imageUrl/$name")
                },
                goToBack = {
                    navController.popBackStack()
                },
                onNavigateToCompanyManagement = {
                    navController.navigate(Screen.CompanyManagement.route)
                }
            )
        }

        composable(Screen.AllUserListScreen.route) {
            AllUserListScreen(
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                goToBack = {
                    navController.popBackStack()
                },
                onNavigateToChatScreen = { otherUserId, imageUrl, name ->
                    navController.navigate("${Screen.ChatScreen.route}/$otherUserId/$imageUrl/$name")
                }
            )
        }

        composable(
            route = "${Screen.ChatScreen.route}/{otherUserId}/{imageUrl}/{name}",
            arguments = listOf(
                navArgument("otherUserId") { type = NavType.StringType },
                navArgument("imageUrl") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            ChatScreen(
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                receiverId = otherUserId,
                imageUrl = imageUrl,
                name = name,
                goToBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.HRMeetingManagement.route) {
            HRMeetingManagementScreen(
                meetingViewModel = meetingViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreateMeeting = {
                    navController.navigate(Screen.CreateMeeting.route)
                },
                onNavigateToEditMeeting = { meetingId ->
                    navController.navigate(Screen.EditMeeting.createRoute(meetingId))
                }
            )
        }

        composable(Screen.CreateMeeting.route) {
            CreateMeetingScreen(
                meetingViewModel = meetingViewModel,
                companyViewModel = companyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditMeeting.route,
            arguments = Screen.EditMeeting.arguments
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: ""
            CreateMeetingScreen(
                meetingViewModel = meetingViewModel,
                companyViewModel = companyViewModel,
                meetingId = meetingId,
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
    object ChatList : Screen("chat_list")
    object AllUserListScreen : Screen("user_list")
    object ChatScreen : Screen("chat_screen")

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

    object HRLeaveManagement : Screen("hr_leave_management")

    object HROfficeLocation : Screen("hr_office_location")
    object HRCompanyAttendance : Screen("hr_company_attendance")

    // Meeting Management Routes
    object HRMeetingManagement : Screen("hr_meeting_management")
    object CreateMeeting : Screen("create_meeting")
    object EditMeeting : Screen("edit_meeting/{meetingId}") {
        fun createRoute(meetingId: String) = "edit_meeting/$meetingId"
        val arguments = listOf(
            navArgument("meetingId") { type = NavType.StringType }
        )
    }
    object EmployeeMeeting : Screen("employee_meeting")
}