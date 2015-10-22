package se.codeunlimited.android.bitmap_utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Peter on 10-Oct-15.
 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

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

    private static int getRotation(String fileName){
        try {
            File imageCaptureFile = new File(fileName);
            ExifInterface exif = new ExifInterface(imageCaptureFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) return 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) return 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) return 270;
        }catch(IOException e) {
            Log.e(TAG, "Failed to read rotation from file: " + fileName, e);
        }
        return 0;
    }

    private static Bitmap rotate(Bitmap bmp, int rotation) {
        if (rotation == 0)
            return bmp;

        try{
            Matrix matrix = new Matrix();
            matrix.preRotate(rotation);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }finally{
            bmp.recycle();
        }
    }

    public static Bitmap decodeSampledBitmap(String fileName, int reqWidth, int reqHeight) {

        final int rotation = getRotation(fileName);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap thumb = null;
        try {
            thumb = BitmapFactory.decodeFile(fileName, options);
            return rotate(thumb, rotation);
        }finally {
            if (thumb!=null) {
                thumb.recycle();
            }
        }
    }

    public static void createThumb(String fileName, int reqWidth, int reqHeight, String thumbFile) {
        Bitmap thumb = null;
        try{
            thumb = decodeSampledBitmap(fileName, reqWidth, reqHeight);
            BitmapUtils.saveBitmap(thumb, thumbFile);
        }finally {
            if (thumb!=null) {
                thumb.recycle();
            }
        }
    }

    public static void saveBitmap(final Bitmap bmp, final String thumbFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(thumbFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            Log.e(TAG, "Failed to save bitmap", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to save bitmap", e);
            }
        }
    }
}
