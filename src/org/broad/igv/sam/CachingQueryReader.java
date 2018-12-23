/*
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
package org.broad.igv.sam;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.util.CloseableIterator;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.exceptions.DataLoadException;
import org.broad.igv.sam.reader.AlignmentQueryReader;
import org.broad.igv.sam.reader.ReadGroupFilter;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.LRUCache;
import org.broad.igv.util.RuntimeUtils;

/**
 * A wrapper for an AlignmentQueryReader that caches query results
 *
 * @author jrobinso
 */
public class CachingQueryReader {

    private static Logger log = Logger.getLogger(CachingQueryReader.class);

    //private static final int LOW_MEMORY_THRESHOLD = 150000000;
    private static final int KB = 1000;
    private static final int MITOCHONDRIA_TILE_SIZE = 1000;
    private static int DEFAULT_TILE_SIZE = 16 * KB;
    private static int MAX_TILE_COUNT = 2;
    private static Set<WeakReference<CachingQueryReader>> activeReaders = Collections.synchronizedSet(new HashSet());

    // DK
    static boolean hack1 = Boolean.parseBoolean(System.getProperty("hack1", "false"));
    
    private Map<String, Date>		showExceptionMap = new LinkedHashMap<String, Date>();
    private long					showExceptionTimeout = 1000 * 60 * 10;	// 10 minutes
    
    // Map of read group -> paired end stats

    //private PairedEndStats peStats;

    private static void cancelReaders() {
        for (WeakReference<CachingQueryReader> readerRef : activeReaders) {
            CachingQueryReader reader = readerRef.get();
            if (reader != null) {
                reader.cancel = true;
            }
        }
        log.debug("Readers canceled");
        activeReaders.clear();
    }


    private String cachedChr = "";
    private int tileSize = DEFAULT_TILE_SIZE;
    private AlignmentQueryReader reader;
    private boolean cancel = false;
    private LRUCache<Integer, AlignmentTile> cache;
    private boolean pairedEnd = false;


    public CachingQueryReader(AlignmentQueryReader reader) {
        this.reader = reader;
        cache = new LRUCache(this, MAX_TILE_COUNT);
        float fvw = PreferenceManager.getInstance().getAsFloat(PreferenceManager.SAM_MAX_VISIBLE_RANGE);
        tileSize = Math.min(DEFAULT_TILE_SIZE, (int) (fvw * KB));
    }

    public AlignmentQueryReader getWrappedReader() {
        return reader;
    }

    public void close() throws IOException {
        reader.close();
    }

    public Set<String> getSequenceNames() {
        return reader.getSequenceNames();
    }

    public SAMFileHeader getHeader() throws IOException {
        return reader.getHeader();
    }

    public CloseableIterator<Alignment> iterator() {
        return reader.iterator();
    }

    public boolean hasIndex() {
        return reader.hasIndex();
    }

    public CloseableIterator<Alignment> query(String sequence, int start, int end, List<AlignmentCounts> counts,
                                              int maxReadDepth, Map<String, PEStats> peStats) {

        // Get the tiles covering this interval
        int startTile = (start + 1) / getTileSize(sequence);
        int endTile = end / getTileSize(sequence);    // <= inclusive

        // Be a bit conservative with maxReadDepth (get a few more reads than we think neccessary)
        int readDepthPlus = (int) (1.1 * maxReadDepth);

        List<AlignmentTile> tiles = getTiles(sequence, startTile, endTile, readDepthPlus, peStats);
        if (tiles.size() == 0) {
            return EmptyAlignmentIterator.getInstance();
        }

        // Count total # of records
        int recordCount = tiles.get(0).getOverlappingRecords().size();
        for (AlignmentTile t : tiles) {
            recordCount += t.getContainedRecords().size();
        }

        List<Alignment> alignments = new ArrayList(recordCount);
        alignments.addAll(tiles.get(0).getOverlappingRecords());
        for (AlignmentTile t : tiles) {
            alignments.addAll(t.getContainedRecords());
            counts.add(t.getCounts());
        }

        return new TiledIterator(start, end, alignments);
    }

