package za.co.wethinkcode.robots.server;
import za.co.wethinkcode.flow.Recorder;
import za.co.wethinkcode.robots.handlers.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Main server class that accepts client connections and provides an admin console for server control.
 * Supports real-time robot monitoring, world state inspection, and graceful shutdown.
 */
public class Server {
    private static volatile boolean isRunning = true;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        int portNumber;
        World world = World.getInstance();

        if (args.length != 1) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the port number: ");
            portNumber = scanner.nextInt();
            scanner.nextLine();
        } else {
            portNumber = Integer.parseInt(args[0]);
        }

        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server started on port " + portNumber + ". Waiting for clients...");

            // launch admin console thread
            startAdminConsole(world);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                new Thread(new ClientHandler(clientSocket, world)).start(); // start new thread to handle multiple clients
            }

        } catch (IOException e) {
            if (!isRunning) {
                System.out.println("Sever shutdown.");
            } else {
                System.out.println("Got an error: " + e);
            }
        }
    }
    private static void startAdminConsole(World world) {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (isRunning) {
                System.out.println("Valid Commands: 'quit', 'robots', 'dump', 'display'");
                System.out.print("[Admin]: ");
                String input = scanner.nextLine().trim().toLowerCase();
                switch (input) {
                    case "quit":
                        System.out.println("Shutting down server...");
                        shutdown();
                        break;
                    case "robots":
                         System.out.println(world.getAllRobotsInfo());
                        break;
                    case "dump":
                        System.out.println(world.getFullWorldState());
                        break;
                    case "display":
                        world.displayWorld();
                        break;
                    default:
                        System.out.println("Unknown admin command.");
                }
            }
        }, "AdminConsole").start();
    }


    public static void shutdown() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Got an error when shutting down: " + e);
        }
    }
    static {
        new Recorder().logRun();
    }
}

