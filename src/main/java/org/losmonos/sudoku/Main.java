package org.losmonos.sudoku;

import com.google.common.base.Joiner;
import org.losmonos.sudoku.dancing.Board;
import org.losmonos.sudoku.dancing.SudokuSolver;
import org.losmonos.sudoku.grabber.DetectSudoku;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.util.*;


/**
 * Created by jose on 4/2/14.
 */

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Filenamemissing");
            System.exit(-1);
        }

        Mat m = Highgui.imread(args[0]);

        DetectSudoku sudoku = new DetectSudoku();
        List<Integer> l = sudoku.extractDigits(m);

        Board b = Board.of(9, Joiner.on(" ").join(l));

        System.out.println("Grabbed sudoku\n==============\n\n");
        System.out.println(b);

        SudokuSolver s = new SudokuSolver(b);
        Board solved = s.solve();

        System.out.println("Solved sudoku\n=============\n\n");
        System.out.println(solved);
    }
}
