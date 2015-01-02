package com.sqm.unit;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sqm.Server;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

public class ServerUnitTests {

	private final String SENDER = "test_sender";
	private final String MESSAGE = "test_message";
	
	/* 
	 * Get function to return some JSON that is well formed
	 * as per the required structure of the program.
	 */
	private String getTestJson() {
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode result = jsonMapper.createObjectNode();
		result.put("message", MESSAGE);
		result.put("sender", SENDER);
		// Received is filled in by the server
		result.put("received", "");
		return result.toString();
	}
	
	/* 
	 * Get function to return some JSON that is considered
	 *  to be malformed.
	 */
	private String getMalformedJson() {
		ObjectMapper jsonMapper = new ObjectMapper();
		ObjectNode result = jsonMapper.createObjectNode();
		result.put("malformedField", "12345");
		return result.toString();
	}
	
	@Test
	public void testVerticle() {
		Server vert = new Server();

		// do something with verticle
	}

	@Test
	public void testSuccessfulJsonParsing() {
		// Create a default mock object. We're not actually going to use
		// server functionality, so this is okay.
		Server server = EasyMock.createNiceMock(Server.class);
		
		// Get test JSON message to send to server
		final String testJson = getTestJson();
		
		// Now do the JSON conversion
		final String result = server.parseAndCreateResponseTree(testJson);
		
		// Validate that this is in fact our message we sent.
		assertTrue(result.contains(SENDER));
		assertTrue(result.contains(MESSAGE));
	}
	
	@Test
	public void testUnsuccessfulJsonParsing() {
		Server server = EasyMock.createNiceMock(Server.class);
		
		// Get test JSON message to send to server
		final String testJson = getMalformedJson();
		
		// Now do the JSON conversion
		final String result = server.parseAndCreateResponseTree(testJson);
		
		// Validate the nulls. Client will check for string nulls on fields.
		assertTrue(result.contains("\"message\":null"));
		assertTrue(result.contains("\"sender\":null"));
	}

}
