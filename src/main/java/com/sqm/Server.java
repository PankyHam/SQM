package com.sqm;

import java.io.File;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.platform.Verticle;

public class Server extends Verticle {
	
	public void start() {
	
		// Matcher to handle http requests on the web client
		RouteMatcher httpRouteMatcher = new RouteMatcher().get("/",
				new Handler<HttpServerRequest>() {
					@Override
					public void handle(final HttpServerRequest request) {
						request.response().sendFile("web/index.html");
					}
				}).get(".*\\.(css|js|woff|html)$",
				new Handler<HttpServerRequest>() {
					@Override
					public void handle(final HttpServerRequest request) {
						request.response().sendFile(
								"web/" + new File(request.path()));
					}
				});

		vertx.createHttpServer().requestHandler(httpRouteMatcher)
				.listen(8080, "localhost");

		// Matcher to handle requests on the web socket
		Handler<ServerWebSocket> wsHandler = new Handler<ServerWebSocket>() {

			@Override
			public void handle(ServerWebSocket connection) {
				final String client = connection.textHandlerID();
				final String room = connection.path();
				
				container.logger().info("WS Received connection from " + client);
				container.logger().info(connection.textHandlerID() + "has joined room " + room);
			}

		};

		vertx.createHttpServer().websocketHandler(wsHandler).listen(8090);
	}
}
