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
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.broad.igv.track;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.RegionOfInterest;
import org.broad.igv.renderer.GraphicUtils;
import org.broad.igv.ui.FontManager;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.variant.VariantTrack;

/**
 * Container for a group of tracks.  Behaves as a single unit when sorting
 * by region score.
 *
 * @author jrobinso
 */
public class TrackGroup {

    private static Logger log = Logger.getLogger(TrackGroup.class);
    /**
     * Key used to group tracks (e.g. SAMPLE_ID).
     */
    private String name;
    private boolean drawBorder = true;
    private List<Track> tracks;
    private boolean selected;


    public TrackGroup() {
        this("");
    }


    public TrackGroup(String name) {
        this.name = name;
        tracks = new ArrayList<Track>();
    }


    public boolean contains(Track track) {
        return tracks.contains(track);
    }

    public List<Track> getTracks(boolean copy) {
    	if ( !copy )
    		return getTracks();
    	else
    		return new LinkedList<Track>(getTracks());
    }
    
    public List<Track> getTracks() {
        return tracks;
    }


    public int indexOf(Track track) {
        return tracks.indexOf(track);
    }


    public int size() {
        return tracks.size();
    }


    public void add(Track track) {
    	if ( !this.tracks.contains(track) )
    		this.tracks.add(track);
    }

    public void add(int pos, Track track) {
        this.tracks.add(pos, track);
    }

    public void addAll(Collection<Track> trackList) {
        tracks.addAll(trackList);
    }

    public void addAll(int index, Collection<Track> trackList) {
        tracks.addAll(index, trackList);
    }

    public void remove(Track track) {
    	if ( tracks.contains(track) )
    		track.removed();    	
        tracks.remove(track);
    }

    public void clear() {
    	for ( Track track : tracks )
    		track.removed();
        tracks.clear();
    }

    /**
     * Return a composite score for the entire group.  For now use the maximum track
     * score.   Note that scores for tracks not appropriate to the score type will
     * return -Float.MAX, so they are effectively ignored.
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
        float score = -Float.MAX_VALUE;
        for (Track track : tracks) {
            if (track.isVisible()) {
                score = Math.max(score, track.getRegionScore(chr, start, end, zoom, type, frame));

            }
        }
        return score;
    }


    public String getName() {
        return name;
    }

    public boolean isDrawBorder() {
        return drawBorder;
    }


    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
    }

    public boolean isVisible() {
        for (Track t : tracks) {
            if ((t != null) && t.isVisible()) {
                return true;
            }
        }
        return false;
    }

    public int getHeight() {

        int h = 0;
        for (Track track : tracks) {
            if (track.isVisible()) {
                h += track.getHeight();
            }
        }
        return h;
    }

    public void renderName(Graphics2D g2D, Rectangle rect, boolean isSelected) {

        // Calculate fontsize
        int fontSize = PreferenceManager.getInstance().getAsInt(PreferenceManager.DEFAULT_FONT_SIZE);

        Font font = FontManager.getFont(Font.BOLD, fontSize);
        g2D.setFont(font);

        GraphicUtils.drawWrappedText(getName(), rect, g2D, true);

    }

    /**
     * Sort tracks by the array of attribute names.
     *
     * @param attributeNames
     * @param ascending
     */

