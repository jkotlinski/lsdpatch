mkdir -p classes
javac -Xlint:deprecation -Xlint:unchecked -d classes *.java
cd classes
jar cvfm ../LSDPatcher.jar ../META-INF/MANIFEST.MF *.class
