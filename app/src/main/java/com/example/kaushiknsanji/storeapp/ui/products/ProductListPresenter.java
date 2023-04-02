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

package com.example.kaushiknsanji.storeapp.ui.products;

import android.content.Intent;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.LoaderProvider;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.StoreContract;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.AppConstants;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


public class ProductListPresenter
        implements ProductListContract.Presenter,
        LoaderManager.LoaderCallbacks<Cursor>, DataRepository.CursorDataLoaderCallback {

    //Constant used for logs
    private static final String LOG_TAG = ProductListPresenter.class.getSimpleName();
    //The Thread name of the Content Observer
    private static final String CONTENT_OBSERVER_THREAD_NAME = "ProductListContentObserverThread";
    //The View Interface of this Presenter
    @NonNull
    private final ProductListContract.View mProductListView;
    //The LoaderProvider instance that provides the CursorLoader instance
    @NonNull
    private final LoaderProvider mLoaderProvider;
    //The LoaderManager instance
    @NonNull
    private final LoaderManager mLoaderManager;
    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;
    //The Thread used by the Content Observer to observe and notify the changes
    private final HandlerThread mContentObserverHandlerThread;
    //The Content Observer to notify changes in the Product data
    private ProductContentObserver mProductContentObserver;

    public ProductListPresenter(@NonNull LoaderProvider loaderProvider,
                                @NonNull LoaderManager loaderManager,
                                @NonNull StoreRepository storeRepository,
                                @NonNull ProductListContract.View productListView) {
        mLoaderProvider = loaderProvider;
        mLoaderManager = loaderManager;
        mStoreRepository = storeRepository;
        mProductListView = productListView;

        //Creating and starting the Content Observer Thread
        mContentObserverHandlerThread = new HandlerThread(CONTENT_OBSERVER_THREAD_NAME);
        mContentObserverHandlerThread.start();

        //Registering the View with the Presenter
        mProductListView.setPresenter(this);
    }

    @Override
    public void start() {
        //Register the Content Observer
        registerContentObserver();
        //Start downloading the Product Information from the database
        triggerProductsLoad(false);
    }

    @Override
    public void onFabAddClicked() {
        addNewProduct();
    }

    @Override
    public void onRefreshMenuClicked() {
        triggerProductsLoad(true);
    }

    private void registerContentObserver() {
        if (mProductContentObserver == null) {
            //When Observer is not initialized

            //Create the Observer Instance
            mProductContentObserver = new ProductContentObserver();
            //Register the Content Observer to monitor the Products URI and its descendants
            mStoreRepository.registerContentObserver(mProductContentObserver.OBSERVER_URI,
                    true,
                    mProductContentObserver
            );

        } else {
            //When Observer is already initialized, reset the observer to receive future notifications again
            mProductContentObserver.resetObserver();
        }
    }

    private void unregisterContentObserver() {
        if (mProductContentObserver != null) {
            //When the Product Content Observer was already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mProductContentObserver);
            mProductContentObserver = null; //Invalidating..
        }
    }

    private void resetObservers() {
        if (mProductContentObserver != null) {
            //When the Product Content Observer was already initialized, reset the same.
            mProductContentObserver.resetObserver();
        }
    }

    @Override
    public void triggerProductsLoad(boolean forceLoad) {
        //Display the Progress Indicator
        mProductListView.showProgressIndicator();
        if (forceLoad) {
            //When forcefully triggered, restart the loader
            mLoaderManager.restartLoader(AppConstants.PRODUCTS_LOADER, null, this);
        } else {
            //When triggered, start a new loader or load the existing loader
            mLoaderManager.initLoader(AppConstants.PRODUCTS_LOADER, null, this);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //Returning the Loader instance for the Product List
        return mLoaderProvider.createCursorLoader(LoaderProvider.PRODUCT_LIST_TYPE);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            //When Cursor is NOT Null
            if (data.getCount() > 0) {
                //When we have data in the Cursor
                onDataLoaded(data);
            } else {
                //When there is no data in the Cursor
                onDataEmpty();
            }
        } else {
            //When Cursor is Null
            onDataNotAvailable();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //When previous Loader was reset
        onDataReset();
    }

    @Override
    public void onDataLoaded(Cursor data) {
        //Hide Empty View
        mProductListView.hideEmptyView();
        //Initializing the ArrayList to load the ProductLite data from the Cursor
        ArrayList<ProductLite> productList = new ArrayList<>();
        //Resetting the cursor position if pointing past the last row
        if (data.isAfterLast()) {
            data.moveToPosition(-1);
        }
        //Iterating over the Cursor data and building the list
        while (data.moveToNext()) {
            productList.add(ProductLite.from(data));
        }
        //Updating the View with the new data
        mProductListView.loadProducts(productList);
        //Hide the Progress Indicator
        mProductListView.hideProgressIndicator();
    }

    @Override
    public void onDataEmpty() {
        //Hide the Progress Indicator
        mProductListView.hideProgressIndicator();
        //Show empty view
        mProductListView.showEmptyView();
    }

    @Override
    public void onDataNotAvailable() {
        //Hide the Progress Indicator
        mProductListView.hideProgressIndicator();
        //Show error message
        mProductListView.showError(R.string.product_list_load_error);
    }

    @Override
    public void onDataReset() {
        //Updating the View with an empty list
        mProductListView.loadProducts(new ArrayList<>());
        //Show empty view
        mProductListView.showEmptyView();
    }

    @Override
    public void onContentChange() {
        //Retrieving the Products Cursor Loader
        Loader<Cursor> productsLoader = mLoaderManager.getLoader(AppConstants.PRODUCTS_LOADER);
        if (productsLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            productsLoader.onContentChanged();
        } else {
            //If Loader not registered, then force restart the load
            triggerProductsLoad(true);
        }

        //Retrieving the Sales Cursor Loader used by SalesListFragment
        Loader<Cursor> salesLoader = mLoaderManager.getLoader(AppConstants.SALES_LOADER);
        if (salesLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            salesLoader.onContentChanged();
        }
    }

    @Override
    public void editProduct(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Reset observers
        resetObservers();
        //Delegating to the view to launch the Activity for editing an Existing Product
        mProductListView.launchEditProduct(productId, activityOptionsCompat);
    }

    @Override
    public void deleteProduct(ProductLite product) {
        //Display the Progress Indicator
        mProductListView.showProgressIndicator();

        //Reset observers
        resetObservers();

        //Executing Product Deletion with the Repository
        mStoreRepository.deleteProductById(product.getId(), new DataRepository.DataOperationsCallback() {

            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mProductListView.hideProgressIndicator();

                //Show the delete success message
                mProductListView.showDeleteSuccess(product.getSku());
            }

            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Hide Progress Indicator
                mProductListView.hideProgressIndicator();

                //Show the error message
                mProductListView.showError(messageId, args);
            }
        });
    }

    @Override
    public void addNewProduct() {
        //Reset observers
        resetObservers();
        //Delegating to the view to launch the Activity for adding a New Product
        mProductListView.launchAddNewProduct();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode >= FragmentActivity.RESULT_FIRST_USER) {
            //When we have the custom results for the requests made

            if (requestCode == ProductConfigActivity.REQUEST_EDIT_PRODUCT) {
                //For an Edit Product request
                if (resultCode == ProductConfigActivity.RESULT_EDIT_PRODUCT) {
                    //When the result is for the Edit action
                    //Show the Update Success message
                    mProductListView.showUpdateSuccess(data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU));
                } else if (resultCode == ProductConfigActivity.RESULT_DELETE_PRODUCT) {
                    //When the result is for the Delete action
                    //Show the Delete Success message
                    mProductListView.showDeleteSuccess(data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU));
                }
            } else if (requestCode == ProductConfigActivity.REQUEST_ADD_PRODUCT &&
                    resultCode == ProductConfigActivity.RESULT_ADD_PRODUCT) {
                //When the request and the result is for Add Product
                //Show the Add Success message
                mProductListView.showAddSuccess(data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU));
            }
        }
    }

    @Override
    public void releaseResources() {
        //Unregister any registered Content Observer
        unregisterContentObserver();
        //Stop the Content Observer Thread
        mContentObserverHandlerThread.quit();
    }

    private class ProductContentObserver extends ContentObserver {

        //URI Matcher codes for identifying the URI of Item and its descendant relationships
        private static final int ITEM_ID = 10;
        private static final int ITEM_IMAGES_ID = 11;
        //URI Matcher for matching the possible URI
        private final UriMatcher mUriMatcher = buildUriMatcher();
        //The URI to be observed which is the Product's URI
        private final Uri OBSERVER_URI = ProductContract.Product.CONTENT_URI;
        //Main Thread Handler to dispatch the notifications to the CursorLoader on Main Thread
        private final Handler mMainThreadHandler;

        //Boolean to control multiple notifications from being issued
        private final AtomicBoolean mDeliveredNotification = new AtomicBoolean(false);

        /**
         * Creates a content observer.
         */
        ProductContentObserver() {
            //Using the custom Content Observer thread to receive notifications on
            super(new Handler(mContentObserverHandlerThread.getLooper()));
            //Instantiating the Main Thread Handler for dispatching notifications to the CursorLoader on Main Thread
            mMainThreadHandler = new Handler(Looper.getMainLooper());
        }

        private UriMatcher buildUriMatcher() {
            //Constructs an empty UriMatcher for the root node
            UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
            //For "content://AUTHORITY/item/#" URI that references a record in 'item' table
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    ProductContract.PATH_ITEM + "/#", ITEM_ID);
            //For "content://AUTHORITY/item/image/#" URI that references a set of records in 'item_image' table
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    ProductContract.PATH_ITEM + "/" + ProductContract.PATH_ITEM_IMAGE + "/#",
                    ITEM_IMAGES_ID);
            //Returning the URI Matcher prepared
            return matcher;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                //When we have the URI, trigger notifications based on the URI
                switch (mUriMatcher.match(uri)) {
                    case ITEM_ID:
                        triggerNotification(uri);
                        break;
                    case ITEM_IMAGES_ID:
                        triggerNotification(uri);
                        break;
                }
            } else if (selfChange) {
                //When it is a self change notification, dispatch the content change
                //notification to Loader

                //Posting notification on Main Thread
                mMainThreadHandler.post(ProductListPresenter.this::onContentChange);
            }
        }

        private void triggerNotification(Uri uri) {
            if (mDeliveredNotification.compareAndSet(false, true)) {
                //When notification was not delivered previously, dispatch the notification and set to TRUE

                Log.i(LOG_TAG, "triggerNotification: Called for " + uri);

                //Posting notification on Main Thread
                mMainThreadHandler.post(ProductListPresenter.this::onContentChange);
            }
        }

        private void resetObserver() {
            mDeliveredNotification.set(false);
        }
    }
}