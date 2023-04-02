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

package com.example.kaushiknsanji.storeapp.ui.inventory.procure;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;
import java.util.List;

public interface SalesProcurementContract {

    interface View extends BaseView<Presenter> {

        void syncContactsState(boolean areSupplierContactsRestored);


        void showProgressIndicator(@StringRes int statusTextId);

        void hideProgressIndicator();

        void showError(@StringRes int messageId, @Nullable Object... args);

        void updatePhoneContacts(ArrayList<SupplierContact> phoneContacts);

        void hidePhoneContacts();

        void updateEmailContacts(ArrayList<SupplierContact> emailContacts);

        void hideEmailContacts();

        void showEmptyContactsView();

        void updateProductSupplierTitle(@StringRes int titleResId, String productName, String productSku, String supplierName, String supplierCode);

        void updateProductSupplierAvailability(int availableQuantity);

        void showOutOfStockAlert();

        void dialPhoneNumber(String phoneNumber);

        void composeEmail(String toEmailAddress, String[] ccAddresses, @StringRes int subjectResId,
                          Object[] subjectArgs, @StringRes int bodyResId, Object[] bodyArgs);
    }

    interface Presenter extends BasePresenter {

        void updateAndSyncContactsState(boolean areSupplierContactsRestored);


        void updateSupplierContacts(List<SupplierContact> supplierContacts);

        void phoneClicked(SupplierContact supplierContact);

        void sendMailClicked(String requiredQuantityStr, ArrayList<SupplierContact> emailContacts);

        void doFinish();

    }

}
