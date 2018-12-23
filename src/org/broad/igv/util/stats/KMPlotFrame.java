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
 * Created by JFormDesigner on Sat Dec 04 19:26:01 EST 2010
 */

package org.broad.igv.util.stats;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.broad.igv.track.AttributeManager;
import org.broad.igv.track.Track;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author jrobinso
 */
public class KMPlotFrame extends JFrame {

    Collection<Track> tracks;
    private XYPlot plot;

    // TODO -- control to set this
    int maxTime = 60;

    public KMPlotFrame(Collection<Track> tracks) {

        //setLocationRelativeTo(owner);

        this.tracks = tracks;

        initComponents();

        censurColumnControl.addItem("");
        sampleColumnControl.addItem("");
        survivalColumnControl.addItem("");
        groupByControl.addItem("");
        for (String key : AttributeManager.getInstance().getAttributeNames()) {
            censurColumnControl.addItem(key);
            sampleColumnControl.addItem(key);
            survivalColumnControl.addItem(key);
            groupByControl.addItem(key);
        }

        censurColumnControl.setSelectedItem("CENSURED");
        sampleColumnControl.setSelectedItem("LINKING_ID");
        survivalColumnControl.setSelectedItem("SURVIVAL (DAYS)");
        groupByControl.setSelectedItem("SUBTYPE");


        XYDataset dataset = updateDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "Months",
                "Survival",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        XYStepRenderer renderer = new XYStepRenderer();
        plot = chart.getXYPlot();
        plot.setRenderer(renderer);
        ChartPanel plotPanel = new ChartPanel(chart);
        contentPanel.add(plotPanel, BorderLayout.CENTER);

    }


    private void closeButtonActionPerformed(ActionEvent e) {
        setVisible(false);
    }

    public XYDataset updateDataset() {

        ArrayList<DataPoint> dataPoints = new ArrayList(tracks.size());
        HashSet<String> participants = new HashSet();
        for (Track t : tracks) {
            try {
                // Get the participant (sample) attribute value for this track
                String participant = t.getAttributeValue(sampleColumnControl.getSelectedItem().toString());

                if (!participants.contains(participant)) {   // Don't add same participant twice.
                    participants.add(participant);

                    // Get the survival time.  TODO -- we need to know the units,  just assuming days for now.
                    String survivalString = t.getAttributeValue(survivalColumnControl.getSelectedItem().toString());
                    int survivalDays = Integer.parseInt(survivalString);
                    int survival = survivalDays;

                    // Is the patient censured at the end of the survival period?
                    String censureString = t.getAttributeValue(censurColumnControl.getSelectedItem().toString());
                    boolean censured = censureString != null && censureString.equals("1");

                    String group = t.getAttributeValue(groupByControl.getSelectedItem().toString());
                    dataPoints.add(new DataPoint(participant, survival, censured, group));
                } else {
                    // TODO -- check consistency of participant data
                }
            } catch (NumberFormatException e) {
                // Just skip
            }
        }

        // Segregate by group
        Map<String, ArrayList<DataPoint>> map = new HashMap();
        for (DataPoint dp : dataPoints) {
            String g = dp.group;
            ArrayList<DataPoint> pts = map.get(g);
            if (pts == null) {
                pts = new ArrayList();
                map.put(g, pts);
            }
            pts.add(dp);
        }


        //XYSeries series1;
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (Map.Entry<String, ArrayList<DataPoint>> entry : map.entrySet()) {

            java.util.List<DataPoint> pts = entry.getValue();
            Collections.sort(pts);

            int[] time = new int[pts.size()];
            boolean[] censured = new boolean[pts.size()];
            for (int i = 0; i < pts.size(); i++) {
                int months = Math.max(1, pts.get(i).time / 30);
                time[i] = months;
                censured[i] = pts.get(i).censured;
            }

            java.util.List<KaplanMeierEstimator.Interval> controlIntervals = KaplanMeierEstimator.compute(time, censured);


            XYSeries series1 = new XYSeries(entry.getKey());
            for (KaplanMeierEstimator.Interval interval : controlIntervals) {
                if (interval.getEnd() < 60)
                    series1.add(interval.getEnd(), interval.getCumulativeSurvival());
            }
            dataset.addSeries(series1);
        }

        return dataset;

    }

    private void updateButtonActionPerformed(ActionEvent e) {
        XYDataset dataset = updateDataset();
        plot.setDataset(dataset);
        repaint();

    }

    public static class DataPoint implements Comparable<DataPoint> {
        String participant;
        int time;
        boolean censured;
        String group;

        DataPoint(String participant, int time, boolean censured, String group) {
            this.censured = censured;
            this.participant = participant;
            this.group = group;
            this.time = time;
        }

        public int compareTo(DataPoint dataPoint) {
            return time - dataPoint.time;
        }
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        censurColumnControl = new JComboBox();
        sampleColumnControl = new JComboBox();
        survivalColumnControl = new JComboBox();
        groupByControl = new JComboBox();
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        label4 = new JLabel();
        updateButton = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Kaplan-Meier Plot");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new BorderLayout());

                //======== panel1 ========
                {
                    panel1.setLayout(null);
                    panel1.add(censurColumnControl);
                    censurColumnControl.setBounds(145, 62, 215, censurColumnControl.getPreferredSize().height);
                    panel1.add(sampleColumnControl);
                    sampleColumnControl.setBounds(145, 0, 215, sampleColumnControl.getPreferredSize().height);
                    panel1.add(survivalColumnControl);
                    survivalColumnControl.setBounds(145, 31, 215, survivalColumnControl.getPreferredSize().height);
                    panel1.add(groupByControl);
                    groupByControl.setBounds(145, 93, 215, groupByControl.getPreferredSize().height);

                    //---- label1 ----
                    label1.setText("Sample column");
                    panel1.add(label1);
                    label1.setBounds(5, 5, 115, label1.getPreferredSize().height);

                    //---- label2 ----
                    label2.setText("Survival column");
                    panel1.add(label2);
                    label2.setBounds(5, 36, 115, 16);

                    //---- label3 ----
                    label3.setText("Censure column");
                    panel1.add(label3);
                    label3.setBounds(5, 67, 115, 16);

                    //---- label4 ----
                    label4.setText("Group by");
                    panel1.add(label4);
                    label4.setBounds(5, 98, 115, 16);

                    //---- updateButton ----
                    updateButton.setText("Update Plot");
                    updateButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            updateButtonActionPerformed(e);
                        }
                    });
                    panel1.add(updateButton);
                    updateButton.setBounds(385, 90, 145, updateButton.getPreferredSize().height);

                    { // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for (int i = 0; i < panel1.getComponentCount(); i++) {
                            Rectangle bounds = panel1.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = panel1.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        panel1.setMinimumSize(preferredSize);
                        panel1.setPreferredSize(preferredSize);
                    }
                }
                contentPanel.add(panel1, BorderLayout.NORTH);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setSize(565, 510);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel panel1;
    private JComboBox censurColumnControl;
    private JComboBox sampleColumnControl;
    private JComboBox survivalColumnControl;
    private JComboBox groupByControl;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JLabel label4;
    private JButton updateButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


}
