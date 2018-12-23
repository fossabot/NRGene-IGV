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

package org.broad.igv.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.exceptions.ParserException;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.renderer.GeneTrackRenderer;
import org.broad.igv.renderer.IGVFeatureRenderer;
import org.broad.igv.track.FeatureCollectionSource;
import org.broad.igv.track.FeatureSource;
import org.broad.igv.track.FeatureTrack;
import org.broad.igv.track.Track;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.ui.IGV;
import org.broad.igv.util.ColorUtilities;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.StringUtils;
import org.broad.tribble.readers.AsciiLineReader;

/**
 * Created by IntelliJ IDEA.
 * User: jrobinso
 * Date: Oct 21, 2009
 * Time: 10:14:24 PM
 * To change this template use File | Settings | File Templates.
 */


public class GFFParser implements FeatureParser {

    static Logger log = Logger.getLogger(GFFParser.class);

    static HashSet exonTerms = new HashSet();
    static HashSet utrTerms = new HashSet();
    static HashSet<String> geneParts = new HashSet();
    static HashSet<String> ignoredTypes = new HashSet();
    
    static final boolean opt_no_parent_description_appended = System.getProperty("gff3_opt_no_parent_description_appended") != null;
    static final boolean opt_no_attributes = System.getProperty("gff3_opt_no_attributes") != null;
    static final boolean opt_parse_attributes_fast = System.getProperty("gff3_opt_parse_attributes_fast") != null;
    
    static final int attributeTrimSize = PreferenceManager.getInstance().getAsInt(PreferenceManager.ATTRIBUTE_TRIM_SIZE);

    //static String[] nameFields = {"gene", "Name", "name", "primary_name", "Locus", "locus","Alias", "alias", "systematic_id", "ID"};

    static private Map<String, FeatureCollectionSource>	sources = new LinkedHashMap<String, FeatureCollectionSource>();
    static private Map<String, Set<Track>> trackSources = new LinkedHashMap<String, Set<Track>>();
    
    static private Map<String, Object> sourcesLocks = new LinkedHashMap<String, Object>();
    static private Object getSourcesLock(String key)
    {
    	synchronized (sourcesLocks) {
			
    		Object		lock = sourcesLocks.get(key);
    		if ( lock == null )
    			sourcesLocks.put(key, lock = new Object());
    		
        	return lock;
		}
    	
    }


    static {
        utrTerms.add("five_prime_UTR");
        utrTerms.add("three_prime_UTR");
        utrTerms.add("5'-utr");
        utrTerms.add("3'-utr");
        utrTerms.add("3'-UTR");
        utrTerms.add("5'-UTR");
        utrTerms.add("5utr");
        utrTerms.add("3utr");
    }

    static {
        exonTerms.addAll(utrTerms);
        exonTerms.add("exon");
        exonTerms.add("coding_exon");
        exonTerms.add("CDS");
        exonTerms.add("cds");

    }


    static {
        geneParts.addAll(exonTerms);
        geneParts.add("transcript");
        geneParts.add("processed_transcript");
        geneParts.add("mrna");
        geneParts.add("mRNA");
        geneParts.add("promoter");
        geneParts.add("intron");
        geneParts.add("CDS_parts");

    }

    static {
        ignoredTypes.add("start_codon");
        ignoredTypes.add("stop_codon");
        ignoredTypes.add("Contig");
        ignoredTypes.add("RealContig");
        ignoredTypes.add("intron");
    }

    /*
    static {
        ignoreAttributes.add("ID");
        ignoreAttributes.add("Parent");
        ignoreAttributes.add("wormprep");
        ignoreAttributes.add("Index");
        ignoreAttributes.add("color");
        ignoreAttributes.add("colour");
        ignoreAttributes.add("exonNumber");

    }
    */


    Helper helper;

    Map<String, GFF3Transcript> transcriptCache = new HashMap(50000);
    Map<String, BasicFeature> geneCache = new HashMap(50000);
    private TrackProperties trackProperties = null;

