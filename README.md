Tattletale EAP-7
====================
Originally JBoss Tattletale report tool, changed to pure maven project and added EAP 7 / EE 7 / JDK 8 report support.


Usage
------------
* Requires: JDK 7 or higher

* To **build**:

      $ mvn clean package

* To **execute**:

      $ java -jar ${tattletale_jar_name} ${input_file} ${report_path}

  For example:
  
      $ java -jar tattletale-eap7-1.3.1.jar /tmp/MyApp.ear /tmp/tattletale/report/

This version of Tattletale EAP-7 is verbose and use [JUL](https://docs.oracle.com/javase/7/docs/api/java/util/logging/package-summary.html) to display messages. 

JUL messages format can be configured through ["java.util.logging.SimpleFormatter.format"](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html) property.
You can set it as a system property through -D option of java executable. 

For example :
  
    $ java -Djava.util.logging.SimpleFormatter.format="%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s - %2$s - %5$s%6$s%n" -jar tattletale-eap7-1.3.1.jar /tmp/MyApp.ear /tmp/tattletale/report/

   or with messages only (without time and date stamping or class or method) :

    $ java -Djava.util.logging.SimpleFormatter.format="%5$s%6$s%n" -jar tattletale-eap7-1.3.1.jar /tmp/MyApp.ear /tmp/tattletale/report/

