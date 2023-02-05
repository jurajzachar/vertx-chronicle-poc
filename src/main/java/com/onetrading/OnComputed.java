package com.onetrading;

import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.tinylog.Logger;

import java.util.Optional;

class OnComputed implements AppendOnlyLog {

    private final String outputDir = Optional.ofNullable(
      System.getProperty("CHRONICLE_QUEUE_DIRECTORY")
    )
    .orElse("/tmp/vertx-chronicle-poc");

    OnComputed() {
      Logger.info("instantiated Chronicle Queue appender with directory: {}", outputDir);
    }

    @Override
    public String append(JsonObject request, JsonObject response) {
      final var payload = response.encode();
      try (ChronicleQueue queue = SingleChronicleQueueBuilder.single(outputDir).build()) {
        // Obtain an ExcerptAppender
        final var appender = queue.acquireAppender();
        appender.writeDocument(w -> w
          .write("account_id")
          .text(request.getString("account_id"))
          .write("req")
          .text(request.encode())
          .write("res")
          .text(payload));
        return payload;
      } catch (Exception e) {
        Logger.error("failed to append data {}, {}, reason:", request.getMap(), response.getMap(), e);
        return new JsonObject().put("error_code", "service unavailable, please try again later").encode();
      }
    }
}
