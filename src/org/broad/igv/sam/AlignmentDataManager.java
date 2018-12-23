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
package org.broad.igv.sam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import net.sf.samtools.util.CloseableIterator;

import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.sam.AlignmentTrack.SortOption;
import org.broad.igv.sam.reader.AlignmentReaderFactory;
import org.broad.igv.sam.reader.SamListReader;
import org.broad.igv.track.MultiFileWrapper;
import org.broad.igv.track.RenderContext;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.ArrayHeapObjectSorter;
import org.broad.igv.util.LongRunningTask;
import org.broad.igv.util.NamedRunnable;
import org.broad.igv.util.ResourceLocator;

public class AlignmentDataManager {

    private static Logger log = Logger.getLogger(AlignmentDataManager.class);

    private static final int DEFAULT_DEPTH = 10;

    /**
     * Map of reference frame -> alignment interval
     */
    //TODO -- this is a  potential memory leak, this map needs cleared when the gene list changes
    private HashMap<String, AlignmentInterval> loadedIntervalMap = new HashMap(50);

    HashMap<String, String> chrMappings = new HashMap();
    private boolean isLoading = false;
    private CachingQueryReader reader;
    private CoverageTrack coverageTrack;
    private int maxLevels;


    private boolean loadAsPairs = false;
    private static final int MAX_ROWS = 1000000;
    Map<String, PEStats> peStats;


    public AlignmentDataManager(ResourceLocator locator) throws IOException {

        PreferenceManager prefs = PreferenceManager.getInstance();
        maxLevels = prefs.getAsInt(PreferenceManager.SAM_MAX_LEVELS);

        if (locator.getPath().endsWith(".sam.list")) {
            MultiFileWrapper mfw = MultiFileWrapper.parse(locator);
            reader = new CachingQueryReader(new SamListReader(mfw.getLocators()));
        } else {
            reader = new CachingQueryReader(AlignmentReaderFactory.getReader(locator));
        }
        peStats = new HashMap();
        initChrMap();
    }


    private void initChrMap() {
        Genome genome = IGV.getInstance().getGenomeManager().getCurrentGenome();
        if (genome != null) {
            Set<String> seqNames = reader.getSequenceNames();
            if (seqNames != null) {
                for (String chr : seqNames) {
                    String alias = genome.getChromosomeAlias(chr);
                    chrMappings.put(alias, chr);
                }
            }
        }
    }

    public CachingQueryReader getReader() {
        return reader;
    }

    public Map<String, PEStats> getPEStats() {
        return peStats;
    }

    public boolean isPairedEnd() {
        return reader.isPairedEnd();
    }

