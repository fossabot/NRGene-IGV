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
package org.broad.igv.util;

//~--- non-JDK imports --------------------------------------------------------

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.renderer.BarChartRenderer;
import org.broad.igv.renderer.GenotypeRenderer;
import org.broad.igv.renderer.HeatmapRenderer;
import org.broad.igv.renderer.LineplotRenderer;
import org.broad.igv.renderer.ScatterplotRenderer;
import org.broad.igv.renderer.SpliceJunctionRenderer;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.tribble.readers.AsciiLineReader;

/**
 * @author jrobinso
 */
public class ParsingUtils {

    private static Logger log = Logger.getLogger(ParsingUtils.class);


    public static BufferedReader openBufferedReader(String pathOrUrl) throws IOException {

        BufferedReader reader;

        if (HttpUtils.getInstance().isURL(pathOrUrl)) {
            URL url = new URL(pathOrUrl);
            reader = new BufferedReader(new InputStreamReader(HttpUtils.getInstance().openConnectionStream(url)));
        } else {
            File file = new File(pathOrUrl);

            FileInputStream fileInput = new FileInputStream(file);
            if (file.getName().endsWith("gz")) {
                GZIPInputStream in = new GZIPInputStream(fileInput);
                reader = new BufferedReader(new InputStreamReader(in));
            } else {
                reader = new BufferedReader(new InputStreamReader(fileInput));
            }
        }

        return reader;
    }


    public static int estimateLineCount(File file) {
        if (file.isDirectory()) {
            int lineCount = 0;
            for (File f : file.listFiles()) {
                // Don't recurse
                if (!f.isDirectory()) {
                    lineCount += estimateLineCount(f.getAbsolutePath());
                }
            }
            return lineCount;

        } else {
            return estimateLineCount(file.getAbsolutePath());
        }

    }

    public static long getContentLength(String path) {
        try {
            long contentLength = -1;
            if (path.startsWith("http:") || path.startsWith("https:")) {
                URL url = new URL(path);
                contentLength = HttpUtils.getInstance().getContentLength(url);

            } else if (path.startsWith("ftp:")) {
                // Use JDK url
                URL url = new URL(path);
                contentLength = url.openConnection().getContentLength();
            } else {
                contentLength = (new File(path)).length();
            }
            return contentLength;
        } catch (IOException e) {
            log.error("Error getting content length for: " + path, e);
            return -1;
        }
    }

