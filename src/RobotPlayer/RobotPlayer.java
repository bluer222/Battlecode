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
    static MapLocation builtTower = null;
    // store the MapLocation objects for found enemy towers
    static ArrayList<MapLocation> foundE = new ArrayList<>();
    static int justFound = 2;

    // when we start an exploration, we should start neer a paint tower and store it
    // here
    static MapInfo start;
    // tile a mopper is going to mop
    static MapLocation tileToClear = null;

    // store all known freindly paint towers(in case of emergency)
    static ArrayList<MapLocation> towers = new ArrayList<>();

    // we must define these variables here so they will be global
    static MapLocation goal = null;

    static ArrayList<MapLocation> paintNext = new ArrayList<>();

    static boolean isOriginal;
    static UnitType botType;
    // if we are a mopper this is the soldier we are following
    static RobotInfo following = null;
    // this is the id of that soldier
    static int followingID;

    static double attackRange;
    static int mapWidth;
    static int mapHeight;
    static Random rng;
    static Direction directionMoved;
    // 0 = explore, 1 = come home, 2 = get paint
    static int task = 0;

    // bots queued to build for towers
    // soldier=1 mopper=2 splasher=3
    static ArrayList<Integer> botQueue = new ArrayList<>();
    static ArrayList<Message> messageQueue = new ArrayList<>();
    // the target ruin

    static MapLocation targetLoc = null;
    // the type of tower we're building
    static UnitType goalType = UnitType.LEVEL_ONE_PAINT_TOWER;
    // have we built the max nummber of towers
    static boolean maxTowers = false;

    // were enemies seen last time we checked
    static boolean enemiesNearby = false;
    public static int unoptimalMoves;
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
        /*
         * This RNG to make some random
         * moves. The Random class is provided by the java.util.Random import at the
         * top of this file. Here, we *seed* the RNG with a constant number (6147);
         * this makes sure we get the same sequence of numbers every time this code
         * is run. This is very useful for debugging!
         */
        rng = new Random(rc.getID());
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

        // how many times have we failed to move in the best direction
        unoptimalMoves = 0;
        if (rc.getRoundNum() == 1) {
            isOriginal = true;
        }

        botType = rc.getType();

        if (botType == UnitType.SPLASHER || botType == UnitType.LEVEL_ONE_MONEY_TOWER
                || botType == UnitType.LEVEL_ONE_PAINT_TOWER) {
            attackRange = 9;
        } else if (botType == UnitType.SOLDIER || botType == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
            // sqrt 20
            attackRange = 9;
            goal = setFarthest(rc.getLocation(), 30);
        } else if (botType == UnitType.MOPPER) {
            // sqrt 2
            attackRange = 2;
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

                rc.setIndicatorString(
                        "task: " + task + " goal: " + goal + " moving " + directionMoved + " to paint: "
                                + paintNext.size());

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
        if (locations.size() == 0) {
            return null;
        }
        int smallest = 10000000;
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
                    if (ally.getID() != m.getSenderID() && messageCount < 20
                            && rc.canSendMessage(ally.getLocation(), m.getBytes())) {
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
                // we need to make one splasher
                botQueue.add(3);
            }
            if (botType == UnitType.LEVEL_ONE_PAINT_TOWER) {
                // we are the paint tower
                // we need to make one soldier and one mopper
                botQueue.add(2);
                botQueue.add(1);

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
        if (!botQueue.isEmpty()) {
            // sometimes someone takes

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
    }

    /**
     * Run a single turn for a Soldier. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException {
        // set our indicator to the task for debug
        rc.setIndicatorString(Integer.toString(unoptimalMoves));
        // if we have built all towers
        if (maxTowers) {
            // we should attack
            // if new or if no goal
            if (task == -1 || goal == null) {
                // set to closest enemy tower
                if (foundE.size() > 0) {
                    goal = getClosest(rc, foundE);
                    // 0 = attack
                    task = 0;
                } else {
                    // we need to find enemy towers
                    // 1 = find
                    task = 1;
                    goal = setFarthest(rc.getLocation(), 50);
                }
            }
            // defend ourselves
            if (turnCount % 2 == 0 || enemiesNearby) {
                bots nearbyBots = findNearbyBots(rc);
                // lets check allies too
                if (nearbyBots.allies.size() > 0) {
                    for (RobotInfo ally : nearbyBots.allies) {
                        if (isPaintTower(ally.type) && !towers.contains(ally.getLocation())) {
                            towers.add(ally.getLocation());
                        }
                    }
                }
                if (nearbyBots.enemies.size() > 0) {
                    enemiesNearby = true;
                    // Get a list of MapLocations
                    ArrayList<MapLocation> mapLocations = new ArrayList<>();
                    for (RobotInfo mapInfo : nearbyBots.enemies) {
                        mapLocations.add(mapInfo.getLocation());
                        if (mapInfo.type.isTowerType() && !foundE.contains(mapInfo.getLocation())) {
                            goal = mapInfo.getLocation();
                            foundE.add(mapInfo.getLocation());
                        }
                    }
                    // attack closest
                    MapLocation closest = getClosest(rc, mapLocations);
                    if (rc.canAttack(closest)) {
                        rc.attack(closest);
                    }
                } else {
                    enemiesNearby = false;
                }
            }
            // no one nearby
            if (!enemiesNearby) {
                // we can probably paint below us
                if (!rc.senseMapInfo(rc.getLocation()).getPaint().isAlly() && rc.canAttack(rc.getLocation())) {
                    rc.attack(rc.getLocation());
                }
            }
            if (goal != null && rc.getLocation().isAdjacentTo(goal)) {
                // we're at the goal
                // if we are finding
                if (task == 1) {
                    // we failed to find
                    // set a new goal
                    goal = setFarthest(rc.getLocation(), 50);
                } else if (task == 0) {
                    // we are attacking
                    // if we kiled the tower then remove it from the list and set a new goal
                    if (!rc.canSenseRobotAtLocation(goal)) {
                        goal = null;
                    }
                } else if (task == 2) {
                    // we are refilling on paint
                    if (rc.isActionReady()) {
                        int paint = rc.senseRobotAtLocation(goal).getPaintAmount();

                        // if its more than we need, then take the ammount we need
                        if (paint >= 200 - rc.getPaint()) {
                            rc.transferPaint(goal, -(200 - rc.getPaint()));
                        } else if (paint > 0) {
                            // if its less than we need, take it all
                            rc.transferPaint(goal, -paint);
                        }
                        if (rc.getPaint() == 200) {
                            goal = null;
                            task = 0;
                        }
                    }
                }

            } else {
                // move twards goal
                Direction dir = moveTowards(rc, rc.getLocation(), goal);
                if (dir != null) {
                    rc.move(dir);
                }
            }
            // if our paint is low, and we are building a tower
            if (rc.getPaint() <= 100) {
                rc.setIndicatorString("going to refil paint");
                // TODO: we need to ask others for tower locations if we have none(instead of
                // erroring)
                if (towers.size() > 0) {
                    // stop building, set the task to refil instead
                    task = 2;
                    goal = getClosest(rc, towers);
                } else {
                    // oh no, we dont know any towers
                    // we're fucked
                }
            }
        } else {
            // task3 means we are looking for a paint tower so we can refil
            if (task == 3) {
                // lets only check every other turn
                if (turnCount % 2 == 0) {
                    // sense ruins
                    MapLocation[] ruins = rc.senseNearbyRuins(-1);
                    // for each
                    for (int i = 0; i < ruins.length; i++) {
                        // towers are a type of robot, this checks if there is a tower on the ruin
                        if (rc.canSenseRobotAtLocation(ruins[i])) {
                            RobotInfo tower = rc.senseRobotAtLocation(ruins[i]);
                            // is it our our team
                            if (tower.getTeam() == rc.getTeam()) {
                                rc.setIndicatorString("found a freindly tower2");

                                // if its a paint tower
                                if (isPaintTower(tower.getType())) {
                                    // add it to the allied towers
                                    if (!towers.contains(ruins[i])) {
                                        towers.add(ruins[i]);
                                        // yay we can get paint now
                                        goal = ruins[i];
                                        task = 1;
                                    }
                                }
                            } else {
                                // its an enemy tower
                                rc.setIndicatorString("found an enemy tower");
                                // add it to found enemies
                                if (!foundE.contains(ruins[i])) {
                                    foundE.add(ruins[i]);
                                }
                            }
                        }
                    }
                }
            } else if (task == 2) {
                // task2 means we have 0.built 1.came back+reported and finally now we're
                // refilling
                if (turnCount % 2 == 0) {
                    rc.setIndicatorString("refilling paint");
                    // since we just went over to the tower to report, we're already adjacent so we
                    // can transfer
                    // check how much paint the tower has
                    int paint = rc.senseRobotAtLocation(goal).getPaintAmount();
                    if (rc.isActionReady()) {
                        // if its more than we need, then take the ammount we need
                        if (paint >= 200 - rc.getPaint()) {
                            rc.transferPaint(goal, -(200 - rc.getPaint()));
                        } else if (paint > 0) {
                            // if its less than we need, take it all
                            rc.transferPaint(goal, -paint);

                        }
                    }
                    // if our paint is good enogh
                    if (rc.getPaint() > 170) {
                        // task = 0 means we will go and explore again
                        task = 0;
                        if (targetLoc != null) {
                            goal = targetLoc;
                        } else {
                            goal = setFarthest(rc.getLocation(), 30);

                        }
                    }
                }
            } else if (task == 1) {
                // task1 means we found and built a tower, and now we're going back
                // if builttower is null then we are going back to refill paint instead
                // if we reached the tower
                if (rc.getLocation().isAdjacentTo(goal)) {
                    rc.setIndicatorString("at tower");
                    // if we built it
                    if (builtTower != null) {
                        rc.setIndicatorString("telling about new tower");

                        // if we've built a tower then tell it
                        if (rc.canSendMessage(goal)) {
                            rc.sendMessage(goal, buildMessage(1, builtTower));
                            builtTower = null;
                            if (rc.getPaint() != 200) {
                                task = 2;
                            } else {
                                // task = 0 means we will go and explore again
                                task = 0;
                                goal = setFarthest(rc.getLocation(), 30);
                            }
                        }
                    } else {
                        // we must just be refilling
                        if (rc.getPaint() != 200) {
                            task = 2;
                        } else {
                            // task = 0 means we will go and explore again
                            task = 0;
                            if (targetLoc != null) {
                                goal = targetLoc;
                            } else {
                                goal = setFarthest(rc.getLocation(), 30);

                            }
                        }
                    }
                }
            } else if (task == 0) {
                // if task it 0 then we're exploring
                // this runst if we're exploring and have not found a ruin
                if (targetLoc == null && turnCount % 2 == 0) {
                    rc.setIndicatorString("finding ruins");
                    // array of unbuilt ruins
                    ArrayList<MapLocation> emptyRuins = new ArrayList<>();
                    // sense ruins
                    MapLocation[] ruins = rc.senseNearbyRuins(-1);
                    // for each
                    for (int i = 0; i < ruins.length; i++) {
                        // towers are a type of robot, this checks if there is a tower on the ruin
                        if (rc.canSenseRobotAtLocation(ruins[i])) {
                            RobotInfo tower = rc.senseRobotAtLocation(ruins[i]);
                            // is it our our team
                            if (tower.getTeam() == rc.getTeam()) {
                                rc.setIndicatorString("found a freindly tower");

                                // if its a paint tower
                                if (isPaintTower(tower.getType())) {
                                    // add it to the allied towers
                                    if (!towers.contains(ruins[i])) {
                                        towers.add(ruins[i]);
                                    }
                                }
                            } else {
                                // its an enemy tower
                                rc.setIndicatorString("found an enemy tower");
                                // add it to found enemies
                                if (!foundE.contains(ruins[i])) {
                                    foundE.add(ruins[i]);
                                }
                            }
                        } else {
                            // no tower, this is an empty ruin
                            emptyRuins.add(ruins[i]);
                        }
                    }
                    // did we find any empty ruins?
                    if (emptyRuins.size() > 0) {
                        rc.setIndicatorString("found a ruin!");

                        // get closest ruin
                        targetLoc = getClosest(rc, emptyRuins);
                        // set it as the goal
                        goal = targetLoc;
                        justFound = 2;
                        unoptimalMoves = 0;
                        // set the tower type we'll build
                        if (rc.getMoney() < 1500) {
                            goalType = UnitType.LEVEL_ONE_MONEY_TOWER;
                        } else {
                            goalType = UnitType.LEVEL_ONE_PAINT_TOWER;
                        }
                    }
                } else if (targetLoc != null && rc.getLocation().isAdjacentTo(targetLoc)) {
                    // if we are building

                    // if the queue is empty
                    if (paintNext.size() == 0 && rc.isActionReady()) {
                        // maybe its a new find
                        if (justFound > 0) {
                            // its not painted
                            // get direction to the ruin
                            Direction dir = rc.getLocation().directionTo(targetLoc);

                            // the tile adjacent to the ruin on our side should be marked
                            MapLocation shouldBeMarked = targetLoc.subtract(dir);
                            // if no mark then mark
                            if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY
                                    && rc.canMarkTowerPattern(goalType, targetLoc)) {
                                rc.markTowerPattern(goalType, targetLoc);
                                System.out.println("Trying to build a tower at " + targetLoc);

                            }
                            // find nearby tiles
                            paintNext = getLocationsWithinRadiusSquared(targetLoc, 8);
                            justFound -= 1;
                        } else if (rc.getChips() < 1000) {
                            // we dont have enoguht chips
                            // just do nothing until we have enough
                        } else {
                            // this must be a known tower
                            // maybe its already built
                            // if another explorer built it for us
                            if (rc.canSenseRobotAtLocation(targetLoc)) {
                                System.out.println("Somoene else already built tower at " + targetLoc);

                                // yay we created

                                // now the task is to go back and refil on paint
                                if (towers.size() > 0) {
                                    // stop building, set the task to refil instead
                                    task = 1;
                                    goal = getClosest(rc, towers);
                                } else {
                                    // oh no, we dont know any towers
                                    // task3 has us look for towers
                                    task = 3;
                                    // set a random goal
                                    unoptimalMoves = 0;
                                    goal = setFarthest(rc.getLocation(), 30);
                                }
                                // if applicable add this as a new paint tower
                                if (isPaintTower(rc.senseRobotAtLocation(targetLoc).getType())) {
                                    towers.add(targetLoc);
                                }
                                // reset target and reset built
                                builtTower = null;
                                targetLoc = null;
                                paintNext.clear();
                                // maybe we can build it
                            } else if (rc.canSenseLocation(targetLoc.add(Direction.SOUTH).add(Direction.SOUTH))) {

                                // if we can detect the type
                                boolean paintTower = !rc
                                        .senseMapInfo(targetLoc.add(Direction.SOUTH).add(Direction.SOUTH))
                                        .getPaint()
                                        .isSecondary();
                                UnitType towerType;
                                if (paintTower) {
                                    towerType = UnitType.LEVEL_ONE_PAINT_TOWER;
                                } else {
                                    towerType = UnitType.LEVEL_ONE_MONEY_TOWER;

                                }

                                if (rc.canCompleteTowerPattern(towerType, targetLoc)) {
                                    System.out.println("built " + targetLoc);
                                    // Complete the ruin if we can.
                                    rc.completeTowerPattern(towerType, targetLoc);
                                    // yay we created
                                    // now the task is to go back and refil on paint
                                    if (towers.size() > 0) {
                                        // stop building, set the task to refil instead
                                        task = 1;
                                        goal = getClosest(rc, towers);
                                    } else {
                                        // oh no, we dont know any towers
                                        // task3 has us look for towers
                                        task = 3;
                                        // set a random goal
                                        unoptimalMoves = 0;
                                        goal = setFarthest(rc.getLocation(), 30);
                                    }
                                    // if applicable add this as a new paint tower
                                    if (paintTower) {
                                        towers.add(targetLoc);
                                    }
                                    // reset target and set built
                                    builtTower = targetLoc;
                                    targetLoc = null;
                                    // logs
                                    rc.setTimelineMarker("Tower built", 0, 255, 0);
                                    paintNext.clear();
                                } else {
                                    // we musve already built all the towers
                                    maxTowers = true;
                                    task = -1;
                                    targetLoc = null;
                                }
                            }
                        }
                    }
                    if (task == 0 && paintNext.size() != 0 && targetLoc != null
                            && rc.getLocation().isAdjacentTo(targetLoc)) {
                        for (int i = 0; i < paintNext.size(); i++) {
                            if (rc.canSenseLocation(paintNext.get(i))) {
                                // get the tile
                                MapInfo tile = rc.senseMapInfo(paintNext.get(i));
                                // if its enemy
                                if (tile.getPaint().isEnemy() && tile.getMark() != PaintType.EMPTY) {
                                    //do nothing
                                } else if (tile.getMark() != tile.getPaint() && tile.getMark() != PaintType.EMPTY) {
                                    // if it is incorreclty painted
                                    // get correct mark
                                    boolean useSecondaryColor = tile.getMark() == PaintType.ALLY_SECONDARY;
                                    // is within range
                                    if (rc.getLocation().distanceSquaredTo(paintNext.get(i)) < attackRange) {
                                        rc.setIndicatorString("painting " + paintNext.get(i));
                                        // is in range, paint it
                                        rc.attack(paintNext.get(i), useSecondaryColor);
                                        paintNext.remove(i);
                                        i--;
                                        // we're done paiting for this turn
                                        break;
                                    } else {
                                        // not in range
                                        Direction PatternTileDirection = moveTowards(rc, rc.getLocation(),
                                                paintNext.get(i));
                                        if (PatternTileDirection != null) {
                                            // if moving would make it in range
                                            if (paintNext.get(i).subtract(PatternTileDirection)
                                                    .distanceSquaredTo(rc.getLocation()) < attackRange) {
                                                rc.setIndicatorString("moving and painting");

                                                // move twards
                                                rc.move(PatternTileDirection);
                                                // now its within range so paint it
                                                rc.attack(paintNext.get(i), useSecondaryColor);
                                                paintNext.remove(i);
                                                i--;

                                                // we're done paiting for this turn
                                                break;
                                            }
                                        } else {
                                            // this tile is outside sensing range, correctly painted, or too far, remove
                                            // it
                                            paintNext.remove(i);
                                            i--;
                                        }
                                    }
                                }else {
                                    // this tile is outside sensing range, correctly painted, or too far, remove
                                    // it
                                    paintNext.remove(i);
                                    i--;
                                }
                            } else {
                                // this tile is outside sensing range, correctly painted, or too far, remove it
                                paintNext.remove(i);
                                i--;
                            }
                        }
                    }
                }
            }

            // if our paint is low, and we are building a tower
            if (rc.getPaint() <= 100 && task == 0) {
                rc.setIndicatorString("going to refil paint");
                paintNext.clear();
                // TODO: we need to ask others for tower locations if we have none(instead of
                // erroring)
                if (towers.size() > 0) {
                    // stop building, set the task to refil instead
                    task = 1;
                    goal = getClosest(rc, towers);
                } else {
                    // oh no, we dont know any towers
                    // task3 has us look for towers
                    task = 3;
                    // set a random goal
                    goal = setFarthest(rc.getLocation(), 30);
                    unoptimalMoves = 0;
                }
            }

            // if we havent moved and we're not next to the destination then move
            if (!goal.isAdjacentTo(rc.getLocation())) {
                rc.setIndicatorString("moving twards destination");

                // move twards destination
                Direction dir = moveTowards(rc, rc.getLocation(), goal);
                // if we got to this point theres no way we already moved
                if (dir != null) {
                    rc.move(dir);
                } else {
                    unoptimalMoves += 1;
                }
            } else {
                // we are already at destination
                unoptimalMoves = 10;
            }
            // if we still havent used attack just paint below us
            if (!rc.senseMapInfo(rc.getLocation()).getPaint().isAlly() && rc.canAttack(rc.getLocation())) {
                rc.attack(rc.getLocation());
            }
            // if this goal isnt going anyware
            if (unoptimalMoves > 3 && ((task == 0 && targetLoc == null) || task == 3)) {
                rc.setIndicatorString("SETTING NEW GOAL");

                unoptimalMoves = 0;
                goal = setFarthest(rc.getLocation(), 30);
            }
        }
    }

    public static void runSplasher(RobotController rc) throws GameActionException {

    }

    public static ArrayList<MapLocation> getLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        ArrayList<MapLocation> locations = new ArrayList<>();

        for (int x = center.x - radiusSquared; x <= center.x + radiusSquared; x++) {
            for (int y = center.y - radiusSquared; y <= center.y + radiusSquared; y++) {
                MapLocation location = new MapLocation(x, y);
                if (location.distanceSquaredTo(center) <= radiusSquared) {
                    locations.add(location);
                }
            }
        }

        return locations;
    }

    /**
     * Run a single turn for a Mopper. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runMopper(RobotController rc) throws GameActionException {
        // update the robot we're following
        if (following != null && rc.canSenseRobot(followingID)) {
            following = rc.senseRobot(followingID);
        }
        // if low on paint
        if (goal != null) {
            // go twards tower
            if (!rc.getLocation().isAdjacentTo(goal)) {
                Direction dir = moveTowards(rc, rc.getLocation(), goal);
                if (dir != null) {
                    rc.move(dir);
                }
            }
            if (rc.getLocation().isAdjacentTo(goal)) {
                // check how much paint the tower has
                int paint = rc.senseRobotAtLocation(goal).getPaintAmount();
                if (rc.isActionReady()) {
                    if (rc.getChips() > 5000 && rc.canUpgradeTower(goal)) {
                        rc.upgradeTower(goal);
                    } else {
                        // if its more than we need, then take the ammount we need
                        if (paint >= 100 - rc.getPaint()) {
                            rc.transferPaint(goal, -(100 - rc.getPaint()));
                        } else if (paint > 0) {
                            // if its less than we need, take it all
                            rc.transferPaint(goal, -paint);
                        }
                    }
                }

                // if our paint is full
                if (rc.getPaint() == 100) {
                    // goal null means we will behave normally
                    goal = null;

                }
            }

        } else {
            // once every 3 turns
            if (turnCount % 3 == 0) {
                // sense nearby bots
                bots nearbyBots = findNearbyBots(rc);
                // freinds
                for (int i = 0; i < nearbyBots.allies.size(); i++) {
                    RobotInfo ally = nearbyBots.allies.get(i);
                    if (isPaintTower(ally.getType()) && !towers.contains(ally.getLocation())) {
                        rc.setIndicatorString("added allied tower");
                        towers.add(ally.getLocation());
                    } else if (ally.getType() == UnitType.SOLDIER) {
                        if (following == null) {
                            // lets follow the explorer
                            rc.setIndicatorString("following ally");

                            following = ally;
                            followingID = ally.getID();
                        }
                    }
                }
            }
            // once every 2 turns
            if (turnCount % 2 == 0 && tileToClear == null) {
                // sense nearby tiles
                for (MapInfo patternTile : rc.senseNearbyMapInfos()) {
                    // if it has a mark and is enemy
                    if (patternTile.getMark() != PaintType.EMPTY && patternTile.getPaint().isEnemy()) {
                        rc.setIndicatorString("founed tile need to clear");
                        tileToClear = patternTile.getMapLocation();

                    }
                }

            }
            if (tileToClear != null) {
                if (rc.getLocation().isAdjacentTo(tileToClear)) {
                    // mop it
                    if (rc.canAttack(tileToClear)) {
                        rc.attack(tileToClear);
                        tileToClear = null;
                    }
                } else {
                    // if too far then move twards it
                    Direction dir = moveTowards(rc, rc.getLocation(), tileToClear);
                    if (dir != null) {
                        rc.move(dir);
                    }
                    // check if we're close enough now
                    if (rc.getLocation().isAdjacentTo(tileToClear)) {
                        // mop it
                        if (rc.canAttack(tileToClear)) {
                            rc.attack(tileToClear);
                            tileToClear = null;
                        }
                    }
                }
            } else {
                if (following != null) {
                    // if nothing to clear then follow
                    Direction dir = moveTowards(rc, rc.getLocation(), following.getLocation());
                    if (dir != null) {
                        rc.move(dir);
                    }
                }
            }
        }
        if (rc.getPaint() <= 50) {
            if (towers.size() > 0) {
                goal = getClosest(rc, towers);
            }
        }
        // mop swint randomly.
        // Direction dir = directions[rng.nextInt(directions.length)];
        // if (rc.canMopSwing(dir)) {
        // rc.mopSwing(dir);
        // System.out.println("Mop Swing! Booyah!");
        // }
    }

    static class bots {
        public ArrayList<RobotInfo> allies;
        public ArrayList<RobotInfo> enemies;

        public bots(ArrayList<RobotInfo> nearbyAllies, ArrayList<RobotInfo> nearbyEnemies) {
            allies = nearbyAllies;
            enemies = nearbyEnemies;
        }

    }

    public static boolean isPaintTower(UnitType unit) {
        return unit == UnitType.LEVEL_ONE_PAINT_TOWER || unit == UnitType.LEVEL_TWO_PAINT_TOWER
                || unit == UnitType.LEVEL_THREE_PAINT_TOWER;
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
        Direction optimalDirection = directionTo(currentLoc, endLoc);
        Direction[] orderedDirections = getSortedDirections(optimalDirection);
        for (int i = 0; i < orderedDirections.length; i++) {
            if (rc.canMove(orderedDirections[i])) {
                if (i != 0) {
                    unoptimalMoves = unoptimalMoves + 1;
                }
                directionMoved = orderedDirections[i];
                return orderedDirections[i];
            }
        }
        // ok bro, we're already at the destination so clearly this is bad
        unoptimalMoves = unoptimalMoves + 2;
        directionMoved = null;
        return null;
    }

    public static Direction directionTo(MapLocation currentLoc, MapLocation endLoc) {
        // check if it's right
        if (endLoc.x > currentLoc.x) {
            // is it also up
            if (endLoc.y > currentLoc.y) {
                // then move right+up
                return Direction.NORTHEAST;

            }
            // maybe its right+down then
            if (endLoc.y < currentLoc.y) {
                return Direction.SOUTHEAST;

                // if not then it's directly right(not up or down)
            } else {
                return Direction.EAST;

            }
            // alr bro it ain't that hard figure it out
        }
        // either we're going left or none of the rights worked
        if (endLoc.x < currentLoc.x) {
            // are we going left up
            if (endLoc.y > currentLoc.y) {
                return Direction.NORTHWEST;
            }
            // maybe left down
            if (endLoc.y < currentLoc.y) {
                return Direction.SOUTHWEST;

            } else {

                return Direction.WEST;

            }
        }
        // we arent going left or right if we reached this point
        // try up
        if (endLoc.y > currentLoc.y) {
            return Direction.NORTH;

        }
        // try down
        if (endLoc.y < currentLoc.y) {
            return Direction.SOUTH;

        }
        // ok bro, we're already at the destination
        return Direction.CENTER;
    }

    public static Direction[] getSortedDirections(Direction target) {
        Direction[] orderedDirections;

        switch (target) {
            case NORTH:
                orderedDirections = new Direction[] {
                        Direction.NORTH,
                        Direction.NORTHEAST,
                        Direction.NORTHWEST,
                        Direction.SOUTH,
                        Direction.SOUTHEAST,
                        Direction.SOUTHWEST,
                        Direction.EAST,
                        Direction.WEST
                };
                break;
            case NORTHEAST:
                orderedDirections = new Direction[] {
                        Direction.NORTHEAST,
                        Direction.NORTH,
                        Direction.EAST,
                        Direction.NORTHWEST,
                        Direction.SOUTHEAST,
                        Direction.SOUTH,
                        Direction.SOUTHWEST,
                        Direction.WEST
                };
                break;
            case NORTHWEST:
                orderedDirections = new Direction[] {
                        Direction.NORTHWEST,
                        Direction.NORTH,
                        Direction.WEST,
                        Direction.NORTHEAST,
                        Direction.SOUTHWEST,
                        Direction.SOUTH,
                        Direction.EAST,
                        Direction.SOUTHEAST
                };
                break;
            case SOUTH:
                orderedDirections = new Direction[] {
                        Direction.SOUTH,
                        Direction.SOUTHEAST,
                        Direction.SOUTHWEST,
                        Direction.NORTH,
                        Direction.EAST,
                        Direction.WEST,
                        Direction.NORTHEAST,
                        Direction.NORTHWEST
                };
                break;
            case SOUTHEAST:
                orderedDirections = new Direction[] {
                        Direction.SOUTHEAST,
                        Direction.SOUTH,
                        Direction.EAST,
                        Direction.SOUTHWEST,
                        Direction.NORTHEAST,
                        Direction.NORTH,
                        Direction.NORTHWEST,
                        Direction.WEST
                };
                break;
            case SOUTHWEST:
                orderedDirections = new Direction[] {
                        Direction.SOUTHWEST,
                        Direction.SOUTH,
                        Direction.WEST,
                        Direction.SOUTHEAST,
                        Direction.NORTHWEST,
                        Direction.NORTH,
                        Direction.EAST,
                        Direction.NORTHEAST
                };
                break;
            case EAST:
                orderedDirections = new Direction[] {
                        Direction.EAST,
                        Direction.NORTHEAST,
                        Direction.SOUTHEAST,
                        Direction.WEST,
                        Direction.NORTH,
                        Direction.SOUTH,
                        Direction.NORTHWEST,
                        Direction.SOUTHWEST
                };
                break;
            case WEST:
                orderedDirections = new Direction[] {
                        Direction.WEST,
                        Direction.NORTHWEST,
                        Direction.SOUTHWEST,
                        Direction.EAST,
                        Direction.NORTH,
                        Direction.SOUTH,
                        Direction.NORTHEAST,
                        Direction.SOUTHEAST
                };
                break;
            default:
                orderedDirections = new Direction[0]; // Empty array as a fallback
                break;
        }

        return orderedDirections;
    }

    public static void main(String[] args) {
        Direction target = Direction.NORTH;
        Direction[] sortedDirections = getSortedDirections(target);

        System.out.println("Sorted directions for " + target + ":");
        for (Direction direction : sortedDirections) {
            System.out.println(direction);
        }
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