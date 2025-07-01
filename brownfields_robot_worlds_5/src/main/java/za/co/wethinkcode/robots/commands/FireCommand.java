package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class FireCommand extends Command {
    public FireCommand(Robot robot, String[] arguments) {
        super(robot, arguments);
    }

    @Override
    public String commandName() {
        return "fire";
    }
}