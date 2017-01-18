mkdir -p classes
javac -d classes *.java
cd classes
jar cvfm ../LSDPatcher.jar ../META-INF/MANIFEST.MF *.class
