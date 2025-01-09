
package player;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy. The run()
 * method inside this class is like your main function: this is what we'll call
 * once your robot is created!
 */

public class RobotPlayer {

    /*
     * This RNG to make some random
     * moves. The Random class is provided by the java.util.Random import at the
     * top of this file. Here, we *seed* the RNG with a constant number (6147);
     * this makes sure we get the same sequence of numbers every time this code
     * is run. This is very useful for debugging!
     */
    static final Random rng = new Random(3456);

    /**
     * We will use this variable to count the number of turns this robot has
     * been alive. You can use static variables like this to save any
     * information you want. Keep in mind that even though these variables are
     * static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    // we need to remember the most recent paint tower we saw so we can go back
    // 99 is a default because defaulting to 0 would mean we cant find a paint tower
    // at coords 0, 0(it will register as not being found)
    static int rptx = 99; // rptx = recent paint tower x
    static int rpty = 99;

    // store the mapinfo objects for found things
    static MapInfo[] found = {};

    // when we start an exploration, we should start neer a paint tower and store it
    // here
    static MapInfo start;

    // store all known freindly paint towers(in case of emergency)
    static MapInfo[] towers = {};

    // we must define these variables here so they will be global
    static int goalx = 99;
    static int goaly = 99;
    static boolean isOriginal;
    static UnitType botType;
    static double attackRange;
    static MapLocation location;

    // this is our exploration goal, in exploration the goal will be to travel to
    // these coords and find stuff along the way
    // this will be set randomly in the explorer function(obviously the distance
    // will need to be within reach)

    // movetowards function should be a boolg real life robots it seems li /*
    // why whats a bool
    /*
     * type
     * starts at either 0 or 1
     * this makes it so that some soldiers are explorers and others are builders
     * at turn ??? randomly some of these will set type to 2 and become soldiers
     */

    // were enemies seen last time we checked
    static boolean enemiesNearby = false;
    /**
     * Array containing all the possible movement directions.
     */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. It is like the main function for your robot. If this
     * method returns, the robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from
     *           this robot, and to get information on its current status.
     *           Essentially
     *           your portal to interacting with the world.
     *
     */
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you
        // run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        // the first two towers behave diferently so this will check if the tower is
        // original
        isOriginal = false;

        if (rc.getRoundNum() == 1) {
            isOriginal = true;
        }

        botType = rc.getType();

        if (botType.isRobotType(SPLASHER) || botType.isTowerType(LEVEL_ONE_MONEY_TOWER)
                || botType.isTowerType(LEVEL_ONE_PAINT_TOWER)) {
            attackRange = 3;
        } else if (botType.isRobotType(SOLDIER) || botType.isTowerType(LEVEL_ONE_DEFENSE_TOWER)) {
            // sqrt 20
            attackRange = 4.4721;
        } else if (botType.isRobotType(MOPPER)) {
            // sqrt 2
            attackRange = 1.4142;
        }

        // current location
        // for towers this can just stay the same
        location = rc.getLocation();

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in
            // an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At
            // the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to
            // do.

            turnCount += 1; // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to
            // explode.
            try {
                // The same run() function is called for every robot on your team, even if they
                // are
                // different types. Here, we separate the control depending on the UnitType, so
                // we can
                // use different strategies on different robots. If you wish, you are free to
                // rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case SOLDIER:
                        runSoldier(rc);
                        break;
                    case MOPPER:
                        runMopper(rc);
                        break;
                    case SPLASHER:
                        runSplasher(rc);
                        break; // Consider upgrading examplefuncsplayer to use splashers!
                    default:
                        runTower(rc);
                        break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You
                // should
                // handle GameActionExceptions judiciously, in case unexpected events occur in
                // the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop
                // again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for
            // another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction
        // imminent...
    }

    // returns the index of the lowest number
    public static int getLowest(int[] intList) throws GameActionException {
        int smallest = intList[0];
        int index = 0;
        for (int i = 0; i < intList.length; i++) {
            if (intList[i] < smallest) {
                smallest = intList[i];
                index = i;
            }
        }
        return index;
    }

    // this code runs for the two original towers
    public static void runStartingTower(RobotController rc) throws GameActionException {
    }

