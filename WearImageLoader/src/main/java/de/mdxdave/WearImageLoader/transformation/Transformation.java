package de.mdxdave.WearImageLoader.transformation;

import android.graphics.Bitmap;

public interface Transformation {

    public abstract Bitmap transform(Bitmap bitmap);

    public abstract String key();
}
