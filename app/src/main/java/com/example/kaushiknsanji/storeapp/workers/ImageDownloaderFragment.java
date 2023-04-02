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
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.ImageView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.cache.BitmapImageCache;


public class ImageDownloaderFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Bitmap> {

    //Constant used for logs and Fragment Tag
    private static final String LOG_TAG = ImageDownloaderFragment.class.getSimpleName();

    //Stores the ImageView component that needs to be updated when the Image is downloaded
    private ImageView mImageView;

    //Stores the Image URL whose Image is to be downloaded
    private String mImageURLStr;

    //Stores the OnFailureListener instance
    private OnFailureListener mOnFailureListener;

    //Stores the OnSuccessListener instance
    private OnSuccessListener mOnSuccessListener;


    public static ImageDownloaderFragment newInstance(FragmentManager fragmentManager, int tagId) {
        //Creating the Fragment Tag string using the tagId passed
        String fragmentTagStr = LOG_TAG + "_" + tagId;

        //Retrieving the Fragment from the FragmentManager if existing
        ImageDownloaderFragment imageDownloaderFragment
                = (ImageDownloaderFragment) fragmentManager.findFragmentByTag(fragmentTagStr);
        if (imageDownloaderFragment == null) {
            //When the Fragment is being added for the first time

            //Instantiating the Fragment
            imageDownloaderFragment = new ImageDownloaderFragment();
            //Adding the Fragment to Transaction and committing with state losses
            fragmentManager.beginTransaction().add(imageDownloaderFragment, fragmentTagStr).commitAllowingStateLoss();
        }

        //Returning the Fragment instance
        return imageDownloaderFragment;
    }

    public void executeAndUpdate(ImageView imageView, String imageURLStr, int loaderId) {
        //Delegating to other overloaded method with the derived instance for LoaderManager
        executeAndUpdate(imageView, imageURLStr, loaderId, obtainLoaderManager(imageView));
    }

    public void executeAndUpdate(ImageView imageView, String imageURLStr, int loaderId, LoaderManager loaderManager) {
        //Saving the parameters passed
        mImageView = imageView;
        mImageURLStr = imageURLStr;

        //Normalizing the loaderId to start from the ImageDownloader's base ID
        loaderId += ImageDownloader.IMAGE_LOADER;

        if (loaderManager == null) {
            //When we do not have the LoaderManager instance for downloading the Image
            //throw a Runtime Exception
            throw new IllegalStateException("LoaderManager is not attached.");
        }

        //Getting the loader at the loaderId if any
        ImageDownloader imageDownloader = getImageDownloader(loaderId, loaderManager);

        //Resetting the ImageView to the default Thumbnail Image for lazy loading
        mImageView.setImageResource(R.drawable.ic_all_product_default);

        //Boolean to check if we need to restart the loader
        boolean isNewImageURLStr = false;
        if (imageDownloader != null) {
            //When we have a previously registered loader

            //Set the Loader to be restarted when the Image URL passed is
            //empty/null or not the same as that of the loader
            isNewImageURLStr = TextUtils.isEmpty(mImageURLStr) || !mImageURLStr.equals(imageDownloader.getImageURLStr());
        }

        if (isNewImageURLStr) {
            //Restarting the Loader when the ImageURL is new
            loaderManager.restartLoader(loaderId, null, this);
        } else {
            //Invoking the Loader AS-IS if the ImageURL is the same
            //or if the Loader is not yet registered with the loaderId passed
            loaderManager.initLoader(loaderId, null, this);
        }
    }

    @Nullable
    private ImageDownloader getImageDownloader(int loaderId, LoaderManager loaderManager) {
        //Getting the loader at the loaderId
        Loader<Bitmap> loader = loaderManager.getLoader(loaderId);
        if (loader instanceof ImageDownloader) {
            //Returning the ImageDownloader instance
            return (ImageDownloader) loader;
        } else {
            //Returning NULL when not found
            return null;
        }
    }

    @Nullable
    private FragmentActivity obtainActivity(@Nullable Context context) {
        if (context == null) {
            //Return Null when Null
            return null;
        } else if (context instanceof FragmentActivity) {
            //Return the FragmentActivity instance when Context is of type FragmentActivity
            return (FragmentActivity) context;
        } else if (context instanceof ContextWrapper) {
            //Recall with the Base Context when Context is of type ContextWrapper
            return obtainActivity(((ContextWrapper) context).getBaseContext());
        }
        //Returning Null when we could not derive the Activity instance from the given Context
        return null;
    }

    @Nullable
    private LoaderManager obtainLoaderManager(ImageView imageView) {
        //Obtaining the Activity from the ImageView
        FragmentActivity activity = obtainActivity(imageView.getContext());
        if (activity != null) {
            //When we have the Activity, return the LoaderManager instance
            return activity.getSupportLoaderManager();
        }
        //Returning Null when Activity could not be derived from ImageView
        return null;
    }

    @NonNull
    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        //Returning an Instance of ImageDownloader to start the Image download
        return new ImageDownloader(mImageView.getContext(), mImageURLStr);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Bitmap> loader, Bitmap bitmapImage) {
        if (bitmapImage != null && mImageView != null) {
            //When the bitmap was downloaded successfully and the ImageView is still attached
            onDownloadSuccess(bitmapImage);
        } else if (mImageView != null) {
            //When the bitmap failed to download and the ImageView is still attached
            onDownloadFailure();
        }
    }

    private void onDownloadFailure() {
        //Resetting the ImageView to the default Thumbnail Image when the Bitmap failed to download
        mImageView.setImageResource(R.drawable.ic_all_product_default);
        //When the OnFailureListener is registered, dispatch the failure event
        if (mOnFailureListener != null) {
            mOnFailureListener.onFailure();
        }
    }

    private void onDownloadSuccess(Bitmap bitmapImage) {
        //Updating the ImageView when the Bitmap is downloaded successfully
        mImageView.setImageBitmap(bitmapImage);
        //When the OnSuccessListener is registered, dispatch the success event
        if (mOnSuccessListener != null) {
            mOnSuccessListener.onSuccess(bitmapImage);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Bitmap> loader) {
        //Resetting the ImageView to the default Thumbnail Image
        mImageView.setImageResource(R.drawable.ic_all_product_default);
    }

    public ImageDownloaderFragment setOnFailureListener(OnFailureListener listener) {
        mOnFailureListener = listener;
        return this;
    }

    public ImageDownloaderFragment setOnSuccessListener(OnSuccessListener listener) {
        mOnSuccessListener = listener;
        return this;
    }

    public interface OnFailureListener {

        void onFailure();
    }

    public interface OnSuccessListener {

        void onSuccess(@NonNull Bitmap resultBitmap);
    }

}
