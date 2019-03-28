# sudoku-solver

Yet another sudoku solver based on Knuth's dancing link algorithm. And a sudoku grabber using OpenCV, i use a lot of ideas from stack**overflow**, several blogs in order to build this in java, so many thanks to all people that work hard to transfer knowledge.

## Dancing

Dancing package is the implementation of the dancing links algorithm and the sudoku-problem model. If you give a Board to this you are ready to go.

## Grabber

This is more like a work-in-progress, theres several ideas out there, it work with a few images, and fails miserably with others.
If you want it to run, you need a compiled binary of OpenCV i'm using now 2.4.8, and the jar. Put the sharedlibraty on lib/, install opencv jar with maven and the exec:exec do the rest.

Install in a local maven repo opencv-jar

```
 mvn install:install-file -Dfile=lib/opencv-248.jar
                          -DgroupId=org.opencv
                          -DartifactId=opencv
                          -Dversion=2.4.8
                          -Dpackaging=jar
                          -DgeneratePom=true
                          -DcreateChecksum=true
```

For the shared library, just copy into the lib/ exec:exec will try to look there, or just set _java.library.path_.

Compile it! :-)

```
mvn compile
```

And... probably it will run! (java binary should be in path)

```
mvn exec:exec -Dimage=src/test/resources/sudoku_ok_2.jpg
```

This is the input:

![sudoku_ok_2.jpg](https://raw.githubusercontent.com/joseluisdiaz/sudoku-solver/master/src/test/resources/sudoku_ok_2.jpg)

and the Output is:

```
Grabbed sudoku
==============


3 4 0  0 0 8  0 0 0
0 0 7  0 0 1  8 0 0
0 8 9  5 0 0  4 2 0

0 0 6  0 0 0  9 0 3
0 2 0  0 0 0  0 8 0
4 0 8  0 0 0  2 0 0

0 1 2  0 0 7  6 4 0
0 0 5  8 0 0  7 0 0
0 0 0  1 0 0  0 9 8


Solved sudoku
=============


3 4 1  6 2 8  5 7 9
2 5 7  4 9 1  8 3 6
6 8 9  5 7 3  4 2 1

1 7 6  2 8 4  9 5 3
5 2 3  7 6 9  1 8 4
4 9 8  3 1 5  2 6 7

8 1 2  9 3 7  6 4 5
9 3 5  8 4 6  7 1 2
7 6 4  1 5 2  3 9 8
```

and in the begging you will see some debug information:

```
[3, 3, 3]
[4, 1, 4]
[8, 8, 8]
[7, 7, 7]
...
[1, 1, 1]
[9, 9, 9]
[8, 8, 8]

```

That's the result to apply to the detected digits [KNN](http://es.wikipedia.org/wiki/Knn) with k=3. Also it will generate a file called __cells_boxed.jpg__ witch contains each cell of the sudoku and a boundbox for the digit.



## Author:
*  José Luis Diaz (diazjoseluis at gmail dot com)

## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
