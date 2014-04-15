package org.losmonos.sudoku.grabber;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.losmonos.sudoku.grabber.FeatureDetector.CONTAIN_DIGIT_SUB_MATRIX_DENSITY;
import static org.opencv.imgproc.Imgproc.*;

public class DetectSudoku {

    private Size FOUR_CORNERS = new Size(1, 4);

    private DetectDigit detect = new DetectDigit();

    public static final Function<MatOfPoint, Integer> AREA = new Function<MatOfPoint, Integer>() {
        @Override
        public Integer apply(MatOfPoint input) {
            return (int)contourArea(input);
        }
    };

    public static final Ordering<MatOfPoint> ORDERING_BY_AREA = Ordering.natural().onResultOf(AREA);
    private static final Mat crossKernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));

    /**
     * This method does:
     * - Gaussian blur in order to reduce noise
     * - Threshold transformation
     * - A dilate with a 3x3 crossKernel to augment the
     *
     * @param image
     * @return
     */
    private Mat preprocess(Mat image) {
        Mat bw = new Mat();

        cvtColor(image, bw, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(bw, bw, new Size(11, 11), 0);

        adaptiveThreshold(bw, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);

//        dilate(bw, bw, crossKernel);

        return bw;
    }

    /**
     * This method does:
     * - Gaussian blur in order to reduce noise
     * - Threshold transformation
     * - A dilate/erode to improve conectivity between conected elements
     */
     private Mat preprocess2(Mat image) {
        Mat bw = preprocess(image);
//        erode(bw, bw, crossKernel);

        return bw;
    }


    private MatOfPoint findBiggerPolygon(Mat image) {
        List<MatOfPoint> contours = Lists.newArrayList();
        Mat hierarchy = new Mat();

        findContours(image.clone(), contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            return new MatOfPoint();
        }

        MatOfPoint max = ORDERING_BY_AREA.max(contours);

        return max;
    }

    private MatOfPoint2f aproxPolygon(MatOfPoint poly) {

        MatOfPoint2f dst = new MatOfPoint2f();
        MatOfPoint2f src = new MatOfPoint2f();
        poly.convertTo(src, CvType.CV_32FC2);

        double arcLength = Imgproc.arcLength(src, true);
        approxPolyDP(src, dst, 0.02 * arcLength, true);
        return dst;
    }

    private static final Ordering<Point> SORT = Ordering.natural().nullsFirst().onResultOf(
            new Function<Point, Integer>() {
                public Integer apply(Point foo) {
                    return (int) (foo.x+foo.y);
                }
            }
    );

    private MatOfPoint2f orderPoints(MatOfPoint2f mat) {
        List<Point> pointList = SORT.sortedCopy(mat.toList());

        if (pointList.get(1).x > pointList.get(2).x) {
            Collections.swap(pointList, 1, 2);
        }

        MatOfPoint2f s = new MatOfPoint2f();
        s.fromList(pointList);

        return s;
    }

    private Mat wrapPerspective(int size, MatOfPoint2f src, Mat image) {
        Size reshape = new Size(size, size);

        Mat undistorted = new Mat(reshape, CvType.CV_8UC1);

        MatOfPoint2f d = new MatOfPoint2f();
        d.fromArray(new Point(0, 0), new Point(0, reshape.width), new Point(reshape.height, 0),
                new Point(reshape.width, reshape.height));

        warpPerspective(image, undistorted, getPerspectiveTransform(src, d), reshape);

        return undistorted;
    }

    private Mat applyMask(Mat image, MatOfPoint poly) {
        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);

        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(255), -1);
        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(0), 2);

        Mat dst = new Mat();
        image.copyTo(dst, mask);

        return dst;
    }


    public Mat getSudokuArea(Mat image) {
        Mat preprocessed = preprocess(image);
        MatOfPoint poly = findBiggerPolygon(preprocessed);

        MatOfPoint2f aproxPoly = aproxPolygon(poly);
//
        if (Objects.equals(aproxPoly.size(), FOUR_CORNERS)) {
            int size = distance(aproxPoly);

            Mat cutted = applyMask(image, poly);

            Mat wrapped = wrapPerspective(size, orderPoints(aproxPoly), cutted);
            Mat preprocessed2 = preprocess2(wrapped);
            Mat withOutLines = cleanLines(preprocessed2);

            return withOutLines;
        }

        return preprocessed;
    }

    private Mat cleanLines(Mat image) {
        Mat m = image.clone();
        Mat lines = new Mat();

        int threshold = 50;
        int minLineSize = 200;
        int lineGap = 20;

        Imgproc.HoughLinesP(m, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);

        for (int x = 0; x < lines.cols(); x++) {
            double[] vec = lines.get(0, x);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);

            Core.line(m, start, end, Scalar.all(0), 3);

        }
        return m;
    }

    public List<Integer> extractDigits(Mat m) {
        Mat sudoku = getSudokuArea(m);

        if (sudoku == null) {
            return null;
        }

        return extractCells(sudoku);
    }


    private int distance(MatOfPoint2f poly) {
        Point[] a =  poly.toArray();
        return (int)Math.sqrt((a[0].x - a[1].x)*(a[0].x - a[1].x) +
        (a[0].y - a[1].y)*(a[0].y - a[1].y));
    }


    private List<Mat> getCells(Mat m) {
        int size = m.height() / 9;

        Size cellSize = new Size(size, size);
        List<Mat> cells = Lists.newArrayList();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Rect rect = new Rect(new Point(col * size, row * size), cellSize);

                Mat digit = new Mat(m, rect).clone();
                cells.add(digit);
            }
        }

        return cells;
    }

    private List<Integer> extractCells(Mat m) {
        List<Mat> cells = getCells(m);
        List<Optional<Rect>> digitBoxes = Lists.transform(cells, FeatureDetector.GET_DIGIT_BOX_BYTE_SUM);

        List<Integer> result = Lists.newArrayList();
        List<Mat> cuts = Lists.newArrayList();
        /* zip... zip! :'( */

        for(int i = 0; i < cells.size(); i++ ) {
            Mat cell = cells.get(i);
            Optional<Rect> box = digitBoxes.get(i);

            int d = 0;

            if (box.isPresent() && CONTAIN_DIGIT_SUB_MATRIX_DENSITY.apply(cell)) {
                /* cut current cell to the finded box */
                Mat cutted = new Mat(cell, box.get()).clone();
                Core.rectangle(cell, box.get().tl(), box.get().br(), Scalar.all(255));
                cuts.add(cutted);
                d = detect.detect(cutted);
            }

            Core.rectangle(cell, new Point(0,0), new Point(100,100), Scalar.all(255));

            result.add(d);

        }


        Mat m2 = new Mat(0, cells.get(0).cols(), CvType.CV_8SC1);

        for(Mat digit: cells) {
            m2.push_back(digit.clone());
        }

        Highgui.imwrite("cells_boxed.jpg", m2);

        return result;
    }

}
