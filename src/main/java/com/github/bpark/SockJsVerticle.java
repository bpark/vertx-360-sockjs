package com.github.bpark;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class SockJsVerticle extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);

        SockJSHandlerOptions options = new SockJSHandlerOptions().setInsertJSESSIONID(true);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

        sockJSHandler.socketHandler(sockJSSocket -> {
            sockJSSocket.handler(buffer -> {
                System.out.println(buffer.toString());
                sockJSSocket.write(buffer);
            });
        });

        router.route("/endpoint/*").handler(sockJSHandler);

        router.route().handler(StaticHandler.create("client"));

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router);
        httpServer.listen(8080);

    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(2);
        vertx.deployVerticle(SockJsVerticle.class.getName(), deploymentOptions, result -> {
            if (result.succeeded()) {
                System.out.println("deployment successful, start the client by opening http://localhost:8080");
            }
        });
    }
}
