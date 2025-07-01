package za.co.wethinkcode.robots.AcceptanceTests;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import za.co.wethinkcode.robots.server.RobotWorldClient;
import za.co.wethinkcode.robots.server.RobotWorldJsonClient;

/**
 * Tests for the "state" command in the Robot World Protocol.
 */
class RobotStateTests {
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
     * Scenario: The robot exists in the world.
     * Should return the robot's state.
     */
    @Test
    void getState_whenRobotExists_returnsRobotState() {
        // Launch a robot
        String launchRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        serverClient.sendRequest(launchRequest);

        // Request state
        String stateRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"state\"," +
                "\"arguments\": []" +
                "}";
        JsonNode stateResponse = serverClient.sendRequest(stateRequest);

        // The response should contain a "state" object with expected fields
        JsonNode state = stateResponse.get("state");
        assertNotNull(state, "State object should not be null for existing robot");
        assertNotNull(state.get("position"));
        assertNotNull(state.get("direction"));
        assertNotNull(state.get("shields"));
        assertNotNull(state.get("shots"));
        assertNotNull(state.get("status"));
    }

    /**
     * Scenario: The robot is not in the world.
     * Should return an error.
     */
    @Test
    void getState_whenRobotDoesNotExist_returnsError() {
        String stateRequest = "{" +
                "\"robot\": \"DOES_NOT_EXIST\"," +
                "\"command\": \"state\"," +
                "\"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(stateRequest);

        // Should return an error result
        assertNotNull(response.get("result"));
        assertEquals("ERROR", response.get("result").asText());
        assertNotNull(response.get("data"));
        assertTrue(response.get("data").get("message").asText().toLowerCase().contains("not found")
                || response.get("data").get("message").asText().toLowerCase().contains("does not exist"));
    }
}