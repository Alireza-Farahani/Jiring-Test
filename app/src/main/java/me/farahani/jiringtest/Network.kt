package me.farahani.jiringtest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.Interceptor
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

interface NetworkParams {
  val baseUrl: String
  val interceptors: Set<Interceptor>
}

interface UserService {
  @GET("/todos")
  suspend fun userTodos(@Query("userId") userId: Int): Response<List<NetworkTodo>>
}

interface UsersService {
  @GET("/users")
  suspend fun loginUser(@Query("username") username: String): Response<ResponseBody>
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

class NetworkUser(private val dto: NetworkUserDto, private val api: UserService) : User {
  override val id by dto::id
  override val name by dto::name
  override val username by dto::username
  override val email by dto::email
  override suspend fun todoList(): Result<List<Todo>> {
    return errorHandle { api.userTodos(id) }
  }
}

@Serializable
@JvmInline
value class NetworkEmail(override val value: String) : Email, CharSequence by value

@Serializable
data class NetworkTodo(
  @SerialName("userId")
  override val userId: Int,
  @SerialName("id")
  override val id: Int,
  @SerialName("title")
  override val title: String,
  @SerialName("completed")
  override val isCompleted: Boolean,
) : Todo

class NetworkUsers(
  private val usersApi: UsersService,
  private val userApi: UserService,
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
        try {
          val userDto = json.decodeFromString<List<NetworkUserDto>>(body).first()
          Result.success(NetworkUser(userDto, userApi))
        } catch (e: SerializationException) {
          val jo = json.parseToJsonElement(body).jsonObject
          if (jo.isEmpty())
            Result.failure(InvalidUsernameException())
          else
            throw SerializationException()
        }
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