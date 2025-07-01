package za.co.wethinkcode.robots.AcceptanceTests;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.RobotWorldClient;
import za.co.wethinkcode.robots.server.RobotWorldJsonClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Acceptance test for the "look" command in the Robot World Protocol.
 */
class LookAroundTests {
    private final static int DEFAULT_PORT = 5000;
    private final static String DEFAULT_IP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    @BeforeEach
    void connectToServer() {
        serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
    }

    @AfterEach
    void disconnectFromServer() {
        serverClient.disconnect();
    }


    /**
     * Scenario: The world is empty (no robots launched).
     * Should return an error when trying to look with a non-existent robot.
     */
    @Test
    void lookWhenWorldIsEmptyShouldReturnError() {
        String lookRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"look\"," +
                "\"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(lookRequest);

        // Should return an error result
        assertNotNull(response.get("result"));
        assertEquals("ERROR", response.get("result").asText());
        assertNotNull(response.get("data"));
        assertTrue(response.get("data").get("message").asText().toLowerCase().contains("not found")
                || response.get("data").get("message").asText().toLowerCase().contains("does not exist"));
    }

    @Test
    void lookForNonExistentRobotShouldReturnError() {
        String lookRequest = "{" +
                "\"robot\": \"DOES_NOT_EXIST\"," +
                "\"command\": \"look\"," +
                "\"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(lookRequest);

        // Should return an error result
        assertNotNull(response.get("result"));
        assertEquals("ERROR", response.get("result").asText());
        assertNotNull(response.get("data"));
        assertTrue(response.get("data").get("message").asText().toLowerCase().contains("not found")
                || response.get("data").get("message").asText().toLowerCase().contains("does not exist"));
    }
    
    @Test
    void lookAfterLaunchShouldReturnProtocolCompliantResponse() {
        // Launch a robot
        String launchRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",5,5]" +
                "}";
        serverClient.sendRequest(launchRequest);
    
        // Request look
        String lookRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"look\"," +
                "\"arguments\": []" +
                "}";
        JsonNode lookResponse = serverClient.sendRequest(lookRequest);
    
        // Check protocol compliance
        assertNotNull(lookResponse.get("result"));
        assertEquals("OK", lookResponse.get("result").asText());
    
        assertNotNull(lookResponse.get("data"));
        assertTrue(lookResponse.get("data").has("objects"));
        assertTrue(lookResponse.get("data").get("objects").isArray());
    
        assertNotNull(lookResponse.get("state"));
        assertTrue(lookResponse.get("state").has("position"));
        assertTrue(lookResponse.get("state").has("direction"));
        assertTrue(lookResponse.get("state").has("shields"));
        assertTrue(lookResponse.get("state").has("shots"));
        assertTrue(lookResponse.get("state").has("status"));
    }

}