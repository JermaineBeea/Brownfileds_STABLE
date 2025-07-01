package za.co.wethinkcode.robots.commands;

public class DisconnectCommand extends Command {
    public DisconnectCommand() {
        super(null, new String[0]);
    }

    @Override
    public String commandName() {
        return "disconnect";
    }
}