    public void sortByAttributes(final String attributeNames[],
                                 final boolean[] ascending, boolean sortVariants) {
        if ((tracks != null) && !tracks.isEmpty()) {
            Comparator comparator = new Comparator() {

                public int compare(Object arg0, Object arg1) {
                    Track t1 = (Track) arg0;
                    Track t2 = (Track) arg1;

                    // Loop through the attributes in order (primary, secondary, tertiary, ...).  The
                    // first attribute to yield a non-zero comparison wins
                    for (int i = 0; i < attributeNames.length; i++) {
                        String attName = attributeNames[i];

                        if (attName != null) {
                            String value1 = t1.getAttributeValue(attName);

                            if (value1 == null) {
                                value1 = "";
                            }

                            value1 = value1.toLowerCase();
                            String value2 = t2.getAttributeValue(attName);

                            if (value2 == null) {
                                value2 = "";
                            }
                            value2 = value2.toLowerCase();

                            boolean isNumeric = AttributeManager.getInstance().isNumeric(attName);

                            int c = 0;
                            if (isNumeric) {
                                double d1;
                                try {
                                    d1 = Double.parseDouble(value1);
                                }
                                catch (NumberFormatException e) {
                                    d1 = Double.MIN_VALUE;
                                }
                                double d2;
                                try {
                                    d2 = Double.parseDouble(value2);
                                }
                                catch (NumberFormatException e) {
                                    d2 = Double.MIN_VALUE;
                                }
                                if (d2 > d1) c = 1;
                                else if (d2 < d1) c = -1;
                            } else {
                                c = value1.compareTo(value2);
                            }

                            if (c != 0) {
                                return ascending[i] ? c : -c;
                            }

                        }
                    }

                    // All compares are equal
                    return 0;
                }
            };

            // Step 1,  remove non-sortable tracks and remember position
            List<Track> unsortableTracks = new ArrayList();
            Map<Track, Integer> trackIndeces = new HashMap();
            for (int i = tracks.size() - 1; i >= 0; i--) {
                if (!tracks.get(i).isSortable()) {
                    Track t = tracks.remove(i);
                    unsortableTracks.add(t);
                    trackIndeces.put(t, i);

                }
            }

            // Step 2,  sort "sortable" tracks
            Collections.sort(tracks, comparator);

            // Step 3, put unortable tracks back in original order
            if (unsortableTracks.size() > 0) {
                for (int i = unsortableTracks.size() - 1; i >= 0; i--) {
                    Track t = unsortableTracks.get(i);
                    int index = trackIndeces.get(t);
                    tracks.add(index, t);
                }
            }
            
            // Step4, sort variant tracks
            if ( sortVariants )
	            for ( Track t : tracks )
	            	if ( t instanceof VariantTrack )
            		((VariantTrack)t).sortOnAttribute(attributeNames, ascending);
        }

    }


    public void sortGroup(final RegionOfInterest region,
                          String linkingAtt,
                          final RegionScoreType type,
                          final ReferenceFrame frame) {
        // Step 1,  remove non-sortable tracks and remember position
        List<Track> unsortableTracks = new ArrayList();
        Map<Track, Integer> trackIndeces = new HashMap();
        for (int i = tracks.size() - 1; i >= 0; i--) {
            if (!tracks.get(i).isSortable()) {
                Track t = tracks.remove(i);
                unsortableTracks.add(t);
                trackIndeces.put(t, i);

            }
        }

        List<Track> tracksWithScore = new ArrayList(getTracks().size());
        List<Track> otherTracks = new ArrayList(getTracks().size());
        for (Track t : getTracks(true)) {
            if (t.isRegionScoreType(type)) {
                tracksWithScore.add(t);
            } else {
                otherTracks.add(t);
            }
        }

        sortByRegionScore(tracksWithScore, region, type, frame);
        List<String> sortedAttributes = new ArrayList();
        for (Track t : tracksWithScore) {
            String att = t.getAttributeValue(linkingAtt);
            if (att != null) {
                sortedAttributes.add(att);
            }

        }
        sortByAttributeOrder(otherTracks, sortedAttributes, linkingAtt);

        tracks.clear();
        tracks.addAll(tracksWithScore);
        tracks.addAll(otherTracks);

        // Step 3, put unortable tracks back in original order
        if (unsortableTracks.size() > 0) {
            for (int i = unsortableTracks.size() - 1; i >= 0; i--) {
                Track t = unsortableTracks.get(i);
                int index = trackIndeces.get(t);
                tracks.add(index, t);
            }
        }

    }

    public void sortByRegionScore(final RegionOfInterest region,
                                  final RegionScoreType type,
                                  final ReferenceFrame frame) {
        sortByRegionScore(tracks, region, type, frame);
    }


