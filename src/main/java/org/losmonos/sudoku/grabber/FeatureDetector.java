package org.losmonos.sudoku.grabber;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.opencv.core.*;

import java.util.List;

import static org.opencv.imgproc.Imgproc.*;

public class FeatureDetector {

    public interface DigitBoxDetector extends Function<Mat, Optional<Rect>> {}
    public interface HasDigit extends Predicate<Mat> {}

    public static final HasDigit CONTAIN_DIGIT_SUB_MATRIX_DENSITY = new HasDigit() {
        @Override
        public boolean apply(Mat input) {

            double tl = input.size().height/3;
            double br = input.size().width - input.size().width/3;


            Rect cut = new Rect(new Point(tl,tl), new Point(br,br));


            return Core.countNonZero(new Mat(input, cut)) > 20;
        }
    };

    public static final DigitBoxDetector GET_DIGIT_BOX_CONTOURS = new DigitBoxDetector() {
        @Override
        public Optional<Rect> apply(Mat input) {
            List<MatOfPoint> cont = Lists.newArrayList();

            findContours(input.clone(), cont, new Mat(), RETR_CCOMP, CHAIN_APPROX_SIMPLE);

            List<Point> lp = Lists.newArrayList();

            for (MatOfPoint p : cont) {
                Rect rect = boundingRect(p);

                double aspect = rect.height / (double) rect.width;
                double area = rect.area();

                if (aspect > 0.5 && aspect < 10 && area > 20) {
                    lp.add(rect.tl());
                    lp.add(rect.br());
                }

            }

            if (!lp.isEmpty()) {
                MatOfPoint points = new MatOfPoint();
                points.fromList(lp);
                return Optional.of(boundingRect(points));
            }

            return Optional.absent();

        }
    };

    private static class LineDetector {
        /*
         * 'true' means row-iterator
         * 'false' mean col-iterator
         */
        private boolean aggregator;

        private static int TRIES = 6;
        private static int THRESHOLD = 2;

        private Mat m;

        private boolean found = false;
        private int nTry = 0;
        private int foundAt = 0;

        private Mat temp;

        private LineDetector(Mat m, boolean aggregator) {
            this.m = m;
            this.aggregator = aggregator;
        }

        /**
         * Creates a detector based on 'm' matrix, and iterate throw cols
         */
        static LineDetector col(Mat m) {
            return new LineDetector(m, false);
        }

        /**
         * Creates a detector based on 'm' matrix, and iterate throw rows
         */
        static LineDetector row(Mat m) {
            return new LineDetector(m, true);
        }

        public void step(int i) {

            if (nTry < TRIES) {
                temp = getVector(i);

                if (Core.countNonZero(temp) < THRESHOLD) {
                    if (!found) {
                        foundAt = i;
                        found = true;
                    }

                    nTry++;
                } else if (found) {
                    found = false;
                }
            }
        }

        private Mat getVector(int i) {
            return aggregator ? m.row(i) : m.col(i);
        }

        public int get() {
            return foundAt;
        }

    }

    public static final DigitBoxDetector GET_DIGIT_BOX_BYTE_SUM = new DigitBoxDetector() {
        @Override
        public Optional<Rect> apply(Mat input) {
            List<MatOfPoint> cont = Lists.newArrayList();
            findContours(input.clone(), cont, new Mat(), RETR_CCOMP, CHAIN_APPROX_SIMPLE);
            Mat m = input.clone();

            for (MatOfPoint p : cont) {
                Rect rect = boundingRect(p);

                double aspect = rect.height / (double) rect.width;

                if (aspect < 0.1 || aspect > 10) {
                    Core.rectangle(m, rect.tl(), rect.br(), Scalar.all(0), -1);
                }

            }

            int center = m.rows() / 2;
            int n = m.rows()-1;

            LineDetector rowBottom = LineDetector.row(m);
            LineDetector rowTop = LineDetector.row(m);
            LineDetector colLeft = LineDetector.col(m);
            LineDetector colRight = LineDetector.col(m);

            for (int i = center; i <= n; i++) {
                rowBottom.step(i);
                rowTop.step(n-i);
                colLeft.step(i);
                colRight.step(n-i);
            }

            return Optional.of(new Rect(new Point(colLeft.get(), rowTop.get()), new Point(colRight.get()+1, rowBottom.get()+1)));
        }
    };




}
