package za.co.wethinkcode.robots.handlers;

import org.json.JSONArray;
import org.json.JSONObject;
import za.co.wethinkcode.robots.Robot;
import za.co.wethinkcode.robots.client.Position;
import za.co.wethinkcode.robots.server.*;
import za.co.wethinkcode.robots.commands.*;

import java.util.*;

public class CommandHandler {
    @FunctionalInterface
    public interface CompletionHandler {
        void onComplete(Response response);
    }

    private final World world;
    private final Map<String, HashMap<String, String>> clientRobots = new HashMap<>();
    private final VisibilityHandler visibilityHandler;

    public CommandHandler(World world) {
        this.world = world;

        this.visibilityHandler = new VisibilityHandler(
                world.getRobots(),
                world.getObstacles(),
                world.getHalfWidth(),
                world.getHalfHeight(),
                world.getVisibility(),
                world
        );
    }
/**
 * Handles commands by directing each command to its specific handling logic.
 * This method uses a switch expression with pattern matching to determine the
 * specific type of Command and calls the corresponding handler method.*/

    public void handle(Command command, String clientId, CompletionHandler handler) {
        System.out.println("Executing command: " + command.commandName());

       switch (command) {
            case HelpCommand helpCommand -> handleHelp(helpCommand, handler);
            case LaunchCommand launchCommand -> handleLaunch(launchCommand, clientId, handler);
            case StateCommand stateCommand -> handleState(stateCommand, command.robot.getName(), handler);
            case OrientationCommand orientationCommand -> handleOrientation(orientationCommand, command.robot.getName(), handler);
            case LookCommand lookCommand -> handleLook(lookCommand, command.robot.getName(), handler);
            case MoveCommand moveCommand -> handleMove(moveCommand, handler);
            case TurnCommand turnCommand -> handleTurn(turnCommand, turnCommand.robot.getName(), handler);
            case ShutdownCommand shutdownCommand -> handleShutdown(shutdownCommand, handler);
            case DisconnectCommand ignored -> handler.onComplete(new Response("OK", "Client disconnected."));
            case FireCommand ignored -> handleFire(command.robot, handler);
            case ReloadCommand reloadCommand -> handleReload(reloadCommand, handler);
            case RepairCommand repairCommand -> handleRepair(repairCommand, handler);
            default -> handler.onComplete(new Response("ERROR", "Unsupported command"));
        };
    }

    private void handleHelp(HelpCommand robot, CompletionHandler handler) {
        String helpText = String.join("\n",
                """
                         🌸🤖✨ I CAN UNDERSTAND THESE COMMANDS 🌸🤖✨
                        ┌────────────────────┬──────────────────────────────────────────────┐
                         COMMAND             | DESCRIPTION
                        ├────────────────────┼──────────────────────────────────────────────┤ 
                        |1.❓ help           | Show this help message 🆘                    
                        ├────────────────────┼──────────────────────────────────────────────┤
                        |2.🧭 orientation    | What direction you are facing   
                        ├────────────────────┼──────────────────────────────────────────────┤ 
                        |3.forward <name> <n>| Move forward by n steps (max 5) ⏩   
                        ├────────────────────┼──────────────────────────────────────────────┤ 
                        |4.back <name> <n>   | Move backward by n steps (max 5) ⏪                    
                        ├────────────────────┼──────────────────────────────────────────────┤
                        |5. left             | Turn left 🔄  e.g. turn <name> left 
                        ├────────────────────┼──────────────────────────────────────────────┤ 
                        |6. right            | Turn right 🔁 e.g. turn <name> right                 
                        ├────────────────────┼──────────────────────────────────────────────┤
                        |7. look             | List visible objects 👀   
                        ├────────────────────┼──────────────────────────────────────────────┤ 
                        |8. state            | Show current robot status 📊                    
                        ├────────────────────┼──────────────────────────────────────────────┤
                        |9. fire             | Fire a shot (tank or sniper rules) 🔫   
                        ├────────────────────┼──────────────────────────────────────────────┤
                        |7. reload           | Refill your ammo to maximum 🔄💥   
                        ├────────────────────┼──────────────────────────────────────────────┤ 
                        |8. repair           | Restore your shields (takes time) 🛠️🛡️                    
                        ├────────────────────┼──────────────────────────────────────────────┤
                        |9. disconnect       | Disconnect the client completely 🫤   
                        ├────────────────────┼──────────────────────────────────────────────┤
                        |10. launch          | Launch another robot 🚀 e.g. <type> <name>
                        ├────────────────────┼──────────────────────────────────────────────┤
                                
                        """
        );

        handler.onComplete(new Response("OK", helpText));
    }

