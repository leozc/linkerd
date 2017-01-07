package io.buoyant.telemetry.statsd

import com.timgroup.statsd.StatsDClient
import org.scalatest._

class MetricTest extends FunSuite {

  class MockStatsDClient extends StatsDClient {

    var lastName = ""
    var lastValue = ""

    def count(x$1: String, x$2: Long, x$3: String*): Unit = {
      lastName = x$1
      lastValue = x$2.toString
    }
    def recordExecutionTime(x$1: String, x$2: Long, x$3: Double, x$4: String*): Unit = {
      lastName = x$1
      lastValue = x$2.toString
    }
    def recordGaugeValue(x$1: String, x$2: Double, x$3: String*): Unit = {
      lastName = x$1
      lastValue = x$2.toString
    }

    // we can't simply extend NoOpStatsDClient because it's final
    def count(x$1: String, x$2: Long, x$3: Double, x$4: String*): Unit = ???
    def decrement(x$1: String, x$2: Double, x$3: String*): Unit = ???
    def decrement(x$1: String, x$2: String*): Unit = ???
    def decrementCounter(x$1: String, x$2: Double, x$3: String*): Unit = ???
    def decrementCounter(x$1: String, x$2: String*): Unit = ???
    def gauge(x$1: String, x$2: Long, x$3: Double, x$4: String*): Unit = ???
    def gauge(x$1: String, x$2: Long, x$3: String*): Unit = ???
    def gauge(x$1: String, x$2: Double, x$3: Double, x$4: String*): Unit = ???
    def gauge(x$1: String, x$2: Double, x$3: String*): Unit = ???
    def histogram(x$1: String, x$2: Long, x$3: Double, x$4: String*): Unit = ???
    def histogram(x$1: String, x$2: Long, x$3: String*): Unit = ???
    def histogram(x$1: String, x$2: Double, x$3: Double, x$4: String*): Unit = ???
    def histogram(x$1: String, x$2: Double, x$3: String*): Unit = ???
    def increment(x$1: String, x$2: Double, x$3: String*): Unit = ???
    def increment(x$1: String, x$2: String*): Unit = ???
    def incrementCounter(x$1: String, x$2: Double, x$3: String*): Unit = ???
    def incrementCounter(x$1: String, x$2: String*): Unit = ???
    def recordEvent(x$1: com.timgroup.statsd.Event, x$2: String*): Unit = ???
    def recordExecutionTime(x$1: String, x$2: Long, x$3: String*): Unit = ???
    def recordGaugeValue(x$1: String, x$2: Long, x$3: Double, x$4: String*): Unit = ???
    def recordGaugeValue(x$1: String, x$2: Long, x$3: String*): Unit = ???
    def recordGaugeValue(x$1: String, x$2: Double, x$3: Double, x$4: String*): Unit = ???
    def recordHistogramValue(x$1: String, x$2: Long, x$3: Double, x$4: String*): Unit = ???
    def recordHistogramValue(x$1: String, x$2: Long, x$3: String*): Unit = ???
    def recordHistogramValue(x$1: String, x$2: Double, x$3: Double, x$4: String*): Unit = ???
    def recordHistogramValue(x$1: String, x$2: Double, x$3: String*): Unit = ???
    def recordServiceCheckRun(x$1: com.timgroup.statsd.ServiceCheck): Unit = ???
    def recordSetValue(x$1: String, x$2: String, x$3: String*): Unit = ???
    def serviceCheck(x$1: com.timgroup.statsd.ServiceCheck): Unit = ???
    def stop(): Unit = ???
    def time(x$1: String, x$2: Long, x$3: Double, x$4: String*): Unit = ???
    def time(x$1: String, x$2: Long, x$3: String*): Unit = ???
  }

  test("Counter increments a statsd counter") {
    val name = "foo"
    val value = 123
    val statsDClient = new MockStatsDClient
    val counter = new Metric.Counter(statsDClient, name)
    counter.incr(value)
    counter.send

    assert(statsDClient.lastName == name)
    assert(statsDClient.lastValue == value.toString)
  }

  test("Counter batches deltas on send") {
    val name = "foo"
    val value = 123
    val statsDClient = new MockStatsDClient
    val counter = new Metric.Counter(statsDClient, name)

    counter.incr(value)
    assert(statsDClient.lastName == "")
    assert(statsDClient.lastValue == "")

    counter.incr(value)
    assert(statsDClient.lastName == "")
    assert(statsDClient.lastValue == "")

    counter.send
    assert(statsDClient.lastName == name)
    assert(statsDClient.lastValue == (value + value).toString)
  }

  test("Stat records a statsd execution time") {
    val name = "foo"
    val value = 123.4F
    val statsDClient = new MockStatsDClient
    val stat = new Metric.Stat(statsDClient, name, 1.0d)
    stat.add(value)

    assert(statsDClient.lastName == name)
    assert(statsDClient.lastValue == value.toLong.toString)
  }

  test("Gauge records a statsd gauge value on every send") {
    val name = "foo"
    var value = 123.4F
    def func(): Float = { value += value; value }
    val statsDClient = new MockStatsDClient
    val gauge = new Metric.Gauge(statsDClient, name, func)

    gauge.send
    assert(statsDClient.lastName == name)
    assert(statsDClient.lastValue == value.toDouble.toString)

    gauge.send
    assert(statsDClient.lastName == name)
    assert(statsDClient.lastValue == value.toDouble.toString)
  }
}
