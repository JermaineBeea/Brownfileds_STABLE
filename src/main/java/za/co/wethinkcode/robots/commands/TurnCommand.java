package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class TurnCommand extends Command {
    public TurnCommand(Robot robot, String[] arguments) {
        super(robot, arguments);
    }

    @Override
    public String commandName() {
        return "turn";
    }
}