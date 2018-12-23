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

package org.broad.igv.track;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.math.stat.StatUtils;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.Exon;
import org.broad.igv.feature.IGVFeature;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.renderer.BarChartRenderer;
import org.broad.igv.renderer.ContinuousColorScale;
import org.broad.igv.renderer.DataRange;
import org.broad.igv.renderer.HeatmapRenderer;
import org.broad.igv.renderer.LineplotRenderer;
import org.broad.igv.renderer.RNAiBarChartRenderer;
import org.broad.igv.renderer.ScatterplotRenderer;
import org.broad.igv.ui.DataRangeDialog;
import org.broad.igv.ui.FontManager;
import org.broad.igv.ui.HeatmapScaleDialog;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.IGVContentPane;
import org.broad.igv.ui.NrgeneFeatureDescriptionDialog;
import org.broad.igv.ui.UIConstants;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.ui.panel.IGVPopupMenu;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.ui.panel.TrackPanel;
import org.broad.igv.ui.util.FontChooser;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.ui.util.UIUtilities;
import org.broad.igv.util.stats.KMPlotFrame;
import org.broad.tribble.Feature;

/**
 * @author jrobinso
 */
public class TrackMenuUtils {

    static Logger log = Logger.getLogger(TrackMenuUtils.class);
    final static String LEADING_HEADING_SPACER = "  ";
    private static final WindowFunction[] ORDERED_WINDOW_FUNCTIONS = new WindowFunction[]{
            WindowFunction.min,
            WindowFunction.percentile2,
            WindowFunction.percentile10,
            WindowFunction.median,
            WindowFunction.mean,
            WindowFunction.percentile90,
            WindowFunction.percentile98,
            WindowFunction.max,
            WindowFunction.none
    };


    /**
     * Return a popup menu with items applicable to the collection of tracks.
     *
     * @param tracks
     * @return
     */
    public static IGVPopupMenu getPopupMenu(final Collection<Track> tracks, String title, TrackClickEvent te) {

        if (log.isDebugEnabled()) {
            log.debug("enter getPopupMenu");
        }

        //JidePopupMenu menu = new JidePopupMenu();
        IGVPopupMenu menu = new IGVPopupMenu();
        
        if ( !isOnlyEmpty(tracks) )
        {
		    JLabel popupTitle = new JLabel(LEADING_HEADING_SPACER + title, JLabel.CENTER);
		    popupTitle.setFont(UIConstants.boldFont);
		    if (popupTitle != null) {
		        menu.add(popupTitle);
		        menu.addSeparator();
		    }
        }

        addStandardItems(menu, tracks, te);

        menu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {

            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                close();
            }

            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
                close();
            }

