package matrix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.losmonos.dancing.Board;
import org.losmonos.dancing.DancingLinks;

import static org.losmonos.dancing.DancingLinks.Cell._0;
import static org.losmonos.dancing.DancingLinks.Cell._1;


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
