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

package org.broad.igv.batch;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.broad.igv.feature.Locus;
import org.broad.igv.feature.RegionOfInterest;
import org.broad.igv.feature.genome.mapping.GenomeLocus;
import org.broad.igv.feature.genome.mapping.GenomeMapperException;
import org.broad.igv.feature.genome.mapping.GenomeMappingManager;
import org.broad.igv.feature.genome.mapping.IGenomeMapper;
import org.broad.igv.sam.AlignmentTrack;
import org.broad.igv.track.RegionScoreType;
import org.broad.igv.track.TrackManager;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.Main;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.ui.util.SnapshotUtilities;
import org.broad.igv.util.LRUCache;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.RuntimeUtils;
import org.broad.igv.util.StringUtils;

public class CommandExecutor {

    private static Logger log = Logger.getLogger(CommandExecutor.class);

    private File snapshotDirectory;
    private IGV mainFrame;


    public CommandExecutor() {
        mainFrame = IGV.getFirstInstance();
    }

    private List<String> getArgs(String[] tokens) {
        List<String> args = new ArrayList(tokens.length);
        for (String s : tokens) {
            if (s.trim().length() > 0) {
                args.add(s.trim());
            }
        }
        return args;
    }

    public String execute(String command) {

        List<String> args = getArgs(StringUtils.breakQuotedString(command, ' ').toArray(new String[]{}));

        String result = "OK";


        System.out.println();
        log.debug("Executing: " + command);
        try {
            if (args.size() > 0) {

                String cmd = args.get(0).toLowerCase();
                String param1 = args.size() > 1 ? args.get(1) : null;
                String param2 = args.size() > 2 ? args.get(2) : null;
                String param3 = args.size() > 3 ? args.get(3) : null;
                String param4 = args.size() > 4 ? args.get(4) : null;

                if (cmd.equals("echo")) {
                    result = cmd;
                } else if (cmd.equals("goto")) {
                    result = goto1(args);
                } else if (cmd.equals("snapshotdirectory")) {
                    result = setSnapshotDirectory(param1);

                } else if (cmd.equals("snapshot")) {
                    String filename = param1;
                    createSnapshot(filename);

                } else if ((cmd.equals("loadfile") || cmd.equals("load")) && param1 != null) {
                    result = load(param1, param2, param3);
                } else if (cmd.equals("genome") && args.size() > 1) {
                    result = genome(param1);
                } else if (cmd.equals("new") || cmd.equals("reset") || cmd.equals("clear")) {
                    mainFrame.createNewSession(null);
                } else if (cmd.equals("region")) {
                    defineRegion(param1, param2, param3);
                } else if (cmd.equals("sort")) {
                    sort(param1, param2, param3);
                } else if (cmd.equals("collapse")) {
                    String trackName = param1 == null ? null : param1.replace("\"", "").replace("'", "");
                    collapse(trackName);
                } else if (cmd.equals("expand")) {
                    String trackName = param1 == null ? null : param1.replace("\"", "").replace("'", "");
                    expand(trackName);
                } else if (cmd.equals("tweakdivider")) {
                    IGV.getFirstInstance().tweakPanelDivider();
                } else if (cmd.equals("maxpanelheight") && param1 != null) {
                    return setMaxPanelHeight(param1);
                } else if (cmd.equals("exit")) {
                    System.exit(0);
                } else if (cmd.equals("tofront")) {
                    return bringToFront();
                } else if ( cmd.equals("map") )  {                	
                	result = map(param1, param2, param3);
                } else if ( cmd.equals("minfo") )  {                	
                	result = mappingInfo();
                } else {
                	log.error("UNKOWN COMMAND: " + command);
                	return "UNKOWN COMMAND: " + command;
                }
            } else {
                return "Empty command string";
            }
            IGV.getFirstInstance().doRefresh();

            if (RuntimeUtils.getAvailableMemoryFraction() < 0.5) {
                log.debug("Clearing caches");
                LRUCache.clearCaches();
            }
            log.debug("Finished execution: " + command + "  sleeping ....");
            Thread.sleep(2000);
            log.debug("Finished sleeping");

        } catch (Exception e) {
            log.error("Could not Parse Command", e);
            return "ERROR Could not Parse Command: " + e.toString();
        }
        log.info(result);

        return result;
    }

    private String mappingInfo() 
    {
    	return IGV.getInstance().getGenomeManager().getGenomeMappingManager().getMappingInfo();
	}

	private String map(String fromGenome, String toGenome, String fromLocus) 
    {
    	IGenomeMapper		mapper = IGV.getInstance().getGenomeManager().getGenomeMappingManager().getMapper(fromGenome, toGenome);
    	
    	if ( mapper == null )
    		return "no mapper found";
    	else
    	{
    		try
    		{
    			GenomeLocus		toLocus = mapper.mapLocus(new GenomeLocus(fromLocus));
    			
    			return toLocus.toString();
    			
    		} catch (GenomeMapperException e) {
    			
    			return "genome mapping exception: " + e.getMessage();
    		}
    	}
    }

