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


public class SalesContract implements StoreContract {

    public static final String PATH_ITEM_SUPPLIER_INFO = "salesinfo";

    public static final String PATH_ITEM_SUPPLIER_INVENTORY = "salesinventory";

    private SalesContract() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    public interface ProductSupplierColumns extends BaseColumns {

        String COLUMN_ITEM_ID = "item_id";

        String COLUMN_SUPPLIER_ID = "supplier_id";
    }

    public static final class ProductSupplierInfo implements ProductSupplierColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEM_SUPPLIER_INFO);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM_SUPPLIER_INFO;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM_SUPPLIER_INFO;


        public static final Uri CONTENT_URI_SUPPLIER_ITEMS = Uri.withAppendedPath(CONTENT_URI, SupplierContract.PATH_SUPPLIER);

        public static final String CONTENT_LIST_TYPE_SUPPLIER_ITEMS = CONTENT_LIST_TYPE + "." + SupplierContract.PATH_SUPPLIER;


        public static final Uri CONTENT_URI_ITEM_SUPPLIERS = Uri.withAppendedPath(CONTENT_URI, ProductContract.PATH_ITEM);

        public static final String CONTENT_LIST_TYPE_ITEM_SUPPLIERS = CONTENT_LIST_TYPE + "." + ProductContract.PATH_ITEM;

        public static final String TABLE_NAME = "item_supplier_info";

        public static final String COLUMN_ITEM_UNIT_PRICE = "unit_price";
        public static final float DEFAULT_ITEM_UNIT_PRICE = 0.0f;

        @NonNull
        public static String getQualifiedColumnName(String columnNameStr) {
            return TextUtils.concat(TABLE_NAME, ".", columnNameStr).toString();
        }
    }

    public static final class ProductSupplierInventory implements ProductSupplierColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEM_SUPPLIER_INVENTORY);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM_SUPPLIER_INVENTORY;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM_SUPPLIER_INVENTORY;

        public static final Uri CONTENT_URI_INV_SUPPLIER = Uri.withAppendedPath(CONTENT_URI, SupplierContract.PATH_SUPPLIER);

        public static final String CONTENT_LIST_TYPE_INV_SUPPLIER = CONTENT_LIST_TYPE + "." + SupplierContract.PATH_SUPPLIER;

        public static final Uri CONTENT_URI_INV_ITEM = Uri.withAppendedPath(CONTENT_URI, ProductContract.PATH_ITEM);

        public static final String CONTENT_LIST_TYPE_INV_ITEM = CONTENT_LIST_TYPE + "." + ProductContract.PATH_ITEM;

        public static final String PATH_SHORT_INFO = "short";

        public static final Uri CONTENT_URI_SHORT_INFO = Uri.withAppendedPath(CONTENT_URI, PATH_SHORT_INFO);

        public static final String CONTENT_LIST_TYPE_SHORT_INFO = CONTENT_LIST_TYPE + "." + PATH_SHORT_INFO;
        public static final String TABLE_NAME = "item_supplier_inventory";

        public static final String COLUMN_ITEM_AVAIL_QUANTITY = "available_quantity";

        public static final int DEFAULT_ITEM_AVAIL_QUANTITY = 0;

        @NonNull
        public static String getQualifiedColumnName(String columnNameStr) {
            return TextUtils.concat(TABLE_NAME, ".", columnNameStr).toString();
        }
    }
}
