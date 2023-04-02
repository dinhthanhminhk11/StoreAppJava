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

import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;
import java.util.List;


public interface SalesConfigContract {

    interface View extends BaseView<Presenter> {


        void syncProductState(boolean isProductRestored);


        void syncSuppliersState(boolean areSuppliersRestored);


        void syncOldTotalAvailability(int oldTotalAvailableQuantity);


        void showProgressIndicator(@StringRes int statusTextId);

        void hideProgressIndicator();


        void showError(@StringRes int messageId, @Nullable Object... args);


        void updateProductName(String productName);


        void updateProductSku(String productSku);


        void updateProductCategory(String productCategory);


        void updateProductDescription(String description);


        void updateProductImages(ArrayList<ProductImage> productImages);


        void updateProductAttributes(ArrayList<ProductAttribute> productAttributes);


        void loadProductSuppliersData(ArrayList<ProductSupplierSales> productSupplierSalesList);

        void updateAvailability(int totalAvailableQuantity);


        void showOutOfStockAlert();


        void showProductSupplierSwiped(String supplierCode);


        void showUpdateProductSuccess(String productSku);


        void showUpdateSupplierSuccess(String supplierCode);


        void showDeleteSupplierSuccess(String supplierCode);

        void triggerFocusLost();


        void showDiscardDialog();


        void showDeleteProductDialog();

    }


    interface Presenter extends BasePresenter {

        void updateAndSyncProductState(boolean isProductRestored);

        void updateAndSyncSuppliersState(boolean areSuppliersRestored);

        void updateAndSyncOldTotalAvailability(int oldTotalAvailableQuantity);

        void updateProductName(String productName);

        void updateProductSku(String productSku);


        void updateProductCategory(String productCategory);

        void updateProductDescription(String description);

        void updateProductImage(ArrayList<ProductImage> productImages);

        void updateProductAttributes(ArrayList<ProductAttribute> productAttributes);

        void updateProductSupplierSalesList(List<ProductSupplierSales> productSupplierSalesList);


        void editProduct(int productId);

        void editSupplier(int supplierId);

        void onProductSupplierSwiped(String supplierCode);

        void procureProduct(ProductSupplierSales productSupplierSales);

        void updateAvailability(int totalAvailableQuantity);

        void changeAvailability(int changeInAvailableQuantity);

        void onActivityResult(int requestCode, int resultCode, Intent data);

        void triggerFocusLost();

        void onSave(ArrayList<ProductSupplierSales> updatedProductSupplierSalesList);

        void showDeleteProductDialog();

        void deleteProduct();

        void doSetResult(final int resultCode, final int productId, @NonNull final String productSku);

        void doCancel();

        void onUpOrBackAction();

        void finishActivity();

    }

}