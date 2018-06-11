/**
*   Please feel free to use any fragment of the code in this file that you need
*   in your own work. As far as I am concerned, it's in the public domain. No
*   permission is necessary or required. Credit is always appreciated if you
*   use a large chunk or base a significant product on one of my examples,
*   but that's not required either.
*
*   This code is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*
*      --- Joseph A. Huwaldt
**/
package jahuwaldt.image;


import java.awt.image.ColorModel;
import java.awt.image.ImageFilter;
import java.util.Arrays;
import java.awt.Rectangle;


/**
* Applies a Median filter that replaces each pixel by the median of
* itself and its neighbors. The number of neighbors can be defined
* with the setArea methods.
* <p>
* Can be used as despeckle filter, but the image will lose sharpness.
* The larger the area becomes, the less noise and the less sharpness will remain,
* and the longer it will take.
* <p>
* @author  Joseph A. Huwaldt   Date:  September 24, 2002
* @version October 1, 2012
**/
public class MedianFilter extends ImageFilter {

    private static ColorModel defaultRGB = ColorModel.getRGBdefault();

    private int aWidth=3, aHeight=3;
    private int raster[];
    private int width, height;

    /**
    *  Construct a MedianFilter that has a neighbor window used to
    *  determine each pixel's median that is 3 x 3 pixels.
    **/
    public MedianFilter() {}

    /**
    *  Construct a MedianFilter that has a neighbor window used to
    *  determine each pixel's median that is the given width
    *  and height in pixels.
    *
	*  @param width   width of window in pixels, must be 1 or
	*                 larger and odd.
	*  @param height  height of window in pixels, must be 1 or
	*                 larger and odd.
    **/
    public MedianFilter(int width, int height) throws IllegalArgumentException {
        aWidth = width;
        aHeight = height;
		validateInputs();
    }

	/**
	* Sets the area of the window to be used to determine
	* each pixel's median to the argument width and height.
	*
	* @param width   width of window in pixels, must be 1 or
	*                larger and odd.
	* @param height  height of window in pixels, must be 1 or
	*                larger and odd.
	**/
	public void setArea(int width, int height) throws IllegalArgumentException {
		aWidth = width;
		aHeight = height;
		validateInputs();
	}
	
	/**
	*  Method that validates the user's input for the analysis window size.
	**/
	private void validateInputs() throws IllegalArgumentException {
		if (aWidth < 0) aWidth *= -1;
		if (aHeight < 0) aHeight *= -1;
		if (aWidth == 0)	aWidth = 1;
		if (aHeight == 0)   aHeight = 1;
		
		if (even(aWidth) || even(aHeight))
			throw new IllegalArgumentException("width & height must be odd");
	}

	/**
	*  Test to see if a given integer is even.
	*
	*  @param   n  Integer number to be tested.
	*  @return  True if the number is even, false if it is odd.
	**/
	private static boolean even( int n ) {
		return (n & 1) == 0;
	}

