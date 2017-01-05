# Telemetry

A telemeter may receive stats and trace annotations, i.e. to send to a collector
or export. Telemetry data can be collected and exported from a linkerd process by
configuring telemeters via a top-level `telemetry` section.

<aside class="notice"> These parameters are available to the telemeter
regardless of kind. Telemeters may also have kind-specific parameters. </aside>

Key | Default Value | Description
--- | ------------- | -----------
kind | _required_ | `io.l5d.commonMetrics`, `io.l5d.statsd`, or `io.l5d.tracelog`

## CommonMetrics

> Example CommonMetrics config

```yaml
telemetry:
- kind: io.l5d.commonMetrics
```

kind: `io.l5d.commonMetrics`

Exposes admin endpoints:

* `/admin/metrics`: retrieve a given set of metrics in [twitter-server](https://twitter.github.io/twitter-server/) format
* `/admin/metrics.json`: retrieve all metrics in [twitter-server](https://twitter.github.io/twitter-server/) format
* `/admin/metrics/prometheus`: retrieve all metrics in [Prometheus](https://prometheus.io/) format

This telemeter has no additional parameters.

## StatsD

> Example StatD config

```yaml
telemetry:
- kind: io.l5d.statsd
  prefix: linkerd
  hostname: 127.0.0.1
  port: 8125
  gaugePeriodMs: 10000
```

kind: `io.l5d.statsd`

[StatsD](https://github.com/etsy/statsd) metrics exporting. This telemeter
connects to a given StatsD server via UDP. Counters and timers are exported
immediately. Gauge export interval is configurable.

Key | Default Value | Description
--- | ------------- | -----------
prefix | `linkerd` | String to prefix all exported metric names with.
hostname | `127.0.0.1` | Hostname of the StatsD server.
port | `8125` | Port of the StatsD server.
gaugePeriodMs | `10000` | Interval to export Gauge stats, in milliseconds.

## TraceLog

> Example TraceLog config

```yaml
telemetry:
- kind: io.l5d.tracelog
  sampleRate: 0.2
  level: TRACE
```

kind: `io.l5d.tracelog`

Log all tracing data, given a log-level and sample rate.

Key | Default Value | Description
--- | ------------- | -----------
host | `localhost` | Host to send trace data to.
sampleRate | `1.0` | What percentage of traces to log.
level | `INFO` | Log-level, one of: `ALL`, `CRITICAL`, `DEBUG`, `ERROR`, `FATAL`, `INFO`, `OFF`, `TRACE`, `WARNING`. For full details, see [com.twitter.logging.Level](http://twitter.github.io/util/docs/#com.twitter.logging.Level).
