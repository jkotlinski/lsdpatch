# LSDPatcher

A tool for modifying songs, samples, fonts and palettes on Little Sound Dj (LSDj) ROM images and save files. Requires [Java](http://www.java.com/). If you have problems running the .jar on Windows, try [Jarfix](http://johann.loefflmann.net/en/software/jarfix/index.html).

Download: https://github.com/jkotlinski/lsdpatch/releases

Documentation: https://github.com/jkotlinski/lsdpatch/wiki/Documentation

Tutorial by Little-Scale: http://little-scale.blogspot.com/2008/11/how-to-prepare-samples-and-create-lsdj.html

Video tutorial by 2xAA: http://www.youtube.com/watch?v=FGeVrW5Jxww

Fonts: https://github.com/urbster1/lsdfonts

Palettes: https://github.com/urbster1/lsdpals

## Building

Build using [Maven](https://maven.apache.org/): `mvn package`

## Sample dithering

Sample dithering was a feature of previous versions but since has been removed after concerns around the UI workflow.

To add dithering to your samples before adding them in a kit, you can use tools like [sox] to pre-convert your sample to
11468Hz and 8-bit and adding a dithering pass over it in a single command (courtesy of @urbster1).

```shell
sox.exe raw_sample.wav --rate 11468 -c 1 -b 8 converted_sample.wav --norm=0 dither -p 5
```

[sox]: http://sox.sourceforge.net/