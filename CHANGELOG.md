# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

## [1.13.0] - 2022-04-11
### Fixed
 - Filename filtering in non-Windows file dialogs broke in 1.8.0.

### Added
 - Palette Editor: Color space combo box.
 - Palette Editor: "Reality" color space, which looks close to real Game Boy Color.

## [1.12.0] - 2021-10-01
### Added
 - Kit Editor: "Invert Polarity for GBA" preference.

### Changed
 - Kit Editor: Update dither per sample.
 - Kit Editor: Moved "half-speed" preference to menu.
 - ROM files may now use .gbc extension.

## [1.11.6] - 2021-08-18
### Fixed
 - Kit Editor: Dither preference.
 - Kit Editor: Allow filenames less than 3 characters in length.

### Changed
 - Kit Editor: Moved dither preference to main window.
 
### Added
 - Kit Editor: "Duplicate sample" and "Copy" in context menu
 - Kit Editor: Edit menu for "Trim all samples to equal length" and "Paste"

## [1.11.5] - 2021-06-16
### Changed
 - Kit Editor: Tweaked resampler lowpass-filter settings to preserve more treble.

### Added
 - Kit Editor: Preferences menu for low-pass filter and dither.

## [1.11.4] - 2021-06-01
### Fixed
 - Palette Editor: Color picker could not pick black (0,0,0) due to UI scaling bug.

### Added
 - Palette Editor: Show selected color RGB value in color picker.

## [1.11.3] - 2021-04-25
### Changed
 - Kit Editor: reverted 1.11.2 changes due to cymbal dithering problems.

## [1.11.2] - 2021-04-23
### Changed
 - Kit Editor: tweaked DC level to reduce DMG noise. works best with
   lsdj 9.2.2 and above.

## [1.11.1] - 2021-04-18
### Changed
 - Kit Editor: added internal versioning field to kits, that tells if they were
   created using lsdpatcher 1.11.1 or above. this allows adding old kits to
   lsdj 9.2.0+ without loss of quality.

## [1.11.0] - 2021-04-15
### Fixed
 - Kit Editor: sample preview playback had inverted polarity.
 - Song Manager: remember last used .lsdprj path.
 - Clearing kits would not free up space for adding .lsdprj songs.
 - Minor UI issues.

### Changed
 - Kit Editor: when creating kits, wave frames are now rotated right, so that
   the sample to be played back last is written first.
   this compensates for the Game Boy wave refresh bug that plays back samples
   in wrong order after changing wave.
   best used with Little Sound Dj 9.2.0 and above.

## [1.10.5] - 2021-03-05
### Fixed
 - Kit Editor: inversed sample polarity. broken since always.
 - Kit Editor: garbled sample names when renaming an uninitialized kit.
 - Kit Editor: slow switching to high numbered kits.
 - ROM upgrade tool would not find new versions ending with A-Z.
 - Disabled ROM upgrade if there are unsaved changes.

### Added
 - Kit Editor: F1/F2 shortcut for previous/next bank.
 - Kit Editor: Sample pad tooltip.

### Removed
 - Kit Editor: wave blending, which didn't quite work due to timing issues.

## [1.10.4] - 2021-02-22
### Fixed
 - Kit Editor: removed sample prelisten low-pass filter.

## [1.10.3] - 2021-02-12
### Fixed
 - Kit Editor: various volume and trimming errors.
 - Font Editor: avoid duplicate font names.
 - Font Editor: .png file extension got included in font name when loading a font PNG.
 - Error handling when palettes cannot be parsed.
 - .sav file not found for ROM images ending with ".gb.gb".
 - ROM upgrade did not preserve graphics characters.

### Changed
 - Subwindows are now modal.

### Added
 - Kit Editor: bank switching buttons.

## [1.10.2] - 2020-11-22
### Fixed
 - Kit Editor: when replacing samples, trim sample end to fit.

## [1.10.1] - 2020-11-10
### Fixed
 - New version check at startup.

## [1.10.0] - 2020-11-10
### Fixed
 - Kit Editor: sample duration right alignment.
 - Kit Editor: made text fields handle "enter" key.
 - Kit Editor: stop listening to keypresses when window is closed.
 - Kit Editor: make focus return to main window when bank is changed.
 - Various window resize issues.

### Added
 - Kit Editor: pitch spinner, for changing sample pitch by semitone.
 - Kit Editor: trim spinner, for reducing sample length.
 - New version check at startup.

### Changed
 - Kit Editor: now removes DC offset before resampling.
 - Kit Editor: changed "Reload samples" button to "Reload sample".
 - Kit Editor: when adding a too big sample, trim sample end to fit.
 - Kit Editor: disabled "Add sample" button when kit is full.

## [1.9.0] - 2020-10-31
### Fixed
 - Kit Editor: dramatically improved resampling using libresample4j.
 - Kit Editor: refresh sample view when the sample is reloaded.
 - Kit Editor: update "seconds free" after volume change.
 - Kit Editor: pad kit banks with "rst" instead of "nop" instructions, for crash detection.

### Added
 - Kit Editor: print sample duration in sample view.

## [1.8.1] - 2020-10-25
### Fixed
 - Kit Editor: loading kits with sample volumes stored in settings file.
 - Kit Editor: reduced wave blending noise for emulators that do not have the Game Boy wave refresh bug.
 - Palette Editor: force palette names to upper case.