            private void close() {
                IGV.getInstance().getTrackManager().clearSelections();
                IGV.getInstance().repaint();
            }

        });

        return menu;

    }

    public static boolean isOnlyEmpty(Collection<Track> tracks) 
    {
    	return tracks.size() == 1 && (tracks.iterator().next() instanceof EmptyTrack);
	}

	public static void addStandardItems(JPopupMenu menu, Collection<Track> tracks, TrackClickEvent te) {

        boolean hasDataTracks = false;
        boolean hasFeatureTracks = false;
        boolean hasOtherTracks = false;
        for (Track track : tracks) {

            //  TODO -- this is ugly, refactor to remove instanceof
            if (track instanceof DataTrack) {
                hasDataTracks = true;
            } else if (track instanceof FeatureTrack) {
                hasFeatureTracks = true;
            } else {
                hasOtherTracks = true;
            }
            if (hasDataTracks && hasFeatureTracks && hasOtherTracks) {
                break;
            }
        }

        boolean featureTracksOnly = hasFeatureTracks && !hasDataTracks && !hasOtherTracks;
        boolean dataTracksOnly = !hasFeatureTracks && hasDataTracks && !hasOtherTracks;

        addSharedItems(menu, tracks, hasFeatureTracks);
        menu.addSeparator();
        if (dataTracksOnly) {
            addDataItems(menu, tracks);
            menu.addSeparator();
        } else if (featureTracksOnly) {
            addFeatureItems(menu, tracks, te);
            menu.addSeparator();
        }

        menu.add(getRemoveMenuItem(tracks));
        if ( !isOnlyEmpty(tracks) )
        {
	        menu.add(getReloadMenuItem(tracks));
	        TrackMenuUtils.addMoveMenuItem(menu, tracks);
	        menu.add(getInfoMenuItem(tracks));
        }

    }

    public static void addZoomItems(JPopupMenu menu, final ReferenceFrame frame) {

        if (FrameManager.isGeneListMode()) {
            JMenuItem item = new JMenuItem("Reset panel to '" + frame.getName() + "'");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.reset();
                    // TODO -- paint only panels for this frame
                }
            });
            menu.add(item);
        }


        JMenuItem zoomOutItem = new JMenuItem("Zoom out");
        zoomOutItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                frame.incrementZoom(-1);
            }
        });
        menu.add(zoomOutItem);

        JMenuItem zoomInItem = new JMenuItem("Zoom in");
        zoomInItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                frame.incrementZoom(1);
            }
        });
        menu.add(zoomInItem);


    }


    /**
     * Return popup menu with items applicable to data tracks
     *
     * @return
     */
    private static void addDataItems(JPopupMenu menu, final Collection<Track> tracks) {

        if (log.isDebugEnabled()) {
            log.debug("enter getDataPopupMenu");
        }

        final String[] labels = {"Heatmap", "Bar Chart", "Scatterplot", "Line Plot"};
        final Class[] renderers = {HeatmapRenderer.class, BarChartRenderer.class,
                ScatterplotRenderer.class, LineplotRenderer.class
        };

        //JLabel popupTitle = new JLabel(LEADING_HEADING_SPACER + title, JLabel.CENTER);

        JLabel rendererHeading = new JLabel(LEADING_HEADING_SPACER + "Type of Graph", JLabel.LEFT);
        rendererHeading.setFont(UIConstants.boldFont);

        menu.add(rendererHeading);

        // Get existing selections
        Set<Class> currentRenderers = new HashSet<Class>();
        for (Track track : tracks) {
            if (track.getRenderer() != null) {
                currentRenderers.add(track.getRenderer().getClass());
            }
        }

        // Create and renderer menu items
        for (int i = 0; i < labels.length; i++) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(labels[i]);
            final Class rendererClass = renderers[i];
            if (currentRenderers.contains(rendererClass)) {
                item.setSelected(true);
            }
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    changeRenderer(tracks, rendererClass);
                }
            });
            menu.add(item);
        }
        menu.addSeparator();


        // Get union of all valid window functions for selected tracks
        Set<WindowFunction> avaibleWindowFunctions = new HashSet();
        for (Track track : tracks) {
            avaibleWindowFunctions.addAll(track.getAvailableWindowFunctions());
        }
        avaibleWindowFunctions.add(WindowFunction.none);


        // dataPopupMenu.addSeparator();
        // Collection all window functions for selected tracks
        Set<WindowFunction> currentWindowFunctions = new HashSet<WindowFunction>();
        for (Track track : tracks) {
            if (track.getWindowFunction() != null) {
                currentWindowFunctions.add(track.getWindowFunction());
            }
        }

        if (!avaibleWindowFunctions.isEmpty() || !currentWindowFunctions.isEmpty()) {
            JLabel statisticsHeading = new JLabel(LEADING_HEADING_SPACER + "Windowing Function", JLabel.LEFT);
            statisticsHeading.setFont(UIConstants.boldFont);

            menu.add(statisticsHeading);

            for (final WindowFunction wf : ORDERED_WINDOW_FUNCTIONS) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(wf.getDisplayName());
                if (avaibleWindowFunctions.contains(wf) || currentWindowFunctions.contains(wf)) {
                    if (currentWindowFunctions.contains(wf)) {
                        item.setSelected(true);
                    }
                    item.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {
                            changeStatType(wf.toString(), tracks);
                        }
                    });
                    menu.add(item);
                }
            }
            menu.addSeparator();
        }


        menu.add(getDataRangeItem(tracks));
        menu.add(getHeatmapScaleItem(tracks));

        if (tracks.size() > 0) {
            menu.add(getLogScaleItem(tracks));
        }

        menu.add(getAutoscaleItem(tracks));


        menu.add(getShowDataRangeItem(tracks));


        menu.add(getChangeKMPlotItem(tracks));

        if ( tracks.size() > 0 )
    	{
        	Track		track = tracks.iterator().next();
        	
            final JMenuItem item = new JCheckBoxMenuItem("Show track separator");
            item.setSelected(track.isShowTrackSeparator());
            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    UIUtilities.invokeOnEventThread(new Runnable() {

                        public void run() {
                        	
                        	for ( Track track : tracks )
                        	{
                        		if ( track instanceof FeatureTrack )
                        			((FeatureTrack)track).setShowTrackSeparator(item.isSelected());
                        		else if ( track instanceof DataTrack )
                        			((DataTrack)track).setShowTrackSeparator(item.isSelected());
                        	}
                        	
                        	IGV.getInstance().doRefresh();
                        }
                    });
                }
            });
            menu.add(item);
    	}
    	
    }

    /**
     * Return popup menu with items applicable to feature tracks
     *
     * @return
     */
    private static void addFeatureItems(JPopupMenu featurePopupMenu, final Collection<Track> tracks, final TrackClickEvent te) {


        addDisplayModeItems(tracks, featurePopupMenu);


        if (tracks.size() > 0 ) {
            Track t = tracks.iterator().next();
            Feature f = t.getFeatureAtMousePosition(te);
        	final FeatureTrack	ft = (FeatureTrack)t;
        	
            // DK add nrgene items to popup menu
            {

            	featurePopupMenu.addSeparator();
            	{
                    final JMenuItem item = new JCheckBoxMenuItem("Show track separator");
                    item.setSelected(ft.isShowTrackSeparator());
                    item.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            UIUtilities.invokeOnEventThread(new Runnable() {

                                public void run() {
                                	
                                	for ( Track track : tracks )
                                		if ( track instanceof FeatureTrack )
                                			((FeatureTrack)track).setShowTrackSeparator(item.isSelected());
                                	
                                	IGV.getInstance().doRefresh();
                                }
                            });
                        }
                    });
                    featurePopupMenu.add(item);
            	}
            	
            	if ( tracks.size() == 1 )
            	{
                    final JMenuItem item = new JCheckBoxMenuItem("Set tooltip attributes ...");
                    item.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            String			tooltipAttributes = ft.getTooltipAttributes();
                            String 			val = MessageUtils.showInputDialog("Tooltip Attributes", tooltipAttributes != null ? tooltipAttributes : "");
                            if ( val != null )
                            	ft.setTooltipAttributes(val.length() > 0 ? val : null);
                        }
                    });
                    featurePopupMenu.add(item);
            	}
            }
        	
        	featurePopupMenu.addSeparator();

        	
        	if (f != null) {


                // If we are over an exon, copy its sequence instead of the entire feature.
                if (f instanceof IGVFeature) {
                    double position = te.getChromosomePosition();
                    Collection<Exon> exons = ((IGVFeature) f).getExons();
                    if (exons != null) {
                        for (Exon exon : exons) {
                            if (position > exon.getStart() && position < exon.getEnd()) {
                                f = exon;
                                break;
                            }
                        }
                    }
                }
                


                featurePopupMenu.add(getCopyDetailsItem(f, te));
                featurePopupMenu.add(getCopySequenceItem(f));
            }
        }

        featurePopupMenu.addSeparator();
        featurePopupMenu.add(getChangeFeatureWindow(tracks));
    }

    /**
     * Popup menu with items applicable to both feature and data tracks
     *
     * @return
     */
    private static void addSharedItems(JPopupMenu menu, final Collection<Track> tracks, boolean hasFeatureTracks) {

        //JLabel trackSettingsHeading = new JLabel(LEADING_HEADING_SPACER + "Track Settings", JLabel.LEFT);
        //trackSettingsHeading.setFont(boldFont);
        //menu.add(trackSettingsHeading);

    	if ( !isOnlyEmpty(tracks) )
    	{
	        menu.add(getTrackRenameItem(tracks));
	
	        String colorLabel = hasFeatureTracks
	                ? "Change Track Color..." : "Change Track Color (Positive Values)...";
	        JMenuItem item = new JMenuItem(colorLabel);
	        item.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                changeTrackColor(tracks);
	            }
	        });
	        menu.add(item);
	
	        if (!hasFeatureTracks) {
	
	            // Change track color by attribute
	            item = new JMenuItem("Change Track Color (Negative Values)...");
	            item.setToolTipText(
	                    "Change the alternate track color.  This color is used when graphing negative values");
	            item.addActionListener(new ActionListener() {
	                public void actionPerformed(ActionEvent evt) {
	                    changeAltTrackColor(tracks);
	                }
	            });
	            menu.add(item);
	        }
    	}

        menu.add(getChangeTrackHeightItem(tracks));
        JMenuItem	item = getChangeTrackBlockHeightItem(tracks);
        if ( item != null )
        	menu.add(item);
    	if ( !isOnlyEmpty(tracks) )
    	{
	        menu.add(getChangeFontItem(tracks));
    	}
	        
    	menu.add(getChangeFontColorItem(tracks));
	        
	        // add external tools
    	if ( !isOnlyEmpty(tracks) )
    	{
	        ExternalToolManager.getInstance().addToolsToMenu(menu, tracks);
    	}
    }


    private static void changeStatType(String statType, Collection<Track> selectedTracks) {
        for (Track track : selectedTracks) {
            track.setWindowFunction(WindowFunction.valueOf(statType));
        }
        refresh();
    }


    public static JMenuItem getTrackRenameItem(final Collection<Track> selectedTracks) {
        // Change track height by attribute
        JMenuItem item = new JMenuItem("Rename Track...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                UIUtilities.invokeOnEventThread(new Runnable() {

                    public void run() {
                        renameTrack(selectedTracks);
                    }
                });
            }
        });
        if (selectedTracks.size() > 1) {
            item.setEnabled(false);
        }
        return item;
    }

    private static JMenuItem getHeatmapScaleItem(final Collection<Track> selectedTracks) {

        JMenuItem item = new JMenuItem("Set Heatmap Scale...");

        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (selectedTracks.size() > 0) {

                    ContinuousColorScale colorScale = selectedTracks.iterator().next().getColorScale();
                    HeatmapScaleDialog dlg = new HeatmapScaleDialog(IGV.getMainFrame(), colorScale);

                    dlg.setVisible(true);
                    if (!dlg.isCanceled()) {
                        colorScale = dlg.getColorScale();

                        // dlg.isFlipAxis());
                        for (Track track : selectedTracks) {
                            track.setColorScale(colorScale);
                        }
                        IGV.getInstance().repaint();
                    }

                }

            }
        });
        return item;
    }

    public static JMenuItem getDataRangeItem(final Collection<Track> selectedTracks) {
        JMenuItem item = new JMenuItem("Set Data Range...");

        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (selectedTracks.size() > 0) {
                    // Create a datarange that spans the extent of prev tracks range
                    float mid = 0;
                    float min = Float.MAX_VALUE;
                    float max = Float.MIN_VALUE;
                    boolean drawBaseline = false;
                    for (Track t : selectedTracks) {
                        DataRange dr = t.getDataRange();
                        min = Math.min(min, dr.getMinimum());
                        max = Math.max(max, dr.getMaximum());
                        mid += dr.getBaseline();
                    }
                    mid /= selectedTracks.size();
                    if (mid < min) {
                        mid = min;
                    } else if (mid > max) {
                        min = max;
                    }

                    DataRange prevAxisDefinition = new DataRange(min, mid, max, drawBaseline);
                    DataRangeDialog dlg = new DataRangeDialog(IGV.getMainFrame(), prevAxisDefinition);
                    dlg.setVisible(true);
                    if (!dlg.isCanceled()) {
                        min = Math.min(dlg.getMax(), dlg.getMin());
                        max = Math.max(dlg.getMin(), dlg.getMax());
                        mid = dlg.getBase();
                        mid = Math.max(min, Math.min(mid, max));

                        DataRange axisDefinition = new DataRange(dlg.getMin(), dlg.getBase(), dlg.getMax());

                        for (Track track : selectedTracks) {
                            track.setDataRange(axisDefinition);
                            if (track instanceof DataTrack) {
                                ((DataTrack) track).setAutoscale(false);
                            }
                        }
                        IGV.getInstance().repaint();
                    }

                }
            }
        });
        return item;
    }

    private static JMenuItem getDrawBorderItem() {
        // Change track height by attribute


        final JCheckBoxMenuItem drawBorderItem = new JCheckBoxMenuItem("Draw borders");
        drawBorderItem.setSelected(FeatureTrack.isDrawBorder());
        drawBorderItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                FeatureTrack.setDrawBorder(drawBorderItem.isSelected());
                IGV.getInstance().repaintDataPanels();
            }
        });

        return drawBorderItem;
    }


    private static JMenuItem getLogScaleItem(final Collection<Track> selectedTracks) {
        // Change track height by attribute


        final JCheckBoxMenuItem logScaleItem = new JCheckBoxMenuItem("Log scale");
        final boolean logScale = selectedTracks.iterator().next().getDataRange().isLog();
        logScaleItem.setSelected(logScale);
        logScaleItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                DataRange.Type scaleType = logScaleItem.isSelected() ?
                        DataRange.Type.LOG :
                        DataRange.Type.LINEAR;
                for (Track t : selectedTracks) {
                    t.getDataRange().setType(scaleType);
                }
                IGV.getInstance().repaintDataPanels();
            }
        });

        return logScaleItem;
    }

    private static JMenuItem getAutoscaleItem(final Collection<Track> selectedTracks) {

        final JCheckBoxMenuItem autoscaleItem = new JCheckBoxMenuItem("Autoscale");
        if (selectedTracks.size() == 0) {
            autoscaleItem.setEnabled(false);

        } else {
            boolean autoScale = false;
            for (Track t : selectedTracks) {
                if (t instanceof DataTrack && ((DataTrack) t).isAutoscale()) {
                    autoScale = true;
                    break;
                }
            }

            autoscaleItem.setSelected(autoScale);
            autoscaleItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {

                    boolean autoScale = autoscaleItem.isSelected();
                    for (Track t : selectedTracks) {
                        if (t instanceof DataTrack) {
                            ((DataTrack) t).setAutoscale(autoScale);
                        }
                    }
                    IGV.getInstance().repaintDataPanels();
                }
            });
        }
        return autoscaleItem;
    }

    private static JMenuItem getShowDataRangeItem(final Collection<Track> selectedTracks) {

        final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show Data Range");
        if (selectedTracks.size() == 0) {
            item.setEnabled(false);

        } else {
            boolean showDataRange = true;
            for (Track t : selectedTracks) {
                if (!t.isShowDataRange()) {
                    showDataRange = false;
                    break;
                }
            }

            item.setSelected(showDataRange);
            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {

                    boolean showDataRange = item.isSelected();
                    for (Track t : selectedTracks) {
                        if (t instanceof DataTrack) {
                            ((DataTrack) t).setShowDataRange(showDataRange);
                        }
                    }
                    IGV.getInstance().repaintDataPanels();
                }
            });
        }
        return item;
    }


    public static void addDisplayModeItems(final Collection<Track> tracks, JPopupMenu menu) {

        // Find "most representative" state from track collection
        int expCount = 0;
        int sqCount = 0;
        int collCount = 0;
        for (Track track : tracks) {
            switch (track.getDisplayMode()) {
                case EXPANDED:
                    expCount++;
                    break;
                case SQUISHED:
                    sqCount++;
                    break;
                case COLLAPSED:
                    collCount++;
                    break;
            }
        }
        Track.DisplayMode currentMode = null;
        if (expCount > sqCount && expCount > collCount) {
            currentMode = Track.DisplayMode.EXPANDED;
        } else if (sqCount > expCount && sqCount > collCount) {
            currentMode = Track.DisplayMode.SQUISHED;
        } else {
            currentMode = Track.DisplayMode.COLLAPSED;
        }

        ButtonGroup group = new ButtonGroup();


        JRadioButtonMenuItem m1 = new JRadioButtonMenuItem("Expanded");
        m1.setSelected(currentMode == Track.DisplayMode.EXPANDED);
        m1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setTrackDisplayMode(tracks, Track.DisplayMode.EXPANDED);
                refresh();
            }
        });

        JRadioButtonMenuItem m2 = new JRadioButtonMenuItem("Squished");
        m2.setSelected(currentMode == Track.DisplayMode.SQUISHED);
        m2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setTrackDisplayMode(tracks, Track.DisplayMode.SQUISHED);
                refresh();
            }
        });

        JRadioButtonMenuItem m3 = new JRadioButtonMenuItem("Collapsed");
        m3.setSelected(currentMode == Track.DisplayMode.COLLAPSED);
        m3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setTrackDisplayMode(tracks, Track.DisplayMode.COLLAPSED);
                refresh();
            }
        });

        group.add(m1);
        group.add(m2);
        group.add(m3);
        menu.add(m3);
        menu.add(m2);
        menu.add(m1);


    }


    private static void setTrackDisplayMode(Collection<Track> tracks, Track.DisplayMode mode) {

        for (Track t : tracks) {
            t.setDisplayMode(mode);
        }
    }


    public static JMenuItem getRemoveMenuItem(final Collection<Track> selectedTracks) {

        boolean multiple = selectedTracks.size() > 1;

        JMenuItem item = new JMenuItem("Remove Track" + (multiple ? "s" : ""));
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (selectedTracks.isEmpty()) {
                    return;
                }

                StringBuffer buffer = new StringBuffer();
                for (Track track : selectedTracks) {
                    buffer.append("\n\t");
                    buffer.append(track.getName());
                }
                String deleteItems = buffer.toString();

                JTextArea textArea = new JTextArea();
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                textArea.setText(deleteItems);

                JOptionPane optionPane = new JOptionPane(scrollPane,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.YES_NO_OPTION);
                optionPane.setPreferredSize(new Dimension(550, 500));
                JDialog dialog = optionPane.createDialog(IGV.getMainFrame(), "Remove The Following Tracks");
                dialog.setVisible(true);

                Object choice = optionPane.getValue();
                if ((choice == null) || (JOptionPane.YES_OPTION != ((Integer) choice).intValue())) {
                    return;
                }

                IGV.getInstance().getTrackManager().removeTracks(selectedTracks);
                IGV.getInstance().doRefresh();
            }
        });
        return item;
    }

    public static JMenuItem getInfoMenuItem(final Collection<Track> selectedTracks) {

        final boolean multiple = selectedTracks.size() > 1;

        final String	title = "Track" + (multiple ? "s" : "") + " Info";
        JMenuItem 		item = new JMenuItem("Get " + title);
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (selectedTracks.isEmpty()) {
                    return;
                }

                StringBuilder		info = new StringBuilder();
                int					index = 0;
                int					start = 0, end = 0;
                for ( Track track : selectedTracks )
                {
                	// add separator if more than one track
                	if ( multiple )
                	{
                		if ( index > 0 )
                			info.append("\n\n");
                	}
                	
                	// add info
                	info.append("Name: " + track.getName() + "\n");
                	if ( track.getResourceLocator() != null )
                	{
		            	if ( index == 0 )
		            	{
		            		start = info.length() + 6;
		            		end = start + track.getResourceLocator().getPath().length();
		            	}
		            	info.append("Path: " + track.getResourceLocator().getPath() + "\n");
                	}
                	
                	index++;
                }
                
                NrgeneFeatureDescriptionDialog		dialog = new NrgeneFeatureDescriptionDialog(IGV.getMainFrame(), info.toString());
                dialog.setTitle(title);
            	dialog.setLocationRelativeTo(IGV.getMainFrame());
            	if ( !multiple )
            		dialog.selectText(start, end);
            	dialog.setVisible(true);
             }
        });
        return item;
    }

   public static JMenuItem getReloadMenuItem(final Collection<Track> tracks) 
    {
        boolean 		multiple = tracks.size() > 1;
		JMenuItem 		item = new JMenuItem("Reload Track" + (multiple ? "s" : ""));
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				
				for ( Track track : tracks )			
					if ( track.getResourceLocator().modified() )
						IGV.getInstance().replaceAlterante(track, track.getResourceLocator().getPath(), true);
			}
		});
		item.setEnabled(false);
		for ( Track track : tracks )
			if ( track.getResourceLocator() != null && track.getResourceLocator().modified() )
			{
				item.setEnabled(true);
				break;
			}

		return item;
    }

    
    public static void addMoveMenuItem(JPopupMenu menu, final Collection<Track> tracks)
    {
    	// add spacer track
    	if ( tracks.size() == 1 && !(tracks.iterator().next() instanceof EmptyTrack))
    		menu.add(getAddSpacerTrackMenuItem(tracks));
    	
    	if ( IGVContentPane.isUseTabs() )
    		menu.add(getMoveMenuItem(tracks));
    }
    

    public static JMenuItem getAddSpacerTrackMenuItem(final Collection<Track> tracks)
    {
		JMenuItem 		item = new JMenuItem("Add Spacer Track");
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				
				for ( Track track : tracks )
				{
					String			id = EmptyTrack.createEmptyTrackId();
					EmptyTrack		emptyTrack = new EmptyTrack(id);
					
					for ( TrackPanel trackPanel : IGV.getInstance().getTrackPanels() )
						for ( TrackGroup trackGroup : trackPanel.getGroups() )
						{
							List<Track>	tracks = trackGroup.getTracks(true);
							if ( tracks != null && tracks.contains(track) )
							{
								int		index = tracks.indexOf(track);
								if ( index >= 0 )
								{
									trackGroup.add(index, emptyTrack);
								}
							}
						}
				}
			}
		});

		return item;
    	
    }
    
    public static JMenuItem getMoveMenuItem(final Collection<Track> tracks) 
    {
        boolean 		multiple = tracks.size() > 1;
		JMenu 			menu = new JMenu("Move/Copy Track" + (multiple ? "s" : "") + " to Tab");
		String			currentTab = IGV.getInstance().getContentPane().tabsCurrentTab();
		
		for ( final String tab : IGV.getInstance().getContentPane().tabsList() )
		{
			if ( tab.equals(currentTab) )
				continue;
			
			JMenuItem		item = new JMenuItem("Move to " + tab);
			menu.add(item);
			
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					for ( Track track : tracks )
						IGV.getInstance().getContentPane().tabsMoveTrackTo(track, tab);
				}
			});
		}
		
        menu.addSeparator();
		
		for ( final String tab : IGV.getInstance().getContentPane().tabsList() )
		{
			if ( tab.equals(currentTab) )
				continue;
			
			JMenuItem		item = new JMenuItem("Copy to " + tab);
			menu.add(item);
			
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					for ( Track track : tracks )
						IGV.getInstance().getContentPane().tabsCopyTrackTo(track, tab);
				}
			});
		}

		menu.setEnabled(menu.getItemCount() > 0);

		return menu;
    }

    public static void changeRenderer(final Collection<Track> selectedTracks, Class rendererClass) {
        for (Track track : selectedTracks) {

            // TODO -- a temporary hack to facilitate RNAi development
            if (track.getTrackType() == TrackType.RNAI) {
                if (rendererClass == BarChartRenderer.class) {
                    rendererClass = RNAiBarChartRenderer.class;
                }

            }
            track.setRendererClass(rendererClass);
        }
        refresh();
    }

    public static void renameTrack(final Collection<Track> selectedTracks) {

        if (selectedTracks.isEmpty()) {
            return;
        }
        Track t = selectedTracks.iterator().next();
        String newName = JOptionPane.showInputDialog(IGV.getMainFrame(), "Enter new name: ", t.getName());

        if (newName == null || newName.trim() == "") {
            return;
        }

        t.setName(newName);
        refresh();
    }

    public static void changeTrackHeight(final Collection<Track> selectedTracks) {


        if (selectedTracks.isEmpty()) {
            return;
        }

        final String parameter = "Track height";
        int value = getIntValue(parameter, getRepresentativeTrackHeight(selectedTracks));
        if (value == Integer.MIN_VALUE) {
            return;
        }

        value = Math.max(0, value);

        for (Track track : selectedTracks) {

            track.setHeight(value);

        }

        refresh();
    }

    public static void changeTrackBlockHeight(final Collection<Track> selectedTracks) {


        if (selectedTracks.isEmpty()) {
            return;
        }

        final String parameter = "Track block height (0 means default)";
        int value = getIntValue(parameter, getRepresentativeTrackBlockHeight(selectedTracks));
        if (value == Integer.MIN_VALUE) {
            return;
        }

        value = Math.max(0, value);

        for (Track track : selectedTracks) {
        	if ( track instanceof ITrackBlockHeight )
        		((ITrackBlockHeight)track).setTrackBlockHeight(value);

        }

        refresh();
    }

    public static void changeFeatureVisibilityWindow(final Collection<Track> selectedTracks) {

        Collection<Track> featureTracks = new ArrayList(selectedTracks.size());
        for (Track t : selectedTracks) {
            if (t instanceof FeatureTrack) {
                featureTracks.add(t);
            }
        }

        if (featureTracks.isEmpty()) {
            return;
        }


        int origValue = featureTracks.iterator().next().getVisibilityWindow();
        int origValueKB = Math.max(1, origValue / 1000);
        int value = getIntValue("Visibility window (kb)", origValueKB);
        if (value == Integer.MIN_VALUE) {
            return;
        }

        for (Track track : featureTracks) {
            track.setVisibilityWindow((int) (value * 1000));
        }

        refresh();
    }

    public static void changeFontSize(final Collection<Track> selectedTracks) {


        if (selectedTracks.isEmpty()) {
            return;
        }

        // establish a font to be used as default (to be changed)
        Track				firstTrack = selectedTracks.iterator().next();
        Font				font = firstTrack.getNameFont();
        if ( font == null )
        	font = FontManager.getFont(firstTrack.getFontSize());
    
        // select a new font        
        FontChooser			dialog = new FontChooser(null, font);
        dialog.setModal(true);
        dialog.setVisible(true);
        if ( dialog.isCanceled() )
        	return;
        font = dialog.getSelectedFont();

        for (Track track : selectedTracks) {
            track.setFontSize(font.getSize());
            track.setNameFont(font);
        }

        refresh();
    }


    public static void changeFontColor(final Collection<Track> selectedTracks) {

        if (selectedTracks.isEmpty()) {
            return;
        }

        // establish a color to be used as default (to be changed)
        Track				firstTrack = selectedTracks.iterator().next();
        Color				color = firstTrack.getNameColor();
        if ( color == null )
        	color = Color.BLACK;
    
        // select a new color     
        color = JColorChooser.showDialog(null, "Track Color", color);
        if ( color == null )
        	return;

        for (Track track : selectedTracks) {
        	track.setNameColor(color);
        }

        refresh();
    }


    public static int getIntValue(String parameter, int value) {

        while (true) {

            String height = JOptionPane.showInputDialog(
                    IGV.getMainFrame(), parameter + ": ",
                    String.valueOf(value));

            if ((height == null) || height.trim().equals("")) {
                return Integer.MIN_VALUE;   // <= the logical "null" value
            }

            try {
                value = Integer.parseInt(height);
                return value;
            } catch (NumberFormatException numberFormatException) {
                JOptionPane.showMessageDialog(IGV.getMainFrame(),
                        parameter + " must be an integer number.");
            }
        }
    }

    public static void changeTrackColor(final Collection<Track> selectedTracks) {

        if (selectedTracks.isEmpty()) {
            return;
        }

        Color currentSelection = selectedTracks.iterator().next().getColor();

        Color color = UIUtilities.showColorChooserDialog(
                "Select Track Color (Positive Values)",
                currentSelection);

        if (color != null) {
            for (Track track : selectedTracks) {
                track.setColor(color);
            }
            refresh();
        }

    }

    public static void changeAltTrackColor(final Collection<Track> selectedTracks) {

        if (selectedTracks.isEmpty()) {
            return;
        }

        Color currentSelection = selectedTracks.iterator().next().getColor();

        Color color = UIUtilities.showColorChooserDialog(
                "Select Track Color (Negative Values)",
                currentSelection);

        if (color == null) {
            return;
        }

        for (Track track : selectedTracks) {

            track.setAltColor(color);
        }
        refresh();

    }


    public static JMenuItem getCopyDetailsItem(final Feature f, final TrackClickEvent evt) {
        JMenuItem item = new JMenuItem("Copy Details to Clipboard");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                ReferenceFrame frame = evt.getFrame();
                int mouseX = evt.getMouseEvent().getX();

                double location = frame.getChromosomePosition(mouseX);
                if (f instanceof IGVFeature) {
                    String details = ((IGVFeature) f).getValueString(location, null);
                    if (details != null) {
                        details = details.replace("<br>", System.getProperty("line.separator"));
                        details += System.getProperty("line.separator") +
                                f.getChr() + ":" + (f.getStart() + 1) + "-" + f.getEnd();
                        StringSelection stringSelection = new StringSelection(details);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, null);
                    }
                }
            }
        });
        return item;
    }


    public static JMenuItem getCopySequenceItem(final Feature f) {
        JMenuItem item = new JMenuItem("Copy Sequence");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                Genome genome = IGV.getInstance().getGenomeManager().getCurrentGenome();
                String chr = f.getChr();
                int start = f.getStart();
                int end = f.getEnd();
                byte[] seqBytes = genome.getSequence(chr, start, end);
                if (seqBytes == null) {
                    MessageUtils.showMessage("Sequence not available");
                } else {
                    String sequence = new String(seqBytes);
                    StringSelection stringSelection = new StringSelection(sequence);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            }
        });
        return item;
    }

    /**
     * Return a representative track height to use as the default.  For now
     * using the median track height.
     *
     * @return
     */
    public static int getRepresentativeTrackHeight(Collection<Track> tracks) {

        double[] heights = new double[tracks.size()];
        int i = 0;
        for (Track track : tracks) {
            heights[i] = track.getHeight();
            i++;
        }
        int medianTrackHeight = (int) Math.round(StatUtils.percentile(heights, 50));
        if (medianTrackHeight > 0) {
            return medianTrackHeight;
        }

        return PreferenceManager.getInstance().getAsInt(PreferenceManager.INITIAL_TRACK_HEIGHT);

    }

    public static int getRepresentativeTrackBlockHeight(Collection<Track> tracks) {

        double[] heights = new double[tracks.size()];
        int i = 0;
        for (Track track : tracks) {
        	if ( track instanceof ITrackBlockHeight )
        	{
        		heights[i] = ((ITrackBlockHeight)track).getTrackBlockHeight();
        		i++;
        	}
        }
        return (int) Math.round(StatUtils.max(heights));
    }

    public static void refresh() {
        IGV.getInstance().showLoadedTrackCount();
        IGV.getInstance().doRefresh();
    }

    public static JMenuItem getChangeTrackHeightItem(final Collection<Track> selectedTracks) {
        // Change track height by attribute
        JMenuItem item = new JMenuItem("Change Track Height...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                changeTrackHeight(selectedTracks);
            }
        });
        return item;
    }

    public static JMenuItem getChangeTrackBlockHeightItem(final Collection<Track> selectedTracks) {
    	
    	// check if at least one is capable of changing block height
    	int		count = 0;
    	for ( Track track : selectedTracks )
    		if ( track instanceof ITrackBlockHeight )
    			count++;
    	if ( count == 0 )
    		return null;
    	
        // Change track height by attribute
        JMenuItem item = new JMenuItem("Change Track Block Height...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                changeTrackBlockHeight(selectedTracks);
            }
        });
        return item;
    }

    public static JMenuItem getChangeKMPlotItem(final Collection<Track> selectedTracks) {
        // Change track height by attribute
        JMenuItem item = new JMenuItem("Kaplan-Meier Plot...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                // If one or fewer tracks are selected assume the intent is to use all tracks.  A right-click
                // will always result in one selected track.
                Collection<Track> tracks = selectedTracks.size() > 1 ? selectedTracks :
                        IGV.getInstance().getTrackManager().getAllTracks(false);
                KMPlotFrame frame = new KMPlotFrame(tracks);
                frame.setVisible(true);
            }
        });
        return item;
    }

    public static JMenuItem getChangeFeatureWindow(final Collection<Track> selectedTracks) {
        // Change track height by attribute
        JMenuItem item = new JMenuItem("Set Feature Visibility Window...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                changeFeatureVisibilityWindow(selectedTracks);
            }
        });
        return item;
    }

    public static JMenuItem getChangeFontItem(final Collection<Track> selectedTracks) {
        // Change track height by attribute
        JMenuItem item = new JMenuItem("Change Font ...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                changeFontSize(selectedTracks);
            }
        });
        return item;
    }

    public static JMenuItem getChangeFontColorItem(final Collection<Track> selectedTracks) {
    	
        // Change track height by attribute
        JMenuItem item = new JMenuItem(isOnlyEmpty(selectedTracks) ? "Change Color ..." : "Change Font Color ...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                changeFontColor(selectedTracks);
            }
        });
        return item;
    }
}

