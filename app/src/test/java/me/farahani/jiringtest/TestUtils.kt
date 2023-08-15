package me.farahani.jiringtest

import kotlinx.coroutines.yield
import me.farahani.jiringtest.domain.Email
import me.farahani.jiringtest.domain.Todo
import me.farahani.jiringtest.domain.User
import me.farahani.jiringtest.domain.Users
import me.farahani.jiringtest.network.NetworkEmail
import me.farahani.jiringtest.network.NetworkError

class StubUsers : Users {
  var loginResponse: Result<User> = Result.failure(NetworkError())
  override suspend fun login(username: String): Result<User> {
    yield() // otherwise no intermediate state is dispatched
    return loginResponse
  }
}

data class StubUser(
  override val id: Int,
  override val name: String,
  override val username: String,
  override val email: Email,
) : User {
  var todoResponse: Result<List<Todo>> = Result.failure(NetworkError())
  override suspend fun todoList(): Result<List<Todo>> {
    yield()
    return todoResponse
  }
}

val testUser = StubUser(1, "Ali", "Gandalf", NetworkEmail("a@b.com"))