## [1.8.0] - 2020-10-24
### Fixed
 - Palette Editor: avoid duplicate palette names when loading a palette.
 - Palette Editor: dragging color picker sliders is now more responsive.
 - Palette Editor: improved color picker visibility.
 - Kit Editor: update of "bytes used" field.
 - Font Editor: when loading font from .png, set font name from the file name.
 - Some file dialogs would not remember the last used directory.
 - Saving a ROM when no SAV has been loaded.
 - Switching .sav would not take effect until loading a ROM.

### Added
 - Kit Editor: MPC-like UI with pads. Play by clicking or keys 1234QWERASDFZXC. Right-click pad to rename, replace or delete sample.
 - Kit Editor: "Reload samples", "Save ROM", "Clear kit" buttons.
 - Kit Editor: automatic silence trimming.
 - Kit Editor: when saving kits, remember source sample files + volumes.
 - Font Editor: support for editing graphics characters.

### Changed
 - Kit Editor: "Add Sample" now automatically resamples, normalizes and dithers the sample. No need to prepare samples using sox anymore.
 - Kit Editor: switched to TPDF dither for improved sound.
 - Kit Editor: when adding samples, blend wave frames to reduce impact of the [Game Boy wave refresh bug](https://www.devrs.com/gb/files/gbsnd3.gif).
 - Kit Editor: volume control now adjusts sample volume instead of pre-listening volume.
 - Kit Editor: improved sound playback quality.
 - Kit Editor: click sample view to play.
 - Kit Editor: half-speed setting now also affects "Add sample".
 - Kit Editor: renamed "Export kit" to "Save kit".
 - Kit Editor: show unused space in seconds instead of bytes.
 - Palette Editor: improved mid-tone generation.
 - Various file dialog improvements.
 - Improved command line feedback.

### Removed
 - Font Editor: removed saving of .lsdfnt files, as well as loading/saving multiple fonts in one go.
 - Kit Editor: "Play sample on click" toggle.
 - Kit Editor: "Export all samples" button.

## [1.7.0] - 2020-10-06
### Fixed
 - Kit Editor: sample export broke in 1.6.0.
 - Song Editor: incorrect broken-song warnings. thx michael dufault!

### Added
 - Palette Editor: color picker!
 - Palette Editor: click in lsdj screens to select color.
 - Palette Editor: "swap color" and "clone color" buttons.
 - Palette Editor: "raw" button, which displays colors as-is.

### Changed
 - Palette Editor: switched color correction from Gambatte to Sameboy.
 - Palette Editor: updated screenshots to LSDj v8.9.0.
 - Palette Editor: create brighter mid-tones if the background is brighter than the foreground.
 - Each file extension now has its own last used load/save file path.

### Removed
 - Palette Editor: color spinners.

## [1.6.0] - 2020-10-02
### Fixed
 - Kit sample playback got stuck at times.
 - Old LSDj ROMs (like, v3) would not open.

### Added
 - Startup dialog to choose ROM, SAV and sub-tool.
 - Added song manager from LSDManager project.
 - Song manager warning on corrupted songs.
 - Song manager now saves LSDj Project files (.lsdprj) which contains both song and sample kits.
 - Upgrade ROM button, which downloads the latest ROM images from https://www.littlesounddj.com. The upgrade preserves custom kits, fonts and palettes.
 - Palette editor randomize button.

### Changed
 - Palette editor layout.

## [1.5.0] - 2020-09-13
### Changed
 - Merged LSDPatcher Redux v1.4.6. Full list of changes at [LSDPatcher Redux release page](https://github.com/Eiyeron/lsdpatch/releases).

## [1.4.2] - 2020-08-19
### Added
 - LSDj v8.8.3 support.

## [1.4.1] - 2020-07-04
### Added
 - LSDj v8.7.4 support.

## [1.4.0] - 2020-07-02
### Added
 - Palette editor "Desaturate preview" toggle.
 - Palette editor copy/paste.

## [1.3.0] - 2020-06-27
### Added
 - Allow variable number of palettes. Some LSDj versions have 6 palettes, others 7.

## [1.2.0] - 2020-03-05
### Changed
 - Java 8 now required.
 - Brighter background shade for DMG fonts.

## [1.1.6] - 2017-10-19
### Fixed
 - Recalculate ROM checksum on save.

## [1.1.5] - 2017-10-14
### Added
 - "Import kits from ROM" button.

## [1.1.4] - 2017-05-07
### Changed
 - Kit selector is now hexadecimal.
 - Brought back dot in kit list.

## [1.1.3] - 2017-01-26
### Changed
 - Made palette editor a bit bigger.

### Fixed
 - Shaded and inverted tiles would not always be generated by font editor.

## [1.1.2] - 2017-01-23
### Added
 - Load and save palettes.

### Fixed
 - When loading a ROM, palettes would be added twice.
 - Errors related to combo box in font editor.

## [1.1.1] - 2017-01-23
### Added
 - Font renaming.
 - Font editor grid.
 - Include font name in .lsdfnt

### Fixed
 - Out-of-bounds drawing in font editor.

## [1.1.0] - 2017-01-23
### Added
 - Font editor.

## [1.0.2] - 2017-01-20
### Changed
 - Made preview screens in palette editor bigger.

## [1.0.1] - 2017-01-20
### Fixed
 - Wrong behavior when loading invalid ROM images.

### Changed
 - Lowered required JRE version to 1.6.

## [1.0.0] - 2017-01-20
### Added
 - Palette editor, entered by menu option Palette->Edit Palette.

## [0.19] - 2011-08-20
### Fixed
 - Loading a long sample threw a confusing, empty error message. Thanks to Clay Morrow for reporting.
