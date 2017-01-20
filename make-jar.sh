set -e
mkdir -p classes
javac -Xbootclasspath:c:/Program\ Files/Java/jre6/lib/rt.jar -source 1.6 -target 1.6 -Xlint:deprecation -d classes *.java
cd classes
jar cvfm ../LSDPatcher.jar ../META-INF/MANIFEST.MF *.class ../*.bmp
