set -e
mkdir -p classes
javac -Xbootclasspath:c:/Program\ Files/Java/jre1.8.0_241/lib/rt.jar -source 1.8 -target 1.8 -Xlint:deprecation -d classes *.java
cd classes
jar cvfm ../LSDPatcher.jar ../META-INF/MANIFEST.MF *.class ../*.bmp
