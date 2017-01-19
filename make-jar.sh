set -e
mkdir -p classes
javac -Xlint:deprecation -d classes *.java
cd classes
jar cvfm ../LSDPatcher.jar ../META-INF/MANIFEST.MF *.class ../*.bmp
