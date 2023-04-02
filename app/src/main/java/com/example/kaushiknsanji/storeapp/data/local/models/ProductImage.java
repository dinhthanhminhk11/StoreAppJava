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

public class ProductImage implements Parcelable, Cloneable {


    public static final Creator<ProductImage> CREATOR = new Creator<ProductImage>() {
        @Override
        public ProductImage createFromParcel(Parcel in) {
            return new ProductImage(in);
        }

        @Override
        public ProductImage[] newArray(int size) {
            return new ProductImage[size];
        }
    };
    private final String mImageUri;
    private boolean mIsDefault;

    private ProductImage(String imageUri, boolean isDefault) {
        mImageUri = imageUri;
        mIsDefault = isDefault;
    }

    protected ProductImage(Parcel in) {
        mImageUri = in.readString();
        mIsDefault = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImageUri);
        dest.writeByte((byte) (mIsDefault ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0; //Indicating with no mask
    }

    public String getImageUri() {
        return mImageUri;
    }

    public boolean isDefault() {
        return mIsDefault;
    }

    public void setDefault(boolean isDefault) {
        mIsDefault = isDefault;
    }

    @Override
    public final Object clone() {
        //Returning a new instance of ProductImage constructed using the Builder
        return new Builder().setImageUri(this.mImageUri).setIsDefault(this.mIsDefault).createProductImage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductImage that = (ProductImage) o;

        if (mIsDefault != that.mIsDefault) return false;
        return mImageUri.equals(that.mImageUri);
    }

    @Override
    public int hashCode() {
        int result = mImageUri.hashCode();
        result = 31 * result + (mIsDefault ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProductImage{" + "mImageUri='" + mImageUri + '\'' + ", mIsDefault=" + mIsDefault + '}';
    }

    public static class Builder {

        private String mImageUri;
        private boolean mIsDefault = false;

        public Builder setImageUri(String imageUri) {
            mImageUri = imageUri;
            return this;
        }

        public Builder setIsDefault(boolean isDefault) {
            mIsDefault = isDefault;
            return this;
        }

        public ProductImage createProductImage() {
            return new ProductImage(mImageUri, mIsDefault);
        }
    }
}
