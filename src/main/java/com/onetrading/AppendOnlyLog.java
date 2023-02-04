package com.onetrading;

import io.vertx.core.json.JsonObject;

@FunctionalInterface
public interface AppendOnlyLog {

  /**
   * @param request  client's request to server identified by account_id
   * @param response server's response
   * @return encoded response which is to sent to the client
   */
  String append(JsonObject request, JsonObject response);
}
