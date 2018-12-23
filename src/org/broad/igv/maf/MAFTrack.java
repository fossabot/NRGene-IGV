/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */
package org.broad.igv.maf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.maf.MAFTile.MASequence;
import org.broad.igv.renderer.ContinuousColorScale;
import org.broad.igv.renderer.GraphicUtils;
import org.broad.igv.renderer.Renderer;
import org.broad.igv.track.AbstractTrack;
import org.broad.igv.track.RegionScoreType;
import org.broad.igv.track.RenderContext;
import org.broad.igv.track.TrackClickEvent;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.ui.FontManager;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.ResourceLocator;

/**
 * @author jrobinso
 */
public class MAFTrack extends AbstractTrack {

    public static final int margin = 5;
    private static Logger log = Logger.getLogger(MAFTrack.class);
    private static int EXPANDED_HEIGHT = 14;
    private static int GAPS_HEIGHT = 25;
    //List<Rectangle> featureRects = new ArrayList();
    MAFManager mgr;
    MAFRenderer renderer = new MAFRenderer();
    // A hack until full MAF track is implemented.
    Rectangle visibleNameRect;


    public MAFTrack(ResourceLocator locator) {
        super(locator);
        this.mgr = new MAFManager(locator);
    }

    @Override
    public int getHeight() {
        return GAPS_HEIGHT + (mgr.getSelectedSpecies().size() + 1) * EXPANDED_HEIGHT;
    }

    @Override
    public void renderName(Graphics2D g2D, Rectangle trackRectangle, Rectangle visibleRectangle) {

        this.visibleNameRect = trackRectangle;
        if (isSelected()) {
            g2D.setBackground(Color.LIGHT_GRAY);
        } else {
            g2D.setBackground(Color.WHITE);
        }

        Rectangle rect = new Rectangle(trackRectangle);
        g2D.clearRect(rect.x, rect.y, rect.width, rect.height);

        Font font = FontManager.getFont(fontSize);
        g2D.setFont(font);

        int y = trackRectangle.y;

        rect.height = GAPS_HEIGHT;
        rect.y = y;
        //text, rect.x + rect.width - margin, yPos, g2D

        GraphicUtils.drawVerticallyCenteredText("Gaps", margin, rect, g2D, true);
        rect.y += rect.height;

        rect.height = EXPANDED_HEIGHT;

        String ref = MAFManager.speciesNames.getProperty(mgr.refId);
        if (ref == null) {
            ref = mgr.refId;
        }
        GraphicUtils.drawVerticallyCenteredText(ref, margin, rect, g2D, true);
        rect.y += rect.height;

        for (String sp : mgr.getSelectedSpecies()) {

            String name = MAFManager.speciesNames.getProperty(sp);
            if (name == null) {
                name = sp;
            }

            if (visibleRectangle.intersects(rect)) {

                GraphicUtils.drawVerticallyCenteredText(name, margin, rect, g2D, true);
            }
            rect.y += rect.height;
        }

    }

    public void setColorScale(ContinuousColorScale colorScale) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void render(RenderContext context, Rectangle rect) {

        double locScale = context.getScale();

        if (locScale > 1) {
            Rectangle r = new Rectangle(rect);
            if (visibleNameRect != null) {
                r.y = visibleNameRect.y;
                r.height = visibleNameRect.height;
            }

            Graphics2D g = context.getGraphic2DForColor(Color.black);
            GraphicUtils.drawCenteredText("Zoom in to see alignments.", r, g);
            return;

        }

        double origin = context.getOrigin();
        String chr = context.getChr();

        int start = (int) origin;
        int end = (int) (origin + rect.width * locScale) + 1;

        // TODO -- check genome and chr to load correct MAF file
        //if (!genome.equals("hg18")) {
        //    return;
        //}

// Get tiles
        MAFTile[] tiles = mgr.getTiles(chr, start, end);


        if (tiles != null) {
            for (MAFTile tile : tiles) {
                // render tile
                if (tile != null) {
                    renderTile(context, rect, tile);
                }
            }
        }

    }

    private void renderTile(RenderContext context, Rectangle trackRectangle,
                            MAFTile tile) {

        int y = trackRectangle.y;

        MASequence reference = tile.refSeq;
        if (reference == null) {
            return;
        }

        Rectangle rect = new Rectangle(trackRectangle);
        rect.height = GAPS_HEIGHT;
        rect.y = y;
        renderer.renderGaps(tile.getGaps(), context, rect);
        rect.y += rect.height;

        rect.height = EXPANDED_HEIGHT;
        renderer.renderAligment(reference, reference, null, context, rect, this);
        rect.y += rect.height;

        // TODO Render gaps
        for (String sp : mgr.getSelectedSpecies()) {

            MASequence seq = tile.alignedSequences.get(sp);
            if (seq != null) {
                renderer.renderAligment(seq, reference, tile.getGaps(), context, rect, this);
            }

            rect.y += rect.height;

        }

    }

    public void setWindowFunction(WindowFunction type) {
        // Ignored
    }

    public WindowFunction getWindowFunction() {
        return null;
    }

    public void setRendererClass(Class rc) {
        // Ignored
    }

    public Renderer getRenderer() {
        return null;
    }

    public boolean isLogNormalized() {
        return false;
    }

    public String getValueStringAt(String chr, double position, int y, ReferenceFrame frame) {
        return "Multiple alignments";
    }

    public float getRegionScore(String chr, int start, int end, int zoom,
                                RegionScoreType type, ReferenceFrame frame) {
        return 0;
    }

    @Override
    public boolean handleDataClick(TrackClickEvent te) {
        MouseEvent e = te.getMouseEvent();
        if (e.isPopupTrigger()) {
            configureTrack();
            return true;
        }
        return false;
    }

    private void configureTrack() {
        MAFConfigurationDialog dialog = new MAFConfigurationDialog(
                IGV.getMainFrame(), true, mgr);
        dialog.setLocationRelativeTo(IGV.getMainFrame());
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
        dialog.setVisible(true);

        if (dialog.cancelled) {
        } else {
            List<String> selectedSpecies = dialog.getSelectedSpecies();
            mgr.setSelectedSpecies(selectedSpecies);
            PreferenceManager.getInstance().setMafSpecies(selectedSpecies);
            IGV.getInstance().repaint();
        }

    }
}

