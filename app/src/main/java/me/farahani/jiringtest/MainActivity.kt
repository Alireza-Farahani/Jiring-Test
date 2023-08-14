package me.farahani.jiringtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.farahani.jiringtest.ui.theme.JiringTestTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      JiringTestTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val navController = rememberNavController()
          NavHost(navController, startDestination = "login") {
            composable(route = "login") {
              val vm = viewModel<LoginViewModel>(factory = LoginViewModel.Factory)
              LoginScreen(vm,
                navigateToTodoListScreen = { navController.navigate("todo_list") }
              )
            }
            composable(route = "todo_list") {
              val vm = viewModel<TodoListViewModel>(factory = TodoListViewModel.Factory)
              TodoListScreen(vm,
                navigateToLoginScreen = { navController.navigate("login") }
              )
            }
          }
        }
      }
    }
  }
}