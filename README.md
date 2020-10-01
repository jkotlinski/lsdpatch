# LSDPatcher

A tool for modifying songs, samples, fonts and palettes on [Little Sound Dj][lsdj] (LSDj) ROM images and save files. Requires
[Java][java] 8+. If you have problems running the .jar on Windows, try [Jarfix][jarfix].

[Download][releases] | [Fonts][lsdfnts] | [Palettes][lsdpals]

## Preparing samples

Each kit fits ~2.8 seconds of samples. To prepare your samples for Game Boy, use [sox]:

```shell
sox.exe raw_sample.wav --rate 11468 -c 1 -b 8 converted_sample.wav --norm=0 dither -p 5
```
## Tutorials
- [Tutorial by Little-Scale](http://little-scale.blogspot.com/2008/11/how-to-prepare-samples-and-create-lsdj.html)
- [Video tutorial by 2xAA](http://www.youtube.com/watch?v=FGeVrW5Jxww)

## Building

Build using [Maven](https://maven.apache.org/): `mvn package`

![Java CI with Maven](https://github.com/jkotlinski/lsdpatch/workflows/Java%20CI%20with%20Maven/badge.svg)

[lsdj]: https://www.littlesounddj.com/
[sox]: http://sox.sourceforge.net/
[releases]: https://github.com/jkotlinski/lsdpatch/releases
[wiki]: https://github.com/jkotlinski/lsdpatch/wiki/Documentation
[jarfix]: http://johann.loefflmann.net/en/software/jarfix/index.html
[java]: http://www.java.com/
[lsdfnts]: https://github.com/psgcabal/lsdfonts
[lsdpals]: https://github.com/psgcabal/lsdpals
