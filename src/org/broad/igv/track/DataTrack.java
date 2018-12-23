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
/*
 * FeatureTrackH5.java
 *
 * Created on November 12, 2007, 8:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.broad.igv.track;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.FeatureUtils;
import org.broad.igv.feature.IGVFeature;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.renderer.DataRange;
import org.broad.igv.renderer.DataRenderer;
import org.broad.igv.renderer.XYPlotRenderer;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.NrgeneFeatureDescriptionDialog;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.ResourceLocator;

/**
 * Represents a track of numeric data
 *
 * @author jrobinso
 */
public abstract class DataTrack extends AbstractTrack {

    private static Logger log = Logger.getLogger(DataTrack.class);
    private DataRenderer renderer;
    private boolean autoscale;

    // TODO -- memory leak.  This needs to get cleared when the gene list changes
    private HashMap<String, LoadedDataInterval> loadedIntervalCache = new HashMap(200);
    private boolean featuresLoading = false;

    protected boolean showTrackSeparator;
    
    public DataTrack(ResourceLocator locator, String id, String name) {
        super(locator, id, name);
        autoscale = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.CHART_AUTOSCALE);

    }

    public boolean isAutoscale() {
        return autoscale;
    }

    public void setAutoscale(boolean autoscale) {
        this.autoscale = autoscale;
    }

    public void render(RenderContext context, Rectangle rect) {

        if (featuresLoading) {
            return;
        }

        String chr = context.getChr();
        int start = (int) context.getOrigin();
        int end = (int) context.getEndLocation() + 1;
        int zoom = context.getZoom();

        List<LocusScore> inViewScores = null;

        LoadedDataInterval interval = loadedIntervalCache.get(context.getReferenceFrame().getName());
        if (interval != null && interval.contains(chr, start, end, zoom)) {
            inViewScores = interval.getScores();
        } else {
            inViewScores = load(context, chr, start, end, zoom);

        }

        if (autoscale && !FrameManager.isGeneListMode()) {

            InViewInterval inter = computeScale(start, end, inViewScores);
            if (inter.endIdx > inter.startIdx) {
                inViewScores = inViewScores.subList(inter.startIdx, inter.endIdx);

                DataRange dr = getDataRange();
                float min = Math.min(0, inter.dataMin);
                float base = Math.max(min, dr.getBaseline());
                float max = inter.dataMax;
                // Pathological case where min ~= max  (no data in view)
                if (max - min <= (2 * Float.MIN_VALUE)) {
                    max = min + 1;
                }

                DataRange newDR = new DataRange(min, base, max, dr.isDrawBaseline());
                newDR.setType(dr.getType());
                setDataRange(newDR);
            }

        }

        getRenderer().render(inViewScores, context, rect, this);
    }

    public List<LocusScore> load(final RenderContext context, final String chr, final int start, final int end, final int zoom) {

        try {
            featuresLoading = true;
            int maxEnd = end;
            Genome genome = IGV.getInstance().getGenomeManager().getCurrentGenome();

            if (genome != null) {
                Chromosome c = genome.getChromosome(chr);
                if (c != null) maxEnd = Math.max(c.getLength(), end);
            }
            // Expand interval +/- 50%
            int delta = (end - start) / 2;
            int expandedStart = Math.max(0, start - delta);
            int expandedEnd = Math.min(maxEnd, end + delta);
            List<LocusScore> inViewScores = getSummaryScores(chr, expandedStart, expandedEnd, zoom);
            LoadedDataInterval interval = new LoadedDataInterval(chr, start, end, zoom, inViewScores);
            loadedIntervalCache.put(context.getReferenceFrame().getName(), interval);
            return inViewScores;

        } finally {
            featuresLoading = false;
        }

    }


    public void clearCaches() {
        loadedIntervalCache.clear();
    }


    public void setRendererClass(Class rc) {
        try {
        	Object		newRenderer = rc.newInstance();
        	if ( newRenderer instanceof DataRenderer )
        		renderer = (DataRenderer)newRenderer;
        	else
        		log.warn("newRenderer is not a DataRenderer: " + newRenderer.getClass().getName());
        } catch (Exception ex) {
            log.error("Error instatiating renderer ", ex);
        }
    }


    public DataRenderer getRenderer() {
        if (renderer == null) {
            setRendererClass(getDefaultRendererClass());
        }
        return renderer;
    }


    /**
     * Return a value string for the tooltip window at the given location, or null to signal there is no value
     * at that location
     *
     * @param chr
     * @param position
     * @param y
     * @param frame
     * @return
     */
    public String getValueStringAt(String chr, double position, int y, ReferenceFrame frame) {
        StringBuffer buf = new StringBuffer();
        LocusScore score = getLocusScoreAt(chr, position, frame);
        // If there is no value here, return null to signal no popup
        if (score == null) {
            return null;
        }
        buf.append(getName() + "<br>");
        if ((getDataRange() != null) && (getRenderer() instanceof XYPlotRenderer)) {
            buf.append("Data scale: " + getDataRange().getMinimum() + " - " + getDataRange().getMaximum() + "<br>");
        }

        buf.append(score.getValueString(position, getWindowFunction()));
        return buf.toString();
    }


    private LocusScore getLocusScoreAt(String chr, double position, ReferenceFrame frame) {
        int zoom = Math.max(0, frame.getZoom());
        List<LocusScore> scores = getSummaryScores(chr, (int) position - 10, (int) position + 10, zoom);


        if (scores == null) {
            return null;
        } else {
            // give a 2 pixel window, otherwise very narrow features will be missed.
            double bpPerPixel = frame.getScale();
            int buffer = (int) (2 * bpPerPixel);    /* * */
            return (LocusScore) FeatureUtils.getFeatureAt(position, buffer, scores);
        }
    }


    abstract public List<LocusScore> getSummaryScores(String chr, int startLocation, int endLocation, int zoom);


    @Override
    public void setColor(Color color) {
        super.setColor(color);
    }


    @Override
    public void setAltColor(Color color) {
        super.setAltColor(color);

    }

    private InViewInterval computeScale(double origin, double end, List<LocusScore> scores) {

        InViewInterval interval = new InViewInterval();

        if (scores.size() == 1) {
            interval.dataMax = Math.max(0, scores.get(0).getScore());
            interval.dataMin = Math.min(0, scores.get(0).getScore());
        } else {
            interval.startIdx = 0;
            interval.endIdx = scores.size();
            for (int i = 1; i < scores.size(); i++) {
                if (scores.get(i).getEnd() >= origin) {
                    interval.startIdx = i - 1;
                    break;
                }
            }

            for (int i = interval.startIdx + 1; i < scores.size(); i++) {
                LocusScore locusScore = scores.get(i);
                float value = locusScore.getScore();
                if (Float.isNaN(value)) value = 0;
                interval.dataMax = Math.max(interval.dataMax, value);
                interval.dataMin = Math.min(interval.dataMin, value);
                if (locusScore.getStart() > end) {
                    interval.endIdx = i;
                    break;
                }
            }
        }

        return interval;
    }

    @Override
    public Map<String, String> getPersistentState() {
        Map<String, String> properties = super.getPersistentState();
        properties.put("autoscale", String.valueOf(autoscale));
        if ( showTrackSeparator )
        	properties.put("showTrackSeparator", String.valueOf(showTrackSeparator));
        return properties;
    }


    @Override
    public void restorePersistentState(Map<String, String> attributes) {
        super.restorePersistentState(attributes);
        String as = attributes.get("autoscale");
        if (as != null) {
            try {
                autoscale = Boolean.parseBoolean(as);

            } catch (Exception e) {
                log.error("Error restoring session.  Invalid autoscale value: " + autoscale);

            }
        }

        String value = attributes.get("showTrackSeparator");
        if (value != null) {
        	showTrackSeparator = Boolean.parseBoolean(value);
        }
    }

    /**
     * Method description
     *
     * @param chr
     * @param start
     * @param end
     * @param zoom
     * @param type
     * @param frame
     * @return
     */
    public float getRegionScore(String chr, int start, int end, int zoom, RegionScoreType type, ReferenceFrame frame) {

        if (end <= start) {
            return 0;
        }
        if (isRegionScoreType(type)) {

            List<LocusScore> scores = null;
            LoadedDataInterval loadedInterval = loadedIntervalCache.get(frame.getName());
            if (loadedInterval != null && loadedInterval.contains(chr, start, end, zoom)) {
                scores = loadedInterval.getScores();
            } else {
                scores = this.getSummaryScores(chr, start, end, zoom);
                loadedIntervalCache.put(frame.getName(), new LoadedDataInterval(chr, start, end, zoom, scores));
            }


            if (type == RegionScoreType.FLUX) {
                float sumDiffs = 0;
                float lastScore = Float.NaN;
                for (LocusScore score : scores) {
                    if ((score.getEnd() >= start) && (score.getStart() <= end)) {
                        if (Float.isNaN(lastScore)) {
                            lastScore = Math.min(2, Math.max(-2, logScaleData(score.getScore())));
                        } else {
                            float s = Math.min(2, Math.max(-2, logScaleData(score.getScore())));
                            sumDiffs += Math.abs(s - lastScore);
                            lastScore = s;
                        }
                    }
                }
                return sumDiffs;

            } else if (type == RegionScoreType.MUTATION_COUNT) {
                List<Track> overlayTracks = IGV.getInstance().getTrackManager().getOverlayTracks(this);
                float count = 0;
                if (overlayTracks != null) {
                    for (Track t : overlayTracks) {
                        count += t.getRegionScore(chr, start, end, zoom, type, frame);
                    }
                }
                return count;
            } else {
                float regionScore = 0;
                int intervalSum = 0;
                for (LocusScore score : scores) {
                    if ((score.getEnd() >= start) && (score.getStart() <= end)) {
                        int interval = Math.min(end, score.getEnd()) - Math.max(start, score.getStart());
                        float value = score.getScore();
                        regionScore += value * interval;
                        intervalSum += interval;
                    }
                }
                if (intervalSum <= 0) {
                    return -Float.MAX_VALUE;
                } else {
                    regionScore /= intervalSum;
                    return (type == RegionScoreType.DELETION) ? -regionScore : regionScore;
                }
            }

        } else {
            return -Float.MAX_VALUE;
        }
    }


    class InViewInterval {
        int startIdx;
        int endIdx;
        float dataMax = 0;
        float dataMin = 0;
    }

    //Feature selected by the user.  This is repopulated on each handleDataClick() call.
    protected IGVFeature selectedFeature = null;

  
    @Override
    public boolean handleDataClick(TrackClickEvent te) {

        MouseEvent e = te.getMouseEvent();

        //dhmay adding for feature selection
        selectedFeature = null;

        final ReferenceFrame referenceFrame = te.getFrame();        
        if ( referenceFrame != null && e.getClickCount() == 1 && (e.isShiftDown() || e.isMetaDown()) /*&& e.isAltDown()*/ && !e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) 
        {
        	
        	double location = referenceFrame.getChromosomePosition(e.getX());
            double displayLocation = location + 1;
            
        	String		description = getValueStringAt(referenceFrame.getChrName(), displayLocation, e.getY(), referenceFrame);

        	e.consume();
	        	
            StringBuilder						sb = new StringBuilder();
            sb.append("Location: ");
            sb.append((new DecimalFormat()).format((int) displayLocation));
            sb.append("<br><br>");
            sb.append(description);
        	String								text = sb.toString().replace("<br>", "\n");
        	text = text.replace("<b>", "");
        	text = text.replace("</b>", "");
        	NrgeneFeatureDescriptionDialog		dialog = new NrgeneFeatureDescriptionDialog(IGV.getMainFrame(), text);
        	dialog.setLocationRelativeTo(IGV.getMainFrame());
        	dialog.setVisible(true);
	        	
        	return true;
        }
        
        return true;
    }

	public boolean isShowTrackSeparator() {
		return showTrackSeparator;
	}

	public void setShowTrackSeparator(boolean showTrackSeparator) {
		this.showTrackSeparator = showTrackSeparator;
	}

}
