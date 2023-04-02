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

package com.example.kaushiknsanji.storeapp.ui.products.config;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;
import java.util.List;

public interface ProductConfigContract {

    //Integer Constant used as the Product ID for New Product Configuration
    int NEW_PRODUCT_INT = -1;
    //Constant for custom Category "Other" option
    String CATEGORY_OTHER = "Other";

    interface View extends BaseView<Presenter> {


        void updateCategories(List<String> categories);

        void showCategoryOtherEditTextField();

        void hideCategoryOtherEditTextField();

        void clearCategoryOtherEditTextField();

        void showEmptyFieldsValidationError();

        void showAttributesPartialValidationError();

        void showAttributeNameConflictError(String attributeName);

        void showProgressIndicator(@StringRes int statusTextId);

        void hideProgressIndicator();

        void showError(@StringRes int messageId, @Nullable Object... args);

        void showProductSkuConflictError();

        void showProductSkuEmptyError();

        void updateProductNameField(String name);

        void updateProductSkuField(String sku);

        void lockProductSkuField();

        void updateProductDescriptionField(String description);

        void updateProductAttributes(ArrayList<ProductAttribute> productAttributes);

        void updateProductImages(ArrayList<ProductImage> productImages);

        void updateCategorySelection(String selectedCategory, @Nullable String categoryOtherText);

        void syncExistingProductState(boolean isExistingProductRestored);

        void syncProductSkuValidity(boolean isProductSkuValid);


        void syncProductNameEnteredState(boolean isProductNameEntered);

        void showDiscardDialog();

        void showDeleteProductDialog();

        void triggerFocusLost();

        void showUpdateImagesSuccess();
    }

    interface Presenter extends BasePresenter {

        void updateAndSyncExistingProductState(boolean isExistingProductRestored);

        void updateAndSyncProductSkuValidity(boolean isProductSkuValid);

        void updateAndSyncProductNameEnteredState(boolean isProductNameEntered);

        void onCategorySelected(String categoryName);

        void updateProductNameField(String name);

        void updateProductSkuField(String sku);

        void updateProductDescriptionField(String description);

        void updateCategorySelection(String selectedCategory, @Nullable String categoryOtherText);

        void updateProductAttributes(ArrayList<ProductAttribute> productAttributes);

        void updateProductImages(ArrayList<ProductImage> productImages);

        void onSave(String productName, String productSku, String productDescription,
                    String categorySelected, String categoryOtherText,
                    ArrayList<ProductAttribute> productAttributes);

        void validateProductSku(String productSku);

        void openProductImages();

        void onActivityResult(int requestCode, int resultCode, Intent data);

        void onUpOrBackAction();

        void finishActivity();

        void showDeleteProductDialog();

        void deleteProduct();

        void triggerFocusLost();

        void doSetResult(final int resultCode, final int productId, @NonNull final String productSku);

        void doCancel();
    }
}
