package com.sqm.integration.java;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.testtools.TestVerticle;

import com.sqm.Server;

import static org.vertx.testtools.VertxAssert.*;

public class ServerIntegrationTests extends TestVerticle {

	private static final int PORT_TO_TEST = 8080;
	private static final int SUCCESS_RESPONSE = 200;
	
	@Test
	public void testHTTPServerOnPort() {
		// Create a HTTP Server on the testing port.
		vertx.createHttpServer()
				.requestHandler(new Handler<HttpServerRequest>() {
					public void handle(HttpServerRequest req) {
						req.response().end();
					}
					
				// Register a listener for asynchronous results.
				}).listen(PORT_TO_TEST, new AsyncResultHandler<HttpServer>() {
					@Override
					public void handle(AsyncResult<HttpServer> asyncResult) {
						assertTrue(asyncResult.succeeded());
						
						// Create a client that connects to the server on the test port.
						vertx.createHttpClient().setPort(PORT_TO_TEST)
								.getNow("/", new Handler<HttpClientResponse>() {
									@Override
									public void handle(HttpClientResponse resp) {
										// If the status code is successfull, we're finished.
										assertEquals(SUCCESS_RESPONSE, resp.statusCode());
										testComplete();
									}
								});
					}
				});
	}

	@Test
	/*
	 * This test deploys the server verticle and tests client connections.
	 */
	public void testDeployServerVerticle() {
		// Deploy the server verticle
		container.deployVerticle(Server.class.getName());
		
		// Set a timer to make sure the verticle is deployed.
		vertx.setTimer(1000, new Handler<Long>() {
			@Override
			public void handle(Long timerID) {
				assertNotNull(timerID);

				// Create a basic http client to connect to the verticle.
				vertx.createHttpClient().setPort(PORT_TO_TEST)
				.getNow("/", new Handler<HttpClientResponse>() {
					@Override
					public void handle(HttpClientResponse resp) {
						// If the status code is successful, we're finished.
						assertEquals(SUCCESS_RESPONSE, resp.statusCode());
						testComplete();
					}
				});
			}
		});
		
	}


}
