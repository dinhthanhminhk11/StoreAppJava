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

package com.example.kaushiknsanji.storeapp.ui.inventory.config;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.models.Product;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.ui.BaseView;
import com.example.kaushiknsanji.storeapp.ui.inventory.procure.SalesProcurementActivity;
import com.example.kaushiknsanji.storeapp.ui.products.config.DefaultPhotoChangeListener;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity;

import java.util.ArrayList;
import java.util.List;


public class SalesConfigPresenter implements SalesConfigContract.Presenter {

    //Constant used for Logs
    private static final String LOG_TAG = SalesConfigPresenter.class.getSimpleName();

    //The Product ID of an Existing Product
    private final int mProductId;

    //The View Interface of this Presenter
    @NonNull
    private final SalesConfigContract.View mSalesConfigView;

    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;

    //Navigator that receives callbacks when navigating away from the Current Activity
    private final SalesConfigNavigator mSalesConfigNavigator;

    //Listener that receives callbacks when there is a change in the default photo of the Product
    private final DefaultPhotoChangeListener mDefaultPhotoChangeListener;

    //Stores the SKU of the Product
    private String mProductSku;

    //Stores the Name of the Product
    private String mProductName;

    //Stores the state of Product details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsProductRestored;

    //Stores the state of Supplier details restored,
    //to prevent updating the fields every time during System config change
    private boolean mAreSuppliersRestored;

    //Stores the List of Product's Suppliers downloaded with their Sales information
    private List<ProductSupplierSales> mExistingProductSupplierSalesList;

    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores the value of the Original Total Available Quantity of the Product
    private int mOldTotalAvailableQuantity;

    //Stores the Updated value of the Total Available Quantity of the Product
    private int mNewTotalAvailableQuantity;

    //Stores the ProductImage to be shown
    private ProductImage mProductImageToBeShown;


    SalesConfigPresenter(int productId,
                         @NonNull StoreRepository storeRepository,
                         @NonNull SalesConfigContract.View salesConfigView,
                         @NonNull SalesConfigNavigator salesConfigNavigator,
                         @NonNull DefaultPhotoChangeListener defaultPhotoChangeListener) {
        mProductId = productId;
        mStoreRepository = storeRepository;
        mSalesConfigView = salesConfigView;
        mSalesConfigNavigator = salesConfigNavigator;
        mDefaultPhotoChangeListener = defaultPhotoChangeListener;

        //Registering the View with the Presenter
        mSalesConfigView.setPresenter(this);
    }


    @Override
    public void start() {

        //Download Product data for the Product Id
        loadProductDetails();

        //Download Suppliers of the Product with Price and Inventory
        loadProductSuppliers();

    }


    @Override
    public void updateAndSyncProductState(boolean isProductRestored) {
        //Saving the state
        mIsProductRestored = isProductRestored;

        //Syncing the state to the View
        mSalesConfigView.syncProductState(mIsProductRestored);
    }


    @Override
    public void updateAndSyncSuppliersState(boolean areSuppliersRestored) {
        //Saving the state
        mAreSuppliersRestored = areSuppliersRestored;

        //Syncing the state to the View
        mSalesConfigView.syncSuppliersState(mAreSuppliersRestored);
    }


    @Override
    public void updateAndSyncOldTotalAvailability(int oldTotalAvailableQuantity) {
        //Saving to the Original Total Available Quantity member
        mOldTotalAvailableQuantity = oldTotalAvailableQuantity;
        //Publishing the value to the View to keep it in sync with the Presenter
        mSalesConfigView.syncOldTotalAvailability(mOldTotalAvailableQuantity);
    }

