/*
 * Created by JFormDesigner on Mon Aug 02 22:04:22 EDT 2010
 */

package org.broad.igv.hic;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import org.broad.igv.hic.data.Chromosome;
import org.broad.igv.hic.data.Dataset;
import org.broad.igv.hic.data.DatasetReader;
import org.broad.igv.hic.data.Matrix;
import org.broad.igv.hic.data.MatrixZoomData;
import org.broad.igv.ui.util.IconFactory;
import org.broad.igv.util.stream.IGVSeekableStreamFactory;
import org.broad.tribble.util.SeekableStream;

/**
 * @author James Robinson
 */
public class MainWindow extends JFrame {

    public static final int MIN_BIN_WIDTH = 2;
    public static Cursor fistCursor;
    //int refMaxCount = 500;

    public static int[] zoomBinSizes = {2500000, 1000000, 500000, 250000, 100000, 50000, 25000, 10000, 5000, 2500, 1000};
    public static final int MAX_ZOOM = 10;

    //private int len;
    public Context xContext;
    public Context yContext;
    Dataset dataset;
    MatrixZoomData zd;
    private Chromosome[] chromosomes;

    ColorScale colorScale;
    private int[] chromosomeBoundaries;


    public static void main(String[] args) throws IOException {

        final MainWindow mainWindow = new MainWindow();
        mainWindow.setVisible(true);
        mainWindow.setSize(870, 870);


    }


    public void createCursors() {
        BufferedImage handImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);

