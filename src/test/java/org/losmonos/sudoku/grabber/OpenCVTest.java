package org.losmonos.sudoku.grabber;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.google.common.io.Files;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.collect.Range.closed;
import static java.lang.Math.PI;
import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.*;


public class OpenCVTest {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /*
      *Try to find all lines intersection using houghlines
      */
    public Mat propperUnwrappTest2(Mat m) {
        Mat bw = new Mat(m.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(m, bw, Imgproc.COLOR_RGB2GRAY);

        Imgproc.GaussianBlur(bw, bw, new Size(5, 5), 0);

        int lowThreshold = 100;
        double ratio = 2.4;

        bw = adjustBrigth(bw);

        Mat dst = new Mat();
        bw.copyTo(dst, getMask(bw.clone()));
        Imgproc.Canny(dst, bw, 50, 150, 3, false);

        Mat lines = new Mat();
        Imgproc.HoughLines(bw, lines, 1, PI / 180, 200);

        lines = lines.reshape(1, lines.cols());

        // Normalize direction
        Mat v = new Mat();
        Mat h = new Mat();

        for (int i = 0; i < lines.rows(); i++) {
            double p = lines.get(i, 1)[0];

            if (closed(0., PI / 6).contains(p) || closed(PI * 5 / 6, PI * 7 / 6).contains(p)) {
                v.push_back(lines.row(i));
            } else if (closed(PI * 4 / 3, PI * 5 / 3).contains(p) || closed(PI / 3, PI * 2 / 3).contains(p)) {
                h.push_back(lines.row(i));
            }

        }

        if (h.rows() < 10 || v.rows() < 10) {
            return m;
        }
        h = psedoClassify(h);
        v = psedoClassify(v);

        for (int i = 0; i < h.rows(); i++) {

            Point[] l1 = polarToCart(h.row(i));
            Core.line(m, l1[0], l1[1], new Scalar(255,0,0), 2);

            for (int j = 0; j < v.rows(); j++) {
                Point[] l2 = polarToCart(v.row(j));
                Optional<Point> p = lineIntersection(l1[0], l1[1], l2[0], l2[1]);

                Core.line(m, l2[0], l2[1], new Scalar(255,0,0), 2);

                if (p.isPresent()) {
                    Core.circle(m, p.get(), 3, new Scalar(0, 255, 0), 3);
                }
            }
        }

        if (h.rows() == 10 && v.rows() == 10) {
            Highgui.imwrite("/tmp/dd_" + System.currentTimeMillis() + ".jpg", m);
            System.out.println("cuanak!");
        }


        return m;
    }

    public Mat psedoClassify(Mat m) {
        Mat result = new Mat(0, 2, m.type());
        Mat sorted = new Mat();
        Core.sortIdx(m.col(0), sorted, Core.SORT_EVERY_COLUMN);

        double last = Double.MAX_VALUE;

        for (int i = 0; i < m.rows(); i++) {
            int index = (int) sorted.get(i, 0)[0];
            double rho = m.get(index, 0)[0];
            double theta = m.get(index, 1)[0];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;

            if (Math.abs(last-rho) > 15) {
                result.push_back(m.row(index));

//                System.out.println("x0: " + x0 + " y0: " + y0 + " - " + m.row(index).dump()+" - "+result.rows());
            }

            last = rho;
        }

        return result;
    }

    public Point _polarToCart(double[] p) {
        double rho = p[0];
        double theta = p[1];

        return new Point(rho, theta * 100);
    }

    public Point[] polarToCart(Mat p) {
        double rho = p.get(0, 0)[0];
        double theta = p.get(0, 1)[0];

        double a = Math.cos(theta);

        double b = Math.sin(theta);
        double x0 = a * rho, y0 = b * rho;

        Point pt1 = new Point();
        Point pt2 = new Point();

        pt1.x = (int) (x0+2000 * (-b));
        pt1.y = (int) (y0+2000 * (a));

        pt2.x = (int) (x0-2000 * (-b));
        pt2.y = (int) (y0-2000 * (a));

        return new Point[]{pt1, pt2};

    }

    public Optional<Point> lineIntersection(Point p0, Point p1, Point p2, Point p3) {
        double s1_x, s1_y, s2_x, s2_y;

        s1_x = p1.x-p0.x;
        s1_y = p1.y-p0.y;
        s2_x = p3.x-p2.x;
        s2_y = p3.y-p2.y;

        double s, t;

        s = (-s1_y * (p0.x-p2.x)+s1_x * (p0.y-p2.y)) / (-s2_x * s1_y+s1_x * s2_y);
        t = (s2_x * (p0.y-p2.y)-s2_y * (p0.x-p2.x)) / (-s2_x * s1_y+s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // Collision detected
            Point result = new Point();
            result.x = p0.x+(t * s1_x);
            result.y = p0.y+(t * s1_y);

            return Optional.of(result);
        }

        return Optional.absent();
    }


    public Mat adjustBrigth(Mat m) {
        Mat close = new Mat();
        Mat div = new Mat();
        Mat gray = new Mat();
        Mat norm = new Mat();
        Mat result = new Mat();

        Imgproc.GaussianBlur(m, m, new Size(5, 5), 0);

        m.convertTo(gray, CvType.CV_32F);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(9, 9));
        Imgproc.morphologyEx(gray, close, Imgproc.MORPH_CLOSE, kernel);
        Core.divide(gray, close, div);

        normalize(div, norm, 0, 255, Core.NORM_MINMAX);

        norm.convertTo(result, CvType.CV_8U);

        return result;
    }

