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


public class ProductAttribute implements Parcelable {


    public static final Creator<ProductAttribute> CREATOR = new Creator<ProductAttribute>() {

        @Override
        public ProductAttribute createFromParcel(Parcel in) {
            return new ProductAttribute(in);
        }

        @Override
        public ProductAttribute[] newArray(int size) {
            return new ProductAttribute[size];
        }
    };
    private String mAttributeName;
    private String mAttributeValue;

    private ProductAttribute(String attributeName, String attributeValue) {
        mAttributeName = attributeName;
        mAttributeValue = attributeValue;
    }

    protected ProductAttribute(Parcel in) {
        mAttributeName = in.readString();
        mAttributeValue = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAttributeName);
        dest.writeString(mAttributeValue);
    }

    @Override
    public int describeContents() {
        return 0; //Indicating with no mask
    }

    public String getAttributeName() {
        return mAttributeName;
    }

    public void setAttributeName(String attributeName) {
        mAttributeName = attributeName;
    }

    public String getAttributeValue() {
        return mAttributeValue;
    }
    public void setAttributeValue(String attributeValue) {
        mAttributeValue = attributeValue;
    }

    public static class Builder {

        private String mAttributeName;
        private String mAttributeValue;

        public Builder setAttributeName(String attributeName) {
            mAttributeName = attributeName;
            return this;
        }

        public Builder setAttributeValue(String attributeValue) {
            mAttributeValue = attributeValue;
            return this;
        }

        public ProductAttribute createProductAttribute() {
            return new ProductAttribute(mAttributeName, mAttributeValue);
        }
    }
}
