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
package org.broad.igv.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.samtools.util.CloseableIterator;

import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.Locus;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.sam.Alignment;
import org.broad.igv.sam.AlignmentBlock;
import org.broad.igv.sam.ReadMate;
import org.broad.igv.sam.reader.AlignmentQueryReader;
import org.broad.igv.sam.reader.AlignmentReaderFactory;
import org.broad.igv.sam.reader.MergedAlignmentReader;
import org.broad.igv.tools.parsers.DataConsumer;
import org.broad.igv.ui.filefilters.AlignmentFileFilter;
import org.broad.igv.util.FileUtils;
import org.broad.igv.util.stats.Distribution;

/**
 *   TODO -- normalize option
 *
 -n           Normalize the count by the total number of reads. This option
 multiplies each count by (1,000,000 / total # of reads). It
 is useful when comparing multiple chip-seq experiments when
 the absolute coverage depth is not important.
 */

/**
 *
 */
public class CoverageCounter {

    private int countThreshold = 0;

    private int upperExpectedInsertSize = 600;
    private int lowerExpectedInsertSize = 200;
    private Locus interval;


    enum Event {
        mismatch, indel, largeISize, smallISize, inversion, duplication, inter, unmappedMate
    }

    private String alignmentFile;
    private File tdfFile;
    private DataConsumer consumer;
    private float[] buffer;
    private int windowSize = 1;
    // TODO -- make mapping qulaity a parameter
    private int minMappingQuality = 0;
    private int strandOption = -1;
    private int extFactor;
    private int totalCount = 0;
    private File wigFile = null;
    private WigWriter wigWriter = null;
    private boolean keepZeroes = false;
    private boolean includeDuplicates = false;
    private Genome genome;

    private String readGroup; // The read group to count

    Map<Event, WigWriter> writers = new HashMap();

    private boolean computeTDF = true;

    private Distribution coverageHistogram;
    private static final double LOG_1__1 = 0.09531018;

    //private String interval = null;


    public CoverageCounter(String alignmentFile,
                           DataConsumer consumer,
                           int windowSize,
                           int extFactor,
                           File tdfFile,  // For reference
                           File wigFile,
                           Genome genome,
                           String options) {
        this.alignmentFile = alignmentFile;
        this.tdfFile = tdfFile;
        this.consumer = consumer;
        this.windowSize = windowSize;
        this.extFactor = extFactor;
        this.wigFile = wigFile;
        this.genome = genome;
        buffer = strandOption < 0 ? new float[1] : new float[2];

        if (options != null) {
            parseOptions(options);
        }

    }

