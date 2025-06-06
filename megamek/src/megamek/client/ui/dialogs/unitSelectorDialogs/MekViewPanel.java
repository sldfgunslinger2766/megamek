/*
 * MCopyright (C) 2000-2004 Ben Mazur (bmazur@sev.org)
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package megamek.client.ui.dialogs.unitSelectorDialogs;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.Serial;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import megamek.client.ui.util.FluffImageHelper;
import megamek.client.ui.util.UIUtil;
import megamek.client.ui.util.UIUtil.FixedXPanel;
import megamek.common.Entity;
import megamek.common.MekView;
import megamek.common.Report;
import megamek.common.templates.TROView;

import static megamek.MMConstants.BT_URL_SHRAPNEL;
import static megamek.MMConstants.MUL_URL_PREFIX;

/**
 * @author Jay Lawson
 * @since November 2, 2009
 */
public class MekViewPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 2438490306644271135L;

    private final JTextPane txtMek = new JTextPane();
    private final JLabel lblMek = new JLabel();
    private JScrollPane scrMek;

    public static final int DEFAULT_WIDTH = 360;
    public static final int DEFAULT_HEIGHT = 600;

    public MekViewPanel() {
        this(-1, -1, true);
    }

    public MekViewPanel(int width, int height, boolean noBorder) {
        Report.setupStylesheet(txtMek);
        txtMek.setEditable(false);
        txtMek.setBorder(new EmptyBorder(5, 10, 0, 0));
        if (width != -1) {
            txtMek.setMinimumSize(new Dimension(width, height));
            txtMek.setPreferredSize(new Dimension(width, height));
        }

        txtMek.addHyperlinkListener(pe -> {
            if (HyperlinkEvent.EventType.ACTIVATED == pe.getEventType()) {

                boolean isMulAddress = pe.getURL().toString().startsWith(MUL_URL_PREFIX);
                boolean isShrapnelAddress = pe.getURL().toString().startsWith(BT_URL_SHRAPNEL);
                if (isMulAddress || isShrapnelAddress) {
                    UIUtil.browse(pe.getURL().toString(), this);
                } else {
                    String reference = pe.getDescription();
                    if (reference != null && reference.startsWith("#")) {
                        reference = reference.substring(1);
                        String finalReference = reference;
                        SwingUtilities.invokeLater(() -> txtMek.scrollToReference(finalReference));
                    }
                }
            }
        });

        // Add mouse motion listener to show tooltips for links.
        txtMek.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int pos = txtMek.viewToModel2D(e.getPoint());
                if (pos >= 0 && txtMek.getDocument() instanceof HTMLDocument doc) {
                    var elem = doc.getCharacterElement(pos);
                    if (elem != null) {
                        // The Element’s attributes may point us to a <SPAN> tag
                        var attrs = elem.getAttributes();
                        Object attrsAttribute = attrs.getAttribute(HTML.Tag.SPAN);

                        if (attrsAttribute instanceof AttributeSet attributeSet) {
                            String title = (String) attributeSet.getAttribute(HTML.getAttributeKey("title"));
                            if (title != null) {
                                txtMek.setToolTipText(title);
                            }
                        }
                    }
                }
            }
        });
        scrMek = new JScrollPane(txtMek);
        scrMek.getVerticalScrollBar().setUnitIncrement(16);

        if (noBorder) {
            scrMek.setBorder(null);
        }
        scrMek.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        var textPanel = new JPanel(new GridLayout(1, 1));
        if (width != -1) {
            textPanel.setMinimumSize(new Dimension(width, height));
            textPanel.setPreferredSize(new Dimension(width, height));
        }
        textPanel.add(scrMek);

        var fluffPanel = new FixedXPanel();
        if (width != -1) {
            fluffPanel.setMinimumSize(new Dimension(width, height));
            fluffPanel.setPreferredSize(new Dimension(width, height));
        }
        fluffPanel.add(lblMek);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        p.add(textPanel);
        p.add(fluffPanel);
        p.add(Box.createHorizontalGlue());
        setLayout(new BorderLayout());
        add(p);
        addMouseWheelListener(wheelForwarder);
    }

    public void setMek(Entity entity, MekView mekView) {
        txtMek.setText(mekView.getMekReadout());
        txtMek.setCaretPosition(0);
        setFluffImage(entity);
    }

    public void setMek(Entity entity, MekView mekView, String fontName) {
        txtMek.setText(mekView.getMekReadout(fontName));
        txtMek.setCaretPosition(0);
        setFluffImage(entity);
    }

    public void setMek(Entity entity, TROView troView) {
        txtMek.setText(troView.processTemplate());
        txtMek.setCaretPosition(0);
        setFluffImage(entity);
    }

    public void setMek(Entity entity, boolean useAlternateCost) {
        MekView mekView = new MekView(entity, false, useAlternateCost);
        setMek(entity, mekView);
    }

    public void setMek(Entity entity, String fontName) {
        MekView mekView = new MekView(entity, false, false);
        setMek(entity, mekView, fontName);
    }

    private void setFluffImage(Entity entity) {
        Image image = FluffImageHelper.getFluffImage(entity);
        // Scale down to the default width if the image is wider than that
        if (null != image) {
            if (image.getWidth(this) > DEFAULT_WIDTH) {
                image = image.getScaledInstance(DEFAULT_WIDTH, -1, Image.SCALE_SMOOTH);
            }
            lblMek.setIcon(new ImageIcon(image));
        } else {
            lblMek.setIcon(null);
        }
    }

    public void reset() {
        txtMek.setText("");
        lblMek.setIcon(null);
    }

    /** Forwards a mouse wheel scroll on the fluff image or free space to the TRO entry. */
    MouseWheelListener wheelForwarder = e -> {
        MouseWheelEvent converted = (MouseWheelEvent) SwingUtilities.convertMouseEvent(MekViewPanel.this, e, scrMek);
        for (MouseWheelListener listener : scrMek.getMouseWheelListeners()) {
            listener.mouseWheelMoved(converted);
        }
    };
}
