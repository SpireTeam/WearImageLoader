package de.mdxdave.WearImageLoader.daemon;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class ResizeTransformation implements Transformation {
    private int targetWidth;

    public ResizeTransformation(int width) {
        this.targetWidth = width;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        double aspectRatio = (double) source.getHeight() / (double) source.getWidth();

        if (source.getWidth() <= targetWidth) {
            return source;
        }

        int targetHeight = (int) (targetWidth * aspectRatio);
        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
        if (result != source) {
            // Same bitmap is returned if sizes are the same
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return "ResizeTransformation" + targetWidth;
    }
}
