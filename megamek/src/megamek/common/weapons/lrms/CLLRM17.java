/*
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
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
package megamek.common.weapons.lrms;

/**
 * @author Sebastian Brocks
 */
public class CLLRM17 extends LRMWeapon {
    private static final long serialVersionUID = 2286924098199359381L;

    public CLLRM17() {
        super();

        name = "LRM 17";
        setInternalName("CLLRM17");
        heat = 0;
        rackSize = 17;
        minimumRange = WEAPON_NA;
        tonnage = 3.4;
        criticals = 0;
        bv = 215;
        // Per Herb all ProtoMek launcher use the ProtoMek Chassis progression.
        // But LRM Tech Base and Avail Ratings.
        rulesRefs = "231, TM";
        flags = flags.or(F_NO_FIRES).andNot(F_AERO_WEAPON).andNot(F_BA_WEAPON)
        		.andNot(F_MEK_WEAPON).andNot(F_TANK_WEAPON);
        techAdvancement.setTechBase(TechBase.CLAN)
                .setIntroLevel(false)
                .setUnofficial(false)
                .setTechRating(TechRating.F)
                .setAvailability(AvailabilityValue.X, AvailabilityValue.X, AvailabilityValue.C, AvailabilityValue.C)
                .setClanAdvancement(3055, 3060, 3061, DATE_NONE, DATE_NONE)
                .setClanApproximate(true, false, false, false, false)
                .setPrototypeFactions(Faction.CSJ)
                .setProductionFactions(Faction.CSJ);
    }
}
