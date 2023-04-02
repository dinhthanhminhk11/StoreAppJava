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

import android.support.annotation.NonNull;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.products.config.DefaultPhotoChangeListener;

import java.util.ArrayList;
import java.util.List;


public class SalesProcurementPresenter implements SalesProcurementContract.Presenter {

    private static final String LOG_TAG = SalesProcurementPresenter.class.getSimpleName();

    @NonNull
    private final SalesProcurementContract.View mSalesProcurementView;

    @NonNull
    private final StoreRepository mStoreRepository;

    private final SalesProcurementNavigator mSalesProcurementNavigator;

    private final DefaultPhotoChangeListener mDefaultPhotoChangeListener;

    private String mProductName;

    private String mProductSku;

    private ProductImage mProductImage;

    private ProductSupplierSales mProductSupplierSales;
    private boolean mAreSupplierContactsRestored;


    SalesProcurementPresenter(@NonNull StoreRepository storeRepository, @NonNull SalesProcurementContract.View salesProcurementView, String productName, String productSku, ProductImage productImage, ProductSupplierSales productSupplierSales, SalesProcurementNavigator salesProcurementNavigator, @NonNull DefaultPhotoChangeListener defaultPhotoChangeListener) {
        mStoreRepository = storeRepository;
        mSalesProcurementView = salesProcurementView;
        mProductName = productName;
        mProductSku = productSku;
        mProductImage = productImage;
        mProductSupplierSales = productSupplierSales;
        mSalesProcurementNavigator = salesProcurementNavigator;
        mDefaultPhotoChangeListener = defaultPhotoChangeListener;

        //Registering the View with the Presenter
        mSalesProcurementView.setPresenter(this);
    }


    @Override
    public void start() {

        updateProductSupplierTitle();

        updateProductSupplierAvailability();

        updateProductImage();

        loadSupplierContacts();

    }


    @Override
    public void updateAndSyncContactsState(boolean areSupplierContactsRestored) {
        //Saving the state
        mAreSupplierContactsRestored = areSupplierContactsRestored;

        //Syncing the state to the View
        mSalesProcurementView.syncContactsState(mAreSupplierContactsRestored);
    }

    private void loadSupplierContacts() {
        if (!mAreSupplierContactsRestored) {

            mSalesProcurementView.showProgressIndicator(R.string.sales_procurement_status_loading_contacts);
            mStoreRepository.getSupplierContactsById(mProductSupplierSales.getSupplierId(), new DataRepository.GetQueryCallback<List<SupplierContact>>() {
                @Override
                public void onResults(List<SupplierContact> supplierContacts) {
                    updateSupplierContacts(supplierContacts);

                    updateAndSyncContactsState(true);

                    mSalesProcurementView.hideProgressIndicator();
                }

                @Override
                public void onEmpty() {
                    mSalesProcurementView.hidePhoneContacts();
                    mSalesProcurementView.hideEmailContacts();
                    mSalesProcurementView.showEmptyContactsView();

                    mSalesProcurementView.hideProgressIndicator();
                }
            });
        }
    }

    @Override
    public void updateSupplierContacts(List<SupplierContact> supplierContacts) {

        ArrayList<SupplierContact> phoneContacts = new ArrayList<>();

        ArrayList<SupplierContact> emailContacts = new ArrayList<>();

        for (SupplierContact supplierContact : supplierContacts) {
            switch (supplierContact.getType()) {
                case SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE:
                    phoneContacts.add((SupplierContact) supplierContact.clone());
                    break;
                case SupplierContract.SupplierContactType.CONTACT_TYPE_EMAIL:
                    emailContacts.add((SupplierContact) supplierContact.clone());
                    break;
            }
        }

        if (phoneContacts.size() > 0 || emailContacts.size() > 0) {

            if (phoneContacts.size() > 0) {
                mSalesProcurementView.updatePhoneContacts(phoneContacts);
            } else {
                mSalesProcurementView.hidePhoneContacts();
            }

            if (emailContacts.size() > 0) {
                mSalesProcurementView.updateEmailContacts(emailContacts);
            } else {
                mSalesProcurementView.hideEmailContacts();
            }

        } else {
            mSalesProcurementView.hidePhoneContacts();
            mSalesProcurementView.hideEmailContacts();
            mSalesProcurementView.showEmptyContactsView();
        }
    }

    private void updateProductSupplierTitle() {
        mSalesProcurementView.updateProductSupplierTitle(R.string.sales_procurement_title_product_supplier, mProductName, mProductSku, mProductSupplierSales.getSupplierName(), mProductSupplierSales.getSupplierCode());
    }

    private void updateProductSupplierAvailability() {
        int supplierAvailableQuantity = mProductSupplierSales.getAvailableQuantity();

        if (supplierAvailableQuantity > 0) {
            mSalesProcurementView.updateProductSupplierAvailability(supplierAvailableQuantity);

        } else {
            mSalesProcurementView.showOutOfStockAlert();
        }

    }

    private void updateProductImage() {
        if (mProductImage != null) {
            mDefaultPhotoChangeListener.showSelectedProductImage(mProductImage.getImageUri());
        } else {
            mDefaultPhotoChangeListener.showDefaultImage();
        }
    }

    @Override
    public void phoneClicked(SupplierContact supplierContact) {
        //Delegating to the View to launch the Phone Dialer
        mSalesProcurementView.dialPhoneNumber(supplierContact.getValue());
    }

    @Override
    public void sendMailClicked(String requiredQuantityStr, ArrayList<SupplierContact> emailContacts) {
        String toEmailAddress = "";
        ArrayList<String> ccAddressList = new ArrayList<>();

        for (SupplierContact emailContact : emailContacts) {
            if (emailContact.isDefault()) {
                toEmailAddress = emailContact.getValue();
            } else {
                ccAddressList.add(emailContact.getValue());
            }
        }

        String[] subjectArgs = new String[3];
        subjectArgs[0] = requiredQuantityStr;
        subjectArgs[1] = mProductName;
        subjectArgs[2] = mProductSku;

        String[] bodyArgs = new String[6];
        bodyArgs[0] = mProductSupplierSales.getSupplierName();
        bodyArgs[1] = mProductSupplierSales.getSupplierCode();
        bodyArgs[2] = String.valueOf(mProductSupplierSales.getAvailableQuantity());
        bodyArgs[3] = mProductName;
        bodyArgs[4] = mProductSku;
        bodyArgs[5] = requiredQuantityStr;

        mSalesProcurementView.composeEmail(toEmailAddress, ccAddressList.toArray(new String[0]), R.string.sales_procurement_email_subject, subjectArgs, R.string.sales_procurement_email_body, bodyArgs);
    }

    @Override
    public void doFinish() {
        mSalesProcurementNavigator.doFinish();
    }
}