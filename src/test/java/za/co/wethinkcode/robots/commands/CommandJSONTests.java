package za.co.wethinkcode.robots.commands;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandJSONTests {
    @Test
    public void testMovementCommands() {
        String input = "back 1";
        Command command = Command.fromInput(input, "hal");

        assertEquals(MoveCommand.class, command.getClass());
        assertEquals("back", command.commandName());
        assertEquals(3, command.arguments.length);
        assertEquals("back", command.arguments[0]);
        assertEquals("hal", command.arguments[1]);
        assertEquals("1", command.arguments[2]);

        input = "back hal 1";
        command = Command.fromInput(input, null);

        assertEquals(MoveCommand.class, command.getClass());
        assertEquals("back", command.commandName());
        assertEquals(3, command.arguments.length);
        assertEquals("back", command.arguments[0]);
        assertEquals("hal", command.arguments[1]);
        assertEquals("1", command.arguments[2]);

        input = "forward 1";
        command = Command.fromInput(input, "hal");

        assertEquals(MoveCommand.class, command.getClass());
        assertEquals("forward", command.commandName());
        assertEquals(3, command.arguments.length);
        assertEquals("forward", command.arguments[0]);
        assertEquals("hal", command.arguments[1]);
        assertEquals("1", command.arguments[2]);

        input = "forward hal 1";
        command = Command.fromInput(input, null);

        assertEquals(MoveCommand.class, command.getClass());
        assertEquals("forward", command.commandName());
        assertEquals(3, command.arguments.length);
        assertEquals("forward", command.arguments[0]);
        assertEquals("hal", command.arguments[1]);
        assertEquals("1", command.arguments[2]);
    }

    @Test
    public void testTurnCommand() {
        String input = "turn right";
        Command command = Command.fromInput(input, "hal");

        assertEquals(TurnCommand.class, command.getClass());
        assertEquals("turn", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(1, command.arguments.length);
        assertEquals("right", command.arguments[0]);

        input = "turn hal right";
        command = Command.fromInput(input, null);

        assertEquals(TurnCommand.class, command.getClass());
        assertEquals("turn", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(1, command.arguments.length);
        assertEquals("right", command.arguments[0]);

        input = "turn left";
        command = Command.fromInput(input, "hal");

        assertEquals(TurnCommand.class, command.getClass());
        assertEquals("turn", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(1, command.arguments.length);
        assertEquals("left", command.arguments[0]);

        input = "turn hal left";
        command = Command.fromInput(input, null);

        assertEquals(TurnCommand.class, command.getClass());
        assertEquals("turn", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(1, command.arguments.length);
        assertEquals("left", command.arguments[0]);
    }

    @Test
    public void testStateCommand() {
        String input = "state";
        Command command = Command.fromInput(input, "hal");

        assertEquals(StateCommand.class, command.getClass());
        assertEquals("state", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);

        input = "state hal";
        command = Command.fromInput(input, null);

        assertEquals(StateCommand.class, command.getClass());
        assertEquals("state", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);
    }

    @Test
    public void testLookCommand() {
        String input = "look";
        Command command = Command.fromInput(input, "hal");

        assertEquals(LookCommand.class, command.getClass());
        assertEquals("look", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);

        input = "look hal";
        command = Command.fromInput(input, null);

        assertEquals(LookCommand.class, command.getClass());
        assertEquals("look", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);
    }

    @Test
    public void testOrientationCommand() {
        String input = "orientation";
        Command command = Command.fromInput(input, "hal");

        assertEquals(OrientationCommand.class, command.getClass());
        assertEquals("orientation", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);

        input = "orientation hal";
        command = Command.fromInput(input, null);

        assertEquals(OrientationCommand.class, command.getClass());
        assertEquals("orientation", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);
    }

    @Test
    public void testFireCommand() {
        String input = "fire";
        Command command = Command.fromInput(input, "hal");

        assertEquals(FireCommand.class, command.getClass());
        assertEquals("fire", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);

        input = "fire hal";
        command = Command.fromInput(input, null);

        assertEquals(FireCommand.class, command.getClass());
        assertEquals("fire", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);
    }

    @Test
    public void testReloadCommand() {
        String input = "reload";
        Command command = Command.fromInput(input, "hal");

        assertEquals(ReloadCommand.class, command.getClass());
        assertEquals("reload", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);

        input = "reload hal";
        command = Command.fromInput(input, null);

        assertEquals(ReloadCommand.class, command.getClass());
        assertEquals("reload", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);
    }

    @Test
    public void testRepairCommand() {
        String input = "repair";
        Command command = Command.fromInput(input, "hal");

        assertEquals(RepairCommand.class, command.getClass());
        assertEquals("repair", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);

        input = "repair hal";
        command = Command.fromInput(input, null);

        assertEquals(RepairCommand.class, command.getClass());
        assertEquals("repair", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);
    }

    @Test
    public void testOffCommand() {
        String input = "off";
        Command command = Command.fromInput(input, "hal");

        assertEquals(ShutdownCommand.class, command.getClass());
        assertEquals("off", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);

        input = "off hal";
        command = Command.fromInput(input, null);

        assertEquals(ShutdownCommand.class, command.getClass());
        assertEquals("off", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(0, command.arguments.length);
    }

    @Test
    public void testDumpCommand() {
        String input = "dump";
        Command command = Command.fromInput(input, null);

        assertEquals(DumpCommand.class, command.getClass());
        assertEquals("dump", command.commandName());
    }

    @Test
    public void testLaunchCommand() {
        String input = "launch tank";
        Command command = Command.fromInput(input, "hal");

        assertEquals(LaunchCommand.class, command.getClass());
        assertEquals("launch", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals("tank", command.arguments[0]);

        input = "launch tank hal";
        command = Command.fromInput(input, null);

        assertEquals(LaunchCommand.class, command.getClass());
        assertEquals("launch", command.commandName());
        assertEquals("hal", command.robot.getName());
        assertEquals(1, command.arguments.length);
        assertEquals("tank", command.arguments[0]);

    }
}
