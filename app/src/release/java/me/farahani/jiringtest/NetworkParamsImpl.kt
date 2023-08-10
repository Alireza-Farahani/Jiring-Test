package me.farahani.jiringtest

import okhttp3.Interceptor

object NetworkParamsImpl : NetworkParams {
  override val baseUrl = "https://jsonplaceholder.typicode.com"

  override val interceptors = emptySet<Interceptor>()
}