package me.farahani.jiringtest.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.farahani.jiringtest.JiringApplication
import me.farahani.jiringtest.R
import me.farahani.jiringtest.domain.InvalidUsernameException
import me.farahani.jiringtest.domain.LoginSession
import me.farahani.jiringtest.domain.Todo
import me.farahani.jiringtest.domain.Users


sealed interface LoginScreenState {
  data object Loading : LoginScreenState
  data object Idle : LoginScreenState
  data class Error(val message: UIString) : LoginScreenState
}

class LoginViewModel(private val users: Users, private val loginSession: LoginSession) :
  ViewModel() {

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
//        val savedStateHandle = createSavedStateHandle()
        val app =
          (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as JiringApplication)
        val serviceLocator = app.serviceLocator
        LoginViewModel(serviceLocator.users, serviceLocator.loginSession)
      }
    }
  }

  private val _uiState: MutableStateFlow<LoginScreenState> = MutableStateFlow(LoginScreenState.Idle)
  val uiState = _uiState.asStateFlow()
  private val _navigateToTodos = MutableSharedFlow<Unit>()
  val navigateToTodos = _navigateToTodos.asSharedFlow()
  private val _errorEvent = MutableSharedFlow<Throwable>()
  val errorEvent = _errorEvent.asSharedFlow()
  var username by mutableStateOf("")
    private set

  fun updateUsername(newValue: String) {
    username = newValue
  }

  fun login(username: String) {
    viewModelScope.launch {
      _uiState.value = LoginScreenState.Loading
      users.login(username)
        .onSuccess {
          loginSession.begin(it)
          _uiState.value = LoginScreenState.Idle
          _navigateToTodos.emit(Unit)
        }
        .onFailure {
          when (it) {
            is InvalidUsernameException -> {
              _uiState.value = LoginScreenState.Error(
                message = UIString.IdString(R.string.error_invalid_username)
              )
            }

            else -> {
              _uiState.value = LoginScreenState.Idle
              _errorEvent.emit(it)
            }
          }
        }
    }
  }
}


data class TodoListScreenState(
  val name: String,
  val listState: TodoListState,
)

sealed interface TodoListState {
  data object Loading : TodoListState
  data class Ready(val todoList: List<Todo>) : TodoListState
}

class TodoListViewModel(private val loginSession: LoginSession) : ViewModel() {

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
//        val savedStateHandle = createSavedStateHandle()
        val app =
          (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as JiringApplication)
        val serviceLocator = app.serviceLocator
        TodoListViewModel(serviceLocator.loginSession)
      }
    }
  }

  private var currentTodoList = mutableListOf<Todo>()

  private val initialState =
    TodoListScreenState(loginSession.currentUser?.name.orEmpty(), TodoListState.Ready(emptyList()))
  private val _uiState: MutableStateFlow<TodoListScreenState> = MutableStateFlow(initialState)
  val uiState = _uiState.asStateFlow()

  private val _logoutEvent = MutableSharedFlow<Unit>()
  val logoutEvent = _logoutEvent.asSharedFlow()

  private val _errorEvent = MutableSharedFlow<Throwable>()
  val errorEvent = _errorEvent.asSharedFlow()

  private val user
    get() = requireNotNull(loginSession.currentUser) {
      "Session's user is null. Doing something after logout?"
    }

  init {
    userTodoList()
  }

  fun userTodoList() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(listState = TodoListState.Loading)
      user.todoList()
        .onSuccess {
          currentTodoList = it.toMutableList()
          _uiState.value = _uiState.value.copy(listState = TodoListState.Ready(it))
        }
        .onFailure {
          _uiState.value = _uiState.value.copy(listState = TodoListState.Ready(emptyList()))
          _errorEvent.emit(it)
        }
    }
  }

  fun logout() {
    loginSession.end()
    viewModelScope.launch {
      _logoutEvent.emit(Unit)
    }
  }

  fun onTodoCompleteChanged(todo: Todo, isComplete: Boolean) {
    val currentListState = uiState.value.listState
    require(currentListState is TodoListState.Ready)

    viewModelScope.launch {
      todo.update { this.isComplete = isComplete }
        .onSuccess {
          val idx = currentTodoList.indexOfFirst { elem -> elem.id == todo.id }
          currentTodoList[idx] = it
          _uiState.value =
            _uiState.value.copy(listState = TodoListState.Ready(currentTodoList.toList()))
        }
        .onFailure {
          _errorEvent.emit(it)
        }
    }
  }
}