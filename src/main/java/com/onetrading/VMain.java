package com.onetrading;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import org.tinylog.Logger;

import java.time.Clock;

public class VMain extends AbstractVerticle {

  public static int port = 8888;

  //underlying stateful service
  private final PersistentCounter service = new PersistentCounter();

  private void websocketHandler(ServerWebSocket server) {
    final var clientAddress = server.remoteAddress().hostAddress();
    server.textMessageHandler(payload -> {
      try {
        final var json = new JsonObject(payload);
        service.process(json, clientAddress, server::writeTextMessage);
      } catch (Exception e) {
        server.writeTextMessage(new JsonObject().put("error_code", "malformed request").encode());
        //any json parsing errors result in closing of the underlying socket
        server.close(whenDone -> Logger.info("closed client {}; malformed request",
          server.remoteAddress().hostAddress()));
      }
    });
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer()
    //wss
    .webSocketHandler(this::websocketHandler)
    //time endpoint
    .requestHandler(req -> {
      req.response()
        .putHeader("content-type", "application/json")
        .end(new JsonObject()
          .put("time", Clock.systemUTC().instant())
          .toBuffer()
        );
    }).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        Logger.info("http server started on port {}", port);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
