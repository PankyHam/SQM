package com.sqm;

import java.io.File;
import java.nio.file.Paths;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.platform.Verticle;

public class Server extends Verticle {

  public void start() {

	  // Test comment for commit
	
		  RouteMatcher httpRouteMatcher = new RouteMatcher().get("/", new
	  			Handler<HttpServerRequest>() {
	  				@Override
	  				public void handle(final HttpServerRequest request) {
	  					request.response().sendFile("web/index.html");
	  				}
	  			}).get(".*\\.(css|js|woff|html)$", new Handler<HttpServerRequest>() {
	  				@Override
	  				public void handle(final HttpServerRequest request) {
	  					request.response().sendFile("web/" + new File(request.path()));
	  				}
	  			});
		  
		  vertx.createHttpServer().requestHandler(httpRouteMatcher).listen(8080, "localhost");
		  
		  Handler<ServerWebSocket> wsHandler = new Handler<ServerWebSocket>() {

			@Override
			public void handle(ServerWebSocket arg0) {
				// TODO Auto-generated method stub
				container.logger().info("Received connection");
			}
			  
		  };
		  
		  vertx.createHttpServer().websocketHandler(wsHandler).listen(8090); 
  }
}
