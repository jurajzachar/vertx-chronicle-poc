package com.onetrading;

import io.netty.util.internal.StringUtil;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@NoArgsConstructor
public class PersistentCounter {

  enum CMD {ADD, SUBTRACT, YIELD}

  private final Map<String, Number> counters = new ConcurrentHashMap<>();
  private final AppendOnlyLog appender = Optional.ofNullable(System.getProperty("IS_CHRONICLE_ENABLED"))
    .or(() -> Optional.of("false"))
    .map(Boolean::valueOf)
    .<AppendOnlyLog>map(q -> q ? new OnComputed() : (req, res) -> res.encode())
    .get();

  void process(final JsonObject request, String clientAddress, Handler<String> responseHandler) {
    final var accountId = request.getString("account_id");
    if (StringUtil.isNullOrEmpty(accountId)) {
      responseHandler.handle(new JsonObject().put("error_code", "account_id must be set").encode());
      return;
    }

    try {
      final var command = Optional.ofNullable(request.getString("cmd"))
        .orElseThrow(() -> new RuntimeException("cmd must be set"));
      final var value = Optional.ofNullable(request.getNumber("value"))
        .orElseThrow(() -> new RuntimeException("value must be set")).doubleValue();

      Number computed;

      switch (CMD.valueOf(command)) {
        case ADD:
          computed = counters.compute(accountId, (k, v) -> (v == null) ? value : v.doubleValue() + value);
          break;

        case SUBTRACT:
          computed = counters.compute(accountId, (k, v) -> (v == null) ? (value * -1) : v.doubleValue() - value);
          break;

        case YIELD:
          computed = counters.getOrDefault(accountId, 0.0);
          break;

        default:
          throw new RuntimeException(String.format("unprocessable command; cmd='%s', value='%s'",
            command,
            String.valueOf(value)));

      }

      persistAndRespond(request, computed, responseHandler);

    } catch (Exception e) {
      Logger.error("client {} identified as {} submitted invalid command, error:{}",
        clientAddress,
        accountId,
        e);
      responseHandler.handle(new JsonObject().put("error_code", "unprocessable command").encode());
    }
  }

  private void persistAndRespond(JsonObject request, Number computed, Handler<String> responseHandler) {
    //respond to the client once appender successfully completes writing
    responseHandler.handle(appender.append(request, new JsonObject().put("value", computed)));
  }

  Map<String, Object> getCounters() {
    return counters
      .entrySet()
      .stream()
      .map(e -> Map.entry(e.getKey(), (Object) e.getValue()))
      .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
      ;
  }
}
