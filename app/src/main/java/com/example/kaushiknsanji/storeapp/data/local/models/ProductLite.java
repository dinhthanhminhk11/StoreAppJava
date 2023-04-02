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

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.kaushiknsanji.storeapp.data.local.utils.QueryArgsUtility;

public class ProductLite implements Parcelable {


    public static final Creator<ProductLite> CREATOR = new Creator<ProductLite>() {

        @Override
        public ProductLite createFromParcel(Parcel in) {
            return new ProductLite(in);
        }


        @Override
        public ProductLite[] newArray(int size) {
            return new ProductLite[size];
        }
    };
    private final int mId;
    private final String mName;
    private final String mSku;
    private final String mCategory;
    private final String mDefaultImageUri;


    private ProductLite(int id, String name, String sku, String category, String defaultImageUri) {
        mId = id;
        mName = name;
        mSku = sku;
        mCategory = category;
        mDefaultImageUri = defaultImageUri;
    }


    protected ProductLite(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mSku = in.readString();
        mCategory = in.readString();
        mDefaultImageUri = in.readString();
    }


    @NonNull
    public static ProductLite from(Cursor cursor) {
        return new ProductLite(
                cursor.getInt(QueryArgsUtility.ItemsShortInfoQuery.COLUMN_ITEM_ID_INDEX),
                cursor.getString(QueryArgsUtility.ItemsShortInfoQuery.COLUMN_ITEM_NAME_INDEX),
                cursor.getString(QueryArgsUtility.ItemsShortInfoQuery.COLUMN_ITEM_SKU_INDEX),
                cursor.getString(QueryArgsUtility.ItemsShortInfoQuery.COLUMN_ITEM_CATEGORY_NAME_INDEX),
                cursor.getString(QueryArgsUtility.ItemsShortInfoQuery.COLUMN_ITEM_IMAGE_URI_INDEX)
        );
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mSku);
        dest.writeString(mCategory);
        dest.writeString(mDefaultImageUri);
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


    public String getSku() {
        return mSku;
    }


    public String getCategory() {
        return mCategory;
    }


    public String getDefaultImageUri() {
        return mDefaultImageUri;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductLite that = (ProductLite) o;

        if (mId != that.mId) return false;
        if (!mName.equals(that.mName)) return false;
        if (!mSku.equals(that.mSku)) return false;
        if (!mCategory.equals(that.mCategory)) return false;

        return mDefaultImageUri != null ? mDefaultImageUri.equals(that.mDefaultImageUri) : that.mDefaultImageUri == null;
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mName.hashCode();
        result = 31 * result + mSku.hashCode();
        result = 31 * result + mCategory.hashCode();
        result = 31 * result + (mDefaultImageUri != null ? mDefaultImageUri.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "ProductLite{" +
                "mId=" + mId +
                ", mName='" + mName + '\'' +
                ", mSku='" + mSku + '\'' +
                ", mCategory='" + mCategory + '\'' +
                ", mDefaultImageUri='" + mDefaultImageUri + '\'' +
                '}';
    }
}