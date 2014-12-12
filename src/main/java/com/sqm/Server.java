package com.sqm;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.platform.Verticle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Server extends Verticle {
	
	private final String CHAT_IDENTIFIER = "room.";
	
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
				.listen(8080, "10.154.132.245");

		// Matcher to handle requests on the web socket
		Handler<ServerWebSocket> wsHandler = new Handler<ServerWebSocket>() {

			@Override
			public void handle(ServerWebSocket connection) {
				final String client = connection.textHandlerID();
				final String room = connection.path();
				
				container.logger().info("WS Received connection from " + client);
				container.logger().info(connection.textHandlerID() + "has joined room " + room);
				
				// Register the client and room with vert.x built in event bus data sets
				vertx.sharedData().getSet(CHAT_IDENTIFIER + room).add(client);
				
				
				// Handler for data callbacks
				connection.dataHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer message) {
						// Parse JSON from the client and redirect to clients registered on the bus.
						ObjectMapper jsonMapper = new ObjectMapper();
						
						try {
							// Get the JSON tree
							JsonNode root = jsonMapper.readTree(message.toString());
							
							// Build the new node to send back to clients with updated timestamp
							ObjectNode result = jsonMapper.createObjectNode();
							result.put("message", root.get("message"));
							result.put("sender", root.get("sender"));
							result.put("received", new Date().toString());
							
							// Push the json to all the registered clients.
							for (Object ID : vertx.sharedData().getSet(CHAT_IDENTIFIER + room)) {
								vertx.eventBus().send( (String) ID, result.toString());
							}
							
							
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						
					}
					
				});
				
			}

		};

		vertx.createHttpServer().websocketHandler(wsHandler).listen(8090);
	}
}
