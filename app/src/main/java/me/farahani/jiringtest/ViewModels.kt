package me.farahani.jiringtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginScreenState {
  data object Loading : LoginScreenState
  data object IDLE : LoginScreenState
  data class Error(val message: UIString) : LoginScreenState
}

class LoginViewModel(private val users: Users, private val session: Session) : ViewModel() {
  private val _uiState: MutableStateFlow<LoginScreenState> = MutableStateFlow(LoginScreenState.IDLE)
  val uiState = _uiState.asStateFlow()
  private val _navigateToTodos = MutableSharedFlow<Unit>()
  val navigateToTodos = _navigateToTodos.asSharedFlow()
  private val _errorEvent = MutableSharedFlow<Throwable>()
  val errorEvent = _errorEvent.asSharedFlow()

  fun login(username: String) {
    viewModelScope.launch {
      _uiState.value = LoginScreenState.Loading
      users.login(username)
        .onSuccess {
          session.currentUser = it
          _uiState.value = LoginScreenState.IDLE
          _navigateToTodos.emit(Unit)
        }
        .onFailure {
          session.currentUser = null
          when (it) {
            is InvalidUsernameException -> {
              _uiState.value = LoginScreenState.Error(
                message = UIString.ResourceIdString(R.string.error_invalid_username)
              )
            }

            else -> {
              _uiState.value = LoginScreenState.IDLE
              _errorEvent.emit(it)
            }
          }
        }
    }
  }
}

sealed interface TodoListScreenState {
  data object Loading : TodoListScreenState
  data object IDLE : TodoListScreenState
  data class Success(val todoList: List<Todo>) : TodoListScreenState
}

class TodoListViewModel(private val session: Session) : ViewModel() {
  private val _uiState: MutableStateFlow<TodoListScreenState> =
    MutableStateFlow(TodoListScreenState.IDLE)
  val uiState = _uiState.asStateFlow()
  private val _logoutEvent = MutableSharedFlow<Unit>()
  val logoutEvent = _logoutEvent.asSharedFlow()
  private val _errorEvent = MutableSharedFlow<Throwable>()
  val errorEvent = _errorEvent.asSharedFlow()

  private val user get() = requireNotNull(session.currentUser) {
    "Session's user is null. Doing something after logout?"
  }

  fun userTodoList() {
    viewModelScope.launch {
      _uiState.value = TodoListScreenState.Loading
      user.todoList()
        .onSuccess {
          _uiState.value = TodoListScreenState.Success(it)
        }
        .onFailure {
          _uiState.value = TodoListScreenState.IDLE
          _errorEvent.emit(it)
        }
    }
  }

  fun logout() {
    session.logout()
    viewModelScope.launch {
      _logoutEvent.emit(Unit)
    }
  }
}