package za.co.wethinkcode.robots.AcceptanceTests;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import za.co.wethinkcode.robots.server.RobotWorldClient;
import za.co.wethinkcode.robots.server.RobotWorldJsonClient;

/**
 * As a player
 * I want to launch my robot in the online robot world
 * So that I can break the record for the most robot kills
 */
class LaunchRobotTests {
    private final static int DEFAULT_PORT = 5000;
    private final static String DEFAULT_IP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    @BeforeEach
    void connectToServer(){
        serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
    }

    @AfterEach
    void disconnectFromServer(){
        serverClient.disconnect();
    }
    @Test
    void validLaunchShouldSucceed(){
        // Given that I am connected to a running Robot Worlds server
        // And the world is of size 1x1 (The world is configured or hardcoded to this size)
        assertTrue(serverClient.isConnected());

        // When I send a valid launch request to the server
        String request = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode response = serverClient.sendRequest(request);

        // Then I should get a valid response from the server
        assertNotNull(response.get("result"));
        assertEquals("OK", response.get("result").asText());

        // And the position should be (x:0, y:0)
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("position"));
        assertEquals(0, response.get("data").get("position").get(0).asInt());
        assertEquals(0, response.get("data").get("position").get(1).asInt());

        // And I should also get the state of the robot
        assertNotNull(response.get("state"));
    }
    @Test
    void invalidLaunchShouldFail(){
        // Given that I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected());

        // When I send a invalid launch request with the command "luanch" instead of "launch"
        String request = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"luanch\"," +
                "\"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode response = serverClient.sendRequest(request);

        // Then I should get an error response
        assertNotNull(response.get("result"));
        assertEquals("ERROR", response.get("result").asText());

        // And the message "Unsupported command"
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("message"));
        assertTrue(response.get("data").get("message").asText().contains("Unsupported command"));
    }

    /**
     * Scenario: No more space in the world for another robot
     * Should return an error when trying to launch more robots than the world can hold.
     */
    @Test
    void launchShouldFailWhenWorldIsFull() {
        // Launch the first robot
        String firstLaunch = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",5,5]" +
                "}";
        JsonNode firstResponse = serverClient.sendRequest(firstLaunch);
        assertEquals("OK", firstResponse.get("result").asText());
    
        // Attempt to launch a second robot
        String secondLaunch = "{" +
                "\"robot\": \"EVE\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",5,5]" +
                "}";
        JsonNode secondResponse = serverClient.sendRequest(secondLaunch);
    
        // Must be ERROR with exact message
        assertEquals("ERROR", secondResponse.get("result").asText());
        assertNotNull(secondResponse.get("data"));
        assertEquals("No more space in this world", secondResponse.get("data").get("message").asText());
        // No state field expected
        assertNull(secondResponse.get("state"));
    }
    
    @Test
    void launchShouldFailWhenRobotNameExists() {
        // Launch the first robot
        String firstLaunch = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",5,5]" +
                "}";
        JsonNode firstResponse = serverClient.sendRequest(firstLaunch);
        assertEquals("OK", firstResponse.get("result").asText());
    
        // Attempt to launch another robot with the same name
        String duplicateLaunch = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",5,5]" +
                "}";
        JsonNode duplicateResponse = serverClient.sendRequest(duplicateLaunch);
    
        // Must be ERROR with exact message
        assertEquals("ERROR", duplicateResponse.get("result").asText());
        assertNotNull(duplicateResponse.get("data"));
        assertEquals("Too many of you in this world", duplicateResponse.get("data").get("message").asText());
        // No state field expected
        assertNull(duplicateResponse.get("state"));
    }
  
}