    private void handleLaunch(LaunchCommand command, String clientId, CompletionHandler completionHandler) {
        String robotName = command.robot.getName();
        clientRobots.putIfAbsent(clientId, new HashMap<>());
        Response response;

        if (clientRobots.get(clientId).size() >= 2) {
            response = new Response("ERROR", "Cannot launch more than 2 robots.");
        }

        Status status = world.addRobot(command.robot);
        if (status == Status.OK) {
            clientRobots.get(clientId).put(robotName, command.robot.getMake()); // Track the launched robot
        }

        response = switch (status) {
            case HitObstaclePIT ->  new Response("ERROR", command.robot.getName() + " fell into a pit and died.");
            case OK -> new Response("OK", "Launched " + command.robot.getName() + " into the world");
            case ExistingName -> new Response("ERROR", "Robot with the same name already exists");
            case OutOfBounds -> new Response("ERROR", "Failed to launch " + command.robot.getName() + " because it crashed outside of the world");
            case HitObstacle -> new Response("ERROR", "Failed to launch " + command.robot.getName() + " because it hit an obstacle");
        };

        if (status == Status.OK) {
            JSONObject data = new JSONObject();

            data.put("position", new JSONArray().put(command.robot.getX()).put(command.robot.getY()));
            data.put("visibility", this.world.getVisibility());
            data.put("reload", this.world.getReloadTime());
            data.put("repair", this.world.getShieldRepairTime());
            data.put("shields", this.world.getMaxShieldStrength());
            response.object.put("data", data);
        }

        world.stateForRobot(command.robot, response);

        completionHandler.onComplete(response);
    }

    private void handleMove(MoveCommand command, CompletionHandler handler) {
        if (command.arguments.length != 3) {
            handler.onComplete(new Response("ERROR", "Invalid move command format. Use '<direction> <robotName> <steps>'."));
            return;
        }

        String direction = command.arguments[0].toLowerCase();
        String robotName = command.arguments[1];
        int steps = parseSteps(command.arguments[2]);

        if (!direction.equals("forward") && !direction.equals("back")) {
            handler.onComplete(new Response("ERROR", "Invalid move direction. Use 'forward' or 'back'."));
            return;
        }

        if (steps < 1 || steps > 5) {
            handler.onComplete(new Response("ERROR", "You can only move a maximum of 5 steps."));
            return;
        }

        Robot robot = world.findRobot(robotName);
        if (robot == null) {
            handler.onComplete(new Response("ERROR", "Robot not found."));
            return;
        }
        if (robot.status == Robot.RobotStatus.Dead) {
            handler.onComplete(new Response("ERROR", robot.getName() + " is DEAD and cannot move."));
            return;
        }

        if (robot.status == Robot.RobotStatus.Reload) {
            handler.onComplete(new Response("ERROR", robot.getName() + " is reloading and cannot move"));
            return;
        }

        if (robot.status == Robot.RobotStatus.Repair) {
            handler.onComplete(new Response("ERROR", robot.getName() + " is repairing and cannot move"));
            return;
        }

        Status status = Status.OK;
        Position previousPosition = new Position(robot.getX(), robot.getY());

        switch (direction) {
            case "forward":
                for (int i = 0; i < steps; i++) {
                    robot.moveForward(1);
                    status = world.isPositionValid(robot.getPosition());

                    if (status == Status.HitObstaclePIT) {
                        robot.status = Robot.RobotStatus.Dead;
                        Response response = new Response("ERROR", robot.getName() + " fell into a pit and died.");
                        world.stateForRobot(robot, response);
                        handler.onComplete(response);
                        return;
                    }

                    if (status == Status.HitObstacle) {
                        robot.setPosition(previousPosition.getX(), previousPosition.getY());
                        Response response = new Response("ERROR", robot.getName() + " hit an obstacle.");
                        world.stateForRobot(robot, response);
                        handler.onComplete(response);
                        return;
                    }

                    if (status == Status.OutOfBounds) {
                        robot.status = Robot.RobotStatus.Dead;
                        Response response = new Response("ERROR", robot.getName() + " fell off the world.");
                        world.stateForRobot(robot, response);
                        handler.onComplete(response);
                        return;
                    }
                }
                break;
            case "back":
                for (int i = 0; i < steps; i++) {
                    robot.moveBackward(1);
                    status = world.isPositionValid(robot.getPosition());

                    if (status == Status.HitObstaclePIT) {
                        robot.status = Robot.RobotStatus.Dead;
                        Response response = new Response("ERROR", robot.getName() + " fell into a pit and died.");
                        world.stateForRobot(robot, response);
                        handler.onComplete(response);
                        return;
                    }

                    if (status == Status.HitObstacle) {
                        robot.setPosition(previousPosition.getX(), previousPosition.getY());
                        Response response = new Response("ERROR", robot.getName() + " hit an obstacle.");
                        world.stateForRobot(robot, response);
                        handler.onComplete(response);
                        return;
                    }

                    if (status == Status.OutOfBounds) {
                        robot.status = Robot.RobotStatus.Dead;
                        Response response = new Response("ERROR", robot.getName() + " fell off the world.");
                        world.stateForRobot(robot, response);
                        handler.onComplete(response);
                        return;
                    }
                }
                break;
            default:
                handler.onComplete(new Response("ERROR", "Invalid move command."));
        }

        Response response = new Response("OK", "Moved " + robot.getName() + " to [" + robot.getPosition().getX() + "," + robot.getPosition().getY() + "]");
        world.stateForRobot(robot, response);

        handler.onComplete(response);
    }


