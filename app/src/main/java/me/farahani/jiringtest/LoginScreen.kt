package me.farahani.jiringtest

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.farahani.jiringtest.ui.theme.JiringTestTheme

@Composable
fun LoginScreen(
  vm: LoginViewModel,
  navigateToTodoListScreen: () -> Unit,
) {
  val state by vm.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    vm.errorEvent.collect {
      Toast.makeText(context, it::class.simpleName, Toast.LENGTH_SHORT).show()
    }
  }
  LaunchedEffect(Unit) {
    vm.navigateToTodos.collect {
      navigateToTodoListScreen()
    }
  }
  LoginScreen(
    username = vm.username,
    uiState = state,
    onUsernameChanged = vm::updateUsername,
    onLogin = vm::login
  )
}

@Composable
fun LoginScreen(
  username: String,
  uiState: LoginScreenState,
  onUsernameChanged: (String) -> Unit,
  onLogin: (String) -> Unit,
) {
  Column(
    modifier = Modifier
      .widthIn(min = 300.dp, max = 480.dp)
      .padding(48.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val isError = uiState is LoginScreenState.Error
    val isLoading = uiState is LoginScreenState.Loading
    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = username,
      enabled = !isLoading,
      placeholder = { Text(stringResource(id = R.string.hint_username)) },
      onValueChange = onUsernameChanged,
      isError = isError,
      supportingText = {
        if (isError) {
          Text(
            text = (uiState as LoginScreenState.Error).message.asString(),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error
          )
        }
      },
      trailingIcon = {
        if (isError)
          Icon(Icons.Filled.Warning, "error", tint = MaterialTheme.colorScheme.error)
      },
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (isLoading) {
      CircularProgressIndicator()
    } else {
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onLogin(username) },
        enabled = username.isNotBlank()
      ) {
        Text(stringResource(id = R.string.button_login))
      }
    }
  }
}

@Composable
@Preview(name = "Login Screen")
fun PreviewLoginScreenEmptyForm() {
  JiringTestTheme {
    // A surface container using the 'background' color from the theme
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      LoginScreen(
        username = "",
        uiState = LoginScreenState.IDLE,
        onUsernameChanged = {},
        onLogin = {})
    }
  }
}

@Composable
@Preview(name = "Login Screen")
fun PreviewLoginScreenFilledForm() {
  JiringTestTheme {
    // A surface container using the 'background' color from the theme
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      LoginScreen(
        username = "AlirezaF",
        uiState = LoginScreenState.IDLE,
        onUsernameChanged = {},
        onLogin = {})
    }
  }
}

@Composable
@Preview(name = "Login Screen", uiMode = UI_MODE_NIGHT_YES)
fun PreviewLoginScreenErrorForm() {
  JiringTestTheme {
    // A surface container using the 'background' color from the theme
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      LoginScreen(
        username = "AlirezaF",
        uiState = LoginScreenState.Error(UIString.IdString(R.string.error_invalid_username)),
        onUsernameChanged = {},
        onLogin = {}
      )
    }
  }
}

@Composable
@Preview(name = "Login Screen")
fun PreviewLoginScreenLoadingForm() {
  JiringTestTheme {
    // A surface container using the 'background' color from the theme
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      LoginScreen(
        username = "AlirezaF",
        uiState = LoginScreenState.Loading,
        onUsernameChanged = {},
        onLogin = {}
      )
    }
  }
}
