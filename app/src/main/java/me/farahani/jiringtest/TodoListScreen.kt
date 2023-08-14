package me.farahani.jiringtest

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.farahani.jiringtest.ui.theme.JiringTestTheme

@Composable
fun TodoListScreen(
  vm: TodoListViewModel,
  navigateToLoginScreen: () -> Unit,
) {
  val state by vm.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    vm.errorEvent.collect {
      Toast.makeText(context, it::class.simpleName, Toast.LENGTH_SHORT).show()
    }
  }
  LaunchedEffect(Unit) {
    vm.logoutEvent.collect {
      navigateToLoginScreen()
    }
  }
  TodoListScreen(
    uiState = state,
    refreshList = vm::userTodoList,
    logout = vm::logout,
    onTodoCompletedChanged = vm::onTodoCompleteChanged
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
  uiState: TodoListScreenState,
  refreshList: () -> Unit,
  logout: () -> Unit,
  onTodoCompletedChanged: (Todo, Boolean) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        title = {
          Text(
            "${uiState.name}'s todo list",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        actions = {
          IconButton(onClick = refreshList) {
            Icon(
              Icons.Filled.Refresh,
              contentDescription = stringResource(id = R.string.menu_refresh)
            )
          }
          IconButton(onClick = logout) {
            Icon(
              Icons.Filled.ExitToApp,
              contentDescription = stringResource(id = R.string.menu_logout)
            )
          }
        },
        scrollBehavior = scrollBehavior
      )
    }
  ) { contentPadding ->
    when (uiState.listState) {
      is TodoListState.Ready -> {
        if (uiState.listState.todoList.isEmpty()) {
          Box(
            modifier = Modifier
              .padding(contentPadding)
              .fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              stringResource(id = R.string.todolist_empty),
              style = MaterialTheme.typography.headlineSmall
            )
          }
        } else {
          LazyColumn(
            contentPadding = contentPadding,//PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            items(uiState.listState.todoList, { todo -> todo.id }) { todo ->
              Todo(todo, onTodoCompletedChanged, modifier = Modifier.fillMaxWidth())
            }
          }
        }
      }

      is TodoListState.Loading -> {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.padding(top = 16.dp)) {
          CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Todo(
  todo: Todo,
  onTodoCompletedChanged: (Todo, Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(modifier) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        todo.title,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleMedium
      )
      Spacer(modifier = Modifier.size(16.dp))
      // remove default touch padding
      CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Checkbox(

          checked = todo.isCompleted,
          onCheckedChange = { onTodoCompletedChanged(todo, it) }
        )
      }
    }
  }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewEmptyTodoList() {
  JiringTestTheme {
    // A surface container using the 'background' color from the theme
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      TodoListScreen(
        uiState = TodoListScreenState("Bret", TodoListState.Ready(emptyList())),
        refreshList = {},
        logout = {},
        onTodoCompletedChanged = { _, _ -> }
      )
    }
  }
}

@Preview
@Composable
fun PreviewNonEmptyTodoList() {
  JiringTestTheme {
    // A surface container using the 'background' color from the theme
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      TodoListScreen(
        uiState = TodoListScreenState(
          "Bret", TodoListState.Ready(
            listOf(
              StubTodo(1, 1, "Unfinished Business", false),
              StubTodo(1, 2, "Establish World Peace", false),
              StubTodo(1, 3, "Make The Forests Burn", true),
              StubTodo(1, 4, "Stop being a perfectionist and send the Jiring test task", true),
              StubTodo(
                1,
                5,
                "Many of life's failures are people who did not realize how close they were to success when they gave up. -Thomas A. Edison",
                false
              ),
            )
          )
        ),
        refreshList = {},
        logout = {},
        onTodoCompletedChanged = { _, _ -> }
      )
    }
  }
}

data class StubTodo(
  override val userId: Int,
  override val id: Int,
  override val title: String,
  override val isCompleted: Boolean,
) : Todo {
  override suspend fun update(changes: Todo.UpdateParams.() -> Unit): Result<Todo> {
    throw NotImplementedError()
  }
}