# Blinkenlights Stereoscope Java Tools

These are the Java tools built to power the Blinkenlights Stereoscope project in Toronto.

## Top-level components

* bmix - a mixer which can composite multiple video streams of varying framerates and create
outputs with stable framerates. Also supports transparency, clipping, alpha-masking, scaling and more!

* bmix-sender - a Java GUI application to load a bmix animation file and send it to bmix.  Most Blinkenlights animation 
formats are supported.

* bmix-monitor - a simple Java GUI which can display a blinkenlights network stream.

* bmix-statistics - a Java GUI which displays the configuration and status of a running bmix instance.

## See everything work together in an example configuration

Requires JDK 11 or newer on your $PATH or in $JAVA_HOME.

    ./demo.sh

This builds the Java sources, runs bmix with an example configuration (same as was used on Stereoscope in Toronto),
monitors it with BMix Statistics, and opens a sender window.

When prompted (by BMix Statistics) for which BMix to monitor, the default "localhost" is correct.

To play videos:

* [Get some .blm or .bml movie files](https://github.com/blinkenlights/blinkenlights/tree/master)
* Drag a movie file onto the Sender window 
* Set a target port number in your sender window, e.g. "localhost:2326"
  * Consult the BMix Statistics window to choose a port number. For example, background is localhost:2329.
* Open more Sender windows with File --> New Window and send other videos to other ports

## How to build

    $ ./mvnw package

## How to run bmix

    $ cd blinkenlights-java/bmix
    $ java -jar target/bmix-1.5-SNAPSHOT-jar-with-dependencies.jar src/main/config/bmix-example.xml

## How to run bmix-sender

(OS X) Navigate to blinkenlights-java/bmix-sender/target in the Finder and double-click the application
or 

    $ cd blinkenlights-java/bmix-sender
    $ java -jar target/bmix-sender-1.5-SNAPSHOT-jar-with-dependencies.jar 

## How to run bmix-statistics

Make sure a bmix is running already so you can connect to it!

    $ cd blinkenlights-java/bmix-statistics
    $ java -jar target/bmix-statistics-1.5-SNAPSHOT-jar-with-dependencies.jar 

When the thingy pops up, give it the hostname to the place from whence you ran bmix.


