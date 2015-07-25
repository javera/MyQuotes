
package com.mjaworski.myQuotes.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class ImageUtils
{
    public static final int COVER_IMAGE_WIDTH_DP = 192;
    public static final int COVER_IMAGE_HEIGHT_DP = 192;
    
    public static Bitmap decodeSampledBitmapFromUri(Uri bitmapURI, float reqWidth, float reqHeight)
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        
//        if (bitmapURI.getPath().contains("#"))
//            BitmapFactory.decodeFile(bitmapURI.toString(), options);
//        else
//            BitmapFactory.decodeFile(bitmapURI.getPath(), options);
           
        
        BitmapFactory.decodeFile(bitmapURI.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(bitmapURI.getPath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, float reqWidth, float reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / reqHeight);
            final int widthRatio = Math.round((float) width / reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