    private void handleState(StateCommand command, String robotName, CompletionHandler completionHandler) {
        Robot robot = world.findRobot(robotName);
        if (robot != null) {
            String message = "\n" +
                    "State for " + robotName + " 🤖:" +
                    "\n" +
                    " 🌎 Position: [" + robot.getX() + "," + robot.getY() + "]" +
                    "\n" +
                    " 🧭 Direction: " + robot.getDirection().getDirection().symbolForDirection() +
                    "\n" +
                    " 🛡️ Shields: " + robot.getShields() +
                    "\n" +
                    " 🔫 Shots: " + robot.getShots() +
                    "\n" +
                    " 📋 Status: " + robot.status.toString().toUpperCase() +
                    "\n";

            Response response = new Response("OK", message);
            world.stateForRobot(robot, response);

            completionHandler.onComplete(response);
        } else {
             completionHandler.onComplete(new Response("ERROR", "Could not find robot: " + robotName));
        }
    }

    private void handleLook(LookCommand command, String robotName, CompletionHandler completionHandler) {
        if (robotName == null || robotName.isBlank()) {
            // Get the first robot in the world (if any) and use its name
            List<Robot> robots = world.getRobots();
            if (robots.isEmpty()) {
                 completionHandler.onComplete(new Response("ERROR", "No robots available in the world."));
                 return;
            }

            robotName = robots.getFirst().getName();
        }

        Robot robot = world.findRobot(robotName);

        if (robot == null) {
            completionHandler.onComplete(new Response("ERROR", "Could not find robot: " + robotName));
            return;
        }

        completionHandler.onComplete(visibilityHandler.lookAround(robot));
    }

    private void handleOrientation(OrientationCommand command, String robotName, CompletionHandler completionHandler) {
        Robot robot = world.findRobot(robotName);
        if (robot != null) {
            String direction = robot.orientation(); // Get the current direction
            completionHandler.onComplete(new Response("OK", robot.getName() + " is facing " + direction + "."));
        } else {
            completionHandler.onComplete(new Response("ERROR", "Could not find robot: " + robotName));
        }
    }

    private void handleTurn(TurnCommand turnCommand, String robotName, CompletionHandler completionHandler) {
        Response response;

        if (turnCommand.arguments.length > 0) {
            String directionInput = turnCommand.arguments[0].toLowerCase();

            Robot robot = world.findRobot(robotName);
            if (robot != null) {
                if (robot.status == Robot.RobotStatus.Reload) {
                    response = new Response("ERROR", robot.getName() + " is reloading and cannot turn");
                }

                if (robot.status == Robot.RobotStatus.Repair) {
                    response = new Response("ERROR", robot.getName() + " is repairing and cannot turn");
                }

                response = switch (directionInput) {
                    case "left" -> {
                        robot.turnLeft();
                        yield new Response("OK", robot.getName() + " turned left to " + robot.orientation());
                    }
                    case "right" -> {
                        robot.turnRight();
                        yield new Response("OK", robot.getName() + " turned right to " + robot.orientation());
                    }
                    default -> new Response("ERROR", "Invalid direction. Use 'left' or 'right'.");
                };

                world.stateForRobot(robot, response);
            } else {
                response = new Response("ERROR", "Robot not found: " + robotName);
            }
        } else {
            response = new Response("ERROR", "Missing direction for turn command.");
        }

        completionHandler.onComplete(response);
    }

