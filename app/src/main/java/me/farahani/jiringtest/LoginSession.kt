package me.farahani.jiringtest

import kotlinx.serialization.json.Json

interface ILoginSession {
  val currentUser: User?
  fun begin(user: User)
  fun end()
}

interface StringSerializer<T> {
  fun asString(value: T): String
  fun fromString(string: String): T
}

class DefaultLoginSession(
  private val storage: Storage,
  private val userSerializer: StringSerializer<User>,
) : ILoginSession {
  private var _currentUser: User? = null

  init {
    _currentUser = storage.get("current_user")?.let { userSerializer.fromString(it) }
  }

  override val currentUser: User? get() = _currentUser
  override fun begin(user: User) {
    _currentUser = user
    storage.put("current_user", userSerializer.asString(user))
  }

  override fun end() {
    _currentUser = null
    storage.remove("current_user")
  }
}

class NetworkUserSerializer(
  private val json: Json,
  private val userFactory: (NetworkUserDto) -> NetworkUser,
) : StringSerializer<User> {
  override fun asString(value: User): String {
    return json.encodeToString(
      NetworkUserDto.serializer(),
      NetworkUserDto(value.id, value.name, value.username, NetworkEmail(value.email.value))
    )
  }

  override fun fromString(string: String): User {
    return userFactory(json.decodeFromString(NetworkUserDto.serializer(), string))
  }
}