    private void sortByRegionScore(List<Track> tracks,
                                   final RegionOfInterest region,
                                   final RegionScoreType type,
                                   final ReferenceFrame frame) {
        if ((tracks != null) && (region != null) && !tracks.isEmpty()) {
            final int zoom = Math.max(0, frame.getZoom());
            final String chr = region.getChr();
            final int start = region.getStart();
            final int end = region.getEnd();

            Comparator<Track> c = new Comparator<Track>() {

                public int compare(Track t1, Track t2) {
                    try {
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;

                        float s1 = t1.getRegionScore(chr, start, end, zoom, type, frame);
                        float s2 = t2.getRegionScore(chr, start, end, zoom, type, frame);

                        if (s2 > s1) {
                            return 1;
                        } else if (s1 < s2) {
                            return -1;
                        } else {
                            return 0;
                        }
                    } catch (Exception e) {
                        log.error("Error sorting tracks. Sort might not be accurate.", e);
                        return 0;
                    }

                }
            };
            Collections.sort(tracks, c);

        }
    }

    /**
     * @param sortedAttributes
     * @param attributeId
     */
    private void sortByAttributeOrder(List<Track> tracks,
                                      List<String> sortedAttributes,
                                      final String attributeId) {
        if ((tracks != null) && (sortedAttributes != null) && !tracks.isEmpty()) {

            // Create a rank hash.  Loop backwards so that the lowest index for an attribute
            final HashMap<String, Integer> rankMap = new HashMap(
                    sortedAttributes.size() * 2);
            for (int i = sortedAttributes.size() - 1; i >=
                    0; i--) {
                rankMap.put(sortedAttributes.get(i), i);
            }
            // Comparator for sorting in ascending order
            Comparator<Track> c = new Comparator<Track>() {

                public int compare(Track t1, Track t2) {
                    String a1 = t1.getAttributeValue(attributeId);
                    String a2 = t2.getAttributeValue(attributeId);
                    Integer r1 = ((a1 == null) ? null : rankMap.get(a1));
                    Integer r2 = ((a2 == null) ? null : rankMap.get(a2));
                    if ((r1 == null) && (r2 == null)) {
                        return 0;
                    } else if (r1 == null) {
                        return 1;
                    } else if (r2 == null) {
                        return -1;
                    } else {
                        return r1.intValue() - r2.intValue();
                    }

                }
            };

            Collections.sort(tracks, c);

        }

    }

    /**
     * @param trackIds
     */
    public void sortByList(List<String> trackIds) {

        final Map<String, Integer> trackPositions = new HashMap();
        for (int i = 0; i < trackIds.size(); i++) {
            trackPositions.put(trackIds.get(i), i);
        }
        Comparator c = new Comparator<Track>() {
            public int compare(Track t1, Track t2) {
                String id1 = t1.getId();
                int p1 = trackPositions.containsKey(id1) ? trackPositions.get(id1) : Integer.MAX_VALUE;
                String id2 = t2.getId();
                int p2 = trackPositions.containsKey(id2) ? trackPositions.get(id2) : Integer.MAX_VALUE;
                return p1 - p2;
            }
        };
        Collections.sort(tracks, c);
    }

    public void removeTracks(Collection<Track> tracksToRemove) {
    	for ( Track track : tracksToRemove )
    		if ( tracks.contains(track) )
    			track.removed();
        tracks.removeAll(tracksToRemove);
    }


    /**
     * If this group contains the targetTrack, insert the selectedTracks collection either before or after
     * the target and return true.   Otherwise return false.
     *
     * @param selectedTracks
     * @param targetTrack
     * @param before
     */
    public boolean moveSelectedTracksTo(Collection<Track> selectedTracks,
                                        Track targetTrack,
                                        boolean before) {

        int index = (targetTrack == null ? tracks.size() : tracks.indexOf(targetTrack));
        if (index < 0) {
            return false;
        }

        if (!before) {
            index = index + 1;
        }

        // 1. Divdide the target list up into 2 parts, one before the index and one after
        List<Track> beforeList = new ArrayList(tracks.subList(0, index));
        List<Track> afterList = new ArrayList(tracks.subList(index, tracks.size()));

        // 2.  Remove the selected tracks from anywhere they occur
        beforeList.removeAll(selectedTracks);
        afterList.removeAll(selectedTracks);

        // 3. Now insert the selected tracks
        tracks.clear();
        tracks.addAll(beforeList);
        tracks.addAll(selectedTracks);
        tracks.addAll(afterList);

        return true;

    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        TrackManager tm = IGV.getInstance().getTrackManager();
        tm.clearSelections();
        tm.setTrackSelections(new HashSet(tracks));

    }
}
