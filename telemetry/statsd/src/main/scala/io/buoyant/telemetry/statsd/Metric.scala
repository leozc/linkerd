package io.buoyant.telemetry.statsd

import com.timgroup.statsd.StatsDClient
import com.twitter.finagle.stats.{Counter => FCounter, Stat => FStat}
import java.util.concurrent.atomic.AtomicInteger

private[statsd] object Metric {

  // counters batch up deltas and flush on send
  class Counter(statsDClient: StatsDClient, name: String) extends FCounter {
    val count = new AtomicInteger(0)

    def incr(delta: Int): Unit = {
      val _ = count.addAndGet(delta)
    }

    def send: Unit = statsDClient.count(name, count.getAndSet(0))
  }

  // gauges simply evaluate on send
  class Gauge(statsDClient: StatsDClient, name: String, f: => Float) {
    def send: Unit = statsDClient.recordGaugeValue(name, f)
  }

  // stats (histograms) only send when Math.random() <= sampleRate
  class Stat(statsDClient: StatsDClient, name: String, sampleRate: Double) extends FStat {
    def add(value: Float): Unit =
      // would prefer `recordHistogramValue`, but that is Datadog specific
      statsDClient.recordExecutionTime(name, value.toLong, sampleRate)
  }
}
