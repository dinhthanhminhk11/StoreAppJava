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

package com.example.kaushiknsanji.storeapp.data.local.contracts;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public class SupplierContract implements StoreContract {

    public static final String PATH_SUPPLIER = "supplier";

    public static final String PATH_CONTACT_TYPE = "contacttype";

    public static final String PATH_SUPPLIER_CONTACT = "contact";

    private SupplierContract() {
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    public static final class Supplier implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SUPPLIER);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_SUPPLIER;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_SUPPLIER;


        public static final String PATH_SHORT_INFO = "short";

        public static final Uri CONTENT_URI_SHORT_INFO = Uri.withAppendedPath(CONTENT_URI, PATH_SHORT_INFO);


        public static final String CONTENT_LIST_TYPE_SHORT_INFO = CONTENT_LIST_TYPE + "." + PATH_SHORT_INFO;

        public static final String PATH_SUPPLIER_CODE = "code";

        public static final Uri CONTENT_URI_SUPPLIER_CODE = Uri.withAppendedPath(CONTENT_URI, PATH_SUPPLIER_CODE);


        public static final String CONTENT_ITEM_TYPE_SUPPLIER_CODE = CONTENT_ITEM_TYPE + "." + PATH_SUPPLIER_CODE;


        public static final String TABLE_NAME = "supplier";


        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";


        public static final String COLUMN_SUPPLIER_CODE = "supplier_code";


        public static Uri buildSupplierCodeUri(String supplierCode) {
            return CONTENT_URI_SUPPLIER_CODE.buildUpon().appendPath(supplierCode).build();
        }


        @NonNull
        public static String getQualifiedColumnName(String columnNameStr) {
            return TextUtils.concat(TABLE_NAME, ".", columnNameStr).toString();
        }
    }


    public static final class SupplierContactType implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACT_TYPE);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_CONTACT_TYPE;


        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_CONTACT_TYPE;


        public static final String TABLE_NAME = "contact_type";


        public static final String COLUMN_CONTACT_TYPE_NAME = "type_name";

        public static final String CONTACT_TYPE_PHONE = "Phone";
        public static final int CONTACT_TYPE_ID_PHONE = 0;
        public static final String CONTACT_TYPE_EMAIL = "Email";
        public static final int CONTACT_TYPE_ID_EMAIL = 1;

        private static final String[] PRELOADED_CONTACT_TYPES = new String[]{CONTACT_TYPE_PHONE, CONTACT_TYPE_EMAIL};


        @NonNull
        public static String getQualifiedColumnName(String columnNameStr) {
            return TextUtils.concat(TABLE_NAME, ".", columnNameStr).toString();
        }

        public static String[] getPreloadedContactTypes() {
            return PRELOADED_CONTACT_TYPES;
        }
    }


    public static final class SupplierContact implements BaseColumns {

        //The Content URI to access the 'supplier_contact' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Supplier.CONTENT_URI, PATH_SUPPLIER_CONTACT);


        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_SUPPLIER + "." + PATH_SUPPLIER_CONTACT;


        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_SUPPLIER + "." + PATH_SUPPLIER_CONTACT;


        public static final String TABLE_NAME = "supplier_contact";


        public static final String COLUMN_SUPPLIER_CONTACT_TYPE_ID = "contact_type_id";


        public static final String COLUMN_SUPPLIER_CONTACT_VALUE = "contact_value";

        public static final String COLUMN_SUPPLIER_CONTACT_DEFAULT = "is_default";


        public static final String COLUMN_SUPPLIER_ID = "supplier_id";


        public static final int SUPPLIER_CONTACT_DEFAULT = 1;
        public static final int SUPPLIER_CONTACT_NON_DEFAULT = 0;

        @NonNull
        public static String getQualifiedColumnName(String columnNameStr) {
            return TextUtils.concat(TABLE_NAME, ".", columnNameStr).toString();
        }
    }

}
