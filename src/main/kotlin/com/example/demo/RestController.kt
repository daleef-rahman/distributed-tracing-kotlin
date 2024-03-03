package com.example.demo

import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.cloud.sleuth.ScopedSpan
import org.springframework.cloud.sleuth.Tracer
import org.springframework.cloud.sleuth.instrument.kotlin.asContextElement
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Rest(val tracer: Tracer) {
  val fixedThreadpool = newFixedThreadPoolContext(2, "custom-thread-pool")

  @GetMapping("api")
  fun get() {
    runBlocking (tracer.asContextElement() + fixedThreadpool) {
      val currSpan = tracer.currentSpan()
      println("1: ${Thread.currentThread()} traceId: ${currSpan?.context()?.traceId()} spanId: ${tracer.currentSpan()?.context()?.spanId()}")

      val span: ScopedSpan = tracer.startScopedSpan("second-part")
      withContext(tracer.asContextElement()) {
        println("2.1: ${Thread.currentThread()} traceId: ${tracer.currentSpan()?.context()?.traceId()} spanId: ${tracer.currentSpan()?.context()?.spanId()}")
        function1()
        println("2.2: ${Thread.currentThread()} traceId: ${tracer.currentSpan()?.context()?.traceId()} spanId: ${tracer.currentSpan()?.context()?.spanId()}")
        span.end()
      }

      println("3: ${Thread.currentThread()} traceId: ${currSpan?.context()?.traceId()} spanId: ${tracer.currentSpan()?.context()?.spanId()}")
    }

  }

  suspend fun function1() {
    delay(2000)
  }

}
