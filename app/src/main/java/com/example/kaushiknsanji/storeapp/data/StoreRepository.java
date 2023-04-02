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

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.data.local.models.Product;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.data.local.models.Supplier;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;

import java.util.ArrayList;
import java.util.List;


public class StoreRepository implements DataRepository, FileRepository {

    //Constant used for logs
    private static final String LOG_TAG = StoreRepository.class.getSimpleName();

    //Singleton instance of StoreRepository
    private static volatile StoreRepository INSTANCE;

    //Instance of DataRepository to communicate with Database
    private final DataRepository mLocalDataSource;

    //Instance of FileRepository to communicate with Files
    private final FileRepository mLocalFileSource;

    private StoreRepository(@NonNull DataRepository localDataSource, @NonNull FileRepository localFileSource) {
        mLocalDataSource = localDataSource;
        mLocalFileSource = localFileSource;
    }

    public static StoreRepository getInstance(@NonNull DataRepository localDataSource, @NonNull FileRepository localFileSource) {
        if (INSTANCE == null) {
            //When instance is not available
            synchronized (StoreRepository.class) {
                //Apply lock and check for the instance again
                if (INSTANCE == null) {
                    //When there is no instance, create a new one
                    INSTANCE = new StoreRepository(localDataSource, localFileSource);
                }
            }
        }
        //Returning the instance of StoreRepository
        return INSTANCE;
    }

    @Override
    public void getAllCategories(@NonNull GetQueryCallback<List<String>> queryCallback) {
        mLocalDataSource.getAllCategories(queryCallback);
    }

    @Override
    public void getCategoryByName(@NonNull String categoryName, @NonNull GetQueryCallback<Integer> queryCallback) {
        mLocalDataSource.getCategoryByName(categoryName, queryCallback);
    }


    @Override
    public void getProductDetailsById(int productId, @NonNull GetQueryCallback<Product> queryCallback) {
        mLocalDataSource.getProductDetailsById(productId, queryCallback);
    }


    @Override
    public void getProductSkuUniqueness(@NonNull String productSku, @NonNull GetQueryCallback<Boolean> queryCallback) {
        mLocalDataSource.getProductSkuUniqueness(productSku, queryCallback);
    }


    @Override
    public void saveNewProduct(@NonNull Product newProduct, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveNewProduct(newProduct, operationsCallback);
    }

    @Override
    public void saveUpdatedProduct(@NonNull Product existingProduct, @NonNull Product newProduct,
                                   @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveUpdatedProduct(existingProduct, newProduct, operationsCallback);
    }

    @Override
    public void deleteProductById(int productId, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.deleteProductById(productId, operationsCallback);
    }


    @Override
    public void saveProductImages(@NonNull Product existingProduct, @NonNull ArrayList<ProductImage> productImages, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveProductImages(existingProduct, productImages, operationsCallback);
    }


    @Override
    public void registerContentObserver(@NonNull Uri uri, boolean notifyForDescendants, @NonNull ContentObserver observer) {
        mLocalDataSource.registerContentObserver(uri, notifyForDescendants, observer);
    }


    @Override
    public void unregisterContentObserver(@NonNull ContentObserver observer) {
        mLocalDataSource.unregisterContentObserver(observer);
    }


    @Override
    public void getSupplierDetailsById(int supplierId, @NonNull GetQueryCallback<Supplier> queryCallback) {
        mLocalDataSource.getSupplierDetailsById(supplierId, queryCallback);
    }


    @Override
    public void getSupplierContactsById(int supplierId, @NonNull GetQueryCallback<List<SupplierContact>> queryCallback) {
        mLocalDataSource.getSupplierContactsById(supplierId, queryCallback);
    }


    @Override
    public void getSupplierCodeUniqueness(@NonNull String supplierCode, @NonNull GetQueryCallback<Boolean> queryCallback) {
        mLocalDataSource.getSupplierCodeUniqueness(supplierCode, queryCallback);
    }


    @Override
    public void getShortProductInfoForProducts(@Nullable List<String> productIds, @NonNull GetQueryCallback<List<ProductLite>> queryCallback) {
        mLocalDataSource.getShortProductInfoForProducts(productIds, queryCallback);
    }


    @Override
    public void saveNewSupplier(@NonNull Supplier newSupplier, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveNewSupplier(newSupplier, operationsCallback);
    }


    @Override
    public void saveUpdatedSupplier(@NonNull Supplier existingSupplier, @NonNull Supplier newSupplier, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveUpdatedSupplier(existingSupplier, newSupplier, operationsCallback);
    }


    @Override
    public void deleteSupplierById(int supplierId, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.deleteSupplierById(supplierId, operationsCallback);
    }


    @Override
    public void decreaseProductSupplierInventory(int productId, String productSku,
                                                 int supplierId, String supplierCode,
                                                 int availableQuantity, int decreaseQuantityBy,
                                                 @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.decreaseProductSupplierInventory(productId, productSku, supplierId, supplierCode, availableQuantity, decreaseQuantityBy, operationsCallback);
    }


    @Override
    public void getProductSuppliersSalesInfo(int productId, @NonNull GetQueryCallback<List<ProductSupplierSales>> queryCallback) {
        mLocalDataSource.getProductSuppliersSalesInfo(productId, queryCallback);
    }


    @Override
    public void saveUpdatedProductSalesInfo(int productId, String productSku,
                                            @NonNull List<ProductSupplierSales> existingProductSupplierSales,
                                            @NonNull List<ProductSupplierSales> updatedProductSupplierSales,
                                            @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveUpdatedProductSalesInfo(productId, productSku,
                existingProductSupplierSales, updatedProductSupplierSales, operationsCallback);
    }


    @Override
    public void saveImageToFile(Context context, Uri fileContentUri, FileOperationsCallback<Uri> operationsCallback) {
        mLocalFileSource.saveImageToFile(context, fileContentUri, operationsCallback);
    }


    @Override
    public void takePersistablePermissions(Uri fileContentUri, int intentFlags) {
        mLocalFileSource.takePersistablePermissions(fileContentUri, intentFlags);
    }


    @Override
    public void deleteImageFiles(List<String> fileContentUriList, FileOperationsCallback<Boolean> operationsCallback) {
        mLocalFileSource.deleteImageFiles(fileContentUriList, operationsCallback);
    }


    @Override
    public void deleteImageFilesSilently(List<String> fileContentUriList) {
        mLocalFileSource.deleteImageFiles(fileContentUriList, new FileRepository.FileOperationsCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean results) {
                Log.i(LOG_TAG, "onSuccess: deleteImageFilesSilently: All Image files deleted");
            }

            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                Log.i(LOG_TAG, "onFailure: deleteImageFilesSilently: Some Image files were not deleted");
            }
        });
    }
}
