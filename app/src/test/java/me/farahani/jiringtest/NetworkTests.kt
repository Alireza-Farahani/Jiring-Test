package me.farahani.jiringtest

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
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
    val json = Json { ignoreUnknownKeys = true }
    val retrofit = Retrofit.Builder()
      .baseUrl(serverRule.url("/").toString())
      .client(
        OkHttpClient.Builder()
          .callTimeout(1, TimeUnit.SECONDS)
          .build()
      )
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
    users = NetworkUsers(retrofit.create(), { dto -> NetworkUser(dto, mockk(), mockk()) }, json)
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
    val json = Json { ignoreUnknownKeys = true }
    val retrofit = Retrofit.Builder()
      .baseUrl(serverRule.url("/").toString())
      .client(
        OkHttpClient.Builder()
          .callTimeout(1, TimeUnit.SECONDS)
          .build()
      )
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
    // user params are not used in these tests. we just use its apis
    val stubDto = NetworkUserDto(1, "Ali", "Alavi", NetworkEmail("a@b.com"))
    user = NetworkUser(stubDto, retrofit.create()) { dto -> NetworkTodo(dto, retrofit.create()) }
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