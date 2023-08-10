package me.farahani.jiringtest

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail


class UsersServiceTest {
  private lateinit var usersService: UsersService
  private lateinit var userService: UserService
  private lateinit var json: Json

  @JvmField @Rule
  val serverRule = MockWebServerRule()

  @Before
  fun setup() {
    val networkParams = object : NetworkParams {
      override val baseUrl = serverRule.url("/").toString()
      override val interceptors = emptySet<Interceptor>()
    }
    val sl = ServiceLocator()
    sl.createRetrofit(networkParams)
    usersService = sl.usersService
    userService = sl.userService
    json = sl.json
  }

  @Test
  fun `valid json is converted to logged-in user`() = runTest {
    serverRule.enqueue(
      MockResponse().setBody("""
        [
          {
            "id": 2,
            "name": "Ervin Howell",
            "username": "Antonette",
            "email": "Shanna@melissa.tv",
            "address": {
              "street": "Victor Plains",
              "suite": "Suite 879",
              "city": "Wisokyburgh",
              "zipcode": "90566-7771",
              "geo": {
                "lat": "-43.9509",
                "lng": "-34.4618"
              }
            },
            "phone": "010-692-6593 x09125",
            "website": "anastasia.net",
            "company": {
              "name": "Deckow-Crist",
              "catchPhrase": "Proactive didactic contingency",
              "bs": "synergize scalable supply-chains"
            }
          }
        ]
      """.trimIndent()
      ).addHeader("Content-Type", "application/json; charset=utf-8")
    )

    val user = NetworkUsers(usersService, userService, json).login("Antonette").getOrElse { fail() }

    assertEquals(2, user.id)
    assertEquals("Ervin Howell", user.name)
  }

  @Test
  fun `empty json means unsuccessful login`() = runTest {
    serverRule.enqueue(
      MockResponse()
        .setBody("""{}""")
        .addHeader("Content-Type", "application/json; charset=utf-8")
    )
    val result = NetworkUsers(usersService, userService, json).login("Antonette")
    assertIs<InvalidUsernameException>(result.exceptionOrNull())
  }

  @Test(expected = SerializationException::class)
  fun `invalid server response throws SerializationException`() = runTest {
    serverRule.enqueue(
      MockResponse()
        .setBody("""{
            "id": 2,
            "name": "Ervin Howell",
            "username": "Antonette",
            "email": "Shanna@melissa.tv"
          }""".trimIndent())
        .addHeader("Content-Type", "application/json; charset=utf-8")
    )
    NetworkUsers(usersService, userService, json).login("Whatever")
  }

  @Test
  fun `connection errors are handled`() = runTest {
    serverRule.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
    val result = NetworkUsers(usersService, userService, json).login("Antonette")
    assertTrue { result.isFailure }
    assertIs<NetworkError>(result.exceptionOrNull())
  }
}

class UserServiceTest {
  @JvmField @Rule
  val serverRule = MockWebServerRule()
  private lateinit var userService: UserService
  private lateinit var serviceLocator: ServiceLocator

  @Before
  fun setup() {
    val networkParams = object : NetworkParams {
      override val baseUrl = serverRule.url("/").toString()
      override val interceptors = emptySet<Interceptor>()
    }
    serviceLocator = ServiceLocator()
    serviceLocator.createRetrofit(networkParams)
    userService = serviceLocator.userService
  }

  @Test
  fun `server json is converted to list of Todo objects`() = runTest {
    serverRule.enqueue(
      MockResponse().setBody("""
        [
          {
            "userId": 1,
            "id": 1,
            "title": "delectus aut autem",
            "completed": false
          },
          {
            "userId": 1,
            "id": 2,
            "title": "quis ut nam facilis et officia qui",
            "completed": false
          }
        ]
      """.trimIndent()
      ).addHeader("Content-Type", "application/json; charset=utf-8")
    )
    val userDto = NetworkUserDto(1, "Ali", "Alavi", NetworkEmail("a@b.com"))
    val user = NetworkUser(userDto, userService)
    val userTodos = user.todoList().getOrElse { fail() }

    assertEquals(2, userTodos.size)
    assertEquals(false, userTodos.find { it.id == 1 }!!.isCompleted)
  }
}