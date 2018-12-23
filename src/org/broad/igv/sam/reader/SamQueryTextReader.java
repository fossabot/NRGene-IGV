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
package org.broad.igv.sam.reader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;

import org.apache.log4j.Logger;
import org.broad.igv.sam.Alignment;
import org.broad.igv.sam.EmptyAlignmentIterator;
import org.broad.igv.sam.SamAlignment;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;
import org.broad.tribble.util.SeekableStream;
import org.broad.tribble.util.SeekableStreamFactory;

/**
 * A wrapper for SamTextReader that supports query by interval.
 *
 * @author jrobinso
 */
public class SamQueryTextReader implements AlignmentQueryReader {

    static Logger log = Logger.getLogger(SamQueryTextReader.class);
    String samFile;
    FeatureIndex featureIndex;
    SAMFileHeader header;


    public SamQueryTextReader(String samFile) throws IOException {
        this(samFile, true);
    }

    public SamQueryTextReader(String samFile, boolean requireIndex) throws IOException {
        this.samFile = samFile;
        loadHeader();

        if (requireIndex) {
            featureIndex = SamUtils.getIndexFor(samFile);
            if (featureIndex == null) {
                throw new IndexNotFoundException(samFile);
            }
        }
    }

    public SAMFileHeader getHeader() {
        return header;
    }


    private void loadHeader() {

        InputStream is = null;
        SAMFileReader reader = null;
        try {
            is = ParsingUtils.openInputStream(new ResourceLocator(samFile));
            BufferedInputStream bis = new BufferedInputStream(is);
            reader = new SAMFileReader(bis);
            header = reader.getFileHeader();

        }
        catch (IOException e) {
            log.error("Error loading header", e);
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (reader != null) {
                    reader.close();
                }

            }
            catch (Exception e) {

            }
        }
    }

    /*
    private void loadHeader() throws IOException {

        SeekableStream stream = null;
        try {
            stream = SeekableStreamFactory.getStreamFor(samFile);
            SAMFileReader reader = new SAMFileReader(stream);
            header = reader.getFileHeader();
            reader.close();
        }
        finally {
            stream.close();
            stream = null;
        }
    } */

    public CloseableIterator<Alignment> query(final String sequence, final int start, final int end, final boolean contained) {

        if (featureIndex == null) {
            featureIndex = SamUtils.getIndexFor(samFile);
        }

        if (featureIndex == null) {
            throw new java.lang.UnsupportedOperationException("SAM files must be indexed to support query methods");
        }
        if (!featureIndex.containsChromosome(sequence)) {
            return EmptyAlignmentIterator.getInstance();
        }

        // If contained == false (include overlaps) we need to adjust the start to
        // ensure we get features that extend into this segment.
        int startAdjustment = contained ? 0 : featureIndex.getLongestFeature(sequence);
        int startTileNumber = Math.max(0, (start - startAdjustment)) / featureIndex.getTileWidth();

        FeatureIndex.TileDef seekPos = featureIndex.getTileDef(sequence, startTileNumber);

        if (seekPos != null) {
            SeekableStream stream = null;
            try {

                // Skip to the start of the query interval and open a sam file reader
                stream = SeekableStreamFactory.getStreamFor(samFile);
                stream.seek(seekPos.getStartPosition());
                SAMFileReader reader = new SAMFileReader(stream);
                reader.setValidationStringency(ValidationStringency.SILENT);

                CloseableIterator<SAMRecord> iter = reader.iterator();
                return new SAMQueryIterator(sequence, start, end, contained, iter);

            } catch (IOException ex) {
                log.error("Error opening sam file", ex);
                throw new RuntimeException("Error opening: " + samFile, ex);
            }
        }
        return EmptyAlignmentIterator.getInstance();
    }

    public boolean hasIndex() {
        if (featureIndex == null) {
            getIndex();
        }
        return featureIndex != null;
    }

    public void close() throws IOException {
        // Nothing to close
    }


    private FeatureIndex getIndex() {
        if (featureIndex == null) {
            featureIndex = SamUtils.getIndexFor(samFile);
        }
        return featureIndex;
    }

    public Set<String> getSequenceNames() {
        FeatureIndex idx = getIndex();
        if (idx == null) {
            return null;
        } else {
            return idx.getIndexedChromosomes();
        }

    }

    public CloseableIterator<Alignment> iterator() { 
        try {

            // Skip to the start of the query interval and open a sam file reader
            SeekableStream stream = SeekableStreamFactory.getStreamFor(samFile);

            SAMFileReader reader = new SAMFileReader(stream);
            reader.setValidationStringency(ValidationStringency.SILENT);
            CloseableIterator<SAMRecord> iter = reader.iterator();
            return new SAMQueryIterator(iter);
        } catch (IOException e) {
            log.error("Error opening stream: " + samFile);
            throw new RuntimeException("Error creating stream for: " + samFile, e);
        }


    }

    /**
     *
     */
    class SAMQueryIterator implements CloseableIterator<Alignment> {

        String chr;
        int start;
        int end;
        boolean contained;
        SAMRecord currentRecord;
        CloseableIterator<SAMRecord> wrappedIterator;

        public SAMQueryIterator(CloseableIterator<SAMRecord> wrappedIterator) {
            this.chr = null;
            this.wrappedIterator = wrappedIterator;
            currentRecord = wrappedIterator.next();
        }

        public SAMQueryIterator(String sequence, int start, int end, boolean contained,
                                CloseableIterator<SAMRecord> wrappedIterator) {
            this.chr = sequence;
            this.start = start;
            this.end = end;
            this.contained = contained;
            this.wrappedIterator = wrappedIterator;
            advanceToFirstRecord();
        }

        private void advanceToFirstRecord() {
            while (wrappedIterator.hasNext()) {
                currentRecord = wrappedIterator.next();
                if (!currentRecord.getReferenceName().equals(chr)) {
                    break;
                } else if ((contained && currentRecord.getAlignmentStart() >= start) ||
                        (!contained && currentRecord.getAlignmentEnd() >= start)) {
                    break;
                }
            }
        }

        public void close() {
            wrappedIterator.close();
        }

        public boolean hasNext() {
            if (chr == null && currentRecord != null) {
                return true;
            }
            if (currentRecord == null || (chr != null && !chr.equals(currentRecord.getReferenceName()))) {
                return false;
            } else {
                return contained ? currentRecord.getAlignmentEnd() <= end
                        : currentRecord.getAlignmentStart() <= end;
            }
        }

        public SamAlignment next() {
            SAMRecord ret = currentRecord;
            if (wrappedIterator.hasNext()) {
                currentRecord = wrappedIterator.next();
            } else {
                currentRecord = null;
            }
            return new SamAlignment(ret);

        }

        public void remove() {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
