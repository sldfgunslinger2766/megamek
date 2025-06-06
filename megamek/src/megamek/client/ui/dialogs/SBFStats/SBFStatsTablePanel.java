/*
 * Copyright (C) 2021-2025 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 *
 * MechWarrior Copyright Microsoft Corporation. MegaMek was created under
 * Microsoft's "Game Content Usage Rules"
 * <https://www.xbox.com/en-US/developers/rules> and it is not endorsed by or
 * affiliated with Microsoft.
 */
package megamek.client.ui.dialogs.SBFStats;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Collection;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SpringLayout;

import megamek.client.ui.dialogs.abstractDialogs.ASConversionInfoDialog;
import megamek.client.ui.clientGUI.calculationReport.FlexibleCalculationReport;
import megamek.client.ui.panels.alphaStrike.ASStatsTablePanel;
import megamek.client.ui.util.SpringUtilities;
import megamek.client.ui.util.UIUtil;
import megamek.common.annotations.Nullable;
import megamek.common.strategicBattleSystems.SBFFormation;
import megamek.common.strategicBattleSystems.SBFUnit;

/**
 * A panel with a table of Strategic Battle Force stats for any SBF Formations that are added to it. The table will show
 * buttons for calling up conversion reports.
 */
public class SBFStatsTablePanel {

    public static final int COLUMNS = 13;

    private final JFrame parent;
    private final boolean showElements;
    private final Collection<SBFFormation> formations;
    private final Box contentPane = Box.createVerticalBox();

    /**
     * Constructs the panel for the given formations. The given frame is needed as a parent to the conversion report
     * windows.
     *
     * @param formations   The SBF formations to show
     * @param parent       The parent frame (important for giving a parent to conversion report dialogs)
     * @param showElements When true, the elements in the formations are also listed
     */
    public SBFStatsTablePanel(JFrame parent, Collection<SBFFormation> formations, boolean showElements) {
        this.parent = parent;
        this.formations = formations;
        this.showElements = showElements;
        updatePanel();
    }

    /** @return The contracted stats table panel. */
    public JComponent getPanel() {
        return contentPane;
    }

    private void addBetaInfo() {
        var betaLabel = new JLabel(UIUtil.WARNING_SIGN +
                                         " Please note: SBF conversion is currently in development " +
                                         "and results may change in future releases.");
        betaLabel.setFont(UIUtil.getDefaultFont());
        betaLabel.setForeground(UIUtil.uiYellow());
        betaLabel.setAlignmentX(0.5f);
        contentPane.add(Box.createVerticalStrut(30));
        contentPane.add(betaLabel);
        contentPane.add(Box.createVerticalStrut(30));
    }

    private void updatePanel() {
        contentPane.removeAll();
        addBetaInfo();
        for (SBFFormation formation : formations) {
            contentPane.add(formationPanel(formation));
        }
        contentPane.revalidate();
        contentPane.repaint();
    }

    private Component formationPanel(SBFFormation formation) {
        Box formationPanel = Box.createVerticalBox();
        JPanel summaryPanel = new JPanel(new SpringLayout());

        addFormationHeaders(summaryPanel);
        addGroupName(summaryPanel, formation.getName());
        addGridElement(summaryPanel, formation.getType().toString(), UIUtil.uiDarkBlue());
        addGridElement(summaryPanel, formation.getSize() + "", UIUtil.uiDarkBlue());
        addGridElement(summaryPanel, formation.getMovement() + formation.getMovementCode() + "", UIUtil.uiDarkBlue());
        addGridElement(summaryPanel, formation.getJumpMove() + "", UIUtil.uiDarkBlue());
        addGridElement(summaryPanel,
              formation.getTrspMovement() + formation.getTrspMovementCode() + "",
              UIUtil.uiDarkBlue());
        addGridElement(summaryPanel, formation.getTmm() + "", UIUtil.uiDarkBlue());
        addGridElement(summaryPanel, formation.getTactics() + "", UIUtil.uiDarkBlue());
        addGridElement(summaryPanel, formation.getMorale() + "", UIUtil.uiDarkBlue());
        addGridElement(summaryPanel, formation.getSkill() + "", UIUtil.uiDarkBlue());
        addGridElement(summaryPanel, formation.getPointValue() + "", UIUtil.uiDarkBlue());
        addGridElement(summaryPanel,
              formation.getSpecialsDisplayString(", ", formation) + "",
              UIUtil.uiDarkBlue(),
              FlowLayout.LEFT);
        addConversionInfo(summaryPanel, (FlexibleCalculationReport) formation.getConversionReport(), parent);

        addUnitHeaders(summaryPanel);
        int row = 1;
        for (SBFUnit unit : formation.getUnits()) {
            boolean oddRow = (row++ % 2) == 1;
            Color bgColor = oddRow ? UIUtil.alternateTableBGColor() : null;
            addGridElement(summaryPanel, "  " + unit.getName(), bgColor, FlowLayout.LEFT);
            addGridElement(summaryPanel, unit.getType().toString(), bgColor);
            addGridElement(summaryPanel, unit.getSize() + "", bgColor);
            addGridElement(summaryPanel, unit.getMovement() + unit.getMovementCode(), bgColor);
            addGridElement(summaryPanel, unit.getJumpMove() + "", bgColor);
            addGridElement(summaryPanel, unit.getTrspMovement() + unit.getTrspMovementCode(), bgColor);
            addGridElement(summaryPanel, unit.getTmm() + "", bgColor);
            addGridElement(summaryPanel, unit.getArmor() + "", bgColor);
            addGridElement(summaryPanel, unit.getDamage() + "", bgColor);
            addGridElement(summaryPanel, unit.getSkill() + "", bgColor);
            addGridElement(summaryPanel, unit.getPointValue() + "", bgColor);
            addGridElement(summaryPanel, unit.getSpecialsDisplayString(", ", unit), bgColor, FlowLayout.LEFT);
            addGridElement(summaryPanel, "", bgColor);
        }
        SpringUtilities.makeCompactGrid(summaryPanel, summaryPanel.getComponentCount() / COLUMNS, COLUMNS, 5, 5, 1, 5);
        formationPanel.add(summaryPanel);

        if (showElements) {
            var p = new ASStatsTablePanel(null);
            formation.getUnits().forEach(u -> p.add(u.getElements(), u.getName()));
            formationPanel.add(p.getPanel());
        }
        formationPanel.add(Box.createVerticalStrut(25));
        return formationPanel;
    }


