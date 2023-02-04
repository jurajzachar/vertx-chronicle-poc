package com.onetrading;

import io.vertx.core.json.JsonObject;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * When starting a JVM explicitly using the “java” command, the following command line parameters need to be passed in:
 * <p>
 * --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED
 * --add-exports=java.base/sun.nio.ch=ALL-UNNAMED
 * --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED
 * --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
 * --add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED
 * --add-opens=java.base/java.lang=ALL-UNNAMED
 * --add-opens=java.base/java.lang.reflect=ALL-UNNAMED
 * --add-opens=java.base/java.io=ALL-UNNAMED
 * --add-opens=java.base/java.util=ALL-UNNAMED
 *
 * @see https://chronicle.software/chronicle-support-java-17/
 */
public class TestOnComputed {

  private static final String dir = "./target/test-chronicle-queue";

  final AppendOnlyLog appender = new OnComputed();

  @BeforeAll
  static void prepareFilesystem() {
    try {
      Files.createDirectories(Paths.get(dir));
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.setProperty("CHRONICLE_QUEUE_DIRECTORY", dir);
  }

  @AfterAll
  static void cleanupFilesystem() throws IOException {
    Files.walk(Paths.get(dir))
      .sorted(Comparator.reverseOrder())
      .map(Path::toFile)
      .forEach(File::delete);
  }

  @Test
  void shouldSaveDataSuccessfully() {
    final var accountId = UUID.randomUUID().toString();
    final var req = new JsonObject().put("data", System.nanoTime()).put("account_id", accountId);
    final var res = new JsonObject().put("data", "1234567890_abcdefghijklmnopqrstuvwxyz");

    //response is returned encoded
    assertThat(appender.append(req, res)).isEqualTo(res.encode());

    //read data with a tailer
    try (ChronicleQueue queue = ChronicleQueue.singleBuilder(dir).build()) {
      final ExcerptTailer tailer = queue.createTailer();
      tailer.readDocument(w -> {
        assertThat(w.read(() -> "account_id").text()).isEqualTo(accountId);
        assertThat(w.read(() -> "req").text()).isEqualTo(req.encode());
        assertThat(w.read(() -> "res").text()).isEqualTo(res.encode());
      });
    }
  }
}
