package com.onetrading;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import org.tinylog.Logger;

import java.util.stream.IntStream;

public class VMain extends AbstractVerticle {

  public static int port = 8888;

  //underlying stateful service
  private static final PersistentCounter service = new PersistentCounter();

  private static void websocketHandler(ServerWebSocket server) {
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
    final var server = new AbstractVerticle(){
      @Override
      public void start() throws Exception {
        vertx.createHttpServer()
          //wss
          .webSocketHandler(VMain::websocketHandler)
          //time endpoint
          .requestHandler(req -> {
            req.response()
              .putHeader("content-type", "application/json")
              .end(
                new JsonObject().put("data", new JsonObject(service.getCounters())).toBuffer()
              );
          }).listen(port, http -> {
            if (http.succeeded()) {
              startPromise.tryComplete();
              Logger.info("http server started on port {}", port);
            } else {
              startPromise.tryFail(http.cause());
            }
          });
      }
    };

    // M1 Pro
    IntStream.rangeClosed(1, 10).forEach(instance -> {
      Logger.info("deploying server instance #  {}", instance);
      vertx.deployVerticle(server);
    });
  }
}
