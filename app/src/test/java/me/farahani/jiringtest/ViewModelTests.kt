package me.farahani.jiringtest

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.farahani.jiringtest.domain.InvalidUsernameException
import me.farahani.jiringtest.domain.LoginSession
import me.farahani.jiringtest.network.NetworkError
import me.farahani.jiringtest.network.ServerError
import me.farahani.jiringtest.ui.LoginScreenState
import me.farahani.jiringtest.ui.LoginViewModel
import me.farahani.jiringtest.ui.TodoListState
import me.farahani.jiringtest.ui.TodoListViewModel
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
  private lateinit var mockSession: LoginSession

  @Before
  fun setup() {
    stubUsers = StubUsers()
    mockSession = mockk(relaxUnitFun = true)
  }

  @Test
  fun `initial state is IDLE`() = runTest {
    val vm = LoginViewModel(stubUsers, mockk())
    assertEquals(LoginScreenState.Idle, vm.uiState.value)
  }

  @Test
  fun `state is LOADING until response is arrived`() = runTest {
    val vm = LoginViewModel(stubUsers, mockSession)
    vm.uiState.test {
      vm.login("Ali Alavi")
      skipItems(1) // IDLE state
      assertEquals(LoginScreenState.Loading, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `navigation event dispatched on successful login`() = runTest {
    val vm = LoginViewModel(stubUsers, mockSession)
    stubUsers.loginResponse = Result.success(testUser)
    vm.navigateToTodos.test {
      vm.login("Ali Alavi")
      assertEquals(Unit, awaitItem())
    }
    assertEquals(LoginScreenState.Idle, vm.uiState.value)
  }

  @Test
  fun `session is began on successful login`() = runTest {
    stubUsers.loginResponse = Result.success(testUser)
    val vm = LoginViewModel(stubUsers, mockSession)
    vm.login("Whatever")
    advanceUntilIdle()
    verify { mockSession.begin(testUser) }
  }

  @Test
  fun `state becomes ERROR on invalid username`() = runTest {
    val vm = LoginViewModel(stubUsers, mockSession)
    stubUsers.loginResponse = Result.failure(InvalidUsernameException())
    vm.login("Invalid username")
    assertIs<LoginScreenState.Error>(vm.uiState.value)
  }

  @Test
  fun `state become IDLE with error event on server errors`() = runTest {
    val vm = LoginViewModel(stubUsers, mockSession)
    stubUsers.loginResponse = Result.failure(ServerError(500))
    vm.errorEvent.test {
      vm.login("Whatever")
      val e = awaitItem()
      assertIs<ServerError>(e)
      assertEquals(500, e.code)
    }
    assertEquals(LoginScreenState.Idle, vm.uiState.value)
  }

  @Test
  fun `state become IDLE with error event on connection errors`() = runTest {
    val vm = LoginViewModel(stubUsers, mockSession)
    stubUsers.loginResponse = Result.failure(NetworkError())
    vm.errorEvent.test {
      vm.login("Whatever")
      assertIs<NetworkError>(awaitItem())
    }
    assertEquals(LoginScreenState.Idle, vm.uiState.value)
  }

  @Test
  fun `no navigation event is dispatched on invalid login`() = runTest {
    val vm = LoginViewModel(stubUsers, mockSession)
    stubUsers.loginResponse = Result.failure(InvalidUsernameException())
    vm.navigateToTodos.test {
      vm.login("Invalid username")
      expectNoEvents()
    }
  }

  @Test
  fun `no navigation event is dispatched on server errors`() = runTest {
    val vm = LoginViewModel(stubUsers, mockSession)
    stubUsers.loginResponse = Result.failure(ServerError(500))
    vm.navigateToTodos.test {
      vm.login("Whatever")
      expectNoEvents()
    }
  }

  @Test
  fun `no no navigation event is dispatched on connection errors`() = runTest {
    val vm = LoginViewModel(stubUsers, mockSession)
    stubUsers.loginResponse = Result.failure(NetworkError())
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
  private lateinit var mockSession: LoginSession

  @Before
  fun setup() {
    stubUser = testUser
    mockSession = mockk(relaxUnitFun = true)
    every { mockSession.currentUser } returns stubUser
  }

  @Test
  fun `initial state is empty Ready`() = runTest {
    val vm = TodoListViewModel(mockSession)
    assertEquals(TodoListState.Ready(emptyList()), vm.uiState.value.listState)
  }

  @Test
  fun `logout event is dispatched on logout`() = runTest {
    val vm = TodoListViewModel(mockSession)
    vm.logoutEvent.test {
      vm.logout()
      assertEquals(Unit, awaitItem())
    }
  }

  @Test
  fun `session is ended on logout`() = runTest {
    val vm = TodoListViewModel(mockSession)
    vm.logout()
    verify { mockSession.end() }
  }

  @Test
  fun `state is LOADING until network response arrives`() = runTest {
    val vm = TodoListViewModel(mockSession)
    vm.uiState.test {
      vm.userTodoList()
      skipItems(1) // IDLE state
      assertEquals(TodoListState.Loading, awaitItem().listState)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `state becomes Ready on receiving correct response`() = runTest {
    stubUser.todoResponse = Result.success(emptyList())
    val vm = TodoListViewModel(mockSession)
    vm.userTodoList()
    advanceUntilIdle()

    val listState = vm.uiState.value.listState
    assertIs<TodoListState.Ready>(listState)
    assertEquals(emptyList(), listState.todoList)
  }

  @Test
  fun `state becomes empty Ready with error event on network error`() = runTest {
    stubUser.todoResponse = Result.failure(NetworkError())
    val vm = TodoListViewModel(mockSession)
    vm.errorEvent.test {
      vm.userTodoList()
      assertIs<NetworkError>(awaitItem())
    }
    assertEquals(TodoListState.Ready(emptyList()), vm.uiState.value.listState)
  }

  @Test
  fun `state becomes empty Ready with error event on server error`() = runTest {
    stubUser.todoResponse = Result.failure(ServerError(500))
    val vm = TodoListViewModel(mockSession)
    vm.errorEvent.test {
      vm.userTodoList()
      assertIs<ServerError>(awaitItem())
    }
    assertEquals(TodoListState.Ready(emptyList()), vm.uiState.value.listState)
  }
}


