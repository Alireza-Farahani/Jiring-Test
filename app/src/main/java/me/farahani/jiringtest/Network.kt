package me.farahani.jiringtest

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

interface NetworkParams {
  val baseUrl: String
  val interceptors: Set<Interceptor>
}

interface UserService {
  @GET("/todos")
  suspend fun userTodos(@Query("userId") userId: Int): Response<List<NetworkTodoDto>>
}

interface UsersService {
  @GET("/users")
  suspend fun loginUser(@Query("username") username: String): Response<ResponseBody>
}

interface TodoService {
  @PUT("/todos/{id}")
  suspend fun updateTodo(@Path("id") id: Int, @Body todo: NetworkTodoDto): Response<NetworkTodoDto>
}

@Serializable
data class NetworkUserDto(
  @SerialName("id")
  val id: Int,
  @SerialName("name")
  val name: String,
  @SerialName("username")
  val username: String,
  @SerialName("email")
  val email: NetworkEmail,
)

class NetworkUser(
  private val dto: NetworkUserDto,
  private val api: UserService,
//  private val todoApi: TodoService,
  private val todoFactory: (NetworkTodoDto) -> NetworkTodo,
) : User {
  override val id by dto::id
  override val name by dto::name
  override val username by dto::username
  override val email by dto::email
  override suspend fun todoList(): Result<List<Todo>> {
    val result = errorHandle { api.userTodos(id) }
    return result.map { it.map { dto -> todoFactory(dto) } }
  }
}

@Serializable
@JvmInline
value class NetworkEmail(override val value: String) : Email, CharSequence by value

@Serializable
data class NetworkTodoDto(
  @SerialName("userId")
  val userId: Int,
  @SerialName("id")
  val id: Int,
  @SerialName("title")
  var title: String,
  @SerialName("completed")
  var isCompleted: Boolean,
)

class NetworkTodo(
  override val userId: Int,
  override val id: Int,
  override val title: String,
  override val isCompleted: Boolean,
  private val api: TodoService,
) : Todo {
  constructor(dto: NetworkTodoDto, api: TodoService) : this(
    dto.userId, dto.id, dto.title, dto.isCompleted, api
  )

  override suspend fun update(changes: Todo.UpdateParams.() -> Unit): Result<Todo> {
    val updateParams = Todo.UpdateParams(title, isCompleted)
    updateParams.changes()
    val dto = NetworkTodoDto(userId, id, updateParams.title, updateParams.isComplete)
    val result = errorHandle { api.updateTodo(id, dto) }
    return result.map { NetworkTodo(it, api) }
  }
}

class NetworkUsers(
  private val usersApi: UsersService,
  private val userFactory: (NetworkUserDto) -> NetworkUser,
  private val json: Json,
) : Users {
  override suspend fun login(username: String): Result<NetworkUser> {
    val result = try {
      usersApi.loginUser(username)
    } catch (e: IOException) {
      return Result.failure(NetworkError())
    }

    return when (val code = result.code()) {
      in 200..<300 -> {
        val body = result.body()!!.string()
        val userDtoList = json.decodeFromString<List<NetworkUserDto>>(body)
        if (userDtoList.isEmpty())
          Result.failure(InvalidUsernameException())
        else
          Result.success(userFactory(userDtoList.first()))
      }

      in 500..<600 -> Result.failure(ServerError(code))
      in 400..<500 -> Result.failure(ClientError(code))
      else -> throw IllegalStateException("Unexpected Http Error with code $code")
    }
  }
}

suspend fun <R> errorHandle(block: suspend () -> Response<R>): Result<R> {
  val result = try {
    block()
  } catch (e: IOException) {
    return Result.failure(NetworkError())
  } catch (t: Throwable) {
    return Result.failure(UnexpectedError())
  }

  return when (val code = result.code()) {
    in 200..<300 -> Result.success(result.body()!!)
    in 500..<600 -> Result.failure(ServerError(code))
    in 400..<500 -> Result.failure(ClientError(code))
    else -> throw IllegalStateException("Unexpected Http Error with code $code")
  }
}

fun createRetrofit(params: NetworkParams, json: Json): Retrofit {
  return Retrofit.Builder()
    .baseUrl(params.baseUrl)
    .client(
      OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.SECONDS)
        .apply {
          params.interceptors.forEach { addInterceptor(it) }
        }.build()
    )
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()
}