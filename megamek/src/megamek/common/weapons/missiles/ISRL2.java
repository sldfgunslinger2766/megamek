/*
 * Copyright (c) 2005 - Ben Mazur (bmazur@sev.org).
 * Copyright (c) 2022 - The MegaMek Team. All Rights Reserved.
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
package megamek.common.weapons.missiles;

import java.io.Serial;

/**
 * Duplicate of IS BA RL, not available to meks at this rack size Commented out in {@link megamek.common.WeaponType}
 *
 * @author Sebastian Brocks
 */
public class ISRL2 extends RLWeapon {
    @Serial
    private static final long serialVersionUID = -3501679876316953438L;

    public ISRL2() {
        super();
        name = "Rocket Launcher 2";
        setInternalName("RL2");
        addLookupName("RL 2");
        addLookupName("ISRocketLauncher2");
        addLookupName("IS RLauncher-2");
        rackSize = 2;
        shortRange = 3;
        mediumRange = 7;
        longRange = 12;
        extremeRange = 18;
        bv = 3;
        rulesRefs = "229, TM";
        techAdvancement.setTechBase(TechBase.ALL)
              .setIntroLevel(false)
              .setUnofficial(true)
              .setTechRating(TechRating.B)
              .setAvailability(AvailabilityValue.B, AvailabilityValue.B, AvailabilityValue.B, AvailabilityValue.B)
              .setISAdvancement(DATE_ES, 3064, 3067, DATE_NONE, DATE_NONE)
              .setISApproximate(false, false, false, false, false)
              .setClanAdvancement(DATE_ES, DATE_NONE, DATE_NONE, 2823, DATE_NONE)
              .setClanApproximate(false, false, false, false, false)
              .setProductionFactions(Faction.MH);
    }
}
