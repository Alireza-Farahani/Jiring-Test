package me.farahani.jiringtest

import kotlinx.serialization.json.Json
import me.farahani.jiringtest.domain.DefaultLoginSession
import me.farahani.jiringtest.domain.NetworkUserSerializer
import me.farahani.jiringtest.domain.Users
import me.farahani.jiringtest.localdata.AndroidSharedPrefStorage
import me.farahani.jiringtest.network.NetworkTodo
import me.farahani.jiringtest.network.NetworkTodoDto
import me.farahani.jiringtest.network.NetworkUser
import me.farahani.jiringtest.network.NetworkUserDto
import me.farahani.jiringtest.network.NetworkUsers
import me.farahani.jiringtest.network.TodoService
import me.farahani.jiringtest.network.UserService
import me.farahani.jiringtest.network.UsersService
import me.farahani.jiringtest.network.createRetrofit
import retrofit2.create

class ServiceLocator(app: JiringApplication) {
  val json = Json { ignoreUnknownKeys = true }
  private val retrofit = createRetrofit(NetworkParamsImpl, json)
  private val userApi: UserService by lazy { retrofit.create() }
  val usersApi: UsersService by lazy { retrofit.create() }
  private val todoApi: TodoService by lazy { retrofit.create() }

  private val todoFactory: (NetworkTodoDto) -> NetworkTodo = { dto -> NetworkTodo(dto, todoApi) }
  val userFactory: (NetworkUserDto) -> NetworkUser =
    { dto -> NetworkUser(dto, userApi, todoFactory) }
  val users: Users by lazy { NetworkUsers(usersApi, userFactory, json) }
  val loginSession = DefaultLoginSession(
    AndroidSharedPrefStorage(app, "session"),
    NetworkUserSerializer(json, userFactory)
  )
}