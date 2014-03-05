package matrix;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import org.losmonos.dancing.Board;
import org.losmonos.dancing.SudokuSolver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;

public class SudokuSolverTest {


    public Board get(String fileName, Integer size) throws URISyntaxException, IOException {
        URL resource = Resources.getResource(fileName);
        File file = new File(resource.toURI());

        String board = Files.asCharSource(file, Charsets.UTF_8).read();

        return Board.of(size, board);
    }

    @Test
    public void simpleBoardTest() throws IOException, URISyntaxException {
        String board = "4 0 0  0 0 0  8 0 5  \n"+
                "0 3 0  0 0 0  0 0 0  \n"+
                "0 0 0  7 0 0  0 0 0  \n"+
                "\n"+
                "0 2 0  0 0 0  0 6 0  \n"+
                "0 0 0  0 8 0  4 0 0  \n"+
                "0 0 0  0 1 0  0 0 0  \n"+
                "\n"+
                "0 0 0  6 0 3  0 7 0  \n"+
                "5 0 0  2 0 0  0 0 0  \n"+
                "1 0 4  0 0 0  0 0 0  \n"+
                "\n";

        Assert.assertThat(get("sudoku9x9.txt",9).toString(), is(board));
    }

    @Test
    public void solverTest() throws IOException, URISyntaxException {
        SudokuSolver s = new SudokuSolver(get("sudoku4x4.txt", 4));
        String board = "4 2  3 1  \n"+
                "1 3  2 4  \n"+
                "\n"+
                "2 4  1 3  \n"+
                "3 1  4 2  \n\n";

        Assert.assertThat(s.solve().toString(), is(board));
    }

}
