package me.farahani.jiringtest.network

class UnexpectedError : Exception()
class NetworkError : Exception()
class ServerError(val code: Int) : Exception("Server error: $code")

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class ClientError(val code: Int) : Exception("Client error: $code")