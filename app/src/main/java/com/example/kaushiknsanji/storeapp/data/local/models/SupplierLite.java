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


public class SupplierLite implements Parcelable {

    public static final Creator<SupplierLite> CREATOR = new Creator<SupplierLite>() {
        @Override
        public SupplierLite createFromParcel(Parcel in) {
            return new SupplierLite(in);
        }

        @Override
        public SupplierLite[] newArray(int size) {
            return new SupplierLite[size];
        }
    };
    private final int mId;
    @NonNull
    private final String mName;
    @NonNull
    private final String mCode;
    private final String mDefaultPhone;
    private final String mDefaultEmail;
    private final int mItemCount;

    private SupplierLite(int id, @NonNull String name, @NonNull String code, String defaultPhone, String defaultEmail, int itemCount) {
        mId = id;
        mName = name;
        mCode = code;
        mDefaultPhone = defaultPhone;
        mDefaultEmail = defaultEmail;
        mItemCount = itemCount;
    }

    protected SupplierLite(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mCode = in.readString();
        mDefaultPhone = in.readString();
        mDefaultEmail = in.readString();
        mItemCount = in.readInt();
    }

    public static SupplierLite from(Cursor cursor) {
        return new SupplierLite(
                cursor.getInt(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_ID_INDEX),
                cursor.getString(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_NAME_INDEX),
                cursor.getString(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_CODE_INDEX),
                cursor.getString(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_DEFAULT_PHONE_INDEX),
                cursor.getString(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_DEFAULT_EMAIL_INDEX),
                cursor.getInt(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_ITEM_COUNT_INDEX)
        );
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mCode);
        dest.writeString(mDefaultPhone);
        dest.writeString(mDefaultEmail);
        dest.writeInt(mItemCount);
    }

    @Override
    public int describeContents() {
        return 0; //Indicating with no mask
    }

    public int getId() {
        return mId;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @NonNull
    public String getCode() {
        return mCode;
    }

    public String getDefaultPhone() {
        return mDefaultPhone;
    }

    public String getDefaultEmail() {
        return mDefaultEmail;
    }

    public int getItemCount() {
        return mItemCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SupplierLite that = (SupplierLite) o;

        if (mId != that.mId) return false;
        if (mItemCount != that.mItemCount) return false;
        if (!mName.equals(that.mName)) return false;
        if (!mCode.equals(that.mCode)) return false;
        if (mDefaultPhone != null ? !mDefaultPhone.equals(that.mDefaultPhone) : that.mDefaultPhone != null)
            return false;
        return mDefaultEmail != null ? mDefaultEmail.equals(that.mDefaultEmail) : that.mDefaultEmail == null;
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mName.hashCode();
        result = 31 * result + mCode.hashCode();
        result = 31 * result + (mDefaultPhone != null ? mDefaultPhone.hashCode() : 0);
        result = 31 * result + (mDefaultEmail != null ? mDefaultEmail.hashCode() : 0);
        result = 31 * result + mItemCount;
        return result;
    }
}
