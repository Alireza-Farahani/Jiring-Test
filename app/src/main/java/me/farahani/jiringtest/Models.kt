package me.farahani.jiringtest

class Session(var currentUser: User? = null) {
  fun logout() {
    currentUser = null
  }
}

interface Email {
  val value: String
}

interface User {
  val id: Int
  val name: String
  val username: String
  val email: Email

  suspend fun todoList(): Result<List<Todo>>
}

interface Users {
  suspend fun login(username: String): Result<User>
}

interface Todo {
  val userId: Int
  val id: Int
  val title: String
  val isCompleted: Boolean

  data class UpdateParams(var title: String, var isComplete: Boolean)

  suspend fun update(changes: UpdateParams.() -> Unit): Result<Todo>
}