    //static StringBuffer buf = new StringBuffer();


    public GFFParser(String path) {
        // Assume V2 until proven otherwise
        if (path.toLowerCase().endsWith("gff3") || path.toLowerCase().endsWith("gff3.gz")) {
            helper = new GFF3Helper();
        } else {
            helper = new GFF2Helper();
        }
    }

    /**
     * By definition this is a feature file
     */
    public boolean isFeatureFile(ResourceLocator locator) {
        return true;
    }


    public List<FeatureTrack> loadTracks(ResourceLocator locator, Genome genome) {

        if (locator.getPath().toLowerCase().endsWith("gff3") || locator.getPath().toLowerCase().endsWith("gff3.gz")) {
            helper = new GFF3Helper();
        }

        try {
            FeatureCollectionSource			source = getSource(locator, genome);

            FeatureTrack track = new FeatureTrack(locator, source);
            track.setName(locator.getTrackName());
            track.setRendererClass(IGVFeatureRenderer.class);
            track.setMinimumHeight(35);
            track.setHeight(45);
            track.setRendererClass(GeneTrackRenderer.class);

            if (trackProperties != null) {
                track.setProperties(trackProperties);
            }

            List<FeatureTrack> tracks = new ArrayList();
            tracks.add(track);
            return tracks;

        } catch (Exception ex) {
            log.error(ex);
            throw new RuntimeException(ex);

        } 
    }

    /**
     * Method description
     *
     * @param reader
     * @return
     */


