package com.onetrading;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class TestPersistentCounter {

  final String accountId = UUID.randomUUID().toString();

  private PersistentCounter givenService() {
    return new PersistentCounter();
  }

  @Test
  void shouldReturnErrorWhenAccountIdNotSet() {
    givenService().process(new JsonObject(), "fake address", res -> {
      assertThat(new JsonObject(res)).containsExactly(Map.entry("error_code", "account_id must be set"));
    });
  }

  @Test
  void shouldReturnErrorWhenCommandNotSet() {
    givenService().process(new JsonObject().put("account_id", UUID.randomUUID().toString()), "fake address", res -> {
      assertThat(new JsonObject(res)).containsExactly(Map.entry("error_code", "unprocessable command"));
    });
  }

  @Test
  void shouldReturnErrorWhenValueNotSet() {
    givenService().process(
      new JsonObject()
        .put("account_id", UUID.randomUUID().toString()).put("cmd", "ADD"),
      "fake address",
      res ->
        assertThat(new JsonObject(res)).containsExactly(Map.entry("error_code", "unprocessable command"))
    );
  }

  @Test
  void shouldAddNumericalValue() {
    final var service = givenService();
    //before = 0, after = 1
    service.process(
      new JsonObject()
        .put("account_id", accountId)
        .put("cmd", "ADD")
        .put("value", 1),
      "fake address",
      res ->
        assertThat(new JsonObject(res)).containsExactly(Map.entry("value", 1.0))
    );
    //before = 1, after = 1.1
    service.process(
      new JsonObject()
        .put("account_id", accountId)
        .put("cmd", "ADD")
        .put("value", 0.1),
      "fake address",
      res ->
        assertThat(new JsonObject(res)).containsExactly(Map.entry("value", 1.1))
    );
  }

  @Test
  void shouldSubtractNumericalValue() {
    final var service = givenService();
    //before = 0, after = 1
    service.process(
      new JsonObject()
        .put("account_id", accountId)
        .put("cmd", "SUBTRACT")
        .put("value", 1),
      "fake address",
      res ->
        assertThat(new JsonObject(res)).containsExactly(Map.entry("value", -1.0))
    );
    //before = 1, after = 1.1
    service.process(
      new JsonObject()
        .put("account_id", accountId)
        .put("cmd", "SUBTRACT")
        .put("value", 0.1),
      "fake address",
      res ->
        assertThat(new JsonObject(res)).containsExactly(Map.entry("value", -1.1))
    );
  }

  @Test
  void shouldYieldNumericalValue() {
    final var service = givenService();
    service.process(
      new JsonObject()
        .put("account_id", accountId)
        .put("cmd", "ADD")
        .put("value", 1),
      "fake address",
      res -> {}
    );
    service.process(
      new JsonObject()
        .put("account_id", accountId)
        .put("cmd", "SUBTRACT")
        .put("value", 1),
      "fake address",
      res -> {}
    );
    service.process(
      new JsonObject()
        .put("account_id", accountId)
        .put("cmd", "YIELD")
        .put("value", 1),
      "fake address",
      res ->
        assertThat(new JsonObject(res)).containsExactly(Map.entry("value", 0.0))
    );
  }

}