    public boolean hasIndex() {
        return reader.hasIndex();
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    public void setMaxLevels(int maxLevels) {
        clear();
        reader.clearCache();
        this.maxLevels = maxLevels;
    }

    public void setCoverageTrack(CoverageTrack coverageTrack) {
        this.coverageTrack = coverageTrack;
    }

    /**
     * Return the loaded interval for the specified frame.  Note this can be null if the interval isn't loaded
     * yet.
     *
     * @param frame
     * @return
     */
    public AlignmentInterval getLoadedInterval(ReferenceFrame frame) {
        return loadedIntervalMap.get(frame.getName());
    }

    /**
     * Return the loaded interval for the RenderContext.  This method forces a load if the interval isn't present.
     * It is provided to work aroud problems with batch mode image generation.
     *
     * @param context
     * @return
     */
    public AlignmentInterval getLoadedInterval(RenderContext context) {
        ReferenceFrame frame = context.getReferenceFrame();
        if (!loadedIntervalMap.containsKey(frame.getName())) {
            // IF in batch mode force a load of the interval if its missing
            if (Globals.isBatch()) {
                int start = Math.max(0, (int) context.getOrigin() - 100);
                int end = (int) context.getEndLocation() + 100;
                loadAlignments(context.getChr(), start, end, context);
            }
        }

        return loadedIntervalMap.get(frame.getName());

    }


    /**
     * Sort alignment rows such that alignments that intersect from the
     * center appear left to right by start position
     */
    public void sortRows(SortOption option, ReferenceFrame referenceFrame) {
        AlignmentInterval loadedInterval = loadedIntervalMap.get(referenceFrame.getName());
        if (loadedInterval != null) {
            loadedInterval.sortRows(option, referenceFrame);
        }
    }

    public void sortRows(SortOption option, ReferenceFrame referenceFrame, double location) {
        AlignmentInterval loadedInterval = loadedIntervalMap.get(referenceFrame.getName());
        if (loadedInterval != null) {
            loadedInterval.sortRows(option, location);
        }
    }


    public boolean isLoadAsPairs() {
        return loadAsPairs;
    }

    public void setLoadAsPairs(boolean loadAsPairs) {
        if (loadAsPairs == this.loadAsPairs) {
            return;
        }
        boolean currentPairState = this.loadAsPairs;
        this.loadAsPairs = loadAsPairs;

        for (ReferenceFrame frame : FrameManager.getFrames()) {
            repackAlignments(frame, currentPairState);
        }

    }

    private void repackAlignments(ReferenceFrame referenceFrame, boolean currentPairState) {
        if (currentPairState == true) {
            AlignmentInterval loadedInterval = loadedIntervalMap.get(referenceFrame.getName());
            if (loadedInterval == null) {
                return;
            }
            List<AlignmentInterval.Row> alignmentRows = loadedInterval.getAlignmentRows();
            List<Alignment> alignments = new ArrayList(Math.min(50000, alignmentRows.size() * 1000));
            for (AlignmentInterval.Row row : alignmentRows) {
                for (Alignment al : row.alignments) {
                    if (al instanceof PairedAlignment) {
                        PairedAlignment pair = (PairedAlignment) al;
                        alignments.add(pair.firstAlignment);
                        if (pair.secondAlignment != null) {
                            alignments.add(pair.secondAlignment);
                        }
                    } else {
                        alignments.add(al);
                    }
                }
            }

            // ArrayHeapObjectSorter sorts in place (no additional memory required).
            ArrayHeapObjectSorter<Alignment> heapSorter = new ArrayHeapObjectSorter();
            heapSorter.sort(alignments, new Comparator<Alignment>() {
                public int compare(Alignment alignment, Alignment alignment1) {
                    return alignment.getStart() - alignment1.getStart();
                }
            });

            // When repacking keep all currently loaded alignments (don't limit to levels)
            int max = Integer.MAX_VALUE;
            List<AlignmentInterval.Row> tmp = (new AlignmentPacker()).packAlignments(
                    alignments.iterator(),
                    loadedInterval.getEnd(),
                    loadAsPairs, null, MAX_ROWS);

            loadedInterval.setAlignmentRows(tmp);

        } else {
            repackAlignments(referenceFrame);
        }
    }


    public void repackAlignments(ReferenceFrame referenceFrame) {
        repackAlignments(referenceFrame, null);
    }


    /**
     * Repack currently loaded alignments.
     *
     * @param referenceFrame
     */
    public void repackAlignments(ReferenceFrame referenceFrame, SortOption option) {
        AlignmentInterval loadedInterval = loadedIntervalMap.get(referenceFrame.getName());
        if (loadedInterval == null) {
            return;
        }

        Iterator<Alignment> iter = loadedInterval.getAlignmentIterator();

        // When repacking keep all currently loaded alignments (don't limit to levels)
        int max = Integer.MAX_VALUE;
        List<AlignmentInterval.Row> alignmentRows = (new AlignmentPacker()).packAlignments(
                iter,
                loadedInterval.getEnd(),
                loadAsPairs,
                option,
                MAX_ROWS);

        loadedInterval.setAlignmentRows(alignmentRows);
    }

    public synchronized List<AlignmentInterval.Row> getAlignmentRows(RenderContext context) {

        final String genomeId = context.getGenomeId();
        final String chr = context.getChr();
        final int start = (int) context.getOrigin();
        final int end = (int) context.getEndLocation() + 1;

        AlignmentInterval loadedInterval = loadedIntervalMap.get(context.getReferenceFrame().getName());

        // If we've moved out of the loaded interval start a new load.
        if (loadedInterval == null || !loadedInterval.contains(chr, start, end)) {
            log.debug("Loading alignments: " + chr + ":" + start + "-" + end);
            loadAlignments(chr, start, end, context);
        }

        // If there is any overlap in the loaded interval and the requested interval return it.
        if (loadedInterval != null && loadedInterval.overlaps(chr, start, end)) {
            return loadedInterval.getAlignmentRows();
        } else {
            return new LinkedList<AlignmentInterval.Row>();
        }
    }


    public void clear() {
        reader.clearCache();
        loadedIntervalMap.clear();
    }

    public void loadAlignments(final String chr, final int start, final int end, final RenderContext context) {

        if (isLoading || chr.equals(Globals.CHR_ALL)) {
            return;
        }

        log.debug("Load alignments.  isLoading=" + isLoading);
        isLoading = true;
        NamedRunnable runnable = new NamedRunnable() {

            public String getName() {
                return "loadAlignments";
            }

            public void run() {

                // Expand start and end to facilitate panning, but by no more than
                // 1 screen or 8kb, whichever is less
                // DON'T expand mitochondria

                int expandLength = reader.getTileSize(chr) / 2;
                int intervalStart = start - expandLength;
                int intervalEnd = end + expandLength;

                CloseableIterator<Alignment> iter = null;
                try {

                    String sequence = chrMappings.containsKey(chr) ? chrMappings.get(chr) : chr;

                    List<AlignmentCounts> counts = new ArrayList();

                    iter = reader.query(sequence, intervalStart, intervalEnd, counts, maxLevels, peStats);

                    final AlignmentPacker alignmentPacker = new AlignmentPacker();

                    List<AlignmentInterval.Row> alignmentRows = alignmentPacker.packAlignments(iter,
                            intervalEnd, loadAsPairs, null, maxLevels);

                    AlignmentInterval loadedInterval = new AlignmentInterval(chr, intervalStart, intervalEnd,
                            alignmentRows, counts);
                    loadedIntervalMap.put(context.getReferenceFrame().getName(), loadedInterval);


                    if (coverageTrack != null) {
                        coverageTrack.rescale(context.getReferenceFrame());
                    }

                    // TODO --- we need to force a repaint of the coverageTrack, which might not be in the same panel
                    if (context.getPanel() != null) context.getPanel().repaint();

                    //TODO -- this has to be done after every load in every panel.  Centralize this somewhere?  Have
                    //TODO --  a "DataLoadRunnable"?
                    IGV.getInstance().layoutMainPanel();


                } catch (Exception exception) {
                    if ( reader.showExceptionMessage(exception.getMessage()) )
                    {
                        log.error("Error loading alignments", exception);
                    	JOptionPane.showMessageDialog(IGV.getMainFrame(), "Error reading file: " + exception.getMessage());
                    }
                } finally {

                    if (iter != null) {
                        iter.close();
                    }
                    isLoading = false;
                }
            }
        };

        LongRunningTask.submit(runnable);


    }


    private boolean isMitochondria(String chr) {
        return chr.equals("M") || chr.equals("chrM") ||
                chr.equals("MT") || chr.equals("chrMT");
    }

    /**
     * TODO -- hacked to get by for now,
     *
     * @return the alignmentRows
     */
    public List<AlignmentInterval.Row> getAlignmentRows(ReferenceFrame referenceFrame) {
        AlignmentInterval loadedInterval = loadedIntervalMap.get(referenceFrame.getName());
        return loadedInterval == null ? null : loadedInterval.getAlignmentRows();
    }

    public int getNLevels() {
        int nLevels = 0;
        for (AlignmentInterval loadedInterval : loadedIntervalMap.values()) {
            nLevels = Math.max(nLevels, loadedInterval.getAlignmentRows().size());
        }
        return nLevels;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
                log.error("Error closing AlignmentQueryReader. ", ex);
            }
        }

    }

    public Collection<AlignmentInterval> getLoadedIntervals() {
        return loadedIntervalMap.values();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void updatePEStats(AlignmentTrack.RenderOptions renderOptions) {
        if (this.peStats != null) {
            for (PEStats stats : peStats.values()) {
                stats.compute(renderOptions.getMinInsertSizePercentile(), renderOptions.getMaxInsertSizePercentile());
            }
        }
    }

}

