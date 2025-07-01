package za.co.wethinkcode.robots.handlers;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.Robot;
import za.co.wethinkcode.robots.client.Direction;
import za.co.wethinkcode.robots.server.Obstacle;
import za.co.wethinkcode.robots.server.ObstacleType;
import za.co.wethinkcode.robots.server.Response;
import za.co.wethinkcode.robots.server.World;

import static org.junit.jupiter.api.Assertions.*;

public class VisibilityHandlerTest {

    private World world;
    private Robot robot;

    @BeforeEach
    public void setUp() {
        world = new World(10, 10);
        robot = new Robot("Robot", "tank", 0, 0);
        world.addRobot(robot);
        robot.setPosition(0,0);
    }

    @Test
    public void testVisibility() {
        VisibilityHandler visibilityHandler = new VisibilityHandler(
                world.getRobots(),
                world.getObstacles(),
                world.getHalfWidth(),
                world.getHalfHeight(),
                world.getVisibility(),
                world
        );
        Response response = visibilityHandler.lookAround(robot);
        assertNotNull(response.object.get("objects"));
        assertTrue(response.object.getJSONArray("objects").length() >= 0);
    }

    @Test
    public void testVisibilityWithObstacle() {
        Obstacle obstacle = new Obstacle(ObstacleType.MOUNTAIN, 1, 0, 1, 1);
        world.addObstacle(obstacle); // Add obstacle to the world
        VisibilityHandler visibilityHandler = new VisibilityHandler(
                world.getRobots(),
                world.getObstacles(),
                world.getHalfWidth(),
                world.getHalfHeight(),
                world.getVisibility(),
                world
        );
        robot.setPosition(3, 0);
        world.displayWorld();
        Response response = visibilityHandler.lookAround(robot);

        JSONObject jsonObject = (JSONObject)response.object.getJSONArray("objects").get(3);
        assertEquals("OBSTACLE", jsonObject.getString("type"));
        assertEquals(Direction.CardinalDirection.WEST, jsonObject.get("direction"));
        assertEquals(1, jsonObject.getInt("distance"));
        assertFalse(response.object.getJSONArray("objects").isEmpty());
    }

    @Test
    public void testVisibilityWithRobot() {
        Robot otherRobot = new Robot("OtherRobot", "tank", 1, 0);
        world.addRobot(otherRobot);
        otherRobot.setPosition(1, 0);
        VisibilityHandler visibilityHandler = new VisibilityHandler(
                world.getRobots(),
                world.getObstacles(),
                world.getHalfWidth(),
                world.getHalfHeight(),
                world.getVisibility(),
                world
        );
        Response response = visibilityHandler.lookAround(robot);
        JSONObject jsonObject = (JSONObject)response.object.getJSONArray("objects").get(2);
        assertEquals("ROBOT", jsonObject.getString("type"));
        assertEquals(Direction.CardinalDirection.EAST, jsonObject.get("direction"));
        assertEquals(1, jsonObject.getInt("distance"));
        assertFalse(response.object.getJSONArray("objects").isEmpty());
    }
}