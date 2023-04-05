# Blinkenlights Stereoscope Java Tools

These are the Java tools built to power the Blinkenlights Stereoscope project in Toronto.

## Top-level components

* bmix - a mixer which can composite multiple video streams of varying framerates and create
outputs with stable framerates. Also supports transparency, clipping, alpha-masking, scaling and more!

* bmix-sender - a Java GUI application to load a bmix animation file and send it to bmix.  Most Blinkenlights animation 
formats are supported.

* bmix-monitor - a simple Java GUI which can display a blinkenlights network stream.

* bmix-statistics - a Java GUI which displays the configuration and status of a running bmix instance.

## How to build

    $ cd blinkenlights-java
    $ ./mvnw install

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


