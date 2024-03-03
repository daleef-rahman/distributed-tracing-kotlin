package com.example.demo

import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.ScopedSpan
import org.springframework.cloud.sleuth.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController(val tracer: Tracer) {
  var log: Logger = LoggerFactory.getLogger(RestController::class.java)
  val fixedThreadpool = newFixedThreadPoolContext(2, "custom-thread-pool")

  @GetMapping("api")
  fun get() {
    runBlocking (TracingContextElement() + fixedThreadpool) {
      log("1")
      delay(2000)

      // Start a new span to track the long-running function
      val span: ScopedSpan = tracer.startScopedSpan("long-running-functions")
      withContext(TracingContextElement()) {
        longRunningFunction()
        longRunningFunction()
      }
      span.end()

      log("3")
    }
  }

  suspend fun longRunningFunction() {
    log("2.1")
    delay(2000)
    log("2.2")
  }

  fun log(message: String) {
    log.info("$message ${Thread.currentThread()} traceId: ${tracer.currentSpan()?.context()?.traceId()} spanId: ${tracer.currentSpan()?.context()?.spanId()}")
  }
}