    //
    public Mat getMask(Mat m) {
        Mat hierarchy = new Mat();

        Imgproc.adaptiveThreshold(m, m, 255, 0, 1, 19, 2);

        java.util.List<MatOfPoint> contours = Lists.newArrayList();
        Imgproc.findContours(m, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint max = DetectSudoku.ORDERING_BY_AREA.max(contours);

        Mat mask = Mat.zeros(m.size(), CvType.CV_8UC1);

        Imgproc.drawContours(mask, ImmutableList.of(max), 0, Scalar.all(255), -1);
        Imgproc.drawContours(mask, ImmutableList.of(max), 0, Scalar.all(0), 2);

        return mask;
    }
//
    /*
     * try to find all lines intersection using sobel operator
     */
    public Mat propperUnwrappTest(Mat m) {

        Mat gray = new Mat();
        cvtColor(m, gray, Imgproc.COLOR_RGB2GRAY);

        Mat srcAdjusted = adjustBrigth(gray);

        Mat dst = new Mat();
        srcAdjusted.copyTo(dst, getMask(srcAdjusted.clone()));

        int lowThreshold = 100;
        double ratio = 2.4;

        Canny(dst, dst, lowThreshold, lowThreshold * ratio);


        Mat h = horizontalLines(dst.clone());
        Mat v = verticalLines(dst.clone());

        Mat dst2 = new Mat();
        h.copyTo(dst2, v);

        java.util.List<MatOfPoint> contours2 = Lists.newArrayList();
        findContours(dst2, contours2, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);

        Mat orig = m.clone();

        int i = 0;
        for (MatOfPoint p: contours2) {
            Moments mom = moments(p);
            int x = (int)(mom.get_m10()/mom.get_m00());
            int y = (int)(mom.get_m01()/mom.get_m00());
            Point centroid = new Point(x,y);

            circle(orig, centroid  ,4, new Scalar(0,255,0),-1);

            putText(orig, Integer.toString(i++), centroid, Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(0, 255, 0));

        }

        return dst;
    }

    public Mat verticalLines(Mat m) {
        Mat kernel = getStructuringElement(MORPH_RECT, new Size(2, 10));

        Mat result = new Mat(m.size(), CvType.CV_8SC1);
        Sobel(m, result, CvType.CV_16S, 2, 0);

        convertScaleAbs(result, result);
        normalize(result, result, 0, 255, Core.NORM_MINMAX);

        morphologyEx(result, result, MORPH_DILATE, kernel);

        threshold(result, result, 0, 255, THRESH_OTSU);

        java.util.List<MatOfPoint> cdx = Lists.newArrayList();

        findContours(result.clone(), cdx, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint point : cdx) {
            Rect rect = boundingRect(point);
            if (rect.area() < 1000) {
                rectangle(result, rect.tl(), rect.br(), Scalar.all(0), -1);
            }
        }

        return result;
    }

    public Mat horizontalLines(Mat m) {
        Mat kernel = getStructuringElement(MORPH_RECT, new Size(10, 2));

        Mat result = new Mat(m.size(), CvType.CV_8SC1);

        erode(m, result, kernel);

        Sobel(m, result, CvType.CV_16S, 0, 2);

        convertScaleAbs(result, result);
        normalize(result, result, 0, 255, Core.NORM_MINMAX);

        morphologyEx(result, result, MORPH_DILATE, kernel);

        threshold(result, result, 0, 255, THRESH_OTSU);

        java.util.List<MatOfPoint> cdx = Lists.newArrayList();

        findContours(result.clone(), cdx, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint point : cdx) {
            Rect rect = boundingRect(point);
            if (rect.area() < 1000) {
                rectangle(result, rect.tl(), rect.br(), Scalar.all(0), -1);
            }
        }

        return result;
    }

    @Test
    public void testSudoku() {
        DetectSudoku sudoku = new DetectSudoku();
        Mat m = Highgui.imread("/home/jose/Code/IIA/sudoku/src/test/resources/sudoku_4.jpg");
        sudoku.extractDigits(m);
    }

    @Test
    public void testDetectSudoku() throws InterruptedException {
        DetectSudoku sudoku = new DetectSudoku();

        JFrame frame = new JFrame("WebCam Capture");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final CameraPanel panel = new CameraPanel();
        frame.setSize(400, 400);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.setFocusable(true);

        Mat image = new Mat();

        VideoCapture capture = new VideoCapture(0);

        capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 1024 );

        capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 800);