    // this code runs when there are enemies nearby
    public static void attackEnemies(RobotController rc, RobotInfo[] enemies) throws GameActionException {
        // each turn we get one single-block attack and one multi block attack
        // if theres only one enemy just use both attacks on them
        if (enemies.length == 1) {
            rc.attack(enemies[0].getLocation());
            rc.attack(enemies[0].getLocation());
        } else {
            // find all the locations and distances
            MapLocation[] locations;
            int[] distance;
            for (int i = 0; i < enemies.length; i++) {
                RobotInfo enemy = enemies[i];
                locations[i] = enemy.getLocation();
                distance[i] = distanceBetween(locations[i].x, locations[i].y, location.x, location.y);
            }
            // first, we'll do our single tile attack on the closest bot
            MapLocation closest = locations[getLowest(distance)];
            rc.attack(closest);
            
            //area is yet to be coded, just attack again
            rc.attack(closest);
        }
    }

    /*
     * Run a single turn for towers. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runTower(RobotController rc) throws GameActionException {
        // if the turn is even
        if (turnCount % 2 == 0 || enemiesNearby) {
            // find enemies
            RobotInfo[] nearbyEnemies = findEnemyRobots(rc);
            // are there any
            if (nearbyEnemies.length == 0) {
                // no enemies, we are safe
                enemiesNearby = false;
            } else {
                // there are enemies
                // we set enemiesNearby to true so next turn we will check even if its an odd
                // turn
                enemiesNearby = true;
                attackEnemies(rc, nearbyEnemies);
            }
            // if this is the first turn and this is a starting tower
            if (isOriginal && turnCount == 1) {
                if (botType.isTowerType(LEVEL_ONE_MONEY_TOWER)) {
                    // we are the money tower
                    // we need to make two soldiers
                }
                if (botType.isTowerType(LEVEL_ONE_PAINT_TOWER)) {
                    // we are the paint tower
                    // we need to make one splasher
                }
            }
            // Pick a direction to build in.
            Direction dir = directions[rng.nextInt(directions.length)];
            // create a maplocation thats the currentlocation + direction
            MapLocation nextLoc = rc.getLocation().add(dir);
            // Pick a random robot type to build.
            int robotType = rng.nextInt(3);
            // build it
            if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
                System.out.println("BUILT A SOLDIER");
            } else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
                rc.buildRobot(UnitType.MOPPER, nextLoc);
                System.out.println("BUILT A MOPPER");
            } else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
                rc.buildRobot(UnitType.SPLASHER, nextLoc);
                System.out.println("BUILT A SPLASHER");
            }

            // Read incoming messages
            Message[] messages = rc.readMessages(-1);
            for (Message m : messages) {
                System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
            }

            // TODO: can we attack other bots?
        }
    }

    /**
     * Run a single turn for a Soldier. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException {
        // Sense information about all visible nearby tiles.
        // f
        // get our current location
        Maplocation currenLoc = rc.getLocation();
        // get our ammount of paint
        Maplocation currentPaint = rc.getPaint();

        // we are a builder/explorer
        if (type == 0) {
            // what is this
            // oh i thought it was for the explorers
            // the eplorers need more complex code so when they move they check all of the
            // tiles that are newly visable
            // we havent set a goal yet
            if (goalx == 99) {
                // lets set a location
                //
                // wait how do we get current location
                // theres something on it in the documentation
            }
            // lets search our surriondings
            // we should coordinate
            // ok
            // just set a random goal coord and it will end up going in diff direcitons

            MapInfo curRuin = null;
            for (MapInfo tile : nearbyTiles) {

                if (tile.hasRuin()) {
                    curRuin = tile;
                }
                if (tile.hasRuin()) {
                    curRuin = tile;
                }
            }
            // we found
            // Search for a nearby ruin to complete.
            // shouldnt we go to the coords that explorers send us
            // so you want to just wait and waste time until we get an explorer
            // what if the explorers were builders
            // and just built on the ruin if the had enough paint
            // if not then they go back, replenish then return
            // ok yeah lets just have one type

            if (curRuin != null) {

            }
        } else if (type == 1) {
            // we are a explorer

        }

        // if we found a ruin
        if (curRuin != null) {
            // get the location
            MapLocation targetLoc = curRuin.getMapLocation();
            Direction dir = rc.getLocation().directionTo(targetLoc);
            // move twards it
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
            // Mark the pattern we need to draw to build a tower here if we haven't already.
            MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
            if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY
                    && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)) {
                rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                System.out.println("Trying to build a tower at " + targetLoc);
            }
            // Fill in any spots in the pattern with the appropriate paint.
            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)) {
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY) {
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                    if (rc.canAttack(patternTile.getMapLocation())) {
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                    }
                }
            }
            // Complete the ruin if we can.
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)) {
                rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + targetLoc + "!");
            }
        }

        // Move and attack randomly if no objective.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        // Try to paint beneath us as we walk to avoid paint penalties.
        // Avoiding wasting paint by re-painting our own tiles.
        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())) {
            rc.attack(rc.getLocation());
        }
    }

    public static void runSplasher(RobotController rc) throws GameActionException {

    }

    /**
     * Run a single turn for a Mopper. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runMopper(RobotController rc) throws GameActionException {
        // Move and attack randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        if (rc.canMopSwing(dir)) {
            rc.mopSwing(dir);
            System.out.println("Mop Swing! Booyah!");
        } else if (rc.canAttack(nextLoc)) {
            rc.attack(nextLoc);
        }
        // We can also move our code into different methods or classes to better
        // organize it!
        // updateEnemyRobots(rc);
    }

    // returns an array of enemy robots
    public static RobotInfo[] findEnemyRobots(RobotController rc) throws GameActionException {
        // find nearby robots
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        // create an array, this will store enemies
        RobotInfo[] nearbyEnemies;

        int a = 0;
        // go through each bot
        for (int i = 0; i < nearbyRobots.length; i++) {
            if (nearbyRobots[i].getTeam() != rc.getTeam()) {
                nearbyEnemies[a] = nearbyRobots[i];
                a++;
            }
        }
        return nearbyEnemies;
    }

    // calculate the distance between two points
    public static int distanceBetween(int x1, int y1, int x2, int y2) throws GameActionException {
        int a = x1 - x2;
        int b = y1 - y2;
        // we floor the distance because a tile just needs to partially be within the
        // range circle to count
        return (int) Math.floor(Math.sqrt((a * a) + (b * b)));
    }

    // figure out the best direction to move given a destination and start
    public static Direction moveTowards(MapLocation currentLoc, MapLocation endLoc) {
        Direction dir;
        boolean picknext = false;
        // check if it's right
        if (endLoc.x > currentLoc.x || picknext) {
            // is it also up
            if (endLoc.y > currentLoc.y || picknext) {
                // then move right+up
                if (rc.canMove(Direction.NORTHEAST)) {
                    return Direction.NORTHEAST;
                } else {
                    // we couldent move there
                    picknext = true;
                }
            }
            // maybe its right+down then
            if (endLoc.y < currentLoc.y || picknext) {
                if (rc.canMove(Direction.SOUTHEAST)) {
                    return Direction.SOUTHEAST;
                } else {
                    picknext = true;
                }
                // if not then it's directly right(not up or down)
            } else {
                if (rc.canMove(Direction.EAST)) {
                    return Direction.EAST;
                } else {
                    picknext = true;
                }
            }
            // alr bro it ain't that hard figure it out
        }
        // either we're going left or none of the rights worked
        if (endLoc.x < currentLoc.x || picknext) {
            // are we going left up
            if (endLoc.y > currentLoc.y || picknext) {
                if (rc.canMove(Direction.NORTHWEST)) {
                    return Direction.NORTHWEST;
                } else {
                    picknext = true;
                }
            }
            // maybe left down
            if (endLoc.y < currentLoc.y || picknext) {
                if (rc.canMove(Direction.SOUTHWEST)) {
                    return Direction.SOUTHWEST;
                } else {
                    picknext = true;
                }
            } else {
                // ok we must be moving left then
                if (rc.canMove(Direction.WEST)) {
                    return Direction.WEST;
                } else {
                    picknext = true;
                }
            }
        }
        // we arent going left or right if we reached this point
        // try up
        if (endLoc.y > currentLoc.y || picknext) {
            if (rc.canMove(Direction.UP)) {
                return Direction.UP;
            } else {
                picknext = true;
            }
        }
        // try down
        if (endLoc.y > currentLoc.y || picknext) {
            if (rc.canMove(Direction.DOWN)) {
                return Direction.DOWN;
            } else {
                picknext = true;
            }
        } else {
            // ok bro, we're already at the destination
            return Direction.CENTER;
        }
    }
    //Generates a random map location the farthest distance a player can move
    //If all maps are square, you can merge x and y size
    //This doesn't pythagorean theorem this just uses max side length of that triange
    //I'll probably add pythagorean theorem to calculate that tomorrow
    public static MapLocation setFarthest(MapLocation currentLoc, int maxDistance, int mapXSize, int mapYSize){
        int sideLength = maxDistance * 2;
        int x = currentLoc.x - maxDistance + random.nextInt(sideLength + 1);
        int y = currentLoc.y - maxDistance + random.nextInt(sideLength + 1);

        Maplocation newLoc = new MapLocation(x,y)
        //clamps the x and y to inside the map.
        newLoc.x = clamp(newLoc.x, 0, mapXSize);
        newLoc.y = clamp(newLoc.y, 0, mapYSize);
        return newLoc;
    }
}
