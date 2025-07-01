package za.co.wethinkcode.robots.client;

import za.co.wethinkcode.robots.commands.Command;
import za.co.wethinkcode.robots.commands.DisconnectCommand;
import za.co.wethinkcode.robots.server.Response;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientApp {
    /***
     * The ClientApp class is a Java console application that connects to a RobotWorld server,
     * allows users to launch robots, and send commands to control them. It validates input and
     * manages communication with the server.
     */
    private static final int MAX_ROBOTS = 2;
    private static final List<String> VALID_ROBOT_TYPES = List.of("sniper", "tank");

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Map<String, String> robots = new HashMap<>();

        String host = prompt(scanner, "Hello! Welcome to RobotWorld. Please enter the IP address of the server you'd like to connect to:");
        int portNumber = promptInt(scanner, "Enter the port number:");

        try (
                Socket socket = new Socket(host, portNumber);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.println("Ready for launch!");

            while (robots.size() < MAX_ROBOTS) {
                String robotName = prompt(scanner, "Enter a name for your robot:");
                if (!isValidRobotName(robotName)) {
                    System.out.println("Robot name must only contain letters and numbers. Please try again.");
                    continue;
                }

                String robotType = prompt(scanner, "Enter a type for your robot (sniper/tank):").toLowerCase().trim();
                if (!VALID_ROBOT_TYPES.contains(robotType)) {
                    System.out.println("Invalid robot type. Valid types are: sniper, tank. Please try again.");
                    continue;
                }

                Command cmd;
                try {
                    cmd = Command.fromInput("launch " + robotType + " " + robotName, robotName);
                    out.println(cmd.toJSONString());
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid Command. Try again");
                    continue;
                }

                String jsonString = in.readLine();
                Response response = Response.responseFromJSONString(jsonString);

                if (!response.isOKResponse()) {
                    System.out.println("Server: " + response.getMessage());
                    continue;
                }

                System.out.println("Launching your robot into the world 🚀");
                sleep(4000);
                System.out.println(response.getMessage());
                robots.put(robotName, robotType);
                System.out.println("To check what you can do: use 'help'\n");

                handleCommands(scanner, robotName, in, out);
                break;
            }

            if (robots.size() >= MAX_ROBOTS) {
                System.out.println("ERROR: Cannot launch more than " + MAX_ROBOTS + " robots.");
            }

        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    private static void handleCommands(Scanner scanner, String robotName, BufferedReader in, PrintWriter out) throws IOException {
        while (true) {
            System.out.print("Enter command: ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("disconnect")) {
                out.println(new DisconnectCommand().toJSONString());
                System.out.println("Server: " + in.readLine());
                break;
            }

            if (isRestrictedCommand(message)) {
                System.out.println("This command can only be run by the server admin");
                continue;
            }

            try {
                Command cmd = Command.fromInput(message, robotName);
                out.println(cmd.toJSONString());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid Command. Try again");
                continue;
            }

            System.out.println("Server: " + Response.responseFromJSONString(in.readLine()).getMessage());

            if (message.contains("reload") || message.contains("repair")) {
                // this is pretty nasty but this is to handle getting two messages from the server
                System.out.println("Server: " + Response.responseFromJSONString(in.readLine()).getMessage());
            }
        }
    }

    private static String prompt(Scanner scanner, String message) {
        System.out.println(message);
        return scanner.nextLine();
    }

    private static int promptInt(Scanner scanner, String message) {
        System.out.println(message);
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number:");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }

    private static boolean isValidRobotName(String name) {
        return !name.trim().isEmpty() && name.matches("[a-zA-Z0-9]+");
    }

    private static boolean isRestrictedCommand(String message) {
        String lower = message.toLowerCase();
        return lower.equals("quit") || lower.equals("robots") || lower.equals("dump");
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }
}