    /**
    * Filters the information provided in the setDimensions method
    * of the ImageConsumer interface.
    * <p>
    * Note: This method is intended to be called by the ImageProducer
    * of the Image whose pixels are being filtered.  Developers using
    * this class to filter pixels from an image should avoid calling
    * this method directly since that operation could interfere      
    * with the filtering operation.  
    * @see ImageConsumer#setDimensions
    **/
    @Override
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        raster = new int[width*height];
        consumer.setDimensions(width, height);
    }

    /**
    * Filter the information provided in the setColorModel method
    * of the ImageConsumer interface.
    * <p>
    * Note: This method forces the consumer's color model to be
    * the default RGB model.
    * @see ImageConsumer#setColorModel
    **/
    @Override
    public void setColorModel(ColorModel model) {
        consumer.setColorModel(defaultRGB);
    }

    /**
    * Filters the information provided in the setHints method
    * of the ImageConsumer interface.
    * <p>
    * Note: This method sets the consumer's hints to be
    * TOPDOWNLEFTRIGHT | COMPLETESCANLINES | SINGLEPASS |
    * (hintflags & SINGLEFRAME).
    * @see ImageConsumer#setHints
    **/
    @Override
    public void setHints(int hintflags) {
        consumer.setHints(TOPDOWNLEFTRIGHT
                          | COMPLETESCANLINES
                          | SINGLEPASS
                          | (hintflags & SINGLEFRAME));
    }

    /**
    * Filters the information provided in the setPixels method of the
    * ImageConsumer interface which takes an array of bytes.
    * <p>
    * Note: This method is intended to be called by the ImageProducer
    * of the Image whose pixels are being filtered.  Developers using
    * this class to filter pixels from an image should avoid calling
    * this method directly since that operation could interfere      
    * with the filtering operation.  
    * @see ImageConsumer#setPixels
    **/
    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model,
                          byte pixels[], int off, int scansize) {
        int srcoff = off;
        int dstoff = y*width + x;
        for (int yc = 0; yc < h; yc++) {
            for (int xc = 0; xc < w; xc++) {
                raster[dstoff++] = model.getRGB(pixels[srcoff++] & 0xff);
            }
            srcoff += (scansize - w);
            dstoff += (width - w);
        }
    }

    /**
    * Filters the information provided in the setPixels method of the
    * ImageConsumer interface which takes an array of integers.
    * <p>
    * Note: This method is intended to be called by the ImageProducer
    * of the Image whose pixels are being filtered.  Developers using
    * this class to filter pixels from an image should avoid calling
    * this method directly since that operation could interfere      
    * with the filtering operation.  
    * @see ImageConsumer#setPixels
    **/
    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model,
                          int pixels[], int off, int scansize) {
        int srcoff = off;
        int dstoff = y*width + x;
        if (model == defaultRGB) {
            for (int yc = 0; yc < h; yc++) {
                System.arraycopy(pixels, srcoff, raster, dstoff, w);
                srcoff += scansize;
                dstoff += width;
            }
        } else {
            for (int yc = 0; yc < h; yc++) {
                for (int xc = 0; xc < w; xc++) {
                    raster[dstoff++] = model.getRGB(pixels[srcoff++]);
                }
                srcoff += (scansize - w);
                dstoff += (width - w);
            }
        }
    }

    /**
    * Filters the information provided in the imageComplete method of
    * the ImageConsumer interface.
    * <p>
    * Note: This method is intended to be called by the ImageProducer
    * of the Image whose pixels are being filtered.  Developers using
    * this class to filter pixels from an image should avoid calling
    * this method directly since that operation could interfere      
    * with the filtering operation.
    * <p>
    * This is where the median filtering is actually being done.
    *
    * @see ImageConsumer#imageComplete
    **/
    @Override
    public void imageComplete(int status) {
        if (status == IMAGEERROR || status == IMAGEABORTED) {
            consumer.imageComplete(status);
            return;
        }
        
        //  Allocate memory for our array of neighbors.
        int areaLength = aWidth*aHeight;
        int[] area = new int[areaLength];
        int[] band = new int[areaLength];

        //  Allocate memory for the pixels in the raster image.
        int numPixels = width*height;
        int pixels[] = new int[numPixels];
        
        int awo2 = aWidth/2;
        int aho2 = aHeight/2;

        int pos = 0;
        for (int y = 0; y < height; ++y) {
            
            //  Deal with edge regions.
            if (y < aho2 || y >= height-aho2) {
                System.arraycopy(raster, pos, pixels, pos, width);
                pos += width;
                continue;
            }
            
            for (int x = 0; x < width; ++x) {
                //  Deal with edge regions.
                if (x < awo2 || x >= width-awo2) {
                    pixels[pos] = raster[pos];
                    ++pos;
                    continue;
                }
                
                fillArea(pos, awo2, aho2, area);
                pixels[pos] = median(area, band);
                
                ++pos;
            }
        }

        //  Pass along the newly filtered pixels.
        consumer.setPixels(0, 0, width, height, defaultRGB, pixels, 0, width);
       
        consumer.imageComplete(status);
    }

    /**
    *  Method that fills in the matrix of neighbor pixels that
    *  are used to determine the median.
    *
    *  @param  pos  Position in the overall raster array of the
    *               target pixel (center of the area matrix).
    *  @param  wo2  The width of the area matrix/2.
    *  @param  ho2  The height of the area matrix/2.
    *  @param  area Reference to the area array [aWidth*aHeight]
    *               that will be filled in.
    **/
    private void fillArea(int pos, int wo2, int ho2, int[] area) {
        
        int aPos = 0;
        int pos0 = pos - ho2*width - wo2;
        
        for (int i=0; i < aHeight; ++i) {
            pos = pos0 + i*width;
            
            for (int j=0; j < aWidth; ++j)
                area[aPos++] = raster[pos++];
        }
        
    }

    /**
    *  Method that computes the median pixel of all the pixels in the
    *  specified matrix of neighboring pixels.  The median of each color
    *  band is found separately.
    *
    *  @param  area  The matrix of neighboring pixels used to compute the
    *                the median.  The target pixel is at the center of the
    *                matrix.
    *  @param  band  Temporary, preallocated, storage space used to store
    *                individual color bands during processing.
    **/
    private static int median(int[] area, int[] band) {

		int mPos = (int)(0.5F*(area.length - 1));
		
        int alpha = medianBand(area, band, 24, mPos);
        int red = medianBand(area, band, 16, mPos);
        int green = medianBand(area, band, 8, mPos);
        int blue = medianBand(area, band, 0, mPos);

        int median = (alpha << 24) | (red << 16) | (green << 8) | blue;
        return median;
    }

    /**
    *  Method that computes the median for a single color band from all the pixels in
    *  the specified matrix of neighboring pixels.
    *
    *  @param  area       The matrix of neighboring pixels used to compute the
    *                     the median.  The target pixel is at the center of the
    *                     matrix.
    *  @param  tmpArr     Temporary, preallocated, storage space used to store
    *                     individual color bands during processing.
    *  @param  bitOffset  The offset into the integer of the color band we are
    *                     interested in (24 - alpha, 16 - red, 8 - green, 0 - blue).
    *  @param  mPos       Position of the median value in the sorted array of color
    *                     values:  mPos = 0.5*(length - 1).
    **/
    private static int medianBand(int[] area, int[] tmpArr, int bitOffset, int mPos) {
        //  Extract the color band of interest.
        int length = area.length;
        for (int i=0; i < length; ++i)
            tmpArr[i] = (area[i] >> bitOffset) & 0xff;

        //  Sort color band array.
        Arrays.sort(tmpArr);
        
        //  Return the median value.
        return (tmpArr[mPos]);
    }
    
}
