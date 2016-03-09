DaVinci
=======

[![Release](https://jitpack.io/v/de.mdxdave/WearImageLoader.svg?style=flat-square)](https://jitpack.io/#de.mdxdave/WearImageLoader)

![Alt DaVinciDroid](https://raw.githubusercontent.com/florent37/DaVinci/master/mobile/src/main/res/drawable-hdpi/davinci_new_small.jpg)

WearImageLoader is an image downloading and caching library for Android Wear

Usage
--------

Use WearImageLoader from your SmartWatch app
```java
WearImageLoader.with(context).load("/image/0").into(imageView);
WearImageLoader.with(context).load("http://i.imgur.com/o3ELrbX.jpg").into(imageView);
```

Into an imageview
```java
WearImageLoader.with(context).load("/image/0").into(imageView);
```

Into a FragmentGridPagerAdapter
```java
@Override
public Drawable getBackgroundForRow(final int row) {
    return WearImageLoader.with(context).load("/image/" + row).into(this, row);
}
```

Into a CallBack
```java
DaVinci.with(context).load("http://i.imgur.com/o3ELrbX.jpg").into(new WearImageLoader.Callback() {
            @Override
            public void onBitmapLoaded(String path, Bitmap bitmap) {

            }
});
```

By default, the asset name used for the bitmap is "image", you can modify this 
```java
WearImageLoader.with(context).load("/image/0").setImageAssetName("myImage").into(imageView);
```

Send Bitmaps
--------

In your smartphone service
```java
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        WearImageLoaderDaemon.with(getApplicationContext()).handleMessage(messageEvent);
        ...
    }
```

Preload Bitmaps
--------

Send image to wear
```java
WearImageLoaderDaemon.with(getApplicationContext()).load("http://i.imgur.com/o3ELrbX.jpg").send();
```

or with "/image/0" path
```java
WearImageLoaderDaemon.with(getApplicationContext()).load("http://i.imgur.com/o3ELrbX.jpg").into("/image/0");
```

Image Transformation
--------

You can specify custom transformations on your Bitmaps

```java
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
```

Pass an instance of this class to the transform method

```java
WearImageLoader.with(context).load(url).transform(new ResizeTransformation(300)).into(imageView);
```

Prodvided Transformations :

**Blur**
```java
WearImageLoader.with(context).load(url).transform(new BlurTransformation()).into(imageView);
```

**Resizing**
```java
WearImageLoader.with(context).load(url).transform(new ResizeTransformation(maxWidth)).into(imageView);
```

Include
-------


In your wear module
```groovy
repositories {
    maven { url "https://jitpack.io" }
}

compile 'de.mdxdave.WearImageLoader:WearImageLoader:1.1.1';
```

In your smartphone module 
```groovy
repositories {
    maven { url "https://jitpack.io" }
}

compile 'de.mdxdave.WearImageLoader:Daemon:1.1.1';
```

Don't forget to add WRITE_EXTERNAL_STORAGE in your Wear AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

TODO
--------

- Customize bitmap resizing (actually : width=300px)
- Enabling multiples transformations
- Apply transformations on Smartphone then send them to Wear


Dependencies
-------

* [Picasso][picasso] used in DaVinciDaemon (from Square)
* [DiskLruCache][disklrucache] used in DaVinci (from JakeWharton)

Changelog
-------

**1.1.1**
- Made runnable again

Credits
-------

Author: Florent Champigny www.florentchampigny.com/
Updated by: MDXDave https://mdxdave.de 


Pictures by Logan Bourgouin

<a href="https://plus.google.com/+LoganBOURGOIN">
  <img alt="Follow me on Google+"
       src="https://raw.githubusercontent.com/florent37/DaVinci/master/mobile/src/main/res/drawable-hdpi/gplus.png" />
</a>

License
--------

    Copyright 2015 florent37, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[snap]: https://oss.sonatype.org/content/repositories/snapshots/
[android_doc]: https://developer.android.com/training/wearables/data-layer/assets.html
[tuto_wear]: http://tutos-android-france.com/developper-une-application-pour-les-montres-android-wear/
[picasso]: https://github.com/square/picasso
[disklrucache]: https://github.com/JakeWharton/DiskLruCache
