package za.co.wethinkcode.robots.commands;

import org.json.JSONArray;
import org.json.JSONObject;
import za.co.wethinkcode.robots.Robot;

import java.util.Arrays;

/**
 * Abstract representation of a command sent to robots.
 * Defines interface and common behavior for all commands.
 */
public abstract class Command {
    public Robot robot;
    public String[] arguments;

    public Command(Robot robot, String[] arguments) {
        this.robot = robot;
        this.arguments = arguments;
    }

    public static boolean isValidCommand(String command) {
        return switch (command.toLowerCase()) {
            case "forward", "back", "turn", "look", "state", "launch", "dump", "orientation", "shutdown",
                 "disconnect", "fire", "repair", "reload", "help" -> true;
            default -> false;
        };
    }

    public String toJSONString() {
        JSONObject json = new JSONObject();
        json.put("command", commandName().toLowerCase());
        json.put("arguments", arguments);

        if (robot != null) {
            json.put("robot", robot.getName());
        }

        return json.toString();
    }

    public static Command fromJSON(JSONObject json) {
        String command = json.getString("command").toLowerCase();
        if (command.equals("disconnect")) {
            return new DisconnectCommand(); // handle disconnect command separately
        }

        String robotName = json.getString("robot");
        JSONArray jsonArgs = json.getJSONArray("arguments");
        String[] args = new String[jsonArgs.length()];

        for (int i = 0; i < jsonArgs.length(); i++) {
            args[i] = jsonArgs.getString(i);
        }

        return switch (command) {
            case "repair" -> new RepairCommand(new Robot(robotName), args);
            case "reload" -> new ReloadCommand(new Robot(robotName), args);
            case "help" -> new HelpCommand(new Robot(robotName), new String[]{});
            case "dump" -> new DumpCommand(new Robot(robotName), new String[]{});
            case "look" -> new LookCommand(new Robot(robotName), new String[]{});
            case "state" -> new StateCommand(new Robot(robotName), new String[]{});
            case "launch" -> {
                // Accepts at least one argument (robot type), but can accept more (e.g. position)
                if (args.length < 1) {
                    throw new IllegalArgumentException("Missing robot type for launch command.");
                }
                yield new LaunchCommand(new Robot(robotName, args[0]), args);
            }
            case "forward" -> new MoveCommand(new Robot(robotName), "forward", args);
            case "back" -> new MoveCommand(new Robot(robotName), "back", args);
            case "turn" -> new TurnCommand(new Robot(robotName), args);
            case "orientation" -> new OrientationCommand(new Robot(robotName));
            case "shutdown", "off" -> new ShutdownCommand(new Robot(robotName), new String[]{});
            case "fire" -> new FireCommand(new Robot(robotName), args);
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
    }

    public static Command fromInput(String input, String robotName) {
        String[] tokens = input.trim().split(" ");
        String command = tokens[0];

        if (tokens.length == 0 || tokens[0].isEmpty()) {
            throw new IllegalArgumentException("Invalid or empty command ");
        }

        String robot = "";
        String[] args = new String[]{};

        switch (command.toLowerCase()) {
            case "forward":
            case "back":
                if (tokens.length >= 3) {
                    robot = tokens[1];
                    args = new String[]{tokens[0], tokens[1], tokens[2]}; // direction, robot, steps
                } else if (robotName != null) {
                    robot = robotName;
                    args =  new String[]{tokens[0], robotName, tokens[1]};
                }
                break;
            case "turn":
                robot = tokens.length >= 3 ? tokens[1] : robotName;
                args = tokens.length >= 3 ? new String[]{tokens[2]} : new String[]{tokens[1]}; // only need turn direction
                break;
            case "state":
            case "look":
            case "orientation":
            case "fire":
            case "repair":
            case "reload":
            case "off":
                robot = tokens.length > 1 ? tokens[1] : robotName;
                args = new String[]{};
                break;
            case "launch":
                robot = tokens.length >= 3 ? tokens[2] :robotName;
                String robotType = tokens.length >= 2 ? tokens[1] : "";
                args = new String[]{robotType};
                break;
        }

        return fromJSON(new JSONObject()
                .put("robot", robot)
                .put("command", command)
                .put("arguments", new JSONArray(Arrays.asList(args))));
    }

    public abstract String commandName();
}