package me.farahani.jiringtest


class InvalidUsernameException : IllegalArgumentException()

class UnexpectedError : Exception()
class NetworkError : Exception()
class ServerError(val code: Int) : Exception("Server error: $code")
@Suppress("MemberVisibilityCanBePrivate")
class ClientError(val code: Int) : Exception("Client error: $code")