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
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityOptionsCompat;

import com.example.kaushiknsanji.storeapp.data.local.models.SalesLite;
import com.example.kaushiknsanji.storeapp.ui.PagerPresenter;
import com.example.kaushiknsanji.storeapp.ui.PagerView;

import java.util.ArrayList;


public interface SalesListContract {


    interface View extends PagerView<Presenter> {


        void showProgressIndicator();


        void hideProgressIndicator();


        void showError(@StringRes int messageId, @Nullable Object... args);


        void showEmptyView();


        void hideEmptyView();


        void loadSalesList(ArrayList<SalesLite> salesList);


        void showDeleteSuccess(String productSku);


        void showSellQuantitySuccess(String productSku, String supplierCode);


        void showUpdateInventorySuccess(String productSku);


        void launchEditProductSales(int productId, ActivityOptionsCompat activityOptionsCompat);

    }


    interface Presenter extends PagerPresenter {


        void triggerProductSalesLoad(boolean forceLoad);


        void onProductContentChange();


        void releaseResources();


        void deleteProduct(int productId, String productSku);


        void sellOneQuantity(SalesLite salesLite);


        void onActivityResult(int requestCode, int resultCode, Intent data);


        void editProductSales(int productId, ActivityOptionsCompat activityOptionsCompat);
    }
}
