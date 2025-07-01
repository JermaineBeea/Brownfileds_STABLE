package za.co.wethinkcode.robots.commands;

import za.co.wethinkcode.robots.Robot;

public class OrientationCommand extends Command {
    public OrientationCommand(Robot robot) {
        super(robot, new String[]{});
    }

    @Override
    public String commandName() {
        return "orientation";
    }
}