#!/bin/sh
echo "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <settings
xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"
xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><localRepository>$WORKSPACE/mavenRepository</localRepository><mirrors><mirror><id>mead-mirror</id><url>http://download.lab.bos.redhat.com/brewroot/repos/jb-eap-6-rhel-6-build/latest/maven/</url><mirrorOf>*</mirrorOf></mirror></mirrors></settings>" > minimalSettings.xml
