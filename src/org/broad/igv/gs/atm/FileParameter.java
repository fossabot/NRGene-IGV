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

package org.broad.igv.gs.atm;

import java.util.List;

/**
* @author Jim Robinson
* @date Aug 3, 2011
*/
public class FileParameter {
    String name;// 	Name of the parameter.
    String description;// 	Description of the parameter.
    String required;// 	Boolean flag indicating whether or not the parameter is required.
    String compositeFilename;//  	True means that the value for this parameter can contain multiple filenames.  The filenames are separated by the nameDelimeters below.
    String nameDelimiters;// 	The string used to delimit multiple filenames.  This is only used if compositeFilename is true.
    List<GSDataFormat> formats;// 	A set of GSDataFormats, describing the acceptable file formats for this FileParameter.  See below.

    public FileParameter(String name, String description, String required, String compositeFilename,
                         String nameDelimiters, List<GSDataFormat> formats) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.compositeFilename = compositeFilename;
        this.nameDelimiters = nameDelimiters;
        this.formats = formats;
    }

    public void print() {
        System.out.print(name + "  ");
        for(GSDataFormat df : formats) {
            System.out.print(" " + df + ",");
        }
        System.out.println();

    }

}
