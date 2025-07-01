package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class ShutdownCommand extends Command{
    public ShutdownCommand (Robot robot, String[] arguments) {
        super(robot, arguments);
    }

    @Override
    public String commandName() {
        return "off";
    }
}