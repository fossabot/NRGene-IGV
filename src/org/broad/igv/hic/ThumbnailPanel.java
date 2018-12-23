package org.broad.igv.hic;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import javax.swing.JComponent;

/**
 * @author jrobinso
 * @date Aug 2, 2010
 */
public class ThumbnailPanel extends JComponent implements Serializable {

    private MainWindow mainWindow;

    Image image;

    public static final AlphaComposite ALPHA_COMP = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
    private double yScale;
    private double xScale;
    Point lastPoint = null;

    private Rectangle innerRectangle;


    public ThumbnailPanel() {

         addMouseListener(new MouseAdapter() {


            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int xBP = (int) (mouseEvent.getX() * xScale);
                int yBP = (int) (mouseEvent.getY() * yScale);
                mainWindow.center(xBP, yBP);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (innerRectangle != null && innerRectangle.contains(mouseEvent.getPoint())) {
                    lastPoint = mouseEvent.getPoint();
                    setCursor(MainWindow.fistCursor);
                }

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                lastPoint = null;
                setCursor(Cursor.getDefaultCursor());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if (lastPoint != null) {
                    int dxBP = ((int) ((mouseEvent.getX() - lastPoint.x) * xScale));
                    int dyBP = ((int) ((mouseEvent.getY() - lastPoint.y) * xScale));
                    mainWindow.moveBy(dxBP, dyBP);
                    lastPoint = mouseEvent.getPoint();
                }


            }
        });

    }

    public void setImage(Image image) {
        this.image = image;
        int maxLen = Math.max(mainWindow.xContext.getChrLength(), mainWindow.yContext.getChrLength());
        xScale = ((double) maxLen) / getWidth();
        yScale = xScale;
    }

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public String getName() {
        return null;
    }

    public void setName(String nm) {

    }

    @Override
    protected void paintComponent(Graphics g) {

        if (image != null) {
            g.drawImage(image, 0, 0, null);
            renderVisibleWindow((Graphics2D) g);
        }
    }

    private void renderVisibleWindow(Graphics2D g) {


        if (mainWindow != null && mainWindow.xContext != null) {

            int wPixels = mainWindow.getHeatmapPanel().getWidth();
            int hPixels = mainWindow.getHeatmapPanel().getHeight();

            int originX = mainWindow.xContext.getOrigin();
            int x = (int) (originX / xScale);

            int originY = mainWindow.yContext.getOrigin();
            int y = (int) (originY / yScale);

            int wBP = (int) (mainWindow.xContext.getScale() * wPixels);
            int w = (int) (wBP / xScale);

            int yBP = (int) (mainWindow.yContext.getScale() * hPixels);
            int h = (int) (yBP / yScale);

            if (w < 4) {
                int delta = 4 - w;
                x -= delta / 2;
                w = 4;
            }
            if (h < 4) {
                int delta = 4 - h;
                y -= delta / 2;
                h = 4;
            }

            Rectangle outerRectangle = new Rectangle(0, 0, getBounds().width, getBounds().height);
            innerRectangle = new Rectangle(x, y, w, h);
            Shape shape = new SquareDonut(outerRectangle, innerRectangle);

            g.setColor(Color.gray);
            g.setComposite(ALPHA_COMP);
            g.fill(shape);

            g.draw(innerRectangle);
        }
    }

    private static class SquareDonut implements Shape {
        private final Area area;

        public SquareDonut(Rectangle outerRectangle, Rectangle innerRectangle) {
            this.area = new Area(outerRectangle);
            area.subtract(new Area(innerRectangle));
        }

        public Rectangle getBounds() {
            return area.getBounds();
        }

        public Rectangle2D getBounds2D() {
            return area.getBounds2D();
        }

        public boolean contains(double v, double v1) {
            return area.contains(v, v1);
        }

        public boolean contains(Point2D point2D) {
            return area.contains(point2D);
        }

        public boolean intersects(double v, double v1, double v2, double v3) {
            return area.intersects(v, v1, v2, v3);
        }

        public boolean intersects(Rectangle2D rectangle2D) {
            return area.intersects(rectangle2D);
        }

        public boolean contains(double v, double v1, double v2, double v3) {
            return area.contains(v, v1, v2, v3);
        }

        public boolean contains(Rectangle2D rectangle2D) {
            return area.contains(rectangle2D);
        }

        public PathIterator getPathIterator(AffineTransform affineTransform) {
            return area.getPathIterator(affineTransform);
        }

        public PathIterator getPathIterator(AffineTransform affineTransform, double v) {
            return area.getPathIterator(affineTransform, v);
        }
    }
}
