package me.farahani.jiringtest

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit


class ServiceLocator {
  val session = Session()
  val json = Json { ignoreUnknownKeys = true }

  private lateinit var retrofit: Retrofit
  val userService: UserService by lazy { retrofit.create(UserService::class.java) }
  val usersService: UsersService by lazy { retrofit.create(UsersService::class.java) }

  fun createRetrofit(params: NetworkParams) {
    retrofit = Retrofit.Builder()
      .baseUrl(params.baseUrl)
      .client(
        OkHttpClient.Builder()
          .apply {
            params.interceptors.forEach { addInterceptor(it) }
          }.build()
      )
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
  }
}