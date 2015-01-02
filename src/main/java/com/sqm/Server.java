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
	private final String ADDRESS = "localhost";

	public void start() {

		// Matcher to handle http requests on the web client
		RouteMatcher httpRouteMatcher = new RouteMatcher().get("/",
				new Handler<HttpServerRequest>() {
					@Override
					public void handle(final HttpServerRequest request) {
						request.response().sendFile("web/index.html");
					}
				}).get(".*\\.(css|js|html)$",
				new Handler<HttpServerRequest>() {
					@Override
					public void handle(final HttpServerRequest request) {
						request.response().sendFile(
								"web/" + new File(request.path()));
					}
				});

		vertx.createHttpServer().requestHandler(httpRouteMatcher)
				.listen(8080, ADDRESS);

		// Matcher to handle requests on the web socket
		Handler<ServerWebSocket> wsHandler = new Handler<ServerWebSocket>() {

			@Override
			public void handle(ServerWebSocket connection) {
				final String client = connection.textHandlerID();
				final String room = connection.path();

				container.logger().info("WS Received connection from " + client);
				container.logger().info(connection.textHandlerID() + "has joined room " + room);

				// Register the client and room with vert.x built in event bus
				// data sets
				vertx.sharedData().getSet(CHAT_IDENTIFIER + room).add(client);

				// Handler for data callbacks
				connection.dataHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer message) {
						final String result = parseAndCreateResponseTree(message.toString());
						
						// Push the json to all the registered clients.
						for (Object ID : vertx.sharedData().getSet(CHAT_IDENTIFIER + room)) {
							vertx.eventBus().send((String) ID, result);
						}

					}

				});

			}

		};

		vertx.createHttpServer().websocketHandler(wsHandler).listen(8090);
	}

	/**
	 * Method to parse a string message and return a string message in the
	 * correct format
	 * 
	 * @return String JSON Formatted string
	 */
	public final String parseAndCreateResponseTree(final String message) {
		// Parse JSON from the client and redirect to clients registered on the
		// bus.
		ObjectMapper jsonMapper = new ObjectMapper();

		try {
			// Get the JSON tree
			JsonNode root = jsonMapper.readTree(message);

			// Build the new node to send back to clients with updated timestamp
			ObjectNode result = jsonMapper.createObjectNode();
			result.put("message", root.get("message"));
			result.put("sender", root.get("sender"));
			result.put("received", new Date().toString());

			return result.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// If we fail to parse, return nothing
		return null;
	}
}
