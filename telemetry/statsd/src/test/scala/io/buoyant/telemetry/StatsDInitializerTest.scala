package io.buoyant.telemetry

import com.twitter.finagle.Stack
import com.twitter.finagle.util.LoadService
import io.buoyant.config.Parser
import io.buoyant.telemetry.statsd.StatsDTelemeter
import org.scalatest._

class StatsDInitializerTest extends FunSuite {

  test("io.l5d.statsd telemeter loads with defaults") {
    val yaml =
      """|kind: io.l5d.statsd
         |""".stripMargin

    val config = Parser.objectMapper(yaml, Seq(LoadService[TelemeterInitializer]))
      .readValue[TelemeterConfig](yaml)

    val telemeter = config.mk(Stack.Params.empty)
    assert(telemeter.isInstanceOf[StatsDTelemeter])
    assert(!telemeter.stats.isNull)
    assert(telemeter.tracer.isNull)
    val _ = telemeter.run.close
  }

  test("io.l5d.statsd telemeter loads") {
    val yaml =
      """|kind: io.l5d.statsd
         |prefix: linkerd
         |hostname: 127.0.0.1
         |port: 8125
         |gaugePeriodMs: 10000
         |""".stripMargin

    val config = Parser.objectMapper(yaml, Seq(LoadService[TelemeterInitializer]))
      .readValue[TelemeterConfig](yaml)

    val telemeter = config.mk(Stack.Params.empty)
    assert(telemeter.isInstanceOf[StatsDTelemeter])
    assert(!telemeter.stats.isNull)
    assert(telemeter.tracer.isNull)
    val _ = telemeter.run.close
  }

}
