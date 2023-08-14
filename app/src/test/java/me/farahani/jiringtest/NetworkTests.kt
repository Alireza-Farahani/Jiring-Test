package me.farahani.jiringtest

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
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


class NetworkUsersTest {
  private lateinit var users: NetworkUsers

  @JvmField
  @Rule
  val serverRule = MockWebServerRule()

  @Before
  fun setup() {
    val networkParams = object : NetworkParams {
      override val baseUrl = serverRule.url("/").toString()
      override val interceptors = emptySet<Interceptor>()
    }
    val sl = ServiceLocator()
    sl.createRetrofit(networkParams)
    users = NetworkUsers(sl.usersApi, sl.userFactory, sl.json)
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

    val user = users.login("Antonette").getOrElse { fail() }

    assertEquals(2, user.id)
    assertEquals("Ervin Howell", user.name)
  }

  @Test
  fun `empty json means unsuccessful login`() = runTest {
    serverRule.enqueue(
      MockResponse()
        .setBody("""[]""")
        .addHeader("Content-Type", "application/json; charset=utf-8")
    )
    val result = users.login("Antonette")
    assertIs<InvalidUsernameException>(result.exceptionOrNull())
  }

  @Test(expected = SerializationException::class)
  fun `invalid server response throws SerializationException`() = runTest {
    serverRule.enqueue(
      MockResponse()
        .setBody(
          """{
            "id": 2,
            "name": "Ervin Howell",
            "username": "Antonette",
            "email": "Shanna@melissa.tv"
          }""".trimIndent()
        )
        .addHeader("Content-Type", "application/json; charset=utf-8")
    )
    users.login("Whatever")
  }

  @Test
  fun `connection errors are handled`() = runTest {
    serverRule.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
    val result = users.login("Antonette")
    assertTrue { result.isFailure }
    assertIs<NetworkError>(result.exceptionOrNull())
  }
}

class NetworkUserTest {
  @JvmField
  @Rule
  val serverRule = MockWebServerRule()

  private lateinit var user: NetworkUser

  @Before
  fun setup() {
    val networkParams = object : NetworkParams {
      override val baseUrl = serverRule.url("/").toString()
      override val interceptors = emptySet<Interceptor>()
    }
    val sl = ServiceLocator()
    sl.createRetrofit(networkParams)

    // user params are not used in these tests. we just use its apis
    val userDto = NetworkUserDto(1, "Ali", "Alavi", NetworkEmail("a@b.com"))
    user = sl.userFactory(userDto)
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
    val userTodos = user.todoList().getOrElse { fail() }

    assertEquals(2, userTodos.size)
    assertEquals(false, userTodos.find { it.id == 1 }!!.isCompleted)
  }
}