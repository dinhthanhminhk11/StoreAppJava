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

package com.example.kaushiknsanji.storeapp.data.local.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class Supplier implements Parcelable {

    public static final Creator<Supplier> CREATOR = new Creator<Supplier>() {

        @Override
        public Supplier createFromParcel(Parcel in) {
            return new Supplier(in);
        }

        @Override
        public Supplier[] newArray(int size) {
            return new Supplier[size];
        }
    };
    private final int mId;
    private String mName;
    private String mCode;
    private ArrayList<SupplierContact> mContacts;
    private ArrayList<ProductSupplierInfo> mProductSupplierInfoList;

    private Supplier(int id,
                     String name,
                     String code,
                     ArrayList<SupplierContact> contacts,
                     ArrayList<ProductSupplierInfo> productSupplierInfoList) {
        mId = id;
        mName = name;
        mCode = code;
        mContacts = contacts;
        mProductSupplierInfoList = productSupplierInfoList;
    }

    protected Supplier(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mCode = in.readString();
        mContacts = in.createTypedArrayList(SupplierContact.CREATOR);
        mProductSupplierInfoList = in.createTypedArrayList(ProductSupplierInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mCode);
        dest.writeTypedList(mContacts);
        dest.writeTypedList(mProductSupplierInfoList);
    }


    @Override
    public int describeContents() {
        return 0; //Indicating with no mask
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public ArrayList<SupplierContact> getContacts() {
        return mContacts;
    }

    public void setContacts(ArrayList<SupplierContact> contacts) {
        mContacts = contacts;
    }

    public ArrayList<ProductSupplierInfo> getProductSupplierInfoList() {
        return mProductSupplierInfoList;
    }

    public void setProductSupplierInfoList(ArrayList<ProductSupplierInfo> productSupplierInfoList) {
        mProductSupplierInfoList = productSupplierInfoList;
    }

    public static class Builder {

        private int mId;
        private String mName;
        private String mCode;
        private ArrayList<SupplierContact> mContacts;
        private ArrayList<ProductSupplierInfo> mProductSupplierInfoList;


        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setCode(String code) {
            mCode = code;
            return this;
        }

        public Builder setContacts(ArrayList<SupplierContact> contacts) {
            mContacts = contacts;
            return this;
        }

        public Builder setProductSupplierInfoList(ArrayList<ProductSupplierInfo> productSupplierInfoList) {
            mProductSupplierInfoList = productSupplierInfoList;
            return this;
        }

        public Supplier createSupplier() {
            //Returning the instance built
            return new Supplier(mId, mName, mCode, mContacts, mProductSupplierInfoList);
        }
    }
}
