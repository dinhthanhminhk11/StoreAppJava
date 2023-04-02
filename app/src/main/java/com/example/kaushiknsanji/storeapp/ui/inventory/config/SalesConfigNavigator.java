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

import android.support.annotation.NonNull;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;


public interface SalesConfigNavigator {


    void doSetResult(final int resultCode, final int productId, @NonNull final String productSku);

    void doCancel();

    void launchEditProduct(int productId);

    void launchProcureProduct(ProductSupplierSales productSupplierSales, ProductImage productImageToBeShown, String productName, String productSku);

    void launchEditSupplier(int supplierId);
}