    private void loadProductDetails() {
        if (!mIsProductRestored) {
            //When Product data was not previously downloaded

            //Display progress indicator
            mSalesConfigView.showProgressIndicator(R.string.product_config_status_loading_existing_product);

            //Retrieving the Product Details from the Repository
            mStoreRepository.getProductDetailsById(mProductId, new DataRepository.GetQueryCallback<Product>() {

                @Override
                public void onResults(Product product) {
                    //Update the Product Name field
                    updateProductName(product.getName());

                    //Update the Product SKU field
                    updateProductSku(product.getSku());

                    //Update the Product Category
                    updateProductCategory(product.getCategory());

                    //Update the Product Description
                    updateProductDescription(product.getDescription());

                    //Update the Product Image to be shown
                    updateProductImage(product.getProductImages());

                    //Update the Product Attributes
                    updateProductAttributes(product.getProductAttributes());

                    //Marking as downloaded/restored
                    updateAndSyncProductState(true);

                    //Hide progress indicator
                    mSalesConfigView.hideProgressIndicator();
                }


                @Override
                public void onFailure(@StringRes int messageId, @Nullable Object... args) {
                    //Hide progress indicator
                    mSalesConfigView.hideProgressIndicator();

                    //Show the error message
                    mSalesConfigView.showError(messageId, args);
                }

                @Override
                public void onEmpty() {
                    //No-op, not called for this implementation
                }
            });
        }
    }

    @Override
    public void updateProductName(String productName) {
        //Saving the Product Name
        mProductName = productName;
        //Updating to the View to display the Product Name
        mSalesConfigView.updateProductName(productName);
    }

    @Override
    public void updateProductSku(String productSku) {
        //Saving the Product SKU
        mProductSku = productSku;
        //Updating to the View to display the Product SKU
        mSalesConfigView.updateProductSku(productSku);
    }

    public void updateProductCategory(String productCategory) {
        mSalesConfigView.updateProductCategory(productCategory);
    }

    public void updateProductDescription(String description) {
        mSalesConfigView.updateProductDescription(description);
    }

    public void updateProductImage(ArrayList<ProductImage> productImages) {
        //Finding the Product Image to be shown if Present
        if (productImages != null && productImages.size() > 0) {
            //When we have Product Images

            //Save the Product Images list
            mProductImages = productImages;

            //Send the list to the view to save for subsequent reloads
            mSalesConfigView.updateProductImages(mProductImages);

            //Saves the ProductImage to be shown
            mProductImageToBeShown = null;

            //Iterating over the list of Images to find the selected Image
            for (ProductImage productImage : mProductImages) {
                if (productImage.isDefault()) {
                    //When selected, grab the ProductImage and bail out of the loop
                    mProductImageToBeShown = productImage;
                    break;
                }
            }

            if (mProductImageToBeShown != null) {
                //Delegating to the listener to show the selected Product Image by passing the String URI of the Image to the View
                mDefaultPhotoChangeListener.showSelectedProductImage(mProductImageToBeShown.getImageUri());
            } else {
                //This case should not occur, since at least one image needs to be selected.

                //Logging the error
                Log.e(LOG_TAG, "ERROR!!! updateProductImages: Product Images found but no default image");

                //Delegating to the listener to show the Default Image
                mDefaultPhotoChangeListener.showDefaultImage();
            }

        } else {
            //When there are no images, delegate to the listener to show the default image instead
            mDefaultPhotoChangeListener.showDefaultImage();

            //Initialize the ArrayList
            mProductImages = new ArrayList<>();
        }
    }

    public void updateProductAttributes(ArrayList<ProductAttribute> productAttributes) {
        mSalesConfigView.updateProductAttributes(productAttributes);
    }

    private void loadProductSuppliers() {
        if (!mAreSuppliersRestored) {
            //When Suppliers data was not previously downloaded

            //Display progress indicator
            mSalesConfigView.showProgressIndicator(R.string.sales_config_status_loading_suppliers);

            //Retrieving the Suppliers with Sales information from the Repository
            mStoreRepository.getProductSuppliersSalesInfo(mProductId, new DataRepository.GetQueryCallback<List<ProductSupplierSales>>() {

                @Override
                public void onResults(List<ProductSupplierSales> productSupplierSalesList) {
                    //Update the Product Suppliers data with the list downloaded
                    updateProductSupplierSalesList(productSupplierSalesList);

                    //Marking as downloaded/restored
                    updateAndSyncSuppliersState(true);

                    //Hide progress indicator
                    mSalesConfigView.hideProgressIndicator();
                }

                @Override
                public void onEmpty() {
                    //Update the Product Suppliers data with an Empty list
                    updateProductSupplierSalesList(new ArrayList<>());

                    //Just hide progress indicator
                    mSalesConfigView.hideProgressIndicator();
                }
            });
        }
    }

