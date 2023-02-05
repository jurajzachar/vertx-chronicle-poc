# About

This is a sample project "Persistent Counter" illustrating a sample webcoket server built using [Vert.x](https://github.com/vert-x), which is backed by a durable atomic
append-only [Chronicle queue](https://github.com/OpenHFT/Chronicle-Queue).


This application was generated using http://start.vertx.io

## Building

To launch your tests:

```
./mvnw clean test
```

To package your application:

```
./mvnw clean package
```

To run your application:

```
./mvnw clean compile exec:java
```

## Persistent Counters Websocket API

Open a connection to `ws://localhost:8888` and submit a command instructing server to increment, subtract or yield your account-bound counter:

```
{
  "account_id": "alice",
  "cmd": "ADD",
  "value": 1.0
},
{
  "account_id": "alice",
  "cmd": "SUBTRACT",
  "value": 0.01
},
{
  "account_id": "alice",
  "cmd": "YIELD"
}
```

## Benchmark

### Persistent Counter Arithmetics
```
Benchmark                   (value)   Mode  Cnt  Score   Error   Units
PerfBenchmark.arithmetics     10.01  thrpt    5  3,948 ± 0,112  ops/us
PerfBenchmark.arithmetics     1.201  thrpt    5  4,148 ± 0,511  ops/us
PerfBenchmark.arithmetics      3.02  thrpt    5  4,011 ± 0,276  ops/us
PerfBenchmark.arithmetics      4.50  thrpt    5  5,466 ± 0,365  ops/us
PerfBenchmark.arithmetics    1000.1  thrpt    5  3,898 ± 0,091  ops/us
PerfBenchmark.arithmetics     1.001  thrpt    5  3,898 ± 0,050  ops/us
PerfBenchmark.arithmetics    99.001  thrpt    5  4,028 ± 0,042  ops/us
PerfBenchmark.arithmetics    0.0001  thrpt    5  4,144 ± 0,136  ops/us
PerfBenchmark.arithmetics  123.0123  thrpt    5  3,951 ± 0,034  ops/us
PerfBenchmark.arithmetics     10.10  thrpt    5  4,019 ± 0,100  ops/us
```

### Artillery Load Test
Project includes an Artilerry load test manifest located under `src/test/resources`.

1. launch project locally using maven exec plugin.

``` mvn compile exec:java -DIS_CHRONICLE_ENABLED=true```

2. run artillery script

```DEBUG=ws artillery run src/test/resources/loadtest.yml -o load_test_report.json```

This load test simulates websocket API under an increasing load, it begins with one virtual user (vuser) and
continuously ramps the traffic to over 100 concurrent users.

Each connected virtual user fires a burst of **100** messages in a single iteration.

```
vusers.completed: .............................................................. 108
vusers.created: ................................................................ 108
vusers.created_by_name.0: ...................................................... 108
vusers.failed: ................................................................. 0
vusers.session_length:
  min: ......................................................................... 3
  max: ......................................................................... 15.6
  median: ...................................................................... 5.1
  p95: ......................................................................... 10.1
  p99: ......................................................................... 12.1
websocket.messages_sent: ....................................................... 10800
websocket.send_rate: ........................................................... 1154/sec
```

## Help

### Java 17 Support
Follow [Chronicle's official guidelines](https://chronicle.software/chronicle-support-java-17/).
