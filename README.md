Tattletale EAP-7
====================
Originally JBoss Tattletale report tool, changed to pure maven project and added EAP 7 / EE 7 / JDK 8 report support.

Usage
------------
* Requires: JDK 7 or higher
* To build:  $ mvn clean package
* To execute:  $ java -jar ${tattletale_name} ${target_name} ${report_path}
    For example:  $ java -jar tattletale-eap7-0.1-beta.jar  /tmp/MyApp.war  /tmp/tattletale/report/
