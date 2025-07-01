package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class LookCommand extends Command {
    public LookCommand(Robot robot, String[] arguments) {
        super(robot, arguments);
    }

    @Override
    public String commandName() {
        return "look";
    }
}