    public List<AlignmentTile> getTiles(String seq, int startTile, int endTile, int maxReadDepth, Map<String, PEStats> peStats) {

        if (!seq.equals(cachedChr)) {
            cache.clear();
            cachedChr = seq;
        }

        List<AlignmentTile> tiles = new ArrayList(endTile - startTile + 1);
        List<AlignmentTile> tilesToLoad = new ArrayList(endTile - startTile + 1);

        int tileSize = getTileSize(seq);
        for (int t = startTile; t <= endTile; t++) {
            AlignmentTile tile = cache.get(t);

            if (tile == null) {
                int start = t * tileSize;
                int end = start + tileSize;

                tile = new AlignmentTile(seq, t, start, end, maxReadDepth);
            }

            tiles.add(tile);

            // The current tile is loaded,  load any preceding tiles we have pending and clear "to load" list
            if (tile.isLoaded()) {
                if (tilesToLoad.size() > 0) {
                    boolean success = loadTiles(seq, tilesToLoad, peStats);
                    if (!success) {
                        // Loading was canceled, return what we have
                        return tiles;
                    }
                }
                tilesToLoad.clear();
            } else {
                tilesToLoad.add(tile);
            }
        }

        if (tilesToLoad.size() > 0) {
            loadTiles(seq, tilesToLoad, peStats);
        }

        return tiles;
    }

