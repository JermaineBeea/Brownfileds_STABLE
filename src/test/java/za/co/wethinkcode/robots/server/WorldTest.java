package za.co.wethinkcode.robots.server;

import org.junit.Test;
import za.co.wethinkcode.robots.Robot;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class WorldTest {
    @Test
    public void testInitialization() {
        int passInHeight = 5;
        int passInWidth = 20;

        World world = new World(passInWidth, passInHeight);

        assertEquals(passInHeight, world.getHeight());
        assertEquals(passInWidth, world.getWidth());
        assertEquals(passInWidth / 2, world.getHalfWidth());
        assertEquals(passInHeight / 2, world.getHalfHeight());
        assertEquals(0, world.getRobots().size());
        assertEquals(0, world.getObstacles().size());
    }

    @Test
    public void testInitializationOfSharedInstance() {
        ConfigLoader configLoader = new ConfigLoader();
        int passInHeight = 0;
        int passInWidth = 0;

        try {
            Properties properties = configLoader.loadConfig("config.properties");
            passInWidth = Integer.parseInt(properties.getProperty("world.width"));
            passInHeight = Integer.parseInt(properties.getProperty("world.height"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        World world = World.getInstance();

        assertEquals(passInHeight, world.getHeight());
        assertEquals(passInWidth, world.getWidth());
        assertEquals(passInWidth / 2, world.getHalfWidth());
        assertEquals(passInHeight / 2, world.getHalfHeight());
        assertEquals(0, world.getRobots().size());
        assertNotEquals(0, world.getObstacles().size(), "The default world instance should have Obstacles!");
    }

    @Test
    public void testFailingToLoadConfiguration() {
        // This sets that the default dimensions are used if the configuration cannot find the file
        ConfigLoader configLoader = new ConfigLoader();
        World world = new World(10, 10);
        configLoader.applyConfigToWorld(world, "blah.blah.does.not.exist");

        assertEquals(100, world.getWidth());
        assertEquals(50, world.getHeight());
    }

    @Test
    public void testAddingRobotsIncrements() {
        World world = new World(10, 10);

        assertEquals(Status.OK, world.addRobot(new Robot("Hal")));
        assertEquals(1, world.getRobots().size(), "When adding robots the array count should be updated!");
        assertEquals(Status.OK, world.addRobot(new Robot("Rover")));
        assertEquals(2, world.getRobots().size(), "When adding robots the array count should be updated!");
    }

    @Test
    public void testAddingObstaclesIncrements() {
        World world = new World(10, 10);
        Obstacle obstacle = new Obstacle(ObstacleType.MOUNTAIN, 0, 0, 1, 1);

        assertTrue(world.addObstacle(obstacle));
        assertEquals(1, world.getObstacles().size());
    }

    @Test
    public void testOverlappingObstacles() {
        World world = new World(10, 10);
        Obstacle obstacle = new Obstacle(ObstacleType.MOUNTAIN, 0, 0, 1, 1);
        Obstacle overlappingObstacle = new Obstacle(ObstacleType.PIT, obstacle.getX(), obstacle.getY(), obstacle.width(), obstacle.height());

        assertTrue(world.addObstacle(obstacle));
        assertFalse(world.addObstacle(overlappingObstacle), "Should not be able to add this obstacle because it overlaps");
        assertEquals(1, world.getObstacles().size());
    }

    @Test
    public void testAddingDuplicateRobot() {
        World world = new World(10, 10);

        assertEquals(Status.OK, world.addRobot(new Robot("Hal")));
        assertEquals(Status.ExistingName, world.addRobot(new Robot("Hal")), "Should not be able to add another robot with the same name!");
    }

    @Test
    public void testAddingRobotUsesRandomPosition() {
        World world = new World(1000, 1000);
        Robot robot = new Robot("Hal");
        int initialY = robot.getY();
        int initialX = robot.getX();

        assertEquals(Status.OK, world.addRobot(robot));
        assertNotEquals(initialX, robot.getX(), "After adding the robot should have a different location");
        assertNotEquals(initialY, robot.getY(), "After adding the robot should have a different");
    }

    @Test
    public void testRemovingRobot() {
        World world = new World(10, 10);

        assertEquals("ERROR", world.removeRobot("Hal").object.getString("result"), "Should be an error this there is no robot by this name in the world as yet");

        Robot robot = new Robot("Hal");
        assertEquals(Status.OK, world.addRobot(robot));
        assertEquals(1, world.getRobots().size());
        assertEquals("OK", world.removeRobot("Hal").object.getString("result"));
        assertEquals(0, world.getRobots().size());
    }

    @Test
    public void testFindingRobot() {
        World world = new World(10, 10);
        Robot robot = new Robot("Hal");

        assertEquals(Status.OK, world.addRobot(robot));
        assertEquals(robot, world.findRobot("Hal"));
        assertNull(world.findRobot("Rover"), "Should be null since there is no robot by that name in the world");
    }

    @Test
    public void testRobotsInfo() {
        World world = new World(1000, 1000);

        assertTrue(world.getAllRobotsInfo().contains("No robots in the world."));

        Robot robot = new Robot("Hal");
        assertEquals(Status.OK, world.addRobot(robot));

        assertTrue(world.getAllRobotsInfo().contains("Robots in the world:"));
        assertTrue(world.getAllRobotsInfo().contains(robot.getName()));
    }

    @Test
    public void testWorldState() {
        // A bit crude but works to ensure the robot and obstacle are included in the state
        World world = new World(1000, 1000);
        Obstacle obstacle = new Obstacle(ObstacleType.MOUNTAIN, 0, 0, 1, 1);
        Robot robot = new Robot("Hal");

        world.addObstacle(obstacle);
        assertEquals(Status.OK, world.addRobot(robot));

        assertTrue(world.getFullWorldState().contains(obstacle.toString()));
        assertTrue(world.getFullWorldState().contains(robot.getName()));
    }
}
