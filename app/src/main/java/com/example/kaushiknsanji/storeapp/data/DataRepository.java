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

package com.example.kaushiknsanji.storeapp.data;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.kaushiknsanji.storeapp.data.local.models.Product;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.data.local.models.Supplier;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;

import java.util.ArrayList;
import java.util.List;


public interface DataRepository {

    void getAllCategories(@NonNull GetQueryCallback<List<String>> queryCallback);

    void getCategoryByName(@NonNull String categoryName, @NonNull GetQueryCallback<Integer> queryCallback);

    void getProductDetailsById(int productId, @NonNull GetQueryCallback<Product> queryCallback);

    void getProductSkuUniqueness(@NonNull String productSku, @NonNull GetQueryCallback<Boolean> queryCallback);


    void saveNewProduct(@NonNull Product newProduct, @NonNull DataOperationsCallback operationsCallback);

    void saveUpdatedProduct(@NonNull Product existingProduct, @NonNull Product newProduct,
                            @NonNull DataOperationsCallback operationsCallback);

    void deleteProductById(int productId, @NonNull DataOperationsCallback operationsCallback);

    void saveProductImages(@NonNull Product existingProduct, @NonNull ArrayList<ProductImage> productImages, @NonNull DataOperationsCallback operationsCallback);

    void registerContentObserver(@NonNull Uri uri, boolean notifyForDescendants,
                                 @NonNull ContentObserver observer);

    void unregisterContentObserver(@NonNull ContentObserver observer);

    void getSupplierDetailsById(int supplierId, @NonNull GetQueryCallback<Supplier> queryCallback);

    void getSupplierContactsById(int supplierId, @NonNull GetQueryCallback<List<SupplierContact>> queryCallback);

    void getSupplierCodeUniqueness(@NonNull String supplierCode, @NonNull GetQueryCallback<Boolean> queryCallback);

    void getShortProductInfoForProducts(@Nullable List<String> productIds, @NonNull GetQueryCallback<List<ProductLite>> queryCallback);

    void saveNewSupplier(@NonNull Supplier newSupplier, @NonNull DataOperationsCallback operationsCallback);


    void saveUpdatedSupplier(@NonNull Supplier existingSupplier, @NonNull Supplier newSupplier,
                             @NonNull DataOperationsCallback operationsCallback);


    void deleteSupplierById(int supplierId, @NonNull DataOperationsCallback operationsCallback);


    void decreaseProductSupplierInventory(int productId, String productSku, int supplierId, String supplierCode, int availableQuantity,
                                          int decreaseQuantityBy, @NonNull DataOperationsCallback operationsCallback);


    void getProductSuppliersSalesInfo(int productId, @NonNull GetQueryCallback<List<ProductSupplierSales>> queryCallback);


    void saveUpdatedProductSalesInfo(int productId, String productSku, @NonNull List<ProductSupplierSales> existingProductSupplierSales,
                                     @NonNull List<ProductSupplierSales> updatedProductSupplierSales,
                                     @NonNull DataOperationsCallback operationsCallback);


    interface GetQueryCallback<T> {

        void onResults(T results);


        void onEmpty();

        default void onFailure(@StringRes int messageId, @Nullable Object... args) {
        }
    }


    interface DataOperationsCallback {

        void onSuccess();


        void onFailure(@StringRes int messageId, @Nullable Object... args);
    }


    interface CursorDataLoaderCallback {

        void onDataLoaded(Cursor data);

        void onDataEmpty();


        void onDataNotAvailable();

        void onDataReset();


        void onContentChange();
    }
}
