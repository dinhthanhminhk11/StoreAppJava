/*
 * Copyright 2018 Kaushik N. Sanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kaushiknsanji.storeapp.workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.cache.BitmapImageCache;
import com.example.kaushiknsanji.storeapp.utils.ImageStorageUtility;

import java.io.IOException;

public class ImageDownloader extends AsyncTaskLoader<Bitmap> {

    //Integer Constant of the Loader
    final static int IMAGE_LOADER = 1000;
    //Constant used for logs
    private static final String LOG_TAG = ImageDownloader.class.getSimpleName();
    //Stores the Image URL String from which the Image needs to be downloaded
    private String mImageURLStr;

    //Stores the Bitmap Downloaded
    private Bitmap mDownloadedBitmap;

    ImageDownloader(Context context, String imageURLStr) {
        super(context);
        mImageURLStr = imageURLStr;
    }

    @Override
    public Bitmap loadInBackground() {
        if (!TextUtils.isEmpty(mImageURLStr)) {
            //When we have the Image URI
            try {
                //Looking up for the Image in Memory Cache for the given URL
                Bitmap cachedBitmap = BitmapImageCache.getBitmapFromCache(mImageURLStr);
                if (cachedBitmap != null) {
                    //When Bitmap image was present in Memory Cache, return the Bitmap retrieved
                    return cachedBitmap;
                } else {
                    //When Bitmap image was NOT present in Memory Cache, download the Bitmap for the Image Content URI
                    Bitmap downloadedBitmap = ImageStorageUtility.getOptimizedBitmapFromContentUri(getContext(), Uri.parse(mImageURLStr));
                    if (downloadedBitmap != null) {
                        //On Successful download

                        //Uploading the Bitmap to GPU for caching in background thread (for faster loads)
                        downloadedBitmap.prepareToDraw();

                        //Adding the downloaded Bitmap to cache
                        BitmapImageCache.addBitmapToCache(mImageURLStr, downloadedBitmap);

                        return downloadedBitmap; //Returning the Bitmap downloaded
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "loadInBackground: Failed while downloading the bitmap for the URI " + mImageURLStr, e);
            }
        }
        //For all else, returning null
        return null;
    }

    @Override
    public void deliverResult(Bitmap newBitmap) {
        if (isReset()) {
            //Ignoring the result if the loader is already reset
            newBitmap = null;
            //Returning when the loader is already reset
            return;
        }

        //Storing a reference to the old Bitmap as we are about to deliver the result
        Bitmap oldBitmap = mDownloadedBitmap;
        mDownloadedBitmap = newBitmap;

        if (isStarted()) {
            //Delivering the result when the loader is started
            super.deliverResult(newBitmap);
        }

        //invalidating the old bitmap as it is not required anymore
        if (oldBitmap != null && oldBitmap != newBitmap) {
            oldBitmap = null;
        }

    }

    @Override
    protected void onStartLoading() {
        if (mDownloadedBitmap != null) {
            //Deliver the result immediately if the Bitmap is already downloaded
            deliverResult(mDownloadedBitmap);
        }

        if (takeContentChanged() || mDownloadedBitmap == null) {
            //Force a new Load when the Bitmap Image is not yet downloaded
            //or the content has changed
            forceLoad();
        }

    }

    @Override
    protected void onStopLoading() {
        //Canceling the load if any as the loader has entered Stopped state
        cancelLoad();
    }

    @Override
    protected void onReset() {
        //Ensuring the loader has stopped
        onStopLoading();

        //Releasing the resources associated with the Loader
        releaseResources();
    }

    @Override
    public void onCanceled(Bitmap data) {
        //Canceling any asynchronous load
        super.onCanceled(data);

        //Releasing the resources associated with the Loader, as the Loader is canceled
        releaseResources();
    }

    private void releaseResources() {
        //Invalidating the Loader data
        if (mDownloadedBitmap != null) {
            mDownloadedBitmap = null;
            mImageURLStr = null;
        }
    }

    String getImageURLStr() {
        return mImageURLStr;
    }

}

