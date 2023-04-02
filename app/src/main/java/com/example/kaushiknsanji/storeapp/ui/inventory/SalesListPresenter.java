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

package com.example.kaushiknsanji.storeapp.ui.inventory;

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
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.StoreContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.models.SalesLite;
import com.example.kaushiknsanji.storeapp.ui.inventory.config.SalesConfigActivity;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.AppConstants;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SalesListPresenter implements SalesListContract.Presenter,
        LoaderManager.LoaderCallbacks<Cursor>, DataRepository.CursorDataLoaderCallback {

    //Constant used for logs
    private static final String LOG_TAG = SalesListPresenter.class.getSimpleName();
    //The Thread name of the Content Observer
    private static final String CONTENT_OBSERVER_THREAD_NAME = "ProductSalesContentObserverThread";
    //The View Interface of this Presenter
    @NonNull
    private final SalesListContract.View mSalesListView;
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
    //Boolean to control multiple Content Observer notifications from being issued
    private final AtomicBoolean mDeliveredNotification = new AtomicBoolean(false);
    //The Content Observer to notify changes in the Product data
    private ProductSalesContentObserver mProductContentObserver;
    //The Content Observer to notify changes in the Supplier data
    private ProductSalesContentObserver mSupplierContentObserver;
    //The Content Observer to notify changes in the Price data
    private ProductSalesContentObserver mPriceContentObserver;
    //The Content Observer to notify changes in the Product Inventory
    private ProductSalesContentObserver mInventoryContentObserver;

    public SalesListPresenter(@NonNull LoaderProvider loaderProvider,
                              @NonNull LoaderManager loaderManager,
                              @NonNull StoreRepository storeRepository,
                              @NonNull SalesListContract.View salesListView) {
        mLoaderProvider = loaderProvider;
        mLoaderManager = loaderManager;
        mStoreRepository = storeRepository;
        mSalesListView = salesListView;

        //Creating and starting the Content Observer Thread
        mContentObserverHandlerThread = new HandlerThread(CONTENT_OBSERVER_THREAD_NAME);
        mContentObserverHandlerThread.start();

        //Registering the View with the Presenter
        mSalesListView.setPresenter(this);
    }

    @Override
    public void start() {
        registerContentObservers();
        //Start downloading the Products with Sales Information from the database
        triggerProductSalesLoad(false);
    }


    private void registerContentObservers() {
        if (mProductContentObserver == null) {
            //When Products Observer is not initialized

            //Create the Observer Instance
            mProductContentObserver = new ProductSalesContentObserver(ProductContract.Product.CONTENT_URI);
            //Register the Content Observer to monitor the Products URI and its descendants
            mStoreRepository.registerContentObserver(ProductContract.Product.CONTENT_URI,
                    true, mProductContentObserver);
        }

        if (mSupplierContentObserver == null) {
            //When Suppliers Observer is not initialized

            //Create the Observer Instance
            mSupplierContentObserver = new ProductSalesContentObserver(SupplierContract.Supplier.CONTENT_URI);
            //Register the Content Observer to monitor the Suppliers URI and its descendants
            mStoreRepository.registerContentObserver(SupplierContract.Supplier.CONTENT_URI,
                    true, mSupplierContentObserver);
        }

        if (mPriceContentObserver == null) {
            //When Product Price Observer is not initialized

            //Create the Observer Instance
            mPriceContentObserver = new ProductSalesContentObserver(SalesContract.ProductSupplierInfo.CONTENT_URI);
            //Register the Content Observer to monitor the ProductSupplierInfo URI and its descendants
            mStoreRepository.registerContentObserver(SalesContract.ProductSupplierInfo.CONTENT_URI,
                    true, mPriceContentObserver);
        }

        if (mInventoryContentObserver == null) {
            //When Product Inventory Observer is not initialized

            //Create the Observer Instance
            mInventoryContentObserver = new ProductSalesContentObserver(SalesContract.ProductSupplierInventory.CONTENT_URI);
            //Register the Content Observer to monitor the ProductSupplierInventory URI and its descendants
            mStoreRepository.registerContentObserver(SalesContract.ProductSupplierInventory.CONTENT_URI,
                    true, mInventoryContentObserver);
        }

        //Reset all observers to receive notifications
        resetObservers();
    }

    private void unregisterContentObservers() {
        if (mProductContentObserver != null) {
            //When Products Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mProductContentObserver);
            mProductContentObserver = null;
        }

        if (mSupplierContentObserver != null) {
            //When Suppliers Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mSupplierContentObserver);
            mSupplierContentObserver = null;
        }

        if (mPriceContentObserver != null) {
            //When Product Price Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mPriceContentObserver);
            mPriceContentObserver = null;
        }

        if (mInventoryContentObserver != null) {
            //When Product Inventory Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mInventoryContentObserver);
            mInventoryContentObserver = null;
        }
    }

    private void resetObservers() {
        //Resetting the observers' notification control boolean to FALSE,
        //to again trigger any new notification that occurs later.
        mDeliveredNotification.set(false);
    }

    @Override
    public void onFabAddClicked() {
        //No-op for this screen as there is no FAB
    }

    @Override
    public void onRefreshMenuClicked() {
        triggerProductSalesLoad(true);
    }

    @Override
    public void triggerProductSalesLoad(boolean forceLoad) {
        //Display the Progress Indicator
        mSalesListView.showProgressIndicator();
        if (forceLoad) {
            //When forcefully triggered, restart the loader
            mLoaderManager.restartLoader(AppConstants.SALES_LOADER, null, this);
        } else {
            //When triggered, start a new loader or load the existing loader
            mLoaderManager.initLoader(AppConstants.SALES_LOADER, null, this);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //Returning the Loader instance for the Sales List
        return mLoaderProvider.createCursorLoader(LoaderProvider.SALES_LIST_TYPE);
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
        mSalesListView.hideEmptyView();
        //Initializing the ArrayList to load the SalesLite data from the Cursor
        ArrayList<SalesLite> salesList = new ArrayList<>();
        //Resetting the cursor position if pointing past the last row
        if (data.isAfterLast()) {
            data.moveToPosition(-1);
        }
        //Iterating over the Cursor data and building the list
        while (data.moveToNext()) {
            salesList.add(SalesLite.from(data));
        }
        //Updating the View with the new data
        mSalesListView.loadSalesList(salesList);
        //Hide the Progress Indicator
        mSalesListView.hideProgressIndicator();
    }

    @Override
    public void onDataEmpty() {
        //Hide the Progress Indicator
        mSalesListView.hideProgressIndicator();
        //Show empty view
        mSalesListView.showEmptyView();
    }

    @Override
    public void onDataNotAvailable() {
        //Hide the Progress Indicator
        mSalesListView.hideProgressIndicator();
        //Show error message
        mSalesListView.showError(R.string.sales_list_load_error);
    }

    @Override
    public void onDataReset() {
        //Updating the View with an empty list
        mSalesListView.loadSalesList(new ArrayList<>());
        //Show empty view
        mSalesListView.showEmptyView();
    }

    @Override
    public void onContentChange() {
        //Retrieving the Sales Cursor Loader
        Loader<Cursor> salesLoader = mLoaderManager.getLoader(AppConstants.SALES_LOADER);
        if (salesLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            salesLoader.onContentChanged();
        } else {
            //If Loader not registered, then force restart the load
            triggerProductSalesLoad(true);
        }
    }

    @Override
    public void onProductContentChange() {
        //Retrieving the Products Cursor Loader used by ProductListFragment
        Loader<Cursor> productsLoader = mLoaderManager.getLoader(AppConstants.PRODUCTS_LOADER);
        if (productsLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            productsLoader.onContentChanged();
        }
    }
    @Override
    public void releaseResources() {
        //Unregister any registered Content Observers
        unregisterContentObservers();
        //Stop the Content Observer Thread
        mContentObserverHandlerThread.quit();
    }


    @Override
    public void deleteProduct(int productId, String productSku) {
        //Display the Progress Indicator
        mSalesListView.showProgressIndicator();

        //Reset observers
        resetObservers();

        //Executing Product Deletion with the Repository
        mStoreRepository.deleteProductById(productId, new DataRepository.DataOperationsCallback() {

            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mSalesListView.hideProgressIndicator();

                //Show the delete success message
                mSalesListView.showDeleteSuccess(productSku);
            }

            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Hide Progress Indicator
                mSalesListView.hideProgressIndicator();

                //Show the error message
                mSalesListView.showError(messageId, args);
            }
        });
    }

    @Override
    public void sellOneQuantity(SalesLite salesLite) {
        //Display the Progress Indicator
        mSalesListView.showProgressIndicator();

        //Reset observers
        resetObservers();

        //Updating the Quantity via the Repository
        mStoreRepository.decreaseProductSupplierInventory(salesLite.getProductId(),
                salesLite.getProductSku(), salesLite.getSupplierId(), salesLite.getTopSupplierCode(),
                salesLite.getSupplierAvailableQuantity(), 1, new DataRepository.DataOperationsCallback() {
                    @Override
                    public void onSuccess() {
                        //Hide Progress Indicator
                        mSalesListView.hideProgressIndicator();

                        //Show the success message
                        mSalesListView.showSellQuantitySuccess(salesLite.getProductSku(), salesLite.getTopSupplierCode());
                    }
                    @Override
                    public void onFailure(int messageId, @Nullable Object... args) {
                        //Hide Progress Indicator
                        mSalesListView.hideProgressIndicator();

                        //Show the error message
                        mSalesListView.showError(messageId, args);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode >= FragmentActivity.RESULT_FIRST_USER) {
            //When we have the custom results for the requests made

            if (requestCode == SalesConfigActivity.REQUEST_EDIT_SALES) {
                //For an Edit Sales request

                if (resultCode == SalesConfigActivity.RESULT_EDIT_SALES) {
                    //When the result is for the Edit Sales action
                    //Show the Update Inventory Success message
                    mSalesListView.showUpdateInventorySuccess(data.getStringExtra(SalesConfigActivity.EXTRA_RESULT_PRODUCT_SKU));

                } else if (resultCode == ProductConfigActivity.RESULT_DELETE_PRODUCT) {
                    //When the result is for the Delete Product action
                    //Show the Delete Success message
                    mSalesListView.showDeleteSuccess(data.getStringExtra(SalesConfigActivity.EXTRA_RESULT_PRODUCT_SKU));
                }

            }
        }
    }

    @Override
    public void editProductSales(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Reset observers
        resetObservers();
        //Delegating to the View to launch the Activity
        mSalesListView.launchEditProductSales(productId, activityOptionsCompat);
    }

    private class ProductSalesContentObserver extends ContentObserver {
        //URI Matcher codes for identifying the URI of Product and its descendant relationships
        private static final int ITEM_ID = 10;
        private static final int ITEM_IMAGES_ID = 11;
        //URI Matcher codes for identifying the URI of Supplier and its descendant relationships
        private static final int SUPPLIER_ID = 20;
        //URI Matcher codes for identifying the URI of ProductSupplierInfo and its descendant relationships
        private static final int SUPPLIER_ITEMS_ID = 30;
        //URI Matcher codes for identifying the URI of ProductSupplierInventory and its descendant relationships
        private static final int SALES_INVENTORY_ITEM_ID = 40;
        //URI Matcher for matching the possible URI
        private final UriMatcher mUriMatcher = buildUriMatcher();
        //The URI observed by the Observer for changes
        private final Uri mObserverUri;
        //Main Thread Handler to dispatch the notifications to the CursorLoader on Main Thread
        private final Handler mMainThreadHandler;

        ProductSalesContentObserver(Uri observerUri) {
            //Using the custom Content Observer thread to receive notifications on
            super(new Handler(mContentObserverHandlerThread.getLooper()));
            //Instantiating the Main Thread Handler for dispatching notifications to the CursorLoader on Main Thread
            mMainThreadHandler = new Handler(Looper.getMainLooper());
            //Saving the URI being observed
            mObserverUri = observerUri;
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
            //For "content://AUTHORITY/supplier/#" URI that references a record in 'supplier' table
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SupplierContract.PATH_SUPPLIER + "/#", SUPPLIER_ID);
            //For "content://AUTHORITY/salesinfo/supplier/#" URI that references a set of records in 'item_supplier_info' table
            //identified by 'supplier_id'
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SalesContract.PATH_ITEM_SUPPLIER_INFO + "/" + SupplierContract.PATH_SUPPLIER + "/#",
                    SUPPLIER_ITEMS_ID);
            //For "content://AUTHORITY/salesinventory/item/#" URI that references a set of records in 'item_supplier_inventory' table
            //identified by 'item_id'
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SalesContract.PATH_ITEM_SUPPLIER_INVENTORY + "/" + ProductContract.PATH_ITEM + "/#",
                    SALES_INVENTORY_ITEM_ID);
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

                //Finding the URI Match code
                int uriMatch = mUriMatcher.match(uri);

                if (mObserverUri.equals(ProductContract.Product.CONTENT_URI)) {
                    //For the URI of Product and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case ITEM_ID:
                            triggerNotification(uri);
                            break;
                        case ITEM_IMAGES_ID:
                            triggerNotification(uri);
                            break;
                    }
                } else if (mObserverUri.equals(SupplierContract.Supplier.CONTENT_URI)) {
                    //For the URI of Supplier and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case SUPPLIER_ID:
                            triggerNotification(uri);
                            break;
                    }
                } else if (mObserverUri.equals(SalesContract.ProductSupplierInfo.CONTENT_URI)) {
                    //For the URI of ProductSupplierInfo and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case SUPPLIER_ITEMS_ID:
                            triggerNotification(uri);
                            break;
                    }
                } else if (mObserverUri.equals(SalesContract.ProductSupplierInventory.CONTENT_URI)) {
                    //For the URI of ProductSupplierInventory and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case SALES_INVENTORY_ITEM_ID:
                            triggerNotification(uri);
                            break;
                    }
                }

            } else if (selfChange) {
                //When it is a self change notification, dispatch the content change
                //notification to Loader

                //Posting notification on Main Thread
                mMainThreadHandler.post(SalesListPresenter.this::onContentChange);
            }
        }

        private void triggerNotification(Uri uri) {
            if (mDeliveredNotification.compareAndSet(false, true)) {
                //When notification was not delivered previously, dispatch the notification and set to TRUE

                Log.i(LOG_TAG, "triggerNotification: Called for " + uri);

                //Posting notification on Main Thread
                mMainThreadHandler.post(SalesListPresenter.this::onContentChange);

                //Posting another notification specifically for any change in Products
                //to trigger a reload in the ProductListFragment
                if (mObserverUri.equals(ProductContract.Product.CONTENT_URI)) {
                    mMainThreadHandler.post(SalesListPresenter.this::onProductContentChange);
                }
            }
        }

    }

}