    public List<org.broad.tribble.Feature> loadFeatures(AsciiLineReader reader, Genome genome) {
    	    	
        List<org.broad.tribble.Feature> features = new ArrayList();
        String line = null;
        try {

            if ( genome == null )
            	genome = IGV.getInstance().getGenomeManager().getCurrentGenome();
            Set<String> featuresToHide = new HashSet();
            String[] tokens = new String[200];
            
            double[]		startFraction = new double[1];
            double[]		endFraction = new double[1];

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("##gff-version") && line.endsWith("3")) {
                    helper = new GFF3Helper();
                }


                if (line.startsWith("#")) {
                    if (line.startsWith("#track") || line.startsWith("##track")) {
                        trackProperties = new TrackProperties();
                        ParsingUtils.parseTrackLine(line, trackProperties);
                    } else if (line.startsWith("#nodecode") || line.startsWith("##nodecode")) {
                        helper.setUrlDecoding(false);
                    } else if (line.startsWith("#hide") || line.startsWith("##hide")) {
                        String[] kv = line.split("=");
                        if (kv.length > 1) {
                            featuresToHide.addAll(Arrays.asList(kv[1].split(",")));
                        }
                    } else if (line.startsWith("#displayName") || line.startsWith("##displayName")) {
                        String [] nameTokens = line.split("=");
                        if(nameTokens.length < 2) {
                            helper.setNameFields(null);
                        }
                        else {
                            String [] fields = nameTokens[1].split(",");
                            helper.setNameFields(fields);
                        }

                    } else if (line.startsWith("#colorBy")) {

                    }
                    continue;
                }


                int nTokens = ParsingUtils.split(line, tokens, '\t');

                // GFF files have 9 tokens
                if (nTokens < 9) {
                    // Maybe its a track line?
                    if (line.startsWith("track")) {
                        trackProperties = new TrackProperties();
                        ParsingUtils.parseTrackLine(line, trackProperties);
                    }
                    continue;
                }

                // The type
                String featureType = new String(tokens[2].trim());

                if (ignoredTypes.contains(featureType)) {
                    continue;
                }


                String chromosome = genome == null ? tokens[0] : genome.getChromosomeAlias(tokens[0]);

                // GFF coordinates are 1-based inclusive (length = end - start + 1)
                // IGV (UCSC) coordinates are 0-based exclusive.  Adjust start and end accordingly
                int start;
                int end;
                
                try {
                	start = genome.parseIntCoordinate(tokens[3], startFraction) - 1;
                    //start = Integer.parseInt(tokens[3]) - 1;
                }
                catch (NumberFormatException ne) {
                    throw new ParserException("Column 4 must contain a numeric value", reader.getCurrentLineNumber(), line);
                }

                try {
                	end = genome.parseIntCoordinate(tokens[4], endFraction) - 1;
                    //end = Integer.parseInt(tokens[4]);
                }
                catch (NumberFormatException ne) {
                    throw new ParserException("Column 5 must contain a numeric value", reader.getCurrentLineNumber(), line);
                }

                Strand strand = convertStrand(tokens[6]);

                String attributeString = tokens[8];

                LinkedHashMap<String, String> attributes = new LinkedHashMap();
                //attributes.put("Type", featureType);
                if ( !opt_no_attributes )
            		helper.parseAttributes(attributeString, attributes);

                String description = getDescription(attributes, featureType);

                String id = helper.getID(attributes);

                String[] parentIds = helper.getParentIds(attributes, attributeString);


                 if (featureType.equals("CDS_parts")) {
                    for (String pid : parentIds) {
                        getGFF3Transcript(pid).addCDSParts(chromosome, start, end);
                    }

                } else if (featureType.equals("intron")) {

                    for (String pid : parentIds) {
                        getGFF3Transcript(pid).addCDSParts(chromosome, start, end);
                    }
                } else if (exonTerms.contains(featureType) && parentIds != null && parentIds.length > 0 &&
                        parentIds[0] != null && parentIds[0].length() > 0 && !parentIds[0].equals(".")) {
                    String name = getName(attributes);
                    int phase = -1;
                    String phaseString = tokens[7].trim();
                    if (!phaseString.equals(".")) {
                        try {
                            phase = Integer.parseInt(phaseString);

                        } catch (NumberFormatException numberFormatException) {

                            // Just skip setting the phase
                            log.error("GFF3 error: non numeric phase: " + phaseString);
                        }
                    }

                    // Make a copy of the exon record for each parent
                    for (String pid : parentIds) {

                        Exon exon = new Exon(chromosome, start, end, strand);
                        String		extendedDescription = description;
                        GFF3Transcript	transcript = getGFF3Transcript(pid);
                        if ( !opt_no_parent_description_appended )
	                        if ( transcript != null )
	                        	if ( transcript.transcript.description != null )
	                        		extendedDescription = description + "<br>" + pid + "<br>" + transcript.transcript.description;
                        exon.setDescription(extendedDescription);
                        exon.setAttributes(attributes);
                        exon.setUTR(utrTerms.contains(featureType));

                        if (phase >= 0) {
                            exon.setPhase(phase);

                        }
                        exon.setName(name);

                        if (featureType.equals("exon")) {
                        	if (attributes.containsKey("color")) {
                            	exon.setColor(ColorUtilities.getColorFromString(attributes.get("color")));
                            }
                            if (attributes.containsKey("Color")) {
                            	exon.setColor(ColorUtilities.getColorFromString(attributes.get("Color")));
                            }
                            getGFF3Transcript(pid).addExon(exon);
                        } else if (featureType.equals("CDS")) {
                            getGFF3Transcript(pid).addCDS(exon);
                        } else if (featureType.equals("five_prime_UTR") || featureType.equals("5'-UTR")) {
                            getGFF3Transcript(pid).setFivePrimeUTR(exon);
                        } else if (featureType.equals("three_prime_UTR") || featureType.equals("3'-UTR")) {
                            getGFF3Transcript(pid).setThreePrimeUTR(exon);
                        }
                    }


                } else {
                    BasicFeature f = new BasicFeature(chromosome, start, end, strand, startFraction[0], endFraction[0]);
                    f.setName(getName(attributes));
                    f.setDescription(description);
                    f.setAttributes(attributes);

                    if (attributes.containsKey("color")) {
                        f.setColor(ColorUtilities.stringToColor(attributes.get("color")));
                    }
                    if (attributes.containsKey("Color")) {
                        f.setColor(ColorUtilities.stringToColor(attributes.get("Color")));
                    }


                    f.setPath(reader.getPath());
                    
                    if (id == null) {
                        features.add(f);
                    } else {
                        f.setIdentifier(id);

                        if (featureType.equals("gene")) {
                            geneCache.put(id, f);
                            if (featuresToHide.contains(featureType)) {
                                FeatureDB.addFeature(f, genome);
                            } else {
                                features.add(f);
                            }
                        } else if (featureType.equals("mRNA") || featureType.equals("transcript")) {
                            String pid = null;
                            if (parentIds != null && parentIds.length > 0) {
                                pid = parentIds[0];
                            }
                            getGFF3Transcript(id).transcript(f, pid);
                        } else if (featuresToHide.contains(featureType)) {
                            FeatureDB.addFeature(f, genome);
                        } else {
                            features.add(f);
                        }
                    }
                }
            }

