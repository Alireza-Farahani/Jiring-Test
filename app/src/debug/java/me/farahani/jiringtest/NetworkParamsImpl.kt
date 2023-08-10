package me.farahani.jiringtest

import okhttp3.logging.HttpLoggingInterceptor


object NetworkParamsImpl: NetworkParams {
  override val baseUrl = "https://jsonplaceholder.typicode.com"

  override val interceptors = setOf(
    HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }
  )
}