    private void addConversionInfo(JComponent targetPanel, FlexibleCalculationReport conversionReport, JFrame frame) {
        var panel = new UIUtil.FixedYPanel();
        JButton button = new JButton("?");
        button.setFont(UIUtil.getDefaultFont());
        button.setEnabled(conversionReport != null);
        button.addActionListener(e -> new ASConversionInfoDialog(frame, conversionReport).setVisible(true));
        panel.add(button);
        targetPanel.add(panel);
    }

    private void addGridElement(JComponent targetPanel, String text, Color bgColor) {
        addGridElement(targetPanel, text, bgColor, FlowLayout.CENTER);
    }

    private void addGridElement(JComponent targetPanel, String text, Color bgColor, int alignment) {
        writeGridElement(targetPanel, text, bgColor, alignment, null);
    }

    private void writeGridElement(JComponent targetPanel, String text, Color bgColor, int alignment,
          @Nullable Color textColor) {
        var panel = new UIUtil.FixedYPanel(new FlowLayout(alignment));
        panel.setBackground(bgColor);
        panel.setForeground(textColor);
        var textLabel = new JLabel(text);
        textLabel.setForeground(textColor);
        textLabel.setFont(UIUtil.getDefaultFont());
        panel.add(textLabel);
        targetPanel.add(panel);
    }

    private void addHeader(JComponent targetPanel, String text, float alignment) {
        writeHeader(targetPanel, text, alignment, UIUtil.uiLightBlue());
    }

    private void addHeader(JComponent targetPanel, String text) {
        addHeader(targetPanel, text, JComponent.CENTER_ALIGNMENT);
    }

    private void addGroupName(JComponent targetPanel, String text) {
        writeGridElement(targetPanel, text, UIUtil.uiDarkBlue(), FlowLayout.LEFT, UIUtil.uiLightGreen());
    }

    private void writeHeader(JComponent targetPanel, String text, float alignment, Color color) {
        var namePanel = new UIUtil.FixedYPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.PAGE_AXIS));
        var textLabel = new JLabel(text);
        textLabel.setAlignmentX(alignment);
        textLabel.setForeground(color);
        textLabel.setFont(UIUtil.getDefaultFont());
        namePanel.add(textLabel);
        namePanel.add(Box.createVerticalStrut(5));
        namePanel.add(new JSeparator());
        targetPanel.add(namePanel);
    }

    private void addFormationHeaders(JComponent targetPanel) {
        addHeader(targetPanel, "SBF Formation", JComponent.LEFT_ALIGNMENT);
        addHeader(targetPanel, "Type");
        addHeader(targetPanel, "Size");
        addHeader(targetPanel, "Move");
        addHeader(targetPanel, "JUMP");
        addHeader(targetPanel, "T. Move");
        addHeader(targetPanel, "TMM");
        addHeader(targetPanel, "Tactics");
        addHeader(targetPanel, "Morale");
        addHeader(targetPanel, "Skill");
        addHeader(targetPanel, "PV");
        addHeader(targetPanel, "Specials");
        addHeader(targetPanel, "Conversion");
    }

    private void addUnitHeaders(JComponent targetPanel) {
        addHeader(targetPanel, "   Unit", JComponent.LEFT_ALIGNMENT);
        addHeader(targetPanel, " ");
        addHeader(targetPanel, " ");
        addHeader(targetPanel, " ");
        addHeader(targetPanel, " ");
        addHeader(targetPanel, " ");
        addHeader(targetPanel, " ");
        addHeader(targetPanel, "Arm");
        addHeader(targetPanel, "Dmg");
        addHeader(targetPanel, " ");
        addHeader(targetPanel, " ");
        addHeader(targetPanel, " ");
        addHeader(targetPanel, " ");
    }
}
