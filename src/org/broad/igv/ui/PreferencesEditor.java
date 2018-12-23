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
package org.broad.igv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.broad.igv.PreferenceManager;
import org.broad.igv.batch.CommandListener;
import org.broad.igv.data.expression.ProbeToLocusMap;
import org.broad.igv.ui.legend.LegendDialog;
import org.broad.igv.ui.util.FontChooser;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.ui.util.UIUtilities;
import org.broad.igv.util.ColorUtilities;
import org.broad.igv.util.HttpUtils;
import org.broad.igv.util.Utilities;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import com.jidesoft.dialog.ButtonPanel;

/**
 * @author jrobinso
 */
public class PreferencesEditor extends javax.swing.JDialog {

    private boolean canceled = false;
    Map<String, String> updatedPreferenceMap = new HashMap();
    PreferenceManager prefMgr = PreferenceManager.getInstance();
    boolean updateOverlays = false;
    boolean inputValidated = true;
    private static int lastSelectedIndex = 0;
    boolean proxySettingsChanged;


    private void backgroundColorPanelMouseClicked(MouseEvent e) {
        final PreferenceManager prefMgr = PreferenceManager.getInstance();
        Color backgroundColor = UIUtilities.showColorChooserDialog("Choose background color",
                prefMgr.getAsColor(PreferenceManager.BACKGROUND_COLOR));
        if (backgroundColor != null) {
            prefMgr.put(PreferenceManager.BACKGROUND_COLOR, ColorUtilities.colorToString(backgroundColor));
            IGV.getInstance().getMainPanel().setBackground(backgroundColor);
            backgroundColorPanel.setBackground(backgroundColor);
        }

    }

    private void resetBackgroundButtonActionPerformed(ActionEvent e) {
        final PreferenceManager prefMgr = PreferenceManager.getInstance();
        prefMgr.remove(PreferenceManager.BACKGROUND_COLOR);
        Color backgroundColor = prefMgr.getAsColor(PreferenceManager.BACKGROUND_COLOR);
        if (backgroundColor != null) {
            prefMgr.put(PreferenceManager.BACKGROUND_COLOR, ColorUtilities.colorToString(backgroundColor));
            IGV.getInstance().getMainPanel().setBackground(backgroundColor);
            backgroundColorPanel.setBackground(backgroundColor);
        }

    }