    public void updateProductSupplierSalesList(List<ProductSupplierSales> productSupplierSalesList) {
        //Saving the existing list
        mExistingProductSupplierSalesList = productSupplierSalesList;

        //Stores the Original Total Available Quantity of the Product
        int totalAvailableQuantity = 0;

        //Creating a new ArrayList to make a deep copy of the submitted list
        ArrayList<ProductSupplierSales> newProductSupplierSalesList = new ArrayList<>();
        //Iterating over the submitted list to add the deep copy of the ProductSupplierSales of the submitted list
        //and Calculating the Original Total Available Quantity
        for (ProductSupplierSales productSupplierSales : productSupplierSalesList) {
            newProductSupplierSalesList.add((ProductSupplierSales) productSupplierSales.clone());
            //Adding and Calculating the Original Total Available Quantity
            totalAvailableQuantity += productSupplierSales.getAvailableQuantity();
        }

        //Updating the Total Available value to the view
        //(Should be done before updating the ProductSupplierSales list to the View,
        // since new total will be calculated by the Adapter)
        updateAvailability(totalAvailableQuantity);

        //Update and Sync the Original Total Available Quantity to the View
        updateAndSyncOldTotalAvailability(totalAvailableQuantity);

        //Dispatching the deep copied list of ProductSupplierSales to the View
        mSalesConfigView.loadProductSuppliersData(newProductSupplierSalesList);
    }

    @Override
    public void editProduct(int productId) {
        //Delegating to the Navigator to launch the Activity for editing the Product
        mSalesConfigNavigator.launchEditProduct(productId);
    }

    @Override
    public void editSupplier(int supplierId) {
        //Delegating to the Navigator to launch the Activity for editing the Supplier
        mSalesConfigNavigator.launchEditSupplier(supplierId);
    }

    @Override
    public void onProductSupplierSwiped(String supplierCode) {
        mSalesConfigView.showProductSupplierSwiped(supplierCode);
    }

    @Override
    public void procureProduct(ProductSupplierSales productSupplierSales) {
        //Delegating to the Navigator to launch the Activity for procuring the Product
        mSalesConfigNavigator.launchProcureProduct(productSupplierSales, mProductImageToBeShown, mProductName, mProductSku);
    }

    @Override
    public void updateAvailability(int totalAvailableQuantity) {
        //Updating the value to the New Total Available Quantity member
        mNewTotalAvailableQuantity = totalAvailableQuantity;

        //Publishing to the View
        if (mNewTotalAvailableQuantity > 0) {
            //When we have quantity

            //Show the new total to the View
            mSalesConfigView.updateAvailability(mNewTotalAvailableQuantity);
        } else {
            //When we do not have quantity

            //Show the Out of Stock Alert to the View
            mSalesConfigView.showOutOfStockAlert();
        }
    }

