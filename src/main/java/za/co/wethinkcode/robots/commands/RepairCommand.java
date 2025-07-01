package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class RepairCommand extends Command {
    public RepairCommand(Robot robot, String[] arguments) {
        super(robot, arguments);
    }

    @Override
    public String commandName() {
        return "repair";
    }
}
