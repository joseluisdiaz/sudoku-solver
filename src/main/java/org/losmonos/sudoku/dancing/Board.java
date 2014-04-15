package org.losmonos.sudoku.dancing;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

/**
 * Created by jose on 3/4/14.
 */
public class Board {
    public Integer cellSize;
    public Integer size;
    public Integer[][] m;

    public Board() {
    }

    public static Board of(Integer size) {
        Board b = new Board();

        b.cellSize = Double.valueOf(Math.sqrt(size)).intValue();
        b.m = new Integer[size][size];
        b.size = size;
        return b;
    }

    public static Board of(Integer size, String board) {
        Board b = new Board();

        b.cellSize = Double.valueOf(Math.sqrt(size)).intValue();
        b.m = new Integer[size][size];
        b.size = size;

        Iterable<String> linearBoard = Splitter.on(CharMatcher.DIGIT.negate())
                .trimResults()
                .omitEmptyStrings()
                .split(board);

        Integer i = 0;

        for (String cell : linearBoard) {
            Integer row = i / size;
            Integer col = i % size;

            b.m[row][col] = Integer.valueOf(cell);
            i++;
        }

        return b;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < m.length; row++) {

            for (int col = 0; col < m.length; col++) {
                sb.append(m[row][col]);
                sb.append(" ");
                if ((col+1) % cellSize == 0) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
            if ((row+1) % cellSize == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
