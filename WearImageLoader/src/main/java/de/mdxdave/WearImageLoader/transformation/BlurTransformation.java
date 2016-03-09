package de.mdxdave.WearImageLoader.transformation;

import android.graphics.Bitmap;

import de.mdxdave.WearImageLoader.utils.FastBlurHelper;

public class BlurTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap result = FastBlurHelper.doBlur(source, 10, false);
        if (result != source) {
            // Same bitmap is returned if sizes are the same
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return "BlurTransformation2";
    }
}