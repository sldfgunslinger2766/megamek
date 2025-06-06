/*
 * MegaMek - Copyright (C) 2000-2011 Ben Mazur (bmazur@sev.org)
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.client.bot.princess;

import megamek.client.bot.princess.BotGeometry.CoordFacingCombo;
import megamek.common.BuildingTarget;
import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.EntityMovementType;
import megamek.common.moves.MovePath;
import megamek.common.Targetable;
import megamek.common.options.OptionsConstants;
import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * EntityState describes a hypothetical situation an entity could be in when firing
 *
 * @author Deric Page (deric dot page at usa dot net)
 * @since 12/18/13 9:28 AM
 */
public class EntityState {
    private Coords position;
    private int facing;
    private int secondaryFacing; // to account for torso twists
    private int heat;
    private final int hexesMoved;
    private final boolean prone;
    private final boolean immobile;
    private final boolean jumping;
    private final EntityMovementType movementType;
    private boolean building;
    private boolean aero;
    private boolean airborne;
    private final boolean naturalAptGun;
    private final boolean naturalAptPilot;

    /**
     * Initialize an entity state from the state an entity is actually in (or something that isn't an entity)
     */
    EntityState(Targetable target) {
        position = target.getPosition();
        facing = 0;
        hexesMoved = 0;
        heat = 0;
        prone = false;
        immobile = true;
        jumping = false;
        movementType = EntityMovementType.MOVE_NONE;
        setSecondaryFacing(0);
        building = (target instanceof BuildingTarget);
        aero = false;
        naturalAptGun = false;
        naturalAptPilot = false;
    }

    EntityState(Entity entity) {
        position = entity.getPosition();
        facing = entity.getFacing();
        hexesMoved = entity.delta_distance;
        heat = entity.heat;
        prone = entity.isProne() || entity.isHullDown();
        immobile = entity.isImmobile();
        jumping = (entity.moved == EntityMovementType.MOVE_JUMP);
        movementType = entity.moved;
        setSecondaryFacing(entity.getSecondaryFacing());
        building = false;
        aero = entity.isAero();
        airborne = entity.isAirborne() || entity.isAirborneVTOLorWIGE();
        naturalAptGun = entity.hasAbility(OptionsConstants.PILOT_APTITUDE_GUNNERY);
        naturalAptPilot = entity.hasAbility(OptionsConstants.PILOT_APTITUDE_PILOTING);
    }

    /**
     * Initialize an entity state from a movement path
     */
    EntityState(MovePath path) {
        position = path.getFinalCoords();
        facing = path.getFinalFacing();
        hexesMoved = path.getHexesMoved();
        heat = path.getEntity().heat;

        if (path.getLastStepMovementType() == EntityMovementType.MOVE_WALK) {
            heat = getHeat() + 1;
        } else if (path.getLastStepMovementType() == EntityMovementType.MOVE_RUN) {
            heat = getHeat() + 2;
        } else if ((path.getLastStepMovementType() == EntityMovementType.MOVE_JUMP) && (getHexesMoved() <= 3)) {
            heat = getHeat() + 3;
        } else if ((path.getLastStepMovementType() == EntityMovementType.MOVE_JUMP) && (getHexesMoved() > 3)) {
            heat = getHeat() + getHexesMoved();
        }

        prone = path.getFinalProne() || path.getFinalHullDown();
        immobile = path.getEntity().isImmobile();
        jumping = path.isJumping();
        movementType = path.getLastStepMovementType();
        naturalAptGun = path.getEntity().hasAbility(OptionsConstants.PILOT_APTITUDE_GUNNERY);
        naturalAptPilot = path.getEntity().hasAbility(OptionsConstants.PILOT_APTITUDE_PILOTING);
        setSecondaryFacing(getFacing());
    }

    /**
     * Create an entity state from a Targetable, but pretend it's in a different hex facing in a different direction.
     */
    EntityState(Targetable target, CoordFacingCombo projectedTargetLocation) {
        this(target);
        position = projectedTargetLocation.getCoords();
        facing = projectedTargetLocation.getFacing();
    }

    public Coords getPosition() {
        return position;
    }

    public int getFacing() {
        return facing;
    }

    public int getSecondaryFacing() {
        return secondaryFacing;
    }

    public int getHeat() {
        return heat;
    }

    public int getHexesMoved() {
        return hexesMoved;
    }

    public boolean isProne() {
        return prone;
    }

    public boolean isImmobile() {
        return immobile;
    }

    public boolean isJumping() {
        return jumping;
    }

    public EntityMovementType getMovementType() {
        return movementType;
    }

    public void setSecondaryFacing(int secondaryFacing) {
        this.secondaryFacing = secondaryFacing;
    }

    public boolean isBuilding() {
        return building;
    }

    public boolean isAero() {
        return aero;
    }

    public boolean isAirborne() {
        return airborne;
    }

    public boolean isAirborneAero() {
        return aero && airborne;
    }

    public boolean hasNaturalAptGun() {
        return naturalAptGun;
    }

    public boolean hasNaturalAptPiloting() {
        return naturalAptPilot;
    }

    @Override
    public String toString() {
        return new ParameterizedMessage("EntityState{ position = {}, movementType = {}, facing = {}, secondaryFacing" +
                                              " = {}, heat = {}, hexesMoved = {}, prone = {}, immobile = {}, building" +
                                              " = {}, aero = {}, airborne = {}, naturalAptGun = {}, naturalAptPilot = {}, }",
              position,
              movementType,
              facing,
              secondaryFacing,
              heat,
              hexesMoved,
              prone,
              immobile,
              jumping,
              building,
              aero,
              airborne,
              naturalAptGun,
              naturalAptPilot).getFormattedMessage();
    }
}
