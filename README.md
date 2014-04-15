#sudoku-solver

Yet another sudoku solver based on Knuth's dancing link algorithm. And a sudoku grabber using OpenCV, i use a lot of ideas from openstack, several blogs in order to build this in java, so many thanks to all people that work hard for knowledge transfer.

##Dancing

Dancing package is the implementation of the dancing links algorithm and the sudoku-problem model. If you give a Board to this you are ready to go.

##Grabber

This is more like a work-in-progress, theres several ideas out there, it work with a few images, and fails miserably with others.
If you want it to run, you need a compiled binary of OpenCV i'm using now 2.4.8, and the jar. Put the sharedlibraty on lib/, install opencv jar with maven and the exec:exec do the rest.

install in a local repo opencv-jar

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

And... probably it will run!

```
mvn exec:exec -Dimage=src/test/resources/sudoku_ok_2.jpg
```

## Author:
*  José Luis Diaz (jose at rtfm dot org dot ar)

## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0