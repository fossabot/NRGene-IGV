/*
 * Copyright (c) 2007-2010 by The Broad Institute, Inc. and the Massachusetts Institute of Technology.
 * All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL), Version 2.1 which
 * is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR WARRANTIES OF
 * ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT
 * OR OTHER DEFECTS, WHETHER OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR
 * RESPECTIVE TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES OF
 * ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES, ECONOMIC
 * DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER THE BROAD OR MIT SHALL
 * BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT SHALL KNOW OF THE POSSIBILITY OF THE
 * FOREGOING.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.ui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.NrgeneFeatureDescriptionDialog;
import org.broad.igv.ui.UIConstants;
import org.broad.igv.ui.util.FileChooserDialog;

/**
 * This menu action classes is used for both "Open Session ..." and load recent
 * session menu items.  In the "Open Session..." the user has to specify a
 * session file through the file menu.  For "load recent"  the action is
 * instantiated with a specific session file.
 *
 * @author jrobinso
 */
public class LinkToSessionMenuAction extends MenuAction {

    private static Logger log = Logger.getLogger(LinkToSessionMenuAction.class);
    private IGV mainFrame;
    private File sessionFile = null;
    private boolean autoload = false;
    
	static private String		linkTemplate = UIConstants.NRGENE_LINK_TEMPLATE;


    public LinkToSessionMenuAction(String label, File sessionFile, IGV mainFrame) {
        super(label);
        this.sessionFile = sessionFile;
        this.mainFrame = mainFrame;
        setToolTipText(UIConstants.LINKTO_SESSION_TOOLTIP);
        autoload = true;
    }

    public LinkToSessionMenuAction(String label, int mnemonic, IGV mainFrame) {
        super(label, null, mnemonic);
        this.mainFrame = mainFrame;
        setToolTipText(UIConstants.LINKTO_SESSION_TOOLTIP);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //dhmay adding if statement for restore of specific session specified in menu
        if (sessionFile == null || autoload == false) {
            FileChooserDialog dialog = new FileChooserDialog(IGV.getMainFrame(), true);
            dialog.setTitle("Link to Session");
            dialog.setSelectedFile(null);
            dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

            File lastSessionDirectory =
                    PreferenceManager.getInstance().getLastSessionDirectory();
            dialog.setCurrentDirectory(lastSessionDirectory);
            dialog.setVisible(true);

            if (dialog.isCanceled()) {
                return;
            }
            sessionFile = dialog.getSelectedFile();
        }
        doLinkToSession();

    }

    final public void doLinkToSession() {


        if (sessionFile != null) {

        	String			text;
        	try
        	{
        		text = linkTemplate.replaceAll("\\$1", URLEncoder.encode(sessionFile.getAbsolutePath(), "UTF-8"));
        	}
        	catch ( UnsupportedEncodingException e)
        	{
        		e.printStackTrace();
        		
        		text = e.getMessage();
        	}

        	NrgeneFeatureDescriptionDialog		dialog = new NrgeneFeatureDescriptionDialog(IGV.getMainFrame(), text);
        	dialog.setTitle("Link to Session");
        	dialog.setLocationRelativeTo(IGV.getMainFrame());
        	dialog.setVisible(true);
        	dialog.selectAll();
        }
    }

}

