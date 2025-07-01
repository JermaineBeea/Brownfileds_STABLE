package za.co.wethinkcode.robots;

import za.co.wethinkcode.robots.client.Direction;
import za.co.wethinkcode.robots.client.Position;

import static za.co.wethinkcode.robots.client.Direction.CardinalDirection.*;

/**
 * Models a robot with a name, position, direction, and state.
 * Handles robot-specific properties and actions.
 */
public class Robot {
    private String make;
    private String name;
    private int shields;
    private int shots;
    private int maxShots;
    private Direction direction;
    private Position position;
    public RobotStatus status;
    private boolean repairing;
    private boolean reloading;

    public enum RobotStatus {
        Normal,
        Dead,
        Reload,
        Repair
    }

    public Robot(String name, String make, int x, int y) {
        this.name = name;
        this.make = make;
        this.position =  new Position(x, y);
        this.direction= new Direction(NORTH); // default direction

        if (make.equalsIgnoreCase("tank")) {
            this.shields = 10;
            this.shots = 3;
        } else if (make.equalsIgnoreCase("sniper")) {
            this.shields = 5;
            this.shots = 20;
        }
        this.maxShots = shots;
        this.status = RobotStatus.Normal;
    }

    public Robot(String name) {
        this(name, "tank", 0, 0);
    }

    public Robot(String name, String make) {
        this(name, make, 0,0);
    }

    public void moveForward(int steps) {
        for (int i = 0; i < steps; i++) {
            // Logic to update the robot's position based on its current direction
            switch (direction.getDirection()) {
                case NORTH: this.setPosition(getX(), getY() + 1); break;
                case SOUTH: this.setPosition(getX(), getY() - 1); break;
                case EAST: this.setPosition(getX() + 1, getY()); break;
                case WEST: this.setPosition(getX() - 1, getY()); break;
            }
        }
    }
    public void moveBackward(int steps) {
        for (int i = 0; i < steps; i++) {
            // Logic to update the robot's position based on its current direction
            switch (direction.getDirection()) {
                case NORTH: this.setPosition(getX(), getY() - 1); break;
                case SOUTH: this.setPosition(getX(), getY() + 1); break;
                case EAST: this.setPosition(getX() - 1, getY()); break;
                case WEST: this.setPosition(getX() + 1, getY()); break;
            }
        }
    }

    public int getMaxShots() {
        return maxShots;
    }

    public boolean isRepairing() {
        return this.repairing;
    }

    public void setRepairing(boolean repairing) {
        this.repairing = repairing;
    }

    public boolean isReloading() {
        return reloading;
    }

    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    public void turnLeft() {
        direction.turnLeft();
    }

    public void turnRight() {
        direction.turnRight();
    }

    public String orientation() {
        return direction.toString();
    }

    public Position getPosition() {
        return position;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public String getName() {
        return name;
    }

    public String getMake() {
        return make;
    }

    public int getShields() {
        return shields;
    }

    public int getShots() {
        return shots;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public void setPosition(int x, int y) {
        this.position = new Position(x, y);
    }

    public void setShields(int shields){
        this.shields = shields;
    }

    public void takeHit() {
        if (shields > 0) {
            shields--;
        } else {
            this.status = RobotStatus.Dead;
        }
    }
    public boolean isDead() {
        return this.status == RobotStatus.Dead;
    }

}