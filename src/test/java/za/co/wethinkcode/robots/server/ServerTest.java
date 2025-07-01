package za.co.wethinkcode.robots.server;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

import org.junit.jupiter.api.*;

import java.net.Socket;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // this is needed so our tests run in a specified order
// we want the shutdown test to run last
public class ServerTest {
    private static final int TEST_PORT = 12345;

    @BeforeAll
    public static void setUpServer() throws Exception {
        new Thread(() -> {
            try {
                za.co.wethinkcode.robots.server.Server.main(new String[]{String.valueOf(TEST_PORT)});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Give server a moment to start
        Thread.sleep(1000);
    }

    @Test
    @Order(1)
    public void testLaunchCommand() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send a launch command
            String request = "{\"robot\":\"TestBot\",\"command\":\"launch\",\"arguments\":[\"shooter\"]}";
            out.write(request);
            out.newLine();
            out.flush();

            // Read response
            String response = in.readLine();
            assertNotNull(response, "Server should respond");
            assertTrue(response.contains("result"), "Response should contain a result field");
            assertTrue(response.contains("TestBot"), "Response should mention the robot name");
        }
    }

    @Test
    @Order(2)
    public void testInvalidCommand() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send an invalid command
            String request = "{\"robot\":\"InvalidBot\",\"command\":\"fly\",\"arguments\":[]}";
            out.write(request);
            out.newLine();
            out.flush();

            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("error") || response.contains("Unknown command"), "Should handle invalid commands");
        }
    }

    @Test
    @Order(3)
    public void testServerShutdown() throws Exception {
        Server.shutdown();

        Thread.sleep(500);

        assertThrows(IOException.class, () -> {
            try (Socket ignored = new Socket("localhost", TEST_PORT)) {
                // If we connect, test fails
            }
        });
    }
}
