package za.co.wethinkcode.robots.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.Robot;
import za.co.wethinkcode.robots.server.Obstacle;
import za.co.wethinkcode.robots.server.ObstacleType;
import za.co.wethinkcode.robots.server.World;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


public class CommandTest {

    private World world;

    @BeforeEach
    public void setUp() {
        world = World.getInstance();
    }

        @Test
        public void testValidCommands() {
            assertTrue(Command.isValidCommand("forward"));
            assertTrue(Command.isValidCommand("back"));
            assertTrue(Command.isValidCommand("turn"));
            assertTrue(Command.isValidCommand("look"));
            assertTrue(Command.isValidCommand("state"));
            assertTrue(Command.isValidCommand("launch"));
        }

        @Test
        public void testInvalidCommands() {
            assertFalse(Command.isValidCommand("teleport"));
            assertFalse(Command.isValidCommand("fly"));
            assertFalse(Command.isValidCommand("dance"));
            assertFalse(Command.isValidCommand("spin around"));
            assertFalse(Command.isValidCommand("l00k"));
        }


    @Test
    public void testLaunchTwoRobotsPerClientLimit() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});
        world.execute(command, clientId, response -> {
           assertTrue(response.isOKResponse());
        });
    }

    @Test
    public void testLookCommand() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
            Robot launchedRobot = world.getRobots().getFirst();

            world.execute(new LookCommand(launchedRobot, new String[]{}), clientId, lookResponse ->  {
               assertTrue(lookResponse.isOKResponse());
               assertNotEquals(0, lookResponse.object.getJSONArray("objects").length(), "Should be able to set objects");
            });
        });
    }

    @Test
    public void testLookWithnNoRobots() {
        String clientId = "client-xyz";
        Robot robot = new Robot("Alpha", "tank");
        World world = new World(10, 10);

        world.execute(new LookCommand(robot, new String[]{}), clientId, lookResponse -> {
            assertFalse(lookResponse.isOKResponse());
            assertEquals("Could not find robot: Alpha", lookResponse.getMessage());
        });
    }

    @Test
    public void testSuccessfulLaunch() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
        });
    }

    @Test
    public void testOrientationCommand() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
            Robot launchedRobot = world.getRobots().getFirst();

            OrientationCommand orientationCommand = new OrientationCommand(launchedRobot);

            world.execute(orientationCommand, clientId, orientationResponse -> {
                assertTrue(orientationResponse.isOKResponse());
                assertEquals("Alpha is facing NORTH.", orientationResponse.getMessage());
            });
        });
    }

    @Test
    public void testHandleTurnLeft() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
            Robot launchedRobot = world.getRobots().getFirst();

            TurnCommand turnCommand = new TurnCommand(launchedRobot, new String[]{"left"});

            world.execute(turnCommand, clientId, turnResponse -> {
                assertTrue(turnResponse.isOKResponse());
                assertEquals("Alpha turned left to WEST", turnResponse.getMessage());
                assertEquals("WEST", turnResponse.object.getJSONObject("state").getString("direction"));
            });
        });
    }

    @Test
    public void testHandleTurnRight() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
            Robot launchedRobot = world.getRobots().getFirst();

            TurnCommand turnCommand = new TurnCommand(launchedRobot, new String[]{"right"});

            world.execute(turnCommand, clientId, turnResponse -> {
                assertTrue(turnResponse.isOKResponse());
                assertEquals("Alpha turned right to EAST", turnResponse.getMessage());
                assertEquals("EAST", turnResponse.object.getJSONObject("state").getString("direction"));
            });
        });
    }

    @Test
    public void testMoveForward() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
            Robot launchedRobot = world.getRobots().getFirst();

            launchedRobot.setPosition(0, 0);
            String[] args = new String[]{"forward", "Alpha", "1"};
            MoveCommand moveCommand = new MoveCommand(launchedRobot, "forward", args);

            world.execute(moveCommand, clientId, moveResponse -> {
                assertTrue(moveResponse.isOKResponse());
                assertTrue(moveResponse.getMessage().contains("Moved Alpha to"));
            });
        });
    }
    @Test
    public void testMoveForwardIntoPit() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);

        world.addRobot(robot1);

        robot1.setPosition(0, 0);
        Obstacle pit = new Obstacle(ObstacleType.PIT, 0, 1, 1,1);

        world.addObstacle(pit);

        String[] args = new String[]{"forward", "Alpha", "1"};
        MoveCommand moveCommand = new MoveCommand(robot1, "forward", args);

        world.execute(moveCommand, clientId, moveResponse -> {
            assertFalse(moveResponse.isOKResponse());
            assertTrue(moveResponse.getMessage().contains("Alpha fell into a pit and died."));
            assertEquals(Robot.RobotStatus.Dead, world.getRobots().getFirst().status);
        });
    }

    @Test
    public void testMoveBack() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
            Robot launchedRobot = world.getRobots().getFirst();

            launchedRobot.setPosition(0, 0);
            String[] args = new String[]{"back", "Alpha", "1"};
            MoveCommand moveCommand = new MoveCommand(launchedRobot, "back", args);

            world.execute(moveCommand, clientId, moveResponse -> {
                assertTrue(moveResponse.isOKResponse());
                assertTrue(moveResponse.getMessage().contains("Moved Alpha to"));
            });
        });
    }

    @Test
    public void testMoveBackIntoPit() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);

        world.addRobot(robot1);

        robot1.setPosition(0, 0);
        Obstacle pit = new Obstacle(ObstacleType.PIT, 0, -1, 1,1);

        world.addObstacle(pit);

        String[] args = new String[]{"back", "Alpha", "1"};
        MoveCommand moveCommand = new MoveCommand(robot1, "forward", args);

        world.execute(moveCommand, clientId, moveResponse -> {
            assertFalse(moveResponse.isOKResponse());
            assertTrue(moveResponse.getMessage().contains("Alpha fell into a pit and died."));
            assertEquals(Robot.RobotStatus.Dead, world.getRobots().getFirst().status);
        });
    }

    @Test
    public void testStateCommand() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
            Robot launchedRobot = world.getRobots().getFirst();

            StateCommand stateCommand = new StateCommand(launchedRobot, new String[]{});

            world.execute(stateCommand, clientId, stateResponse -> {
                assertTrue(stateResponse.isOKResponse());
                assertEquals("NORTH", stateResponse.object.getJSONObject("state").getString("direction"));
                assertEquals("NORMAL", stateResponse.object.getJSONObject("state").getString("status"));
            });
        });
    }

    @Test
    public void testFireMisses() {
        String clientId = "client-xyz";
        Robot robot1 = new Robot("Alpha", "tank");
        World world = new World(10, 10);
        LaunchCommand command = new LaunchCommand(robot1, new String[]{"tank"});

        world.execute(command, clientId, response -> {
            assertTrue(response.isOKResponse());
            Robot launchedRobot = world.getRobots().getFirst();

            FireCommand fireCommand = new FireCommand(launchedRobot, new String[]{});

            world.execute(fireCommand, clientId, fireResponse -> {
                assertTrue(fireResponse.isOKResponse());
                assertEquals("You have missed 🥲!", fireResponse.getMessage());
            });
        });
    }

    @Test
    public void testFireHits() {
        String clientId = "client-xyz";
        Robot shooter = new Robot("Alpha", "tank");
        Robot target = new Robot("Hal", "tank");
        World world = new World(10, 10);

        world.addRobot(shooter);
        world.addRobot(target);

        shooter.setPosition(0, 1);
        target.setPosition(0, 2);

        int shooterInitialShots = shooter.getShots();
        int targetInitialShield = target.getShields();

        world.displayWorld();

        FireCommand fireCommand = new FireCommand(shooter, new String[]{});

        world.execute(fireCommand, clientId, fireResponse -> {
            assertTrue(fireResponse.isOKResponse());
            assertEquals(shooterInitialShots - 1, fireResponse.object.getJSONObject("state").getInt("shots"));
            assertEquals(targetInitialShield - 1, fireResponse.object.getJSONObject("data").getJSONObject("state").getInt("shields"));
            assertEquals(target.getName(), fireResponse.object.getJSONObject("data").getString("robot"));
        });
    }

    @Test
    public void testFireHitsWithSniper() {
        String clientId = "client-xyz";
        Robot shooter = new Robot("Alpha", "sniper");
        Robot target = new Robot("Hal", "tank");
        World world = new World(10, 10);

        world.addRobot(shooter);
        world.addRobot(target);

        shooter.setPosition(0, 1);
        target.setPosition(0, 2);

        int shooterInitialShots = shooter.getShots();
        int targetInitialShield = target.getShields();

        world.displayWorld();

        FireCommand fireCommand = new FireCommand(shooter, new String[]{});

        world.execute(fireCommand, clientId, fireResponse -> {
            assertTrue(fireResponse.isOKResponse());
            assertEquals(shooterInitialShots - 1, fireResponse.object.getJSONObject("state").getInt("shots"));
            assertEquals(targetInitialShield - 1, fireResponse.object.getJSONObject("data").getJSONObject("state").getInt("shields"));
            assertEquals(target.getName(), fireResponse.object.getJSONObject("data").getString("robot"));
        });
    }

    @Test
    public void testFireKills() {
        String clientId = "client-xyz";
        Robot shooter = new Robot("Alpha", "tank");
        Robot target = new Robot("Hal", "tank");
        World world = new World(10, 10);

        world.addRobot(shooter);
        world.addRobot(target);

        shooter.setPosition(0, 1);
        target.setPosition(0, 2);
        target.setShields(0);

        int shooterInitialShots = shooter.getShots();

        world.displayWorld();

        FireCommand fireCommand = new FireCommand(shooter, new String[]{});

        world.execute(fireCommand, clientId, fireResponse -> {
            assertTrue(fireResponse.isOKResponse());
            assertEquals(shooterInitialShots - 1, fireResponse.object.getJSONObject("state").getInt("shots"));
            assertEquals(0, fireResponse.object.getJSONObject("data").getJSONObject("state").getInt("shields"));
            assertEquals("DEAD", fireResponse.object.getJSONObject("data").getJSONObject("state").getString("status"));
            assertEquals(target.getName(), fireResponse.object.getJSONObject("data").getString("robot"));
        });
    }

    @Test
    public void testReloading() {
        String clientId = "client-xyz";
        Robot robot = new Robot("Alpha", "tank");
        World world = new World(10, 10);

        world.addRobot(robot);
        int shooterInitialShots = robot.getShots();

        FireCommand fireCommand = new FireCommand(robot, new String[]{});

        world.execute(fireCommand, clientId, fireResponse -> {
            assertTrue(fireResponse.isOKResponse());
            assertEquals(shooterInitialShots - 1, robot.getShots());

            ReloadCommand reloadCommand = new ReloadCommand(robot, new  String[]{});
            AtomicInteger invocations = new AtomicInteger(0);

            world.execute(reloadCommand, clientId, reloadResponse -> {
                invocations.getAndIncrement();
                assertTrue(reloadResponse.isOKResponse());

                if (invocations.get() == 1) {
                    assertEquals("Alpha is now reloading.", reloadResponse.getMessage());
                } else {
                    assertEquals("Alpha is done.", reloadResponse.getMessage());
                    assertEquals(shooterInitialShots, reloadResponse.object.getJSONObject("state").getInt("shots"));
                }
            });
        });
    }

    @Test
    public void testRepairing() {
        String clientId = "client-xyz";
        Robot robot = new Robot("Alpha", "tank");
        World world = new World(10, 10);

        world.addRobot(robot);
        int robotInitialShields = robot.getShields();

        robot.takeHit();
        RepairCommand repairCommand = new RepairCommand(robot, new String[]{});

        world.execute(repairCommand, clientId, repairResponse -> {
            assertTrue(repairResponse.isOKResponse());

            if (repairResponse.isOKResponse() && repairResponse.getMessage().equalsIgnoreCase("Alpha is now repairing.")) {
                assertNotEquals(robotInitialShields, robot.getShields());
            } else if (repairResponse.isOKResponse()) {
                assertEquals(robotInitialShields, robot.getShields());
            }
        });
    }

    @Test
    public void testHelpCommand() {
        String clientId = "client-xyz";
        World world = new World(10, 10);

        HelpCommand helpCommand = new HelpCommand(null, null);

        world.execute(helpCommand, clientId, helpResponse -> {
            assertTrue(helpResponse.isOKResponse());
            assertTrue(helpResponse.getMessage().contains("I CAN UNDERSTAND THESE COMMANDS"));
        });
    }

    @Test
    public void testDumpCommand() {
        String clientId = "client-xyz";
        World world = new World(10, 10);

        DisconnectCommand disconnectCommand = new DisconnectCommand();

        world.execute(disconnectCommand, clientId, disconnectResponse -> {
            assertTrue(disconnectResponse.isOKResponse());
        });
    }

    @Test
    public void testShutdown() {
        String clientId = "client-xyz";
        Robot robot = new Robot("Alpha", "tank");
        World world = new World(10, 10);

        world.addRobot(robot);

        ShutdownCommand shutdownCommand = new ShutdownCommand(robot, new String[]{});

        world.execute(shutdownCommand, clientId, shutdownResponse -> {
            assertTrue(shutdownResponse.isOKResponse());
            assertEquals("Removed robot Alpha from the world.", shutdownResponse.getMessage());
        });
    }
}