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

import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract;

public class ProductSupplierInfo implements Parcelable, Cloneable {


    public static final Creator<ProductSupplierInfo> CREATOR = new Creator<ProductSupplierInfo>() {

        @Override
        public ProductSupplierInfo createFromParcel(Parcel in) {
            return new ProductSupplierInfo(in);
        }


        @Override
        public ProductSupplierInfo[] newArray(int size) {
            return new ProductSupplierInfo[size];
        }
    };
    private final int mItemId;
    private final int mSupplierId;
    private float mUnitPrice;


    private ProductSupplierInfo(final int itemId, final int supplierId, float unitPrice) {
        mItemId = itemId;
        mSupplierId = supplierId;
        mUnitPrice = unitPrice;
    }
    protected ProductSupplierInfo(Parcel in) {
        mItemId = in.readInt();
        mSupplierId = in.readInt();
        mUnitPrice = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mItemId);
        dest.writeInt(mSupplierId);
        dest.writeFloat(mUnitPrice);
    }

    @Override
    public int describeContents() {
        return 0; //Indicating with no mask
    }


    public int getItemId() {
        return mItemId;
    }


    public int getSupplierId() {
        return mSupplierId;
    }


    public float getUnitPrice() {
        return mUnitPrice;
    }


    public void setUnitPrice(float unitPrice) {
        mUnitPrice = unitPrice;
    }


    @Override
    public final Object clone() {
        //Returning a new instance of ProductSupplierInfo constructed using the Builder
        return new Builder()
                .setItemId(this.mItemId)
                .setSupplierId(this.mSupplierId)
                .setUnitPrice(this.mUnitPrice)
                .createProductSupplierInfo();
    }


    @Override
    public String toString() {
        return "ProductSupplierInfo{" +
                "mItemId=" + mItemId +
                ", mSupplierId=" + mSupplierId +
                ", mUnitPrice=" + mUnitPrice +
                '}';
    }


    public static class Builder {

        private int mItemId;
        private int mSupplierId;
        private float mUnitPrice = SalesContract.ProductSupplierInfo.DEFAULT_ITEM_UNIT_PRICE;


        public Builder setItemId(int itemId) {
            mItemId = itemId;
            return this;
        }


        public Builder setSupplierId(int supplierId) {
            mSupplierId = supplierId;
            return this;
        }


        public Builder setUnitPrice(float unitPrice) {
            mUnitPrice = unitPrice;
            return this;
        }


        public ProductSupplierInfo createProductSupplierInfo() {
            return new ProductSupplierInfo(mItemId, mSupplierId, mUnitPrice);
        }
    }
}
