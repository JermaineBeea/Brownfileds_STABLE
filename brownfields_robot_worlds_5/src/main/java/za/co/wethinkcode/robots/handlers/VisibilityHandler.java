package za.co.wethinkcode.robots.handlers;

import org.json.JSONObject;
import za.co.wethinkcode.robots.Robot;
import za.co.wethinkcode.robots.client.Direction;
import za.co.wethinkcode.robots.server.Obstacle;
import za.co.wethinkcode.robots.server.ObstacleType;
import za.co.wethinkcode.robots.server.Response;
import za.co.wethinkcode.robots.server.World;

import java.util.*;

/**
 * Handles visibility logic for robots in the world.
 * Determines which objects (robots, obstacles, or world edges) are visible from a given robot's position
 * in each cardinal direction within a defined viewing range. Filters and formats data for look command responses.
 */
public class VisibilityHandler {
    private final List<Robot> robots;
    private final List<Obstacle> obstacles;
    private final int halfWidth;
    private final int halfHeight;
    private final int maxDistance;
    private final World world;

    public VisibilityHandler(List<Robot> robots, List<Obstacle> obstacles, int halfWidth, int halfHeight, int maxDistance, World world) {
        this.robots = robots;
        this.obstacles = obstacles;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.maxDistance = maxDistance;
        this.world = world;
    }

    public Response lookAround(Robot robot) {
        List<Map<String, Object>> visibleObjects = new ArrayList<>();
        int maxDistance = this.maxDistance;

        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("\nLooking around for ").append(robot.getName()).append(" 🤖:");
        messageBuilder.append("\n  Objects").append(":");

        for (Direction.CardinalDirection direction : Direction.CardinalDirection.values()) {
            List<Map<String, Object>> map = checkVisibleObjects(robot, direction, maxDistance);

            for (Map<String, Object> dictionary : map) {
                String type = (String) dictionary.get("type");
                Direction.CardinalDirection objectDirection = (Direction.CardinalDirection)dictionary.get("direction");
                int distance = (int) dictionary.get("distance");
                String directionSymbol = objectDirection.symbolForDirection();

                if (type.equalsIgnoreCase("obstacle")) {
                    messageBuilder.append("\n   🚧 Found an obstacle nearby!");
                } else if (type.equalsIgnoreCase("robot")) {
                    messageBuilder.append("\n   🤖 Found another robot nearby!");
                } else {
                    messageBuilder.append("\n   🧭 Found the edge of the world");
                }

                messageBuilder.append("\n       🧭 Direction ").append(directionSymbol);
                messageBuilder.append("\n       🦶 Steps ").append(distance);

                visibleObjects.add(createObjectMap(type, objectDirection, distance));
            }
        }

        if (visibleObjects.isEmpty()) {
            messageBuilder.append("\n   🥲 Could not find anything, try moving around to find more objects");
        }

        String snapshot = world.displayDirectionalCross(robot, maxDistance);

        messageBuilder.append("\nHere is a snapshot of you can see:").append("\n").append(snapshot);

        Response response = new Response("OK", messageBuilder.toString());
        response.object.put("objects", visibleObjects);
        return response;
    }

    private List<Map<String, Object>> checkVisibleObjects(Robot robot, Direction.CardinalDirection direction, int maxDistance) {
        List<Map<String, Object>> objects = new ArrayList<>();
        List<Map<String, Object>> map = new ArrayList<>();

        int dx = 0, dy = 0;
        switch (direction) {
            case EAST -> dx = 1;
            case WEST -> dx = -1;
            case NORTH -> dy = 1;
            case SOUTH -> dy = -1;
        }

        int startX = robot.getX();
        int startY = robot.getY();

        for (int step = 1; step <= maxDistance; step++) {
            int x = startX + dx * step;
            int y = startY + dy * step;

            for (Obstacle obs : obstacles) {
                if (mapContainsObject(map, obs)) {
                    continue;
                }

                boolean found = false;

                for (int obX = obs.getX(); obX < obs.getMaxX(); obX++) {
                    for (int obY = obs.getY(); obY < obs.getMaxY(); obY++) {
                        if (obY == y && obX == x) {
                            found = true;
                        }
                    }
                }

                if (found) {
                    Map<String, Object> object = new HashMap<>();
                    object.put("object", obs);
                    object.put("distance", step);
                    object.put("direction", direction);
                    object.put("type", "OBSTACLE");

                    map.add(object);
                }
            }

            for (Robot nextRobot : robots) {
                if (!nextRobot.equals(robot) && !mapContainsObject(map, nextRobot)) {
                    if (nextRobot.getX() == x && nextRobot.getY() == y) {
                        Map<String, Object> object = new HashMap<>();
                        object.put("object", nextRobot);
                        object.put("distance", step);
                        object.put("direction", direction);
                        object.put("type", "ROBOT");
                        map.add(object);
                    }
                }
            }

            if (isAtEdge(x, y, direction)) {
                if (!mapContainsObject(map, direction)) {
                    Map<String, Object> object = new HashMap<>();
                    object.put("object", direction);
                    object.put("distance", step);
                    object.put("direction", direction);
                    object.put("type", "EDGE");

                    map.add(object);
                }
            }
        }

        // Sort by the closest objects
        map.sort(Comparator.comparingInt(o -> (int) o.get("distance")));

        for (Map<String, Object> dictionary : map) {
            Object o = dictionary.get("object");

            if (o instanceof Obstacle obstacle) {
                if (obstacle.type() == ObstacleType.MOUNTAIN) {
                    // Break since once we hit a mountain we can't see the other objects behind it
                    objects.add(dictionary);
                    break;
                }
            }

            objects.add(dictionary);
        }

        return objects;
    }

    private  boolean mapContainsObject(List<Map<String, Object>> map, Object object) {
        boolean contains = false;

        for (Map<String, Object> dictionary : map) {
            Object o = dictionary.get("object");

            if (o.equals(object)) {
                contains = true;
                break;
            }
        }

        return contains;
    }

    private Map<String, Object> createObjectMap(String type, Direction.CardinalDirection direction, int distance) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("direction", direction);
        map.put("distance", distance);
        return map;
    }

    private boolean isAtEdge(int x, int y, Direction.CardinalDirection dir) {
        return switch (dir) {
            case NORTH -> y >= halfHeight;
            case SOUTH -> y <= -halfHeight;
            case EAST -> x >= halfWidth;
            case WEST -> x <= -halfWidth;
        };
    }
}