    /**
     * Load alignments for the list of tiles
     *
     * @param chr
     * @param tiles
     * @return true if successful,  false if canceled.
     */
    private boolean loadTiles(String chr, List<AlignmentTile> tiles, Map<String, PEStats> peStats) {

    	// DK
    	long			startedAt = System.currentTimeMillis();
    	
        assert (tiles.size() > 0);

        boolean filterFailedReads = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SAM_FILTER_FAILED_READS);
        ReadGroupFilter filter = ReadGroupFilter.getFilter();
        boolean showDuplicates = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SAM_SHOW_DUPLICATES);
        int qualityThreshold = PreferenceManager.getInstance().getAsInt(PreferenceManager.SAM_QUALITY_THRESHOLD);

        //maxReadCount = PreferenceManager.getInstance().getAsInt(PreferenceManager.SAM_MAX_READS);

        if (log.isDebugEnabled()) {
            int first = tiles.get(0).getTileNumber();
            int end = tiles.get(tiles.size() - 1).getTileNumber();
            log.debug("Loading tiles: " + first + "-" + end);
        }

        int start = tiles.get(0).start;
        int end = tiles.get(tiles.size() - 1).end;


        CloseableIterator<Alignment> iter = null;

        //log.debug("Loading : " + start + " - " + end);
        int alignmentCount = 0;
        WeakReference<CachingQueryReader> ref = new WeakReference(this);
        try {
            Map<String, Alignment> mappedMates = new HashMap(1000);
            Map<String, Alignment> unmappedMates = new HashMap(1000);


            activeReaders.add(ref);
            iter = reader.query(chr, start, end, false);

            int tileSize = getTileSize(chr);
            while (iter != null && iter.hasNext()) {

                if (cancel) {
                    return false;
                }

                Alignment record = iter.next();

                // Set mate seqeunce of unmapped mates
                String readName = record.getReadName();
                if (record.isPaired() && record.getMate() != null ) {
                    pairedEnd = true;
                    if (record.isMapped()) {
                        if (!record.getMate().isMapped()) {
                            // record is mapped, mate is not
                            Alignment mate = unmappedMates.get(readName);
                            if (mate == null) {
                                mappedMates.put(readName, record);
                            } else {
                                record.setMateSequence(mate.getReadSequence());
                                unmappedMates.remove(readName);
                                mappedMates.remove(readName);
                            }

                        }
                    } else if (record.getMate().isMapped()) {
                        // record not mapped, mate is
                        Alignment mappedMate = mappedMates.get(readName);
                        if (mappedMate == null) {
                            unmappedMates.put(readName, record);
                        } else {
                            mappedMate.setMateSequence(record.getReadSequence());
                            unmappedMates.remove(readName);
                            mappedMates.remove(readName);
                        }
                    }
                }


                if (!record.isMapped() || (!showDuplicates && record.isDuplicate()) ||
                        (filterFailedReads && record.isVendorFailedRead()) ||
                        record.getMappingQuality() < qualityThreshold ||
                        (filter != null && filter.filterAlignment(record))) {
                    continue;
                }

                // Range of tile indeces that this alignment contributes to.
                int aStart = record.getAlignmentStart();
                int aEnd = record.getEnd();
                int idx0 = Math.max(0, (aStart - start) / tileSize);
                int idx1 = Math.min(tiles.size() - 1, (aEnd - start) / tileSize);

                // Loop over tiles this read overlaps
                for (int i = idx0; i <= idx1; i++) {
                    AlignmentTile t = tiles.get(i);
                    t.addRecord(record);
                }

                alignmentCount++;
                if (alignmentCount % 1000 == 0) {
                    if (cancel) return false;
                    IGV.getInstance().setStatusBarMessage("Reads loaded: " + alignmentCount);
                    if (checkMemory() == false) {
                        cancelReaders();
                        return false;        // <=  TODO need to cancel all readers
                    }
                }

                // Update pe stats
                if (peStats != null && record.isPaired() && record.isProperPair()) {
                    String lb = record.getLibrary();
                    if (lb == null) lb = "null";
                    PEStats stats = peStats.get(lb);
                    if (stats == null) {
                        stats = new PEStats(lb);
                        peStats.put(lb, stats);
                    }
                    stats.update(record);
                }
            }

            // Compute peStats
            if (peStats != null) {
                // TODO -- something smareter re the percentiles.  For small samples these will revert to min and max
                double minPercentile = PreferenceManager.getInstance().getAsFloat(PreferenceManager.SAM_MIN_INSERT_SIZE_PERCENTILE);
                double maxPercentile = PreferenceManager.getInstance().getAsFloat(PreferenceManager.SAM_MAX_INSERT_SIZE_PERCENTILE);
                for (PEStats stats : peStats.values()) {
                    stats.compute(minPercentile, maxPercentile);
                }
            }

            // Clean up any remaining unmapped mate seqeunces
            for (Alignment mappedMate : mappedMates.values()) {
                Alignment mate = unmappedMates.get(mappedMate.getReadName());
                if (mate != null) {
                    mappedMate.setMateSequence(mate.getReadSequence());
                }
            }
            mappedMates = null;
            unmappedMates = null;

            for (AlignmentTile t : tiles) {
                t.setLoaded(true);
                cache.put(t.getTileNumber(), t);
            }

            // DK
            long	finishedAt = System.currentTimeMillis();
            log.info("loaded in " + (finishedAt - startedAt) + " ms");
            
            return true;

        } catch (Throwable e) {

        	if ( showExceptionMessage(e.getMessage()) )
        		log.error("Error loading alignment data", e);
            throw new DataLoadException("", "Error: " + e.toString());
        }

        finally {
            // reset cancel flag.  It doesn't matter how we got here,  the read is complete and this flag is reset
            // for the next time
            cancel = false;
            activeReaders.remove(ref);
            if (iter != null) {
                iter.close();
            }
            IGV.getInstance().resetStatusMessage();
        }
    }


    private static synchronized boolean checkMemory() {
        if (RuntimeUtils.getAvailableMemoryFraction() < 0.2) {
            LRUCache.clearCaches();
            System.gc();
            if (RuntimeUtils.getAvailableMemoryFraction() < 0.2) {
                String msg = "Memory is low, reading terminating.";
                MessageUtils.showMessage(msg);
                return false;
            }

        }
        return true;
    }

    /**
     * @return the tileSize
     */
    public int getTileSize(String chr) {
        if (chr.equals("M") || chr.equals("chrM") || chr.equals("MT") || chr.equals("chrMT")) {
            return MITOCHONDRIA_TILE_SIZE;
        } else {
            return tileSize;
        }
    }

    public void clearCache() {
        if (cache != null) cache.clear();
    }

    /**
     * Does this file contain paired end data?  Assume not until proven otherwise.
     */
    public boolean isPairedEnd() {
        return pairedEnd;
    }

    public class TiledIterator implements CloseableIterator<Alignment> {

        Iterator<Alignment> currentSamIterator;
        int end;
        Alignment nextRecord;
        int start;
        List<Alignment> alignments;

        TiledIterator(int start, int end, List<Alignment> alignments) {
            this.alignments = alignments;
            this.start = start;
            this.end = end;
            currentSamIterator = alignments.iterator();
            advanceToFirstRecord();
        }

        public void close() {
            // No-op
        }

        public boolean hasNext() {
            return nextRecord != null;
        }

        public Alignment next() {
            Alignment ret = nextRecord;

            advanceToNextRecord();

            return ret;
        }

        public void remove() {
            // ignored
        }

        private void advanceToFirstRecord() {
            advanceToNextRecord();
        }

        private void advanceToNextRecord() {
            advance();

            while ((nextRecord != null) && (nextRecord.getEnd() < start)) {
                advance();
            }
        }

        private void advance() {
            if (currentSamIterator.hasNext()) {
                nextRecord = currentSamIterator.next();
                if (nextRecord.getAlignmentStart() > end) {
                    nextRecord = null;
                }
            } else {
                nextRecord = null;
            }
        }


    }

    /**
     * Caches alignments and counts for the coverage plot.
     * <p/>
     * Notes:
     * A "bucket" is a virtual container holding all the alignments with identical start position.  The concept
     * is introduced to control the # of alignments we hold in memory for deep coverage regions.  In practice,
     * little or no information is added by displaying more than ~50X coverage.  For an average alignment length L
     * and coverage deptgh D we do not need to store more than D/L alignments at any given start position.
     */

    //

    public static class AlignmentTile {

        private boolean loaded = false;
        private List<Alignment> containedRecords;
        private int end;
        private List<Alignment> overlappingRecords;
        private int start;
        private int tileNumber;
        private AlignmentCounts counts;

        int maxDepth;
        int e1;
        //int depthCount;

        private Map<String, Alignment> currentBucket;
        private Map<String, Alignment> currentMates;
        private List<Alignment> overflows;   // "Overflows"
        private Set<String> pairedReadNames;

        private static final Random RAND = new Random(System.currentTimeMillis());

        AlignmentTile(String chr, int tileNumber, int start, int end, int maxDepth) {
            this.tileNumber = tileNumber;
            this.start = start;
            this.end = end;
            containedRecords = new ArrayList(16000);
            overlappingRecords = new ArrayList();
            this.counts = new AlignmentCounts(chr, start, end);

            this.maxDepth = maxDepth;
            e1 = -1;
            currentBucket = new HashMap((int) (3 * maxDepth));
            currentMates = new HashMap((int) (3 * maxDepth));
            pairedReadNames = new HashSet(5 * maxDepth);
            overflows = new LinkedList<Alignment>();
        }

        public int getTileNumber() {
            return tileNumber;
        }


        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }


        /**
         * Add an alignment record to this tile.  This record is not neccessarily retained after down-sampling.
         *
         * @param record
         */
        public void addRecord(Alignment record) {

            if (record.getStart() > e1) {
                emptyBucket();
                e1 = record.getEnd();
            } else {
                e1 = Math.min(e1, record.getEnd());
            }

            final String readName = record.getReadName();
            if (!currentBucket.containsKey(readName)) {
                currentBucket.put(readName, record);
            } else if (!currentMates.containsKey(readName)) {
                currentMates.put(readName, record);
            } else {
                overflows.add(record);
            }
            counts.incCounts(record);

        }


        private void emptyBucket() {

        	// DK
        	if ( log.isDebugEnabled() )
        		log.debug(String.format("emptyBucket: %s IN contained:%d", this, containedRecords.size()));
        	
            List<Alignment> sampledRecords = sampleCurrentBucket();
            for (Alignment alignment : sampledRecords) {
                int aStart = alignment.getAlignmentStart();
                int aEnd = alignment.getEnd();
                if ((aStart >= start) && (aStart < end)) {
                    containedRecords.add(alignment);
                } else if ((aEnd >= start) && (aStart < start)) {
                    overlappingRecords.add(alignment);
                }
            }
            currentBucket.clear();
            currentMates.clear();
            overflows.clear();
            
        	// DK
        	if ( log.isDebugEnabled() )
        		log.debug(String.format("emptyBucket: %s OUT contained:%d", this, containedRecords.size()));
        }


        /**
         * Sample the current bucket of alignments to achieve the desired depth.
         *
         * @return
         */
        private List<Alignment> sampleCurrentBucket() {

        	// DK - set maxDepth to use for a threshold to be a bit far from the use used for trimming - so not to trim on each iteration
        	int			maxDepthThreshold = !hack1 ? maxDepth : (maxDepth * 3);
        	
            List<Alignment> sampledList = new ArrayList(maxDepth);
            if (currentBucket.size() + currentMates.size() + overflows.size() < maxDepthThreshold) {
                sampledList.addAll(currentBucket.values());
                sampledList.addAll(currentMates.values());
                sampledList.addAll(overflows);
            } else {

                // First pull out any mates of reads sampled from previous buckets
                List<String> added = new ArrayList(pairedReadNames.size());
                for (String readName : pairedReadNames) {
                    if (currentBucket.containsKey(readName)) {
                        sampledList.add(currentBucket.get(readName));
                        currentBucket.remove(readName);
                        added.add(readName);
                    }
                }
                pairedReadNames.removeAll(added);

                List<String> keys = new LinkedList(currentBucket.keySet());
                while (sampledList.size() < maxDepth && keys.size() > 0) {

                    // Remove a random alignment from the bucket.
                    String key = keys.remove(RAND.nextInt(keys.size()));
                    Alignment a = currentBucket.remove(key);
                    sampledList.add(a);

                    // If this alignment is paired,  add its mate to the list, or if its mate is not present
                    // in this bucket record the read name.
                    if (a.isPaired() && a.getMate() != null && a.getMate().isMapped()) {
                        Alignment m = currentMates.remove(key);
                        if (m != null) {
                            sampledList.add(m);
                        } else {
                            pairedReadNames.add(key);
                        }
                    }
                }

                //If there's still room,  sample the "overflows"
                while (sampledList.size() < maxDepth && (overflows.size() > 0)) {
                    sampledList.add(overflows.remove(RAND.nextInt(overflows.size())));
                }


                // Since we added in 2 passes we need to sort
                Collections.sort(sampledList, new Comparator<Alignment>() {
                    public int compare(Alignment alignment, Alignment alignment1) {
                        return alignment.getStart() - alignment1.getStart();
                    }
                });
            }
            return sampledList;
        }


        public List<Alignment> getContainedRecords() {
            return containedRecords;
        }


        public List<Alignment> getOverlappingRecords() {
            return overlappingRecords;
        }

        public boolean isLoaded() {
            return loaded;
        }

        public void setLoaded(boolean loaded) {
            this.loaded = loaded;

            if (loaded) {
                // Empty any remaining alignments in the current bucket
                emptyBucket();
                currentBucket = null;
                currentMates = null;
                pairedReadNames = null;
                overflows = null;
            }
        }

        public AlignmentCounts getCounts() {
            return counts;
        }
    }

	public boolean showExceptionMessage(String key) 
	{
		Date			lastDate = showExceptionMap.get(key);
		Date			now = new Date();
		
		// first time?
		if ( lastDate == null )
		{
			showExceptionMap.put(key, now);
			return true;
		}
		
		// stale?
		if ( (now.getTime() - lastDate.getTime()) >= showExceptionTimeout )
		{
			showExceptionMap.put(key, now);
			return true;			
		}
		
		// otherwise, ignore
		return false;
	}


}


