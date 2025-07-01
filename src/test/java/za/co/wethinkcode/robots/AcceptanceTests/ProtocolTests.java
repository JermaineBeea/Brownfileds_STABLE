package za.co.wethinkcode.robots.AcceptanceTests;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import za.co.wethinkcode.robots.server.RobotWorldClient;
import za.co.wethinkcode.robots.server.RobotWorldJsonClient;

class ProtocolTests {
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
    void unsupportedCommandShouldReturnError() {
            assertTrue(serverClient.isConnected());
        String request = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"fly\"," +
                "\"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(request);

        assertEquals("ERROR", response.get("result").asText());
        assertTrue(response.get("data").get("message").asText().contains("Unsupported command"));

    }

    @Test
    void stateCommandShouldReturnRobotState() {
                // Launch robot first
        String launchRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",5,5]" +
                "}";
        serverClient.sendRequest(launchRequest);

        String stateRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"state\"," +
                "\"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(stateRequest);

        assertNotNull(response.get("state"));
        assertTrue(response.get("state").has("position"));
        assertTrue(response.get("state").has("direction"));
        assertTrue(response.get("state").has("shields"));
        assertTrue(response.get("state").has("shots"));
        assertTrue(response.get("state").has("status"));

    }

    @Test
    void lookCommandShouldReturnObjectsArray() {
        // Launch robot first
        String launchRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",5,5]" +
                "}";
        serverClient.sendRequest(launchRequest);

        String lookRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"look\"," +
                "\"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(lookRequest);

        assertEquals("OK", response.get("result").asText());
        assertNotNull(response.get("data"));
        assertTrue(response.get("data").has("objects"));
        assertTrue(response.get("data").get("objects").isArray());
        assertNotNull(response.get("state"));

    }
}