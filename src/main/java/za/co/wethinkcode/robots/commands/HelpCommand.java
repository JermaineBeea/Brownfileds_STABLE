package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class HelpCommand extends Command {
    public HelpCommand(Robot robot, String[] arguments) {
        super(robot, arguments);
    }

    @Override
    public String commandName() {
        return "help";
    }
}
