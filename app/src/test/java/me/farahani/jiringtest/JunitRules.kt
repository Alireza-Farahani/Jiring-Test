package me.farahani.jiringtest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger


@Suppress("unused")
class MockWebServerRule : TestWatcher() {
  companion object {
    private val logger = Logger.getLogger(MockWebServerRule::class.java.name)
  }

  private val server = MockWebServer()
  private var started = false

  override fun starting(description: Description) {
    before()
  }

  private fun before() {
    if (started) return
    started = true
    try {
      server.start()
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  override fun finished(description: Description) {
    after()
  }

  private fun after() {
    try {
      server.shutdown()
    } catch (e: IOException) {
      logger.log(Level.WARNING, "MockWebServer shutdown failed", e)
    }
  }

  fun getHostName(): String {
    if (!started) before()
    return server.hostName
  }

  fun getPort(): Int {
    if (!started) before()
    return server.port
  }

  fun getRequestCount(): Int {
    return server.requestCount
  }

  fun enqueue(response: MockResponse) {
    server.enqueue(response)
  }

  @Throws(InterruptedException::class)
  fun takeRequest(): RecordedRequest {
    return server.takeRequest()
  }

  fun url(path: String): HttpUrl {
    return server.url(path)
  }

  /** For any other functionality, use the [MockWebServer] directly.  */
  fun get(): MockWebServer {
    return server
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("MemberVisibilityCanBePrivate")
class MainDispatcherRule(
  val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
  override fun starting(description: Description) {
    Dispatchers.setMain(testDispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}
