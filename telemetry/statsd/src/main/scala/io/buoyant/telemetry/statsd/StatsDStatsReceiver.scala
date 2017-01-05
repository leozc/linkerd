package io.buoyant.telemetry.statsd

import java.util.concurrent.ConcurrentHashMap
import com.timgroup.statsd.StatsDClient
import com.twitter.finagle.stats.{Counter, Stat, StatsReceiverWithCumulativeGauges}
import scala.collection.JavaConverters._

private[telemetry] object StatsDStatsReceiver {
  // from https://github.com/researchgate/diamond-linkerd-collector/
  private[statsd] def mkName(name: Seq[String]): String = {
    name.mkString("/")
      .replace(".", "_")
      .replace("#", "")
      .replace("//", "/")
      .replace("/", ".") // http://graphite.readthedocs.io/en/latest/feeding-carbon.html#step-1-plan-a-naming-hierarchy
  }
}

private[telemetry] class StatsDStatsReceiver(statsDClient: StatsDClient)
  extends StatsReceiverWithCumulativeGauges {
  import StatsDStatsReceiver._

  val repr: AnyRef = this

  private[statsd] def flushGauges(): Unit = gauges.values.foreach(_.send)
  private[statsd] def close(): Unit = statsDClient.stop()

  private[this] val gauges = new ConcurrentHashMap[String, Metric.Gauge].asScala

  protected[this] def registerGauge(name: Seq[String], f: => Float): Unit = {
    val statsDName = mkName(name)
    gauges(mkName(name)) = new Metric.Gauge(statsDClient, statsDName, f)
  }

  protected[this] def deregisterGauge(name: Seq[String]): Unit = {
    val _ = gauges.remove(mkName(name))
  }

  def counter(name: String*): Counter =
    new Metric.Counter(statsDClient, mkName(name))

  def stat(name: String*): Stat =
    new Metric.Stat(statsDClient, mkName(name))
}