            // Create and add IGV genes
            for (GFF3Transcript transcript : transcriptCache.values()) {
                BasicFeature igvTranscript = transcript.createTranscript();
                if (igvTranscript != null) {
                    features.add(igvTranscript);
                }
            }

            FeatureDB.addFeatures(features, genome);

        }
        catch (ParserException e) {
            throw e;
        }
        catch (Exception
                ex) {
            log.error("Error parsing GFF file", ex);
            if (line != null && reader.getCurrentLineNumber() != 0) {

                throw new ParserException(ex.getMessage(), ex, reader.getCurrentLineNumber(), line);
            } else {
                throw new RuntimeException(ex);
            }
        }

        return features;
    }

    static String getDescription(Map<String, String> attributes) {
        return getDescription(attributes, null);
    }


    static String getDescription(Map<String, String> attributes, String type) {
        // 30 attributes is the maximum visible on a typical screen
    	// DK changed to 3000 to avoid clipping all together
        int max = 3000;
        int count = 0;
        
        // Reset buffer.
        StringBuilder		buf = new StringBuilder();
        if (type != null) {
            buf.append(type);
            buf.append("<br>");
        }
        for (Map.Entry<String, String> att : attributes.entrySet()) {

            //System.out.println("thread " + Thread.currentThread() + " key " + att.getKey());

            //if (!ignoreAttributes.contains(att.getKey())) {
        	String attValue = att.getValue();
        	if ( !att.getKey().equals("URL") && attValue.indexOf(';') >= 0 )
        		attValue = attValue.replaceAll(";", "<br>");
            buf.append(att.getKey());
            buf.append(" = ");
            buf.append(attValue);
            buf.append("<br>");
            if (++count > max) {
                buf.append("...");
                break;
                
            }

            //}
        }
        String description = buf.toString();

        return description;
    }

    private Strand convertStrand(String strandString) {
        Strand strand = Strand.NONE;
        if (strandString.equals("-")) {
            strand = Strand.NEGATIVE;
        } else if (strandString.equals("+")) {
            strand = Strand.POSITIVE;
        }

        return strand;
    }

    private GFF3Transcript getGFF3Transcript(String id) {
        GFF3Transcript transcript = transcriptCache.get(id);
        if (transcript == null) {
            transcript = new GFF3Transcript(id);
            transcriptCache.put(id, transcript);
        }
        return transcript;
    }


    String getName(Map<String, String> attributes) {

        if (attributes.size() == 0) {
            return null;
        }

        return helper.getName(attributes);

    }


    public static void splitFileByType(String gffFile, String outputDirectory) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(gffFile));
        String nextLine;
        String ext = gffFile.substring(gffFile.length() - 4);

        Map<String, PrintWriter> writers = new HashMap();
        String [] tokens = new String[20];

        while ((nextLine = br.readLine()) != null) {
            nextLine = nextLine.trim();
            if (!nextLine.startsWith("#")) {
                int nTokens = ParsingUtils.split(nextLine.trim().replaceAll("\"", ""), tokens, '\t');

                // GFF files have 9 columns
                String type = tokens[2];
                if (geneParts.contains(type)) {
                    type = "gene";
                }
                if (!writers.containsKey(type)) {
                    writers.put(type,
                            new PrintWriter(new FileWriter(new File(outputDirectory,
                                    type + ext))));
                }
            }
        }
        br.close();

        br = new BufferedReader(new FileReader(gffFile));
        PrintWriter currentWriter = null;
        while ((nextLine = br.readLine()) != null) {
            nextLine = nextLine.trim();
            if (nextLine.startsWith("#")) {
                ParsingUtils.split(nextLine.trim().replaceAll("\"", ""), tokens, '\t');

                // GFF files have 9 columns
                for (PrintWriter pw : writers.values()) {
                    pw.println(nextLine);
                }
            } else {
                int nTokens = ParsingUtils.split(nextLine.trim().replaceAll("\"", ""), tokens, '\t');
                String type = tokens[2];
                if (geneParts.contains(type)) {
                    type = "gene";
                }
                currentWriter = writers.get(type);

                if (currentWriter != null) {
                    currentWriter.println(nextLine);
                } else {
                    System.out.println("No writer for: " + type);
                }
            }

        }

        br.close();
        for (PrintWriter pw : writers.values()) {
            pw.close();
        }
    }

    public TrackProperties getTrackProperties() {
        return trackProperties;
    }

    class GFF3Transcript {

        private String id;
        private Set<Exon> exons = new HashSet();
        private List<Exon> cdss = new ArrayList();
        private Exon fivePrimeUTR;
        private Exon threePrimeUTR;
        private BasicFeature transcript;
        private String parentId;
        String chr = null;
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        String description;
        Map<String, String> attributes;

        GFF3Transcript(String id) {
            this.id = id;
        }

        void transcript(BasicFeature mRNA, String parent) {
            this.transcript = mRNA;
            this.parentId = parent;
            if (mRNA.getName() == null) {
                mRNA.setName(mRNA.getIdentifier());
            }
            int prefixIndex = mRNA.getName().indexOf(":");
            if (prefixIndex > 0) {
                mRNA.setName(mRNA.getName().substring(prefixIndex + 1));
            }

        }

        void setFivePrimeUTR(Exon exon) {
            fivePrimeUTR = exon;
            this.start = Math.min(exon.getStart(), start);
            this.end = Math.max(exon.getEnd(), end);
        }

        void setThreePrimeUTR(Exon exon) {
            threePrimeUTR = exon;
            this.start = Math.min(exon.getStart(), start);
            this.end = Math.max(exon.getEnd(), end);
        }

        void addExon(Exon exon) {
            exons.add(exon);
            this.start = Math.min(exon.getStart(), start);
            this.end = Math.max(exon.getEnd(), end);
        }

        void addCDS(Exon cds) {
            cdss.add(cds);
            this.start = Math.min(cds.getStart(), start);
            this.end = Math.max(cds.getEnd(), end);
        }

        void addCDSParts(String chr, int start, int end) {
            this.chr = chr;
            this.start = Math.min(this.start, start);
            this.end = Math.max(this.end, end);
        }

        void appendDescription(String desc, Map<String, String> atts) {
            if (description == null) {
                description = "<html>";
            } else {
                description += "<br>---------<br>";
            }
            description += desc;

            if(attributes == null) {
                attributes = atts;
            }
            else {
                attributes.putAll(atts);
            }
        }

        /**
         * Create a transcript from its constituitive parts. "
         *
         * @return
         */
        BasicFeature createTranscript() {

            Strand strand = Strand.NONE;
            String name = null;

            // Combine CDS and exons
            while (!cdss.isEmpty()) {
                Exon cds = cdss.get(0);
                Exon exon = findMatchingExon(cds);
                if (exon == null) {
                    cds.setCodingStart(cds.getStart());
                    cds.setCodingEnd(cds.getEnd());
                    exons.add(cds);
                } else {
                    exon.setCodingStart(cds.getStart());
                    exon.setCodingEnd(cds.getEnd());
                    exon.setReadingFrame(cds.getReadingShift());
                }
                cdss.remove(0);
            }
            for (Exon exon : exons) {
                chr = exon.getChr();
                strand = exon.getStrand();
                start = Math.min(exon.getStart(), start);
                end = Math.max(exon.getEnd(), end);
                name = exon.getName();
            }


            if (transcript == null) {
                // transcript is implied
                transcript = new BasicFeature(chr, start, end, strand);
                transcript.setIdentifier(id);
                transcript.setName(name == null ? id : name);
                transcript.setDescription(description);
                transcript.setAttributes(attributes);
            }

            if ((parentId != null) && geneCache.containsKey(parentId)) {
                BasicFeature gene = geneCache.get(parentId);
                geneCache.remove(parentId);
                if (transcript.getName() == null && gene.getName() != null) {
                    transcript.setName(gene.getName());
                }
                transcript.setDescription("Transcript<br>" + transcript.getDescription() + "<br>--------<br>Gene<br>" + gene.getDescription());

                // mRNA.setName(gene.getName());
            }

            for (Exon exon : exons) {
                transcript.addExon(exon);
            }


            transcript.sortExons();

            // If 5'UTR is represented by an exon, adjust its start, else add an exon to represnet 5'utr
            if (fivePrimeUTR != null) {
                fivePrimeUTR.setUTR(true);
                transcript.addExon(fivePrimeUTR);
                Exon exon = findMatchingExon(fivePrimeUTR);
                if (exon != null) {
                    if (exon.getStrand() == Strand.POSITIVE) {
                        exon.setStart(fivePrimeUTR.getEnd());
                    } else {
                        exon.setEnd(fivePrimeUTR.getStart());
                    }
                }
            }

            if (threePrimeUTR != null) {
                threePrimeUTR.setUTR(true);
                transcript.addExon(threePrimeUTR);
                Exon exon = findMatchingExon(threePrimeUTR);
                if (exon != null) {
                    if (exon.getStrand() == Strand.POSITIVE) {
                        exon.setEnd(threePrimeUTR.getStart());
                    } else {
                        exon.setStart(threePrimeUTR.getEnd());
                    }
                }
            }

            //transcript.setDescription(getDescription(transcript.getAttributes()));

            return transcript;
        }

        Exon findMatchingExon(IGVFeature cds) {
            for (Exon exon : exons) {
                if (exon.contains(cds)) {
                    return exon;
                }
            }
            return null;
        }
    }

    protected interface Helper {

        String[] getParentIds(Map<String, String> attributes, String attributeString);

        void parseAttributes(String attributeString, Map<String, String> map);

        String getID(Map<String, String> attributes);

        void setUrlDecoding(boolean b);

        String getName(Map<String, String> attributes);

        void setNameFields(String[] fields);
    }

    public static class GFF2Helper implements Helper {

        static String[] idFields = {"systematic_id", "ID", "transcript_id", "Name", "name", "primary_name", "gene", "Locus", "locus", "alias"};
        static String[] DEFAULT_NAME_FIELDS = {"gene", "Name", "name", "primary_name", "Locus", "locus", "Alias", "alias", "systematic_id", "ID"};

        private String[] nameFields;

        GFF2Helper() {
            this(DEFAULT_NAME_FIELDS);
        }

        GFF2Helper(String[] nameFields) {
            if (nameFields != null) {
                this.nameFields = nameFields;
            }

        }

        public void setUrlDecoding(boolean b) {
            // Ignored,  GFF files are never url DECODED
        }


        public void parseAttributes(String description, Map<String, String> kvalues) {

            List<String> kvPairs = StringUtils.breakQuotedString(description.trim(), ';');

            for (String kv : kvPairs) {
                List<String> tokens = StringUtils.breakQuotedString(kv, ' ');
                if (tokens.size() >= 2) {
                    String key = tokens.get(0).trim().replaceAll("\"", "");
                    String value = tokens.get(1).trim().replaceAll("\"", "");
                    kvalues.put(key, value);
                }
            }
        }


        public String[] getParentIds(Map<String, String> attributes, String attributeString) {

            String[] parentIds = new String[1];
            if (attributes.isEmpty()) {
                parentIds[0] = attributeString;
            } else {
                parentIds[0] = attributes.get("id");
                if (parentIds[0] == null) {
                    parentIds[0] = attributes.get("mRNA");
                }
                if (parentIds[0] == null) {
                    parentIds[0] = attributes.get("systematic_id");
                }
                if (parentIds[0] == null) {
                    parentIds[0] = attributes.get("transcript_id");
                }
                if (parentIds[0] == null) {
                    parentIds[0] = attributes.get("gene");
                }
                if (parentIds[0] == null) {
                    parentIds[0] = attributes.get("transcriptId");
                }
                if (parentIds[0] == null) {
                    parentIds[0] = attributes.get("proteinId");
                }
            }
            return parentIds;

        }


        public String getID(Map<String, String> attributes) {
            for (String nf : idFields) {
                if (attributes.containsKey(nf)) {
                    return attributes.get(nf);
                }
            }
            return getName(attributes);
        }

        public String getName(Map<String, String> attributes) {

            if (attributes.size() > 0 && nameFields != null) {
                for (String nf : nameFields) {
                    if (attributes.containsKey(nf)) {
                        return attributes.get(nf);
                    }
                }
            }

            return null;
        }

        public void setNameFields(String[] nameFields) {
            this.nameFields = nameFields;
        }
    }

    public static class GFF3Helper implements Helper {

        static String[] DEFAULT_NAME_FIELDS = {"Name", "Alias", "ID", "Gene", "gene", "Locus", "locus"};
        private boolean useUrlDecoding = true;

        private String[] nameFields;

        GFF3Helper() {
            this(DEFAULT_NAME_FIELDS);
        }

        GFF3Helper(String[] nameFields) {
            if (nameFields != null) {
                this.nameFields = nameFields;
            }

        }


        public String[] getParentIds(Map<String, String> attributes, String ignored) {
            String parentIdString = attributes.get("Parent");
            if (parentIdString != null) {
                return attributes.get("Parent").split(",");
            } else {
                return null;
            }
        }

        /**
         * Parse the column 9 attributes.  Attributes are separated by semicolons.
         *
         * @param description
         * @param kvalues
         */
        public void parseAttributes(String description, Map<String, String> kvalues) {

        	if ( opt_parse_attributes_fast )
        	{
        		for ( String kv : org.apache.commons.lang.StringUtils.split(description,  ';') )
        		{
        			String[]	tmp = org.apache.commons.lang.StringUtils.split(kv, '=');
        			int			nValues = tmp.length;
        			if ( nValues > 0 )
        			{
        				String		key = tmp[0];
        				String		value = ((nValues == 1) ? "" : tmp[1]);
        				
        				if ( useUrlDecoding )
        				{
        					if ( value.indexOf('%') >= 0 )
        						value = URLDecoder.decode(value);
        					
        					if (!key.equals("URL") && value.length() > attributeTrimSize) {
                                value = value.substring(0, attributeTrimSize) + " ...";
        					}
        				}
        					
        				kvalues.put(key, value);
        			}
        		}
        		
        		return;
        		}
        	
            List<String> kvPairs = StringUtils.breakQuotedString(description.trim(), ';');
            for (String kv : kvPairs) {
                //int nValues = ParsingUtils.split(kv, tmp, '=');
                List<String> tmp = StringUtils.breakQuotedString(kv, '=');
                int nValues = tmp.size();
                if (nValues > 0) {
                    String key = tmp.get(0).trim();
                    String value = ((nValues == 1) ? "" : tmp.get(1).trim());

                    if (useUrlDecoding) {
                        key = URLDecoder.decode(key);
                        value = URLDecoder.decode(value);
                        // Limit values to 50 characters
                        if(!key.equals("URL") && value.length() > attributeTrimSize) {
                            value = value.substring(0, attributeTrimSize) + " ...";
                        }
                    }
                    kvalues.put(key, value);
                } else {
                    log.debug("No attributes: " + description);
                }
            }
        }

        public void setUrlDecoding(boolean useUrlDecoding) {
            this.useUrlDecoding = useUrlDecoding;
        }

        public String getName(Map<String, String> attributes) {

            if (attributes.size() > 0 && nameFields != null) {
                for (String nf : nameFields) {
                    if (attributes.containsKey(nf)) {
                        return attributes.get(nf);
                    }
                }
            }

            return null;
        }

        public String getID(Map<String, String> attributes) {
            String id = attributes.get("ID");
            return id;
        }

        public String[] getNameFields() {
            return nameFields;
        }

        public void setNameFields(String[] nameFields) {
            this.nameFields = nameFields;
        }
    }


    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("SpitFilesByType <gffFile> <outputDirectory>");
            return;
        }
        splitFileByType(args[0], args[1]);
    }
    
    public static void clearSources()
    {
    	synchronized (sources) {
        	sources.clear();
		}
    	synchronized (trackSources) {
        	trackSources.clear();
		}
    }
    
    private static String getSourceKey(ResourceLocator locator, Genome genome)
    {
    	String	key = locator.getFileName();
    	if ( genome != null) 
    		key += "@" + genome.getDisplayName();
    	
    	File	file = new File(locator.getPath());
    	
    	if ( file.exists() )
    		key += "_" + file.lastModified();
    	
    	return key;
    	
    }
    
    private FeatureCollectionSource getSource(ResourceLocator locator, Genome genome) throws IOException
    {
    	AsciiLineReader 				reader = null;
    	FeatureCollectionSource			source = null;
    	String							key = getSourceKey(locator, genome);
    	Object							lock = getSourcesLock(key);
    	
    	synchronized (lock) {
    		
    		synchronized (sources)
    		{
    			if ( (source = sources.get(key)) != null )
    				return source;
    		}
			
	    	try
	    	{
	    		reader = ParsingUtils.openAsciiReader(locator);
	    		
	    		List<org.broad.tribble.Feature> features = loadFeatures(reader, genome);
	    		
	    		source = new FeatureCollectionSource(features, genome);
	    		source.setKey(key);
	    		
	    		synchronized (sources)
	    		{
	    			sources.put(key, source);
	    		}
	    	}
	    	finally
	    	{
	    		if ( reader != null )
	    			reader.close();
	    	}
	        
	        return source;
		}
    	
    }
    
    public static void registerTrackSource(Track track, FeatureSource source)
    {
    	if ( source instanceof FeatureCollectionSource )
    	{
    		String		key = ((FeatureCollectionSource)source).getKey();
    		
    		if ( sources.containsKey(key) )
    		{
	    		synchronized (trackSources) {
	
	    			Set<Track>	tracks = trackSources.get(key);
	    			if ( tracks == null )
	    				trackSources.put(key, tracks = new LinkedHashSet<Track>());
	    			
	    			tracks.add(track);
				}
    		}
    	}
    }
    
    public static void deregisterTrackSource(Track track, FeatureSource source)
    {
    	if ( source instanceof FeatureCollectionSource )
    	{
    		String		key = ((FeatureCollectionSource)source).getKey();
    		
    		if ( sources.containsKey(key) )
    		{
	    		synchronized (trackSources) {
	
	    			Set<Track>	tracks = trackSources.get(key);
	    			if ( tracks != null )
	    			{
	    				tracks.remove(track);
	    				if ( tracks.size() == 0 )
	    				{
	    					trackSources.remove(key);
	    					sources.remove(key);
	    				}
	    			}
				}
    		}
    	}
    }
}
