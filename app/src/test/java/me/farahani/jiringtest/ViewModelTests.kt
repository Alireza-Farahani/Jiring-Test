package me.farahani.jiringtest

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

  @JvmField
  @Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var stubUsers: StubUsers

  @Before
  fun setup() {
    stubUsers = StubUsers()
  }

  @Test
  fun `initial state is IDLE`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    assertEquals(LoginScreenState.IDLE, vm.uiState.value)
  }

  @Test
  fun `state is LOADING until response is arrived`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    vm.uiState.test {
      vm.login("Ali Alavi")
      skipItems(1) // IDLE state
      assertEquals(LoginScreenState.Loading, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `navigation event dispatched on successful login`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    stubUsers.response = Result.success(testUser)
    vm.navigateToTodos.test {
      vm.login("Ali Alavi")
      assertEquals(Unit, awaitItem())
    }
    assertEquals(LoginScreenState.IDLE, vm.uiState.value)
  }

  @Test
  fun `session is filled on successful login`() = runTest {
    val session = Session()
    stubUsers.response = Result.success(testUser)
    val vm = LoginViewModel(stubUsers, session)
    vm.login("Whatever")
    advanceUntilIdle()
    assertEquals(testUser, session.currentUser)
  }

  @Test
  fun `state becomes ERROR on invalid username`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    stubUsers.response = Result.failure(InvalidUsernameException())
    vm.login("Invalid username")
    assertIs<LoginScreenState.Error>(vm.uiState.value)
  }

  @Test
  fun `state become IDLE with error event on server errors`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    stubUsers.response = Result.failure(ServerError(500))
    vm.errorEvent.test {
      vm.login("Whatever")
      val e = awaitItem()
      assertIs<ServerError>(e)
      assertEquals(500, e.code)
    }
    assertEquals(LoginScreenState.IDLE, vm.uiState.value)
  }

  @Test
  fun `state become IDLE with error event on connection errors`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    stubUsers.response = Result.failure(NetworkError())
    vm.errorEvent.test {
      vm.login("Whatever")
      assertIs<NetworkError>(awaitItem())
    }
    assertEquals(LoginScreenState.IDLE, vm.uiState.value)
  }

  @Test
  fun `no navigation event is dispatched on invalid login`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    stubUsers.response = Result.failure(InvalidUsernameException())
    vm.navigateToTodos.test {
      vm.login("Invalid username")
      expectNoEvents()
    }
  }

  @Test
  fun `no navigation event is dispatched on server errors`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    stubUsers.response = Result.failure(ServerError(500))
    vm.navigateToTodos.test {
      vm.login("Whatever")
      expectNoEvents()
    }
  }

  @Test
  fun `no no navigation event is dispatched on connection errors`() = runTest {
    val vm = LoginViewModel(stubUsers, Session())
    stubUsers.response = Result.failure(NetworkError())
    vm.navigateToTodos.test {
      vm.login("Whatever")
      expectNoEvents()
    }
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {
  @JvmField
  @Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var stubUser: StubUser

  @Before
  fun setup() {
    stubUser = testUser
  }

  @Test
  fun `initial state is IDLE`() = runTest {
    val vm = TodoListViewModel(Session(stubUser))
    assertEquals(TodoListScreenState.IDLE, vm.uiState.value)
  }

  @Test
  fun `logout event is dispatched on logout`() = runTest {
    val vm = TodoListViewModel(Session(stubUser))
    vm.logoutEvent.test {
      vm.logout()
      assertEquals(Unit, awaitItem())
    }
  }

  @Test
  fun `state is LOADING until network response arrives`() = runTest {
    val vm = TodoListViewModel(Session(stubUser))
    vm.uiState.test {
      vm.userTodoList()
      skipItems(1) // IDLE state
      assertEquals(TodoListScreenState.Loading, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `state becomes SUCCESSFUL on receiving correct response`() = runTest {
    stubUser.todoResponse = Result.success(emptyList())
    val vm = TodoListViewModel(Session(stubUser))
    vm.userTodoList()
    advanceUntilIdle()

    val state = vm.uiState.value
    assertIs<TodoListScreenState.Success>(state)
    assertEquals(emptyList(), state.todoList)
  }

  @Test
  fun `state becomes IDLE with error event on network error`() = runTest {
    stubUser.todoResponse = Result.failure(NetworkError())
    val vm = TodoListViewModel(Session(stubUser))
    vm.errorEvent.test {
      vm.userTodoList()
      assertIs<NetworkError>(awaitItem())
    }
    assertEquals(TodoListScreenState.IDLE, vm.uiState.value)
  }

  @Test
  fun `state becomes IDLE with error event on server error`() = runTest {
    stubUser.todoResponse = Result.failure(ServerError(500))
    val vm = TodoListViewModel(Session(stubUser))
    vm.errorEvent.test {
      vm.userTodoList()
      assertIs<ServerError>(awaitItem())
    }
    assertEquals(TodoListScreenState.IDLE, vm.uiState.value)
  }
}