        if (capture.isOpened()) {
            Thread.sleep(500);

            while (true) {
                capture.read(image);
                if (!image.empty()) {
                    frame.setSize(image.width()+40, image.height()+60);
                    //Apply the classifier to the captured image
                    Mat m = sudoku.getSudokuArea(image);
                    System.out.println(image);
                    panel.matToBufferedImage(m);
                    panel.repaint();
                } else {
                    System.out.println("empty!");
                    break;
                }
            }
        }
        capture.release(); //release the webcam

    }

    @Test
    public void testCatalog() throws IOException, InterruptedException {
        DetectSudoku sudoku = new DetectSudoku();

        JFrame frame = new JFrame("WebCam Capture");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final CameraPanel panel = new CameraPanel();
        frame.setSize(400, 400);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.setFocusable(true);


        final File tmp = new File("/tmp/cell");
        final Set<String> files = Sets.newHashSet();

        for (File f : tmp.listFiles()) {
            files.add(f.getAbsolutePath());
        }


        frame.addKeyListener(new KeyListener() {
            Iterator<String> fileIterator = files.iterator();
            String current;
            Mat image;

            {
                next();
            }

            private void next() {
                current = fileIterator.next();
                image = Highgui.imread(current, 0);
                panel.matToBufferedImage(image);
            }

            @Override
            public void keyTyped(KeyEvent e) {

                System.out.println(current+" - "+e.getKeyChar());
                File src = new File(current);
                File dst = new File("/tmp/cell/"+e.getKeyChar()+"/"+src.getName());

                try {
                    Files.move(src, dst);
                } catch (IOException e1) {
//                    e1.printStackTrace();
                }

                next();
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        while (true) {
            Thread.sleep(100);
            panel.repaint();
        }

    }

}
