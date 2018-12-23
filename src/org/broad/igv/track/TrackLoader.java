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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bigwig.BigWigDataSource;
import org.broad.igv.das.DASFeatureSource;
import org.broad.igv.data.DataSource;
import org.broad.igv.data.DatasetDataSource;
import org.broad.igv.data.IGVDataset;
import org.broad.igv.data.WiggleDataset;
import org.broad.igv.data.WiggleParser;
import org.broad.igv.data.db.SampleInfoSQLReader;
import org.broad.igv.data.db.SegmentedSQLReader;
import org.broad.igv.data.expression.GCTDataset;
import org.broad.igv.data.expression.GCTDatasetParser;
import org.broad.igv.data.rnai.RNAIDataSource;
import org.broad.igv.data.rnai.RNAIGCTDatasetParser;
import org.broad.igv.data.rnai.RNAIGeneScoreParser;
import org.broad.igv.data.rnai.RNAIHairpinParser;
import org.broad.igv.data.seg.FreqData;
import org.broad.igv.data.seg.SegmentedAsciiDataSet;
import org.broad.igv.data.seg.SegmentedBinaryDataSet;
import org.broad.igv.data.seg.SegmentedDataSource;
import org.broad.igv.exceptions.DataLoadException;
import org.broad.igv.feature.AbstractFeatureParser;
import org.broad.igv.feature.FeatureParser;
import org.broad.igv.feature.GisticFileParser;
import org.broad.igv.feature.MutationParser;
import org.broad.igv.feature.PSLParser;
import org.broad.igv.feature.dranger.DRangerParser;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.feature.tribble.FeatureFileHeader;
import org.broad.igv.goby.GobyAlignmentQueryReader;
import org.broad.igv.goby.GobyCountArchiveDataSource;
import org.broad.igv.gwas.GWASData;
import org.broad.igv.gwas.GWASParser;
import org.broad.igv.gwas.GWASTrack;
import org.broad.igv.lists.GeneList;
import org.broad.igv.lists.GeneListManager;
import org.broad.igv.lists.VariantListManager;
import org.broad.igv.maf.MAFTrack;
import org.broad.igv.maf.conservation.OmegaDataSource;
import org.broad.igv.maf.conservation.OmegaTrack;
import org.broad.igv.nrgene.api.ApiRequest;
import org.broad.igv.peaks.PeakTrack;
import org.broad.igv.renderer.CosmicFeatureRenderer;
import org.broad.igv.renderer.HeatmapRenderer;
import org.broad.igv.renderer.IGVFeatureRenderer;
import org.broad.igv.renderer.MutationRenderer;
import org.broad.igv.renderer.ScatterplotRenderer;
import org.broad.igv.sam.AlignmentDataManager;
import org.broad.igv.sam.AlignmentTrack;
import org.broad.igv.sam.BedRenderer;
import org.broad.igv.sam.CoverageTrack;
import org.broad.igv.sam.EWigTrack;
import org.broad.igv.sam.SpliceJunctionFinderTrack;
import org.broad.igv.sam.reader.IndexNotFoundException;
import org.broad.igv.synteny.BlastMapping;
import org.broad.igv.synteny.BlastParser;
import org.broad.igv.tdf.TDFDataSource;
import org.broad.igv.tdf.TDFReader;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.TrackFilter;
import org.broad.igv.ui.panel.AttributeHeaderPanel;
import org.broad.igv.ui.util.ConfirmDialog;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.HttpUtils;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.variant.VariantMenu;
import org.broad.igv.variant.VariantMenu.AttributesComparator;
import org.broad.igv.variant.VariantTrack;
import org.broad.igv.variant.util.PedigreeUtils;
import org.broad.tribble.util.SeekableBufferedStream;
import org.broad.tribble.util.SeekableStream;
import org.broad.tribble.util.SeekableStreamFactory;
import org.broadinstitute.sting.utils.codecs.vcf.VCFHeader;

/**
 * User: jrobinso
 * Date: Feb 14, 2010
 */
public class TrackLoader {

    private static Logger log = Logger.getLogger(TrackLoader.class);
   
    private IGV igv;
    
