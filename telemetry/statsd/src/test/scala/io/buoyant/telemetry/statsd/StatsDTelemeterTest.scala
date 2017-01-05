package io.buoyant.telemetry.statsd

import com.timgroup.statsd.{NoOpStatsDClient, StatsDClient}
import com.twitter.conversions.time._
import com.twitter.util.{Time, MockTimer}
import org.scalatest._

class StatsDTelemeterTest extends FunSuite {

  class MockStatsDStatsReceiver(statsDClient: StatsDClient)
    extends StatsDStatsReceiver(statsDClient: StatsDClient) {

    var flushes = 0
    var closed = false

    override private[statsd] def flushGauges(): Unit = { flushes += 1 }
    override private[statsd] def close(): Unit = { closed = true }
  }

  test("creates a telemeter") {
    val stats = new MockStatsDStatsReceiver(new NoOpStatsDClient)

    val telemeter = new StatsDTelemeter(
      stats,
      10000,
      new MockTimer
    )

    assert(stats.flushes == 0)
    assert(!stats.closed)
  }

  test("stops on close") {
    val stats = new MockStatsDStatsReceiver(new NoOpStatsDClient)

    val telemeter = new StatsDTelemeter(
      stats,
      10000,
      new MockTimer
    )

    val closable = telemeter.run()

    assert(!stats.closed)
    val _ = closable.close(0.millis)
    assert(stats.closed)
  }

  test("flushes gauges every period until close") {
    val gaugePeriodMs = 10000
    val stats = new MockStatsDStatsReceiver(new NoOpStatsDClient)
    val timer = new MockTimer

    val telemeter = new StatsDTelemeter(
      stats,
      gaugePeriodMs,
      timer
    )

    Time.withCurrentTimeFrozen { time =>
      val closable = telemeter.run()

      assert(stats.flushes == 0)

      time.advance(gaugePeriodMs.millis)
      timer.tick()
      assert(stats.flushes == 1)

      time.advance(gaugePeriodMs.millis)
      timer.tick()
      assert(stats.flushes == 2)

      val _ = closable.close(0.millis)

      time.advance(gaugePeriodMs.millis)
      timer.tick()
      assert(stats.flushes == 2)
    }
  }
}
