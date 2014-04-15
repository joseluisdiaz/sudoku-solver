package org.losmonos.sudoku.dancing;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.losmonos.sudoku.dancing.DancingLinks.Cell._0;
import static org.losmonos.sudoku.dancing.DancingLinks.Cell._1;


public class DancingLinksTest {

    @Test
    public void exactCoverTest() {
        DancingLinks d = new DancingLinks();

        d.addColumn(ImmutableList.of("a", "b", "c", "d", "e", "f", "g"));

        d.addRow(_0, _0, _1, _0, _1, _1, _0);
        d.addRow(_1, _0, _0, _1, _0, _0, _1);
        d.addRow(_0, _1, _1, _0, _0, _1, _0);
        d.addRow(_1, _0, _0, _1, _0, _0, _0);
        d.addRow(_0, _1, _0, _0, _0, _0, _1);
        d.addRow(_0, _0, _0, _1, _1, _0, _1);

        System.out.println(d.search());
    }

}
