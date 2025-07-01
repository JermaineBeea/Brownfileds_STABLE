package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class MoveCommand extends Command {
    private final String direction;

    public MoveCommand(Robot robot, String direction, String[] arguments) {
        super(robot, arguments);
        this.direction = direction.toLowerCase();
    }

    @Override
    public String commandName() {
        return direction;
    }
}