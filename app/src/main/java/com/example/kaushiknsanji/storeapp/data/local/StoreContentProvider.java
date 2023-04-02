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

package com.example.kaushiknsanji.storeapp.data.local;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.StoreContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.utils.QueryArgsUtility;

import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.AND;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.EQUALS;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.PLACEHOLDER;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.combineSelectionPairs;


public class StoreContentProvider extends ContentProvider {

    //Constant Used for Logs
    private static final String LOG_TAG = StoreContentProvider.class.getSimpleName();
    //URI Matcher codes for identifying the URI of Item and its descendant relationships
    private static final int ITEMS = 10;
    private static final int ITEM_SHORT_INFO = 11;
    private static final int ITEM_ID = 12;
    private static final int ITEM_BY_SKU = 13;
    private static final int ITEM_ATTRS_ID = 14;
    private static final int ITEM_IMAGES_ID = 15;
    //URI Matcher codes for identifying the URI of Category and its descendant relationships
    private static final int CATEGORIES = 20;
    private static final int CATEGORY_BY_ID = 21;
    private static final int CATEGORY_BY_NAME = 22;
    //URI Matcher codes for identifying the URI of Supplier and its descendant relationships
    private static final int SUPPLIERS = 30;
    private static final int SUPPLIER_SHORT_INFO = 31;
    private static final int SUPPLIER_ID = 32;
    private static final int SUPPLIER_BY_CODE = 33;
    private static final int SUPPLIER_CONTACTS_ID = 34;
    //URI Matcher codes for identifying the URI of ProductSupplierInfo and its descendant relationships
    private static final int SUPPLIER_ITEMS_ID = 41;
    private static final int ITEM_SUPPLIERS_ID = 42;
    //URI Matcher codes for identifying the URI of ProductSupplierInventory and its descendant relationships
    private static final int SALES_SHORT_INFO = 50;
    private static final int SALES_INVENTORY_ITEM_ID = 51;
    private static final int SALES_INVENTORY_SUPPLIER_ID = 52;
    //URI Matcher for matching the possible URI
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    //Stores the instance of SQLiteOpenHelper
    private StoreDbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        //Constructs an empty UriMatcher for the root node
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //For "content://AUTHORITY/item" URI that references the entire 'item' table
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_ITEM, ITEMS);

        //For "content://AUTHORITY/item/short" URI that references
        //the entire 'item' table with relationship data
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_ITEM + "/" + ProductContract.Product.PATH_SHORT_INFO,
                ITEM_SHORT_INFO);

        //For "content://AUTHORITY/item/#" URI that references a record in 'item' table
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_ITEM + "/#", ITEM_ID);

        //For "content://AUTHORITY/item/sku/*" URI that references a record in 'item' table
        //identified by 'item_sku'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_ITEM + "/" + ProductContract.Product.PATH_ITEM_SKU + "/*",
                ITEM_BY_SKU);

        //For "content://AUTHORITY/item/attr/#" URI that references a set of records in 'item_attr' table
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_ITEM + "/" + ProductContract.PATH_ITEM_ATTR + "/#",
                ITEM_ATTRS_ID);

        //For "content://AUTHORITY/item/image/#" URI that references a set of records in 'item_image' table
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_ITEM + "/" + ProductContract.PATH_ITEM_IMAGE + "/#",
                ITEM_IMAGES_ID);

        //For "content://AUTHORITY/category" URI that references the entire 'item_category' table
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_CATEGORY, CATEGORIES);

        //For "content://AUTHORITY/category/#" URI that references a record in 'item_category' table
        //identified by '_id'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_CATEGORY + "/#", CATEGORY_BY_ID);

        //For "content://AUTHORITY/category/*" URI that references a record in 'item_category' table
        //identified by 'category_name'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                ProductContract.PATH_CATEGORY + "/*", CATEGORY_BY_NAME);

        //For "content://AUTHORITY/supplier" URI that references the entire 'supplier' table
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SupplierContract.PATH_SUPPLIER, SUPPLIERS);

        //For "content://AUTHORITY/supplier/short" URI that references
        //the entire 'supplier' table with relationship data
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SupplierContract.PATH_SUPPLIER + "/" + SupplierContract.Supplier.PATH_SHORT_INFO,
                SUPPLIER_SHORT_INFO);

        //For "content://AUTHORITY/supplier/#" URI that references a record in 'supplier' table
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SupplierContract.PATH_SUPPLIER + "/#", SUPPLIER_ID);

        //For "content://AUTHORITY/supplier/code/*" URI that references a record in 'supplier' table
        //identified by 'supplier_code'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SupplierContract.PATH_SUPPLIER + "/" + SupplierContract.Supplier.PATH_SUPPLIER_CODE + "/*",
                SUPPLIER_BY_CODE);

        //For "content://AUTHORITY/supplier/contact/#" URI that references a set of records in 'supplier_contact' table
        //identified by 'supplier_id'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SupplierContract.PATH_SUPPLIER + "/" + SupplierContract.PATH_SUPPLIER_CONTACT + "/#",
                SUPPLIER_CONTACTS_ID);

        //For "content://AUTHORITY/salesinfo/supplier/#" URI that references a set of records in 'item_supplier_info' table
        //identified by 'supplier_id'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SalesContract.PATH_ITEM_SUPPLIER_INFO + "/" + SupplierContract.PATH_SUPPLIER + "/#",
                SUPPLIER_ITEMS_ID);

        //For "content://AUTHORITY/salesinfo/item/#" URI that references a set of records in 'item_supplier_info' table
        //identified by 'item_id'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SalesContract.PATH_ITEM_SUPPLIER_INFO + "/" + ProductContract.PATH_ITEM + "/#",
                ITEM_SUPPLIERS_ID);

        //For "content://AUTHORITY/salesinventory/short" URI that references
        //the entire 'item_supplier_inventory' table with relationship data
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SalesContract.PATH_ITEM_SUPPLIER_INVENTORY + "/" + SalesContract.ProductSupplierInventory.PATH_SHORT_INFO,
                SALES_SHORT_INFO);

        //For "content://AUTHORITY/salesinventory/item/#" URI that references a set of records in 'item_supplier_inventory' table
        //identified by 'item_id'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SalesContract.PATH_ITEM_SUPPLIER_INVENTORY + "/" + ProductContract.PATH_ITEM + "/#",
                SALES_INVENTORY_ITEM_ID);

        //For "content://AUTHORITY/salesinventory/supplier/#" URI that references a set of records in 'item_supplier_inventory' table
        //identified by 'supplier_id'
        matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                SalesContract.PATH_ITEM_SUPPLIER_INVENTORY + "/" + SupplierContract.PATH_SUPPLIER + "/#",
                SALES_INVENTORY_SUPPLIER_ID);

        //Returning the URI Matcher prepared
        return matcher;
    }


    @Override
    public boolean onCreate() {
        //Initializing the database helper object
        mDbHelper = StoreDbHelper.getInstance(getContext());
        return true; //Returning true to indicate that the provider is loaded successfully
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable Bundle queryArgs, @Nullable CancellationSignal cancellationSignal) {
        //Propagating the call to super (This is required for Android O and above)
        return super.query(uri, projection, queryArgs, cancellationSignal);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //Retrieving the database in read mode
        SQLiteDatabase readableDatabase = mDbHelper.getReadableDatabase();

        //Declaring the cursor that will hold the result of the query
        Cursor retCursor;

        //Matching the queried Uri to execute the correct query
        switch (sUriMatcher.match(uri)) {
            case ITEM_ATTRS_ID:
                //For Item's Attributes, identified by the item '_id'
                retCursor = getItemAttributes(uri, readableDatabase, projection);
                break;
            case ITEM_IMAGES_ID:
                //For Item's Images, identified by the item '_id'
                retCursor = getItemImages(uri, readableDatabase, projection);
                break;
            case ITEM_ID:
                //For an Item identified by its '_id'
                retCursor = getItemDetails(uri, readableDatabase, projection);
                break;
            case ITEM_BY_SKU:
                //For an Item identified by its 'item_sku'
                retCursor = getItemBySku(uri, readableDatabase, projection);
                break;
            case ITEM_SHORT_INFO:
                //For Items with Short relationship info
                retCursor = getItemsWithShortInfo(readableDatabase, projection, selection, selectionArgs, sortOrder);
                break;
            case CATEGORIES:
                //For all Categories
                retCursor = getCategories(readableDatabase, projection, sortOrder);
                break;
            case CATEGORY_BY_ID:
                //For a Category identified by the '_id'
                retCursor = getCategoryById(uri, readableDatabase, projection);
                break;
            case CATEGORY_BY_NAME:
                //For a Category identified by the 'category_name'
                retCursor = getCategoryByName(uri, readableDatabase, projection);
                break;
            case SUPPLIER_ID:
                //For a Supplier identified by its '_id'
                retCursor = getSupplierDetails(uri, readableDatabase, projection);
                break;
            case SUPPLIER_BY_CODE:
                //For a Supplier identified by its 'supplier_code'
                retCursor = getSupplierByCode(uri, readableDatabase, projection);
                break;
            case SUPPLIER_CONTACTS_ID:
                //For Supplier's Contacts, identified by the 'supplier_id'
                retCursor = getSupplierContacts(uri, readableDatabase, projection);
                break;
            case SUPPLIER_ITEMS_ID:
                //For Supplier's list of items, identified by the 'supplier_id'
                retCursor = getSupplierItems(uri, readableDatabase, projection);
                break;
            case ITEM_SUPPLIERS_ID:
                //For Item's list of suppliers, identified by the 'item_id'
                retCursor = getItemSuppliers(uri, readableDatabase, projection);
                break;
            case SUPPLIER_SHORT_INFO:
                //For Suppliers with Short relationship info
                retCursor = getSuppliersWithShortInfo(readableDatabase, projection, selection, selectionArgs, sortOrder);
                break;
            case SALES_SHORT_INFO:
                //For Sales Inventory with Short relationship info
                retCursor = getSalesWithShortInfo(readableDatabase, projection, selection, selectionArgs, sortOrder);
                break;
            case SALES_INVENTORY_ITEM_ID:
                //For Item's Suppliers with Inventory and Price details
                retCursor = getItemSuppliersSalesInfo(uri, readableDatabase, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Cannot query unknown URI " + uri);
        }

        //Registering the ContentResolver to watch the Content URI for any changes to notify any
        //listeners attached to the ContentResolver
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        //Returning the query result
        return retCursor;
    }

    private Cursor getItemBySku(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        return readableDatabase.query(
                ProductContract.Product.TABLE_NAME,
                projection,
                //Where Clause
                QueryArgsUtility.ItemBySkuQuery.getSelection(),
                //The value in Where Clause which is the 'item_sku' passed in the URI
                QueryArgsUtility.ItemBySkuQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getItemsWithShortInfo(SQLiteDatabase readableDatabase, String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //Instantiating a Query Builder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //Setting up the Tables involved in the query
        QueryArgsUtility.ItemsShortInfoQuery.setTables(queryBuilder);
        //Setting up the Projection Map to use
        QueryArgsUtility.ItemsShortInfoQuery.setProjectionMap(queryBuilder);
        //Building the Selection Clause and its Arguments
        Pair<String, String[]> selectionPairs = Pair.create(
                QueryArgsUtility.ItemsShortInfoQuery.getSelection(),
                QueryArgsUtility.ItemsShortInfoQuery.getSelectionArgs());

        if (!TextUtils.isEmpty(selection)) {
            //Combining the Selection clauses when Selection Clause is passed
            selectionPairs = combineSelectionPairs(
                    selectionPairs,
                    Pair.create(selection, selectionArgs),
                    AND
            );
        }

        //Querying for the content using the Query Builder
        return queryBuilder.query(
                readableDatabase,
                projection,
                //Where Clause
                selectionPairs != null ? selectionPairs.first : null,
                //The value in Where Clause
                selectionPairs != null ? selectionPairs.second : null,
                null,
                null,
                sortOrder
        );
    }


    private Cursor getItemDetails(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        //Instantiating a Query Builder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //Setting up the Tables involved in the query
        QueryArgsUtility.ItemByIdQuery.setTables(queryBuilder);
        //Setting up the Projection Map to use
        QueryArgsUtility.ItemByIdQuery.setProjectionMap(queryBuilder);

        //Querying for the content using the Query Builder
        return queryBuilder.query(
                readableDatabase,
                projection,
                //Where Clause
                QueryArgsUtility.ItemByIdQuery.getSelection(),
                //The value in Where Clause which is the '_id' passed in the URI
                QueryArgsUtility.ItemByIdQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }

    private Cursor getCategoryById(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        return readableDatabase.query(
                ProductContract.ProductCategory.TABLE_NAME,
                projection,
                //Where Clause
                QueryArgsUtility.CategoryByIdQuery.getSelection(),
                //The value in Where Clause which is the '_id' passed in the URI
                QueryArgsUtility.CategoryByIdQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getCategoryByName(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        return readableDatabase.query(
                ProductContract.ProductCategory.TABLE_NAME,
                projection,
                //Where Clause
                QueryArgsUtility.CategoryByNameQuery.getSelection(),
                //The value in Where Clause which is the 'category_name' passed in the URI
                QueryArgsUtility.CategoryByNameQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getCategories(SQLiteDatabase readableDatabase, @Nullable String[] projection, String sortOrder) {
        return readableDatabase.query(
                ProductContract.ProductCategory.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }


    private Cursor getItemImages(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        //Instantiating a Query Builder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //Setting up the Tables involved in the query
        QueryArgsUtility.ItemImagesQuery.setTables(queryBuilder);
        //Setting up the Projection Map to use
        QueryArgsUtility.ItemImagesQuery.setProjectionMap(queryBuilder);

        //Querying for the content using the Query Builder
        return queryBuilder.query(
                readableDatabase,
                projection,
                //Where Clause
                QueryArgsUtility.ItemImagesQuery.getSelection(),
                //The value in Where Clause which is the Id(key) passed in the URI
                QueryArgsUtility.ItemImagesQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getItemAttributes(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        //Instantiating a Query Builder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //Setting up the Tables involved in the query
        QueryArgsUtility.ItemAttributesQuery.setTables(queryBuilder);
        //Setting up the Projection Map to use
        QueryArgsUtility.ItemAttributesQuery.setProjectionMap(queryBuilder);

        //Querying for the content using the Query Builder
        return queryBuilder.query(
                readableDatabase,
                projection,
                //Where Clause
                QueryArgsUtility.ItemAttributesQuery.getSelection(),
                //The value in Where Clause which is the Id(key) passed in the URI
                QueryArgsUtility.ItemAttributesQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getSupplierDetails(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        return readableDatabase.query(
                SupplierContract.Supplier.TABLE_NAME,
                projection,
                //Where Clause
                QueryArgsUtility.SupplierByIdQuery.getSelection(),
                //The value in Where Clause which is the '_id' passed in the URI
                QueryArgsUtility.SupplierByIdQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getSupplierByCode(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        return readableDatabase.query(
                SupplierContract.Supplier.TABLE_NAME,
                projection,
                //Where Clause
                QueryArgsUtility.SupplierByCodeQuery.getSelection(),
                //The value in Where Clause which is the 'supplier_code' passed in the URI
                QueryArgsUtility.SupplierByCodeQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getSupplierItems(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        return readableDatabase.query(
                SalesContract.ProductSupplierInfo.TABLE_NAME,
                projection,
                //Where Clause
                QueryArgsUtility.SupplierItemsQuery.getSelection(),
                //The value in Where Clause which is the 'supplier_id' passed in the URI
                QueryArgsUtility.SupplierItemsQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getItemSuppliers(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        return readableDatabase.query(
                SalesContract.ProductSupplierInfo.TABLE_NAME,
                projection,
                //Where Clause
                QueryArgsUtility.ItemSuppliersQuery.getSelection(),
                //The value in Where Clause which is the 'item_id' passed in the URI
                QueryArgsUtility.ItemSuppliersQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getSupplierContacts(Uri uri, SQLiteDatabase readableDatabase, String[] projection) {
        //Instantiating a Query Builder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //Setting up the Tables involved in the query
        QueryArgsUtility.SupplierContactsQuery.setTables(queryBuilder);
        //Setting up the Projection Map to use
        QueryArgsUtility.SupplierContactsQuery.setProjectionMap(queryBuilder);

        //Querying for the content using the Query Builder
        return queryBuilder.query(
                readableDatabase,
                projection,
                //Where Clause
                QueryArgsUtility.SupplierContactsQuery.getSelection(),
                //The value in Where Clause which is the Supplier '_id' passed in the URI
                QueryArgsUtility.SupplierContactsQuery.getSelectionArgs(uri),
                null,
                null,
                null
        );
    }


    private Cursor getSuppliersWithShortInfo(SQLiteDatabase readableDatabase, String[] projection,
                                             String selection, String[] selectionArgs, String sortOrder) {
        //Instantiating a Query Builder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //Setting up the Tables involved in the query
        QueryArgsUtility.SuppliersShortInfoQuery.setTables(queryBuilder);
        //Setting up the Projection Map to use
        QueryArgsUtility.SuppliersShortInfoQuery.setProjectionMap(queryBuilder);

        //Querying for the content using the Query Builder
        return queryBuilder.query(
                readableDatabase,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getSalesWithShortInfo(SQLiteDatabase readableDatabase, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Instantiating a Query Builder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //Setting up the Tables involved in the query
        QueryArgsUtility.SalesShortInfoQuery.setTables(queryBuilder);
        //Setting up the Projection Map to use
        QueryArgsUtility.SalesShortInfoQuery.setProjectionMap(queryBuilder);
        //Building the Selection Clause and its Arguments
        Pair<String, String[]> selectionPairs = Pair.create(
                QueryArgsUtility.SalesShortInfoQuery.getSelection(),
                QueryArgsUtility.SalesShortInfoQuery.getSelectionArgs());

        if (!TextUtils.isEmpty(selection)) {
            //Combining the Selection clauses when Selection Clause is passed
            selectionPairs = combineSelectionPairs(
                    selectionPairs,
                    Pair.create(selection, selectionArgs),
                    AND
            );
        }

        //Querying for the content using the Query Builder
        return queryBuilder.query(
                readableDatabase,
                projection,
                //Where Clause
                selectionPairs != null ? selectionPairs.first : null,
                //The value in Where Clause
                selectionPairs != null ? selectionPairs.second : null,
                null,
                null,
                sortOrder
        );
    }


    private Cursor getItemSuppliersSalesInfo(Uri uri, SQLiteDatabase readableDatabase, String[] projection, String sortOrder) {
        //Instantiating a Query Builder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //Setting up the Tables involved in the query
        QueryArgsUtility.ItemSuppliersSalesQuery.setTables(queryBuilder);
        //Setting up the Projection Map to use
        QueryArgsUtility.ItemSuppliersSalesQuery.setProjectionMap(queryBuilder);

        //Querying for the content using the Query Builder
        return queryBuilder.query(
                readableDatabase,
                projection,
                //Where Clause
                QueryArgsUtility.ItemSuppliersSalesQuery.getSelection(),
                //The value in Where Clause which is the 'item_id' passed in the URI
                QueryArgsUtility.ItemSuppliersSalesQuery.getSelectionArgs(uri),
                null,
                null,
                sortOrder
        );
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //When there are no values to insert, throw an exception
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        }

        //Retrieving the database in write mode
        SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();

        //Declaring the Uri that will hold the Uri of the record inserted
        Uri returnUri;

        //Locking the database for insert
        writableDatabase.beginTransaction();
        try {
            //Using URI matcher to find the possible URI
            switch (sUriMatcher.match(uri)) {
                case CATEGORIES:
                    //For 'item_category' table
                    returnUri = insertWithConflictFail(uri,
                            ProductContract.ProductCategory.TABLE_NAME,
                            writableDatabase,
                            values
                    );
                    break;
                case ITEMS:
                    //For 'item' table
                    returnUri = insertWithConflictFail(uri,
                            ProductContract.Product.TABLE_NAME,
                            writableDatabase,
                            values
                    );
                    break;
                case SUPPLIERS:
                    //For 'supplier' table
                    returnUri = insertWithConflictFail(uri,
                            SupplierContract.Supplier.TABLE_NAME,
                            writableDatabase,
                            values
                    );
                    break;
                default:
                    throw new IllegalArgumentException("Unknown/Unsupported uri: " + uri);
            }
        } finally {
            //Releasing the lock in the end
            writableDatabase.endTransaction();
        }

        //Returning the URI generated for the record inserted
        return returnUri;
    }

    @Nullable
    private Uri insertWithConflictFail(Uri uri, String tableName, SQLiteDatabase writableDatabase, ContentValues values) {
        //Declaring a Uri that will hold the Uri of the record inserted
        Uri returnUri = null;

        try {
            //Inserting record into the table passed
            long recordId = writableDatabase.insertOrThrow(
                    tableName,
                    null,
                    values
            );

            //Validating the operation
            if (recordId == -1) {
                //Logging the error when insertion fails
                Log.e(LOG_TAG, "insertWithConflictFail: " + tableName + ": Failed to insert row for " + uri);
            } else {
                //On success of inserting the record

                //Mark the Transaction as successful
                writableDatabase.setTransactionSuccessful();

                //Notify listeners attached to the Content Resolver
                //that the data at the URI has changed
                getContext().getContentResolver().notifyChange(uri, null);

                //Generate the URI for the record inserted, with the id appended at the end
                returnUri = ContentUris.withAppendedId(uri, recordId);
            }
        } catch (SQLiteConstraintException e) {
            //Can occur if the inserted record already exists
            Log.e(LOG_TAG, "insertWithConflictFail: " + tableName + ": Record already exists for " + uri, e);
        } catch (SQLException e) {
            //For an error in SQL string or any other unknown causes
            //Logging the error
            Log.e(LOG_TAG, "insertWithConflictFail: " + tableName + ": Failed to insert row for " + uri, e);
        }

        //Returning the URI generated for the record inserted
        return returnUri;
    }


    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        if (values.length == 0) {
            //If the ContentValues is empty then propagate the call to super
            return super.bulkInsert(uri, values);
        }

        //Retrieving the database in write mode
        SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();

        //Stores the count of records inserted
        int noOfRecordsInserted;

        //Using URI matcher to find the possible URI
        switch (sUriMatcher.match(uri)) {
            case ITEM_ATTRS_ID:
                //For 'item_attr' table

                //Executing bulk insert after delete
                noOfRecordsInserted = bulkInsertHangOffTable(uri,
                        ProductContract.ProductAttribute.TABLE_NAME,
                        writableDatabase,
                        values,
                        true
                );
                break;
            case ITEM_IMAGES_ID:
                //For 'item_image' table

                //Executing bulk insert after delete
                noOfRecordsInserted = bulkInsertHangOffTable(uri,
                        ProductContract.ProductImage.TABLE_NAME,
                        writableDatabase,
                        values,
                        true
                );
                break;
            case SUPPLIER_CONTACTS_ID:
                //For 'supplier_contact' Table

                //Executing bulk insert without delete
                //(Because the Table definition supports CONFLICT REPLACE)
                noOfRecordsInserted = bulkInsertHangOffTable(uri,
                        SupplierContract.SupplierContact.TABLE_NAME,
                        writableDatabase,
                        values,
                        false
                );
                break;
            case SUPPLIER_ITEMS_ID:
            case ITEM_SUPPLIERS_ID:
                //For 'item_supplier_info' Table

                //Executing bulk insert without delete
                //(Because the Table definition supports CONFLICT REPLACE)
                noOfRecordsInserted = bulkInsertHangOffTable(uri,
                        SalesContract.ProductSupplierInfo.TABLE_NAME,
                        writableDatabase,
                        values,
                        false
                );
                break;
            case SALES_INVENTORY_ITEM_ID:
            case SALES_INVENTORY_SUPPLIER_ID:
                //For 'item_supplier_inventory' Table

                //Executing bulk insert without delete
                //(Because the Table definition supports CONFLICT REPLACE)
                noOfRecordsInserted = bulkInsertHangOffTable(uri,
                        SalesContract.ProductSupplierInventory.TABLE_NAME,
                        writableDatabase,
                        values,
                        false
                );
                break;
            default:
                noOfRecordsInserted = super.bulkInsert(uri, values);
        }

        //Returning the number of records inserted
        return noOfRecordsInserted;
    }


    private int bulkInsertHangOffTable(Uri uri, String tableName, SQLiteDatabase writableDatabase,
                                       ContentValues[] values, boolean deleteAllFirst) {

        if (deleteAllFirst) {
            //When the flag is passed as True

            //Delete all the records from the tables first
            delete(uri, null, null);
        }

        //Stores the count of records inserted
        int noOfRecordsInserted = 0;

        //Locking the database for insert
        writableDatabase.beginTransaction();
        try {
            //Using URI matcher to find the possible URI
            switch (sUriMatcher.match(uri)) {
                case ITEM_ATTRS_ID:
                    //For 'item_attr' table
                {
                    //Retrieving the Product ID
                    final long itemId = ContentUris.parseId(uri);

                    //Iterating over the ContentValues to insert the records
                    for (ContentValues contentValue : values) {
                        //Adding the 'item_id' of the record to insert
                        contentValue.put(ProductContract.ProductAttribute.COLUMN_ITEM_ID, itemId);

                        //Inserting the record into the table
                        if (insertBulkRecord(uri, tableName, writableDatabase, contentValue)) {
                            //On Successful insert, increment the number of records inserted
                            noOfRecordsInserted++;
                        }
                    }
                }
                break;
                case ITEM_IMAGES_ID:
                    //For 'item_image' table
                {
                    //Retrieving the Product ID
                    final long itemId = ContentUris.parseId(uri);

                    //Iterating over the ContentValues to insert the records
                    for (ContentValues contentValue : values) {
                        //Adding the 'item_id' of the record to insert
                        contentValue.put(ProductContract.ProductImage.COLUMN_ITEM_ID, itemId);

                        //Inserting the record into the table
                        if (insertBulkRecord(uri, tableName, writableDatabase, contentValue)) {
                            //On Successful insert, increment the number of records inserted
                            noOfRecordsInserted++;
                        }
                    }
                }
                break;
                case SUPPLIER_CONTACTS_ID:
                    //For 'supplier_contact' Table
                {
                    //Retrieving the Supplier ID
                    final long supplierId = ContentUris.parseId(uri);

                    //Iterating over the ContentValues to insert the records
                    for (ContentValues contentValue : values) {
                        //Adding the 'supplier_id' of the record to insert
                        contentValue.put(SupplierContract.SupplierContact.COLUMN_SUPPLIER_ID, supplierId);

                        //Inserting the record into the table
                        if (insertBulkRecord(uri, tableName, writableDatabase, contentValue)) {
                            //On Successful insert, increment the number of records inserted
                            noOfRecordsInserted++;
                        }
                    }
                }
                break;
                case SUPPLIER_ITEMS_ID:
                    //For 'item_supplier_info' Table, identified by 'supplier_id'
                {
                    //Retrieving the Supplier ID
                    final long supplierId = ContentUris.parseId(uri);

                    //Iterating over the ContentValues to insert the records
                    for (ContentValues contentValue : values) {
                        //Adding the 'supplier_id' of the record to insert
                        contentValue.put(SalesContract.ProductSupplierInfo.COLUMN_SUPPLIER_ID, supplierId);

                        //Inserting the record into the table
                        if (insertBulkRecord(uri, tableName, writableDatabase, contentValue)) {
                            //On Successful insert, increment the number of records inserted
                            noOfRecordsInserted++;
                        }
                    }
                }
                break;
                case ITEM_SUPPLIERS_ID:
                    //For 'item_supplier_info' Table, identified by 'item_id'
                {
                    //Retrieving the Product ID
                    final long itemId = ContentUris.parseId(uri);

                    //Iterating over the ContentValues to insert the records
                    for (ContentValues contentValue : values) {
                        //Adding the 'item_id' of the record to insert
                        contentValue.put(SalesContract.ProductSupplierInfo.COLUMN_ITEM_ID, itemId);

                        //Inserting the record into the table
                        if (insertBulkRecord(uri, tableName, writableDatabase, contentValue)) {
                            //On Successful insert, increment the number of records inserted
                            noOfRecordsInserted++;
                        }
                    }
                }
                break;
                case SALES_INVENTORY_ITEM_ID:
                    //For 'item_supplier_inventory' Table, identified by 'item_id'
                {
                    //Retrieving the Product ID
                    final long itemId = ContentUris.parseId(uri);

                    //Iterating over the ContentValues to insert the records
                    for (ContentValues contentValue : values) {
                        //Adding the 'item_id' of the record to insert
                        contentValue.put(SalesContract.ProductSupplierInventory.COLUMN_ITEM_ID, itemId);

                        //Inserting the record into the table
                        if (insertBulkRecord(uri, tableName, writableDatabase, contentValue)) {
                            //On Successful insert, increment the number of records inserted
                            noOfRecordsInserted++;
                        }
                    }
                }
                break;
                case SALES_INVENTORY_SUPPLIER_ID:
                    //For 'item_supplier_inventory' Table, identified by 'supplier_id'
                {
                    //Retrieving the Supplier ID
                    final long supplierId = ContentUris.parseId(uri);

                    //Iterating over the ContentValues to insert the records
                    for (ContentValues contentValue : values) {
                        //Adding the 'supplier_id' of the record to insert
                        contentValue.put(SalesContract.ProductSupplierInventory.COLUMN_SUPPLIER_ID, supplierId);

                        //Inserting the record into the table
                        if (insertBulkRecord(uri, tableName, writableDatabase, contentValue)) {
                            //On Successful insert, increment the number of records inserted
                            noOfRecordsInserted++;
                        }
                    }
                }
                break;
            }
        } finally {

            if (noOfRecordsInserted == values.length) {
                //Mark the Transaction as successful when all the records were inserted
                writableDatabase.setTransactionSuccessful();
            }

            //Releasing the lock in the end
            writableDatabase.endTransaction();
        }

        //Returning the number of records inserted
        return noOfRecordsInserted;
    }


    private boolean insertBulkRecord(Uri uri, String tableName, SQLiteDatabase writableDatabase, ContentValues contentValue) {
        //Inserting the record into the table
        long recordId = writableDatabase.insert(tableName,
                null, contentValue);

        //Validating the operation
        if (recordId == -1) {
            //Logging the error when insertion fails
            Log.e(LOG_TAG, "insertBulkRecord: " + tableName + ": Failed to insert row for " + uri);
            //Returning False on Failure
            return false;
        } else {
            //On success of inserting the record

            //Notify listeners attached to the Content Resolver
            //that the data at the URI has changed
            getContext().getContentResolver().notifyChange(uri, null);

            //Returning True on Success
            return true;
        }
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //Retrieving the database in write mode
        SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();

        //Stores the count of records deleted
        int noOfRecordsDeleted;

        //Locking the database for delete
        writableDatabase.beginTransaction();
        try {
            //Using URI matcher to find the possible URI
            switch (sUriMatcher.match(uri)) {
                case ITEM_ATTRS_ID:
                    //For specific set of records from the 'item_attr' table
                    //identified by the 'item_id' passed in the URI

                    //Building the where clause
                    selection = ProductContract.ProductAttribute.COLUMN_ITEM_ID + EQUALS + PLACEHOLDER;
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            ProductContract.ProductAttribute.TABLE_NAME,
                            selection,
                            selectionArgs
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                    break;

                case ITEM_IMAGES_ID:
                    //For specific set of records from the 'item_image' table
                    //identified by the 'item_id' passed in the URI

                    //Building the where clause
                    selection = ProductContract.ProductImage.COLUMN_ITEM_ID + EQUALS + PLACEHOLDER;
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            ProductContract.ProductImage.TABLE_NAME,
                            selection,
                            selectionArgs
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                    break;

                case ITEM_ID:
                    //For a specific Item from the 'item' Table
                    //identified by the Item's '_id' passed in the URI

                    //Building the where clause
                    selection = ProductContract.Product._ID + EQUALS + PLACEHOLDER;
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            ProductContract.Product.TABLE_NAME,
                            selection,
                            selectionArgs
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                    break;

                case SUPPLIER_ID:
                    //For a specific Supplier from the 'supplier' Table
                    //identified by the Supplier's '_id' passed in the URI

                    //Building the where clause
                    selection = SupplierContract.Supplier._ID + EQUALS + PLACEHOLDER;
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            SupplierContract.Supplier.TABLE_NAME,
                            selection,
                            selectionArgs
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                    break;

                case SUPPLIER_CONTACTS_ID:
                    //For specific set of records from the 'supplier_contact' table
                    //identified by the 'supplier_id' passed in the URI
                {
                    //Building the where clause and its Arguments
                    String selection1 = SupplierContract.SupplierContact.COLUMN_SUPPLIER_ID + EQUALS + PLACEHOLDER;
                    String[] selectionArgs1 = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Building a Pair of where clause and its Arguments
                    Pair<String, String[]> selectionPairs = Pair.create(
                            selection1,
                            selectionArgs1);

                    if (!TextUtils.isEmpty(selection)) {
                        //Combining the Selection clauses when Selection Clause is passed
                        selectionPairs = combineSelectionPairs(
                                selectionPairs,
                                Pair.create(selection, selectionArgs),
                                AND
                        );
                    }

                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            SupplierContract.SupplierContact.TABLE_NAME,
                            selectionPairs != null ? selectionPairs.first : null,
                            selectionPairs != null ? selectionPairs.second : null
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                }
                break;

                case SUPPLIER_ITEMS_ID:
                    //For specific set of records from the 'item_supplier_info' table
                    //identified by the 'supplier_id' passed in the URI
                {
                    //Building the where clause and its Arguments
                    String selection1 = SalesContract.ProductSupplierInfo.COLUMN_SUPPLIER_ID + EQUALS + PLACEHOLDER;
                    String[] selectionArgs1 = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Building a Pair of where clause and its Arguments
                    Pair<String, String[]> selectionPairs = Pair.create(
                            selection1,
                            selectionArgs1);

                    if (!TextUtils.isEmpty(selection)) {
                        //Combining the Selection clauses when Selection Clause is passed
                        selectionPairs = combineSelectionPairs(
                                selectionPairs,
                                Pair.create(selection, selectionArgs),
                                AND
                        );
                    }

                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            SalesContract.ProductSupplierInfo.TABLE_NAME,
                            selectionPairs != null ? selectionPairs.first : null,
                            selectionPairs != null ? selectionPairs.second : null
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                }
                break;

                case ITEM_SUPPLIERS_ID:
                    //For specific set of records from the 'item_supplier_info' table
                    //identified by the 'item_id' passed in the URI
                {
                    //Building the where clause and its Arguments
                    String selection1 = SalesContract.ProductSupplierInfo.COLUMN_ITEM_ID + EQUALS + PLACEHOLDER;
                    String[] selectionArgs1 = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Building a Pair of where clause and its Arguments
                    Pair<String, String[]> selectionPairs = Pair.create(
                            selection1,
                            selectionArgs1);

                    if (!TextUtils.isEmpty(selection)) {
                        //Combining the Selection clauses when Selection Clause is passed
                        selectionPairs = combineSelectionPairs(
                                selectionPairs,
                                Pair.create(selection, selectionArgs),
                                AND
                        );
                    }

                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            SalesContract.ProductSupplierInfo.TABLE_NAME,
                            selectionPairs != null ? selectionPairs.first : null,
                            selectionPairs != null ? selectionPairs.second : null
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                }
                break;

                case SALES_INVENTORY_ITEM_ID:
                    //For specific set of records from the 'item_supplier_inventory' table
                    //identified by the 'item_id' passed in the URI
                {
                    //Building the where clause
                    String selection1 = SalesContract.ProductSupplierInventory.COLUMN_ITEM_ID + EQUALS + PLACEHOLDER;
                    String[] selectionArgs1 = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Building a Pair of where clause and its Arguments
                    Pair<String, String[]> selectionPairs = Pair.create(
                            selection1,
                            selectionArgs1);

                    if (!TextUtils.isEmpty(selection)) {
                        //Combining the Selection clauses when Selection Clause is passed
                        selectionPairs = combineSelectionPairs(
                                selectionPairs,
                                Pair.create(selection, selectionArgs),
                                AND
                        );
                    }

                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            SalesContract.ProductSupplierInventory.TABLE_NAME,
                            selectionPairs != null ? selectionPairs.first : null,
                            selectionPairs != null ? selectionPairs.second : null
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                }
                break;

                case SALES_INVENTORY_SUPPLIER_ID:
                    //For specific set of records from the 'item_supplier_inventory' table
                    //identified by the 'supplier_id' passed in the URI
                {
                    //Building the where clause
                    String selection1 = SalesContract.ProductSupplierInventory.COLUMN_SUPPLIER_ID + EQUALS + PLACEHOLDER;
                    String[] selectionArgs1 = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Building a Pair of where clause and its Arguments
                    Pair<String, String[]> selectionPairs = Pair.create(
                            selection1,
                            selectionArgs1);

                    if (!TextUtils.isEmpty(selection)) {
                        //Combining the Selection clauses when Selection Clause is passed
                        selectionPairs = combineSelectionPairs(
                                selectionPairs,
                                Pair.create(selection, selectionArgs),
                                AND
                        );
                    }

                    //Executing delete
                    noOfRecordsDeleted = writableDatabase.delete(
                            SalesContract.ProductSupplierInventory.TABLE_NAME,
                            selectionPairs != null ? selectionPairs.first : null,
                            selectionPairs != null ? selectionPairs.second : null
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                }
                break;

                default:
                    throw new IllegalArgumentException("Delete operation is not supported for " + uri);
            }
        } finally {
            //Releasing the lock in the end
            writableDatabase.endTransaction();
        }

        if (noOfRecordsDeleted > 0) {
            //Notifying the listeners that the data at the URI has changed, when some rows are deleted
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Returning the number of records deleted
        return noOfRecordsDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        //When there are no values to update, throw an exception
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        }

        //Retrieving the database in write mode
        SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();

        //Stores the count of records updated
        int noOfRecordsUpdated;

        //Locking the database for update
        writableDatabase.beginTransaction();
        try {
            //Using URI matcher to find the possible URI
            switch (sUriMatcher.match(uri)) {
                case ITEM_ID:
                    //For a specific Item from the 'item' Table
                    //identified by the Item's '_id' passed in the URI

                    //Building the where clause
                    selection = ProductContract.Product._ID + EQUALS + PLACEHOLDER;
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Executing update
                    noOfRecordsUpdated = writableDatabase.update(
                            ProductContract.Product.TABLE_NAME,
                            values,
                            selection,
                            selectionArgs
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                    break;

                case SUPPLIER_ID:
                    //For a specific Supplier from the 'supplier' Table
                    //identified by the Supplier's '_id' passed in the URI

                    //Building the where clause
                    selection = SupplierContract.Supplier._ID + EQUALS + PLACEHOLDER;
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Executing update
                    noOfRecordsUpdated = writableDatabase.update(
                            SupplierContract.Supplier.TABLE_NAME,
                            values,
                            selection,
                            selectionArgs
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                    break;

                case SALES_INVENTORY_ITEM_ID:
                    //For specific set of records from the 'item_supplier_inventory' table
                    //identified by the 'item_id' passed in the URI
                {
                    //Building the where clause
                    String selection1 = SalesContract.ProductSupplierInventory.COLUMN_ITEM_ID + EQUALS + PLACEHOLDER;
                    String[] selectionArgs1 = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Building a Pair of where clause and its Arguments
                    Pair<String, String[]> selectionPairs = Pair.create(
                            selection1,
                            selectionArgs1);

                    if (!TextUtils.isEmpty(selection)) {
                        //Combining the Selection clauses when Selection Clause is passed
                        selectionPairs = combineSelectionPairs(
                                selectionPairs,
                                Pair.create(selection, selectionArgs),
                                AND
                        );
                    }

                    //Executing update
                    noOfRecordsUpdated = writableDatabase.update(
                            SalesContract.ProductSupplierInventory.TABLE_NAME,
                            values,
                            selectionPairs != null ? selectionPairs.first : null,
                            selectionPairs != null ? selectionPairs.second : null
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                }
                break;

                case SALES_INVENTORY_SUPPLIER_ID:
                    //For specific set of records from the 'item_supplier_inventory' table
                    //identified by the 'supplier_id' passed in the URI
                {
                    //Building the where clause
                    String selection1 = SalesContract.ProductSupplierInventory.COLUMN_SUPPLIER_ID + EQUALS + PLACEHOLDER;
                    String[] selectionArgs1 = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    //Building a Pair of where clause and its Arguments
                    Pair<String, String[]> selectionPairs = Pair.create(
                            selection1,
                            selectionArgs1);

                    if (!TextUtils.isEmpty(selection)) {
                        //Combining the Selection clauses when Selection Clause is passed
                        selectionPairs = combineSelectionPairs(
                                selectionPairs,
                                Pair.create(selection, selectionArgs),
                                AND
                        );
                    }

                    //Executing update
                    noOfRecordsUpdated = writableDatabase.update(
                            SalesContract.ProductSupplierInventory.TABLE_NAME,
                            values,
                            selectionPairs != null ? selectionPairs.first : null,
                            selectionPairs != null ? selectionPairs.second : null
                    );
                    //Marking the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                }
                break;

                default:
                    throw new IllegalArgumentException("Update operation is not supported for " + uri);
            }
        } finally {
            //Releasing the lock in the end
            writableDatabase.endTransaction();
        }

        if (noOfRecordsUpdated > 0) {
            //Notifying the listeners that the data at the URI has changed, when some rows are updated
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Returning the number of records updated
        return noOfRecordsUpdated;
    }
}
