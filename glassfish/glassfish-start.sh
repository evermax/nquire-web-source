#!/bin/sh
mkdir $GLASSFISH_HOME/lib
cp /gf/lib/log4j-api-2.1.jar $GLASSFISH_HOME/lib/
cp /gf/lib/log4j-core-2.1.jar $GLASSFISH_HOME/lib/
cp /gf/lib/log4j-slf4j-impl-2.1.jar $GLASSFISH_HOME/lib/
cp /gf/lib/raven-log4j2-6.0.0.jar $GLASSFISH_HOME/lib/
/usr/local/glassfish4/bin/asadmin start-domain
/usr/local/glassfish4/bin/asadmin create-jvm-options '-Dlog4j.configurationFile=file\:///${com.sun.aas.instanceRoot}/config/log4j2.xml'
/usr/local/glassfish4/bin/asadmin -u admin deploy --contextroot "/" /project/$*
/usr/local/glassfish4/bin/asadmin stop-domain
cp /gf/glassfish-log4j2.xml $GLASSFISH_HOME/glassfish/domains/domain1/config/log4j2.xml
/usr/local/glassfish4/bin/asadmin start-domain --verbose
