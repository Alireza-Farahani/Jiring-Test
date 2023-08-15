package me.farahani.jiringtest

import kotlinx.serialization.json.Json
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