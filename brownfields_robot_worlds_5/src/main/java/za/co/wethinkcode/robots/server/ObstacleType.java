package za.co.wethinkcode.robots.server;

public enum ObstacleType {
    MOUNTAIN,
    LAKE,
    PIT;

    public String getSymbol() {
        return switch (this) {
            case MOUNTAIN -> "🌋";
            case LAKE -> "🌊";
            case PIT -> "⚫️";
        };
    }
}

