package za.co.wethinkcode.robots.server;

import org.json.JSONObject;
import java.util.*;
import java.util.Random;

import za.co.wethinkcode.robots.client.Position;
import za.co.wethinkcode.robots.Robot;
import za.co.wethinkcode.robots.commands.*;
import za.co.wethinkcode.robots.handlers.*;

public class World {
    private static final World INSTANCE = new World();
    private final CommandHandler commandHandler;
    private int width;
    private int height;
    private int halfWidth;
    private int halfHeight;
    private int maxShieldStrength;
    private int shieldRepairTime;
    private int reloadTime;
    private int visibility;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Robot> robots = new ArrayList<>();

    public static World getInstance() {
        return INSTANCE;
    }

    public World() {
        ConfigLoader configLoader = new ConfigLoader();
        configLoader.applyConfigToWorld(this, "config.properties");
        this.commandHandler = new CommandHandler(this);
        generateDefaultObstacles();
        displayWorld();
    }

    public World(int width, int height) {
        setDimensions(width, height);
        this.visibility = this.halfWidth;
        this.commandHandler = new CommandHandler(this);
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        this.halfWidth = Math.max(1, width / 2);
        this.halfHeight = Math.max(1, height / 2);
    }

    public void setDefaultDimensions() {
        this.width = 100;
        this.height = 50;
        this.halfWidth = Math.max(1, width / 2);
        this.halfHeight = Math.max(1, height / 2);
    }

    public void setWorldProperties(int shieldRepairTime, int reloadTime, int maxShieldStrength, int visibility) {
        this.shieldRepairTime = shieldRepairTime;
        this.reloadTime = reloadTime;
        this.maxShieldStrength = shieldRepairTime;
        this.visibility = visibility;
    }

    public void setDefaultWorldProperties() {
        int visibility = (int) (this.getWidth() * 0.30);

        this.shieldRepairTime = 5;
        this.reloadTime = 3;
        this.maxShieldStrength = 10;
        this.visibility = visibility;
    }

    public void execute(Command command, String clientId, CommandHandler.CompletionHandler completionHandler) {
        commandHandler.handle(command, clientId, completionHandler);
    }

    public void displayWorld() {
       System.out.println(displayViewport(-halfWidth, halfHeight, width, height));
    }

