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

package org.broad.igv.feature.tribble;


import java.io.IOException;

import org.broad.igv.feature.BasicFeature;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.track.TrackType;
import org.broad.igv.util.ParsingUtils;
import org.broad.tribble.Feature;
import org.broad.tribble.exception.CodecLineParsingException;
import org.broad.tribble.readers.LineReader;

/**
 * @author jrobinso
 * @date Aug 5, 2010
 */
public abstract class UCSCCodec implements org.broad.tribble.FeatureCodec  {

    protected String[] tokens = new String[50];
    protected int startBase = 0;
    FeatureFileHeader header;

    public Object readHeader(LineReader reader) {
        String nextLine;
        header = new FeatureFileHeader();
        int nLines = 0;

        try {
            while ((nextLine = reader.readLine()) != null &&
                    (nextLine.startsWith("#") || nextLine.startsWith("track")) ||
                    nextLine.startsWith("browser")) {
                nLines++;
                if (nextLine.startsWith("#type")) {
                    String[] tokens = nextLine.split("=");
                    if (tokens.length > 1) {
                        try {
                            header.setTrackType(TrackType.valueOf(tokens[1]));
                        } catch (Exception e) {
                            // log.error("Error converting track type: " + tokens[1]);
                        }
                    }
                } else if (nextLine.startsWith("track")) {
                    TrackProperties tp = new TrackProperties();
                    ParsingUtils.parseTrackLine(nextLine, tp);
                    header.setTrackProperties(tp);
                }
            }
            return header;
        }
        catch (IOException e) {
            throw new CodecLineParsingException("Error parsing header", e);
        }
    }

    public Feature decodeLoc(String line) {
        return decode(line);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class getFeatureType() {
        return BasicFeature.class;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public Feature decode(String line) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Feature decode(String line, Genome genome) {
		return decode(line);
	}

	@Override
	public boolean canDecode(String path) {
		// TODO Auto-generated method stub
		return false;
	}
    
}