    public static int estimateLineCount(String path) {

        AsciiLineReader reader = null;
        try {
            final int defaultLength = 100000;
            long fileLength = getContentLength(path);
            if (fileLength <= 0) {
                return defaultLength;
            }

            reader = openAsciiReader(new ResourceLocator(path));
            String nextLine;
            int lines = 0;
            // Skip the first 10 lines (headers, etc)
            int nSkip = 10;
            while (nSkip-- > 0 && reader.readLine() != null) {
            }
            long startPos = reader.getPosition();

            while ((nextLine = reader.readLine()) != null & lines < 100) {
                lines++;
            }

            if (lines == 0) {
                return defaultLength;
            }

            double bytesPerLine = (double) ((reader.getPosition() - startPos) / lines);
            int nLines = (int) (fileLength / bytesPerLine);
            return nLines;

        } catch (Exception e) {
            log.error("Error estimating line count", e);
            return 1000;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // Ignore errors closing reader
            }
        }

    }

    public static AsciiLineReader openAsciiReader(ResourceLocator locator) throws IOException {
        InputStream stream = openInputStream(locator);
        return new AsciiLineReader(stream, locator.getPath());

    }


    public static InputStream openInputStream(ResourceLocator locator) throws IOException {

        if (locator.getServerURL() != null) {
            URL url = new URL(locator.getServerURL() + "?method=getContents&file=" + locator.getPath());
            InputStream is = HttpUtils.getInstance().openConnectionStream(url);

            // Note -- assumption that url stream is compressed!
            try {
                return new GZIPInputStream(is);
            } catch (Exception ex) {
                log.error("Error with gzip stream", ex);
                throw new RuntimeException(
                        "There was a server error loading file: " + locator.getTrackName() +
                                ". Please report to " + PreferenceManager.SUPPORT_EMAIL);

            }

        } else {

            InputStream inputStream = null;
            if (HttpUtils.isURL(locator.getPath())) {
                URL url = new URL(locator.getPath());
                inputStream = HttpUtils.getInstance().openConnectionStream(url, locator); // xxx
            } else {
                String path = locator.getPath();
                if (path.startsWith("file://")) {
                    path = path.substring(7);
                }
                File file = new File(path);
                inputStream = new FileInputStream(file);
            }

            if (locator.getPath().endsWith("gz")) {
                return new GZIPInputStream(inputStream);
            } else {
                return inputStream;
            }
        }
    }

    /**
     * Split the string into tokesn separated by the given delimiter.  Profiling has
     * revealed that the standard string.split() method typically takes > 1/2
     * the total time when used for parsing ascii files.
     *
     * @param aString the string to split
     * @param tokens  an array to hold the parsed tokens
     * @param delim   character that delimits tokens
     * @return the number of tokens parsed
     */
    public static int split(String aString, String[] tokens, char delim) {

        int maxTokens = tokens.length;
        int nTokens = 0;
        int start = 0;
        int end = aString.indexOf(delim);
        if (end == 0) {
            if (aString.length() > 1) {
                start = 1;
                end = aString.indexOf(delim, start);
            } else {
                return 0;
            }
        }
        if (end < 0) {
            tokens[nTokens++] = aString;
            return nTokens;
        }
        while ((end > 0) && (nTokens < maxTokens)) {
            //tokens[nTokens++] = new String(aString.toCharArray(), start, end-start); //  aString.substring(start, end);
            tokens[nTokens++] = aString.substring(start, end);
            start = end + 1;
            end = aString.indexOf(delim, start);

        }

        // Add the trailing string 
        if (nTokens < maxTokens) {
            String trailingString = aString.substring(start);
            tokens[nTokens++] = trailingString;
        }
        return nTokens;
    }

    /**
     * Split the string into tokens separated by one or more space.  This method
     * was added so support PLINK files.
     *
     * @param aString the string to split
     * @param tokens  an array to hold the parsed tokens
     * @return the number of tokens parsed
     */
    public static int splitSpaces(String aString, String[] tokens) {

        aString = aString.trim();
        int maxTokens = tokens.length;
        int nTokens = 0;
        int start = 0;
        int end = aString.indexOf(' ');
        if (end < 0) {
            tokens[nTokens++] = aString;
            return nTokens;
        }
        while ((end > 0) && (nTokens < maxTokens)) {

            String t = aString.substring(start, end);
            if (t.length() > 0) {
                tokens[nTokens++] = t;
            }
            start = end + 1;

            end = aString.indexOf(' ', start);

        }

        // Add the trailing string,  if there is room and if it is not empty.
        if (nTokens < maxTokens) {
            String trailingString = aString.substring(start);
            if (trailingString.length() > 0) {
                tokens[nTokens++] = trailingString;
            }
        }
        return nTokens;
    }


    /**
     * Split the string into tokesn separated by tab or space(s).  This method
     * was added so support wig and bed files, which apparently accept
     * either.
     *
     * @param aString the string to split
     * @param tokens  an array to hold the parsed tokens
     * @return the number of tokens parsed
     */
    public static int splitWhitespace(String aString, String[] tokens) {

        int maxTokens = tokens.length;
        int nTokens = 0;
        int start = 0;
        int tabEnd = aString.indexOf('\t');
        int spaceEnd = aString.indexOf(' ');
        int end = tabEnd < 0 ? spaceEnd : spaceEnd < 0 ? tabEnd : Math.min(spaceEnd, tabEnd);
        while ((end > 0) && (nTokens < maxTokens)) {
            //tokens[nTokens++] = new String(aString.toCharArray(), start, end-start); //  aString.substring(start, end);
            tokens[nTokens++] = aString.substring(start, end);

            start = end + 1;
            // Gobble up any whitespace before next token -- don't gobble tabs, consecutive tabs => empty cell
            while (start < aString.length() && aString.charAt(start) == ' ') {
                start++;
            }

            tabEnd = aString.indexOf('\t', start);
            spaceEnd = aString.indexOf(' ', start);
            end = tabEnd < 0 ? spaceEnd : spaceEnd < 0 ? tabEnd : Math.min(spaceEnd, tabEnd);

        }

        // Add the trailing string
        if (nTokens < maxTokens) {
            String trailingString = aString.substring(start).trim();
            tokens[nTokens++] = trailingString;
        }
        return nTokens;
    }

    /**
     * Method description
     *
     * @param str
     * @param ifile
     * @param ofile
     * @throws IOException
     */
    public static void replaceString(String str, String rplString, File ifile, File ofile) throws IOException {

        PrintWriter pw = null;
        BufferedReader br = null;

        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(ofile)));
            br = new BufferedReader(new FileReader(ifile));
            String nextLine = null;
            while ((nextLine = br.readLine()) != null) {
                if (!nextLine.startsWith("##")) {
                    pw.println(nextLine.replace(str, rplString));
                }
            }
        } finally {
            pw.close();
            br.close();
        }
    }

    /**
     * Method description
     *
     * @param str
     * @param ifile
     * @param ofile
     * @throws IOException
     */
    public static void dropLinesContaining(String str, File ifile, File ofile) throws IOException {

        PrintWriter pw = null;
        BufferedReader br = null;

        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(ofile)));
            br = new BufferedReader(new FileReader(ifile));
            String nextLine = null;
            while ((nextLine = br.readLine()) != null) {
                if (!nextLine.contains(str)) {
                    pw.println(nextLine);
                }
            }
        } finally {
            pw.close();
            br.close();
        }
    }

    /**
     * Method description
     *
     * @param file
     * @return
     */
    public static List<String> loadRegions(File file) {
        try {
            FileInputStream fileInput = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInput));
            String nextLine;
            List<String> features = new ArrayList<String>();
            while ((nextLine = reader.readLine()) != null && (nextLine.trim().length() > 0)) {
                try {
                    if (nextLine.startsWith("chr")) {
                        String[] tokens = nextLine.split("\t");
                        String region = tokens[0] + ":" + tokens[1] + "-" + tokens[2];
                        features.add(region);
                    }
                } catch (NumberFormatException e) {
                    log.error("Error parsing numer in line: " + nextLine);
                }
            }

            reader.close();
            return features;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * graphType         bar|points           # default is bar
     * yLineMark         real-value           # default is 0.0
     * yLineOnOff        on|off               # default is off
     * windowingFunction maximum|mean|minimum # default is maximum
     * smoothingWindow   off|[2-16]           # default is off
     *
     * @param nextLine
     * @param trackProperties
     * @throws NumberFormatException
     */
    public static boolean parseTrackLine(String nextLine, TrackProperties trackProperties)
            throws NumberFormatException {

        boolean foundProperties = false;
        try {
            // track type=wiggle_0 name="CSF +" description="CSF +" visibility=full autoScale=off viewLimits=-50:50
            List<String> tokens = StringUtils.breakQuotedString(nextLine, ' ');
            for (String pair : tokens) {
                List<String> kv = StringUtils.breakQuotedString(pair, '=');
                if (kv.size() == 2) {
                    foundProperties = true;
                    String key = kv.get(0).toLowerCase().trim();
                    String value = kv.get(1).replaceAll("\"", "");

                    if (key.equals("coords")) {
                        if (value.equals("0")) {
                            trackProperties.setBaseCoord(TrackProperties.BaseCoord.ZERO);
                        } else if (value.equals("1")) {
                            trackProperties.setBaseCoord(TrackProperties.BaseCoord.ONE);
                        }

                    }
                    if (key.equals("name")) {
                        trackProperties.setName(value);
                        //dhmay adding name check for TopHat junctions files. graphType is also checked.
                        if (value.equals("junctions")) {
                            trackProperties.setRendererClass(SpliceJunctionRenderer.class);
                            trackProperties.setHeight(60);
                        }
                    } else if (key.equals("description")) {
                        trackProperties.setDescription(value);
                    } else if (key.equals("itemrgb")) {
                        trackProperties.setItemRGB(value.toLowerCase().equals("on") || value.equals("1"));
                    } else if (key.equals("usescore")) {
                        trackProperties.setUseScore(value.equals("1"));
                    } else if (key.equals("color")) {
                        Color color = ColorUtilities.stringToColor(value);
                        trackProperties.setColor(color);
                    } else if (key.equals("altcolor")) {
                        Color color = ColorUtilities.stringToColor(value);
                        trackProperties.setAltColor(color);
                    } else if (key.equals("midcolor")) {
                        Color color = ColorUtilities.stringToColor(value);
                        trackProperties.setMidColor(color);
                    } else if (key.equals("autoscale")) {
                        boolean autoscale = value.equals("on");
                        trackProperties.setAutoScale(autoscale);
                    } else if (key.equals("maxheightpixels")) {
                        // There should be 3 values per UCSC spec,  max:default:min.  In the past we have accepted
                        // 2 values,  def:min,  so keep this for backwards compatibility.   IGV currently doesn't
                        // have a "max height"
                        String[] maxDefMin = value.split(":");
                        if (maxDefMin.length >= 2) {
                            int defIDX = (maxDefMin.length == 2 ? 0 : 1);
                            trackProperties.setHeight(Integer.parseInt(maxDefMin[defIDX].trim()));
                            trackProperties.setMinHeight(Integer.parseInt(maxDefMin[defIDX + 1].trim()));
                        }

                    } else if (key.equals("url")) {
                        trackProperties.setUrl(value);
                    } else if (key.equals("graphtype")) {

                        if (value.equals("bar")) {
                            trackProperties.setRendererClass(BarChartRenderer.class);
                        } else if (value.equals("points")) {
                            trackProperties.setRendererClass(ScatterplotRenderer.class);
                            trackProperties.setWindowingFunction(WindowFunction.none);
                        } else if (value.equals("line")) {
                            trackProperties.setRendererClass(LineplotRenderer.class);
                        } else if (value.equals("heatmap")) {
                            trackProperties.setRendererClass(HeatmapRenderer.class);
                        } else if (value.equals("junctions")) {
                            //dhmay adding check for graphType=junction.  name is also checked
                            trackProperties.setRendererClass(SpliceJunctionRenderer.class);
                        } else if (value.equals("genotype")) {
                            //dhmay adding check for graphType=junction.  name is also checked
                            trackProperties.setRendererClass(GenotypeRenderer.class);
                        }
                    } else if (key.toLowerCase().equals("viewlimits")) {
                        String[] limits = value.split(":");
                        if (limits.length == 2) {
                            try {
                                float min = Float.parseFloat(limits[0].trim());
                                float max = Float.parseFloat(limits[1].trim());
                                trackProperties.setMinValue(min);
                                trackProperties.setMaxValue(max);
                            } catch (NumberFormatException e) {
                                log.error("viewLimits values must be numeric: " + value);
                            }
                        }
                    } else if (key.equals("midrange")) {
                        String[] limits = value.split(":");
                        if (limits.length == 2) {
                            try {
                                float from = Float.parseFloat(limits[0].trim());
                                float to = Float.parseFloat(limits[1].trim());
                                trackProperties.setNeutralFromValue(from);
                                trackProperties.setNeutralToValue(to);
                            } catch (NumberFormatException e) {
                                log.error("midrange values must be numeric: " + value);
                            }
                        }
                    } else if (key.equals("ylinemark")) {
                        try {
                            float yLine = Float.parseFloat(value);
                            trackProperties.setyLine(yLine);
                        } catch (NumberFormatException e) {
                            log.error("Number format exception in track line (ylinemark): " + nextLine);
                        }
                    } else if (key.equals("ylineonoff")) {
                        trackProperties.setDrawYLine(value.equals("on"));
                    } else if (key.equals("windowingfunction")) {
                        if (value.equals("maximum")) {
                            trackProperties.setWindowingFunction(WindowFunction.max);
                        } else if (value.equals("minimum")) {
                            trackProperties.setWindowingFunction(WindowFunction.min);

                        } else if (value.equals("mean")) {
                            trackProperties.setWindowingFunction(WindowFunction.mean);

                        } else if (value.equals("median")) {
                            trackProperties.setWindowingFunction(WindowFunction.median);

                        } else if (value.equals("percentile10")) {
                            trackProperties.setWindowingFunction(WindowFunction.percentile10);

                        } else if (value.equals("percentile90")) {
                            trackProperties.setWindowingFunction(WindowFunction.percentile90);
                        } else if (value.equals("none")) {
                            trackProperties.setWindowingFunction(WindowFunction.none);
                        }
                    } else if (key.equals("maxfeaturewindow") || key.equals("featurevisibilitywindow") ||
                            key.equals("visibilitywindow")) {
                        try {
                            int windowSize = Integer.parseInt(value);
                            trackProperties.setFeatureVisibilityWindow(windowSize);
                        } catch (NumberFormatException e) {
                            log.error(key + " must be numeric: " + nextLine);

                        }

                    } else if (key.equals("scaletype")) {
                        if (value.equals("log")) {
                            trackProperties.setLogScale(true);
                        }
                    } else if (key.equals("gfftags")) {
                        // Any value other than 0 or off => on
                        boolean gffTags = !(value.equals("0") || (value.toLowerCase().equals("off")));
                        trackProperties.setGffTags(gffTags);
                    } else if (key.equals("sortable")) {
                        // Any value other than 0 or off => on
                        boolean sortable = (value.equals("1") || (value.toLowerCase().equals("true")));
                        trackProperties.setSortable(sortable);
                    } else if (key.equals("alternateexoncolor")) {
                        trackProperties.setAlternateExonColor(value.toLowerCase().equals("on") || value.equals("1"));
                    }
                }
            }

        } catch (
                Exception exception
                )

        {
            MessageUtils.showMessage("Error parsing track line: " + nextLine + " (" + exception.getMessage() + ")");
        }

        return foundProperties;

    }


    public static boolean pathExists(String covPath) {
        try {
            return (new File(covPath)).exists() ||
                    (HttpUtils.getInstance().isURL(covPath) && HttpUtils.getInstance().resourceAvailable(new URL(covPath)));
        } catch (MalformedURLException e) {
            // todo -- log
            return false;
        }
    }

    /**
     * Return the contents of the resource at path as a byte array
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static byte[] readAll(String path) throws IOException {
        InputStream is = null;
        try {
            byte [] buffer = new byte[100000];
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000000);
            is = openInputStream(new ResourceLocator(path));
            int nRead;
            while((nRead = is.read(buffer)) >= 0) {
                bos.write(buffer, 0, nRead);
            }
            return bos.toByteArray();
        } finally {
            is.close();
        }


    }
}
