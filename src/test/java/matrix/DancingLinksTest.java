package matrix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.losmonos.dancing.DancingLinks;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static org.losmonos.dancing.DancingLinks.Cell.*;
import static org.losmonos.dancing.DancingLinks.Node;

/**
 * Created by jose on 2/26/14.
 */
public class DancingLinksTest {

    @Test
    public void exactCoverTest() {
        DancingLinks d = new DancingLinks();
        d.addColumn("a", "b", "c", "d", "e", "f", "g");

        d.addRow(_0, _0, _1, _0, _1, _1, _0);
        d.addRow(_1, _0, _0, _1, _0, _0, _1);
        d.addRow(_0, _1, _1, _0, _0, _1, _0);
        d.addRow(_1, _0, _0, _1, _0, _0, _0);
        d.addRow(_0, _1, _0, _0, _0, _0, _1);
        d.addRow(_0, _0, _0, _1, _1, _0, _1);

        System.out.println(d.search());
    }

}