        // Make backgroun transparent
        Graphics2D g = handImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, 32, 32);
        g.fill(rect);

        // Draw hand image in middle
        g = handImage.createGraphics();
        g.drawImage(IconFactory.getInstance().getIcon(IconFactory.IconID.FIST).getImage(), 0, 0, null);
        MainWindow.fistCursor = getToolkit().createCustomCursor(handImage, new Point(8, 6), "Move");
    }


    public MainWindow() throws IOException {

        int initialMaxCount = 50000;
        colorScale = new ColorScale();
        colorScale.maxCount = initialMaxCount;
        colorScale.background = Color.white;

        initComponents();

        // setup the glass pane to display a wait cursor when visible, and to grab all mouse events
        rootPane.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        rootPane.getGlassPane().addMouseListener(new MouseAdapter() {});


        createCursors();

        maxRange.setText(String.valueOf(colorScale.maxCount));
        minRange.setText(String.valueOf(colorScale.minCount));

        thumbnailPanel.setMainWindow(this);

        getHeatmapPanel().setSize(500, 500);

        thumbnailPanel.setPreferredSize(new Dimension(100, 100));

        //2500000, 1000000, 500000, 250000, 100000, 50000, 25000, 10000, 5000, 2500, 1000
        // TODO -- these should be read from the data file  (zd.binSize)
        ZoomLabel[] zooms = new ZoomLabel[]{
                new ZoomLabel("2.5 mb", 0),
                new ZoomLabel("1   mb", 1),
                new ZoomLabel("500 kb", 2),
                new ZoomLabel("250 kb", 3),
                new ZoomLabel("100 kb", 4),
                new ZoomLabel("50  kb", 5),
                new ZoomLabel("25  kb", 6),
                new ZoomLabel("10  kb", 7),
                new ZoomLabel("5   kb", 8),
                new ZoomLabel("2.5 kb", 9),
                new ZoomLabel("1   kb", 10)};
       // zoomComboBox.setModel(new DefaultComboBoxModel(zooms));

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }

    public HeatmapPanel getHeatmapPanel() {
        return heatmapPanel;
    }

    public boolean isWholeGenome() {
        return xContext != null && xContext.getChromosome().getName().equals("All");
    }

    public Chromosome[] getChromosomes() {
        return chromosomes;
    }

    /**
     * Chromosome "0" is whole genome
     *
     * @param chromosomes
     */
    public void setChromosomes(Chromosome[] chromosomes) {
        this.chromosomes = chromosomes;
        chromosomeBoundaries = new int[chromosomes.length - 1];
        long bound = 0;
        for (int i = 1; i < chromosomes.length; i++) {
            Chromosome c = chromosomes[i];
            bound += (c.getSize() / 1000);
            getChromosomeBoundaries()[i-1] = (int) bound;
        }
    }

    public int[] getChromosomeBoundaries() {
        return chromosomeBoundaries;
    }

    public void setSelectedChromosomes(Chromosome xChrom, Chromosome yChrom) {
        chrBox1.setSelectedIndex(yChrom.getIndex());
        chrBox2.setSelectedIndex(xChrom.getIndex());
        refreshChromosomes();
    }

    class ZoomLabel {
        String label;
        int zoom;

        ZoomLabel(String label, int zoom) {
            this.label = label;
            this.zoom = zoom;
        }

        public String toString() {
            return label;
        }
    }

    private void load(String file) throws IOException {
        if (file.endsWith("hic")) {
            SeekableStream ss = IGVSeekableStreamFactory.getStreamFor(file);
            dataset = (new DatasetReader(ss)).read();
            setChromosomes(dataset.getChromosomes());
            chrBox1.setModel(new DefaultComboBoxModel(getChromosomes()));
            chrBox2.setModel(new DefaultComboBoxModel(getChromosomes()));
        } else {
            // error -- unknown file type
        }
        setTitle(file);
        xContext = null;
        yContext = null;
        refreshChromosomes();
    }

    private void refreshChromosomes() {

        getRootPane().getGlassPane().setVisible(true);

        SwingWorker worker = new SwingWorker() {
            @Override
            protected void done() {
                getGlassPane().setVisible(false);
                getContentPane().repaint();
            }

            @Override
            protected Object doInBackground() throws Exception {


                Chromosome chr1 = (Chromosome) chrBox1.getSelectedItem();
                Chromosome chr2 = (Chromosome) chrBox2.getSelectedItem();

                //zoomComboBox.setEnabled(chr1.getIndex() != 0);
                zoomInButton.setEnabled(chr1.getIndex() != 0);
                zoomOutButton.setEnabled(chr1.getIndex() != 0);

                int t1 = chr1.getIndex();
                int t2 = chr2.getIndex();

                if (t1 > t2) {
                    Chromosome tmp = chr2;
                    chr2 = chr1;
                    chr1 = tmp;
                }

                getHeatmapPanel().clearTileCache();
                if ((xContext != null && xContext.getChromosome().getIndex() == chr2.getIndex()) &&
                        (yContext != null && yContext.getChromosome().getIndex() == chr1.getIndex())) {
                    repaint();
                } else {

                    xContext = new Context(chr2);
                    yContext = new Context(chr1);
                    rulerPanel2.setFrame(xContext, HiCRulerPanel.Orientation.HORIZONTAL);
                    rulerPanel1.setFrame(yContext, HiCRulerPanel.Orientation.VERTICAL);

                    Matrix m = dataset.getMatrix(chr1, chr2);
                    if (m == null) {
                    } else {
                        setInitialZoom();
                    }


                    Image thumbnail = getHeatmapPanel().getThumbnailImage(zd, thumbnailPanel.getWidth(), thumbnailPanel.getHeight());
                    thumbnailPanel.setImage(thumbnail);
                }

                return null;
            }

        };

        worker.execute();

    }

    private void setInitialZoom() {


        int len = (Math.max(xContext.getChrLength(), yContext.getChrLength()));
        int pixels = getHeatmapPanel().getWidth();
        int maxNBins = pixels;

        if (xContext.getChromosome().getName().equals("All")) {
            setZoom(0, -1, -1);
        } else {// Find right zoom level
            int bp_bin = len / maxNBins;
            int initialZoom = zoomBinSizes.length - 1;
            for (int z = 1; z < zoomBinSizes.length; z++) {
                if (zoomBinSizes[z] < bp_bin) {
                    initialZoom = z - 1;
                    break;
                }
            }

            setZoom(initialZoom, -1, -1);
        }
    }


    /**
     * Change zoom level while staying centered on current location.
     *
     * @param newZoom
     */
    public void setZoom(int newZoom) {
        newZoom = Math.max(0, Math.min(newZoom, MAX_ZOOM));
        int centerLocationX = (int) xContext.getChromosomePosition(getHeatmapPanel().getWidth() / 2);
        int centerLocationY = (int) yContext.getChromosomePosition(getHeatmapPanel().getHeight() / 2);
        setZoom(newZoom, centerLocationX, centerLocationY);

        zoomInButton.setEnabled(newZoom < MAX_ZOOM);
        zoomOutButton.setEnabled(newZoom > 0);
    }

    /**
     * Change zoom level and recenter
     *
     * @param newZoom
     * @param centerLocationX center X location in base pairs
     * @param centerLocationY center Y location in base pairs
     */
    public void setZoom(int newZoom, int centerLocationX, int centerLocationY) {

        if (newZoom < 0 || newZoom > MAX_ZOOM) return;

        Chromosome chr1 = xContext.getChromosome();
        Chromosome chr2 = yContext.getChromosome();
        zd = dataset.getMatrix(chr1, chr2).getZoomData(newZoom);

        int newBinSize = zd.getBinSize();

        // Scale in basepairs per screen pixel
        double scale = (double) newBinSize;

        double xScaleMax = (double) xContext.getChrLength() / getHeatmapPanel().getWidth();
        double yScaleMax = (double) yContext.getChrLength() / getHeatmapPanel().getWidth();
        double scaleMax = Math.min(xScaleMax, yScaleMax);

        scale = Math.min(scale, scaleMax);

        xContext.setZoom(newZoom, scale);
        yContext.setZoom(newZoom, scale);

        //zoomComboBox.setSelectedIndex(newZoom);

        center(centerLocationX, centerLocationY);

        getHeatmapPanel().clearTileCache();

        repaint();

    }


    public void zoomTo(double xBP, double yBP, double scale) {

        // Find zoom level with resolution
        int zoom = zoomBinSizes.length - 1;
        for (int z = 1; z < zoomBinSizes.length; z++) {
            if (zoomBinSizes[z] < scale) {
                zoom = z - 1;
                break;
            }
        }

        Chromosome chr1 = xContext.getChromosome();
        Chromosome chr2 = yContext.getChromosome();
        zd = dataset.getMatrix(chr1, chr2).getZoomData(zoom);

        xContext.setZoom(zoom, scale);
        yContext.setZoom(zoom, scale);

        //zoomComboBox.setSelectedIndex(zoom);

        xContext.setOrigin((int) xBP);
        yContext.setOrigin((int) yBP);
        getHeatmapPanel().clearTileCache();

        repaint();
    }


    public void center(int centerLocationX, int centerLocationY) {

        if (centerLocationX < 0) {
            xContext.setOrigin(0);
        } else {
            int binSize = zd.getBinSize();
            double w = (getHeatmapPanel().getWidth() * xContext.getScale());
            xContext.setOrigin((int) (centerLocationX - w / 2));
        }
        if (centerLocationY < 0) {
            yContext.setOrigin(0);
        } else {
            int binSize = zd.getBinSize();
            double h = (getHeatmapPanel().getHeight() * yContext.getScale());
            yContext.setOrigin((int) (centerLocationY - h / 2));
        }
        repaint();
    }


    public void moveBy(int dx, int dy) {

        int maxX = (int) (xContext.getChrLength() - xContext.getScale() * getHeatmapPanel().getWidth());
        int maxY = (int) (yContext.getChrLength() - yContext.getScale() * getHeatmapPanel().getHeight());

        int x = Math.max(0, Math.min(maxX, xContext.getOrigin() + dx));
        int y = Math.max(0, Math.min(maxY, yContext.getOrigin() + dy));

        xContext.setOrigin(x);
        yContext.setOrigin(y);
        repaint();
    }


    private void heatmapPanelMouseDragged(MouseEvent e) {
        // TODO add your code here
    }

    private void refreshButtonActionPerformed(ActionEvent e) {
        refreshChromosomes();
    }

    private void loadMenuItemActionPerformed(ActionEvent e) {
        FileDialog dlg = new FileDialog(this);
        dlg.setMode(FileDialog.LOAD);
        dlg.setVisible(true);
        String file = dlg.getFile();
        if (file != null) {
            try {
                File f = new File(dlg.getDirectory(), dlg.getFile());
                load(f.getAbsolutePath());
            } catch (IOException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    private void loadFromURLActionPerformed(ActionEvent e) {
        String url = JOptionPane.showInputDialog("Enter URL: ");
        if (url != null) {
            try {
                load(url);
            } catch (IOException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    private void exitActionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
        System.exit(0);
    }

    private void loadDmelDatasetActionPerformed(ActionEvent e) {
        try {
            colorScale.maxCount = 20000;
            maxRange.setText("20000");
            minRange.setText("0");
            zd = null;
            load("http://iwww.broadinstitute.org/igvdata/hic/dmel/selected_formatted.hic");

        } catch (IOException e1) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e1.getMessage());
        }
    }

    private void loadGMActionPerformed(ActionEvent e) {
        try {
            colorScale.maxCount = 100;
            maxRange.setText("100");
            minRange.setText("0");
            zd = null;
            load("http://www.broadinstitute.org/igvdata/hic/hg18/GM.summary.binned.hic");
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e1.getMessage());
        }
    }

    private void load562ActionPerformed(ActionEvent e) {
        try {
            colorScale.maxCount = 100;
            maxRange.setText("100");
            minRange.setText("0");
            zd = null;
            load("http://www.broadinstitute.org/igvdata/hic/hg18/K562.summary.binned.hic");
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e1.getMessage());
        }
    }


    private void minRangeFocusLost(FocusEvent e) {
        minRangeActionPerformed(null);
    }

    private void maxRangeFocusLost(FocusEvent e) {
        maxRangeActionPerformed(null);
    }

    private void minRangeActionPerformed(ActionEvent e) {
        try {
            int min = Integer.parseInt(minRange.getText());
            colorScale.minCount = min;
            heatmapPanel.clearTileCache();
            repaint();

        } catch (NumberFormatException ex) {

        }
    }

    private void maxRangeActionPerformed(ActionEvent e) {
        try {
            int max = Integer.parseInt(maxRange.getText());
            colorScale.maxCount = max;
            heatmapPanel.clearTileCache();
            repaint();
        } catch (NumberFormatException ex) {

        }
    }

    private void chrBox1ActionPerformed(ActionEvent e) {
        if(chrBox1.getSelectedIndex() == 0) {
            chrBox2.setSelectedIndex(0);
            refreshChromosomes();
        }
    }

    private void chrBox2ActionPerformed(ActionEvent e) {
        if(chrBox2.getSelectedIndex() == 0) {
            chrBox1.setSelectedIndex(0);
            refreshChromosomes();
        }
    }


    private void zoomOutButtonActionPerformed(ActionEvent e) {
        int z = xContext.getZoom();
        int newZoom = Math.max(z - 1, 0);
        setZoom(newZoom);
        repaint();
    }

    private void zoomInButtonActionPerformed(ActionEvent e) {
        int z = xContext.getZoom();
        int newZoom = Math.min(z+1, MAX_ZOOM );
        setZoom(newZoom);
        repaint();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel2 = new JPanel();
        panel4 = new JPanel();
        chrSelectionPanel = new JPanel();
        chrBox1 = new JComboBox();
        chrBox2 = new JComboBox();
        refreshButton = new JButton();
        panel1 = new JPanel();
        label1 = new JLabel();
        minRange = new JTextField();
        label3 = new JLabel();
        maxRange = new JTextField();
        panel7 = new JPanel();
        label2 = new JLabel();
        zoomOutButton = new JButton();
        zoomInButton = new JButton();
        panel3 = new JPanel();
        panel5 = new JPanel();
        spacerLeft = new JPanel();
        rulerPanel2 = new HiCRulerPanel(this);
        spacerRight = new JPanel();
        heatmapPanel = new HeatmapPanel(this);
        rulerPanel1 = new HiCRulerPanel(this);
        panel8 = new JPanel();
        thumbnailPanel = new ThumbnailPanel();
        menuBar1 = new JMenuBar();
        fileMenu = new JMenu();
        loadMenuItem = new JMenuItem();
        loadFromURL = new JMenuItem();
        loadDmelDataset = new JMenuItem();
        loadGM = new JMenuItem();
        load562 = new JMenuItem();
        exit = new JMenuItem();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel2 ========
        {
            panel2.setLayout(new BorderLayout());

            //======== panel4 ========
            {
                panel4.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                panel4.setLayout(new FlowLayout(FlowLayout.LEFT, 25, 5));

                //======== chrSelectionPanel ========
                {
                    chrSelectionPanel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                    chrSelectionPanel.setLayout(new FlowLayout());

                    //---- chrBox1 ----
                    chrBox1.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            chrBox1ActionPerformed(e);
                        }
                    });
                    chrSelectionPanel.add(chrBox1);

                    //---- chrBox2 ----
                    chrBox2.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            chrBox2ActionPerformed(e);
                        }
                    });
                    chrSelectionPanel.add(chrBox2);

                    //---- refreshButton ----
                    refreshButton.setText("Refresh");
                    refreshButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            refreshButtonActionPerformed(e);
                        }
                    });
                    chrSelectionPanel.add(refreshButton);
                }
                panel4.add(chrSelectionPanel);

                //======== panel1 ========
                {
                    panel1.setBorder(new LineBorder(Color.black));
                    panel1.setLayout(new FlowLayout());

                    //---- label1 ----
                    label1.setText("Color Range");
                    label1.setHorizontalAlignment(SwingConstants.CENTER);
                    label1.setToolTipText("Range of color scale in counts per mega-base squared.");
                    panel1.add(label1);

                    //---- minRange ----
                    minRange.setPreferredSize(new Dimension(64, 26));
                    minRange.setMinimumSize(new Dimension(14, 26));
                    minRange.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            minRangeFocusLost(e);
                        }
                    });
                    minRange.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            minRangeActionPerformed(e);
                        }
                    });
                    panel1.add(minRange);

                    //---- label3 ----
                    label3.setText("to");
                    panel1.add(label3);

                    //---- maxRange ----
                    maxRange.setPreferredSize(new Dimension(64, 26));
                    maxRange.setMinimumSize(new Dimension(14, 26));
                    maxRange.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            maxRangeFocusLost(e);
                        }
                    });
                    maxRange.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            maxRangeActionPerformed(e);
                        }
                    });
                    panel1.add(maxRange);
                }
                panel4.add(panel1);

                //======== panel7 ========
                {
                    panel7.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                    panel7.setLayout(new FlowLayout());

                    //---- label2 ----
                    label2.setText("Zoom ");
                    panel7.add(label2);

                    //---- zoomOutButton ----
                    zoomOutButton.setText("-");
                    zoomOutButton.setMinimumSize(new Dimension(5, 5));
                    zoomOutButton.setPreferredSize(new Dimension(29, 29));
                    zoomOutButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            zoomOutButtonActionPerformed(e);
                        }
                    });
                    panel7.add(zoomOutButton);

                    //---- zoomInButton ----
                    zoomInButton.setText("+");
                    zoomInButton.setPreferredSize(new Dimension(25, 29));
                    zoomInButton.setMinimumSize(new Dimension(5, 29));
                    zoomInButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            zoomInButtonActionPerformed(e);
                        }
                    });
                    panel7.add(zoomInButton);
                }
                panel4.add(panel7);
            }
            panel2.add(panel4, BorderLayout.NORTH);

            //======== panel3 ========
            {
                panel3.setLayout(new BorderLayout());

                //======== panel5 ========
                {
                    panel5.setLayout(new BorderLayout());

                    //======== spacerLeft ========
                    {
                        spacerLeft.setMaximumSize(new Dimension(50, 50));
                        spacerLeft.setMinimumSize(new Dimension(50, 50));
                        spacerLeft.setPreferredSize(new Dimension(50, 50));
                        spacerLeft.setLayout(null);
                    }
                    panel5.add(spacerLeft, BorderLayout.WEST);

                    //---- rulerPanel2 ----
                    rulerPanel2.setMaximumSize(new Dimension(4000, 50));
                    rulerPanel2.setMinimumSize(new Dimension(1, 50));
                    rulerPanel2.setPreferredSize(new Dimension(1, 50));
                    rulerPanel2.setBorder(null);
                    panel5.add(rulerPanel2, BorderLayout.CENTER);

                    //======== spacerRight ========
                    {
                        spacerRight.setMinimumSize(new Dimension(120, 0));
                        spacerRight.setPreferredSize(new Dimension(120, 0));
                        spacerRight.setMaximumSize(new Dimension(120, 32767));
                        spacerRight.setLayout(null);
                    }
                    panel5.add(spacerRight, BorderLayout.EAST);
                }
                panel3.add(panel5, BorderLayout.NORTH);

                //---- heatmapPanel ----
                heatmapPanel.setBorder(LineBorder.createBlackLineBorder());
                heatmapPanel.setMaximumSize(new Dimension(500, 500));
                heatmapPanel.setMinimumSize(new Dimension(500, 500));
                heatmapPanel.setPreferredSize(new Dimension(500, 500));
                heatmapPanel.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        heatmapPanelMouseDragged(e);
                    }
                });
                panel3.add(heatmapPanel, BorderLayout.CENTER);

                //---- rulerPanel1 ----
                rulerPanel1.setMaximumSize(new Dimension(50, 4000));
                rulerPanel1.setPreferredSize(new Dimension(50, 500));
                rulerPanel1.setBorder(null);
                rulerPanel1.setMinimumSize(new Dimension(50, 1));
                panel3.add(rulerPanel1, BorderLayout.WEST);

                //======== panel8 ========
                {
                    panel8.setMaximumSize(new Dimension(120, 100));
                    panel8.setBorder(new EmptyBorder(0, 10, 0, 0));
                    panel8.setLayout(new FlowLayout());

                    //---- thumbnailPanel ----
                    thumbnailPanel.setMaximumSize(new Dimension(100, 100));
                    thumbnailPanel.setMinimumSize(new Dimension(100, 100));
                    thumbnailPanel.setPreferredSize(new Dimension(100, 100));
                    thumbnailPanel.setBorder(LineBorder.createBlackLineBorder());
                    panel8.add(thumbnailPanel);
                }
                panel3.add(panel8, BorderLayout.EAST);
            }
            panel2.add(panel3, BorderLayout.CENTER);
        }
        contentPane.add(panel2, BorderLayout.CENTER);

        //======== menuBar1 ========
        {

            //======== fileMenu ========
            {
                fileMenu.setText("File");

                //---- loadMenuItem ----
                loadMenuItem.setText("Load...");
                loadMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        loadMenuItemActionPerformed(e);
                    }
                });
                fileMenu.add(loadMenuItem);

                //---- loadFromURL ----
                loadFromURL.setText("Load from URL ...");
                loadFromURL.setName("loadFromURL");
                loadFromURL.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        loadFromURLActionPerformed(e);
                    }
                });
                fileMenu.add(loadFromURL);
                fileMenu.addSeparator();

                //---- loadDmelDataset ----
                loadDmelDataset.setText("Fly");
                loadDmelDataset.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        loadDmelDatasetActionPerformed(e);
                    }
                });
                fileMenu.add(loadDmelDataset);

                //---- loadGM ----
                loadGM.setText("GM cell line (human)");
                loadGM.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        loadGMActionPerformed(e);
                    }
                });
                fileMenu.add(loadGM);

                //---- load562 ----
                load562.setText("K562 cell line (human)");
                load562.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        load562ActionPerformed(e);
                    }
                });
                fileMenu.add(load562);
                fileMenu.addSeparator();

                //---- exit ----
                exit.setText("Exit");
                exit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        exitActionPerformed(e);
                    }
                });
                fileMenu.add(exit);
            }
            menuBar1.add(fileMenu);
        }
        contentPane.add(menuBar1, BorderLayout.NORTH);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel2;
    private JPanel panel4;
    private JPanel chrSelectionPanel;
    private JComboBox chrBox1;
    private JComboBox chrBox2;
    private JButton refreshButton;
    private JPanel panel1;
    private JLabel label1;
    private JTextField minRange;
    private JLabel label3;
    private JTextField maxRange;
    private JPanel panel7;
    private JLabel label2;
    private JButton zoomOutButton;
    private JButton zoomInButton;
    private JPanel panel3;
    private JPanel panel5;
    private JPanel spacerLeft;
    private HiCRulerPanel rulerPanel2;
    private JPanel spacerRight;
    private HeatmapPanel heatmapPanel;
    private HiCRulerPanel rulerPanel1;
    private JPanel panel8;
    ThumbnailPanel thumbnailPanel;
    private JMenuBar menuBar1;
    private JMenu fileMenu;
    private JMenuItem loadMenuItem;
    private JMenuItem loadFromURL;
    private JMenuItem loadDmelDataset;
    private JMenuItem loadGM;
    private JMenuItem load562;
    private JMenuItem exit;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


}