    public String displayViewport(int originX, int originY, int viewWidth, int viewHeight) {
        StringBuilder sb = new StringBuilder();
        String[][] grid = new String[viewHeight][viewWidth];

        for (int i = 0; i < viewHeight; i++) {
            for (int j = 0; j < viewWidth; j++) {
                int worldX = originX + j;
                int worldY = originY - i;

                if (isWithinBounds(worldX, worldY)) {
                    grid[i][j] = "◾️";
                } else {
                    grid[i][j] = "  ";
                }
            }
        }

        for (Obstacle obstacle : obstacles) {
            for (int y = obstacle.getY(); y < obstacle.getMaxY(); y++) {
                for (int x = obstacle.getX(); x < obstacle.getMaxX(); x++) {
                    if (x >= originX && x < originX + viewWidth &&
                            y <= originY && y > originY - viewHeight) {
                        int gx = x - originX;
                        int gy = originY - y;

                        if (gy <= viewHeight && gx <= viewWidth) {
                            grid[gy][gx] = obstacle.type().getSymbol();
                        }
                    }
                }
            }
        }

        for (Robot robot : robots) {
            int x = robot.getX();
            int y = robot.getY();
            if (x >= originX && x < originX + viewWidth &&
                    y <= originY && y > originY - viewHeight) {
                int gx = x - originX;
                int gy = originY - y;

                if (gy <= viewHeight && gx <= viewWidth) {
                    grid[gy][gx] = "🤖";
                }
            }
        }

        for (int i = 0; i < viewHeight; i++) {
            for (int j = 0; j < viewWidth; j++) {
                sb.append(grid[i][j]).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String displayDirectionalCross(Robot robot, int maxDistance) {
        StringBuilder sb = new StringBuilder();

        int robotX = robot.getX();
        int robotY = robot.getY();

        // Calculate the actual limits based on world bounds and maxDistance
        int minX = Math.max(robotX - maxDistance, -halfWidth);
        int maxX = Math.min(robotX + maxDistance, halfWidth);
        int minY = Math.max(robotY - maxDistance, -halfHeight);
        int maxY = Math.min(robotY + maxDistance, halfHeight);

        // Grid dimensions
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        String[][] grid = new String[height][width];

        // Fill grid with spaces initially
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = "  ";
            }
        }

        // Fill cross lines with base tile ◾️
        // The vertical line (X == robotX)
        if (robotX >= minX && robotX <= maxX) {
            int col = robotX - minX;
            for (int row = 0; row < height; row++) {
                grid[row][col] = "◾️";
            }
        }

        // The horizontal line (Y == robotY)
        if (robotY >= minY && robotY <= maxY) {
            int row = maxY - robotY; // Y decreases down rows
            for (int col = 0; col < width; col++) {
                grid[row][col] = "◾️";
            }
        }

        // Place obstacles on cross lines within bounds
        for (Obstacle obstacle : obstacles) {
            for (int y = obstacle.getY(); y < obstacle.getMaxY(); y++) {
                for (int x = obstacle.getX(); x < obstacle.getMaxX(); x++) {
                    if (x < minX || x > maxX || y < minY || y > maxY) continue;

                    if (x == robotX) {
                        int row = maxY - y;
                        int col = x - minX;
                        grid[row][col] = obstacle.type().getSymbol();
                    } else if (y == robotY) {
                        int row = maxY - y;
                        int col = x - minX;
                        grid[row][col] = obstacle.type().getSymbol();
                    }
                }
            }
        }

        // Place other robots on cross lines within bounds
        for (Robot other : robots) {
            if (other.equals(robot)) continue;

            int x = other.getX();
            int y = other.getY();

            if (x < minX || x > maxX || y < minY || y > maxY) continue;

            if (x == robotX || y == robotY) {
                int row = maxY - y;
                int col = x - minX;
                grid[row][col] = "🤖";
            }
        }

        // Place current robot
        if (robotX >= minX && robotX <= maxX && robotY >= minY && robotY <= maxY) {
            int row = maxY - robotY;
            int col = robotX - minX;
            grid[row][col] = "🤖";
        }

        // Build output string
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                sb.append(grid[i][j]).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public Status isPositionValid(Position position) {
        // Check if the position is within world bounds
        if (!isWithinBounds(position.getX(), position.getY())) {
            return Status.OutOfBounds;
        }

        // Check for obstacle collisions
        for (Obstacle obstacle : obstacles) {
            if (obstacle.contains(position)) {
                if (obstacle.type() == ObstacleType.PIT) {
                    return Status.HitObstaclePIT;
                } else {
                    return Status.HitObstacle;
                }
            }
        }

        return Status.OK; // Position is valid
    }

    public boolean addObstacle(Obstacle obstacle) {
        boolean overlaps = false;

        for (Obstacle existing : obstacles) {
                if (existing.overlaps(obstacle)) {
                overlaps = true;
                break;
            }
        }

       boolean withinBounds = isWithinBounds(obstacle.getMaxX(), obstacle.getMaxY());

        if (!overlaps && withinBounds) {
            obstacles.add(obstacle);
            return true;
        }

        return false;
    }

    public Status addRobot(Robot robot) {
        for (Robot nextRobot : robots) {
            if (nextRobot.getName().equals(robot.getName())) {
                return Status.ExistingName; // Robot with the same name already exists
            }
        }

        Random random = new Random();

        int randomX = random.nextInt((halfWidth * 2) + 1) - halfWidth;
        int randomY = random.nextInt((halfHeight * 2) + 1) - halfHeight;
        Status status = isPositionValid(new Position(randomX, randomY));

        if (status == Status.OK) {
            robot.setPosition(randomX, randomY);
            this.robots.add(robot);

            return Status.OK;
        }

        return status;
    }

    public Robot findRobot(String name) {
        for (Robot robot : robots) {
            if (robot.getName().equals(name)) {
                return robot;
            }
        }
        return null;
    }

    public Response removeRobot(String robotName) {
        Robot robot = findRobot(robotName);
        if (robot == null) {
            return new Response("ERROR", "Robot not found.");
        }

        robots.remove(robot);
        return new Response("OK", "Removed robot " + robotName + " from the world.");
    }

    public void stateForRobot(Robot robot, Response response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("position", "[" + robot.getX() + ", " + robot.getY() + "]");
        jsonObject.put("direction", robot.orientation().toUpperCase());
        jsonObject.put("shields", robot.getShields());
        jsonObject.put("shots", robot.getShots());
        jsonObject.put("status", robot.status.toString().toUpperCase());
        response.object.put("state", jsonObject);
    }

    public String getAllRobotsInfo() {
        if (robots.isEmpty()) {
            return "No robots in the world.";
        }
        StringBuilder sb = new StringBuilder("Robots in the world:");
        for (Robot robot : robots) {
            Response response = new Response("", "State for " + robot.getName());

            stateForRobot(robot, response);

            sb.append("\n- ").append(robot.getName()).append(" ").append(response.toJSONString());
        }
        return sb.toString();
    }

    public String getFullWorldState() {
        StringBuilder sb = new StringBuilder("World State:\n");
        sb.append("Dimensions: ").append(width).append(" x ").append(height).append("\n");
        sb.append("Obstacles (").append(obstacles.size()).append("):\n");
        for (Obstacle obs: obstacles) {
            sb.append(" - \n").append(obs.toString());
        }
        sb.append("- \n Robots (").append(robots.size()).append("):\n");
        for (Robot robot : robots) {
            sb.append("- ").append(robot.getName())
                    .append(" at (").append(robot.getX()).append(", ").append(robot.getY()).append(")\n");
        }
        return sb.toString();
    }

    public List<Robot> getRobots() {
        return robots;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public int getHalfWidth() {
        return halfWidth;
    }

    public int getHalfHeight() {
        return halfHeight;
    }

    public int getMaxShieldStrength() {
        return maxShieldStrength;
    }

    public int getReloadTime() {
        return reloadTime;
    }

    public int getShieldRepairTime() {
        return shieldRepairTime;
    }

    public int getVisibility() {
        return visibility;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void generateDefaultObstacles() {
        int obstacleCount = (int) ((height + width) * 0.30);

        for (int i = 0; i <= obstacleCount; i++) {
            Random random = new Random();
            boolean added = false;

            while (!added) {
                int randomWidth = random.nextInt(1, 4);
                int randomHeight = random.nextInt(1, 4);
                int randomX = random.nextInt(-halfWidth, halfWidth);
                int randomY = random.nextInt(-halfHeight, halfHeight);

                ObstacleType type = ObstacleType.values()[random.nextInt(ObstacleType.values().length)];
                Obstacle obstacle = new Obstacle(type, randomX, randomY, randomWidth, randomHeight);

                if (addObstacle(obstacle)) {
                    added = true;
                }
            }
        }
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= -halfWidth && x <= halfWidth  && y >= -halfHeight && y <= halfHeight;
    }
}