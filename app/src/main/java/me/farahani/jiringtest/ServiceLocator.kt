package me.farahani.jiringtest

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


class ServiceLocator {
  val session = Session()
  val json = Json { ignoreUnknownKeys = true }

  private lateinit var retrofit: Retrofit
  private val userApi: UserService by lazy { retrofit.create(UserService::class.java) }
  val usersApi: UsersService by lazy { retrofit.create(UsersService::class.java) }
  private val todoApi: TodoService by lazy { retrofit.create(TodoService::class.java) }

  private val todoFactory: (NetworkTodoDto) -> NetworkTodo = { dto -> NetworkTodo(dto, todoApi) }
  val userFactory: (NetworkUserDto) -> NetworkUser =
    { dto -> NetworkUser(dto, userApi, todoFactory) }
  val users: Users by lazy { NetworkUsers(usersApi, userFactory, json) }


  fun createRetrofit(params: NetworkParams) {
    retrofit = Retrofit.Builder()
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
}