    public PreferencesEditor(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initValues();

        tabbedPane.setSelectedIndex(lastSelectedIndex);

        // Conditionally remove database panel
        if (!prefMgr.getAsBoolean(PreferenceManager.DB_ENABLED)) {
            int idx = tabbedPane.indexOfTab("Database");
            if (idx > 0) {
                tabbedPane.remove(idx);
            }
        }

        setLocationRelativeTo(parent);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner non-commercial license
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        generalPanel = new JPanel();
        jPanel10 = new JPanel();
        missingDataExplanation = new JLabel();
        showMissingDataCB = new JCheckBox();
        combinePanelsCB = new JCheckBox();
        showAttributesDisplayCheckBox = new JCheckBox();
        searchZoomCB = new JCheckBox();
        zoomToFeatureExplanation = new JLabel();
        label4 = new JLabel();
        geneListFlankingField = new JTextField();
        zoomToFeatureExplanation2 = new JLabel();
        label5 = new JLabel();
        label6 = new JLabel();
        seqResolutionThreshold = new JTextField();
        label10 = new JLabel();
        defaultFontField = new JTextField();
        fontChangeButton = new JButton();
        showRegionBoundariesCB = new JCheckBox();
        label7 = new JLabel();
        backgroundColorPanel = new JPanel();
        resetBackgroundButton = new JButton();
        tracksPanel = new JPanel();
        jPanel6 = new JPanel();
        jLabel5 = new JLabel();
        defaultChartTrackHeightField = new JTextField();
        trackNameAttributeLabel = new JLabel();
        trackNameAttributeField = new JTextField();
        missingDataExplanation2 = new JLabel();
        jLabel8 = new JLabel();
        defaultTrackHeightField = new JTextField();
        missingDataExplanation4 = new JLabel();
        missingDataExplanation5 = new JLabel();
        missingDataExplanation3 = new JLabel();
        expandCB = new JCheckBox();
        normalizeCoverageCB = new JCheckBox();
        missingDataExplanation8 = new JLabel();
        expandIconCB = new JCheckBox();
        overlaysPanel = new JPanel();
        jPanel5 = new JPanel();
        jLabel3 = new JLabel();
        overlayAttributeTextField = new JTextField();
        overlayTrackCB = new JCheckBox();
        jLabel2 = new JLabel();
        jLabel4 = new JLabel();
        colorCodeMutationsCB = new JCheckBox();
        chooseMutationColorsButton = new JButton();
        label11 = new JLabel();
        showOrphanedMutationsCB = new JCheckBox();
        label12 = new JLabel();
        chartPanel = new JPanel();
        jPanel4 = new JPanel();
        topBorderCB = new JCheckBox();
        label1 = new JLabel();
        chartDrawTrackNameCB = new JCheckBox();
        bottomBorderCB = new JCheckBox();
        jLabel7 = new JLabel();
        colorBordersCB = new JCheckBox();
        labelYAxisCB = new JCheckBox();
        autoscaleCB = new JCheckBox();
        jLabel9 = new JLabel();
        showDatarangeCB = new JCheckBox();
        panel1 = new JPanel();
        label13 = new JLabel();
        showAllHeatmapFeauresCB = new JCheckBox();
        label14 = new JLabel();
        alignmentPanel = new JPanel();
        jPanel1 = new JPanel();
        jPanel11 = new JPanel();
        samMaxDepthField = new JTextField();
        jLabel11 = new JLabel();
        jLabel16 = new JLabel();
        mappingQualityThresholdField = new JTextField();
        jLabel14 = new JLabel();
        jLabel13 = new JLabel();
        jLabel15 = new JLabel();
        samMaxWindowSizeField = new JTextField();
        jLabel12 = new JLabel();
        jLabel26 = new JLabel();
        snpThresholdField = new JTextField();
        jPanel12 = new JPanel();
        samMinBaseQualityField = new JTextField();
        samShadeMismatchedBaseCB = new JCheckBox();
        samMaxBaseQualityField = new JTextField();
        showCovTrackCB = new JCheckBox();
        samFilterDuplicatesCB = new JCheckBox();
        jLabel19 = new JLabel();
        filterCB = new JCheckBox();
        filterURL = new JTextField();
        samFlagUnmappedPairCB = new JCheckBox();
        filterFailedReadsCB = new JCheckBox();
        label2 = new JLabel();
        showSoftClippedCB = new JCheckBox();
        showCenterLineCB = new JCheckBox();
        zeroQualityAlignmentCB = new JCheckBox();
        panel2 = new JPanel();
        isizeComputeCB = new JCheckBox();
        jLabel17 = new JLabel();
        insertSizeMinThresholdField = new JTextField();
        jLabel20 = new JLabel();
        insertSizeThresholdField = new JTextField();
        jLabel30 = new JLabel();
        jLabel18 = new JLabel();
        insertSizeMinPercentileField = new JTextField();
        insertSizeMaxPercentileField = new JTextField();
        label8 = new JLabel();
        label9 = new JLabel();
        panel3 = new JPanel();
        showJunctionTrackCB = new JCheckBox();
        junctionFlankingTextField = new JTextField();
        label15 = new JLabel();
        label16 = new JLabel();
        junctionCoverageTextField = new JTextField();
        expressionPane = new JPanel();
        jPanel8 = new JPanel();
        expMapToGeneCB = new JRadioButton();
        jLabel24 = new JLabel();
        expMapToLociCB = new JRadioButton();
        jLabel21 = new JLabel();
        advancedPanel = new JPanel();
        jPanel3 = new JPanel();
        jPanel2 = new JPanel();
        jLabel1 = new JLabel();
        genomeServerURLTextField = new JTextField();
        jLabel6 = new JLabel();
        dataServerURLTextField = new JTextField();
        editServerPropertiesCB = new JCheckBox();
        jButton1 = new JButton();
        clearGenomeCacheButton = new JButton();
        genomeUpdateCB = new JCheckBox();
        jPanel7 = new JPanel();
        enablePortCB = new JCheckBox();
        portField = new JTextField();
        jLabel22 = new JLabel();
        jPanel9 = new JPanel();
        useByteRangeCB = new JCheckBox();
        jLabel25 = new JLabel();
        proxyPanel = new JPanel();
        jPanel15 = new JPanel();
        jPanel16 = new JPanel();
        proxyUsernameField = new JTextField();
        jLabel28 = new JLabel();
        authenticateProxyCB = new JCheckBox();
        jLabel29 = new JLabel();
        proxyPasswordField = new JPasswordField();
        jPanel17 = new JPanel();
        proxyHostField = new JTextField();
        proxyPortField = new JTextField();
        jLabel27 = new JLabel();
        jLabel23 = new JLabel();
        useProxyCB = new JCheckBox();
        label3 = new JLabel();
        clearProxySettingsButton = new JButton();
        dbPanel = new JPanel();
        label17 = new JLabel();
        label18 = new JLabel();
        label19 = new JLabel();
        dbHostField = new JTextField();
        dbPortField = new JTextField();
        dbNameField = new JTextField();
        label20 = new JLabel();
        okCancelButtonPanel = new ButtonPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== tabbedPane ========
        {

            //======== generalPanel ========
            {
                generalPanel.setLayout(new BorderLayout());

                //======== jPanel10 ========
                {
                    jPanel10.setBorder(new BevelBorder(BevelBorder.RAISED));
                    jPanel10.setLayout(null);

                    //---- missingDataExplanation ----
                    missingDataExplanation.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    missingDataExplanation.setText("<html>Distinguish  regions with zero values from regions with no data on plots <br>(e.g. bar charts).  Regions with no data are indicated with a gray background.");
                    jPanel10.add(missingDataExplanation);
                    missingDataExplanation.setBounds(41, 35, 474, missingDataExplanation.getPreferredSize().height);

                    //---- showMissingDataCB ----
                    showMissingDataCB.setText("Distinguish Missing Data");
                    showMissingDataCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            showMissingDataCBActionPerformed(e);
                        }
                    });
                    jPanel10.add(showMissingDataCB);
                    showMissingDataCB.setBounds(new Rectangle(new Point(10, 6), showMissingDataCB.getPreferredSize()));

                    //---- combinePanelsCB ----
                    combinePanelsCB.setText("Combine Data and Feature Panels");
                    combinePanelsCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            combinePanelsCBActionPerformed(e);
                        }
                    });
                    jPanel10.add(combinePanelsCB);
                    combinePanelsCB.setBounds(new Rectangle(new Point(10, 95), combinePanelsCB.getPreferredSize()));

                    //---- showAttributesDisplayCheckBox ----
                    showAttributesDisplayCheckBox.setText("Show Attribute Display");
                    showAttributesDisplayCheckBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            showAttributesDisplayCheckBoxActionPerformed(e);
                        }
                    });
                    jPanel10.add(showAttributesDisplayCheckBox);
                    showAttributesDisplayCheckBox.setBounds(new Rectangle(new Point(10, 130), showAttributesDisplayCheckBox.getPreferredSize()));

                    //---- searchZoomCB ----
                    searchZoomCB.setText("Zoom to features");
                    searchZoomCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            searchZoomCBActionPerformed(e);
                        }
                    });
                    jPanel10.add(searchZoomCB);
                    searchZoomCB.setBounds(new Rectangle(new Point(10, 205), searchZoomCB.getPreferredSize()));

                    //---- zoomToFeatureExplanation ----
                    zoomToFeatureExplanation.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    zoomToFeatureExplanation.setText("<html>This option controls the behavior of feature searchs.  If true, the zoom level is changed as required to size the view to the feature size.  If false the zoom level is unchanged.");
                    zoomToFeatureExplanation.setVerticalAlignment(SwingConstants.TOP);
                    jPanel10.add(zoomToFeatureExplanation);
                    zoomToFeatureExplanation.setBounds(50, 230, 644, 50);

                    //---- label4 ----
                    label4.setText("Feature flanking region (bp): ");
                    jPanel10.add(label4);
                    label4.setBounds(new Rectangle(new Point(15, 365), label4.getPreferredSize()));

                    //---- geneListFlankingField ----
                    geneListFlankingField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            geneListFlankingFieldFocusLost(e);
                        }
                    });
                    geneListFlankingField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            geneListFlankingFieldActionPerformed(e);
                        }
                    });
                    jPanel10.add(geneListFlankingField);
                    geneListFlankingField.setBounds(215, 360, 255, geneListFlankingField.getPreferredSize().height);

                    //---- zoomToFeatureExplanation2 ----
                    zoomToFeatureExplanation2.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    zoomToFeatureExplanation2.setText("<html><i>Added before and after feature locus when zooming to a feature.  Also used when defining panel extents in gene/loci list views.");
                    zoomToFeatureExplanation2.setVerticalAlignment(SwingConstants.TOP);
                    jPanel10.add(zoomToFeatureExplanation2);
                    zoomToFeatureExplanation2.setBounds(45, 395, 637, 50);

                    //---- label5 ----
                    label5.setText("<html><i>Resolution in base-pairs per pixel at which sequence track becomes visible. ");
                    label5.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    jPanel10.add(label5);
                    label5.setBounds(new Rectangle(new Point(50, 320), label5.getPreferredSize()));

                    //---- label6 ----
                    label6.setText("Sequence Resolution Threshold (bp/pixel):");
                    jPanel10.add(label6);
                    label6.setBounds(new Rectangle(new Point(15, 290), label6.getPreferredSize()));

                    //---- seqResolutionThreshold ----
                    seqResolutionThreshold.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            seqResolutionThresholdFocusLost(e);
                        }
                    });
                    seqResolutionThreshold.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            seqResolutionThresholdActionPerformed(e);
                        }
                    });
                    jPanel10.add(seqResolutionThreshold);
                    seqResolutionThreshold.setBounds(315, 285, 105, seqResolutionThreshold.getPreferredSize().height);

                    //---- label10 ----
                    label10.setText("Default font: ");
                    label10.setLabelFor(defaultFontField);
                    jPanel10.add(label10);
                    label10.setBounds(new Rectangle(new Point(15, 530), label10.getPreferredSize()));

                    //---- defaultFontField ----
                    defaultFontField.setEditable(false);
                    jPanel10.add(defaultFontField);
                    defaultFontField.setBounds(105, 525, 238, defaultFontField.getPreferredSize().height);

                    //---- fontChangeButton ----
                    fontChangeButton.setText("Change...");
                    fontChangeButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            fontChangeButtonActionPerformed(e);
                        }
                    });
                    jPanel10.add(fontChangeButton);
                    fontChangeButton.setBounds(360, 525, 97, fontChangeButton.getPreferredSize().height);

                    //---- showRegionBoundariesCB ----
                    showRegionBoundariesCB.setText("Show Region Boundaries");
                    showRegionBoundariesCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            showRegionBoundariesCBActionPerformed(e);
                        }
                    });
                    jPanel10.add(showRegionBoundariesCB);
                    showRegionBoundariesCB.setBounds(10, 165, 275, 23);

                    //---- label7 ----
                    label7.setText("Background color click to change): ");
                    jPanel10.add(label7);
                    label7.setBounds(15, 480, 235, label7.getPreferredSize().height);

                    //======== backgroundColorPanel ========
                    {
                        backgroundColorPanel.setPreferredSize(new Dimension(20, 20));
                        backgroundColorPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
                        backgroundColorPanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                backgroundColorPanelMouseClicked(e);
                            }
                        });
                        backgroundColorPanel.setLayout(null);
                    }
                    jPanel10.add(backgroundColorPanel);
                    backgroundColorPanel.setBounds(255, 474, 30, 29);

                    //---- resetBackgroundButton ----
                    resetBackgroundButton.setText("Reset to default");
                    resetBackgroundButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            resetBackgroundButtonActionPerformed(e);
                        }
                    });
                    jPanel10.add(resetBackgroundButton);
                    resetBackgroundButton.setBounds(315, 474, 150, resetBackgroundButton.getPreferredSize().height);

                    { // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for (int i = 0; i < jPanel10.getComponentCount(); i++) {
                            Rectangle bounds = jPanel10.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = jPanel10.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        jPanel10.setMinimumSize(preferredSize);
                        jPanel10.setPreferredSize(preferredSize);
                    }
                }
                generalPanel.add(jPanel10, BorderLayout.CENTER);
            }
            tabbedPane.addTab("General", generalPanel);


            //======== tracksPanel ========
            {
                tracksPanel.setLayout(null);

                //======== jPanel6 ========
                {
                    jPanel6.setLayout(null);

                    //---- jLabel5 ----
                    jLabel5.setText("Default Track Height, Charts (Pixels)");
                    jPanel6.add(jLabel5);
                    jLabel5.setBounds(new Rectangle(new Point(10, 12), jLabel5.getPreferredSize()));

                    //---- defaultChartTrackHeightField ----
                    defaultChartTrackHeightField.setText("40");
                    defaultChartTrackHeightField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            defaultChartTrackHeightFieldActionPerformed(e);
                        }
                    });
                    defaultChartTrackHeightField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            defaultChartTrackHeightFieldFocusLost(e);
                        }
                    });
                    jPanel6.add(defaultChartTrackHeightField);
                    defaultChartTrackHeightField.setBounds(271, 6, 57, defaultChartTrackHeightField.getPreferredSize().height);

                    //---- trackNameAttributeLabel ----
                    trackNameAttributeLabel.setText("Track Name Attribute");
                    jPanel6.add(trackNameAttributeLabel);
                    trackNameAttributeLabel.setBounds(new Rectangle(new Point(10, 120), trackNameAttributeLabel.getPreferredSize()));

                    //---- trackNameAttributeField ----
                    trackNameAttributeField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            trackNameAttributeFieldActionPerformed(e);
                        }
                    });
                    trackNameAttributeField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            trackNameAttributeFieldFocusLost(e);
                        }
                    });
                    jPanel6.add(trackNameAttributeField);
                    trackNameAttributeField.setBounds(150, 115, 216, trackNameAttributeField.getPreferredSize().height);

                    //---- missingDataExplanation2 ----
                    missingDataExplanation2.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    missingDataExplanation2.setText("<html>Name of an attribute to be used to label  tracks.  If provided tracks will be labeled with the corresponding attribute values from the sample information file");
                    missingDataExplanation2.setVerticalAlignment(SwingConstants.TOP);
                    jPanel6.add(missingDataExplanation2);
                    missingDataExplanation2.setBounds(40, 170, 578, 54);

                    //---- jLabel8 ----
                    jLabel8.setText("Default Track Height, Other (Pixels)");
                    jPanel6.add(jLabel8);
                    jLabel8.setBounds(new Rectangle(new Point(10, 45), jLabel8.getPreferredSize()));

                    //---- defaultTrackHeightField ----
                    defaultTrackHeightField.setText("15");
                    defaultTrackHeightField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            defaultTrackHeightFieldActionPerformed(e);
                        }
                    });
                    defaultTrackHeightField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            defaultTrackHeightFieldFocusLost(e);
                        }
                    });
                    jPanel6.add(defaultTrackHeightField);
                    defaultTrackHeightField.setBounds(271, 39, 57, defaultTrackHeightField.getPreferredSize().height);

                    //---- missingDataExplanation4 ----
                    missingDataExplanation4.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    missingDataExplanation4.setText("<html>Default height of chart tracks (barcharts, scatterplots, etc)");
                    jPanel6.add(missingDataExplanation4);
                    missingDataExplanation4.setBounds(350, 5, 354, 25);

                    //---- missingDataExplanation5 ----
                    missingDataExplanation5.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    missingDataExplanation5.setText("<html>Default height of all other tracks");
                    jPanel6.add(missingDataExplanation5);
                    missingDataExplanation5.setBounds(350, 41, 1141, 25);

                    //---- missingDataExplanation3 ----
                    missingDataExplanation3.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    missingDataExplanation3.setText("<html><i> If selected feature tracks are expanded by default.");
                    jPanel6.add(missingDataExplanation3);
                    missingDataExplanation3.setBounds(new Rectangle(new Point(876, 318), missingDataExplanation3.getPreferredSize()));

                    //---- expandCB ----
                    expandCB.setText("Expand Feature Tracks");
                    expandCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            expandCBActionPerformed(e);
                        }
                    });
                    jPanel6.add(expandCB);
                    expandCB.setBounds(new Rectangle(new Point(6, 272), expandCB.getPreferredSize()));

                    //---- normalizeCoverageCB ----
                    normalizeCoverageCB.setText("Normalize Coverage Data");
                    normalizeCoverageCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            normalizeCoverageCBActionPerformed(e);
                        }
                    });
                    normalizeCoverageCB.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            normalizeCoverageCBFocusLost(e);
                        }
                    });
                    jPanel6.add(normalizeCoverageCB);
                    normalizeCoverageCB.setBounds(new Rectangle(new Point(6, 372), normalizeCoverageCB.getPreferredSize()));

                    //---- missingDataExplanation8 ----
                    missingDataExplanation8.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    missingDataExplanation8.setText("<html><i> Applies to coverage tracks computed with igvtools (.tdf files).  If selected coverage values are scaled by (1,000,000 / totalCount),  where totalCount is the total number of features or alignments.");
                    missingDataExplanation8.setVerticalAlignment(SwingConstants.TOP);
                    jPanel6.add(missingDataExplanation8);
                    missingDataExplanation8.setBounds(50, 413, 608, 52);

                    //---- expandIconCB ----
                    expandIconCB.setText("Show Expand Icon");
                    expandIconCB.setToolTipText("If checked displays an expand/collapse icon on feature tracks.");
                    expandIconCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            expandIconCBActionPerformed(e);
                        }
                    });
                    jPanel6.add(expandIconCB);
                    expandIconCB.setBounds(new Rectangle(new Point(6, 318), expandIconCB.getPreferredSize()));

                    { // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for (int i = 0; i < jPanel6.getComponentCount(); i++) {
                            Rectangle bounds = jPanel6.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = jPanel6.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        jPanel6.setMinimumSize(preferredSize);
                        jPanel6.setPreferredSize(preferredSize);
                    }
                }
                tracksPanel.add(jPanel6);
                jPanel6.setBounds(40, 20, 725, 480);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < tracksPanel.getComponentCount(); i++) {
                        Rectangle bounds = tracksPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = tracksPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    tracksPanel.setMinimumSize(preferredSize);
                    tracksPanel.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Tracks", tracksPanel);


            //======== overlaysPanel ========
            {

                //======== jPanel5 ========
                {
                    jPanel5.setLayout(null);

                    //---- jLabel3 ----
                    jLabel3.setText("Linking attribute column:");
                    jPanel5.add(jLabel3);
                    jLabel3.setBounds(new Rectangle(new Point(65, 86), jLabel3.getPreferredSize()));

                    //---- overlayAttributeTextField ----
                    overlayAttributeTextField.setText("LINKING_ID");
                    overlayAttributeTextField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            overlayAttributeTextFieldActionPerformed(e);
                        }
                    });
                    overlayAttributeTextField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            overlayAttributeTextFieldFocusLost(e);
                        }
                    });
                    jPanel5.add(overlayAttributeTextField);
                    overlayAttributeTextField.setBounds(240, 80, 228, overlayAttributeTextField.getPreferredSize().height);

                    //---- overlayTrackCB ----
                    overlayTrackCB.setSelected(true);
                    overlayTrackCB.setText("Overlay mutation tracks");
                    overlayTrackCB.setActionCommand("overlayTracksCB");
                    overlayTrackCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            overlayTrackCBActionPerformed(e);
                        }
                    });
                    jPanel5.add(overlayTrackCB);
                    overlayTrackCB.setBounds(new Rectangle(new Point(6, 51), overlayTrackCB.getPreferredSize()));

                    //---- jLabel2 ----
                    jLabel2.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    jPanel5.add(jLabel2);
                    jLabel2.setBounds(new Rectangle(new Point(6, 6), jLabel2.getPreferredSize()));

                    //---- jLabel4 ----
                    jLabel4.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    jPanel5.add(jLabel4);
                    jLabel4.setBounds(new Rectangle(new Point(6, 12), jLabel4.getPreferredSize()));

                    //---- colorCodeMutationsCB ----
                    colorCodeMutationsCB.setText("Color code mutations");
                    colorCodeMutationsCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            colorMutationsCBActionPerformed(e);
                        }
                    });
                    jPanel5.add(colorCodeMutationsCB);
                    colorCodeMutationsCB.setBounds(new Rectangle(new Point(0, 295), colorCodeMutationsCB.getPreferredSize()));

                    //---- chooseMutationColorsButton ----
                    chooseMutationColorsButton.setText("Choose colors");
                    chooseMutationColorsButton.setFont(UIManager.getFont("Button.font"));
                    chooseMutationColorsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
                    chooseMutationColorsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            chooseMutationColorsButtonActionPerformed(e);
                        }
                    });
                    jPanel5.add(chooseMutationColorsButton);
                    chooseMutationColorsButton.setBounds(new Rectangle(new Point(185, 292), chooseMutationColorsButton.getPreferredSize()));

                    //---- label11 ----
                    label11.setText("<html><i>Name of a sample attribute column to link mutation and data tracks");
                    label11.setVerticalAlignment(SwingConstants.TOP);
                    jPanel5.add(label11);
                    label11.setBounds(110, 115, 360, 50);

                    //---- showOrphanedMutationsCB ----
                    showOrphanedMutationsCB.setText("Show orphaned mutation tracks");
                    showOrphanedMutationsCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            showOrphanedMutationsCBActionPerformed(e);
                        }
                    });
                    jPanel5.add(showOrphanedMutationsCB);
                    showOrphanedMutationsCB.setBounds(new Rectangle(new Point(70, 180), showOrphanedMutationsCB.getPreferredSize()));

                    //---- label12 ----
                    label12.setText("<html><i>Select to show mutation tracks with no corresponding data track.");
                    label12.setVerticalAlignment(SwingConstants.TOP);
                    jPanel5.add(label12);
                    label12.setBounds(110, 210, 360, 55);

                    { // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for (int i = 0; i < jPanel5.getComponentCount(); i++) {
                            Rectangle bounds = jPanel5.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = jPanel5.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        jPanel5.setMinimumSize(preferredSize);
                        jPanel5.setPreferredSize(preferredSize);
                    }
                }

                GroupLayout overlaysPanelLayout = new GroupLayout(overlaysPanel);
                overlaysPanel.setLayout(overlaysPanelLayout);
                overlaysPanelLayout.setHorizontalGroup(
                        overlaysPanelLayout.createParallelGroup()
                                .add(overlaysPanelLayout.createSequentialGroup()
                                        .add(28, 28, 28)
                                        .add(jPanel5, GroupLayout.PREFERRED_SIZE, 673, GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(80, Short.MAX_VALUE))
                );
                overlaysPanelLayout.setVerticalGroup(
                        overlaysPanelLayout.createParallelGroup()
                                .add(overlaysPanelLayout.createSequentialGroup()
                                        .add(55, 55, 55)
                                        .add(jPanel5, GroupLayout.PREFERRED_SIZE, 394, GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(117, Short.MAX_VALUE))
                );
            }
            tabbedPane.addTab("Mutations", overlaysPanel);


            //======== chartPanel ========
            {
                chartPanel.setLayout(null);

                //======== jPanel4 ========
                {
                    jPanel4.setBorder(LineBorder.createBlackLineBorder());
                    jPanel4.setLayout(null);

                    //---- topBorderCB ----
                    topBorderCB.setText("Draw Top Border");
                    topBorderCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            topBorderCBActionPerformed(e);
                        }
                    });
                    jPanel4.add(topBorderCB);
                    topBorderCB.setBounds(new Rectangle(new Point(30, 36), topBorderCB.getPreferredSize()));

                    //---- label1 ----
                    label1.setFont(label1.getFont());
                    label1.setText("<html><b>Default settings for barcharts and scatterplots:");
                    jPanel4.add(label1);
                    label1.setBounds(10, 10, 380, 25);

                    //---- chartDrawTrackNameCB ----
                    chartDrawTrackNameCB.setText("Draw Track Label");
                    chartDrawTrackNameCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            chartDrawTrackNameCBActionPerformed(e);
                        }
                    });
                    jPanel4.add(chartDrawTrackNameCB);
                    chartDrawTrackNameCB.setBounds(new Rectangle(new Point(30, 126), chartDrawTrackNameCB.getPreferredSize()));

                    //---- bottomBorderCB ----
                    bottomBorderCB.setText("Draw Bottom Border");
                    bottomBorderCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            bottomBorderCBActionPerformed(e);
                        }
                    });
                    jPanel4.add(bottomBorderCB);
                    bottomBorderCB.setBounds(new Rectangle(new Point(30, 66), bottomBorderCB.getPreferredSize()));

                    //---- jLabel7 ----
                    jLabel7.setText("<html><i>Dynamically rescale to the range of the data in view.");
                    jPanel4.add(jLabel7);
                    jLabel7.setBounds(220, 170, 371, 50);

                    //---- colorBordersCB ----
                    colorBordersCB.setText("Color Borders");
                    colorBordersCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            colorBordersCBActionPerformed(e);
                        }
                    });
                    jPanel4.add(colorBordersCB);
                    colorBordersCB.setBounds(new Rectangle(new Point(30, 96), colorBordersCB.getPreferredSize()));

                    //---- labelYAxisCB ----
                    labelYAxisCB.setText("Label Y Axis");
                    labelYAxisCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            labelYAxisCBActionPerformed(e);
                        }
                    });
                    jPanel4.add(labelYAxisCB);
                    labelYAxisCB.setBounds(new Rectangle(new Point(30, 156), labelYAxisCB.getPreferredSize()));

                    //---- autoscaleCB ----
                    autoscaleCB.setText("Continuous Autoscale");
                    autoscaleCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            autoscaleCBActionPerformed(e);
                        }
                    });
                    jPanel4.add(autoscaleCB);
                    autoscaleCB.setBounds(new Rectangle(new Point(30, 186), autoscaleCB.getPreferredSize()));

                    //---- jLabel9 ----
                    jLabel9.setText("<html><i>Draw a label centered over the track. ");
                    jPanel4.add(jLabel9);
                    jLabel9.setBounds(220, 159, 355, jLabel9.getPreferredSize().height);

                    //---- showDatarangeCB ----
                    showDatarangeCB.setText("Show Data Range");
                    showDatarangeCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            showDatarangeCBActionPerformed(e);
                        }
                    });
                    showDatarangeCB.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            showDatarangeCBFocusLost(e);
                        }
                    });
                    jPanel4.add(showDatarangeCB);
                    showDatarangeCB.setBounds(30, 216, showDatarangeCB.getPreferredSize().width, 26);

                    { // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for (int i = 0; i < jPanel4.getComponentCount(); i++) {
                            Rectangle bounds = jPanel4.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = jPanel4.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        jPanel4.setMinimumSize(preferredSize);
                        jPanel4.setPreferredSize(preferredSize);
                    }
                }
                chartPanel.add(jPanel4);
                jPanel4.setBounds(20, 20, 650, 290);

                //======== panel1 ========
                {
                    panel1.setBorder(LineBorder.createBlackLineBorder());
                    panel1.setLayout(null);

                    //---- label13 ----
                    label13.setText("<html><b>Default settings for heatmaps:");
                    panel1.add(label13);
                    label13.setBounds(10, 5, 250, 30);

                    //---- showAllHeatmapFeauresCB ----
                    showAllHeatmapFeauresCB.setText("Show all features");
                    showAllHeatmapFeauresCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            showAllHeatmapFeauresCBActionPerformed(e);
                        }
                    });
                    panel1.add(showAllHeatmapFeauresCB);
                    showAllHeatmapFeauresCB.setBounds(new Rectangle(new Point(20, 45), showAllHeatmapFeauresCB.getPreferredSize()));

                    //---- label14 ----
                    label14.setText("<html><i>Paint all features/segments with a minimum width of 1 pixel.   If not checked features/segments with screen widths less than 1 pixel are not drawn.");
                    panel1.add(label14);
                    label14.setBounds(200, 35, 425, 60);
                }
                chartPanel.add(panel1);
                panel1.setBounds(20, 340, 650, 135);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < chartPanel.getComponentCount(); i++) {
                        Rectangle bounds = chartPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = chartPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    chartPanel.setMinimumSize(preferredSize);
                    chartPanel.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Charts", chartPanel);


            //======== alignmentPanel ========
            {
                alignmentPanel.setLayout(null);

                //======== jPanel1 ========
                {
                    jPanel1.setLayout(null);

                    //======== jPanel11 ========
                    {
                        jPanel11.setBorder(null);
                        jPanel11.setLayout(null);

                        //---- samMaxDepthField ----
                        samMaxDepthField.setText("jTextField1");
                        samMaxDepthField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                samMaxLevelFieldActionPerformed(e);
                            }
                        });
                        samMaxDepthField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                samMaxLevelFieldFocusLost(e);
                            }
                        });
                        jPanel11.add(samMaxDepthField);
                        samMaxDepthField.setBounds(new Rectangle(new Point(206, 40), samMaxDepthField.getPreferredSize()));

                        //---- jLabel11 ----
                        jLabel11.setText("Visibility range threshold (kb)");
                        jPanel11.add(jLabel11);
                        jLabel11.setBounds(new Rectangle(new Point(6, 12), jLabel11.getPreferredSize()));

                        //---- jLabel16 ----
                        jLabel16.setText("<html><i>Reads with qualities  below the threshold are not shown.");
                        jPanel11.add(jLabel16);
                        jLabel16.setBounds(new Rectangle(new Point(296, 80), jLabel16.getPreferredSize()));

                        //---- mappingQualityThresholdField ----
                        mappingQualityThresholdField.setText("0");
                        mappingQualityThresholdField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                mappingQualityThresholdFieldActionPerformed(e);
                            }
                        });
                        mappingQualityThresholdField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                mappingQualityThresholdFieldFocusLost(e);
                            }
                        });
                        jPanel11.add(mappingQualityThresholdField);
                        mappingQualityThresholdField.setBounds(206, 74, 84, mappingQualityThresholdField.getPreferredSize().height);

                        //---- jLabel14 ----
                        jLabel14.setText("<html><i>Maximum read depth to load (approximate).");
                        jPanel11.add(jLabel14);
                        jLabel14.setBounds(296, 39, 390, 30);

                        //---- jLabel13 ----
                        jLabel13.setText("Maximum read depth:");
                        jPanel11.add(jLabel13);
                        jLabel13.setBounds(new Rectangle(new Point(6, 46), jLabel13.getPreferredSize()));

                        //---- jLabel15 ----
                        jLabel15.setText("Mapping quality threshold:");
                        jPanel11.add(jLabel15);
                        jLabel15.setBounds(new Rectangle(new Point(6, 80), jLabel15.getPreferredSize()));

                        //---- samMaxWindowSizeField ----
                        samMaxWindowSizeField.setText("jTextField1");
                        samMaxWindowSizeField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                samMaxWindowSizeFieldActionPerformed(e);
                            }
                        });
                        samMaxWindowSizeField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                samMaxWindowSizeFieldFocusLost(e);
                            }
                        });
                        jPanel11.add(samMaxWindowSizeField);
                        samMaxWindowSizeField.setBounds(new Rectangle(new Point(206, 6), samMaxWindowSizeField.getPreferredSize()));

                        //---- jLabel12 ----
                        jLabel12.setText("<html><i>Nominal window size at which alignments become visible");
                        jPanel11.add(jLabel12);
                        jLabel12.setBounds(new Rectangle(new Point(296, 12), jLabel12.getPreferredSize()));

                        //---- jLabel26 ----
                        jLabel26.setText("Coverage allele-freq threshold");
                        jPanel11.add(jLabel26);
                        jLabel26.setBounds(6, 114, 200, jLabel26.getPreferredSize().height);

                        //---- snpThresholdField ----
                        snpThresholdField.setText("0");
                        snpThresholdField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                snpThresholdFieldActionPerformed(e);
                            }
                        });
                        snpThresholdField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                snpThresholdFieldFocusLost(e);
                            }
                        });
                        jPanel11.add(snpThresholdField);
                        snpThresholdField.setBounds(206, 108, 84, snpThresholdField.getPreferredSize().height);

                        { // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < jPanel11.getComponentCount(); i++) {
                                Rectangle bounds = jPanel11.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = jPanel11.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            jPanel11.setMinimumSize(preferredSize);
                            jPanel11.setPreferredSize(preferredSize);
                        }
                    }
                    jPanel1.add(jPanel11);
                    jPanel11.setBounds(5, 10, 755, 150);

                    //======== jPanel12 ========
                    {
                        jPanel12.setBorder(null);
                        jPanel12.setLayout(null);

                        //---- samMinBaseQualityField ----
                        samMinBaseQualityField.setText("0");
                        samMinBaseQualityField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                samMinBaseQualityFieldActionPerformed(e);
                            }
                        });
                        samMinBaseQualityField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                samMinBaseQualityFieldFocusLost(e);
                            }
                        });
                        jPanel12.add(samMinBaseQualityField);
                        samMinBaseQualityField.setBounds(580, 105, 50, samMinBaseQualityField.getPreferredSize().height);

                        //---- samShadeMismatchedBaseCB ----
                        samShadeMismatchedBaseCB.setText("Shade mismatched bases by quality. ");
                        samShadeMismatchedBaseCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                samShadeMismatchedBaseCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(samShadeMismatchedBaseCB);
                        samShadeMismatchedBaseCB.setBounds(263, 105, 264, samShadeMismatchedBaseCB.getPreferredSize().height);

                        //---- samMaxBaseQualityField ----
                        samMaxBaseQualityField.setText("0");
                        samMaxBaseQualityField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                samMaxBaseQualityFieldActionPerformed(e);
                            }
                        });
                        samMaxBaseQualityField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                samMaxBaseQualityFieldFocusLost(e);
                            }
                        });
                        jPanel12.add(samMaxBaseQualityField);
                        samMaxBaseQualityField.setBounds(680, 105, 50, samMaxBaseQualityField.getPreferredSize().height);

                        //---- showCovTrackCB ----
                        showCovTrackCB.setText("Show coverage track");
                        showCovTrackCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                showCovTrackCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(showCovTrackCB);
                        showCovTrackCB.setBounds(263, 10, 270, showCovTrackCB.getPreferredSize().height);

                        //---- samFilterDuplicatesCB ----
                        samFilterDuplicatesCB.setText("Filter duplicate reads");
                        samFilterDuplicatesCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                samShowDuplicatesCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(samFilterDuplicatesCB);
                        samFilterDuplicatesCB.setBounds(6, 10, 290, samFilterDuplicatesCB.getPreferredSize().height);

                        //---- jLabel19 ----
                        jLabel19.setText("Min: ");
                        jPanel12.add(jLabel19);
                        jLabel19.setBounds(new Rectangle(new Point(540, 110), jLabel19.getPreferredSize()));

                        //---- filterCB ----
                        filterCB.setText("Filter alignments by read group");
                        filterCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                filterCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(filterCB);
                        filterCB.setBounds(6, 140, 244, filterCB.getPreferredSize().height);

                        //---- filterURL ----
                        filterURL.setText("URL or path to filter file");
                        filterURL.setEnabled(false);
                        filterURL.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                filterURLActionPerformed(e);
                            }
                        });
                        filterURL.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                filterURLFocusLost(e);
                            }
                        });
                        jPanel12.add(filterURL);
                        filterURL.setBounds(270, 140, 440, filterURL.getPreferredSize().height);

                        //---- samFlagUnmappedPairCB ----
                        samFlagUnmappedPairCB.setText("Flag unmapped pairs");
                        samFlagUnmappedPairCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                samFlagUnmappedPairCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(samFlagUnmappedPairCB);
                        samFlagUnmappedPairCB.setBounds(6, 74, 310, samFlagUnmappedPairCB.getPreferredSize().height);

                        //---- filterFailedReadsCB ----
                        filterFailedReadsCB.setText("Filter vendor failed reads");
                        filterFailedReadsCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                filterVendorFailedReadsCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(filterFailedReadsCB);
                        filterFailedReadsCB.setBounds(new Rectangle(new Point(6, 42), filterFailedReadsCB.getPreferredSize()));

                        //---- label2 ----
                        label2.setText("Max:");
                        jPanel12.add(label2);
                        label2.setBounds(new Rectangle(new Point(640, 110), label2.getPreferredSize()));

                        //---- showSoftClippedCB ----
                        showSoftClippedCB.setText("Show soft-clipped bases");
                        showSoftClippedCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                showSoftClippedCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(showSoftClippedCB);
                        showSoftClippedCB.setBounds(new Rectangle(new Point(263, 42), showSoftClippedCB.getPreferredSize()));

                        //---- showCenterLineCB ----
                        showCenterLineCB.setText("Show center line");
                        showCenterLineCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                showCenterLineCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(showCenterLineCB);
                        showCenterLineCB.setBounds(6, 105, 199, showCenterLineCB.getPreferredSize().height);

                        //---- zeroQualityAlignmentCB ----
                        zeroQualityAlignmentCB.setText("Flag zero-quality alignments");
                        zeroQualityAlignmentCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                zeroQualityAlignmentCBActionPerformed(e);
                            }
                        });
                        jPanel12.add(zeroQualityAlignmentCB);
                        zeroQualityAlignmentCB.setBounds(new Rectangle(new Point(263, 74), zeroQualityAlignmentCB.getPreferredSize()));

                        { // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < jPanel12.getComponentCount(); i++) {
                                Rectangle bounds = jPanel12.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = jPanel12.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            jPanel12.setMinimumSize(preferredSize);
                            jPanel12.setPreferredSize(preferredSize);
                        }
                    }
                    jPanel1.add(jPanel12);
                    jPanel12.setBounds(5, 145, 755, 180);

                    //======== panel2 ========
                    {
                        panel2.setBorder(new TitledBorder("Insert Size Options"));
                        panel2.setLayout(null);

                        //---- isizeComputeCB ----
                        isizeComputeCB.setText("Compute");
                        isizeComputeCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                isizeComputeCBActionPerformed(e);
                                isizeComputeCBActionPerformed(e);
                                isizeComputeCBActionPerformed(e);
                            }
                        });
                        panel2.add(isizeComputeCB);
                        isizeComputeCB.setBounds(new Rectangle(new Point(360, 76), isizeComputeCB.getPreferredSize()));

                        //---- jLabel17 ----
                        jLabel17.setText("Maximum (bp):");
                        panel2.add(jLabel17);
                        jLabel17.setBounds(100, 110, 110, jLabel17.getPreferredSize().height);

                        //---- insertSizeMinThresholdField ----
                        insertSizeMinThresholdField.setText("0");
                        insertSizeMinThresholdField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                insertSizeThresholdFieldActionPerformed(e);
                                insertSizeMinThresholdFieldActionPerformed(e);
                                insertSizeMinThresholdFieldActionPerformed(e);
                                insertSizeMinThresholdFieldActionPerformed(e);
                            }
                        });
                        insertSizeMinThresholdField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                insertSizeThresholdFieldFocusLost(e);
                                insertSizeMinThresholdFieldFocusLost(e);
                            }
                        });
                        panel2.add(insertSizeMinThresholdField);
                        insertSizeMinThresholdField.setBounds(220, 75, 84, 28);

                        //---- jLabel20 ----
                        jLabel20.setText("Minimum (bp):");
                        panel2.add(jLabel20);
                        jLabel20.setBounds(100, 80, 110, 16);

                        //---- insertSizeThresholdField ----
                        insertSizeThresholdField.setText("0");
                        insertSizeThresholdField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                insertSizeThresholdFieldActionPerformed(e);
                                insertSizeThresholdFieldActionPerformed(e);
                            }
                        });
                        insertSizeThresholdField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                insertSizeThresholdFieldFocusLost(e);
                            }
                        });
                        panel2.add(insertSizeThresholdField);
                        insertSizeThresholdField.setBounds(220, 105, 84, insertSizeThresholdField.getPreferredSize().height);

                        //---- jLabel30 ----
                        jLabel30.setText("Minimum (percentile):");
                        panel2.add(jLabel30);
                        jLabel30.setBounds(460, 80, 155, 16);

                        //---- jLabel18 ----
                        jLabel18.setText("Maximum (percentile):");
                        panel2.add(jLabel18);
                        jLabel18.setBounds(460, 110, 155, 16);

                        //---- insertSizeMinPercentileField ----
                        insertSizeMinPercentileField.setText("0");
                        insertSizeMinPercentileField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                insertSizeThresholdFieldActionPerformed(e);
                                insertSizeMinThresholdFieldActionPerformed(e);
                                insertSizeMinThresholdFieldActionPerformed(e);
                                insertSizeMinThresholdFieldActionPerformed(e);
                                insertSizeMinPercentileFieldActionPerformed(e);
                            }
                        });
                        insertSizeMinPercentileField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                insertSizeThresholdFieldFocusLost(e);
                                insertSizeMinThresholdFieldFocusLost(e);
                                insertSizeMinPercentileFieldFocusLost(e);
                            }
                        });
                        panel2.add(insertSizeMinPercentileField);
                        insertSizeMinPercentileField.setBounds(625, 75, 84, 28);

                        //---- insertSizeMaxPercentileField ----
                        insertSizeMaxPercentileField.setText("0");
                        insertSizeMaxPercentileField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                insertSizeThresholdFieldActionPerformed(e);
                                insertSizeThresholdFieldActionPerformed(e);
                                insertSizeMaxPercentileFieldActionPerformed(e);
                            }
                        });
                        insertSizeMaxPercentileField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                insertSizeThresholdFieldFocusLost(e);
                                insertSizeMaxPercentileFieldFocusLost(e);
                            }
                        });
                        panel2.add(insertSizeMaxPercentileField);
                        insertSizeMaxPercentileField.setBounds(625, 105, 84, 28);

                        //---- label8 ----
                        label8.setText("<html><i>These options control the color coding of paired alignments by inferred insert size.   Base pair values set default values.  If \"compute\" is selected  values are computed from the actual size distribution of each library.");
                        panel2.add(label8);
                        label8.setBounds(5, 15, 735, 55);

                        //---- label9 ----
                        label9.setText("Defaults ");
                        panel2.add(label9);
                        label9.setBounds(new Rectangle(new Point(15, 80), label9.getPreferredSize()));

                        { // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel2.getComponentCount(); i++) {
                                Rectangle bounds = panel2.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel2.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel2.setMinimumSize(preferredSize);
                            panel2.setPreferredSize(preferredSize);
                        }
                    }
                    jPanel1.add(panel2);
                    panel2.setBounds(5, 410, 755, 145);

                    //======== panel3 ========
                    {
                        panel3.setBorder(new TitledBorder(""));
                        panel3.setLayout(null);

                        //---- showJunctionTrackCB ----
                        showJunctionTrackCB.setText("Show splice junction track");
                        showJunctionTrackCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                showJunctionTrackCBActionPerformed(e);
                            }
                        });
                        panel3.add(showJunctionTrackCB);
                        showJunctionTrackCB.setBounds(new Rectangle(new Point(10, 15), showJunctionTrackCB.getPreferredSize()));

                        //---- junctionFlankingTextField ----
                        junctionFlankingTextField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                junctionFlankingTextFieldActionPerformed(e);
                            }
                        });
                        junctionFlankingTextField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                junctionFlankingTextFieldFocusLost(e);
                            }
                        });
                        panel3.add(junctionFlankingTextField);
                        junctionFlankingTextField.setBounds(445, 12, 105, junctionFlankingTextField.getPreferredSize().height);

                        //---- label15 ----
                        label15.setText("Minimum read flanking width:");
                        panel3.add(label15);
                        label15.setBounds(230, 18, 205, label15.getPreferredSize().height);

                        //---- label16 ----
                        label16.setText("Minimum junction coverage:");
                        panel3.add(label16);
                        label16.setBounds(new Rectangle(new Point(230, 45), label16.getPreferredSize()));

                        //---- junctionCoverageTextField ----
                        junctionCoverageTextField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                junctionCoverageTextFieldActionPerformed(e);
                            }
                        });
                        junctionCoverageTextField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                junctionCoverageTextFieldFocusLost(e);
                            }
                        });
                        panel3.add(junctionCoverageTextField);
                        junctionCoverageTextField.setBounds(445, 39, 105, junctionCoverageTextField.getPreferredSize().height);

                        { // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel3.getComponentCount(); i++) {
                                Rectangle bounds = panel3.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel3.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel3.setMinimumSize(preferredSize);
                            panel3.setPreferredSize(preferredSize);
                        }
                    }
                    jPanel1.add(panel3);
                    panel3.setBounds(5, 325, 755, 84);

                    { // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for (int i = 0; i < jPanel1.getComponentCount(); i++) {
                            Rectangle bounds = jPanel1.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = jPanel1.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        jPanel1.setMinimumSize(preferredSize);
                        jPanel1.setPreferredSize(preferredSize);
                    }
                }
                alignmentPanel.add(jPanel1);
                jPanel1.setBounds(0, 0, 760, 560);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < alignmentPanel.getComponentCount(); i++) {
                        Rectangle bounds = alignmentPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = alignmentPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    alignmentPanel.setMinimumSize(preferredSize);
                    alignmentPanel.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Alignments", alignmentPanel);


            //======== expressionPane ========
            {
                expressionPane.setLayout(null);

                //======== jPanel8 ========
                {

                    //---- expMapToGeneCB ----
                    expMapToGeneCB.setText("Map probes to genes");
                    expMapToGeneCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            expMapToGeneCBActionPerformed(e);
                        }
                    });

                    //---- jLabel24 ----
                    jLabel24.setText("Expression probe mapping options: ");

                    //---- expMapToLociCB ----
                    expMapToLociCB.setText("<html>Map probes to target loci");
                    expMapToLociCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            expMapToLociCBActionPerformed(e);
                        }
                    });

                    //---- jLabel21 ----
                    jLabel21.setText("<html><i>Note: Changes will not affect currently loaded datasets.");

                    GroupLayout jPanel8Layout = new GroupLayout(jPanel8);
                    jPanel8.setLayout(jPanel8Layout);
                    jPanel8Layout.setHorizontalGroup(
                            jPanel8Layout.createParallelGroup()
                                    .add(jPanel8Layout.createSequentialGroup()
                                            .add(jPanel8Layout.createParallelGroup()
                                                    .add(jPanel8Layout.createSequentialGroup()
                                                            .add(45, 45, 45)
                                                            .add(jPanel8Layout.createParallelGroup()
                                                                    .add(expMapToLociCB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                    .add(expMapToGeneCB)))
                                                    .add(jPanel8Layout.createSequentialGroup()
                                                            .addContainerGap()
                                                            .add(jPanel8Layout.createParallelGroup()
                                                                    .add(jPanel8Layout.createSequentialGroup()
                                                                            .add(24, 24, 24)
                                                                            .add(jLabel21, GroupLayout.PREFERRED_SIZE, 497, GroupLayout.PREFERRED_SIZE))
                                                                    .add(jLabel24))))
                                            .addContainerGap(179, Short.MAX_VALUE))
                    );
                    jPanel8Layout.setVerticalGroup(
                            jPanel8Layout.createParallelGroup()
                                    .add(jPanel8Layout.createSequentialGroup()
                                            .addContainerGap()
                                            .add(jLabel24)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(jLabel21, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(expMapToLociCB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .add(14, 14, 14)
                                            .add(expMapToGeneCB)
                                            .addContainerGap(158, Short.MAX_VALUE))
                    );
                }
                expressionPane.add(jPanel8);
                jPanel8.setBounds(10, 30, 720, 310);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < expressionPane.getComponentCount(); i++) {
                        Rectangle bounds = expressionPane.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = expressionPane.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    expressionPane.setMinimumSize(preferredSize);
                    expressionPane.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Probes", expressionPane);


            //======== advancedPanel ========
            {
                advancedPanel.setBorder(new EmptyBorder(1, 10, 1, 10));
                advancedPanel.setLayout(null);

                //======== jPanel3 ========
                {

                    //======== jPanel2 ========
                    {
                        jPanel2.setLayout(null);

                        //---- jLabel1 ----
                        jLabel1.setText("Genome Server URL");
                        jPanel2.add(jLabel1);
                        jLabel1.setBounds(new Rectangle(new Point(35, 47), jLabel1.getPreferredSize()));

                        //---- genomeServerURLTextField ----
                        genomeServerURLTextField.setText("jTextField1");
                        genomeServerURLTextField.setEnabled(false);
                        genomeServerURLTextField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                genomeServerURLTextFieldActionPerformed(e);
                            }
                        });
                        genomeServerURLTextField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                genomeServerURLTextFieldFocusLost(e);
                            }
                        });
                        jPanel2.add(genomeServerURLTextField);
                        genomeServerURLTextField.setBounds(191, 41, 494, genomeServerURLTextField.getPreferredSize().height);

                        //---- jLabel6 ----
                        jLabel6.setText("Data Registry URL");
                        jPanel2.add(jLabel6);
                        jLabel6.setBounds(new Rectangle(new Point(35, 81), jLabel6.getPreferredSize()));

                        //---- dataServerURLTextField ----
                        dataServerURLTextField.setEnabled(false);
                        dataServerURLTextField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                dataServerURLTextFieldActionPerformed(e);
                            }
                        });
                        dataServerURLTextField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                dataServerURLTextFieldFocusLost(e);
                            }
                        });
                        jPanel2.add(dataServerURLTextField);
                        dataServerURLTextField.setBounds(191, 75, 494, dataServerURLTextField.getPreferredSize().height);

                        //---- editServerPropertiesCB ----
                        editServerPropertiesCB.setText("Edit server properties");
                        editServerPropertiesCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                editServerPropertiesCBActionPerformed(e);
                            }
                        });
                        jPanel2.add(editServerPropertiesCB);
                        editServerPropertiesCB.setBounds(new Rectangle(new Point(6, 7), editServerPropertiesCB.getPreferredSize()));

                        //---- jButton1 ----
                        jButton1.setText("Reset to Defaults");
                        jButton1.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                jButton1ActionPerformed(e);
                            }
                        });
                        jPanel2.add(jButton1);
                        jButton1.setBounds(new Rectangle(new Point(190, 6), jButton1.getPreferredSize()));

                        //---- clearGenomeCacheButton ----
                        clearGenomeCacheButton.setText("Clear Genome Cache");
                        clearGenomeCacheButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                clearGenomeCacheButtonActionPerformed(e);
                            }
                        });
                        jPanel2.add(clearGenomeCacheButton);
                        clearGenomeCacheButton.setBounds(new Rectangle(new Point(6, 155), clearGenomeCacheButton.getPreferredSize()));

                        //---- genomeUpdateCB ----
                        genomeUpdateCB.setText("<html>Automatically check for updated genomes.    &nbsp;&nbsp;&nbsp;   <i>Most users should leave this checked.");
                        genomeUpdateCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                genomeUpdateCBActionPerformed(e);
                            }
                        });
                        jPanel2.add(genomeUpdateCB);
                        genomeUpdateCB.setBounds(new Rectangle(new Point(14, 121), genomeUpdateCB.getPreferredSize()));

                        { // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < jPanel2.getComponentCount(); i++) {
                                Rectangle bounds = jPanel2.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = jPanel2.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            jPanel2.setMinimumSize(preferredSize);
                            jPanel2.setPreferredSize(preferredSize);
                        }
                    }

                    //======== jPanel7 ========
                    {

                        //---- enablePortCB ----
                        enablePortCB.setText("Enable port");
                        enablePortCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                enablePortCBActionPerformed(e);
                            }
                        });

                        //---- portField ----
                        portField.setText("60151");
                        portField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                portFieldActionPerformed(e);
                            }
                        });
                        portField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                portFieldFocusLost(e);
                            }
                        });

                        //---- jLabel22 ----
                        jLabel22.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        jLabel22.setText("Enable port to send commands and http requests to IGV. ");

                        GroupLayout jPanel7Layout = new GroupLayout(jPanel7);
                        jPanel7.setLayout(jPanel7Layout);
                        jPanel7Layout.setHorizontalGroup(
                                jPanel7Layout.createParallelGroup()
                                        .add(jPanel7Layout.createSequentialGroup()
                                                .add(jPanel7Layout.createParallelGroup()
                                                        .add(jPanel7Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .add(enablePortCB)
                                                                .add(39, 39, 39)
                                                                .add(portField, GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE))
                                                        .add(jPanel7Layout.createSequentialGroup()
                                                                .add(48, 48, 48)
                                                                .add(jLabel22)))
                                                .addContainerGap(302, Short.MAX_VALUE))
                        );
                        jPanel7Layout.setVerticalGroup(
                                jPanel7Layout.createParallelGroup()
                                        .add(jPanel7Layout.createSequentialGroup()
                                                .add(28, 28, 28)
                                                .add(jPanel7Layout.createParallelGroup(GroupLayout.CENTER)
                                                        .add(enablePortCB)
                                                        .add(portField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.UNRELATED)
                                                .add(jLabel22)
                                                .addContainerGap(20, Short.MAX_VALUE))
                        );
                    }

                    GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
                    jPanel3.setLayout(jPanel3Layout);
                    jPanel3Layout.setHorizontalGroup(
                            jPanel3Layout.createParallelGroup()
                                    .add(jPanel3Layout.createSequentialGroup()
                                            .addContainerGap()
                                            .add(jPanel3Layout.createParallelGroup()
                                                    .add(jPanel7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .add(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                            .addContainerGap())
                    );
                    jPanel3Layout.setVerticalGroup(
                            jPanel3Layout.createParallelGroup()
                                    .add(jPanel3Layout.createSequentialGroup()
                                            .add(20, 20, 20)
                                            .add(jPanel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.RELATED, 20, Short.MAX_VALUE)
                                            .add(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    );
                }
                advancedPanel.add(jPanel3);
                jPanel3.setBounds(10, 0, 750, 330);

                //======== jPanel9 ========
                {

                    //---- useByteRangeCB ----
                    useByteRangeCB.setText("Use http byte-range requests");
                    useByteRangeCB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            useByteRangeCBActionPerformed(e);
                        }
                    });

                    //---- jLabel25 ----
                    jLabel25.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                    jLabel25.setText("<html>This option applies to certain \"Load from Server...\" tracks hosted at the Broad.    Disable this option if you are unable to load the phastCons conservation track under the hg18 annotations.");

                    GroupLayout jPanel9Layout = new GroupLayout(jPanel9);
                    jPanel9.setLayout(jPanel9Layout);
                    jPanel9Layout.setHorizontalGroup(
                            jPanel9Layout.createParallelGroup()
                                    .add(jPanel9Layout.createSequentialGroup()
                                            .addContainerGap()
                                            .add(jPanel9Layout.createParallelGroup()
                                                    .add(jPanel9Layout.createSequentialGroup()
                                                            .add(38, 38, 38)
                                                            .add(jLabel25, GroupLayout.PREFERRED_SIZE, 601, GroupLayout.PREFERRED_SIZE))
                                                    .add(useByteRangeCB))
                                            .addContainerGap(54, Short.MAX_VALUE))
                    );
                    jPanel9Layout.setVerticalGroup(
                            jPanel9Layout.createParallelGroup()
                                    .add(GroupLayout.TRAILING, jPanel9Layout.createSequentialGroup()
                                            .add(59, 59, 59)
                                            .add(useByteRangeCB, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(jLabel25, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                                            .addContainerGap())
                    );
                }
                advancedPanel.add(jPanel9);
                jPanel9.setBounds(30, 340, 710, 120);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < advancedPanel.getComponentCount(); i++) {
                        Rectangle bounds = advancedPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = advancedPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    advancedPanel.setMinimumSize(preferredSize);
                    advancedPanel.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Advanced", advancedPanel);


            //======== proxyPanel ========
            {
                proxyPanel.setLayout(new BoxLayout(proxyPanel, BoxLayout.X_AXIS));

                //======== jPanel15 ========
                {

                    //======== jPanel16 ========
                    {

                        //---- proxyUsernameField ----
                        proxyUsernameField.setText("jTextField1");
                        proxyUsernameField.setEnabled(false);
                        proxyUsernameField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                proxyUsernameFieldActionPerformed(e);
                            }
                        });
                        proxyUsernameField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                proxyUsernameFieldFocusLost(e);
                            }
                        });

                        //---- jLabel28 ----
                        jLabel28.setText("Username");

                        //---- authenticateProxyCB ----
                        authenticateProxyCB.setText("Authentication required");
                        authenticateProxyCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                authenticateProxyCBActionPerformed(e);
                            }
                        });

                        //---- jLabel29 ----
                        jLabel29.setText("Password");

                        //---- proxyPasswordField ----
                        proxyPasswordField.setText("jPasswordField1");
                        proxyPasswordField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                proxyPasswordFieldFocusLost(e);
                            }
                        });

                        GroupLayout jPanel16Layout = new GroupLayout(jPanel16);
                        jPanel16.setLayout(jPanel16Layout);
                        jPanel16Layout.setHorizontalGroup(
                                jPanel16Layout.createParallelGroup()
                                        .add(jPanel16Layout.createSequentialGroup()
                                                .add(jPanel16Layout.createParallelGroup()
                                                        .add(jPanel16Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .add(authenticateProxyCB))
                                                        .add(jPanel16Layout.createSequentialGroup()
                                                                .add(28, 28, 28)
                                                                .add(jPanel16Layout.createParallelGroup()
                                                                        .add(jLabel28)
                                                                        .add(jLabel29))
                                                                .add(37, 37, 37)
                                                                .add(jPanel16Layout.createParallelGroup(GroupLayout.LEADING, false)
                                                                        .add(proxyPasswordField)
                                                                        .add(proxyUsernameField, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE))))
                                                .addContainerGap(353, Short.MAX_VALUE))
                        );
                        jPanel16Layout.setVerticalGroup(
                                jPanel16Layout.createParallelGroup()
                                        .add(jPanel16Layout.createSequentialGroup()
                                                .add(17, 17, 17)
                                                .add(authenticateProxyCB)
                                                .addPreferredGap(LayoutStyle.RELATED)
                                                .add(jPanel16Layout.createParallelGroup(GroupLayout.BASELINE)
                                                        .add(jLabel28)
                                                        .add(proxyUsernameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.RELATED)
                                                .add(jPanel16Layout.createParallelGroup(GroupLayout.BASELINE)
                                                        .add(jLabel29)
                                                        .add(proxyPasswordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addContainerGap(47, Short.MAX_VALUE))
                        );
                    }

                    //======== jPanel17 ========
                    {

                        //---- proxyHostField ----
                        proxyHostField.setText("jTextField1");
                        proxyHostField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                proxyHostFieldActionPerformed(e);
                            }
                        });
                        proxyHostField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                proxyHostFieldFocusLost(e);
                            }
                        });

                        //---- proxyPortField ----
                        proxyPortField.setText("jTextField1");
                        proxyPortField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                proxyPortFieldActionPerformed(e);
                            }
                        });
                        proxyPortField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                proxyPortFieldFocusLost(e);
                            }
                        });

                        //---- jLabel27 ----
                        jLabel27.setText("Proxy port");

                        //---- jLabel23 ----
                        jLabel23.setText("Proxy host");

                        //---- useProxyCB ----
                        useProxyCB.setText("Use proxy");
                        useProxyCB.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                useProxyCBActionPerformed(e);
                            }
                        });

                        GroupLayout jPanel17Layout = new GroupLayout(jPanel17);
                        jPanel17.setLayout(jPanel17Layout);
                        jPanel17Layout.setHorizontalGroup(
                                jPanel17Layout.createParallelGroup()
                                        .add(jPanel17Layout.createSequentialGroup()
                                                .add(jPanel17Layout.createParallelGroup()
                                                        .add(jPanel17Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .add(jPanel17Layout.createParallelGroup()
                                                                        .add(jLabel27)
                                                                        .add(jLabel23))
                                                                .add(28, 28, 28)
                                                                .add(jPanel17Layout.createParallelGroup()
                                                                        .add(proxyPortField, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)
                                                                        .add(proxyHostField, GroupLayout.PREFERRED_SIZE, 485, GroupLayout.PREFERRED_SIZE)))
                                                        .add(jPanel17Layout.createSequentialGroup()
                                                                .add(9, 9, 9)
                                                                .add(useProxyCB)))
                                                .addContainerGap(21, Short.MAX_VALUE))
                        );
                        jPanel17Layout.setVerticalGroup(
                                jPanel17Layout.createParallelGroup()
                                        .add(GroupLayout.TRAILING, jPanel17Layout.createSequentialGroup()
                                                .addContainerGap(29, Short.MAX_VALUE)
                                                .add(useProxyCB)
                                                .add(18, 18, 18)
                                                .add(jPanel17Layout.createParallelGroup(GroupLayout.BASELINE)
                                                        .add(jLabel23)
                                                        .add(proxyHostField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.RELATED)
                                                .add(jPanel17Layout.createParallelGroup(GroupLayout.BASELINE)
                                                        .add(jLabel27)
                                                        .add(proxyPortField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addContainerGap())
                        );
                    }

                    //---- label3 ----
                    label3.setText("<html>Note:  do not use these settings unless you receive error or warning messages about server connections.  On most systems the correct settings will be automatically copied from your web browser.");

                    //---- clearProxySettingsButton ----
                    clearProxySettingsButton.setText("Clear All");
                    clearProxySettingsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            clearProxySettingsButtonActionPerformed(e);
                        }
                    });

                    GroupLayout jPanel15Layout = new GroupLayout(jPanel15);
                    jPanel15.setLayout(jPanel15Layout);
                    jPanel15Layout.setHorizontalGroup(
                            jPanel15Layout.createParallelGroup()
                                    .add(jPanel15Layout.createSequentialGroup()
                                            .add(jPanel15Layout.createParallelGroup()
                                                    .add(jPanel15Layout.createSequentialGroup()
                                                            .add(22, 22, 22)
                                                            .add(label3, GroupLayout.PREFERRED_SIZE, 630, GroupLayout.PREFERRED_SIZE))
                                                    .add(jPanel15Layout.createSequentialGroup()
                                                            .addContainerGap()
                                                            .add(jPanel15Layout.createParallelGroup()
                                                                    .add(jPanel16, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                    .add(jPanel17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                    .add(jPanel15Layout.createSequentialGroup()
                                                            .addContainerGap()
                                                            .add(clearProxySettingsButton)))
                                            .addContainerGap())
                    );
                    jPanel15Layout.setVerticalGroup(
                            jPanel15Layout.createParallelGroup()
                                    .add(jPanel15Layout.createSequentialGroup()
                                            .addContainerGap()
                                            .add(label3, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(jPanel17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .add(18, 18, 18)
                                            .add(jPanel16, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .add(18, 18, 18)
                                            .add(clearProxySettingsButton)
                                            .addContainerGap(100, Short.MAX_VALUE))
                    );
                }
                proxyPanel.add(jPanel15);
            }
            tabbedPane.addTab("Proxy", proxyPanel);


            //======== dbPanel ========
            {
                dbPanel.setLayout(null);

                //---- label17 ----
                label17.setText("Host:");
                dbPanel.add(label17);
                label17.setBounds(new Rectangle(new Point(45, 76), label17.getPreferredSize()));

                //---- label18 ----
                label18.setText("Port (Optional)");
                dbPanel.add(label18);
                label18.setBounds(new Rectangle(new Point(45, 205), label18.getPreferredSize()));

                //---- label19 ----
                label19.setText("Name:");
                dbPanel.add(label19);
                label19.setBounds(new Rectangle(new Point(45, 115), label19.getPreferredSize()));

                //---- dbHostField ----
                dbHostField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dbHostFieldActionPerformed(e);
                    }
                });
                dbHostField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        dbHostFieldFocusLost(e);
                    }
                });
                dbPanel.add(dbHostField);
                dbHostField.setBounds(110, 70, 430, dbHostField.getPreferredSize().height);

                //---- dbPortField ----
                dbPortField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dbPortFieldActionPerformed(e);
                    }
                });
                dbPortField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        dbPortFieldFocusLost(e);
                    }
                });
                dbPanel.add(dbPortField);
                dbPortField.setBounds(155, 200, 120, 28);

                //---- dbNameField ----
                dbNameField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dbNameFieldActionPerformed(e);
                    }
                });
                dbNameField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        dbNameFieldFocusLost(e);
                    }
                });
                dbPanel.add(dbNameField);
                dbNameField.setBounds(110, 110, 430, 28);

                //---- label20 ----
                label20.setText("<html><b>Database configuration  <i>(experimental, subject to change)");
                label20.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
                dbPanel.add(label20);
                label20.setBounds(new Rectangle(new Point(50, 20), label20.getPreferredSize()));

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < dbPanel.getComponentCount(); i++) {
                        Rectangle bounds = dbPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = dbPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    dbPanel.setMinimumSize(preferredSize);
                    dbPanel.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Database", dbPanel);

        }
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        //======== okCancelButtonPanel ========
        {

            //---- okButton ----
            okButton.setText("OK");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okButtonActionPerformed(e);
                }
            });
            okCancelButtonPanel.add(okButton);

            //---- cancelButton ----
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelButtonActionPerformed(e);
                }
            });
            okCancelButtonPanel.add(cancelButton);
        }
        contentPane.add(okCancelButtonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(expMapToGeneCB);
        buttonGroup1.add(expMapToLociCB);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        if (inputValidated) {

            checkForProbeChanges();

            lastSelectedIndex = tabbedPane.getSelectedIndex();

            // Store the changed preferences
            prefMgr.putAll(updatedPreferenceMap);

            if (updatedPreferenceMap.containsKey(PreferenceManager.PORT_ENABLED) ||
                    updatedPreferenceMap.containsKey(PreferenceManager.PORT_NUMBER) ||
                    updatedPreferenceMap.containsKey(PreferenceManager.PORT_MULTI_RANGE)) {
                CommandListener.halt();
                if (enablePortCB.isSelected()) {
                    int port = Integer.parseInt(updatedPreferenceMap.get(PreferenceManager.PORT_NUMBER));
                    int portMultiRange = Integer.parseInt(updatedPreferenceMap.get(PreferenceManager.PORT_MULTI_RANGE));
                    CommandListener.start(port, portMultiRange);
                }
            }

            checkForSAMChanges();

            // Overlays
            if (updateOverlays) {
                IGV.getInstance().getTrackManager().resetOverlayTracks();
            }

            // Proxies
            if (proxySettingsChanged) {
                HttpUtils.getInstance().updateProxySettings();
            }

            updatedPreferenceMap.clear();
            IGV.getInstance().repaint();
            setVisible(false);
        } else {
            resetValidation();
        }
    }


    private void fontChangeButtonActionPerformed(ActionEvent e) {
        Font defaultFont = FontManager.getDefaultFont();
        FontChooser chooser = new FontChooser(this, defaultFont);
        chooser.setModal(true);
        chooser.setVisible(true);
        if (!chooser.isCanceled()) {
            Font font = chooser.getSelectedFont();
            if (font != null) {
                prefMgr.put(PreferenceManager.DEFAULT_FONT_FAMILY, font.getFamily());
                prefMgr.put(PreferenceManager.DEFAULT_FONT_SIZE, String.valueOf(font.getSize()));
                int attrs = Font.PLAIN;
                if (font.isBold()) attrs = Font.BOLD;
                if (font.isItalic()) attrs |= Font.ITALIC;
                prefMgr.put(PreferenceManager.DEFAULT_FONT_ATTRIBUTE, String.valueOf(attrs));
                FontManager.updateDefaultFont();
                updateFontField();
                IGV.getInstance().repaint();
            }
        }
    }


    private void expMapToLociCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expMapToLociCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.PROBE_MAPPING_KEY, String.valueOf(expMapToGeneCB.isSelected()));
    }

    private void clearGenomeCacheButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearGenomeCacheButtonActionPerformed
        IGV.getInstance().getGenomeManager().clearGenomeCache();
        JOptionPane.showMessageDialog(this, "<html>Cached genomes have been removed.");
    }

    private void editServerPropertiesCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editServerPropertiesCBActionPerformed
        boolean edit = editServerPropertiesCB.isSelected();
        dataServerURLTextField.setEnabled(edit);
        genomeServerURLTextField.setEnabled(edit);
    }

    private void dataServerURLTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dataServerURLTextFieldFocusLost
        String attributeName = dataServerURLTextField.getText().trim();
        updatedPreferenceMap.put(PreferenceManager.DATA_SERVER_URL_KEY, attributeName);
    }

    private void dataServerURLTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataServerURLTextFieldActionPerformed
        String attributeName = dataServerURLTextField.getText().trim();
        updatedPreferenceMap.put(PreferenceManager.DATA_SERVER_URL_KEY, attributeName);
    }

    private void genomeServerURLTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_genomeServerURLTextFieldFocusLost
        String attributeName = genomeServerURLTextField.getText().trim();
        updatedPreferenceMap.put(PreferenceManager.GENOMES_SERVER_URL, attributeName);
    }

    private void genomeServerURLTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genomeServerURLTextFieldActionPerformed
        String attributeName = genomeServerURLTextField.getText().trim();
        updatedPreferenceMap.put(PreferenceManager.GENOMES_SERVER_URL, attributeName);
    }


    private void expandIconCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(PreferenceManager.SHOW_EXPAND_ICON, String.valueOf(expandIconCB.isSelected()));

    }

    private void normalizeCoverageCBFocusLost(FocusEvent e) {
        // TODO add your code here
    }


    private void junctionFlankingTextFieldFocusLost(FocusEvent e) {
        junctionFlankingTextFieldActionPerformed(null);
    }

    private void junctionFlankingTextFieldActionPerformed(ActionEvent e) {
        boolean valid = false;
        String flankingWidth = junctionFlankingTextField.getText().trim();
        try {
            int val = Integer.parseInt(flankingWidth);
            if (val >= 0) {
                valid = true;
                updatedPreferenceMap.put(PreferenceManager.SAM_JUNCTION_MIN_FLANKING_WIDTH, flankingWidth);
            }

        } catch (NumberFormatException numberFormatException) {
        }
        if (!valid && e != null) {
            junctionFlankingTextField.setText(prefMgr.get(PreferenceManager.SAM_JUNCTION_MIN_FLANKING_WIDTH));
            MessageUtils.showMessage("Flanking width must be a positive integer.");
        }
    }

    private void junctionCoverageTextFieldActionPerformed(ActionEvent e) {
        junctionCoverageTextFieldFocusLost(null);
    }

    private void junctionCoverageTextFieldFocusLost(FocusEvent e) {
        boolean valid = false;
        String minCoverage = junctionCoverageTextField.getText().trim();
        try {
            int val = Integer.parseInt(minCoverage);
            if (val >= 0) {
                valid = true;
                updatedPreferenceMap.put(PreferenceManager.SAM_JUNCTION_MIN_COVERAGE, minCoverage);
            }
        } catch (NumberFormatException numberFormatException) {
            valid = false;
        }
        if (!valid && e != null) {
            junctionCoverageTextField.setText(prefMgr.get(PreferenceManager.SAM_JUNCTION_MIN_COVERAGE));
            MessageUtils.showMessage("Minimum junction coverage must be a positive integer.");

        }
    }


    private void insertSizeThresholdFieldFocusLost(java.awt.event.FocusEvent evt) {
        this.insertSizeThresholdFieldActionPerformed(null);
    }

    private void insertSizeThresholdFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String insertThreshold = insertSizeThresholdField.getText().trim();
        try {
            Integer.parseInt(insertThreshold);
            updatedPreferenceMap.put(PreferenceManager.SAM_MAX_INSERT_SIZE_THRESHOLD, insertThreshold);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("BlastMapping quality threshold must be an integer.");
        }
    }


    private void insertSizeMinThresholdFieldFocusLost(FocusEvent e) {
        insertSizeMinThresholdFieldActionPerformed(null);
    }

    private void insertSizeMinThresholdFieldActionPerformed(ActionEvent e) {
        String insertThreshold = insertSizeMinThresholdField.getText().trim();
        try {
            Integer.parseInt(insertThreshold);
            updatedPreferenceMap.put(PreferenceManager.SAM_MIN_INSERT_SIZE_THRESHOLD, insertThreshold);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("BlastMapping quality threshold must be an integer.");
        }
    }


    private void mappingQualityThresholdFieldFocusLost(java.awt.event.FocusEvent evt) {
        mappingQualityThresholdFieldActionPerformed(null);
    }//GEN-LAST:event_mappingQualityThresholdFieldFocusLost

    private void mappingQualityThresholdFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String qualityThreshold = mappingQualityThresholdField.getText().trim();
        try {
            Integer.parseInt(qualityThreshold);
            updatedPreferenceMap.put(PreferenceManager.SAM_QUALITY_THRESHOLD, qualityThreshold);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage(
                    "BlastMapping quality threshold must be an integer.");
        }
    }

    private void samMaxLevelFieldFocusLost(java.awt.event.FocusEvent evt) {
        samMaxLevelFieldActionPerformed(null);
    }

    private void samMaxLevelFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String maxLevelString = samMaxDepthField.getText().trim();
        try {
            Integer.parseInt(maxLevelString);
            updatedPreferenceMap.put(PreferenceManager.SAM_MAX_LEVELS, maxLevelString);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Maximum read count must be an integer.");
        }
    }//GEN-LAST:event_samMaxLevelFieldActionPerformed

    private void samShadeMismatchedBaseCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(
                PreferenceManager.SAM_SHADE_BASE_QUALITY,
                String.valueOf(samShadeMismatchedBaseCB.isSelected()));
        samMinBaseQualityField.setEnabled(samShadeMismatchedBaseCB.isSelected());
        samMaxBaseQualityField.setEnabled(samShadeMismatchedBaseCB.isSelected());

    }

    private void showCenterLineCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(
                PreferenceManager.SAM_SHOW_CENTER_LINE,
                String.valueOf(showCenterLineCB.isSelected()));

    }


    private void zeroQualityAlignmentCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(
                PreferenceManager.SAM_FLAG_ZERO_QUALITY,
                String.valueOf(zeroQualityAlignmentCB.isSelected()));

    }


    private void genomeUpdateCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(
                PreferenceManager.AUTO_UPDATE_GENOMES,
                String.valueOf(this.genomeUpdateCB.isSelected()));
    }

    private void samFlagUnmappedPairCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(
                PreferenceManager.SAM_FLAG_UNMAPPED_PAIR,
                String.valueOf(samFlagUnmappedPairCB.isSelected()));
    }

    private void samShowDuplicatesCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(
                PreferenceManager.SAM_SHOW_DUPLICATES,
                String.valueOf(!samFilterDuplicatesCB.isSelected()));
    }

    private void showSoftClippedCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(
                PreferenceManager.SAM_SHOW_SOFT_CLIPPED,
                String.valueOf(showSoftClippedCB.isSelected()));
    }


    private void isizeComputeCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(
                PreferenceManager.SAM_COMPUTE_ISIZES,
                String.valueOf(isizeComputeCB.isSelected()));
    }


    private void insertSizeMinPercentileFieldFocusLost(FocusEvent e) {
        insertSizeMinPercentileFieldActionPerformed(null);
    }

    private void insertSizeMinPercentileFieldActionPerformed(ActionEvent e) {
        String valueString = insertSizeMinPercentileField.getText().trim();
        try {
            Double.parseDouble(valueString);
            updatedPreferenceMap.put(PreferenceManager.SAM_MIN_INSERT_SIZE_PERCENTILE, valueString);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Minimum insert size percentile must be a number.");
        }
    }


    private void insertSizeMaxPercentileFieldFocusLost(FocusEvent e) {
        insertSizeMaxPercentileFieldActionPerformed(null);
    }

    private void insertSizeMaxPercentileFieldActionPerformed(ActionEvent e) {
        String valueString = insertSizeMaxPercentileField.getText().trim();
        try {
            Double.parseDouble(valueString);
            updatedPreferenceMap.put(PreferenceManager.SAM_MAX_INSERT_SIZE_PERCENTILE, valueString);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Maximum insert size percentile must be a number.");
        }
    }


    private void samMaxWindowSizeFieldFocusLost(java.awt.event.FocusEvent evt) {
        String maxSAMWindowSize = samMaxWindowSizeField.getText().trim();
        try {
            Float.parseFloat(maxSAMWindowSize);
            updatedPreferenceMap.put(PreferenceManager.SAM_MAX_VISIBLE_RANGE, maxSAMWindowSize);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Visibility range must be a number.");
        }
    }

    private void samMaxWindowSizeFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String maxSAMWindowSize = String.valueOf(samMaxWindowSizeField.getText());
        try {
            Float.parseFloat(maxSAMWindowSize);
            updatedPreferenceMap.put(PreferenceManager.SAM_MAX_VISIBLE_RANGE, maxSAMWindowSize);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Visibility range must be a number.");
        }
    }


    private void seqResolutionThresholdActionPerformed(ActionEvent e) {
        samMaxWindowSizeFieldFocusLost(null);
    }

    private void seqResolutionThresholdFocusLost(FocusEvent e) {
        String seqResolutionSize = String.valueOf(seqResolutionThreshold.getText());
        try {
            float value = Float.parseFloat(seqResolutionSize.replace(",", ""));
            if (value < 1 || value > 10000) {
                MessageUtils.showMessage("Visibility range must be a number between 1 and 10000.");
            } else {
                updatedPreferenceMap.put(PreferenceManager.MAX_SEQUENCE_RESOLUTION, seqResolutionSize);
            }
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Visibility range must be a number between 1 and 10000.");
        }

    }


    private void chartDrawTrackNameCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(PreferenceManager.CHART_DRAW_TRACK_NAME,
                String.valueOf(chartDrawTrackNameCB.isSelected()));
    }

    private void autoscaleCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(PreferenceManager.CHART_AUTOSCALE, String.valueOf(autoscaleCB.isSelected()));
    }


    private void colorBordersCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(
                PreferenceManager.CHART_COLOR_BORDERS,
                String.valueOf(colorBordersCB.isSelected()));
    }

    private void bottomBorderCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(
                PreferenceManager.CHART_DRAW_BOTTOM_BORDER,
                String.valueOf(bottomBorderCB.isSelected()));
    }

    private void topBorderCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(
                PreferenceManager.CHART_DRAW_TOP_BORDER,
                String.valueOf(topBorderCB.isSelected()));
    }

    private void showAllHeatmapFeauresCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(
                PreferenceManager.CHART_SHOW_ALL_HEATMAP,
                String.valueOf(showAllHeatmapFeauresCB.isSelected()));
    }


    private void chooseMutationColorsButtonActionPerformed(ActionEvent e) {
        (new LegendDialog(IGV.getMainFrame(), true)).setVisible(true);
    }


    private void colorMutationsCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(PreferenceManager.COLOR_MUTATIONS, String.valueOf(
                colorCodeMutationsCB.isSelected()));
    }

    private void showOrphanedMutationsCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(PreferenceManager.SHOW_ORPHANED_MUTATIONS, String.valueOf(
                showOrphanedMutationsCB.isSelected()));
    }

    private void overlayTrackCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(PreferenceManager.OVERLAY_MUTATION_TRACKS, String.valueOf(
                overlayTrackCB.isSelected()));
        overlayAttributeTextField.setEnabled(overlayTrackCB.isSelected());
        showOrphanedMutationsCB.setEnabled(overlayTrackCB.isSelected());
        updateOverlays = true;
    }//GEN-LAST:event_overlayTrackCBActionPerformed


    private void overlayAttributeTextFieldFocusLost(java.awt.event.FocusEvent evt) {
        String attributeName = String.valueOf(overlayAttributeTextField.getText());
        if (attributeName != null) {
            attributeName = attributeName.trim();
        }
        updatedPreferenceMap.put(PreferenceManager.OVERLAY_ATTRIBUTE_KEY, attributeName);
        updateOverlays = true;
    }//GEN-LAST:event_overlayAttributeTextFieldFocusLost

    private void overlayAttributeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(PreferenceManager.OVERLAY_ATTRIBUTE_KEY, String.valueOf(
                overlayAttributeTextField.getText()));
        updateOverlays = true;
        // TODO add your handling code here:
    }//GEN-LAST:event_overlayAttributeTextFieldActionPerformed

    private void defaultTrackHeightFieldFocusLost(java.awt.event.FocusEvent evt) {
        String defaultTrackHeight = String.valueOf(defaultChartTrackHeightField.getText());
        try {
            Integer.parseInt(defaultTrackHeight);
            updatedPreferenceMap.put(PreferenceManager.TRACK_HEIGHT_KEY, defaultTrackHeight);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Track height must be an integer number.");
        }
    }//GEN-LAST:event_defaultTrackHeightFieldFocusLost

    private void defaultTrackHeightFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String defaultTrackHeight = String.valueOf(defaultChartTrackHeightField.getText());
        try {
            Integer.parseInt(defaultTrackHeight);
            updatedPreferenceMap.put(PreferenceManager.TRACK_HEIGHT_KEY, defaultTrackHeight);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Track height must be an integer number.");
        }
    }


    private void trackNameAttributeFieldFocusLost(java.awt.event.FocusEvent evt) {
        String attributeName = String.valueOf(trackNameAttributeField.getText());
        if (attributeName != null) {
            attributeName = attributeName.trim();
        }
        updatedPreferenceMap.put(PreferenceManager.TRACK_ATTRIBUTE_NAME_KEY, attributeName);
    }

    private void trackNameAttributeFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String attributeName = String.valueOf(trackNameAttributeField.getText());
        if (attributeName != null) {
            attributeName = attributeName.trim();
        }
        updatedPreferenceMap.put(PreferenceManager.TRACK_ATTRIBUTE_NAME_KEY, attributeName);
    }

    private void defaultChartTrackHeightFieldFocusLost(java.awt.event.FocusEvent evt) {
        defaultChartTrackHeightFieldActionPerformed(null);
    }

    private void defaultChartTrackHeightFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String defaultTrackHeight = String.valueOf(defaultChartTrackHeightField.getText());
        try {
            Integer.parseInt(defaultTrackHeight);
            updatedPreferenceMap.put(PreferenceManager.CHART_TRACK_HEIGHT_KEY, defaultTrackHeight);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Track height must be an integer number.");
        }
    }


    private void geneListFlankingFieldFocusLost(FocusEvent e) {
        geneListFlankingFieldActionPerformed(null);
    }


    private void geneListFlankingFieldActionPerformed(ActionEvent e) {
        String flankingRegion = String.valueOf(geneListFlankingField.getText());
        try {
            Integer.parseInt(flankingRegion);
            updatedPreferenceMap.put(PreferenceManager.FLANKING_REGION, flankingRegion);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Track height must be an integer number.");
        }

    }


    private void showAttributesDisplayCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        boolean state = ((JCheckBox) evt.getSource()).isSelected();
        updatedPreferenceMap.put(PreferenceManager.SHOW_ATTRIBUTE_VIEWS_KEY, String.valueOf(state));
        IGV.getInstance().doShowAttributeDisplay(state);
    }

    private void combinePanelsCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(PreferenceManager.SHOW_SINGLE_TRACK_PANE_KEY, String.valueOf(
                combinePanelsCB.isSelected()));
    }

    private void showMissingDataCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(PreferenceManager.SHOW_MISSING_DATA_KEY, String.valueOf(
                showMissingDataCB.isSelected()));
    }


    private void showRegionBoundariesCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(PreferenceManager.SHOW_REGION_BARS, String.valueOf(
                showRegionBoundariesCB.isSelected()));
    }

    private void filterVendorFailedReadsCBActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(
                PreferenceManager.SAM_FILTER_FAILED_READS,
                String.valueOf(filterFailedReadsCB.isSelected()));
    }


    private void samMinBaseQualityFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String baseQuality = samMinBaseQualityField.getText().trim();
        try {
            Integer.parseInt(baseQuality);
            updatedPreferenceMap.put(PreferenceManager.SAM_BASE_QUALITY_MIN, baseQuality);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Base quality must be an integer.");
        }
    }//GEN-LAST:event_samMinBaseQualityFieldActionPerformed

    private void samMinBaseQualityFieldFocusLost(java.awt.event.FocusEvent evt) {
        samMinBaseQualityFieldActionPerformed(null);
    }//GEN-LAST:event_samMinBaseQualityFieldFocusLost

    private void samMaxBaseQualityFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String baseQuality = samMaxBaseQualityField.getText().trim();
        try {
            Integer.parseInt(baseQuality);
            updatedPreferenceMap.put(PreferenceManager.SAM_BASE_QUALITY_MAX, baseQuality);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Base quality must be an integer.");
        }

    }//GEN-LAST:event_samMaxBaseQualityFieldActionPerformed

    private void samMaxBaseQualityFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_samMaxBaseQualityFieldFocusLost
        samMaxBaseQualityFieldActionPerformed(null);
    }//GEN-LAST:event_samMaxBaseQualityFieldFocusLost

    private void expMapToGeneCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expMapToGeneCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.PROBE_MAPPING_KEY, String.valueOf(expMapToGeneCB.isSelected()));

    }//GEN-LAST:event_expMapToGeneCBActionPerformed

    private void labelYAxisCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelYAxisCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.CHART_DRAW_Y_AXIS, String.valueOf(labelYAxisCB.isSelected()));
    }


    private void showCovTrackCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCovTrackCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.SAM_SHOW_COV_TRACK, String.valueOf(showCovTrackCB.isSelected()));
    }

    private void showJunctionTrackCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCovTrackCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.SAM_SHOW_JUNCTION_TRACK, String.valueOf(showJunctionTrackCB.isSelected()));
    }

    private void filterCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.SAM_FILTER_ALIGNMENTS, String.valueOf(filterCB.isSelected()));
        filterURL.setEnabled(filterCB.isSelected());
    }

    private void filterURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterURLActionPerformed
        updatedPreferenceMap.put(
                PreferenceManager.SAM_FILTER_URL,
                String.valueOf(filterURL.getText()));

    }

    private void filterURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_filterURLFocusLost
        filterURLActionPerformed(null);
    }

    private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed
        String portString = portField.getText().trim();
        try {
            Integer.parseInt(portString);
            updatedPreferenceMap.put(PreferenceManager.PORT_NUMBER, portString);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Port must be an integer.");
        }
    }

    private void portFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portFieldFocusLost
        portFieldActionPerformed(null);
    }

    private void enablePortCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enablePortCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.PORT_ENABLED, String.valueOf(enablePortCB.isSelected()));
        portField.setEnabled(enablePortCB.isSelected());

    }

    private void expandCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandCBActionPerformed
        updatedPreferenceMap.put(
                PreferenceManager.EXPAND_FEAUTRE_TRACKS,
                String.valueOf(expandCB.isSelected()));
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        PreferenceManager prefMgr = PreferenceManager.getInstance();
        genomeServerURLTextField.setEnabled(true);
        genomeServerURLTextField.setText(UIConstants.DEFAULT_SERVER_GENOME_ARCHIVE_LIST);
        updatedPreferenceMap.put(PreferenceManager.GENOMES_SERVER_URL, null);
        dataServerURLTextField.setEnabled(true);
        dataServerURLTextField.setText(PreferenceManager.DEFAULT_DATA_SERVER_URL);
        updatedPreferenceMap.put(PreferenceManager.DATA_SERVER_URL_KEY, null);
    }

    private void searchZoomCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchZoomCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.SEARCH_ZOOM, String.valueOf(searchZoomCB.isSelected()));
    }

    private void useByteRangeCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useByteRangeCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.USE_BYTE_RANGE, String.valueOf(useByteRangeCB.isSelected()));
    }

    private void showDatarangeCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showDatarangeCBActionPerformed
        updatedPreferenceMap.put(PreferenceManager.CHART_SHOW_DATA_RANGE, String.valueOf(showDatarangeCB.isSelected()));
    }

    private void showDatarangeCBFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_showDatarangeCBFocusLost
        showDatarangeCBActionPerformed(null);
    }

    private void snpThresholdFieldActionPerformed(java.awt.event.ActionEvent evt) {
        String snpThreshold = snpThresholdField.getText().trim();
        try {
            Double.parseDouble(snpThreshold);
            updatedPreferenceMap.put(PreferenceManager.SAM_ALLELE_THRESHOLD, snpThreshold);
        } catch (NumberFormatException numberFormatException) {
            inputValidated = false;
            MessageUtils.showMessage("Allele frequency threshold must be a number.");
        }
    }

    private void snpThresholdFieldFocusLost(java.awt.event.FocusEvent evt) {
        snpThresholdFieldActionPerformed(null);
    }

    private void normalizeCoverageCBActionPerformed(java.awt.event.ActionEvent evt) {
        updatedPreferenceMap.put(PreferenceManager.NORMALIZE_COVERAGE, String.valueOf(normalizeCoverageCB.isSelected()));
        portField.setEnabled(enablePortCB.isSelected());

    }


    // Proxy settings


    private void clearProxySettingsButtonActionPerformed(ActionEvent e) {
        if (MessageUtils.confirm("This will immediately clear all proxy settings.  Are you sure?")) {
            this.proxyHostField.setText("");
            this.proxyPortField.setText("");
            this.proxyUsernameField.setText("");
            this.proxyPasswordField.setText("");
            this.useProxyCB.setSelected(false);
            PreferenceManager.getInstance().clearProxySettings();
        }
    }


    private void useProxyCBActionPerformed(java.awt.event.ActionEvent evt) {
        proxySettingsChanged = true;
        boolean useProxy = useProxyCB.isSelected();
        boolean authenticateProxy = authenticateProxyCB.isSelected();
        portField.setEnabled(enablePortCB.isSelected());
        updateProxyState(useProxy, authenticateProxy);
        updatedPreferenceMap.put(PreferenceManager.USE_PROXY, String.valueOf(useProxy));

    }


    private void authenticateProxyCBActionPerformed(java.awt.event.ActionEvent evt) {
        proxySettingsChanged = true;
        boolean useProxy = useProxyCB.isSelected();
        boolean authenticateProxy = authenticateProxyCB.isSelected();
        portField.setEnabled(enablePortCB.isSelected());
        updateProxyState(useProxy, authenticateProxy);
        updatedPreferenceMap.put(PreferenceManager.PROXY_AUTHENTICATE, String.valueOf(authenticateProxy));

    }


    private void proxyHostFieldFocusLost(java.awt.event.FocusEvent evt) {
        proxyHostFieldActionPerformed(null);
    }

    private void proxyHostFieldActionPerformed(java.awt.event.ActionEvent evt) {
        proxySettingsChanged = true;
        updatedPreferenceMap.put(PreferenceManager.PROXY_HOST, proxyHostField.getText());
    }

    private void proxyPortFieldFocusLost(java.awt.event.FocusEvent evt) {
        proxyPortFieldActionPerformed(null);
    }

    private void proxyPortFieldActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            Integer.parseInt(proxyPortField.getText());
            proxySettingsChanged = true;
            updatedPreferenceMap.put(PreferenceManager.PROXY_PORT, proxyPortField.getText());
        } catch (NumberFormatException e) {
            MessageUtils.showMessage("Proxy port must be an integer.");
        }
    }

    // Username

    private void proxyUsernameFieldFocusLost(java.awt.event.FocusEvent evt) {
        proxyUsernameFieldActionPerformed(null);
    }

    private void proxyUsernameFieldActionPerformed(java.awt.event.ActionEvent evt) {
        proxySettingsChanged = true;
        String user = proxyUsernameField.getText();
        updatedPreferenceMap.put(PreferenceManager.PROXY_USER, user);

    }

    // Password

    private void proxyPasswordFieldFocusLost(java.awt.event.FocusEvent evt) {
        proxyPasswordFieldActionPerformed(null);
    }

    private void proxyPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {
        proxySettingsChanged = true;
        String pw = proxyPasswordField.getText();
        String pwEncoded = Utilities.base64Encode(pw);
        updatedPreferenceMap.put(PreferenceManager.PROXY_PW, pwEncoded);

    }


    private void updateProxyState(boolean useProxy, boolean authenticateProxy) {
        proxyHostField.setEnabled(useProxy);
        proxyPortField.setEnabled(useProxy);
        proxyUsernameField.setEnabled(useProxy && authenticateProxy);
        proxyPasswordField.setEnabled(useProxy && authenticateProxy);
    }

    private void resetValidation() {
        // Assume valid input until proven otherwise
        inputValidated = true;
    }

    private void dbHostFieldFocusLost(FocusEvent e) {
        dbHostFieldActionPerformed(null);
    }

    private void dbHostFieldActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(PreferenceManager.DB_HOST, dbHostField.getText());
    }

    private void dbNameFieldFocusLost(FocusEvent e) {
        dbNameFieldActionPerformed(null);
    }

    private void dbNameFieldActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(PreferenceManager.DB_NAME, dbNameField.getText());
    }

    private void dbPortFieldActionPerformed(ActionEvent e) {
        dbPortFieldFocusLost(null);
    }

    private void dbPortFieldFocusLost(FocusEvent e) {

        String portText = dbPortField.getText().trim();
        if (portText.length() == 0) {
            updatedPreferenceMap.put(PreferenceManager.DB_PORT, "-1");
        } else {
            try {
                Integer.parseInt(portText);
                updatedPreferenceMap.put(PreferenceManager.DB_PORT, portText);
            } catch (NumberFormatException e1) {
                updatedPreferenceMap.put(PreferenceManager.DB_PORT, "-1");
            }
        }

    }


    /*
   *    Object selection = geneMappingFile.getSelectedItem();
  String filename = (selection == null ? null : selection.toString().trim());
  updatedPreferenceMap.put(
  PreferenceManager.USER_PROBE_MAP_KEY,
  filename);
   * */

    private void initValues() {
        combinePanelsCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SHOW_SINGLE_TRACK_PANE_KEY));
        //drawExonNumbersCB.setSelected(preferenceManager.getDrawExonNumbers());

        showRegionBoundariesCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SHOW_REGION_BARS));
        defaultChartTrackHeightField.setText(prefMgr.get(PreferenceManager.CHART_TRACK_HEIGHT_KEY));
        defaultTrackHeightField.setText(prefMgr.get(PreferenceManager.TRACK_HEIGHT_KEY));
        showOrphanedMutationsCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SHOW_ORPHANED_MUTATIONS));
        overlayAttributeTextField.setText(prefMgr.get(PreferenceManager.OVERLAY_ATTRIBUTE_KEY));
        overlayTrackCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.OVERLAY_MUTATION_TRACKS));
        showMissingDataCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SHOW_MISSING_DATA_KEY));
        colorCodeMutationsCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.COLOR_MUTATIONS));
        overlayAttributeTextField.setEnabled(overlayTrackCB.isSelected());
        showOrphanedMutationsCB.setEnabled(overlayTrackCB.isSelected());
        seqResolutionThreshold.setText(prefMgr.get(PreferenceManager.MAX_SEQUENCE_RESOLUTION));


        geneListFlankingField.setText(prefMgr.get(PreferenceManager.FLANKING_REGION));

        enablePortCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.PORT_ENABLED));
        portField.setText(String.valueOf(prefMgr.getAsInt(PreferenceManager.PORT_NUMBER)));
        portField.setEnabled(enablePortCB.isSelected());

        expandCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.EXPAND_FEAUTRE_TRACKS));
        searchZoomCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SEARCH_ZOOM));

        useByteRangeCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.USE_BYTE_RANGE));
        showAttributesDisplayCheckBox.setSelected(prefMgr.getAsBoolean(PreferenceManager.SHOW_ATTRIBUTE_VIEWS_KEY));
        trackNameAttributeField.setText(prefMgr.get(PreferenceManager.TRACK_ATTRIBUTE_NAME_KEY));

        genomeServerURLTextField.setText(prefMgr.getGenomeListURL());
        dataServerURLTextField.setText(prefMgr.getDataServerURL());

        // Chart panel
        topBorderCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.CHART_DRAW_TOP_BORDER));
        bottomBorderCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.CHART_DRAW_BOTTOM_BORDER));
        colorBordersCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.CHART_COLOR_BORDERS));
        chartDrawTrackNameCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.CHART_DRAW_TRACK_NAME));
        autoscaleCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.CHART_AUTOSCALE));
        showDatarangeCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.CHART_SHOW_DATA_RANGE));
        labelYAxisCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.CHART_DRAW_Y_AXIS));
        showAllHeatmapFeauresCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.CHART_SHOW_ALL_HEATMAP));

        samMaxWindowSizeField.setText(prefMgr.get(PreferenceManager.SAM_MAX_VISIBLE_RANGE));
        samMaxDepthField.setText(prefMgr.get(PreferenceManager.SAM_MAX_LEVELS));
        mappingQualityThresholdField.setText(prefMgr.get(PreferenceManager.SAM_QUALITY_THRESHOLD));
        insertSizeThresholdField.setText(prefMgr.get(PreferenceManager.SAM_MAX_INSERT_SIZE_THRESHOLD));
        insertSizeMinThresholdField.setText(prefMgr.get(PreferenceManager.SAM_MIN_INSERT_SIZE_THRESHOLD));
        insertSizeMinPercentileField.setText(prefMgr.get(PreferenceManager.SAM_MIN_INSERT_SIZE_PERCENTILE));
        insertSizeMaxPercentileField.setText(prefMgr.get(PreferenceManager.SAM_MAX_INSERT_SIZE_PERCENTILE));
        snpThresholdField.setText((String.valueOf(prefMgr.getAsFloat(PreferenceManager.SAM_ALLELE_THRESHOLD))));
        //samShowZeroQualityCB.setSelected(samPrefs.isShowZeroQuality());
        samFilterDuplicatesCB.setSelected(!prefMgr.getAsBoolean(PreferenceManager.SAM_SHOW_DUPLICATES));
        filterFailedReadsCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_FILTER_FAILED_READS));
        showSoftClippedCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_SHOW_SOFT_CLIPPED));
        samFlagUnmappedPairCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_FLAG_UNMAPPED_PAIR));
        showCenterLineCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_SHOW_CENTER_LINE));
        samShadeMismatchedBaseCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_SHADE_BASE_QUALITY));
        samMinBaseQualityField.setText((String.valueOf(prefMgr.getAsInt(PreferenceManager.SAM_BASE_QUALITY_MIN))));
        samMaxBaseQualityField.setText((String.valueOf(prefMgr.getAsInt(PreferenceManager.SAM_BASE_QUALITY_MAX))));
        samMinBaseQualityField.setEnabled(samShadeMismatchedBaseCB.isSelected());
        samMaxBaseQualityField.setEnabled(samShadeMismatchedBaseCB.isSelected());
        showCovTrackCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_SHOW_COV_TRACK));
        isizeComputeCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_COMPUTE_ISIZES));
        zeroQualityAlignmentCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_FLAG_ZERO_QUALITY));
        filterCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_FILTER_ALIGNMENTS));
        if (prefMgr.get(PreferenceManager.SAM_FILTER_URL) != null) {
            filterURL.setText(prefMgr.get(PreferenceManager.SAM_FILTER_URL));
        }

        showJunctionTrackCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SAM_SHOW_JUNCTION_TRACK));
        junctionFlankingTextField.setText(prefMgr.get(PreferenceManager.SAM_JUNCTION_MIN_FLANKING_WIDTH));
        junctionCoverageTextField.setText(prefMgr.get(PreferenceManager.SAM_JUNCTION_MIN_COVERAGE));

        genomeUpdateCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.AUTO_UPDATE_GENOMES));

        final boolean mapProbesToGenes = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.PROBE_MAPPING_KEY);
        expMapToGeneCB.setSelected(mapProbesToGenes);
        expMapToLociCB.setSelected(!mapProbesToGenes);

        normalizeCoverageCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.NORMALIZE_COVERAGE));


        expandIconCB.setSelected(prefMgr.getAsBoolean(PreferenceManager.SHOW_EXPAND_ICON));

        boolean useProxy = prefMgr.getAsBoolean(PreferenceManager.USE_PROXY);
        useProxyCB.setSelected(useProxy);

        boolean authenticateProxy = prefMgr.getAsBoolean(PreferenceManager.PROXY_AUTHENTICATE);
        authenticateProxyCB.setSelected(authenticateProxy);

        proxyHostField.setText(prefMgr.get(PreferenceManager.PROXY_HOST, ""));
        proxyPortField.setText(prefMgr.get(PreferenceManager.PROXY_PORT, ""));
        proxyUsernameField.setText(prefMgr.get(PreferenceManager.PROXY_USER, ""));
        String pwCoded = prefMgr.get(PreferenceManager.PROXY_PW, "");
        proxyPasswordField.setText(Utilities.base64Decode(pwCoded));

        backgroundColorPanel.setBackground(
                PreferenceManager.getInstance().getAsColor(PreferenceManager.BACKGROUND_COLOR));

        dbHostField.setText(prefMgr.get(PreferenceManager.DB_HOST));
        dbNameField.setText(prefMgr.get(PreferenceManager.DB_NAME));
        String portText = prefMgr.get(PreferenceManager.DB_PORT);
        if (!portText.equals("-1")) {
            dbPortField.setText(portText);
        }

        updateFontField();

        updateProxyState(useProxy, authenticateProxy);
    }

    private void updateFontField() {
        Font font = FontManager.getDefaultFont();
        StringBuffer buf = new StringBuffer();
        buf.append(font.getFamily());
        if (font.isBold()) {
            buf.append(" bold");
        }
        if (font.isItalic()) {
            buf.append(" italic");
        }
        buf.append(" " + font.getSize());
        defaultFontField.setText(buf.toString());

    }

    private void checkForSAMChanges() {
        WaitCursorManager.CursorToken token = WaitCursorManager.showWaitCursor();
        try {
            boolean reloadSAM = false;
            for (String key : SAM_PREFERENCE_KEYS) {
                if (updatedPreferenceMap.containsKey(key)) {
                    reloadSAM = true;
                    break;
                }
            }
            if (reloadSAM) {
                IGV.getInstance().getTrackManager().reloadSAMTracks();
            }
        } catch (NumberFormatException numberFormatException) {
        } finally {
            WaitCursorManager.removeWaitCursor(token);
        }
    }

    private void checkForProbeChanges() {
        if (updatedPreferenceMap.containsKey(PreferenceManager.PROBE_MAPPING_KEY)) {
            ProbeToLocusMap.getInstance().clearProbeMappings();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                PreferencesEditor dialog = new PreferencesEditor(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    //TODO move this to another class,  or resource bundle
    static String overlayText = "<html>These options control the treatment of mutation tracks.  " +
            "Mutation data may optionally<br>be overlayed on other tracks that have a matching attribute value " +
            "from the sample info <br>file. " +
            "This is normally an attribute that identifies a sample or patient. The attribute key <br>is specified in the" +
            "text field below.";

    public String getOverlayText() {
        return overlayText;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JTabbedPane tabbedPane;
    private JPanel generalPanel;
    private JPanel jPanel10;
    private JLabel missingDataExplanation;
    private JCheckBox showMissingDataCB;
    private JCheckBox combinePanelsCB;
    private JCheckBox showAttributesDisplayCheckBox;
    private JCheckBox searchZoomCB;
    private JLabel zoomToFeatureExplanation;
    private JLabel label4;
    private JTextField geneListFlankingField;
    private JLabel zoomToFeatureExplanation2;
    private JLabel label5;
    private JLabel label6;
    private JTextField seqResolutionThreshold;
    private JLabel label10;
    private JTextField defaultFontField;
    private JButton fontChangeButton;
    private JCheckBox showRegionBoundariesCB;
    private JLabel label7;
    private JPanel backgroundColorPanel;
    private JButton resetBackgroundButton;
    private JPanel tracksPanel;
    private JPanel jPanel6;
    private JLabel jLabel5;
    private JTextField defaultChartTrackHeightField;
    private JLabel trackNameAttributeLabel;
    private JTextField trackNameAttributeField;
    private JLabel missingDataExplanation2;
    private JLabel jLabel8;
    private JTextField defaultTrackHeightField;
    private JLabel missingDataExplanation4;
    private JLabel missingDataExplanation5;
    private JLabel missingDataExplanation3;
    private JCheckBox expandCB;
    private JCheckBox normalizeCoverageCB;
    private JLabel missingDataExplanation8;
    private JCheckBox expandIconCB;
    private JPanel overlaysPanel;
    private JPanel jPanel5;
    private JLabel jLabel3;
    private JTextField overlayAttributeTextField;
    private JCheckBox overlayTrackCB;
    private JLabel jLabel2;
    private JLabel jLabel4;
    private JCheckBox colorCodeMutationsCB;
    private JButton chooseMutationColorsButton;
    private JLabel label11;
    private JCheckBox showOrphanedMutationsCB;
    private JLabel label12;
    private JPanel chartPanel;
    private JPanel jPanel4;
    private JCheckBox topBorderCB;
    private JLabel label1;
    private JCheckBox chartDrawTrackNameCB;
    private JCheckBox bottomBorderCB;
    private JLabel jLabel7;
    private JCheckBox colorBordersCB;
    private JCheckBox labelYAxisCB;
    private JCheckBox autoscaleCB;
    private JLabel jLabel9;
    private JCheckBox showDatarangeCB;
    private JPanel panel1;
    private JLabel label13;
    private JCheckBox showAllHeatmapFeauresCB;
    private JLabel label14;
    private JPanel alignmentPanel;
    private JPanel jPanel1;
    private JPanel jPanel11;
    private JTextField samMaxDepthField;
    private JLabel jLabel11;
    private JLabel jLabel16;
    private JTextField mappingQualityThresholdField;
    private JLabel jLabel14;
    private JLabel jLabel13;
    private JLabel jLabel15;
    private JTextField samMaxWindowSizeField;
    private JLabel jLabel12;
    private JLabel jLabel26;
    private JTextField snpThresholdField;
    private JPanel jPanel12;
    private JTextField samMinBaseQualityField;
    private JCheckBox samShadeMismatchedBaseCB;
    private JTextField samMaxBaseQualityField;
    private JCheckBox showCovTrackCB;
    private JCheckBox samFilterDuplicatesCB;
    private JLabel jLabel19;
    private JCheckBox filterCB;
    private JTextField filterURL;
    private JCheckBox samFlagUnmappedPairCB;
    private JCheckBox filterFailedReadsCB;
    private JLabel label2;
    private JCheckBox showSoftClippedCB;
    private JCheckBox showCenterLineCB;
    private JCheckBox zeroQualityAlignmentCB;
    private JPanel panel2;
    private JCheckBox isizeComputeCB;
    private JLabel jLabel17;
    private JTextField insertSizeMinThresholdField;
    private JLabel jLabel20;
    private JTextField insertSizeThresholdField;
    private JLabel jLabel30;
    private JLabel jLabel18;
    private JTextField insertSizeMinPercentileField;
    private JTextField insertSizeMaxPercentileField;
    private JLabel label8;
    private JLabel label9;
    private JPanel panel3;
    private JCheckBox showJunctionTrackCB;
    private JTextField junctionFlankingTextField;
    private JLabel label15;
    private JLabel label16;
    private JTextField junctionCoverageTextField;
    private JPanel expressionPane;
    private JPanel jPanel8;
    private JRadioButton expMapToGeneCB;
    private JLabel jLabel24;
    private JRadioButton expMapToLociCB;
    private JLabel jLabel21;
    private JPanel advancedPanel;
    private JPanel jPanel3;
    private JPanel jPanel2;
    private JLabel jLabel1;
    private JTextField genomeServerURLTextField;
    private JLabel jLabel6;
    private JTextField dataServerURLTextField;
    private JCheckBox editServerPropertiesCB;
    private JButton jButton1;
    private JButton clearGenomeCacheButton;
    private JCheckBox genomeUpdateCB;
    private JPanel jPanel7;
    private JCheckBox enablePortCB;
    private JTextField portField;
    private JLabel jLabel22;
    private JPanel jPanel9;
    private JCheckBox useByteRangeCB;
    private JLabel jLabel25;
    private JPanel proxyPanel;
    private JPanel jPanel15;
    private JPanel jPanel16;
    private JTextField proxyUsernameField;
    private JLabel jLabel28;
    private JCheckBox authenticateProxyCB;
    private JLabel jLabel29;
    private JPasswordField proxyPasswordField;
    private JPanel jPanel17;
    private JTextField proxyHostField;
    private JTextField proxyPortField;
    private JLabel jLabel27;
    private JLabel jLabel23;
    private JCheckBox useProxyCB;
    private JLabel label3;
    private JButton clearProxySettingsButton;
    private JPanel dbPanel;
    private JLabel label17;
    private JLabel label18;
    private JLabel label19;
    private JTextField dbHostField;
    private JTextField dbPortField;
    private JTextField dbNameField;
    private JLabel label20;
    private ButtonPanel okCancelButtonPanel;
    private JButton okButton;
    private JButton cancelButton;
    // End of variables declaration//GEN-END:variables

    public boolean isCanceled() {
        return canceled;
    }

    /**
     * List of keys that affect the alignments loaded.  This list is used to trigger a reload, if required.
     * Not all alignment preferences need trigger a reload, this is a subset.
     */
    static java.util.List<String> SAM_PREFERENCE_KEYS = Arrays.asList(
            PreferenceManager.SAM_QUALITY_THRESHOLD,
            PreferenceManager.SAM_FILTER_ALIGNMENTS,
            PreferenceManager.SAM_FILTER_URL,
            PreferenceManager.SAM_MAX_VISIBLE_RANGE,
            PreferenceManager.SAM_SHOW_DUPLICATES,
            PreferenceManager.SAM_SHOW_SOFT_CLIPPED,
            PreferenceManager.SAM_MAX_LEVELS,
            PreferenceManager.SAM_MAX_READS,
            PreferenceManager.SAM_FILTER_FAILED_READS
    );
}
