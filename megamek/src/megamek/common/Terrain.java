/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package megamek.common;

import static megamek.common.Terrains.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import megamek.common.annotations.Nullable;
import megamek.common.options.OptionsConstants;

/**
 * Represents a single type of terrain or condition in a hex. The type of a
 * terrain is immutable, once created, but the level and exits are changeable.
 * Each type of terrain should only be represented once in a hex.
 *
 * @author Ben
 */
public class Terrain implements Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = -7624691566755134033L;

    /**
     * A terrain level value for use when the terrain isn't present. Must remain negative as many checks rely on
     * this.
     */
    public static final int LEVEL_NONE = Integer.MIN_VALUE;
    public static final int WILDCARD = Integer.MAX_VALUE;
    public static final int ATLEAST = Integer.MAX_VALUE - 1000;

    private final int type;
    /**
     * Terrain level, which is used to indicate varying severity of terrain
     * types (ie, Light Woods vs Heavy woods). Not to be confused with Hex
     * levels.
     */
    private int level;
    private boolean exitsSpecified;
    private int exits;
    private int terrainFactor;
    //endregion Variable Declarations

    //region Constructors
    public Terrain(int type, int level) {
        this(type, level, false, 0);
    }

    public Terrain(int type, int level, boolean exitsSpecified, int exits) {
        this.type = type;
        this.level = level;
        this.exitsSpecified = exitsSpecified;
        this.exits = exits;
        terrainFactor = Terrains.getTerrainFactor(type, level);
    }

    public Terrain(Terrain other) {
        type = other.getType();
        level = other.getLevel();
        exitsSpecified = other.hasExitsSpecified();
        exits = other.getExits();
        terrainFactor = other.getTerrainFactor();
    }

    /**
     * Parses a string containing terrain info into the actual terrain
     */
    public Terrain(String terrain) {
        // should have at least one colon, maybe two
        int firstColon = terrain.indexOf(':');
        int lastColon = terrain.lastIndexOf(':');
        String name = terrain.substring(0, firstColon);

        type = Terrains.getType(name);
        if (firstColon == lastColon) {
            level = levelFor(terrain.substring(firstColon + 1));

            // Buildings *never* use implicit exits.
            exitsSpecified = (type == BUILDING) || (type == FUEL_TANK);
        } else {
            level = levelFor(terrain.substring(firstColon + 1, lastColon));
            exitsSpecified = true;
            exits = levelFor(terrain.substring(lastColon + 1));
        }
        terrainFactor = Terrains.getTerrainFactor(type, level);
    }
    //endregion Constructors

    public static int levelFor(String string) {
        if (string.equals("*")) {
            return WILDCARD;
        }
        return Integer.parseInt(string);
    }

    public int getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getTerrainFactor() {
        return terrainFactor;
    }

    public void setTerrainFactor(int tf) {
        terrainFactor = tf;
    }

    public int getTerrainElevation(boolean inAtmosphere) {
        return Terrains.getTerrainElevation(type, level, inAtmosphere);
    }

    public int getExits() {
        return exits;
    }

    public boolean hasExitsSpecified() {
        return exitsSpecified;
    }

    public void setExits(int exits) {
        this.exits = exits;
    }

    /**
     * Sets the exit in specified direction
     *
     * @param direction the direction to add/remove the exit
     * @param connection true to add, false to remove
     */
    public void setExit(int direction, boolean connection) {
        int mask = (int) Math.pow(2, direction);
        if (connection) {
            exits |= mask;
        } else {
            exits &= (63 ^ mask);
        }
    }

    /**
     * Flips the exits around the vertical axis (North-for-South) and/or the horizontal axis (East-for-West).
     *
     * @param horiz a <code>boolean</code> value that, if <code>true</code>, indicates that the
     *              exits are being flipped North-for-South.
     * @param vert a <code>boolean</code> value that, if <code>true</code>, indicates that the exits
     *             are being flipped East-for-West.
     */
    public void flipExits(boolean horiz, boolean vert) {
        // Do nothing if no flips are defined.
        if (!horiz && !vert) {
            return;
        }

        // Determine the new exits.
        int newExits = 0;

        // Is there a North exit?
        if (0 != (exits & 0x0001)) {
            if (vert) {
                // Becomes South.
                newExits |= 0x08;
            }
        }
        // Is there a North-East exit?
        if (0 != (exits & 0x0002)) {
            if (vert && horiz) {
                // Becomes South-West
                newExits |= 0x10;
            } else if (horiz) {
                // Becomes North-West.
                newExits |= 0x20;
            } else if (vert) {
                // Becomes South-East.
                newExits |= 0x04;
            }
        }
        // Is there a South-East exit?
        if (0 != (exits & 0x0004)) {
            if (vert && horiz) {
                // Becomes North-West
                newExits |= 0x20;
            } else if (horiz) {
                // Becomes South-West.
                newExits |= 0x10;
            } else if (vert) {
                // Becomes North-East.
                newExits |= 0x02;
            }
        }
        // Is there a South exit?
        if (0 != (exits & 0x0008)) {
            if (vert) {
                // Becomes North.
                newExits |= 0x01;
            }
        }
        // Is there a South-West exit?
        if (0 != (exits & 0x0010)) {
            if (vert && horiz) {
                // Becomes North-East
                newExits |= 0x02;
            } else if (horiz) {
                // Becomes South-East.
                newExits |= 0x04;
            } else if (vert) {
                // Becomes North-West.
                newExits |= 0x20;
            }
        }
        // Is there a North-West exit?
        if (0 != (exits & 0x0020)) {
            if (vert && horiz) {
                // Becomes South-East
                newExits |= 0x04;
            } else if (horiz) {
                // Becomes North-East.
                newExits |= 0x02;
            } else if (vert) {
                // Becomes South-West.
                newExits |= 0x10;
            }
        }

        // Update the exits.
        setExits(newExits);

    }

    /**
     * @return true if the terrain in this hex exits to the terrain in the other hex.
     */
    public boolean exitsTo(Terrain other) {
        if (other == null) {
            return false;
        }
        // Check to see if we've got a type that can have exits
        boolean exitableTerrainType = exitableTerrain(type);
        // Buildings shouldn't connect across different types (= level)
        // but all others should
        return (type == other.getType()) && exitableTerrainType && (type != BUILDING || level == other.getLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, type);
    }

    /**
     * Terrains are equal if their types and levels are equal. Does not pay
     * attention to exits.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if ((object == null) || (getClass() != object.getClass())) {
            return false;
        }
        final Terrain other = (Terrain) object;
        return (type == other.type) && (level == other.level);
        // Ints don't need special handling. For more complex objects use:
        // return Objects.equals(level, other.level) && Objects.equals(type,
        // other.type);
    }

    @Override
    public String toString() {
        return getName(type) + ":" + level + (exitsSpecified ? ":" + exits : "");
    }

    /**
     * @param moveMode the movement mode of the pilot
     * @param roll the piloting roll
     * @param enteringRubble if the entered terrain contains rubble
     */
    public void applyPilotingModifier(EntityMovementMode moveMode, PilotingRollData roll, boolean enteringRubble) {
        switch (type) {
            case JUNGLE:
                if (level == 3) {
                    roll.addModifier(level, "Ultra Jungle");
                }
                if (level == 2) {
                    roll.addModifier(level, "Heavy Jungle");
                }
                if (level == 1) {
                    roll.addModifier(level, "Jungle");
                }
                break;
            case MAGMA:
                if (level == 2) {
                    roll.addModifier(4, "Liquid Magma");
                }
                if (level == 1) {
                    roll.addModifier(1, "Magma Crust");
                }
                break;
            case TUNDRA:
                roll.addModifier(1, "Tundra");
                break;
            case SAND:
                roll.addModifier(1, "Sand");
                break;
            case SNOW:
                if (level == 2) {
                    roll.addModifier(1, "Deep Snow");
                }
                break;
            case SWAMP:
                if ((moveMode == EntityMovementMode.BIPED) || (moveMode == EntityMovementMode.QUAD)) {
                    roll.addModifier(1, "Swamp");
                } else {
                    roll.addModifier(2, "Swamp");
                }
                break;
            case MUD:
                if ((moveMode != EntityMovementMode.HOVER) && (moveMode != EntityMovementMode.WIGE)) {
                    roll.addModifier(1, "Mud");
                }
                break;
            case GEYSER:
                if (level == 2) {
                    roll.addModifier(1, "Active Geyser");
                    break;
                }
            case RUBBLE:
                if (level == 6) {
                    if (enteringRubble) {
                        roll.addModifier(1, "entering Ultra Rubble");
                    } else {
                        roll.addModifier(1, "Ultra Rubble");

                    }
                }
                if (level < 6) {
                    if (enteringRubble) {
                        roll.addModifier(0, "entering Rubble");
                    } else {
                        roll.addModifier(0, "Rubble");
                    }
                }
                break;
            case RAPIDS:
                if (level == 2) {
                    roll.addModifier(3, "Torrent");
                } else {
                    roll.addModifier(2, "Rapids");
                }
                break;
            case ICE:
                if ((moveMode != EntityMovementMode.HOVER) && (moveMode != EntityMovementMode.WIGE)) {
                    roll.addModifier(4, "Ice");
                }
                break;
            case BLACK_ICE:
                if ((moveMode != EntityMovementMode.HOVER) && (moveMode != EntityMovementMode.WIGE)) {
                    roll.addModifier(4, "Black Ice");
                }
                break;
            case INDUSTRIAL:
                roll.addModifier(1, "Industrial Zone");
                break;
            default:
                break;
        }
    }

    /**
     * @return The additional movement cost for this terrain
     */
    public int movementCost(Entity e) {
        EntityMovementMode moveMode = e.getMovementMode();
        int mp;
        boolean isCrossCountry = e.hasAbility(OptionsConstants.PILOT_CROSS_COUNTRY);

        switch (type) {
            case MAGMA:
                return level - 1;
            case GEYSER:
                if (level == 2) {
                    return 1;
                }
                return 0;
            case RUBBLE:
                boolean allowRubbleHoverTracked = ((moveMode == EntityMovementMode.HOVER) || (moveMode == EntityMovementMode.TRACKED)) && (level == 6);

                if (level == 6) {
                    mp = 2;
                } else {
                    mp = 1;
                }

                if (isCrossCountry && e.isGround() && e.isCombatVehicle()) {
                    if (allowRubbleHoverTracked || (moveMode == EntityMovementMode.WHEELED)) {
                        mp *= 2;
                    }
                }

                if ((e instanceof Mek) && e.isSuperHeavy()) {
                    mp -= 1;
                }

                if (e.hasAbility(OptionsConstants.PILOT_TM_MOUNTAINEER)) {
                    mp -= 1;
                }

                if ((e.hasAbility(OptionsConstants.INFANTRY_FOOT_CAV)
                        && (moveMode == EntityMovementMode.INF_LEG))) {
                    mp -= 1;
                }
                return Math.max(0, mp);
            case WOODS:
                mp = level;
                if (isCrossCountry && e.isGround() && e.isCombatVehicle()) {
                    if (((level == 1) && ((moveMode == EntityMovementMode.HOVER) || (moveMode == EntityMovementMode.WHEELED)))
                            || (level > 1)) {
                        mp *= 2;
                    }
                }

                if ((e instanceof Mek) && e.isSuperHeavy()) {
                    mp -= 1;
                }

                if (e.hasAbility(OptionsConstants.PILOT_TM_FOREST_RANGER)) {
                    mp -= 1;
                }

                if ((e.hasAbility(OptionsConstants.INFANTRY_FOOT_CAV)
                                && (moveMode == EntityMovementMode.INF_LEG))) {
                    mp -= 1;
                }

                if (e.hasAbility(OptionsConstants.PILOT_ANIMAL_MIMIC)) {
                    if ((e.entityIsQuad()) || ((moveMode == EntityMovementMode.BIPED) && e.hasQuirk("animalistic"))) {
                        mp -= 1;
                    }
                }
                return Math.max(0, mp);
            case JUNGLE:
                mp = level +1;
                if (isCrossCountry && e.isGround() && e.isCombatVehicle()) {
                    mp *= 2;
                }

                if ((e instanceof Mek) && e.isSuperHeavy()) {
                    mp -= 1;
                }

                if (e.hasAbility(OptionsConstants.PILOT_TM_FOREST_RANGER)) {
                    mp -= 1;
                }

                if ((e.hasAbility(OptionsConstants.INFANTRY_FOOT_CAV)
                        && (moveMode == EntityMovementMode.INF_LEG))) {
                    mp -= 1;
                }

                if (e.hasAbility(OptionsConstants.PILOT_ANIMAL_MIMIC)) {
                    if ((e.entityIsQuad()) || ((moveMode == EntityMovementMode.BIPED) && e.hasQuirk("animalistic"))) {
                        mp -= 1;
                    }
                }
                return Math.max(0, mp);
            case SNOW:
                if (level == 2) {
                    if ((moveMode == EntityMovementMode.HOVER) || (moveMode == EntityMovementMode.WIGE)) {
                        return 0;
                    }
                    return 1;
                }
                if ((moveMode == EntityMovementMode.WHEELED) || (e.isConventionalInfantry() &&
                        ((moveMode == EntityMovementMode.INF_JUMP)
                          || (moveMode == EntityMovementMode.INF_LEG)
                          || (moveMode == EntityMovementMode.INF_MOTORIZED)))) {
                    return 1;
                }
                return 0;
            case MUD:
                if (moveMode.isHoverOrWiGE() || e.isNaval()) {
                    return 0;
                }
                if (e.hasAbility(OptionsConstants.PILOT_TM_SWAMP_BEAST)) {
                    return 0;
                }
                return 1;
            case SWAMP:
                mp = 2;
                if ((moveMode == EntityMovementMode.HOVER) || (moveMode == EntityMovementMode.WIGE)) {
                    return 0;
                }
                if (e.hasAbility(OptionsConstants.PILOT_TM_SWAMP_BEAST)) {
                    mp -= 1;
                }
                if ((moveMode == EntityMovementMode.BIPED) || (moveMode == EntityMovementMode.QUAD)) {
                    mp -= 1;
                }
                return Math.max(0, mp);
            case ICE:
            case BLACK_ICE:
                if ((moveMode == EntityMovementMode.HOVER) || (moveMode == EntityMovementMode.WIGE)) {
                    return 0;
                }
                return 1;
            case RAPIDS:
                // Doesn't apply to Hover, or airborne WiGE or VTOL
                if (e.isAirborneVTOLorWIGE() || (e.getMovementMode() == EntityMovementMode.HOVER)) {
                    return 0;
                }

                if (level == 2) {
                    mp = 2;
                } else {
                    mp = 1;
                }

                if ((e instanceof Mek) && e.isSuperHeavy()) {
                    mp -= 1;
                }
                return Math.max(0, mp);
            case ROUGH:
                boolean allowRoughHoverTracked = ((moveMode == EntityMovementMode.HOVER) || (moveMode == EntityMovementMode.TRACKED)) && (level == 2);

                if (level == 2) {
                    mp = 2;
                } else {
                    mp = 1;
                }

                if (isCrossCountry && e.isGround() && e.isCombatVehicle()) {
                    if ( allowRoughHoverTracked || (moveMode == EntityMovementMode.WHEELED)) {
                        mp *= 2;
                    }
                }

                if ((e instanceof Mek) && e.isSuperHeavy()) {
                    mp -= 1;
                }

                if (e.hasAbility(OptionsConstants.PILOT_TM_MOUNTAINEER)) {
                    mp -= 1;
                }

                if ((e.hasAbility(OptionsConstants.INFANTRY_FOOT_CAV)
                        && (moveMode == EntityMovementMode.INF_LEG))) {
                    mp -= 1;
                }
                return Math.max(0, mp);
            case SAND:
                if (((moveMode == EntityMovementMode.WHEELED) && !e.hasWorkingMisc(MiscType.F_DUNE_BUGGY))
                        || (moveMode == EntityMovementMode.INF_JUMP) || (moveMode == EntityMovementMode.INF_LEG)
                        || (moveMode == EntityMovementMode.INF_MOTORIZED)) {
                    return 1;
                } else {
                    return 0;
                }
            case INDUSTRIAL:
                if ((moveMode == EntityMovementMode.BIPED) || (moveMode == EntityMovementMode.QUAD)) {
                    return 1;
                } else {
                    return 0;
                }
            default:
                return 0;
        }
    }

    /**
     * The fire ignition modifier for this terrain
     */
    public int ignitionModifier() {
        switch (type) {
            case JUNGLE:
                return 1;
            case SNOW:
                return (level == 2) ? 2 : 0;
            case FIELDS:
                return -1;
            default:
                return 0;
        }
    }

    public int getBogDownModifier(EntityMovementMode moveMode, boolean largeVee) {
        // hovercraft and WiGE don't bog down. Naval units never touch the floor and thus don't bog down.
        if (moveMode.isHoverOrWiGE() || moveMode.isNaval()) {
            return TargetRoll.AUTOMATIC_SUCCESS;
        }

        switch (type) {
            case SWAMP:
                // if this is quicksand, then you automatically fail
                if (level > 1) {
                    return TargetRoll.AUTOMATIC_FAIL;
                } else if (moveMode.isVTOL()) {
                    return TargetRoll.AUTOMATIC_FAIL;
                } else {
                    return 0;
                }
            case MAGMA:
                // Only applies when jumping into a hex.
                return (level == 2) ? 0 : TargetRoll.AUTOMATIC_SUCCESS;
            case MUD:
                if (moveMode.isBiped() || moveMode.isQuad()) {
                    return TargetRoll.AUTOMATIC_SUCCESS;
                    // any kind of infantry just gets a flat roll
                } else if (moveMode.isLegInfantry() || moveMode.isMotorizedInfantry()
                        || moveMode.isJumpInfantry() || moveMode.isUMUInfantry()) {
                    return 0;
                } else {
                    return -1;
                }
            case TUNDRA:
                return -1;
            case SNOW:
                return (level == 2) ? -1 : TargetRoll.AUTOMATIC_SUCCESS;
            case SAND:
                return largeVee ? 0 : TargetRoll.AUTOMATIC_SUCCESS;
            default:
                return TargetRoll.AUTOMATIC_SUCCESS;
        }
    }

    public void getUnstuckModifier(int elev, PilotingRollData rollTarget) {
        switch (type) {
            case SWAMP:
                if (level > 1) {
                    rollTarget.addModifier((3 + (-3 * elev)), "Quicksand");
                } else {
                    rollTarget.addModifier(0, "Swamp");
                }
                break;
            case MAGMA:
                if (level == 2) {
                    rollTarget.addModifier(0, "Liquid Magma");
                }
                break;
            case MUD:
                rollTarget.addModifier(-1, "Mud");
                break;
            case TUNDRA:
                rollTarget.addModifier(-1, "Tundra");
                break;
            case SNOW:
                rollTarget.addModifier(-1, "Deep Snow");
                break;
            default:
                break;
        }
    }

    /**
     * Returns true when this terrain is valid, i.e. if its level is an allowed value for its type.
     * Exits have no limitations and are not checked. If an error is found, a line detailing the
     * error is added to the given errors list if it not null.
     *
     * @param errors A list of errors to append new errors to if it is not null
     * @return True when this terrain is valid, false otherwise
     */
    public boolean isValid(@Nullable List<String> errors) {
        boolean valid = true;
        if (((type == WOODS)
                || (type == SWAMP)
                || (type == JUNGLE)
                || (type == GEYSER)
                || (type == FOLIAGE_ELEV))
                && ((level < 1) || (level > 3))) {
            valid = false;
        } else if (((type == ROUGH)
                || (type == RAPIDS)
                || (type == MAGMA)
                || (type == MUD)
                || (type == SNOW))
                && ((level < 1) || (level > 2))) {
            valid = false;
        } else if (type == WATER && (level < 0)) {
            valid = false;
        } else if (((type == ICE)
                || (type == BLACK_ICE)
                || (type == FORTIFIED)
                || (type == TUNDRA))
                && (level != 1)) {
            valid = false;
        } else if (type == RUBBLE && (level < 1 || level > 6)) {
            valid = false;
        } else if (type == FIRE && (level < 1 || level > 4)) {
            valid = false;
        } else if (type == SMOKE && (level < 1 || level > 5)) {
            valid = false;
        } else if (((type == PAVEMENT)
                || (type == BRIDGE)
                || (type == BLDG_ELEV)
                || (type == BLDG_CF)
                || (type == BRIDGE_CF)
                || (type == FUEL_TANK_CF))
                && level < 1) {
            valid = false;
        } else if ((type == BRIDGE_ELEV) && (level < 0)) {
            valid = false;
        } else if ((type == HAZARDOUS_LIQUID) && (level < 0 || level > 3)) {
            valid = false;
        }

        if (!valid && (errors != null)) {
            errors.add("Illegal level: " + this);
        }

        return valid;
    }
}
