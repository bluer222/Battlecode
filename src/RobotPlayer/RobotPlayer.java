package RobotPlayer;

import battlecode.common.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

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

    // store the MapLocation for the tower we built
    static MapLocation builtTower;
    // store the MapLocation objects for found enemy towers
    static ArrayList<MapLocation> foundE = new ArrayList<>();

    // when we start an exploration, we should start neer a paint tower and store it
    // here
    static MapInfo start;

    // store all known freindly paint towers(in case of emergency)
    static ArrayList<MapLocation> towers = new ArrayList<>();

    // we must define these variables here so they will be global
    static MapLocation goal = null;
    static boolean isOriginal;
    static UnitType botType;
    static double attackRange;
    static int paintCapacity;
    static int mapWidth;
    static int mapHeight;

    // 0 = explore, 1 = come home, 2 = get paint
    static int done = 0;

    // bots queued to build for towers
    // soldier=1 mopper=2 splasher=3
    static ArrayList<Integer> botQueue = new ArrayList<>();
    static ArrayList<Message> messageQueue = new ArrayList<>();
    // the target ruin

    static MapLocation targetLoc = null;
    // the type of tower we're building
    static UnitType goalType = null;

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
    public static void run(RobotController rc) throws GameActionException {
        mapHeight = rc.getMapHeight();
        mapWidth = rc.getMapWidth();
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you
        // run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        // the first two towers behave diferently so this will check if the tower is
        // original
        isOriginal = false;
        System.out.println(rc.getRoundNum());

        if (rc.getRoundNum() == 1) {
            isOriginal = true;
        }

        botType = rc.getType();

        if (botType == UnitType.SPLASHER || botType == UnitType.LEVEL_ONE_MONEY_TOWER
                || botType == UnitType.LEVEL_ONE_PAINT_TOWER) {
            attackRange = 9;
            paintCapacity = 300;
        } else if (botType == UnitType.SOLDIER || botType == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
            // sqrt 20
            attackRange = 9;
            paintCapacity = 200;
        } else if (botType == UnitType.MOPPER) {
            // sqrt 2
            attackRange = 2;
            paintCapacity = 100;
        }
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
                // the
                // game
                // world. Remember, unhandled exceptions cause your robot to explode!
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

    // returns the closest location
    public static MapLocation getClosest(RobotController rc, ArrayList<MapLocation> locations)
            throws GameActionException {
        int smallest = 100000;
        MapLocation closest = locations.get(0);
        for (int i = 0; i < locations.size(); i++) {
            MapLocation loc = locations.get(i);
            if (rc.getLocation().distanceSquaredTo(loc) < smallest) {
                smallest = rc.getLocation().distanceSquaredTo(loc);
                closest = loc;
            }
        }
        return closest;
    }

    // returns the index of the lowest number
    public static int getLowest(ArrayList<Integer> intList) throws GameActionException {
        int smallest = intList.get(0);
        int index = 0;
        for (int i = 0; i < intList.size(); i++) {
            if (intList.get(i) < smallest) {
                smallest = intList.get(i);
                index = i;
            }
        }
        return index;
    }

    // this code runs when there are enemies nearby
    public static void attackEnemies(RobotController rc, ArrayList<RobotInfo> enemies) throws GameActionException {
        // each turn we get one single-block attack and one multi block attack
        // if theres only one enemy just use both attacks on them
        if (enemies.size() == 1) {
            // if in range
            MapLocation enemyLocation = enemies.get(0).getLocation();
            if (rc.getLocation().distanceSquaredTo(enemyLocation) <= attackRange) {
                // attack
                rc.attack(enemies.get(0).getLocation());
                // area
                rc.attack(null);
            }
        } else {
            // find all the locations
            ArrayList<MapLocation> locations = new ArrayList<>();
            // for each enemy
            enemies.forEach(enemy -> {
                // get enemy location
                locations.add(enemy.getLocation());
            });

            // if within range we'll use our single cell attack on the closest bot
            MapLocation closest = getClosest(rc, locations);
            if (rc.getLocation().distanceSquaredTo(closest) <= attackRange) {
                rc.attack(closest);
                // area
                rc.attack(null);
            }
        }
    }

    /*
     * Run a single turn for towers. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runTower(RobotController rc) throws GameActionException {
        // Read incoming messages every turn
        rc.setIndicatorString("Reading messages");
        Message[] messages = rc.readMessages(-1);
        rc.setIndicatorString("Storing messages");

        // store messages in messagequeue
        for (Message m : messages) {
            messageQueue.add(m);
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

        // if the turn is even
        if (turnCount % 2 == 0 || enemiesNearby) {
            rc.setIndicatorString("Finding enemies");
            // find nearby bots
            bots nearbyBots = findNearbyBots(rc);
            // put the enemies in an array
            ArrayList<RobotInfo> nearbyEnemies = new ArrayList<>(nearbyBots.enemies);
            // are there any
            if (nearbyEnemies.isEmpty()) {
                // no enemies, we are safe
                enemiesNearby = false;
            } else {
                // there are enemies
                // we set enemiesNearby to true so next turn we will check(normally we
                // alternate)
                enemiesNearby = true;
                // defend ourselves
                rc.setIndicatorString("Attacking enemies");
                attackEnemies(rc, nearbyEnemies);
            }

            rc.setIndicatorString("spreading messages");
            // spread messages
            ArrayList<RobotInfo> nearbyAllies = new ArrayList<>(nearbyBots.allies);
            // we can only send 20 messages per turn,
            int messageCount = 0;
            // for every nearby allied bot
            for (RobotInfo ally : nearbyAllies) {
                // send every message
                for (Message m : messageQueue) {
                    // only send if they are not the source(this will save some message sends)
                    if (ally.getID() != m.getSenderID() && messageCount < 20) {
                        rc.sendMessage(ally.getLocation(), m.getBytes());
                        messageCount += 1;
                    }

                }
            }
            // all sent, clear the queue
            messageQueue.clear();
        }
        // if this is the first turn and this is a starting tower
        if (isOriginal && turnCount == 1) {
            rc.setIndicatorString("adding og bots");

            if (botType == UnitType.LEVEL_ONE_MONEY_TOWER) {
                // we are the money tower
                // we need to make two soldiers
                botQueue.add(1);
                botQueue.add(1);
            }
            if (botType == UnitType.LEVEL_ONE_PAINT_TOWER) {
                // we are the paint tower
                // we need to make one splasher
                botQueue.add(3);
            }
        }
        if (!botQueue.isEmpty()) {
            rc.setIndicatorString("building bots");

            // Pick a direction to build in.
            Direction dir = findBuildDirection(rc);
            // if can build
            if (dir != null) {
                // create a maplocation thats the currentlocation + direction
                MapLocation nextLoc = rc.getLocation().add(dir);
                if (botQueue.get(0) == 1) {
                    rc.buildRobot(UnitType.SOLDIER, nextLoc);
                    System.out.println("BUILT A SOLDIER");
                } else if (botQueue.get(0) == 2) {
                    rc.buildRobot(UnitType.MOPPER, nextLoc);
                    System.out.println("BUILT A MOPPER");
                } else {
                    // rc.buildRobot(UnitType.SPLASHER, nextLoc);
                    rc.buildRobot(UnitType.SOLDIER, nextLoc);

                    System.out.println("BUILT A SPLASHER");
                }

                // Remove the first element from the queue
                botQueue.remove(0);
            }
        }
        // finally, we add more bots to the queue if empty and enough chips and enough
        // paint
        // this artificially makes all bots as expensive as the most expensive bot
        // this way we wont only get moppers(normally if one tower chooses a mopper it
        // will always ge tot build first since moppers are cheaper)
        if (botQueue.isEmpty() && rc.getMoney() - 1000 > 400 && rc.getPaint() >= 300) {
            rc.setIndicatorString("added bot to queue messages");

            botQueue.add(rng.nextInt(3) + 1);
        }

    }

    /**
     * Run a single turn for a Soldier. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException {
        rc.setIndicatorString(Integer.toString(done));

        // we havent set a goal
        if (goal == null) {
            // if we have a target location then set there
            if (targetLoc != null) {
                goal = targetLoc;
            } else {
                // set a goal thats 30 tiles away
                // we dont intend to reach our goal, only to find a ruin on the way
                goal = setFarthest(rc.getLocation(), 30);
                rc.setIndicatorString("set goal");
            }
        }
        // if we built a tower, came back, and now we're waiting for paint refil
        if (done == 2) {
            rc.setIndicatorString("refilling paint");
            // check how much paint the tower has
            int paint = rc.senseRobotAtLocation(goal).getPaintAmount();
            if (paint >= paintCapacity - rc.getPaint()) {
                rc.transferPaint(goal, -(paintCapacity - rc.getPaint()));
            } else {
                rc.transferPaint(goal, -paint);
            }
            // if our paint is full
            if (rc.getPaint() == paintCapacity) {
                // start again
                done = 0;
                goal = null;
            }
        } else if (done == 1) {
            // if we built a tower and we're on our way back and we got back
            // paint below us to save on losses
            if (!rc.senseMapInfo(rc.getLocation()).getPaint().isAlly()) {
                rc.attack(rc.getLocation());
            }
            // if we reached the tower
            if (rc.getLocation().isAdjacentTo(goal)) {
                // if we built it
                if (targetLoc == null) {
                    rc.setIndicatorString("telling about new tower");

                    // if we've built a tower then tell it
                    if (rc.canSendMessage(goal)) {
                        rc.sendMessage(goal, buildMessage(1, builtTower));
                        done = 2;
                    }
                } else {
                    // we mush just be refilling
                    done = 2;
                }
            }
        } else if (targetLoc == null) {
            rc.setIndicatorString("finding ruins");

            // if we need to find a ruin
            // lets find nearby towers and ruins
            // array of unbuilt ruins
            ArrayList<MapLocation> emptyRuins = new ArrayList<>();
            // sense
            MapLocation[] ruins = rc.senseNearbyRuins(-1);
            // for each
            for (int i = 0; i < ruins.length; i++) {
                // towers are a type of robot, this checks if there is a tower on the ruin
                if (rc.canSenseRobotAtLocation(ruins[i])) {
                    RobotInfo tower = rc.senseRobotAtLocation(ruins[i]);
                    // is it our our team
                    if (tower.getTeam() == rc.getTeam()) {
                        rc.setIndicatorString("found a freindly tower");

                        // its on our team
                        // if its a paint tower
                        if (tower.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                            // add it to the allied towers
                            if (!towers.contains(ruins[i])) {
                                towers.add(ruins[i]);
                            }
                        }
                    } else {
                        rc.setIndicatorString("found an enemy tower");

                        // its an enemy tower
                        if (!foundE.contains(ruins[i])) {
                            foundE.add(ruins[i]);
                        }
                    }
                } else {
                    // no tower, this is an empty ruin
                    emptyRuins.add(ruins[i]);
                }
            }
            // did we find any?
            if (emptyRuins.size() > 0) {
                rc.setIndicatorString("found a ruin!");

                // get closest one
                targetLoc = getClosest(rc, emptyRuins);
                goal = targetLoc;
                // set goal tower type
                if (rng.nextInt(2) == 0) {
                    goalType = UnitType.LEVEL_ONE_PAINT_TOWER;
                } else {
                    goalType = UnitType.LEVEL_ONE_MONEY_TOWER;

                }
            }

        }

        // if found ruin and adgacent and not refilling
        if (done == 0 && targetLoc != null && rc.getLocation().isAdjacentTo(targetLoc)) {

            // get direction to the destination
            Direction dir = rc.getLocation().directionTo(targetLoc);

            // check for mark
            MapLocation shouldBeMarked = targetLoc.subtract(dir);
            // if no mark then mark
            if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY
                    && rc.canMarkTowerPattern(goalType, targetLoc)) {
                rc.markTowerPattern(goalType, targetLoc);
                System.out.println("Trying to build a tower at " + targetLoc);
            }
            boolean hasPainted = false;
            boolean hasMoved = false;
            // find nearby tiles
            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)) {
                // if the mark is incorrect
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY) {
                    // get correct mark
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                    // is within range
                    if (rc.getLocation().distanceSquaredTo(patternTile.getMapLocation()) < attackRange) {
                        // is in range, paint it
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                        hasPainted = true;
                    } else {
                        // not in range
                        Direction PatternTileDirection = moveTowards(rc, rc.getLocation(),
                                patternTile.getMapLocation());
                        // move twards
                        rc.move(PatternTileDirection);
                        hasMoved = true;
                        // if now within range then attack
                        if (!hasPainted
                                || rc.getLocation().distanceSquaredTo(patternTile.getMapLocation()) < attackRange) {
                            rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                            hasPainted = true;
                        }
                    }
                }
                // we have already painted and moved, just exit the loop
                if (hasMoved && hasPainted) {
                    break;
                }
            }
            // Complete the ruin if we can.
            if (rc.canCompleteTowerPattern(goalType, targetLoc)) {
                rc.completeTowerPattern(goalType, targetLoc);
                // go to the closest preexisting paint tower
                goal = getClosest(rc, towers);

                // if applicable add this as a new paint tower
                if (goalType == UnitType.LEVEL_ONE_PAINT_TOWER) {
                    towers.add(targetLoc);
                }

                done = 1;

                targetLoc = null;
                builtTower = targetLoc;
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + targetLoc + "!");
            }
            // if our paint is low, refil
            if (rc.getPaint() <= 100) {
                rc.setIndicatorString("going to refil paint");
                done = 1;
                goal = getClosest(rc, towers);
            }
        } else {
            // move twards destination
            Direction dir = moveTowards(rc, rc.getLocation(), goal);
            // if we got to this point theres no way we already moved
            if (dir != null) {
                rc.move(dir);
            }
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

    static class bots {
        public ArrayList<RobotInfo> allies;
        public ArrayList<RobotInfo> enemies;

        public bots(ArrayList<RobotInfo> nearbyAllies, ArrayList<RobotInfo> nearbyEnemies) {
            allies = nearbyAllies;
            enemies = nearbyEnemies;
        }
    }

    // returns an enemy and allied bots arrays
    public static bots findNearbyBots(RobotController rc) throws GameActionException {
        // find nearby robots
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        // create an array, this will store enemies
        ArrayList<RobotInfo> nearbyEnemies = new ArrayList<>();

        // create an array, this will store allies
        ArrayList<RobotInfo> nearbyAllies = new ArrayList<>();

        // go through each bot
        for (int i = 0; i < nearbyRobots.length; i++) {
            if (nearbyRobots[i].getTeam() != rc.getTeam()) {
                nearbyEnemies.add(nearbyRobots[i]);
            } else {
                nearbyAllies.add(nearbyRobots[i]);
            }
        }
        return new bots(nearbyAllies, nearbyEnemies);
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
    public static Direction moveTowards(RobotController rc, MapLocation currentLoc, MapLocation endLoc) {
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
            if (rc.canMove(Direction.NORTH)) {
                return Direction.NORTH;
            } else {
                picknext = true;
            }
        }
        // try down
        if (endLoc.y > currentLoc.y || picknext) {
            if (rc.canMove(Direction.SOUTH)) {
                return Direction.SOUTH;
            } else {
                picknext = true;
            }
        }
        // ok bro, we're already at the destination
        return null;
    }

    // figure out what direction a tower can build in
    public static Direction findBuildDirection(RobotController rc) throws GameActionException {
        for (int i = 0; i < directions.length; i++) {
            // check if occupied
            if (rc.canBuildRobot(UnitType.MOPPER, rc.getLocation().add(directions[i]))) {
                return directions[i];
            }
        }

        // ok bro, we cant build
        return null;
    }

    // Generates a random map location the farthest distance a player can move
    // If all maps are square, you can merge x and y size
    // This doesn't pythagorean theorem this just uses max side length of that
    // triange
    // I'll probably add pythagorean theorem to calculate that tomorrow
    public static MapLocation setFarthest(MapLocation currentLoc, int maxDistance) {
        int sideLength = maxDistance * 2;
        int x = currentLoc.x - maxDistance + rng.nextInt(sideLength + 1);
        int y = currentLoc.y - maxDistance + rng.nextInt(sideLength + 1);

        // clamps the x and y to inside the map.
        x = Math.clamp(x, 0, mapWidth);
        y = Math.clamp(y, 0, mapHeight);

        return new MapLocation(x, y);
    }

    public static int buildMessage(int type, MapLocation currentLoc) {
        int message = (type * 10000) + (currentLoc.x * 100) + currentLoc.y;
        return message;
    }

    public static HashMap<String, Integer> parseMessage(Message message) {
        int bytes = message.getBytes();
        int type = bytes / 10000;
        int xloc = (bytes / 100) % 100;
        int yloc = bytes % 100;

        HashMap<String, Integer> messageData = new HashMap<String, Integer>() {
            {
                put("type", type);
                put("xloc", xloc);
                put("yloc", yloc);
                put("sender", message.getSenderID());
                put("roundSent", message.getRound());
            }
        };
        return messageData;
    }
}