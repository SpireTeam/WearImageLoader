package de.mdxdave.WearImageLoader.transformation;

import android.graphics.Bitmap;

public class ResizeTransformation implements Transformation {
    private int targetWidth;

    public ResizeTransformation(int width) {
        this.targetWidth = width;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
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
        return "ResizeTransformation"+targetWidth;
    }
}
