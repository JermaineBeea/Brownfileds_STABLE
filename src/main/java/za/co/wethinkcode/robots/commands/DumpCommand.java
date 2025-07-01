package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class DumpCommand extends Command {
    public DumpCommand(Robot robot, String[] arguments) {
        super(robot, arguments);
    }

    @Override
    public String commandName() {
        return "dump";
    }

}