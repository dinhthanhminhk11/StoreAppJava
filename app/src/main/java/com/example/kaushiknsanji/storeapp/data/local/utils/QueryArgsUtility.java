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

package com.example.kaushiknsanji.storeapp.data.local.utils;

import android.content.ContentUris;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract.Product;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract.ProductCategory;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract.ProductSupplierInfo;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract.ProductSupplierInventory;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract.Supplier;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract.SupplierContact;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract.SupplierContactType;

import java.util.HashMap;
import java.util.Map;

import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.AND;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.AS;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.CLOSE_BRACE;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.COUNT;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.DESC;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.EQUALS;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.IS;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.JOIN;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.LEFT_JOIN;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.NULL;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.ON;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.OPEN_BRACE;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.OR;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.PLACEHOLDER;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.SUM;


public final class QueryArgsUtility {

    private QueryArgsUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }


    public static final class ItemAttributesQuery {

        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_ITEM_ID_INDEX = 0;
        public static final int COLUMN_ITEM_ATTR_NAME_INDEX = 1;
        public static final int COLUMN_ITEM_ATTR_VALUE_INDEX = 2;

        public static void setTables(SQLiteQueryBuilder queryBuilder) {
            String inTables = Product.TABLE_NAME + JOIN
                    + ProductAttribute.TABLE_NAME + ON
                    + Product.getQualifiedColumnName(Product._ID)
                    + EQUALS
                    + ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ID);
            queryBuilder.setTables(inTables);
        }

        public static void setProjectionMap(SQLiteQueryBuilder queryBuilder) {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ID),
                    ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ID));
            columnMap.put(ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ATTR_NAME),
                    ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ATTR_NAME));
            columnMap.put(ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ATTR_VALUE),
                    ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ATTR_VALUE));
            queryBuilder.setProjectionMap(columnMap);
        }

        public static String[] getProjection() {
            return new String[]{
                    ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ID),
                    ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ATTR_NAME),
                    ProductAttribute.getQualifiedColumnName(ProductAttribute.COLUMN_ITEM_ATTR_VALUE)
            };
        }

        public static String getSelection() {
            //Where clause is the 'Item' table's Key
            return Product.getQualifiedColumnName(Product._ID) + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the 'id' passed in the URI
                    String.valueOf(ContentUris.parseId(uri))
            };
        }

    }

    public static final class ItemImagesQuery {

        public static final int COLUMN_ITEM_ID_INDEX = 0;
        public static final int COLUMN_ITEM_IMAGE_URI_INDEX = 1;
        public static final int COLUMN_ITEM_IMAGE_DEFAULT_INDEX = 2;

        public static void setTables(SQLiteQueryBuilder queryBuilder) {
            String inTables = Product.TABLE_NAME + JOIN
                    + ProductImage.TABLE_NAME + ON
                    + ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_ID)
                    + EQUALS
                    + Product.getQualifiedColumnName(Product._ID);
            queryBuilder.setTables(inTables);
        }

        public static void setProjectionMap(SQLiteQueryBuilder queryBuilder) {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_ID),
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_ID));
            columnMap.put(ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI),
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI));
            columnMap.put(ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_DEFAULT),
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_DEFAULT));
            queryBuilder.setProjectionMap(columnMap);
        }

        public static String[] getProjection() {
            return new String[]{
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_ID),
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI),
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_DEFAULT)
            };
        }

        public static String getSelection() {
            //Where clause is the 'Item' table's Key
            return Product.getQualifiedColumnName(Product._ID) + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the 'id' passed in the URI
                    String.valueOf(ContentUris.parseId(uri))
            };
        }
    }

    public static final class CategoriesQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_ITEM_CATEGORY_NAME_INDEX = 0;

        public static String[] getProjection() {
            return new String[]{
                    ProductCategory.COLUMN_ITEM_CATEGORY_NAME
            };
        }
    }


    public static final class CategoryByNameQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_ITEM_CATEGORY_ID_INDEX = 0;
        public static final int COLUMN_ITEM_CATEGORY_NAME_INDEX = 1;

        public static String[] getProjection() {
            return new String[]{
                    ProductCategory._ID,
                    ProductCategory.COLUMN_ITEM_CATEGORY_NAME
            };
        }

        public static String getSelection() {
            //Where clause is only the 'item_category' table's category_name column
            return ProductCategory.COLUMN_ITEM_CATEGORY_NAME + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the 'category_name' passed in the URI
                    uri.getLastPathSegment()
            };
        }
    }

    public static final class CategoryByIdQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_ITEM_CATEGORY_ID_INDEX = 0;
        public static final int COLUMN_ITEM_CATEGORY_NAME_INDEX = 1;

        public static String[] getProjection() {
            return new String[]{
                    ProductCategory._ID,
                    ProductCategory.COLUMN_ITEM_CATEGORY_NAME
            };
        }

        public static String getSelection() {
            //Where clause is only the 'item_category' table's _id column
            return ProductCategory._ID + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the '_id' passed in the URI
                    String.valueOf(ContentUris.parseId(uri))
            };
        }
    }

    public static final class ItemByIdQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_ITEM_ID_INDEX = 0;
        public static final int COLUMN_ITEM_NAME_INDEX = 1;
        public static final int COLUMN_ITEM_SKU_INDEX = 2;
        public static final int COLUMN_ITEM_DESCRIPTION_INDEX = 3;
        public static final int COLUMN_ITEM_CATEGORY_NAME_INDEX = 4;

        public static void setTables(SQLiteQueryBuilder queryBuilder) {
            String inTables = Product.TABLE_NAME + JOIN
                    + ProductCategory.TABLE_NAME + ON
                    + Product.getQualifiedColumnName(Product.COLUMN_ITEM_CATEGORY_ID)
                    + EQUALS
                    + ProductCategory.getQualifiedColumnName(ProductCategory._ID);
            queryBuilder.setTables(inTables);
        }

        public static void setProjectionMap(SQLiteQueryBuilder queryBuilder) {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(Product.getQualifiedColumnName(Product._ID),
                    Product.getQualifiedColumnName(Product._ID));
            columnMap.put(Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME));
            columnMap.put(Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU));
            columnMap.put(Product.getQualifiedColumnName(Product.COLUMN_ITEM_DESCRIPTION),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_DESCRIPTION));
            columnMap.put(ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME),
                    ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME));
            queryBuilder.setProjectionMap(columnMap);
        }

        public static String[] getProjection() {
            return new String[]{
                    Product.getQualifiedColumnName(Product._ID),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_DESCRIPTION),
                    ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME)
            };
        }

        public static String getSelection() {
            //Where clause is the 'Item' table's Key
            return Product.getQualifiedColumnName(Product._ID) + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    String.valueOf(ContentUris.parseId(uri))
            };
        }
    }

    public static final class ItemsShortInfoQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_ITEM_ID_INDEX = 0;
        public static final int COLUMN_ITEM_NAME_INDEX = 1;
        public static final int COLUMN_ITEM_SKU_INDEX = 2;
        public static final int COLUMN_ITEM_CATEGORY_NAME_INDEX = 3;
        public static final int COLUMN_ITEM_IMAGE_URI_INDEX = 4;

        public static void setTables(SQLiteQueryBuilder queryBuilder) {
            String inTables = Product.TABLE_NAME + JOIN
                    + ProductCategory.TABLE_NAME + ON
                    + Product.getQualifiedColumnName(Product.COLUMN_ITEM_CATEGORY_ID)
                    + EQUALS
                    + ProductCategory.getQualifiedColumnName(ProductCategory._ID)
                    + LEFT_JOIN
                    + ProductImage.TABLE_NAME + ON
                    + Product.getQualifiedColumnName(Product._ID)
                    + EQUALS
                    + ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_ID);
            queryBuilder.setTables(inTables);
        }

        public static void setProjectionMap(SQLiteQueryBuilder queryBuilder) {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(Product.getQualifiedColumnName(Product._ID),
                    Product.getQualifiedColumnName(Product._ID));
            columnMap.put(Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME));
            columnMap.put(Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU));
            columnMap.put(ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME),
                    ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME));
            columnMap.put(ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI),
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI));
            queryBuilder.setProjectionMap(columnMap);
        }

        public static String[] getProjection() {
            return new String[]{
                    Product.getQualifiedColumnName(Product._ID),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU),
                    ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME),
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI)
            };
        }

        public static String getSelection() {
            //Where clause is the 'item_image' table's 'is_default' column
            //(item_image.is_default is null or item_image.is_default = 1)
            return ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_DEFAULT) + IS + NULL +
                    OR + ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_DEFAULT) + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs() {
            return new String[]{
                    //Where clause value is '1' that denotes the default image of the item
                    String.valueOf(ProductImage.ITEM_IMAGE_DEFAULT)
            };
        }

    }

    public static final class ItemBySkuQuery {
        public static final int COLUMN_ITEM_ID_INDEX = 0;
        public static final int COLUMN_ITEM_NAME_INDEX = 1;
        public static final int COLUMN_ITEM_SKU_INDEX = 2;

        public static String[] getProjection() {
            return new String[]{
                    Product._ID,
                    Product.COLUMN_ITEM_NAME,
                    Product.COLUMN_ITEM_SKU
            };
        }

        public static String getSelection() {
            //Where clause is only the 'item' table's item_sku column
            return Product.COLUMN_ITEM_SKU + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the 'item_sku' passed in the URI
                    uri.getLastPathSegment()
            };
        }
    }

    public static final class SupplierByIdQuery {
        public static final int COLUMN_SUPPLIER_ID_INDEX = 0;
        public static final int COLUMN_SUPPLIER_NAME_INDEX = 1;
        public static final int COLUMN_SUPPLIER_CODE_INDEX = 2;


        public static String[] getProjection() {
            return new String[]{
                    Supplier._ID,
                    Supplier.COLUMN_SUPPLIER_NAME,
                    Supplier.COLUMN_SUPPLIER_CODE
            };
        }


        public static String getSelection() {
            return Supplier._ID + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the '_id' passed in the URI
                    String.valueOf(ContentUris.parseId(uri))
            };
        }
    }

    public static final class SupplierContactsQuery {
        public static final int COLUMN_SUPPLIER_ID_INDEX = 0;
        public static final int COLUMN_SUPPLIER_CONTACT_TYPE_ID_INDEX = 1;
        public static final int COLUMN_SUPPLIER_CONTACT_VALUE_INDEX = 2;
        public static final int COLUMN_SUPPLIER_CONTACT_DEFAULT_INDEX = 3;

        public static void setTables(SQLiteQueryBuilder queryBuilder) {
            String inTables = Supplier.TABLE_NAME + JOIN
                    + SupplierContact.TABLE_NAME + ON
                    + SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_ID)
                    + EQUALS
                    + Supplier.getQualifiedColumnName(Supplier._ID);
            queryBuilder.setTables(inTables);
        }

        public static void setProjectionMap(SQLiteQueryBuilder queryBuilder) {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(Supplier.getQualifiedColumnName(Supplier._ID),
                    Supplier.getQualifiedColumnName(Supplier._ID));
            columnMap.put(SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_TYPE_ID),
                    SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_TYPE_ID));
            columnMap.put(SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE),
                    SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE));
            columnMap.put(SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_DEFAULT),
                    SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_DEFAULT));
            queryBuilder.setProjectionMap(columnMap);
        }


        public static String[] getProjection() {
            return new String[]{
                    Supplier.getQualifiedColumnName(Supplier._ID),
                    SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_TYPE_ID),
                    SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE),
                    SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_DEFAULT)
            };
        }


        public static String getSelection() {
            return Supplier.getQualifiedColumnName(Supplier._ID) + EQUALS + PLACEHOLDER;
        }


        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the '_id' passed in the URI
                    String.valueOf(ContentUris.parseId(uri))
            };
        }
    }

    public static final class SupplierItemsQuery {
        public static final int COLUMN_SUPPLIER_ID_INDEX = 0;
        public static final int COLUMN_ITEM_ID_INDEX = 1;
        public static final int COLUMN_ITEM_UNIT_PRICE_INDEX = 2;


        public static String[] getProjection() {
            return new String[]{
                    ProductSupplierInfo.COLUMN_SUPPLIER_ID,
                    ProductSupplierInfo.COLUMN_ITEM_ID,
                    ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE
            };
        }


        public static String getSelection() {
            //Where clause is only the 'item_supplier_info' table's supplier_id column
            return ProductSupplierInfo.COLUMN_SUPPLIER_ID + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the 'supplier_id' passed in the URI
                    String.valueOf(ContentUris.parseId(uri))
            };
        }
    }


    public static final class ItemSuppliersQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_SUPPLIER_ID_INDEX = 0;
        public static final int COLUMN_ITEM_ID_INDEX = 1;
        public static final int COLUMN_ITEM_UNIT_PRICE_INDEX = 2;


        public static String[] getProjection() {
            return new String[]{
                    ProductSupplierInfo.COLUMN_SUPPLIER_ID,
                    ProductSupplierInfo.COLUMN_ITEM_ID,
                    ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE
            };
        }


        public static String getSelection() {
            //Where clause is only the 'item_supplier_info' table's item_id column
            return ProductSupplierInfo.COLUMN_ITEM_ID + EQUALS + PLACEHOLDER;
        }

        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the 'item_id' passed in the URI
                    String.valueOf(ContentUris.parseId(uri))
            };
        }
    }

    public static final class SupplierByCodeQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_SUPPLIER_ID_INDEX = 0;
        public static final int COLUMN_SUPPLIER_NAME_INDEX = 1;
        public static final int COLUMN_SUPPLIER_CODE_INDEX = 2;


        public static String[] getProjection() {
            return new String[]{
                    Supplier._ID,
                    Supplier.COLUMN_SUPPLIER_NAME,
                    Supplier.COLUMN_SUPPLIER_CODE
            };
        }


        public static String getSelection() {
            //Where clause is only the 'supplier' table's 'supplier_code' column
            return Supplier.COLUMN_SUPPLIER_CODE + EQUALS + PLACEHOLDER;
        }


        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    //Where clause value is the 'supplier_code' passed in the URI
                    uri.getLastPathSegment()
            };
        }
    }


    public static final class SuppliersShortInfoQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_SUPPLIER_ID_INDEX = 0;
        public static final int COLUMN_SUPPLIER_NAME_INDEX = 1;
        public static final int COLUMN_SUPPLIER_CODE_INDEX = 2;
        public static final int COLUMN_SUPPLIER_DEFAULT_PHONE_INDEX = 3;
        public static final int COLUMN_SUPPLIER_DEFAULT_EMAIL_INDEX = 4;
        public static final int COLUMN_SUPPLIER_ITEM_COUNT_INDEX = 5;
        //Column Name constants for the custom columns
        private static final String COLUMN_SUPPLIER_ITEM_COUNT = "item_count";
        private static final String COLUMN_SUPPLIER_DEFAULT_PHONE = "default_phone";
        private static final String COLUMN_SUPPLIER_DEFAULT_EMAIL = "default_email";

        private static String getDefaultContactSubQuery(int contactTypeId) {
            //Initializing the QueryBuilder
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

            //Constructing the Projection Map
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE),
                    SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE));
            queryBuilder.setProjectionMap(columnMap);

            //Setting the Projection
            String[] projection = new String[]{
                    SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE)
            };

            //Constructing the relationship between Tables involved
            String inTables = SupplierContact.TABLE_NAME + JOIN
                    + SupplierContactType.TABLE_NAME + ON
                    + SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_TYPE_ID)
                    + EQUALS
                    + SupplierContactType.getQualifiedColumnName(SupplierContactType._ID);
            queryBuilder.setTables(inTables);

            //Constructing the WHERE Clause
            String selection = SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_ID)
                    + EQUALS + Supplier.getQualifiedColumnName(Supplier._ID) + AND
                    + SupplierContactType.getQualifiedColumnName(SupplierContactType._ID)
                    + EQUALS + contactTypeId + AND
                    + SupplierContact.getQualifiedColumnName(SupplierContact.COLUMN_SUPPLIER_CONTACT_DEFAULT)
                    + EQUALS + SupplierContact.SUPPLIER_CONTACT_DEFAULT;

            //Returning the Sub Query built
            return queryBuilder.buildQuery(projection, selection, null, null, null, null);
        }


        private static String getItemCountSubQuery() {
            //Initializing the QueryBuilder
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

            //Constructing the Projection Map
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(COLUMN_SUPPLIER_ITEM_COUNT, COUNT + OPEN_BRACE + Product.getQualifiedColumnName(Product._ID) + CLOSE_BRACE + AS + COLUMN_SUPPLIER_ITEM_COUNT);
            queryBuilder.setProjectionMap(columnMap);

            //Setting the Projection
            String[] projection = new String[]{
                    COLUMN_SUPPLIER_ITEM_COUNT
            };

            //Constructing the relationship between Tables involved
            String inTables = Product.TABLE_NAME + JOIN
                    + ProductSupplierInfo.TABLE_NAME + ON
                    + ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_ID)
                    + EQUALS
                    + Product.getQualifiedColumnName(Product._ID);
            queryBuilder.setTables(inTables);

            //Constructing the WHERE Clause
            String selection = ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_SUPPLIER_ID)
                    + EQUALS + Supplier.getQualifiedColumnName(Supplier._ID);

            //Returning the Sub Query built
            return queryBuilder.buildQuery(projection, selection, null, null, null, null);
        }


        public static void setTables(SQLiteQueryBuilder queryBuilder) {
            String inTables = Supplier.TABLE_NAME;
            queryBuilder.setTables(inTables);
        }

        public static void setProjectionMap(SQLiteQueryBuilder queryBuilder) {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(Supplier.getQualifiedColumnName(Supplier._ID), Supplier.getQualifiedColumnName(Supplier._ID));
            columnMap.put(Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME), Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME));
            columnMap.put(Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE), Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE));
            columnMap.put(COLUMN_SUPPLIER_DEFAULT_PHONE, OPEN_BRACE + getDefaultContactSubQuery(SupplierContactType.CONTACT_TYPE_ID_PHONE) + CLOSE_BRACE + AS + COLUMN_SUPPLIER_DEFAULT_PHONE);
            columnMap.put(COLUMN_SUPPLIER_DEFAULT_EMAIL, OPEN_BRACE + getDefaultContactSubQuery(SupplierContactType.CONTACT_TYPE_ID_EMAIL) + CLOSE_BRACE + AS + COLUMN_SUPPLIER_DEFAULT_EMAIL);
            columnMap.put(COLUMN_SUPPLIER_ITEM_COUNT, OPEN_BRACE + getItemCountSubQuery() + CLOSE_BRACE + AS + COLUMN_SUPPLIER_ITEM_COUNT);
            queryBuilder.setProjectionMap(columnMap);
        }


        public static String[] getProjection() {
            return new String[]{
                    Supplier.getQualifiedColumnName(Supplier._ID),
                    Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME),
                    Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE),
                    COLUMN_SUPPLIER_DEFAULT_PHONE,
                    COLUMN_SUPPLIER_DEFAULT_EMAIL,
                    COLUMN_SUPPLIER_ITEM_COUNT
            };
        }
    }

    public static final class SalesShortInfoQuery {
        public static final int COLUMN_ITEM_ID_INDEX = 0;
        public static final int COLUMN_SUPPLIER_ID_INDEX = 1;
        public static final int COLUMN_ITEM_NAME_INDEX = 2;
        public static final int COLUMN_ITEM_SKU_INDEX = 3;
        public static final int COLUMN_ITEM_CATEGORY_NAME_INDEX = 4;
        public static final int COLUMN_ITEM_IMAGE_URI_INDEX = 5;
        public static final int COLUMN_SUPPLIER_NAME_INDEX = 6;
        public static final int COLUMN_SUPPLIER_CODE_INDEX = 7;
        public static final int COLUMN_ITEM_UNIT_PRICE_INDEX = 8;
        public static final int COLUMN_SUPPLIER_AVAIL_QUANTITY_INDEX = 9;
        public static final int COLUMN_TOTAL_AVAIL_QUANTITY_INDEX = 10;
        //Column Name constants for the custom columns
        private static final String COLUMN_SUPPLIER_AVAIL_QUANTITY = "supplier_available_quantity";
        private static final String COLUMN_TOTAL_AVAIL_QUANTITY = "total_available_quantity";


        private static String getTotalAvailQuantitySubQuery() {
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(COLUMN_TOTAL_AVAIL_QUANTITY, SUM + OPEN_BRACE + ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY + CLOSE_BRACE + AS + COLUMN_TOTAL_AVAIL_QUANTITY);
            queryBuilder.setProjectionMap(columnMap);

            String[] projection = new String[]{
                    COLUMN_TOTAL_AVAIL_QUANTITY
            };

            String inTables = ProductSupplierInventory.TABLE_NAME;
            queryBuilder.setTables(inTables);

            String selection = ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_ID)
                    + EQUALS + Product.getQualifiedColumnName(Product._ID);

            return queryBuilder.buildQuery(projection, selection, null, null, null, null);
        }


        private static String getTopSupplierIdSubQuery() {
            //Initializing the QueryBuilder
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

            //Constructing the Projection Map
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(Supplier.getQualifiedColumnName(Supplier._ID), Supplier.getQualifiedColumnName(Supplier._ID));
            queryBuilder.setProjectionMap(columnMap);

            String[] projection = new String[]{
                    Supplier.getQualifiedColumnName(Supplier._ID)
            };

            String inTables = Supplier.TABLE_NAME + JOIN
                    + ProductSupplierInventory.TABLE_NAME + ON
                    + ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_SUPPLIER_ID)
                    + EQUALS
                    + Supplier.getQualifiedColumnName(Supplier._ID);
            queryBuilder.setTables(inTables);

            String selection = ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_ID)
                    + EQUALS + Product.getQualifiedColumnName(Product._ID);

            return queryBuilder.buildQuery(projection, selection, null, null,
                    ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY) + DESC,
                    "1"
            );
        }

        public static void setTables(SQLiteQueryBuilder queryBuilder) {
            String inTables = Product.TABLE_NAME + JOIN
                    + ProductCategory.TABLE_NAME + ON
                    + Product.getQualifiedColumnName(Product.COLUMN_ITEM_CATEGORY_ID)
                    + EQUALS + ProductCategory.getQualifiedColumnName(ProductCategory._ID)
                    + LEFT_JOIN + ProductImage.TABLE_NAME + ON
                    + ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_ID)
                    + EQUALS + Product.getQualifiedColumnName(Product._ID)
                    + JOIN + ProductSupplierInfo.TABLE_NAME + ON
                    + ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_ID)
                    + EQUALS + Product.getQualifiedColumnName(Product._ID)
                    + JOIN + ProductSupplierInventory.TABLE_NAME + ON
                    + ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_ID)
                    + EQUALS + Product.getQualifiedColumnName(Product._ID)
                    + JOIN + Supplier.TABLE_NAME + ON
                    + Supplier.getQualifiedColumnName(Supplier._ID)
                    + EQUALS + ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_SUPPLIER_ID);
            queryBuilder.setTables(inTables);
        }


        public static void setProjectionMap(SQLiteQueryBuilder queryBuilder) {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_ID), ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_ID));
            columnMap.put(ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_SUPPLIER_ID), ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_SUPPLIER_ID));
            columnMap.put(Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME), Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME));
            columnMap.put(Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU), Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU));
            columnMap.put(ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME), ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME));
            columnMap.put(ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI), ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI));
            columnMap.put(Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME), Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME));
            columnMap.put(Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE), Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE));
            columnMap.put(ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE), ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE));
            columnMap.put(COLUMN_SUPPLIER_AVAIL_QUANTITY, ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY) + AS + COLUMN_SUPPLIER_AVAIL_QUANTITY);
            columnMap.put(COLUMN_TOTAL_AVAIL_QUANTITY, OPEN_BRACE + getTotalAvailQuantitySubQuery() + CLOSE_BRACE + AS + COLUMN_TOTAL_AVAIL_QUANTITY);
            queryBuilder.setProjectionMap(columnMap);
        }


        public static String[] getProjection() {
            return new String[]{
                    ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_ID),
                    ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_SUPPLIER_ID),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_NAME),
                    Product.getQualifiedColumnName(Product.COLUMN_ITEM_SKU),
                    ProductCategory.getQualifiedColumnName(ProductCategory.COLUMN_ITEM_CATEGORY_NAME),
                    ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_URI),
                    Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME),
                    Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE),
                    ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE),
                    COLUMN_SUPPLIER_AVAIL_QUANTITY,
                    COLUMN_TOTAL_AVAIL_QUANTITY
            };
        }


        public static String getSelection() {
            return OPEN_BRACE + ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_DEFAULT) + IS + NULL +
                    OR + ProductImage.getQualifiedColumnName(ProductImage.COLUMN_ITEM_IMAGE_DEFAULT) + EQUALS + PLACEHOLDER + CLOSE_BRACE +
                    AND + ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_SUPPLIER_ID) + EQUALS + OPEN_BRACE + getTopSupplierIdSubQuery() + CLOSE_BRACE +
                    AND + ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_SUPPLIER_ID) +
                    EQUALS + ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_SUPPLIER_ID);
        }

        public static String[] getSelectionArgs() {
            return new String[]{
                    //'1' that denotes the default image of the item
                    String.valueOf(ProductImage.ITEM_IMAGE_DEFAULT)
            };
        }
    }


    public static final class ItemSuppliersSalesQuery {
        //Constants of Column Index as they would appear in the Select clause
        public static final int COLUMN_ITEM_ID_INDEX = 0;
        public static final int COLUMN_SUPPLIER_ID_INDEX = 1;
        public static final int COLUMN_SUPPLIER_NAME_INDEX = 2;
        public static final int COLUMN_SUPPLIER_CODE_INDEX = 3;
        public static final int COLUMN_ITEM_UNIT_PRICE_INDEX = 4;
        public static final int COLUMN_AVAIL_QUANTITY_INDEX = 5;

        public static void setTables(SQLiteQueryBuilder queryBuilder) {
            String inTables = Supplier.TABLE_NAME
                    + JOIN + ProductSupplierInfo.TABLE_NAME + ON
                    + ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_SUPPLIER_ID)
                    + EQUALS + Supplier.getQualifiedColumnName(Supplier._ID)
                    + JOIN + ProductSupplierInventory.TABLE_NAME + ON
                    + ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_SUPPLIER_ID)
                    + EQUALS + ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_SUPPLIER_ID);
            queryBuilder.setTables(inTables);
        }


        public static void setProjectionMap(SQLiteQueryBuilder queryBuilder) {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_ID),
                    ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_ID));
            columnMap.put(ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_SUPPLIER_ID),
                    ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_SUPPLIER_ID));
            columnMap.put(Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME),
                    Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME));
            columnMap.put(Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE),
                    Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE));
            columnMap.put(ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE),
                    ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE));
            columnMap.put(ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY),
                    ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY));
            queryBuilder.setProjectionMap(columnMap);
        }


        public static String[] getProjection() {
            return new String[]{
                    ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_ID),
                    ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_SUPPLIER_ID),
                    Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_NAME),
                    Supplier.getQualifiedColumnName(Supplier.COLUMN_SUPPLIER_CODE),
                    ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE),
                    ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY)
            };
        }


        public static String getSelection() {
            return ProductSupplierInventory.getQualifiedColumnName(ProductSupplierInventory.COLUMN_ITEM_ID)
                    + EQUALS + ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_ID)
                    + AND + ProductSupplierInfo.getQualifiedColumnName(ProductSupplierInfo.COLUMN_ITEM_ID)
                    + EQUALS + PLACEHOLDER;
        }


        public static String[] getSelectionArgs(@NonNull Uri uri) {
            return new String[]{
                    String.valueOf(ContentUris.parseId(uri))
            };
        }
    }
}