    private void parseOptions(String options) {
        try {
            String[] opts = options.split(",");
            for (String opt : opts) {
                if (opt.startsWith("d")) {
                    includeDuplicates = true;
                } else if (opt.startsWith("m=")) {
                    String[] tmp = opt.split("=");
                    minMappingQuality = Integer.parseInt(tmp[1]);
                    System.out.println("Minimum mapping quality = " + minMappingQuality);
                } else if (opt.startsWith("t=")) {
                    String[] tmp = opt.split("=");
                    countThreshold = Integer.parseInt(tmp[1]);
                    System.out.println("Count threshold = " + countThreshold);
                } else if (opt.startsWith("l:")) {
                    String[] tmp = opt.split(":");
                    readGroup = tmp[1];
                } else if (opt.startsWith("q")) {
                    String[] tmp = opt.split("@");
                    interval = new Locus(tmp[1]);
                } else if (opt.startsWith("i")) {
                    writers.put(Event.largeISize, new WigWriter(new File(getFilenameBase() + ".large_isize.wig"), windowSize));
                    writers.put(Event.smallISize, new WigWriter(new File(getFilenameBase() + ".small_isize.wig"), windowSize));

                    // Optionally specify mean and std dev (todo -- just specify min and max)
                    String[] tokens = opt.split(":");
                    if (tokens.length > 2) {
                        //float median = Float.parseFloat(tokens[1]);
                        //float mad = Float.parseFloat(tokens[2]);
                        //upperExpectedInsertSize = (int) (median + 3 * mad);
                        //lowerExpectedInsertSize = Math.max(50, (int) (median - 3 * mad));
                        int min = Integer.parseInt(tokens[1]);
                        int max = Integer.parseInt(tokens[2]);
                        upperExpectedInsertSize = min;
                        lowerExpectedInsertSize = max;
                    } else {
                        PairedEndStats stats = PairedEndStats.compute(alignmentFile);
                        if (stats == null) {
                            System.out.println("Warning: error computing stats.  Using default insert size settings");
                        } else {
                            //double median = stats.getMedianInsertSize();
                            //double mad = stats.getMadInsertSize();
                            //upperExpectedInsertSize = (int) (median + 3 * mad);
                            //lowerExpectedInsertSize = Math.max(50, (int) (median - 3 * mad));
                            upperExpectedInsertSize = (int) stats.getMaxPercentileInsertSize();
                            lowerExpectedInsertSize = (int) stats.getMinPercentileInsertSize();
                            System.out.println(alignmentFile + "  min = " + lowerExpectedInsertSize + " max = " + upperExpectedInsertSize);
                        }
                    }

                } else if (opt.equals("o")) {
                    writers.put(Event.inversion, new WigWriter(new File(getFilenameBase() + ".inversion.wig"), windowSize));
                    writers.put(Event.duplication, new WigWriter(new File(getFilenameBase() + ".duplication.wig"), windowSize));
                } else if (opt.equals("m")) {
                    writers.put(Event.mismatch, new WigWriter(new File(getFilenameBase() + ".mismatch.wig"), windowSize));
                } else if (opt.equals("d")) {
                    writers.put(Event.indel, new WigWriter(new File(getFilenameBase() + ".indel.wig"), windowSize));
                } else if (opt.equals("u")) {
                    writers.put(Event.unmappedMate, new WigWriter(new File(getFilenameBase() + ".nomate.wig"), windowSize));
                } else if (opt.equals("r")) {
                    writers.put(Event.inter, new WigWriter(new File(getFilenameBase() + ".inter.wig"), windowSize));
                } else if (opt.equals("h")) {
                    coverageHistogram = new Distribution(200);
                } else if (opt.equals("z")) {
                    keepZeroes = true;
                } else {
                    System.out.println("Unknown coverage option: " + opt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    private String getFilenameBase() {
        String tmp = tdfFile.getAbsolutePath();
        tmp = tmp.substring(0, tmp.length() - 4);
        if (readGroup != null) {
            tmp += "." + readGroup;
        }
        return tmp;
    }


    // TODO -- options to ovveride all of these checks

    private boolean passFilter(Alignment alignment) {
        return (readGroup == null || readGroup.equals(alignment.getReadGroup())) &&
                alignment.isMapped() &&
                (includeDuplicates || !alignment.isDuplicate()) &&
                alignment.getMappingQuality() >= minMappingQuality &&
                !alignment.isVendorFailedRead();
    }

    public void parse() throws IOException {

        int tolerance = (int) (windowSize * (Math.floor(extFactor / windowSize) + 2));
        consumer.setSortTolerance(tolerance);

        AlignmentQueryReader reader = null;
        CloseableIterator<Alignment> iter = null;

        String lastChr = "";
        ReadCounter counter = null;


        try {

            if (wigFile != null) {
                wigWriter = new WigWriter(wigFile, windowSize);
            }


            if (interval == null) {
                reader = getReader(alignmentFile, false);
                iter = reader.iterator();
            } else {
                reader = getReader(alignmentFile, true);
                iter = reader.query(interval.getChr(), interval.getStart(), interval.getEnd(), false);
            }

            while (iter != null && iter.hasNext()) {
                Alignment alignment = iter.next();
                if (passFilter(alignment)) {

                    totalCount++;

                    String alignmentChr = alignment.getChr();

                    // Close all counters with position < alignment.getStart()
                    if (alignmentChr.equals(lastChr)) {
                        if (counter != null) {
                            counter.closeBucketsBefore(alignment.getAlignmentStart() - tolerance);
                        }
                    } else {
                        if (counter != null) {
                            counter.closeBucketsBefore(Integer.MAX_VALUE);
                        }
                        counter = new ReadCounter(alignmentChr);
                        lastChr = alignmentChr;
                    }

                    if (alignment.getMappingQuality() == 0) {
                        // TODO -- mq zero event
                    } else if (alignment.isPaired()) {

                        final int start = alignment.getStart();
                        final int end = alignment.getEnd();

                        counter.incrementPairedCount(start, end);

                        ReadMate mate = alignment.getMate();
                        boolean mateMapped = mate != null && mate.isMapped();
                        boolean sameChromosome = mateMapped && mate.getChr().equals(alignment.getChr());

                        if (mateMapped) {
                            if (sameChromosome) {

                                // Pair orientation
                                String oStr = alignment.getPairOrientation();
                                if (oStr.equals("R1F2") || oStr.equals("R2F1")) {
                                    counter.incrementPairedEvent(start, end, Event.duplication);
                                } else if (oStr.equals("F1F2") || oStr.equals("F2F1") ||
                                        oStr.equals("R1R2") || oStr.equals("R2R1")) {
                                    counter.incrementPairedEvent(start, end, Event.inversion);
                                }

                                // Insert size
                                int isize = Math.abs(alignment.getInferredInsertSize());
                                if (isize > upperExpectedInsertSize) {
                                    counter.incrementPairedEvent(start, end, Event.largeISize);

                                }
                                if (isize < lowerExpectedInsertSize) {
                                    counter.incrementPairedEvent(start, end, Event.smallISize);
                                }

                            } else {
                                counter.incrementPairedEvent(start, end, Event.inter);
                            }
                        } else {  // unmapped mate

                            counter.incrementPairedEvent(start, end, Event.unmappedMate);
                        }


                    }


                    AlignmentBlock[] blocks = alignment.getAlignmentBlocks();
                    if (blocks != null) {
                        int lastBlockEnd = -1;
                        for (AlignmentBlock block : blocks) {

                            if (!block.isSoftClipped()) {
                                if (lastBlockEnd >= 0) {
                                    String c = alignment.getCigarString();
                                    int s = block.getStart();
                                    if (s > lastBlockEnd) {
                                        counter.incrementEvent(lastBlockEnd, Event.indel);
                                    }
                                }

                                byte[] bases = block.getBases();
                                int blockStart = block.getStart();
                                int adjustedStart = block.getStart();
                                int adjustedEnd = block.getEnd();

                                if (alignment.isNegativeStrand()) {
                                    adjustedStart = Math.max(0, adjustedStart - extFactor);
                                } else {
                                    adjustedEnd += extFactor;
                                }

                                if (interval != null) {
                                    adjustedStart = Math.max(interval.getStart() - 1, adjustedStart);
                                    adjustedEnd = Math.min(interval.getEnd(), adjustedEnd);
                                }

                                for (int pos = adjustedStart; pos < adjustedEnd; pos++) {
                                    byte base = 0;
                                    int baseIdx = pos - blockStart;
                                    if (bases != null && baseIdx >= 0 && baseIdx < bases.length) {
                                        base = bases[baseIdx];
                                    }
                                    int idx = pos - blockStart;
                                    byte quality = (idx >= 0 && idx < block.qualities.length) ?
                                            block.qualities[pos - blockStart] : (byte) 0;
                                    counter.incrementCount(pos, base, quality);
                                }

                                lastBlockEnd = block.getEnd();
                            }
                        }
                    } else {
                        int adjustedStart = alignment.getAlignmentStart();
                        int adjustedEnd = alignment.getAlignmentEnd();

                        if (alignment.isNegativeStrand()) {
                            adjustedStart = Math.max(0, adjustedStart - extFactor);
                        } else {
                            adjustedEnd += extFactor;
                        }

                        if (interval != null) {
                            adjustedStart = Math.max(interval.getStart() - 1, adjustedStart);
                            adjustedEnd = Math.min(interval.getEnd(), adjustedEnd);
                        }


                        for (int pos = adjustedStart; pos < adjustedEnd; pos++) {
                            counter.incrementCount(pos, (byte) 0, (byte) 0);
                        }
                    }

                    if (writers.containsKey(Event.indel)) {
                        for (AlignmentBlock block : alignment.getInsertions()) {
                            if (interval == null || (block.getStart() >= interval.getStart() - 1 &&
                                    block.getStart() <= interval.getEnd())) {
                                counter.incrementEvent(block.getStart(), Event.indel);
                            }
                        }

                    }
                }

            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        finally {

            if (counter != null) {
                counter.closeBucketsBefore(Integer.MAX_VALUE);
            }

            consumer.setAttribute("totalCount", String.valueOf(totalCount));
            consumer.parsingComplete();

            if (iter != null) {
                iter.close();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }


            if (wigWriter != null) {
                wigWriter.close();
            }

            for (WigWriter writer : writers.values()) {
                writer.close();
            }


            if (coverageHistogram != null) {
                try {
                    PrintWriter pw = new PrintWriter(new FileWriter(getFilenameBase() + ".hist.txt"));
                    coverageHistogram.print(pw);
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    private AlignmentQueryReader getReader(String alignmentFile, boolean b) throws IOException {

        boolean isList = alignmentFile.indexOf(",") > 0;
        if (isList) {
            String[] tokens = alignmentFile.split(",");
            List<AlignmentQueryReader> readers = new ArrayList(tokens.length);
            for (String f : tokens) {
                readers.add(AlignmentReaderFactory.getReader(f, b));
            }
            return new MergedAlignmentReader(readers);
        } else {
            if (!FileUtils.isRemote(alignmentFile)) {
                File f = new File(alignmentFile);
                if (f.isDirectory()) {
                    List<AlignmentQueryReader> readers = new ArrayList();
                    for (File file : f.listFiles(new AlignmentFileFilter())) {
                        readers.add(AlignmentReaderFactory.getReader(file.getAbsolutePath(), b));
                    }
                    return new MergedAlignmentReader(readers);
                }
            }
            return AlignmentReaderFactory.getReader(alignmentFile, b);
        }

    }

    class ReadCounter {

        String chr;
        TreeMap<Integer, Counter> counts = new TreeMap();

        ReadCounter(String chr) {
            this.chr = chr;
        }

        void incrementCount(int position, byte base, byte quality) {
            final Counter counter = getCounterForPosition(position);
            counter.increment(position, base, quality);
        }

        void incrementEvent(int position, Event type) {
            final Counter counter = getCounterForPosition(position);
            counter.incrementEvent(type, 1);
        }

        void incrementPairedCount(int start, int end) {
            int startIdx = start / windowSize;
            int endIdx = end / windowSize;
            for (int idx = startIdx; idx <= endIdx; idx++) {
                Counter counter = getCounter(idx);
                counter.incrementPairedCount(fractionOverlap(counter, start, end));
            }
        }

        // frac should be between 0 and 1

        void incrementPairedEvent(int start, int end, Event type) {
            int startIdx = start / windowSize;
            int endIdx = end / windowSize;
            for (int idx = startIdx; idx <= endIdx; idx++) {
                Counter counter = getCounter(idx);
                counter.incrementEvent(type, fractionOverlap(counter, start, end));
            }
        }

        // todo -- move to counter class?

        float fractionOverlap(Counter counter, int start, int end) {
            float counterLength = counter.end - counter.start;
            float overlapLength = Math.min(end, counter.end) - Math.max(start, counter.start);
            return overlapLength / counterLength;

        }


        //public void incrementISize(Alignment alignment) {
        //    int startBucket = alignment.getStart() / windowSize;
        //    int endBucket = alignment.getAlignmentEnd() / windowSize;
        //    for (int bucket = startBucket; bucket <= endBucket; bucket++) {
        //        int bucketStartPosition = bucket * windowSize;
        //        int bucketEndPosition = bucketStartPosition + windowSize;
        //        if (!counts.containsKey(bucket)) {
        //            counts.put(bucket, new Counter(chr, bucketStartPosition, bucketEndPosition));
        //        }
        //        final Counter counter = counts.get(bucket);
        //        counter.incrementISize(alignment.getInferredInsertSize());
        //    }
        //}


        private Counter getCounterForPosition(int position) {
            int idx = position / windowSize;
            return getCounter(idx);
        }

        private Counter getCounter(int idx) {
            if (!counts.containsKey(idx)) {
                int counterStartPosition = idx * windowSize;
                int counterEndPosition = counterStartPosition + windowSize;
                counts.put(idx, new Counter(chr, counterStartPosition, counterEndPosition));
            }
            final Counter counter = counts.get(idx);
            return counter;
        }


        //void incrementNegCount(int position) {
        //    Integer bucket = position / windowSize;
        //    if (!counts.containsKey(bucket)) {
        //        counts.put(bucket, new Counter());
        //    }
        //    counts.get(bucket).incrementNeg();
        //}

        void closeBucketsBefore(int position) {
            List<Integer> bucketsToClose = new ArrayList();

            Integer bucket = position / windowSize;
            for (Map.Entry<Integer, Counter> entry : counts.entrySet()) {
                if (entry.getKey() < bucket) {

                    final Counter counter = entry.getValue();
                    int totalCount = counter.getCount();

                    if (totalCount > countThreshold) {

                        // Divide total count by window size.  This is the average count per
                        // base over the window,  so 30x coverage remains 30x irrespective of window size.
                        int bucketStartPosition = entry.getKey() * windowSize;
                        int bucketEndPosition = bucketStartPosition + windowSize;
                        if (genome != null) {
                            Chromosome chromosome = genome.getChromosome(chr);
                            if (chromosome != null) {
                                bucketEndPosition = Math.min(bucketEndPosition, chromosome.getLength());
                            }
                        }
                        int bucketSize = bucketEndPosition - bucketStartPosition;

                        buffer[0] = ((float) totalCount) / bucketSize;

                        if (strandOption > 0) {
                            buffer[1] = ((float) counter.getCount()) / bucketSize;
                        }

                        consumer.addData(chr, bucketStartPosition, bucketEndPosition, buffer, null);

                        for (Map.Entry<Event, WigWriter> entries : writers.entrySet()) {
                            Event evt = entries.getKey();
                            WigWriter writer = entries.getValue();
                            float score = counter.getEventScore(evt);
                            writer.addData(chr, bucketStartPosition, bucketEndPosition, score);
                        }

                        if (wigWriter != null) {
                            wigWriter.addData(chr, bucketStartPosition, bucketEndPosition, buffer[0]);
                        }

                        /*
                        if (coverageHistogram != null) {

                           List<Feature> transcripts = FeatureUtils.getAllFeaturesAt(bucketStartPosition, 10000, 5, genes, false);
                            if (transcripts != null && !transcripts.isEmpty()) {

                                boolean isCoding = false;
                                for (Feature t : transcripts) {
                                    Exon exon = ((IGVFeature) t).getExonAt(bucketStartPosition);
                                    if (exon != null && !exon.isUTR(bucketStartPosition)) {
                                        isCoding = true;
                                        break;
                                    }
                                }

                                if (isCoding) {
                                    int[] baseCounts = counter.getBaseCount();
                                    for (int i = 0; i < baseCounts.length; i++) {
                                        coverageHistogram.addDataPoint(baseCounts[i]);
                                    }
                                }

                       }
                        */
                    }

                    bucketsToClose.add(entry.getKey());
                }
            }

            for (Integer key : bucketsToClose) {
                counts.remove(key);
            }


        }

    }


    /**
     * Events
     * base mismatch
     * translocation
     * insertion (small)
     * insertion (large, rearrangment)
     * deletion  (small)
     * deletion  (large, rearrangment)
     */
    class Counter {
        int count = 0;
        int negCount = 0;
        int qualityCount = 0;

        float pairedCount = 0;
        float mismatchCount = 0;
        float indelCount = 0;
        float largeISizeCount = 0;
        float smallISizeCount = 0;
        float inversionCount = 0;
        float duplicationCount = 0;
        float unmappedMate = 0;
        float interChrCount = 0;
        float totalISizeCount = 0;

        String chr;
        int start;
        int end;
        byte[] ref;
        int[] baseCount;

        //int isizeCount = 0;
        //float isizeZScore = 0;
        //float totalIsize = 0;

        Counter(String chr, int start, int end) {
            this.chr = chr;
            this.start = start;
            this.end = end;
            baseCount = new int[end - start];
            //ref = SequenceHelper.readSequence(genome.getId(), chr, start, end);
        }


        int getCount() {
            return count;
        }

        int getNegCount() {
            return negCount;
        }


        void incrementNeg() {
            negCount++;
        }


        // frac should be between 0 znd 1

        void incrementPairedCount(float frac) {
            pairedCount += frac;
        }


        public int[] getBaseCount() {
            return baseCount;
        }


        void increment(int position, byte base, byte quality) {

            // Qualities of 2 or less => no idea what this base is
            //if (quality <= 2) {
            //    return;
            //}

            int offset = position - start;
            baseCount[offset]++;

            if (ref != null && ref.length > offset) {
                byte refBase = ref[offset];
                if (refBase != base) {
                    mismatchCount += quality;
                }
            }
            count++;
            qualityCount += quality;
        }

        // frac should be between 0 and 1

        void incrementEvent(Event evt, float frac) {
            switch (evt) {
                case indel:
                    indelCount += frac;
                    break;
                case largeISize:
                    largeISizeCount += frac;
                    break;
                case smallISize:
                    smallISizeCount += frac;
                    break;
                case inversion:
                    inversionCount += frac;
                    break;
                case duplication:
                    duplicationCount += frac;
                    break;
                case inter:
                    interChrCount += frac;
                    break;
                case unmappedMate:
                    unmappedMate += frac;
                    break;
            }
        }


        public float getEventScore(Event evt) {
            switch (evt) {
                case mismatch:
                    return qualityCount < 25 ? 0 : mismatchCount / qualityCount;
                case indel:
                    return count < 5 ? 0 : indelCount / count;
                case largeISize:
                    return pairedCount < 5 ? 0 : largeISizeCount / pairedCount;
                case smallISize:
                    return pairedCount < 5 ? 0 : smallISizeCount / pairedCount;
                case inversion:
                    return pairedCount < 5 ? 0 : inversionCount / pairedCount;
                case duplication:
                    return pairedCount < 5 ? 0 : duplicationCount / pairedCount;
                case inter:
                    return (pairedCount < 5 ? 0 : interChrCount / pairedCount);
                case unmappedMate:
                    return (pairedCount < 5 ? 0 : unmappedMate / pairedCount);
            }
            throw new RuntimeException("Unknown event type: " + evt);
        }


        public void incrementISize(int inferredInsertSize) {

            totalISizeCount++;
            if (inferredInsertSize > 600) {
                largeISizeCount++;
            } else if (inferredInsertSize < 200) {
                smallISizeCount++;
            }

            //float zs = Math.min(6, (Math.abs(inferredInsertSize) - meanInsertSize) / stdDevInsertSize);
            //isizeZScore += zs;
            //ppCount++;
            //if (Math.abs(Math.abs(inferredInsertSize) - meanInsertSize) > 2 * stdDevInsertSize) {
            //    isizeCount++;
            //}
            //otalIsize += Math.abs(inferredInsertSize);
        }


        //float getISizeFraction() {
        //    if (pairedCount < 3) {
        //        return 0;
        //    }
        //    float frac = ((float) isizeCount) / pairedCount;
        //    return frac;
        //float avg = isizeZScore / ppCount;
        //return avg;
        //}

        //float getAvgIsize() {
        //    return pairedCount == 0 ? 0 : totalIsize / pairedCount;
        //}

    }


    /**
     * Creates a vary step wig file
     */
    class WigWriter {

        Event event = null;
        String lastChr = null;
        int lastPosition = 0;
        int step;
        int span;
        PrintWriter pw;

        WigWriter(File file, int step) throws IOException {
            this.step = step;
            this.span = step;
            pw = new PrintWriter(new FileWriter(file));
        }

        WigWriter(File file, int step, Event event) throws IOException {
            this.step = step;
            this.span = step;
            pw = new PrintWriter(new FileWriter(file));
            this.event = event;
        }

        public void addData(String chr, int start, int end, float data) {

            if (Float.isNaN(data)) {
                return;
            }
            if (genome.getChromosome(chr) == null) {
                return;
            }

            if ((!keepZeroes && data == 0) || end <= start) {
                return;
            }

            int dataSpan = end - start;

            if (chr == null || !chr.equals(lastChr) || dataSpan != span) {
                span = dataSpan;
                outputStepLine(chr, start + 1);
            }
            pw.println((start + 1) + "\t" + data);
            lastPosition = start;
            lastChr = chr;

        }

        private void close() {
            pw.close();

        }

        private void outputStepLine(String chr, int start) {
            pw.println("variableStep chrom=" + chr + " span=" + span);
        }

    }


}