	private String bringToFront() {
        // Trick to force window to front, the setAlwaysOnTop works on a Mac,  toFront() does nothing.
        Frame mainFrame = IGV.getMainFrame();
        mainFrame.toFront();
        mainFrame.setAlwaysOnTop(true);
        mainFrame.setAlwaysOnTop(false);
        return "OK";
    }


    private String setMaxPanelHeight(String param1) {
        try {
            Integer h = Integer.parseInt(param1.trim());
            SnapshotUtilities.MAX_PANEL_HEIGHT = h;
            return "OK";
        } catch (NumberFormatException e) {
            return "ERROR - max panel height value ('" + param1 + ".) must be a number";
        }
    }

    private String genome(String param1) {
        if (param1 == null) {
            return "ERROR missing genome parameter";
        }
        String result;
        String genomeID = param1;
        IGV.getFirstInstance().selectGenomeFromList(genomeID);
        result = "OK";
        return result;
    }


    /**
     * Load from port or batch command
     *
     * @param param1 filename
     * @param param2 name
     * @param param3 merge
     * @return
     * @throws IOException
     */
    private String load(String param1, String param2, String param3) throws IOException {
        if (param1 == null) {
            return "ERROR: missing path parameter";
        }
        String fileString = param1.replace("\"", "").replace("'", "");

        // Default for merge is "true" for session files,  "false" otherwise
        String file = fileString;
        boolean merge;
        if ( Main.isSessionFile(file) ) {
            // Session file
            merge = false;
        } else {
            // Data file
            merge = true;
        }

        // remaining parameters might be "merge" or "name"
        String name = null;
        for (String param : Arrays.asList(param2, param3)) {
            if (param != null && param.startsWith("name=")) {
                name = param.substring(5);
            } else if (param != null && param.startsWith("merge=")) {
                String mergeString = param.substring(6);
                merge = mergeString.equalsIgnoreCase("true");
            }
        }
        // Locus is not specified from port commands
        String locus = null;
        return loadFiles(fileString, locus, merge, name);
    }

    String loadFiles(final String fileString, final String locus, final boolean merge, String name) throws IOException {

        log.debug("Run load files");

        String[] files = fileString.split(",");
        List<ResourceLocator> fileLocators = new ArrayList<ResourceLocator>();
        List<String> sessionPaths = new ArrayList<String>();

        if (!merge) {
            // If this is a session file unload without asking, otherwise ask
            boolean unload = !merge;
            if ( Main.isSessionFile(fileString) ) {
                unload = !merge;
            } else {
                unload = MessageUtils.confirm("Unload current session before loading new tracks?");
            }
            if (unload) {
                mainFrame.createNewSession(null);
            }
        }

        // Create set of loaded files
        Set<String> loadedFiles = new HashSet<String>();
        for (ResourceLocator rl : IGV.getFirstInstance().getTrackManager().getDataResourceLocators()) {
            loadedFiles.add(rl.getPath());
        }

        // Loop through files
        for (String f : files) {
            // Skip already loaded files TODO -- make this optional?  Check for change?
            if (loadedFiles.contains(f)) continue;

            if ( Main.isSessionFile(f) ) {
                sessionPaths.add(f);
            } else {
                ResourceLocator rl = new ResourceLocator(f);
                if (rl.isLocal()) {
                    File file = new File(f);
                    if (!file.exists()) {
                        return "Error: " + f + " does not exist.";
                    }
                }
                if (name != null) {
                    rl.setName(name);
                }

                fileLocators.add(rl);
            }
        }

        for (String sessionPath : sessionPaths) {
            IGV.getFirstInstance().doRestoreSession(sessionPath, locus, merge);
        }

        IGV.getFirstInstance().loadTracks(fileLocators);

        if (locus != null && !locus.equals("null")) {
            IGV.getFirstInstance().goToLocus(locus);
        }

        return "OK";
    }


    private String setSnapshotDirectory(String param1) {
        if (param1 == null) {
            return "ERROR: missing directory parameter";
        }

        String result;
        File parentDir = new File(param1);
        if (parentDir.exists()) {
            snapshotDirectory = parentDir;
            result = "OK";
        } else {
            parentDir.mkdir();
            if (parentDir.exists()) {
                snapshotDirectory = parentDir;
                result = "OK";
            } else {

                result = "ERROR: directory: " + param1 + " does not exist";
            }
        }
        return result;
    }

    private String goto1(List<String> args) {
        if (args == null || args.size() < 2) {
            return "ERROR: missing locus parameter";
        }
        String locus = args.get(1);
        for (int i = 2; i < args.size(); i++) {
            locus += (" " + args.get(i));
        }
        IGV.getFirstInstance().goToLocus(locus);
        return "OK";
    }

