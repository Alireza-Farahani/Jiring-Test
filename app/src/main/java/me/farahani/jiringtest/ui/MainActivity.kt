package me.farahani.jiringtest.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.farahani.jiringtest.JiringApplication
import me.farahani.jiringtest.ui.theme.JiringTestTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      JiringTestTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val navController = rememberNavController()
          val app = LocalContext.current.applicationContext as JiringApplication
          val currentUser = app.serviceLocator.loginSession.currentUser
          val startDestination = if (currentUser == null) "login" else "todo_list"

          NavHost(navController, startDestination = startDestination) {
            composable(route = "login") {
              val vm = viewModel<LoginViewModel>(factory = LoginViewModel.Factory)
              LoginScreen(vm,
                navigateToTodoListScreen = {
                  navController.navigate("todo_list") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                  }
                }
              )
            }
            composable(route = "todo_list") {
              val vm = viewModel<TodoListViewModel>(factory = TodoListViewModel.Factory)
              TodoListScreen(vm,
                navigateToLoginScreen = {
                  navController.navigate("login") {
                    popUpTo("todo_list") { inclusive = true }
                    launchSingleTop = true
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}