    private void handleFire(Robot robot, CompletionHandler completionHandler) {
        robot = this.world.findRobot(robot.getName());

        if (robot == null) {
            completionHandler.onComplete(new Response("ERROR", "Could not find robot: " + robot.getName()));
            return;
        }

        if (robot.getShots() <= 0) {
            completionHandler.onComplete(new Response("ERROR", "You have no shots remaining."));
            return;
        }

        if (robot.status == Robot.RobotStatus.Reload) {
            completionHandler.onComplete(new Response("ERROR", robot.getName() + " is reloading and cannot fire"));
            return;
        }

        if (robot.status == Robot.RobotStatus.Repair) {
            completionHandler.onComplete(new Response("ERROR", robot.getName() + " is repairing and cannot fire"));
            return;
        }

        Position robotP = robot.getPosition();
        String direction = robot.orientation();

        int dx = 0;
        int dy = 0;

        switch (direction) {
            case "NORTH" -> dy = 1;
            case "SOUTH" -> dy = -1;
            case "EAST" -> dx = 1;
            case "WEST" -> dx = -1;
        }

        // Determine the range based on the robot type and remaining shots
        int range;
        if ("tank".equalsIgnoreCase(robot.getMake())) {
            range = switch (robot.getShots()) {
                case 3 -> 3;
                case 2 -> 4;
                default -> 5;
            };
        } else { // assuming it's a sniper
            range = switch (robot.getShots()) {
                case 10 -> 1;
                case 9 -> 2;
                case 8 -> 3;
                case 7 -> 4;
                case 6 -> 5;
                case 5 -> 6;
                case 4 -> 7;
                case 3 -> 8;
                case 2 -> 9;
                default -> 10;
            };
        }

        // Reduce the shot count
        robot.setShots(robot.getShots() - 1);
        Robot hitRobot = null;
        int distance = 0;

        // Check for hits
        for (int step = 1; step <= range; step++) {
            Position checkPos = new Position(robotP.getX() + step * dx, robotP.getY() + step * dy);

            for (Robot otherRobot : world.getRobots()) {
                if (!otherRobot.getName().equals(robot.getName()) && otherRobot.getPosition().equals(checkPos)) {
                    hitRobot = otherRobot;
                    break;
                }
            }

            if (hitRobot != null) {
                distance = step;
                break; // Stop checking after a hit
            }
        }

        // Handle the result of the shot
        if (hitRobot == null) {
            completionHandler.onComplete(new Response("OK", "You have missed 🥲!"));
            return;
        }

        // Apply damage
        hitRobot.takeHit(); // This reduces shield strength or kills the robot

        Response response;
        Response hitRobotResponse = new Response("OK", "I got hit");

        if (hitRobot.isDead()) {
           response = new Response("OK", "You have hit 💥 " + hitRobot.getName() + "! It is now destroyed.");
        } else {
            response = new Response("OK", "You have hit 💥 " + hitRobot.getName() + "! Remaining shield: " + hitRobot.getShields());
        }

        JSONObject data = new JSONObject();

        world.stateForRobot(hitRobot, hitRobotResponse);
        world.stateForRobot(robot, response);

        data.put("message", "Hit");
        data.put("distance", distance);
        data.put("robot", hitRobot.getName());
        data.put("state", hitRobotResponse.object.getJSONObject("state"));

        response.object.put("data", data);

        completionHandler.onComplete(response);
    }

    private void handleShutdown(ShutdownCommand command, CompletionHandler completionHandler) {
        String robotName = command.robot.getName();
        clientRobots.remove(robotName);
        completionHandler.onComplete(world.removeRobot(robotName));
    }

    private void handleRepair(RepairCommand command, CompletionHandler completionHandler) {
        Robot robot = world.findRobot(command.robot.getName());
        if (robot == null) {
            completionHandler.onComplete(new Response("ERROR", "Robot not found: " + command.robot.getName()));
            return;
        }

        // Check if the robot is already repairing
        if (robot.isRepairing()) {
            completionHandler.onComplete(new Response("ERROR", robot.getName() + " is already repairing."));
            return;
        }

        robot.setRepairing(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                robot.setShields(world.getMaxShieldStrength()); // Repair to max shields
                robot.setRepairing(false);
                Response response = new Response("OK", robot.getName() + " has finished repairing");
                world.stateForRobot(robot, response);

                completionHandler.onComplete(response);
            }
        }, world.getShieldRepairTime() * 1000L); // Repair time in milliseconds

        completionHandler.onComplete(new Response("OK", robot.getName() + " is now repairing."));
    }

    private void handleReload(ReloadCommand command, CompletionHandler completionHandler) {
        Robot robot = world.findRobot(command.robot.getName());

        if (robot == null) {
            completionHandler.onComplete(new Response("ERROR", "Robot not found: " + command.robot.getName()));
            return;
        }

        if (robot.isReloading()) {
            completionHandler.onComplete(new Response("ERROR", robot.getName() + " is already reloading."));
            return;
        }

        robot.setReloading(true);

        completionHandler.onComplete(new Response("OK", robot.getName() + " is now reloading."));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                robot.setReloading(false);
                robot.setShots(robot.getMaxShots());
                Response response = new Response("OK", robot.getName() + " is done.");

                world.stateForRobot(robot, response);
                completionHandler.onComplete(response);
            }
        }, world.getReloadTime() * 1000L); // Repair time in milliseconds
    }

    private int parseSteps(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}