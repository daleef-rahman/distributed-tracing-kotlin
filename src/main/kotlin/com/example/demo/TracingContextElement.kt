package com.example.demo

import brave.Tracing
import brave.propagation.CurrentTraceContext
import brave.propagation.TraceContext
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Manages the traceContext state in current thread when a coroutine is resumed/ suspended.
 *
 * updateThreadContext function is invoked before the coroutine in the current thread is resumed. We add the
 * traceContext to the scope of current thread, whenever the coroutine is resuming.
 * restoreThreadContext is invoked when the coroutine is suspended in current thread. We restore the thread's initial state
 * by closing the scope.
 * */
class TracingContextElement : ThreadContextElement<CurrentTraceContext.Scope>, AbstractCoroutineContextElement(Key) {
  val initial: TraceContext? = Tracing.current()?.currentTraceContext()?.get()
  companion object Key : CoroutineContext.Key<TracingContextElement>

  override fun updateThreadContext(context: CoroutineContext): CurrentTraceContext.Scope {
    return Tracing.current().currentTraceContext().maybeScope(initial)
  }

  override fun restoreThreadContext(context: CoroutineContext, scope: CurrentTraceContext.Scope) {
    scope.close()
  }
}