    private void collapse(String trackName) {
        if (trackName == null) {
            IGV.getFirstInstance().getTrackManager().collapseTracks();
        } else {
            IGV.getFirstInstance().getTrackManager().collapseTrack(trackName);
        }
        IGV.getFirstInstance().repaintDataPanels();
    }


    private void expand(String trackName) {
        if (trackName == null) {
            IGV.getFirstInstance().getTrackManager().expandTracks();
        } else {
            IGV.getFirstInstance().getTrackManager().expandTrack(trackName);
        }
        IGV.getFirstInstance().repaintDataPanels();
    }

    private void defineRegion(String param1, String param2, String param3) {

        RegionOfInterest roi = null;
        if (param1 != null && param2 != null && param3 != null) {
            int start = Math.max(0, Integer.parseInt(param2) - 1);
            int end = Integer.parseInt(param3);
            roi = new RegionOfInterest(param1, start, end, "");
        }
        if (param1 != null) {
            Locus locus = new Locus(param1);
            if (locus.isValid()) {
                int start = Math.max(0, locus.getStart() - 1);
                roi = new RegionOfInterest(locus.getChr(), start, locus.getEnd(), "");

            }
        }
        if (roi != null) {
            IGV.getFirstInstance().addRegionOfInterest(roi);
        }
    }


    private void sort(String sortArg, String locusString, String param3) {
        TrackManager tm = IGV.getFirstInstance().getTrackManager();
        RegionScoreType regionSortOption = getRegionSortOption(sortArg);
        if (regionSortOption != null) {
            RegionOfInterest roi = null;
            if (locusString != null) {
                Locus locus = new Locus(locusString);
                if (locus.isValid()) {
                    int start = Math.max(0, locus.getStart() - 1);
                    roi = new RegionOfInterest(locus.getChr(), start, locus.getEnd(), "");
                }
            }
            tm.sortByRegionScore(roi, regionSortOption, FrameManager.getDefaultFrame());

        } else {
            Double location = null;
            if (param3 != null && param3.trim().length() > 0) {
                try {
                    location = new Double(param3.replace(",", ""));
                } catch (NumberFormatException e) {
                    log.info("Unexpected sort location argument (expected number): " + param3);
                }
            } else if (locusString != null && locusString.trim().length() > 0) {
                try {
                    location = new Double(locusString.replace(",", ""));
                } catch (NumberFormatException e) {
                    log.info("Unexpected sort location argument (expected number): " + param3);
                }

            }
            if (location == null) {
                tm.sortAlignmentTracks(getAlignmentSortOption(sortArg));
            } else {
                tm.sortAlignmentTracks(getAlignmentSortOption(sortArg), location);
            }

        }
        IGV.getFirstInstance().repaintDataPanels();
    }


    private void createSnapshot(String filename) {
        IGV mainFrame = IGV.getFirstInstance();

        if (filename == null) {
            String locus = FrameManager.getDefaultFrame().getFormattedLocusString();
            filename = locus.replaceAll(":", "_").replace("-", "_") + ".png";
        }

        File file = snapshotDirectory == null ? new File(filename) : new File(snapshotDirectory, filename);
        System.out.println("Snapshot: " + file.getAbsolutePath());

        SnapshotUtilities.doSnapshotOffscreen(mainFrame.getMainPanel(), file);
    }

    private static RegionScoreType getRegionSortOption(String str) {
        if (str == null) return null;
        String option = str.toUpperCase();
        try {
            return RegionScoreType.valueOf(option);
        } catch (Exception e) {
            return null;
        }
    }


    //START, STRAND, NUCLEOTIDE, QUALITY, SAMPLE, READ_GROUP
    private static AlignmentTrack.SortOption getAlignmentSortOption(String str) {
        String option = str.toLowerCase();
        if (str == null || option.equals("base")) {
            return AlignmentTrack.SortOption.NUCELOTIDE;
        } else if (option.equals("strand")) {
            return AlignmentTrack.SortOption.STRAND;

        } else if (option.equals("start") || option.equals("position")) {
            return AlignmentTrack.SortOption.START;

        } else if (option.equals("quality")) {
            return AlignmentTrack.SortOption.QUALITY;

        } else if (option.equals("sample")) {
            return AlignmentTrack.SortOption.SAMPLE;

        } else if (option.equals("readGroup") || option.equals("read_group")) {
            return AlignmentTrack.SortOption.READ_GROUP;
        } else if (option.equals("insertSize") || option.equals("insert_size")) {
            return AlignmentTrack.SortOption.INSERT_SIZE;
        }
        return AlignmentTrack.SortOption.NUCELOTIDE;
    }
    
}
