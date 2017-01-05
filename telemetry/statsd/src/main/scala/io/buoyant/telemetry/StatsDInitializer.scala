package io.buoyant.telemetry

import com.timgroup.statsd.NonBlockingStatsDClient
import com.twitter.finagle.Stack
import com.twitter.finagle.util.DefaultTimer
import com.twitter.logging.Logger
import io.buoyant.telemetry.statsd.{StatsDStatsReceiver, StatsDTelemeter}
import java.util.UUID

class StatsDInitializer extends TelemeterInitializer {
  type Config = StatsDConfig
  val configClass = classOf[StatsDConfig]
  override val configId = "io.l5d.statsd"
}

private[telemetry] object StatsDConfig {
  val DefaultPrefix = "linkerd"
  val DefaultHostname = "127.0.0.1"
  val DefaultPort = 8125
  val DefaultGaugePeriodMs = 10000

  val MaxQueueSize = 10000
  val ProcessId = "l5d-uuid-" + UUID.randomUUID().toString
}

case class StatsDConfig(
  prefix: Option[String],
  hostname: Option[String],
  port: Option[Int],
  gaugePeriodMs: Option[Int]
) extends TelemeterConfig {
  import StatsDConfig._

  private[this] val log = Logger.get("io.l5d.statsd")

  // prefix format is: "prefix_l5d-uuid-process-id"
  val statsDPrefix = prefix.getOrElse(DefaultPrefix) + "_" + ProcessId
  val statsDHost = hostname.getOrElse(DefaultHostname)
  val statsDPort = port.getOrElse(DefaultPort)

  // initiate a UDP connection at startup time
  log.info(s"connecting to StatsD at $statsDHost:$statsDPort as $statsDPrefix")
  private[this] val statsDClient = new NonBlockingStatsDClient(
    statsDPrefix,
    statsDHost,
    statsDPort,
    MaxQueueSize
  )

  def mk(params: Stack.Params): StatsDTelemeter =
    new StatsDTelemeter(
      new StatsDStatsReceiver(statsDClient),
      gaugePeriodMs.getOrElse(DefaultGaugePeriodMs),
      DefaultTimer.twitter
    )
}
