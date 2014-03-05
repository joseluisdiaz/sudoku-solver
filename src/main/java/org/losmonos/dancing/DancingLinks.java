package org.losmonos.dancing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

import static org.losmonos.dancing.DancingLinks.Cell._1;


public class DancingLinks {

    /**
     * this represents a cell into the table, according to Knuth paper this is a double-linked list
     * with _l_eft/_r_right and _u_p/_d_own and a point to the header c
     */
    private static class Node {
        public Header c;

        public Node l;
        public Node r;
        public Node u;
        public Node d;

        private Node(Header c, Node l, Node r, Node u, Node d) {
            this.c = c;
            this.l = l;
            this.r = r;
            this.u = u;
            this.d = d;
        }

        public static Node of(Header c, Node u, Node d) {
            return new Node(c,null,null,u,d);
        }

    }

    /**
     * a cell witch represents a Header also have a Name and a Size, the size is de amount of
     * cell for this column
     */
    private static class Header extends Node {
        public Integer size;
        public String name;

        private Header(Integer size, String name) {
            super(null,null,null,null,null);
            this.size = size;
            this.name = name;
            this.c = this;
        }

        public static Header of(Integer size, String name) {
            return new Header(size, name);
        }
    }


    /**
     * pointer to the first column;
     */
    private Header head;

    private Integer colSize = 0;


    public DancingLinks() {
        Header h = Header.of(0, null);
        h.l = h;
        h.r = h;
        h.u = h;
        h.d = h;
        head = h;
    }

    public void addColumn(List<String> names) {
        for (String name: names) {
            addColumn(name);
        }
    }

    public void addColumn(String name) {
        colSize = colSize + 1;

        Header col = Header.of(0, name);
        col.u = col;
        col.d = col;

        Node last = head.l;

        /* head ... <-> last <-> col <-> head */
        last.r = col;

        col.l = last;
        col.r = head;

        head.l = col;


    }
    public enum Cell {
        _0, _1
    }

    public void addRow(Cell... row) {
        addRow(Arrays.asList(row));
    }

    public void printHeader() {
        for (Node col = head.r; col != head; col = col.r )  {
            System.out.print(col.c.name);
            System.out.print(" - ");
        }
        System.out.println("");
    }
    public void addRow(List<Cell> row) {
        Header workingCol = head;
        Node prev = null;

        if (row.size() != colSize) {
            throw new RuntimeException("invalid row size: cannot be distinct than column size");
        }

        for (Cell cell : row) {
            workingCol = (Header) workingCol.r;

            if (cell == _1) {
                workingCol.size += 1;

                Node bottom = workingCol.u;
                Node node = Node.of(workingCol, bottom, workingCol);
                bottom.d = node;
                workingCol.u = node;

                if (prev != null) {
                    Node front = prev.r;

                    prev.r = node;
                    node.l = prev;

                    node.r = front;
                    front.l = node;

                } else {
                    node.l = node;
                    node.r = node;
                }
                prev = node;
            }

        }
    }

    private void cover(Header col) {
        col.r.l = col.l;
        col.l.r = col.r;

        /* Column iterator d -> d -> .. -> d  */
        for (Node row = col.d; row != col; row = row.d) {

            /* Row iterator r -> r -> .. -> r */
            for (Node cell = row.r; cell != row; cell = cell.r) {
                cell.d.u = cell.u;
                cell.u.d = cell.d;
                cell.c.size -= 1;
            }
        }

    }

    private void uncover(Header col ) {
        /* Column iterator u -> u -> .. -> u  */
        for (Node row = col.u; row != col; row = row.u) {

            /* Row iterator l <- l <- .. <- l */
            for (Node cell = row.l; cell != row; cell = cell.l) {
                cell.c.size += 1;

                cell.d.u = cell;
                cell.u.d = cell;
            }

        }

        col.r.l = col;
        col.l.r = col;
    }

    private Header nextColumn() {
        Integer value = Integer.MAX_VALUE;
        Header ret = null;

        for (Header col = (Header)head.r; col != head; col = (Header)col.r) {
            if (col.size < value)  {
                value = col.size;
                ret = col;
            }
        }

        return ret;
    }

    public List<List<String>> search() {
        List<List<String>> result = Lists.newArrayList();

        search(ImmutableList.<Node>of(), result);

        return result;
    }

    public void search(List<Node> current, List<List<String>> solution) {

        if (head.r == head) {
            for (Node row : current) {
                List<String> l = Lists.newArrayList(row.c.name);

                for (Node cell = row.r; cell != row; cell = cell.r) {
                    l.add(cell.c.name);
                }
                solution.add(l);
            }
        }
        else {
            Header col = nextColumn();
            cover(col);

            for (Node row = col.d; col != row; row = row.d) {

                for (Node cell = row.r; row != cell; cell = cell.r) {
                        cover(cell.c);
                }

                List<Node> next = ImmutableList.<Node>builder().addAll(current).add(row).build();
                search(next, solution);

                for (Node cell = row.l; row != cell; cell = cell.l) {
                    uncover(cell.c);
                }
            }
            uncover(col);
        }

    }

}