    /**
     * Switches on various attributes of locator (mainly locator path extension and whether the locator is indexed)
     * to call the appropriate loading method.
     *
     * @param locator
     * @return
     */
    public List<Track> load(ResourceLocator locator, IGV igv, Genome genome) {

        this.igv = igv;
       
        if ( genome == null )
        	genome = igv == null ? null : igv.getGenomeManager().getCurrentGenome();

        final String path = locator.getPath();
        try {
            String typeString = locator.getType();
            if ( typeString == null )
            	typeString = extractTypeFromUrl(path);
            if (typeString == null) {

                // Genome space hack -- check for explicit type converter
                //https://dmtest.genomespace.org:8444/datamanager/files/users/SAGDemo/Step1/TF.data.tab
                //   ?dataformat=http://www.genomespace.org/datamanager/dataformat/gct/0.0.0
            	// DK: further hacked to ease condition (removed isGenomeSpace and ? from ?dataformat
                if ((path.startsWith("http://") || path.startsWith("https://")) /*&& GSUtils.isGenomeSpace(new URL(path))*/ && path.contains("dataformat")) {
                    if (path.contains("dataformat/gct")) {
                        typeString = ".gct";
                    } else if (path.contains("dataformat/bed")) {
                        typeString = ".bed";
                    } else if (path.contains("dataformat/cn")) {
                        typeString = ".cn";
                    } else if (path.contains("dataformat/vcf")) {
                        typeString = ".vcf";
                    }

                } else {
                    typeString = path.toLowerCase();
                    
                    // remove url params
                    /*
                    int			qIndex = typeString.indexOf("?");
                    if ( qIndex >= 0 )
                    	typeString = typeString.substring(0, qIndex);
                    */
                    
                    if (!typeString.endsWith("_sorted.txt") &&
                            (typeString.endsWith(".txt") || typeString.endsWith(
                                    ".xls") || typeString.endsWith(".gz"))) {
                        typeString = typeString.substring(0, typeString.lastIndexOf("."));
                        
                        
                        
                    }
                }
            }
            typeString = typeString.toLowerCase();

            if (typeString.endsWith(".tbi")) {
                MessageUtils.showMessage("<html><b>Error:</b>File type '.tbi' is not recognized.  If this is a 'tabix' index <br>" +
                        " load the associated gzipped file, which should have an extension of '.gz'");
            }

            //This list will hold all new tracks created for this locator
            List<Track> newTracks = new ArrayList<Track>();

            // TODO -- hack for testing/development of database support.  Test DB only has segmented files
            String serverURL = locator.getServerURL();
            if (serverURL != null && serverURL.startsWith("jdbc:")) {
                this.loadFromDatabase(locator, newTracks, genome);
            } else if (typeString.endsWith(".gmt")) {
                loadGMT(locator);
            } else if (typeString.equals("das")) {
                loadDASResource(locator, newTracks);
            } else if (isIndexed(path)) {
                loadIndexed(locator, newTracks, genome);
            } else if (typeString.endsWith(".vcf") || typeString.endsWith(".vcf4")) {
                // TODO This is a hack,  vcf files must be indexed.  Fix in next release.
                throw new IndexNotFoundException(path);
            } else if (typeString.endsWith(".trio")) {
                loadTrioData(locator);
            } else if (typeString.endsWith("varlist")) {
                VariantListManager.loadVariants(locator);
            } else if (typeString.endsWith("samplepathmap")) {
                VariantListManager.loadSamplePathMap(locator);
            } else if (typeString.endsWith("h5") || typeString.endsWith("hbin")) {
                throw new DataLoadException("HDF5 files are no longer supported", locator.getPath());
            } else if (typeString.endsWith(".rnai.gct")) {
                loadRnaiGctFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".gct") || typeString.endsWith("res") || typeString.endsWith("tab")) {
                loadGctFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".cn") || typeString.endsWith(".xcn") || typeString.endsWith(".snp") ||
                    typeString.endsWith(".igv") || typeString.endsWith(".loh")) {
                loadIGVFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".cbs") || typeString.endsWith(".seg") ||
                    typeString.endsWith("glad") || typeString.endsWith("birdseye_canary_calls")) {
                loadSegFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".seg.zip")) {
                loadBinarySegFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".gistic")) {
                loadGisticFile(locator, newTracks);
            } else if (typeString.endsWith(".gs")) {
                loadRNAiGeneScoreFile(locator, newTracks, RNAIGeneScoreParser.Type.GENE_SCORE, genome);
            } else if (typeString.endsWith(".riger")) {
                loadRNAiGeneScoreFile(locator, newTracks, RNAIGeneScoreParser.Type.POOLED, genome);
            } else if (typeString.endsWith(".hp")) {
                loadRNAiHPScoreFile(locator);
            } else if (typeString.endsWith("gene")) {
                loadGeneFile(locator, newTracks, genome);
            } else if (typeString.contains(".tabblastn") || typeString.endsWith(".orthologs")) {
                loadSyntentyMapping(locator, newTracks);
            } else if (typeString.endsWith(".sam") || typeString.endsWith(".bam") ||
                    typeString.endsWith(".sam.list") || typeString.endsWith(".bam.list") ||
                    typeString.endsWith("_sorted.txt") ||
                    typeString.endsWith(".aligned") || typeString.endsWith(".sai") ||
                    typeString.endsWith(".bai")) {
                loadAlignmentsTrack(locator, newTracks, genome);
            } else if (typeString.endsWith(".bedz")) {
                loadIndexdBedFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".omega")) {
                loadOmegaTrack(locator, newTracks, genome);
            } else if (typeString.endsWith(".wig") || (typeString.endsWith(".bedgraph")) ||
                    typeString.endsWith("cpg.txt") || typeString.endsWith(".expr")) {
                loadWigFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".list")) {
                loadListFile(locator, newTracks, genome);
            } else if (typeString.contains(".dranger")) {
                loadDRangerFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".ewig.tdf") || (typeString.endsWith(".ewig.ibf"))) {
                loadEwigIBFFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".bw") || typeString.endsWith(".bb") || typeString.endsWith(".bigwig") ||
                    typeString.endsWith(".bigbed")) {
                loadBWFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".ibf") || typeString.endsWith(".tdf")) {
                loadTDFFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".counts")) {
                loadGobyCountsArchive(locator, newTracks, genome);
            } else if (typeString.endsWith(".psl") || typeString.endsWith(".psl.gz") ||
                    typeString.endsWith(".pslx") || typeString.endsWith(".pslx.gz")) {
                loadPslFile(locator, newTracks, genome);
                //TODO AbstractFeatureParser.getInstanceFor() is called twice.  Wasteful
            } else if (AbstractFeatureParser.getInstanceFor(locator, genome) != null) {
                loadFeatureFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".mut")) { //  MutationParser.isMutationAnnotationFile(locator)) {
                this.loadMutFile(locator, newTracks, genome);
            } else if (WiggleParser.isWiggle(locator)) {
                loadWigFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".maf") || typeString.endsWith(".maf.annotated")) {
                if (MutationParser.isMutationAnnotationFile(locator)) {
                    loadMutFile(locator, newTracks, genome);
                } else {
                    loadMAFTrack(locator, newTracks);
                }
            } else if (path.toLowerCase().contains(".peak.bin")) {
                loadPeakTrack(locator, newTracks, genome);
            } else if ("mage-tab".equals(locator.getType()) || GCTDatasetParser.parsableMAGE_TAB(locator)) {
                locator.setDescription("MAGE_TAB");
                loadGctFile(locator, newTracks, genome);
            } else if (typeString.endsWith(".logistic") || typeString.endsWith(".linear") || typeString.endsWith(".assoc") ||
                    typeString.endsWith(".qassoc") || typeString.endsWith(".gwas")) {
                loadGWASFile(locator, newTracks);
            } else if (GobyAlignmentQueryReader.supportsFileType(path)) {
                loadAlignmentsTrack(locator, newTracks, genome);
            } else if (!typeString.endsWith(".idx") && AttributeManager.isSampleInfoFile(locator)) {
                // This might be a sample information file.
                AttributeManager.getInstance().loadSampleInfo(locator);
            } else {
                MessageUtils.showMessage("<html>Unknown file type: " + path + "<br>Check file extenstion");
            }

            // Track line
            TrackProperties tp = null;
            String trackLine = locator.getTrackLine();
            if (trackLine != null) {
                tp = new TrackProperties();
                ParsingUtils.parseTrackLine(trackLine, tp);
            }
            
            // debug
            if ( locator.getApiRequest() != null )
            {
            	log.info("apiRequest: " + locator.getApiRequest());
            }

            for (Track track : newTracks) {

                if (locator.getUrl() != null) {
                    track.setUrl(locator.getUrl());
                }
                if (tp != null) {
                    track.setProperties(tp);
                }
                if (locator.getColor() != null) {
                    track.setColor(locator.getColor());
                }
                if (locator.getSampleId() != null) {
                    track.setSampleId(locator.getSampleId());
                }
            }


            return newTracks;
        } catch (DataLoadException dle) {
        	dle.printStackTrace();
            throw dle;
        } catch (Exception e) {
        	e.printStackTrace();
            log.error(e);
            throw new DataLoadException(e.getMessage(), path);
        }

    }

    private String extractTypeFromUrl(String path) 
    {
    	// handles only urls
    	if ( !path.startsWith("http") )
    		return null;
    	
    	// get path component (remove params)
    	path = StringUtils.split(path, '?')[0];
    	
    	// get extension
    	int		index = path.lastIndexOf('.');
    	if ( index < 0 )
    		return null;
    	String	ext = path.substring(index);
    	
    	// some sanity
    	if ( ext.length() > 15 )
    		return null;
    	
    	return ext;
	}

	private void loadGMT(ResourceLocator locator) throws IOException {
        List<GeneList> lists = GeneListManager.getInstance().importGMTFile(locator.getPath());
        if (lists.size() == 1) {
            GeneList gl = lists.get(0);
            IGV.getInstance().setGeneList(gl.getName(), true);
        } else {
            MessageUtils.showMessage("Loaded " + lists.size() + " gene lists.");
        }
    }

    private void loadIndexed(ResourceLocator locator, List<Track> newTracks, Genome genome) throws IOException {

        TribbleFeatureSource src = TribbleFeatureSource.getInstance(locator.getPath(), genome);
        String typeString = locator.getPath();
        if ( ApiRequest.isNiu() )
        	typeString = ApiRequest.extractCleanPath(typeString);
        if ( ApiRequest.isApiUrl(locator.getPath()) && (locator.getApiRequest() == null) )
        {
        	locator.setApiRequest(new ApiRequest(new URL(locator.getPath())));
        	locator.getApiRequest().postRequest(true);
        }
        //Track t;

        if (typeString.endsWith("vcf") || typeString.endsWith("vcf.gz")) {

            VCFHeader header = (VCFHeader) src.getHeader();

            // Test if the input VCF file contains methylation rate data:

            // This is determined by testing for the presence of two sample format fields: MR and GB, used in the
            // rendering of methylation rate.
            // MR is the methylation rate on a scale of 0 to 100% and GB is the number of bases that pass
            // filter for the position. GB is needed to avoid displaying positions for which limited coverage
            // prevents reliable estimation of methylation rate.
            boolean enableMethylationRateSupport = (header.getFormatHeaderLine("MR") != null &&
                    header.getFormatHeaderLine("GB") != null);

            List<String> allSamples = new ArrayList(header.getGenotypeSamples());

            VariantTrack t = new VariantTrack(locator, src, allSamples, enableMethylationRateSupport);
            
            // apply global sort
            String		globalSort = AttributeHeaderPanel.getReportedSortTextRepresentation();
            if ( !StringUtils.isEmpty(globalSort) )
            	t.sortSamples(new AttributesComparator(globalSort));
            
            // apply global filter
            String		globalFilter = VariantMenu.getGlobalFilter();
            if ( !StringUtils.isEmpty(globalFilter) )
            	t.filterSamples(new TrackFilter(globalFilter), false);
            
            // VCF tracks handle their own margin
            t.setMargin(0);
            newTracks.add(t);
        } else {

            // Create feature source and track
            FeatureTrack t = new FeatureTrack(locator, src);
            t.setName(locator.getTrackName());
            //t.setRendererClass(BasicTribbleRenderer.class);

            // Set track properties from header
            Object header = src.getHeader();
            if (header != null && header instanceof FeatureFileHeader) {
                FeatureFileHeader ffh = (FeatureFileHeader) header;
                if (ffh.getTrackType() != null) {
                    t.setTrackType(ffh.getTrackType());
                }
                if (ffh.getTrackProperties() != null) {
                    t.setProperties(ffh.getTrackProperties());
                }

                if (ffh.getTrackType() == TrackType.REPMASK) {
                    t.setHeight(15);
                    t.setPreferredHeight(15);
                }
            }
            newTracks.add(t);
        }

    }

    /**
     * Load the input file as a BED or Attribute (Sample Info) file.  First assume
     * it is a BED file,  if no features are found load as an attribute file.
     *
     * @param locator
     * @param newTracks
     */
    private void loadGeneFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        FeatureParser featureParser = AbstractFeatureParser.getInstanceFor(locator, genome);
        if (featureParser != null) {
            List<FeatureTrack> tracks = featureParser.loadTracks(locator, genome);
            newTracks.addAll(tracks);
        }

    }

    private void loadSyntentyMapping(ResourceLocator locator, List<Track> newTracks) {

        List<BlastMapping> mappings = (new BlastParser()).parse(locator.getPath());
        List<org.broad.tribble.Feature> features = new ArrayList<org.broad.tribble.Feature>(mappings.size());
        features.addAll(mappings);

        Genome genome = igv.getGenomeManager().getCurrentGenome();
        FeatureTrack track = new FeatureTrack(locator, new FeatureCollectionSource(features, genome));
        track.setName(locator.getTrackName());
        // track.setRendererClass(AlignmentBlockRenderer.class);
        newTracks.add(track);
    }

    private void loadDRangerFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        DRangerParser parser = new DRangerParser();
        newTracks.addAll(parser.loadTracks(locator, genome));
    }

    /**
     * Load the input file as a feature, mutation, or maf (multiple alignment) file.
     *
     * @param locator
     * @param newTracks
     */
    private void loadPslFile(ResourceLocator locator, List<Track> newTracks, Genome genome) throws IOException {

        PSLParser featureParser = new PSLParser(genome);
        List<FeatureTrack> tracks = featureParser.loadTracks(locator, genome);
        newTracks.addAll(tracks);
        for (FeatureTrack t : tracks) {
            t.setMinimumHeight(10);
            t.setHeight(30);
            t.setPreferredHeight(30);
            t.setDisplayMode(Track.DisplayMode.EXPANDED);

        }


    }

    /**
     * Load the input file as a feature, muation, or maf (multiple alignment) file.
     *
     * @param locator
     * @param newTracks
     */
    private void loadFeatureFile(ResourceLocator locator, List<Track> newTracks, Genome genome) throws IOException {

        if (locator.isLocal() && (locator.getPath().endsWith(".bed") ||
                locator.getPath().endsWith(".bed.txt") ||
                locator.getPath().endsWith(".bed.gz"))) {
            //checkSize takes care of warning the user
            if (!checkSize(locator.getPath())) {
                return;
            }
        }

        FeatureParser featureParser = AbstractFeatureParser.getInstanceFor(locator, genome);
        if (featureParser != null) {
            List<FeatureTrack> tracks = featureParser.loadTracks(locator, genome);
            newTracks.addAll(tracks);
        } else if (MutationParser.isMutationAnnotationFile(locator)) {
            this.loadMutFile(locator, newTracks, genome);
        } else if (WiggleParser.isWiggle(locator)) {
            loadWigFile(locator, newTracks, genome);
        } else if (locator.getPath().toLowerCase().contains(".maf")) {
            loadMAFTrack(locator, newTracks);
        }


    }

    /**
     * Load the input file as a feature, muation, or maf (multiple alignment) file.
     *
     * @param locator
     * @param newTracks
     */
    private void loadIndexdBedFile(ResourceLocator locator, List<Track> newTracks, Genome genome) throws IOException {

        File featureFile = new File(locator.getPath());
        File indexFile = new File(locator.getPath() + ".sai");
        FeatureSource src = new IndexedBEDFeatureSource(featureFile, indexFile, genome);
        Track t = new FeatureTrack(locator, src);
        newTracks.add(t);

    }

    /**
     * Load GWAS PLINK result file
     *
     * @param locator
     * @param newTracks
     * @throws IOException
     */


    private void loadGWASFile(ResourceLocator locator, List<Track> newTracks) throws IOException {

        GWASParser gwasParser = new GWASParser(locator);
        GWASData gwasData = gwasParser.parse();

        GWASTrack gwasTrack = new GWASTrack(locator, locator.getPath(), locator.getFileName(), gwasData, gwasParser);
        newTracks.add(gwasTrack);

    }


    private void loadRnaiGctFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        RNAIGCTDatasetParser parser = new RNAIGCTDatasetParser(locator, genome);

        Collection<RNAIDataSource> dataSources = parser.parse();
        if (dataSources != null) {
            String path = locator.getPath();
            for (RNAIDataSource ds : dataSources) {
                String trackId = path + "_" + ds.getName();
                DataSourceTrack track = new DataSourceTrack(locator, trackId, ds.getName(), ds, genome);

                // Set attributes.
                track.setAttributeValue("SCREEN", ds.getScreen());
                track.setHeight(80);
                track.setPreferredHeight(80);
                newTracks.add(track);
            }
        }
    }

    private void loadGctFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        if (locator.isLocal()) {
            if (!checkSize(locator.getPath())) {
                return;
            }
        }

        GCTDatasetParser parser = null;
        GCTDataset ds = null;

        String fName = locator.getTrackName();

        // TODO -- handle remote resource
        try {
            parser = new GCTDatasetParser(locator, null, igv.getGenomeManager().getCurrentGenome());
        } catch (IOException e) {
            log.error("Error creating GCT parser.", e);
            throw new DataLoadException("Error creating GCT parser: " + e, locator.getPath());
        }
        ds = parser.createDataset();
        ds.setName(fName);
        ds.setNormalized(true);
        ds.setLogValues(true);

        /*
         * File outputFile = new File(IGV.DEFAULT_USER_DIRECTORY, file.getName() + ".h5");
         * OverlappingProcessor proc = new OverlappingProcessor(ds);
         * proc.setZoomMax(0);
         * proc.process(outputFile.getAbsolutePath());
         * loadH5File(outputFile, messages, attributeList, group);
         */

        // Counter for generating ID
        TrackProperties trackProperties = ds.getTrackProperties();
        String path = locator.getPath();
        for (String trackName : ds.getTrackNames()) {
            Genome currentGenome = igv.getGenomeManager().getCurrentGenome();
            DatasetDataSource dataSource = new DatasetDataSource(trackName, ds, currentGenome);
            String trackId = path + "_" + trackName;
            Track track = new DataSourceTrack(locator, trackId, trackName, dataSource, genome);
            track.setRendererClass(HeatmapRenderer.class);
            track.setProperties(trackProperties);
            newTracks.add(track);
        }
    }

    private void loadIGVFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        if (locator.isLocal()) {
            if (!checkSize(locator.getPath())) {
                return;
            }
        }


        String dsName = locator.getTrackName();
        IGVDataset ds = new IGVDataset(locator, genome, igv);
        ds.setName(dsName);

        TrackProperties trackProperties = ds.getTrackProperties();
        String path = locator.getPath();
        TrackType type = ds.getType();
        for (String trackName : ds.getTrackNames()) {

            DatasetDataSource dataSource = new DatasetDataSource(trackName, ds, genome);
            String trackId = path + "_" + trackName;
            DataSourceTrack track = new DataSourceTrack(locator, trackId, trackName, dataSource, genome);

            // track.setRendererClass(HeatmapRenderer.class);
            track.setTrackType(ds.getType());
            track.setProperties(trackProperties);

            if (type == TrackType.ALLELE_FREQUENCY) {
                track.setRendererClass(ScatterplotRenderer.class);
                track.setHeight(40);
                track.setPreferredHeight(40);
            }
            newTracks.add(track);
        }
    }

    private boolean checkSize(String file) {

        if (!PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SHOW_SIZE_WARNING)) {
            return true;
        }

        File f = new File(file);
        String tmp = file;
        if (f.exists()) {
            long size = f.length();
            if (file.endsWith(".gz")) {
                size *= 3;
                tmp = file.substring(0, file.length() - 3);
            }

            if (size > 50000000) {
                String message = "";
                if (tmp.endsWith(".bed") || tmp.endsWith(".bed.txt")) {
                    message = "The file " + file + " is large (" + (size / 1000000) + " mb).  It is recommended " +
                            "that large files be indexed using IGVTools or Tabix. Loading un-indexed " +
                            "ascii fies of this size can lead to poor performance or unresponsiveness (freezing).  " +
                            "<br><br>IGVTools can be launched from the <b>Tools</b> menu or separately as a command line program.  " +
                            "See the user guide for more details.<br><br>Click <b>Continue</b> to continue loading, or <b>Cancel</b>" +
                            " to skip this file.";

                } else {

                    message = "The file " + file + " is large (" + (size / 1000000) + " mb).  It is recommended " +
                            "that large files be converted to the binary <i>.tdf</i> format using the IGVTools " +
                            "<b>tile</b> command. Loading  unconverted ascii fies of this size can lead to poor " +
                            "performance or unresponsiveness (freezing).  " +
                            "<br><br>IGVTools can be launched from the <b>Tools</b> menu or separately as a " +
                            "command line program. See the user guide for more details.<br><br>Click <b>Continue</b> " +
                            "to continue loading, or <b>Cancel</b> to skip this file.";
                }

                return ConfirmDialog.optionallyShowConfirmDialog(message, PreferenceManager.SHOW_SIZE_WARNING, true);

            }
        }
        return true;
    }

    private void loadDOTFile(ResourceLocator locator, List<Track> newTracks) {

        //GraphTrack gt = new GraphTrack(locator);
        //gt.setHeight(80);
        //newTracks.add(gt);

    }

    private void loadWigFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        if (locator.isLocal()) {
            if (!checkSize(locator.getPath())) {
                return;
            }
        }

        WiggleDataset ds = (new WiggleParser(locator, genome)).parse();
        TrackProperties props = ds.getTrackProperties();

        // In case of conflict between the resource locator display name and the track properties name,
        // use the resource locator
        String name = props == null ? null : props.getName();
        String label = locator.getName();
        if (name == null) {
            name = locator.getFileName();
        } else if (label != null) {
            props.setName(label);  // erase name rom track properties
        }

        String path = locator.getPath();
        boolean multiTrack = ds.getTrackNames().length > 1;

        for (String heading : ds.getTrackNames()) {

            String trackId = multiTrack ? path + "_" + heading : path;
            String trackName = multiTrack ? heading : name;


            DatasetDataSource dataSource = new DatasetDataSource(trackId, ds, genome);

            DataSourceTrack track = new DataSourceTrack(locator, trackId, trackName, dataSource, genome);

            String displayName = (label == null || multiTrack) ? heading : label;
            track.setName(displayName);
            track.setProperties(props);

            track.setTrackType(ds.getType());

            if (ds.getType() == TrackType.EXPR) {
                track.setWindowFunction(WindowFunction.none);
            }


            newTracks.add(track);
        }
    }

    private void loadTDFFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {


        if (log.isDebugEnabled()) {
            log.debug("Loading TDFFile: " + locator.toString());
        }

        TDFReader reader = TDFReader.getReader(locator.getPath());
        TrackType type = reader.getTrackType();

        if (log.isDebugEnabled()) {
            log.debug("Parsing track line ");
        }
        TrackProperties props = null;
        String trackLine = reader.getTrackLine();
        if (trackLine != null && trackLine.length() > 0) {
            props = new TrackProperties();
            ParsingUtils.parseTrackLine(trackLine, props);
        }

        // In case of conflict between the resource locator display name and the track properties name,
        // use the resource locator
        String name = locator.getName();
        if (name != null && props != null) {
            props.setName(name);
        }

        if (name == null) {
            name = props == null ? locator.getTrackName() : props.getName();
        }

        int trackNumber = 0;
        String path = locator.getPath();
        boolean multiTrack = reader.getTrackNames().length > 1;

        for (String heading : reader.getTrackNames()) {

            String trackId = multiTrack ? path + "_" + heading : path;
            String trackName = multiTrack ? heading : name;
            final DataSource dataSource = locator.getPath().endsWith(".counts") ?
                    new GobyCountArchiveDataSource(locator) :
                    new TDFDataSource(reader, trackNumber, heading, genome);
            DataSourceTrack track = new DataSourceTrack(locator, trackId, trackName,
                    dataSource, genome);

            String displayName = (name == null || multiTrack) ? heading : name;
            track.setName(displayName);
            track.setTrackType(type);
            if (props != null) {
                track.setProperties(props);
            }
            newTracks.add(track);
            trackNumber++;
        }
    }

    private void loadBWFile(ResourceLocator locator, List<Track> newTracks, Genome genome) throws IOException {

        String trackName = locator.getTrackName();
        String trackId = locator.getPath();

        String path = locator.getPath();
        SeekableStream ss = new SeekableBufferedStream(SeekableStreamFactory.getStreamFor(path), 64000);
        BBFileReader reader = new BBFileReader(path, ss);
        BigWigDataSource bigwigSource = new BigWigDataSource(reader, genome);

        if (reader.isBigWigFile()) {
            DataSourceTrack track = new DataSourceTrack(locator, trackId, trackName, bigwigSource, genome);
            newTracks.add(track);
        } else if (reader.isBigBedFile()) {
            FeatureTrack track = new FeatureTrack(locator, trackId, trackName, bigwigSource);
            newTracks.add(track);
        } else {
            throw new RuntimeException("Unknown BIGWIG type: " + locator.getPath());
        }
    }

    private void loadGobyCountsArchive(ResourceLocator locator, List<Track> newTracks, Genome genome) {


        if (log.isDebugEnabled()) {
            log.debug("Loading Goby counts archive: " + locator.toString());
        }


        String trackId = locator.getSampleId() + " coverage";
        String trackName = locator.getFileName();
        final DataSource dataSource = new GobyCountArchiveDataSource(locator);

        DataSourceTrack track = new DataSourceTrack(locator, trackId, trackName,
                dataSource, genome);

        newTracks.add(track);


    }

    private void loadEwigIBFFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        TDFReader reader = TDFReader.getReader(locator.getPath());
        TrackProperties props = null;
        String trackLine = reader.getTrackLine();
        if (trackLine != null && trackLine.length() > 0) {
            props = new TrackProperties();
            ParsingUtils.parseTrackLine(trackLine, props);
        }

        EWigTrack track = new EWigTrack(locator, genome);
        if (props != null) {
            track.setProperties(props);
        }
        track.setName(locator.getTrackName());
        newTracks.add(track);
    }

    private void loadListFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {
        try {
            FeatureSource source = new FeatureDirSource(locator, genome);
            FeatureTrack track = new FeatureTrack(locator, source);
            track.setName(locator.getTrackName());
            track.setVisibilityWindow(0);
            newTracks.add(track);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private void loadGisticFile(ResourceLocator locator, List<Track> newTracks) {

        GisticTrack track = GisticFileParser.loadData(locator);
        track.setName(locator.getTrackName());
        newTracks.add(track);
    }

    /**
     * Load a rnai gene score file and create a datasource and track.
     * <p/>
     * // TODO -- change parser to use resource locator rather than path.
     *
     * @param locator
     * @param newTracks
     */
    private void loadRNAiGeneScoreFile(ResourceLocator locator,
                                       List<Track> newTracks, RNAIGeneScoreParser.Type type,
                                       Genome genome) {

        RNAIGeneScoreParser parser = new RNAIGeneScoreParser(locator.getPath(), type, genome);

        Collection<RNAIDataSource> dataSources = parser.parse();
        String path = locator.getPath();
        for (RNAIDataSource ds : dataSources) {
            String name = ds.getName();
            String trackId = path + "_" + name;
            DataSourceTrack track = new DataSourceTrack(locator, trackId, name, ds, genome);

            // Set attributes.  This "hack" is neccessary to register these attributes with the
            // attribute manager to get displayed.
            track.setAttributeValue("SCREEN", ds.getScreen());
            if ((ds.getCondition() != null) && (ds.getCondition().length() > 0)) {
                track.setAttributeValue("CONDITION", ds.getCondition());
            }
            track.setHeight(80);
            track.setPreferredHeight(80);
            //track.setDataRange(new DataRange(-3, 0, 3));
            newTracks.add(track);
        }

    }

    /**
     * Load a RNAi haripin score file.  The results of this action are hairpin scores
     * added to the RNAIDataManager.  Currently no tracks are created for hairpin
     * scores, although this could change.
     *
     * @param locator
     */
    private void loadRNAiHPScoreFile(ResourceLocator locator) {
        (new RNAIHairpinParser(locator.getPath())).parse();
    }

    private void loadMAFTrack(ResourceLocator locator, List<Track> newTracks) {
        MAFTrack t = new MAFTrack(locator);
        t.setName("Multiple Alignments");
        newTracks.add(t);
    }

    private void loadPeakTrack(ResourceLocator locator, List<Track> newTracks, Genome genome) throws IOException {
        PeakTrack t = new PeakTrack(locator, genome);
        newTracks.add(t);
    }

    private void loadOmegaTrack(ResourceLocator locator, List<Track> newTracks, Genome genome) {
        OmegaDataSource ds = new OmegaDataSource(genome);
        OmegaTrack track = new OmegaTrack(locator, ds, genome);
        track.setName("Conservation (Omega)");
        track.setHeight(40);
        track.setPreferredHeight(40);
        newTracks.add(track);
    }

    /**
     * Load a rnai gene score file and create a datasource and track.
     *
     * @param locator
     * @param newTracks
     */
    private void loadAlignmentsTrack(ResourceLocator locator, List<Track> newTracks, Genome genome) throws IOException {

        try {
            String dsName = locator.getTrackName();
            String fn = locator.getPath().toLowerCase();
            boolean isBed = fn.endsWith(".bedz") || fn.endsWith(".bed") || fn.endsWith(".bed.gz");

            // If the user tried to load the index,  look for the file (this is a common mistake)
            if (locator.getPath().endsWith(".sai") || locator.getPath().endsWith(".bai")) {
                MessageUtils.showMessage("<html><b>ERROR:</b> Loading SAM/BAM index files are not supported:  " + locator.getPath() +
                        "<br>Load the SAM or BAM file directly. ");
                return;
            }
            AlignmentDataManager dataManager = new AlignmentDataManager(locator);

            if (locator.getPath().toLowerCase().endsWith(".bam")) {
                if (!dataManager.hasIndex()) {
                    MessageUtils.showMessage("<html>Could not load index file for: " +
                            locator.getPath() + "<br>  An index file is required for SAM & BAM files.");
                    return;
                }
            }

            AlignmentTrack alignmentTrack = new AlignmentTrack(locator, dataManager, genome);    // parser.loadTrack(locator, dsName);
            alignmentTrack.setName(dsName);
            if (isBed) {
                alignmentTrack.setRenderer(new BedRenderer());
                alignmentTrack.setPreferredHeight(40);
                alignmentTrack.setHeight(40);
            }

            // Create coverage track
            CoverageTrack covTrack = new CoverageTrack(locator, alignmentTrack.getName() + " Coverage", genome);
            covTrack.setVisible(PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SAM_SHOW_COV_TRACK));
            newTracks.add(covTrack);
            alignmentTrack.setCoverageTrack(covTrack);
            if (!isBed) {
                covTrack.setDataManager(dataManager);
                dataManager.setCoverageTrack(covTrack);
            }

            // Search for precalculated coverage data
            String covPath = locator.getCoverage();
            if (covPath == null) {
                String path = locator.getPath();
                covPath = path + ".tdf";
            }
            if (covPath != null) {
                try {
                    if ((new File(covPath)).exists() || (HttpUtils.getInstance().isURL(covPath) &&
                            HttpUtils.getInstance().resourceAvailable(new URL(covPath)))) {
                        TDFReader reader = TDFReader.getReader(covPath);
                        TDFDataSource ds = new TDFDataSource(reader, 0, alignmentTrack.getName() + " coverage", genome);
                        covTrack.setDataSource(ds);
                    }
                } catch (MalformedURLException e) {
                    // This is expected if
                    //    log.info("Could not loading coverage data: MalformedURL: " + covPath);
                }
            }

            boolean showSpliceJunctionTrack = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SAM_SHOW_JUNCTION_TRACK);
            if (showSpliceJunctionTrack) {
                SpliceJunctionFinderTrack spliceJunctionTrack = new SpliceJunctionFinderTrack(locator.getPath() + "_junctions",
                        alignmentTrack.getName() + " Junctions", dataManager, genome);
//            spliceJunctionTrack.setDataManager(dataManager);
                spliceJunctionTrack.setHeight(60);
                spliceJunctionTrack.setPreferredHeight(60);
                spliceJunctionTrack.setVisible(showSpliceJunctionTrack);
                newTracks.add(spliceJunctionTrack);
                alignmentTrack.setSpliceJunctionTrack(spliceJunctionTrack);
            }

            newTracks.add(alignmentTrack);

        } catch (IndexNotFoundException e) {
            MessageUtils.showMessage("<html>Could not find the index file for  <br><br>&nbsp;&nbsp;" + e.getSamFile() +
                    "<br><br>Note: The index file can be created using igvtools and must be in the same directory as the .sam file.");
        }
    }


    /**
     * Load a ".mut" file (muation file) and create tracks.
     *
     * @param locator
     * @param newTracks
     */
    private void loadMutFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        MutationParser parser = new MutationParser();
        List<FeatureTrack> mutationTracks = parser.loadMutationTracks(locator, genome);
        for (FeatureTrack track : mutationTracks) {
            track.setTrackType(TrackType.MUTATION);
            track.setRendererClass(MutationRenderer.class);
            newTracks.add(track);
        }
    }

    private void loadSegFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        // TODO - -handle remote resource
        SegmentedAsciiDataSet ds = new SegmentedAsciiDataSet(locator, genome);
        String path = locator.getPath();
        TrackProperties props = ds.getTrackProperties();

        // The "freq" track.  TODO - make this optional
        if (ds.getSampleNames().size() > 1) {
            FreqData fd = new FreqData(ds, genome);
            String freqTrackId = path;
            String freqTrackName = (new File(path)).getName();
            CNFreqTrack freqTrack = new CNFreqTrack(locator, freqTrackId, freqTrackName, fd);
            newTracks.add(freqTrack);
        }

        for (String trackName : ds.getDataHeadings()) {
            String trackId = path + "_" + trackName;
            SegmentedDataSource dataSource = new SegmentedDataSource(trackName, ds);
            DataSourceTrack track = new DataSourceTrack(locator, trackId, trackName, dataSource, genome);
            track.setRendererClass(HeatmapRenderer.class);
            track.setTrackType(ds.getType());

            if (props != null) {
                track.setProperties(props);
            }

            newTracks.add(track);
        }
    }


    private void loadFromDatabase(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        if (".seg".equals(locator.getType())) {

            SegmentedAsciiDataSet ds = (new SegmentedSQLReader()).load(locator, genome);

            String path = locator.getPath();
            TrackProperties props = ds.getTrackProperties();

            // The "freq" track.  TODO - make this optional
            if (ds.getSampleNames().size() > 1) {
                FreqData fd = new FreqData(ds, genome);
                String freqTrackId = path;
                String freqTrackName = (new File(path)).getName();
                CNFreqTrack freqTrack = new CNFreqTrack(locator, freqTrackId, freqTrackName, fd);
                newTracks.add(freqTrack);
            }

            for (String trackName : ds.getDataHeadings()) {
                String trackId = path + "_" + trackName;
                SegmentedDataSource dataSource = new SegmentedDataSource(trackName, ds);
                DataSourceTrack track = new DataSourceTrack(locator, trackId, trackName, dataSource, genome);
                track.setRendererClass(HeatmapRenderer.class);
                track.setTrackType(ds.getType());

                if (props != null) {
                    track.setProperties(props);
                }

                newTracks.add(track);
            }
        } else {
            (new SampleInfoSQLReader()).load(locator);
        }
    }


    private void loadBinarySegFile(ResourceLocator locator, List<Track> newTracks, Genome genome) {

        SegmentedBinaryDataSet ds = new SegmentedBinaryDataSet(locator);
        String path = locator.getPath();

        // The "freq" track.  Make this optional?
        FreqData fd = new FreqData(ds, genome);
        String freqTrackId = path;
        String freqTrackName = (new File(path)).getName();
        CNFreqTrack freqTrack = new CNFreqTrack(locator, freqTrackId, freqTrackName, fd);
        newTracks.add(freqTrack);


        for (String trackName : ds.getSampleNames()) {
            String trackId = path + "_" + trackName;
            SegmentedDataSource dataSource = new SegmentedDataSource(trackName, ds);
            DataSourceTrack track = new DataSourceTrack(locator, trackId, trackName, dataSource, genome);
            track.setRendererClass(HeatmapRenderer.class);
            track.setTrackType(ds.getType());
            newTracks.add(track);
        }
    }


    private void loadDASResource(ResourceLocator locator, List<Track> currentTracks) {

        //TODO Connect and get all the attributes of the DAS server, and run the appropriate load statements
        //TODO Currently we are only going to be doing features
        // TODO -- move the source creation to a factory


        DASFeatureSource featureSource = null;
        try {
            featureSource = new DASFeatureSource(locator);
        } catch (MalformedURLException e) {
            log.error("Malformed URL", e);
            throw new DataLoadException("Error: Malformed URL ", locator.getPath());
        }

        FeatureTrack track = new FeatureTrack(locator, featureSource);

        // Try to create a sensible name from the path
        String name = locator.getPath();
        if (locator.getPath().contains("genome.ucsc.edu")) {
            name = featureSource.getType();
        } else {
            name = featureSource.getPath().replace("/das/", "").replace("/features", "");
        }
        track.setName(name);

        // A hack until we can notate this some other way
        if (locator.getPath().contains("cosmic")) {
            track.setRendererClass(CosmicFeatureRenderer.class);
            track.setMinimumHeight(2);
            track.setHeight(20);
            track.setPreferredHeight(20);
            track.setDisplayMode(Track.DisplayMode.EXPANDED);
        } else {
            track.setRendererClass(IGVFeatureRenderer.class);
            track.setMinimumHeight(35);
            track.setHeight(45);
            track.setPreferredHeight(45);
        }
        currentTracks.add(track);
    }


    private void loadTrioData(ResourceLocator locator) throws IOException {
        PedigreeUtils.parseTrioFile(locator.getPath());
    }


    public static boolean isIndexed(String path) {

        // Checking for the index is expensive over HTTP.  First see if this is an indexable format by fetching the codec
        if (!isIndexable(path)) {
            return false;
        }

        // genome space files are never indexed (at least not yet)
        if (path.contains("genomespace.org")) {
            return false;
        }

        String indexPath;
        String indexExtension;
        if ( !ApiRequest.isNiu() || !path.startsWith("http") )
        {
        	indexExtension = path.endsWith("gz") ? ".tbi" : ".idx";
        	indexPath = path + indexExtension;
        }
        else
        {
        	String		cleanPath = ApiRequest.extractCleanPath(path);
        	indexExtension = cleanPath.endsWith("gz") ? ".tbi" : ".idx";
        	indexPath = ApiRequest.buildIndexPath(path, indexExtension);
        }
        
        try {
            if (HttpUtils.getInstance().isURL(path)) {
                boolean		indexResourceAvailable = HttpUtils.getInstance().resourceAvailable(new URL(indexPath));
                if ( !ApiRequest.isNiu() )
                	return indexResourceAvailable;
                else if ( indexResourceAvailable )
                	return true;
                else if ( ApiRequest.isApiUrl(path) && ApiRequest.extractCleanPath(path).endsWith(".vcf") )
                	return true;
                else
                	return false;
                	
                
            } else {
                File f = new File(path + indexExtension);
                return f.exists();
            }

        } catch (IOException e) {
            return false;
        }

    }


    /**
     * Return true if a file represented by "path" is indexable.  This method is an optimization, we could just look
     * for the index but that is expensive to do for remote resources.  All tribble indexable extensions should be
     * listed here.
     *
     * @param path
     * @return
     */
    private static boolean isIndexable(String path) {
    	
    	// new-indexed-url (niu) - ignore url parameters
    	if ( ApiRequest.isNiu() )
    		path = ApiRequest.extractCleanPath(path);
    	
        String fn = path.toLowerCase();
        if (fn.endsWith(".gz")) {
            int l = fn.length() - 3;
            fn = fn.substring(0, l);
        }
        return fn.endsWith(".vcf4") || fn.endsWith(".vcf") || fn.endsWith(".bed") || fn.endsWith(".repmask") ||
                fn.endsWith(".gff3") || fn.endsWith(".gff") || fn.endsWith(".psl") || fn.endsWith(".pslx") ||
                fn.endsWith(".gvf") || fn.endsWith(".gtf");
    }
}
