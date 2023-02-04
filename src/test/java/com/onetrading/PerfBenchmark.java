package com.onetrading;

import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PerfBenchmark {

  @State(Scope.Benchmark)
  public static class ExecutionPlan {

    private final String accountId = UUID.randomUUID().toString();

    @Param({ "10.01", "1.201", "3.02", "4.50", "1000.1", "1.001", "99.001", "0.0001", "123.0123", "10.10" })
    public double value;

    public JsonObject request;

    public PersistentCounter counter = new PersistentCounter();

    @Setup(Level.Invocation)
    public void setUp() {
      request = new JsonObject()
        .put("account_id", accountId)
        .put("cmd", value % 2 == 0 ? "ADD" : "SUBTRACT")
        .put("value", value)
        ;
    }
  }

  @Benchmark
  @Fork(value = 1, warmups = 2)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @BenchmarkMode(Mode.Throughput)
  public void persistentCounter(ExecutionPlan plan) {
    plan.counter.process(plan.request,"fake", res -> {});
  }

  public static void main(String[] args) throws Exception {
    org.openjdk.jmh.Main.main(args);
  }
}
