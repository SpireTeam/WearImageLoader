package de.mdxdave.WearImageLoader.daemon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WearImageLoaderDaemon implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener, DataApi.DataListener, GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = WearImageLoaderDaemon.class.getCanonicalName();

    private Context mContext;
    private String mAuthHeaderToken;
    private GoogleApiClient mApiClient;
    private CallBack mCallBack;
    private String mUrl;
    private OkHttpClient mHttpClient;
    private OkHttp3Downloader mDownloader;
    private Picasso mPicasso;

    private static final String DEFAULT_PATH = "/WearImageLoader/";
    private String imageAssetName = "image";

    private final static String MESSAGE_WEARIMAGELOADER = "/WearImageLoader/";
    private final static String WEARIMAGELOADER_PATH = "/WearImageLoader/";

    private static WearImageLoaderDaemon INSTANCE;

    private WearImageLoaderDaemon(Context context) {
        this.mContext = context;

        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        mHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder requestBuilder = chain.request().newBuilder();

                        Log.d("TESTING STUFF", chain.request().url().host());
                        if (mAuthHeaderToken != null && chain.request().url().host().contains("spire.fit")) {
                            requestBuilder.addHeader("Authorization", "Bearer " + mAuthHeaderToken);
                        }

                        return chain.proceed(requestBuilder.build());
                    }
                }).build();

        mDownloader = new OkHttp3Downloader(mHttpClient);

        mPicasso = new Picasso.Builder(mContext).downloader(mDownloader).build();
        //TODO disconnect when the application close
    }

    public static WearImageLoaderDaemon with(Context context, String authHeaderToken) {
        if (INSTANCE == null) {
            INSTANCE = new WearImageLoaderDaemon(context);
        }

        if (context != null) {
            INSTANCE.mContext = context;
            INSTANCE.mAuthHeaderToken = authHeaderToken;
        }

        return INSTANCE;
    }

    public String getImageAssetName() {
        return imageAssetName;
    }

    public WearImageLoaderDaemon setImageAssetName(String imageAssetName) {
        this.imageAssetName = imageAssetName;
        return this;
    }

    public Bitmap getBitmapFromURL(String url) {
        try {
            Bitmap bitmap = Picasso.with(mContext)
                    .load(url)
                    //.transform(new ResizeTransformation(300))
                    .get();

            Log.d(TAG, "image getted " + url);

            return bitmap;

        } catch (IOException e) {
            Log.e(TAG, "getImage error " + url, e);
        }
        return null;
    }

    public static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private void sendImage(final String url, final String path) {
        mPicasso
            .load(url)
            .transform(new ResizeTransformation(300))
            .into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d(TAG, "Picasso " + url + " loaded");
                    sentBitmap(bitmap, url, path);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.d(TAG, "Picasso " + url + " failed");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
    }

    private String generatePath(final String url, final String path) {
        if (path == null)
            return DEFAULT_PATH + url.hashCode();
        else
            return path;
    }

    private void sentBitmap(Bitmap bitmap, final String url, final String path) {
        if (bitmap != null) {
            final Asset asset = createAssetFromBitmap(bitmap);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String finalPath = generatePath(url, path);

                    final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(finalPath);

                    putDataMapRequest.getDataMap().putString("timestamp", new Date().toString());

                    putDataMapRequest.getDataMap().putAsset("image", asset);

                    if (mApiClient != null && mApiClient.isConnected())
                        Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest()).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {

                                boolean isSuccess = dataItemResult.getStatus().isSuccess();

                                if (isSuccess) {
                                    Log.d(TAG, url + " send");
                                } else {
                                    Log.d(TAG, url + " send error");
                                }

                                if (mCallBack != null) {
                                    if (isSuccess) {
                                        mCallBack.onBitmapSent(url, finalPath);
                                    } else {
                                        mCallBack.onBitmapError(url, finalPath);
                                    }
                                }
                            }
                        });
                    else {
                        Log.d(TAG, "ApiClient null of not connected");

                        if (mCallBack != null)
                            mCallBack.onBitmapError(url, path);
                    }
                }
            }).start();

        } else {
            if (mCallBack != null)
                mCallBack.onBitmapError(url, path);
        }
    }

    public WearImageLoaderDaemon callBack(CallBack callBack) {
        this.mCallBack = callBack;
        return this;
    }


    public WearImageLoaderDaemon load(final String url) {
        this.mUrl = url;
        return this;
    }

    public void send() {
        into(null);
    }

    public void into(final String path) {
        if (path != null && !path.trim().isEmpty()) {
            if (mUrl == null) {
                Log.d(TAG, "must execute .load(url) before");
            } else {
                Log.d(TAG, "load " + mUrl);

                final String tmpUrl = mUrl;

                //main handler for picasso
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        sendImage(tmpUrl, path);
                    }
                });
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mApiClient, this);
        Wearable.DataApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, messageEvent.toString());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, dataEvents.toString());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void handleMessage(MessageEvent messageEvent) {
        final String path = messageEvent.getPath();

        if (path.equals(MESSAGE_WEARIMAGELOADER)) {
            String message = new String(messageEvent.getData());
            if (message.startsWith("http") || message.startsWith("www")) {
                String sendPath = message.hashCode() + "";

                load(message).into(WEARIMAGELOADER_PATH + sendPath);
            }
        }
    }

    public interface CallBack {
        void onBitmapSent(String url, String path);

        void onBitmapError(String url, String path);
    }

}
