package com.jurgendevries.plants;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Pair;

public class ImageResizer {

	/*
	 * Call this static method to resize an image to a specified width and height.
	 * 
	 * @param targetWidth  The width to resize to.
	 * @param targetHeight The height to resize to.
	 * @returns 		   The resized image as a Bitmap.
	 */
	public static Bitmap resizeImage(byte[] imageData, int targetWidth, int targetHeight, int cropW, int cropH) {
		// Use BitmapFactory to decode the image

        Bitmap reducedBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(reducedBitmap, targetWidth, targetHeight, false);

        return resizedBitmap;
	}

    public static Bitmap resizeImageToSquare(byte[] imageData, int shorterSideTarget) {
        Pair<Integer, Integer> dimensions = getDimensions(imageData);

        // Determine the aspect ratio (width/height) of the image
        int imageWidth = dimensions.first;
        int imageHeight = dimensions.second;

        int cropW = (imageWidth - imageHeight) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (imageHeight - imageWidth) / 2;
        cropH = (cropH < 0)? 0: cropH;

        return resizeImage(imageData, shorterSideTarget, shorterSideTarget, cropW, cropH);
    }

	public static Pair<Integer, Integer> getDimensions(byte[] imageData) {
		// Use BitmapFactory to decode the image
        BitmapFactory.Options options = new BitmapFactory.Options();

        // Only decode the bounds of the image, not the whole image, to get the dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
        
        return new Pair<Integer, Integer>(options.outWidth, options.outHeight);
	}

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}

    public static Bitmap getRoundedShape(Bitmap roundBitmap, int targetSize) {
        //int targetWidth = 110;
        //int targetHeight = 110;
        Bitmap targetBitmap = Bitmap.createBitmap(targetSize,
                targetSize,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetSize - 1) / 2,
                ((float) targetSize - 1) / 2,
                (Math.min(((float) targetSize),
                        ((float) targetSize)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = roundBitmap;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetSize, targetSize), new Paint(Paint.FILTER_BITMAP_FLAG));
        return targetBitmap;
    }
}