    @Override
    public void changeAvailability(int changeInAvailableQuantity) {
        //Calculating and Updating the Availability to the View
        updateAvailability(mNewTotalAvailableQuantity + changeInAvailableQuantity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode >= FragmentActivity.RESULT_FIRST_USER) {
            //When we have the custom results for the requests made

            if (requestCode == SupplierConfigActivity.REQUEST_EDIT_SUPPLIER) {
                //For an Edit Supplier request

                if (resultCode == SupplierConfigActivity.RESULT_EDIT_SUPPLIER) {
                    //When the result is for the Edit action

                    //Update as False so that the Product's Suppliers are redownloaded and updated on resume
                    updateAndSyncSuppliersState(false);

                    //Show the Update Success message
                    mSalesConfigView.showUpdateSupplierSuccess(data.getStringExtra(SupplierConfigActivity.EXTRA_RESULT_SUPPLIER_CODE));
                } else if (resultCode == SupplierConfigActivity.RESULT_DELETE_SUPPLIER) {
                    //When the result is for the Delete action

                    //Update as False so that the Product's Suppliers are redownloaded and updated on resume
                    updateAndSyncSuppliersState(false);

                    //Show the Delete Success message
                    mSalesConfigView.showDeleteSupplierSuccess(data.getStringExtra(SupplierConfigActivity.EXTRA_RESULT_SUPPLIER_CODE));
                }

            } else if (requestCode == ProductConfigActivity.REQUEST_EDIT_PRODUCT) {
                //For an Edit Product request

                if (resultCode == ProductConfigActivity.RESULT_EDIT_PRODUCT) {
                    //When the result is for the Edit action

                    //Update as False so that the Product's data is redownloaded and updated on resume
                    updateAndSyncProductState(false);

                    //Show the Update Success message
                    mSalesConfigView.showUpdateProductSuccess(data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU));

                } else if (resultCode == ProductConfigActivity.RESULT_DELETE_PRODUCT) {
                    //When the result is for the Delete action

                    //Send the code to the calling activity to display the delete result
                    doSetResult(ProductConfigActivity.RESULT_DELETE_PRODUCT, mProductId, mProductSku);
                }

            }

        }
    }

    @Override
    public void triggerFocusLost() {
        mSalesConfigView.triggerFocusLost();
    }

    @Override
    public void onSave(ArrayList<ProductSupplierSales> updatedProductSupplierSalesList) {
        //Display save progress indicator
        mSalesConfigView.showProgressIndicator(R.string.sales_config_status_saving);

        //Saving the Item's Inventory details via the Repository
        mStoreRepository.saveUpdatedProductSalesInfo(mProductId, mProductSku,
                mExistingProductSupplierSalesList,
                updatedProductSupplierSalesList, new DataRepository.DataOperationsCallback() {

                    @Override
                    public void onSuccess() {
                        //Hide progress indicator
                        mSalesConfigView.hideProgressIndicator();

                        //Set the result and finish on successful insert/update
                        doSetResult(SalesConfigActivity.RESULT_EDIT_SALES, mProductId, mProductSku);
                    }

                    @Override
                    public void onFailure(int messageId, @Nullable Object... args) {
                        //Hide progress indicator
                        mSalesConfigView.hideProgressIndicator();

                        //Show message for Insert/Update Failure
                        mSalesConfigView.showError(messageId, args);
                    }
                });
    }

    @Override
    public void showDeleteProductDialog() {
        //Delegating to the View to show the dialog
        mSalesConfigView.showDeleteProductDialog();
    }

    @Override
    public void deleteProduct() {
        //Display the Progress Indicator
        mSalesConfigView.showProgressIndicator(R.string.product_config_status_deleting);

        //For building the list of Image File URIs to be deleted
        ArrayList<String> fileContentUriList = new ArrayList<>();

        //Reading the Product Images list and adding it to the list of URIs to be deleted
        for (ProductImage productImage : mProductImages) {
            fileContentUriList.add(productImage.getImageUri());
        }

        //Executing Product Deletion via the Repository
        mStoreRepository.deleteProductById(mProductId, new DataRepository.DataOperationsCallback() {

            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mSalesConfigView.hideProgressIndicator();

                //Deleting Image files if any
                if (fileContentUriList.size() > 0) {
                    //Executing the delete operation silently on the URIs via the repository
                    mStoreRepository.deleteImageFilesSilently(fileContentUriList);
                }

                //Set the result and finish on successful delete
                doSetResult(ProductConfigActivity.RESULT_DELETE_PRODUCT, mProductId, mProductSku);
            }

            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Hide Progress Indicator
                mSalesConfigView.hideProgressIndicator();

                //Show the error message
                mSalesConfigView.showError(messageId, args);
            }
        });
    }

    @Override
    public void doSetResult(int resultCode, int productId, @NonNull String productSku) {
        mSalesConfigNavigator.doSetResult(resultCode, productId, productSku);
    }

    @Override
    public void doCancel() {
        mSalesConfigNavigator.doCancel();
    }

    @Override
    public void onUpOrBackAction() {
        if (mNewTotalAvailableQuantity != mOldTotalAvailableQuantity) {
            //When there is a change in Total Available Quantity, then there are some unsaved changes

            //Show the discard dialog to see if the user wants to keep editing/discard the changes
            mSalesConfigView.showDiscardDialog();
        } else {
            //When there is NO change in Total Available Quantity, then silently close the Activity
            finishActivity();
        }
    }

    @Override
    public void finishActivity() {
        //Return back to the calling activity
        doCancel();
    }
}
