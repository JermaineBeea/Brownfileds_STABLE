package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class LaunchCommand extends Command {
    public LaunchCommand(Robot robot, String[] arguments) {
        super(robot, arguments);
    }

    @Override
    public String commandName() {
        return "launch";
    }
}
