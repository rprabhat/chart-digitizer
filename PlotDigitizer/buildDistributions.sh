#!/bin/sh
# Script used to collect together and archive the various Plot Digitizer distributions.

# The MacOSX distribution can only be created on a MacOS X platform
# but this can be commented out for running this on other platforms.

# Build the windows distribution.
DEST_FILE_PFX="PlotDigitizer_Windows"
DEST_DIR="dist/"$DEST_FILE_PFX
rm -rf $DEST_DIR &>/dev/null
mkdir $DEST_DIR
cp LesserGPL_license.txt $DEST_DIR/.
cp GPL_license.txt $DEST_DIR/.
cp jars/PlotDigitizer.jar $DEST_DIR/.
cp dist/PlotDigitizer.exe $DEST_DIR/.
cp README.txt $DEST_DIR/.
fold -s $DEST_DIR/README.txt > $DEST_DIR/tmp.txt
mv $DEST_DIR/tmp.txt $DEST_DIR/README.txt
svn export svn://svn.code.sf.net/p/plotdigitizer/code/trunk/SamplePlots $DEST_DIR/SamplePlots
cd dist
rm $DEST_FILE_PFX.zip &>/dev/null
zip -9 -l -r $DEST_FILE_PFX $DEST_FILE_PFX
cd ..

# Build the Linux distribution.
DEST_FILE_PFX="PlotDigitizer_Linux_and_Others"
DEST_DIR="dist/"$DEST_FILE_PFX
rm -rf $DEST_DIR &>/dev/null
mkdir $DEST_DIR
cp LesserGPL_license.txt $DEST_DIR/.
cp GPL_license.txt $DEST_DIR/.
cp jars/PlotDigitizer.jar $DEST_DIR/.
cp README.txt $DEST_DIR/.
cp -r dist/PlotDigitizer_Windows/SamplePlots $DEST_DIR/SamplePlots
tar -cvzf dist/$DEST_FILE_PFX.tgz -C dist $DEST_FILE_PFX

# Build the source code distribution.
DEST_FILE_PFX="PlotDigitizer_Source"
DEST_DIR="dist/"$DEST_FILE_PFX
rm -rf $DEST_DIR &>/dev/null
svn export svn://svn.code.sf.net/p/plotdigitizer/code/trunk $DEST_DIR
mkdir $DEST_DIR/dist
cp jars/PlotDigitizer.jar $DEST_DIR/dist/.
cp -r dist/PlotDigitizer.app $DEST_DIR/dist/.
cp buildDistributions.sh $DEST_DIR/.
mkdir $DEST_DIR/Third_Party
svn export https://svn.java.net/svn/mrjadapter~svn/trunk/ $DEST_DIR/Third_Party/MRJAdapter
cd dist
rm $DEST_FILE_PFX.zip &>/dev/null
zip -9 -r $DEST_FILE_PFX $DEST_FILE_PFX
cd ..


# Build the MacOS X distribution.
export DEST_FILE_PFX="PlotDigitizer_MacOSX"
export DEST_DIR="dist/"$DEST_FILE_PFX
rm -rf $DEST_DIR &>/dev/null
mkdir $DEST_DIR
cp LesserGPL_license.txt $DEST_DIR/.
cp GPL_license.txt $DEST_DIR/.
cp -r dist/PlotDigitizer.app $DEST_DIR/.
cp README.txt $DEST_DIR/.
cp -r dist/PlotDigitizer_Windows/SamplePlots $DEST_DIR/SamplePlots
hdiutil create dist/PlotDigitizer_tmp.dmg -srcfolder $DEST_DIR -fs HFS+ -format UDRW -volname "Plot Digitizer" -ov 
# hdiutil convert dist/PlotDigitizer_tmp.dmg -format UDBZ -o dist/$DEST_FILE_PFX.dmg
# rm dist/PlotDigitizer_tmp.dmg

# Copy in the release notes.
cp Release_Notes.txt dist/Readme.txt
