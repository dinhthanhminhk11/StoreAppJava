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

package com.example.kaushiknsanji.storeapp.ui.inventory.config;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigContract;
import com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;


public class SalesConfigActivityFragment extends Fragment implements SalesConfigContract.View, View.OnClickListener {

    //Constant used for logs
    private static final String LOG_TAG = SalesConfigActivityFragment.class.getSimpleName();

    //The Bundle argument constant of this Fragment
    private static final String ARGUMENT_INT_PRODUCT_ID = "argument.PRODUCT_ID";

    //Bundle constants for persisting the data throughout System config changes
    private static final String BUNDLE_PRODUCT_NAME_KEY = "SalesConfig.ProductName";
    private static final String BUNDLE_PRODUCT_SKU_KEY = "SalesConfig.ProductSku";
    private static final String BUNDLE_PRODUCT_DESCRIPTION_KEY = "SalesConfig.ProductDescription";
    private static final String BUNDLE_PRODUCT_CATEGORY_KEY = "SalesConfig.ProductCategory";
    private static final String BUNDLE_PRODUCT_ORIGINAL_TOTAL_AVAIL_QTY_INT_KEY = "SalesConfig.OriginalTotalAvailableQuantity";
    private static final String BUNDLE_PRODUCT_IMAGES_LIST_KEY = "SalesConfig.ProductImages";
    private static final String BUNDLE_PRODUCT_ATTRS_LIST_KEY = "SalesConfig.ProductAttributes";
    private static final String BUNDLE_PRODUCT_SUPPLIERS_LIST_KEY = "SalesConfig.ProductSuppliers";
    private static final String BUNDLE_PRODUCT_RESTORED_BOOL_KEY = "SalesConfig.IsProductRestored";
    private static final String BUNDLE_SUPPLIERS_RESTORED_BOOL_KEY = "SalesConfig.AreSuppliersRestored";

    //The Presenter for this View
    private SalesConfigContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private TextView mTextViewProductName;
    private TextView mTextViewProductSku;
    private TextView mTextViewProductDesc;
    private TextView mTextViewProductCategory;
    private TextView mTextViewProductAvailableQuantity;
    private TableLayout mTableLayoutProductAttrs;
    private RecyclerView mRecyclerViewProductSuppliers;

    //RecyclerView Adapter for Product's Suppliers
    private ProductSuppliersAdapter mProductSuppliersAdapter;

    //Stores the Product ID, retrieved from Bundle arguments passed
    private int mProductId;
    //Stores the Product Attributes list of the Product
    private ArrayList<ProductAttribute> mProductAttributes;
    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores the state of Product details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsProductRestored;

    //Stores the state of Supplier details restored,
    //to prevent updating the fields every time during System config change
    private boolean mAreSuppliersRestored;

    //Stores the value of the Original Total Available Quantity of the Product
    private int mOldTotalAvailableQuantity;

    private DialogInterface.OnClickListener mProductDeleteDialogOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Taking action based on the button clicked
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //For "Yes" Button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Dispatch to the Presenter to delete the Product
                    mPresenter.deleteProduct();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //For "No" Button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener mUnsavedDialogOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Taking action based on the button clicked
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //For "Save" button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Start saving the Product Sales
                    saveProductSales();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //For "Discard" button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Dispatch to the Presenter to finish the Activity without saving changes
                    mPresenter.finishActivity();
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    //For "Keep Editing" button

                    //Just Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    break;
            }
        }
    };

    public SalesConfigActivityFragment() {
    }


    public static SalesConfigActivityFragment newInstance(int productId) {
        //Saving the arguments passed, in a Bundle
        Bundle args = new Bundle(1);
        args.putInt(ARGUMENT_INT_PRODUCT_ID, productId);

        //Instantiating the Fragment
        SalesConfigActivityFragment fragment = new SalesConfigActivityFragment();
        //Passing the Bundle as Arguments to this Fragment
        fragment.setArguments(args);

        //Returning the fragment instance
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Indicating that this fragment has menu options to show
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout 'R.layout.fragment_sales_config' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_sales_config, container, false);

        //Finding the views to initialize
        mTextViewProductName = rootView.findViewById(R.id.text_sales_config_product_name);
        mTextViewProductSku = rootView.findViewById(R.id.text_sales_config_product_sku);
        mTextViewProductDesc = rootView.findViewById(R.id.text_sales_config_product_desc);
        mTextViewProductCategory = rootView.findViewById(R.id.text_sales_config_product_category);
        mTextViewProductAvailableQuantity = rootView.findViewById(R.id.text_sales_config_total_available_quantity);
        mTableLayoutProductAttrs = rootView.findViewById(R.id.tablelayout_sales_config_product_attrs);
        mRecyclerViewProductSuppliers = rootView.findViewById(R.id.recyclerview_sales_config_suppliers);

        //Registering Click listener on Product Edit Image Button
        rootView.findViewById(R.id.imgbtn_sales_config_product_edit).setOnClickListener(this);

        //Retrieving the Product Id and Supplier Id from the Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            mProductId = arguments.getInt(ARGUMENT_INT_PRODUCT_ID, ProductConfigContract.NEW_PRODUCT_INT);
        }

        //Initialize RecyclerView for Product's Suppliers
        setupProductSuppliersRecyclerView();

        //Returning the prepared layout
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mProductId == ProductConfigContract.NEW_PRODUCT_INT) {
            //When the Product Id is not an existing Id, then finish the Activity
            mPresenter.doCancel();
        } else {
            //When the Product Id is valid, start downloading the required data
            mPresenter.start();
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //On Subsequent launch

            //Restoring Product Name
            mPresenter.updateProductName(savedInstanceState.getString(BUNDLE_PRODUCT_NAME_KEY));

            //Restoring Product SKU
            mPresenter.updateProductSku(savedInstanceState.getString(BUNDLE_PRODUCT_SKU_KEY));

            //Restoring Product Description
            mPresenter.updateProductDescription(savedInstanceState.getString(BUNDLE_PRODUCT_DESCRIPTION_KEY));

            //Restoring Product Category
            mPresenter.updateProductCategory(savedInstanceState.getString(BUNDLE_PRODUCT_CATEGORY_KEY));

            //Restoring Product Images
            mPresenter.updateProductImage(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_IMAGES_LIST_KEY));

            //Restoring Product Attributes
            mPresenter.updateProductAttributes(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_ATTRS_LIST_KEY));

            //Restoring Product's Suppliers and Sales information
            mPresenter.updateProductSupplierSalesList(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_SUPPLIERS_LIST_KEY));

            //Restoring the Original Total Available Quantity after loading the Sales information
            //to overwrite with the corrected value
            mPresenter.updateAndSyncOldTotalAvailability(savedInstanceState.getInt(BUNDLE_PRODUCT_ORIGINAL_TOTAL_AVAIL_QTY_INT_KEY));

            //Restoring the state of Product data restored
            mPresenter.updateAndSyncProductState(savedInstanceState.getBoolean(BUNDLE_PRODUCT_RESTORED_BOOL_KEY, false));

            //Restoring the state of Suppliers data restored
            mPresenter.updateAndSyncSuppliersState(savedInstanceState.getBoolean(BUNDLE_SUPPLIERS_RESTORED_BOOL_KEY, false));
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Trigger Focus Loss to capture any value partially entered
        mPresenter.triggerFocusLost();

        //Saving the state
        outState.putString(BUNDLE_PRODUCT_NAME_KEY, mTextViewProductName.getText().toString());
        outState.putString(BUNDLE_PRODUCT_SKU_KEY, mTextViewProductSku.getText().toString());
        outState.putString(BUNDLE_PRODUCT_DESCRIPTION_KEY, mTextViewProductDesc.getText().toString());
        outState.putString(BUNDLE_PRODUCT_CATEGORY_KEY, mTextViewProductCategory.getText().toString());
        outState.putInt(BUNDLE_PRODUCT_ORIGINAL_TOTAL_AVAIL_QTY_INT_KEY, mOldTotalAvailableQuantity);
        outState.putParcelableArrayList(BUNDLE_PRODUCT_IMAGES_LIST_KEY, mProductImages);
        outState.putParcelableArrayList(BUNDLE_PRODUCT_ATTRS_LIST_KEY, mProductAttributes);
        outState.putParcelableArrayList(BUNDLE_PRODUCT_SUPPLIERS_LIST_KEY, mProductSuppliersAdapter.getProductSupplierSalesList());
        outState.putBoolean(BUNDLE_PRODUCT_RESTORED_BOOL_KEY, mIsProductRestored);
        outState.putBoolean(BUNDLE_SUPPLIERS_RESTORED_BOOL_KEY, mAreSuppliersRestored);
    }


    @Override
    public void setPresenter(SalesConfigContract.Presenter presenter) {
        mPresenter = presenter;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Inflating the Menu options from 'R.menu.menu_fragment_sales_config'
        inflater.inflate(R.menu.menu_fragment_sales_config, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling based on the Menu item selected
        switch (item.getItemId()) {
            case R.id.action_delete:
                //On Click of Delete Menu

                //Delegating to the Presenter to show the re-confirmation dialog
                mPresenter.showDeleteProductDialog();
                return true;
            case R.id.action_save:
                //On Click of Save Menu

                //Saving the Product Sales information
                saveProductSales();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveProductSales() {
        //Delegating to the Presenter to trigger focus loss on listener registered Views,
        //in order to persist their data
        mPresenter.triggerFocusLost();

        //Delegating to the Presenter to initiate the Save process
        mPresenter.onSave(mProductSuppliersAdapter.getProductSupplierSalesList());
    }


    private void setupProductSuppliersRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewProductSuppliers.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mProductSuppliersAdapter = new ProductSuppliersAdapter(new ProductSupplierItemUserActionsListener());

        //Setting the Adapter on RecyclerView
        mRecyclerViewProductSuppliers.setAdapter(mProductSuppliersAdapter);

        //Attaching the ItemTouchHelper for Swipe delete
        mProductSuppliersAdapter.getItemTouchHelper().attachToRecyclerView(mRecyclerViewProductSuppliers);
    }

    @Override
    public void syncProductState(boolean isProductRestored) {
        //Saving the state
        mIsProductRestored = isProductRestored;
    }

    @Override
    public void syncSuppliersState(boolean areSuppliersRestored) {
        //Saving the state
        mAreSuppliersRestored = areSuppliersRestored;
    }

    @Override
    public void syncOldTotalAvailability(int oldTotalAvailableQuantity) {
        //Saving the original Total available quantity
        mOldTotalAvailableQuantity = oldTotalAvailableQuantity;
    }

    @Override
    public void showProgressIndicator(int statusTextId) {
        ProgressDialogFragment.showDialog(getChildFragmentManager(), getString(statusTextId));
    }

    @Override
    public void hideProgressIndicator() {
        ProgressDialogFragment.dismissDialog(getChildFragmentManager());
    }

    @Override
    public void showError(int messageId, @Nullable Object... args) {
        if (getView() != null) {
            //When we have the root view

            //Evaluating the message to be shown
            String messageToBeShown;
            if (args != null && args.length > 0) {
                //For the String Resource with args
                messageToBeShown = getString(messageId, args);
            } else {
                //For the String Resource without args
                messageToBeShown = getString(messageId);
            }

            if (!TextUtils.isEmpty(messageToBeShown)) {
                //Displaying the Snackbar message of indefinite time length
                //when we have the error message to be shown

                new SnackbarUtility(Snackbar.make(getView(), messageToBeShown, Snackbar.LENGTH_INDEFINITE))
                        .revealCompleteMessage() //Removes the limit on max lines
                        .setDismissAction(R.string.snackbar_action_ok) //For the Dismiss "OK" action
                        .showSnack();
            }
        }
    }

    @Override
    public void updateProductName(String productName) {
        //Setting the Product Name
        mTextViewProductName.setText(productName);
    }

    @Override
    public void updateProductSku(String productSku) {
        //Setting the Product SKU
        mTextViewProductSku.setText(productSku);
        //Setting Barcode typeface for the SKU
        mTextViewProductSku.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.libre_barcode_128_text_regular));
    }

    @Override
    public void updateProductCategory(String productCategory) {
        mTextViewProductCategory.setText(productCategory);
    }

    @Override
    public void updateProductDescription(String description) {
        mTextViewProductDesc.setText(description);
    }

    @Override
    public void updateProductImages(ArrayList<ProductImage> productImages) {
        //Saving the list of ProductImages
        mProductImages = productImages;
    }

    @Override
    public void updateProductAttributes(ArrayList<ProductAttribute> productAttributes) {
        //Saving the Product Attributes data
        mProductAttributes = productAttributes;

        //Set Stretch all Table Columns
        mTableLayoutProductAttrs.setStretchAllColumns(true);

        //Removing all the Child Views if any
        mTableLayoutProductAttrs.removeAllViewsInLayout();

        //Retrieving the Number of Product Attributes
        int noOfProductAttrs = mProductAttributes.size();

        //Iterating over the list to build a Table for Product Attributes to be shown
        for (int index = 0; index < noOfProductAttrs; index++) {
            //Retrieving the current Product Attribute at the index
            ProductAttribute productAttribute = mProductAttributes.get(index);

            //Creating a new TableRow to show the entry
            TableRow tableRow = new TableRow(requireContext());
            //Setting TableRow LayoutParams
            TableLayout.LayoutParams tableRowLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            //Adding the New Row to the TableLayout
            mTableLayoutProductAttrs.addView(tableRow, tableRowLayoutParams);

            //Setting background for the table row with different color shape for odd and even rows
            if (index % 2 == 0) {
                //When even
                tableRow.setBackgroundResource(R.drawable.shape_sales_config_product_attrs_table_bg_even_row);
            } else {
                //When odd
                tableRow.setBackgroundResource(R.drawable.shape_sales_config_product_attrs_table_bg_odd_row);
            }

            //Setting the LayoutParams for Table Cell TextView
            TableRow.LayoutParams textViewCellLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);

            //Inflating a TextView with Style to hold the Attribute Name
            TextView textViewAttrName = (TextView) LayoutInflater.from(requireContext()).inflate(R.layout.layout_sales_config_product_attrs_table_cell_name, tableRow, false);
            //Setting the Attribute Name on TextView
            textViewAttrName.setText(productAttribute.getAttributeName());
            //Adding Attribute Name TextView to the TableRow
            tableRow.addView(textViewAttrName, textViewCellLayoutParams);

            //Inflating a TextView with Style to hold the Attribute Value
            TextView textViewAttrValue = (TextView) LayoutInflater.from(requireContext()).inflate(R.layout.layout_sales_config_product_attrs_table_cell_value, tableRow, false);
            //Setting the Attribute Value on TextView
            textViewAttrValue.setText(productAttribute.getAttributeValue());
            //Adding Attribute Value TextView to the TableRow
            tableRow.addView(textViewAttrValue, textViewCellLayoutParams);
        }
    }

    @Override
    public void loadProductSuppliersData(ArrayList<ProductSupplierSales> productSupplierSalesList) {
        mProductSuppliersAdapter.submitList(productSupplierSalesList);
    }

    @Override
    public void updateAvailability(int totalAvailableQuantity) {
        //Set the total available quantity value
        mTextViewProductAvailableQuantity.setText(String.valueOf(totalAvailableQuantity));
        //Set the Text Color
        mTextViewProductAvailableQuantity.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
    }

    @Override
    public void showOutOfStockAlert() {
        //Set the Out of Stock message
        mTextViewProductAvailableQuantity.setText(getString(R.string.sales_list_item_out_of_stock));
        //Set the Text Color
        mTextViewProductAvailableQuantity.setTextColor(ContextCompat.getColor(requireContext(), R.color.salesListItemOutOfStockColor));
    }

    @Override
    public void showProductSupplierSwiped(String supplierCode) {
        if (getView() != null) {
            new SnackbarUtility(Snackbar.make(getView(),
                    getString(R.string.sales_config_supplier_swipe_action_success,
                            supplierCode), Snackbar.LENGTH_LONG))
                    .revealCompleteMessage()
                    .setAction(R.string.snackbar_action_undo, (view) -> {
                        //Try and Restore the Adapter data when UNDO is clicked
                        if (mProductSuppliersAdapter.restoreLastRemovedProductSupplierSales()) {
                            //On Success, show a Snackbar message
                            Snackbar.make(getView(),
                                    getString(R.string.sales_config_supplier_swipe_action_undo_success, supplierCode),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .showSnack();
        }
    }

    @Override
    public void showUpdateProductSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_update_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showUpdateSupplierSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_update_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showDeleteSupplierSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_delete_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void triggerFocusLost() {
        //Clearing focus in the last registered view in Product Suppliers Adapter
        if (mProductSuppliersAdapter != null) {
            mProductSuppliersAdapter.triggerFocusLost();
        }
    }

    @Override
    public void showDiscardDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive, neutral and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.sales_config_unsaved_changes_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(R.string.sales_config_unsaved_changes_dialog_positive_text, mUnsavedDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(R.string.sales_config_unsaved_changes_dialog_negative_text, mUnsavedDialogOnClickListener);
        //Set the Neutral Button and its listener
        builder.setNeutralButton(R.string.sales_config_unsaved_changes_dialog_neutral_text, mUnsavedDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    @Override
    public void showDeleteProductDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.sales_config_delete_product_confirm_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(android.R.string.yes, mProductDeleteDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(android.R.string.no, mProductDeleteDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    @Override
    public void onClick(View view) {
        //Taking action based on the Id of the View clicked
        switch (view.getId()) {
            case R.id.imgbtn_sales_config_product_edit:
                //For the Product Edit ImageButton

                //Delegating to the Presenter to launch the ProductConfigActivity to edit the Product
                mPresenter.editProduct(mProductId);
                break;
        }
    }

    private static class ProductSuppliersAdapter extends ListAdapter<ProductSupplierSales, ProductSuppliersAdapter.ViewHolder> {

        private static DiffUtil.ItemCallback<ProductSupplierSales> DIFF_SUPPLIERS
                = new DiffUtil.ItemCallback<ProductSupplierSales>() {

            @Override
            public boolean areItemsTheSame(ProductSupplierSales oldItem, ProductSupplierSales newItem) {
                //Returning the comparison of Product and Supplier's Id
                return (oldItem.getItemId() == newItem.getItemId()) && (oldItem.getSupplierId() == newItem.getSupplierId());
            }

            @Override
            public boolean areContentsTheSame(ProductSupplierSales oldItem, ProductSupplierSales newItem) {
                //Returning the result of equals
                return oldItem.equals(newItem);
            }
        };
        //The Data of this Adapter that stores the Supplier details with Selling Price
        //and Product Availability
        private ArrayList<ProductSupplierSales> mProductSupplierSalesList;
        //Stores the EditText View that had last acquired focus
        private View mLastFocusedView;
        //Stores last removed data if needs to be undone
        private ProductSupplierSales mLastRemovedProductSupplierSales;
        //Listener for User Actions on Product's List of Suppliers
        private ProductSuppliersUserActionsListener mActionsListener;

        ProductSuppliersAdapter(ProductSuppliersUserActionsListener userActionsListener) {
            super(DIFF_SUPPLIERS);
            //Registering the User Actions Listener
            mActionsListener = userActionsListener;
        }

        @NonNull
        @Override
        public ProductSuppliersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_sales_config_supplier'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_config_supplier, parent, false);
            //Returning the Instance of ViewHolder for the inflated Item View
            return new ViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(@NonNull ProductSuppliersAdapter.ViewHolder holder, int position) {
            //Get the data at the position
            ProductSupplierSales productSupplierSales = getItem(position);
            //Binding the data at the position
            holder.bind(productSupplierSales);
        }

        @Override
        public void submitList(List<ProductSupplierSales> submittedList) {
            //Restoring the quantities from the current list if present
            if (mProductSupplierSalesList != null && mProductSupplierSalesList.size() > 0) {
                //When we had some data

                //Creating a SparseArray of Supplier Id with their Quantities for lookup
                SparseIntArray suppliersQuantityArray = new SparseIntArray();
                for (ProductSupplierSales productSupplierSales : mProductSupplierSalesList) {
                    suppliersQuantityArray.put(productSupplierSales.getSupplierId(), productSupplierSales.getAvailableQuantity());
                }

                if (suppliersQuantityArray.size() > 0) {
                    //When we have lookup data

                    //Iterating over the submitted list to update them with the current quantities of Suppliers
                    for (ProductSupplierSales productSupplierSales : submittedList) {
                        productSupplierSales.setAvailableQuantity(suppliersQuantityArray.get(productSupplierSales.getSupplierId()));
                    }
                }
            }

            //Saving the updated list in the Adapter
            mProductSupplierSalesList = new ArrayList<>();
            mProductSupplierSalesList.addAll(submittedList);

            //Calculating the New Total Available Quantity of the Product
            int totalAvailableQuantity = 0;
            for (ProductSupplierSales productSupplierSales : mProductSupplierSalesList) {
                totalAvailableQuantity += productSupplierSales.getAvailableQuantity();
            }
            //Publishing the New Total Available Quantity of the Product to the Listener
            mActionsListener.onUpdatedAvailability(totalAvailableQuantity);

            //Creating a new list to publish the new data passed
            ArrayList<ProductSupplierSales> newProductSupplierSalesList = new ArrayList<>(submittedList);
            //Propagating the new list to super
            super.submitList(newProductSupplierSalesList);
        }


        void triggerFocusLost() {
            if (mLastFocusedView != null) {
                mLastFocusedView.clearFocus();
            }
        }


        ArrayList<ProductSupplierSales> getProductSupplierSalesList() {
            return mProductSupplierSalesList;
        }


        ItemTouchHelper getItemTouchHelper() {
            //Creating and returning an instance of ItemTouchHelper enabled for Left and Right swipe actions
            return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    //Not enabled for Dragging
                    return false;
                }


                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    //Retrieving the Adapter position
                    int itemPosition = viewHolder.getAdapterPosition();
                    //Get the data at the position
                    ProductSupplierSales productSupplierSales = getItem(itemPosition);
                    //Storing the data being removed
                    mLastRemovedProductSupplierSales = productSupplierSales;
                    //Removing the data from the Adapter list
                    mProductSupplierSalesList.remove(productSupplierSales);
                    //Submitting the updated list to the Adapter
                    submitList(mProductSupplierSalesList);
                    //Propagating the event to the listener
                    mActionsListener.onSwiped(itemPosition, mLastRemovedProductSupplierSales);
                }
            });
        }

        boolean restoreLastRemovedProductSupplierSales() {
            if (mLastRemovedProductSupplierSales != null) {
                //When we have the last removed Supplier Sales data

                //Add it back to the Adapter data list
                mProductSupplierSalesList.add(mLastRemovedProductSupplierSales);
                //Submitting the updated list to the Adapter
                submitList(mProductSupplierSalesList);
                //Returning True when done
                return true;
            }
            //Returning False when we do not have the last removed Supplier Sales data
            return false;
        }


        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnFocusChangeListener {
            //The TextView that displays the Supplier Name and Code
            private TextView mTextViewSupplierNameCode;
            //The TextView that displays the Supplier Selling Price
            private TextView mTextViewSupplierPrice;
            //The TextView that displays Out Of Stock Alert
            private TextView mTextViewOutOfStockAlert;
            //The EditText that captures the Available Quantity from the Supplier
            private EditText mEditTextAvailableQuantity;


            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewSupplierNameCode = itemView.findViewById(R.id.text_sales_config_item_supplier_name_code);
                mTextViewSupplierPrice = itemView.findViewById(R.id.text_sales_config_item_supplier_selling_price);
                mTextViewOutOfStockAlert = itemView.findViewById(R.id.text_sales_config_item_supplier_out_of_stock_alert);
                mEditTextAvailableQuantity = itemView.findViewById(R.id.edittext_sales_config_item_supplier_qty);

                //Registering Click Listeners on views
                itemView.findViewById(R.id.imgbtn_sales_config_item_supplier_increase_qty).setOnClickListener(this);
                itemView.findViewById(R.id.imgbtn_sales_config_item_supplier_decrease_qty).setOnClickListener(this);
                itemView.findViewById(R.id.btn_sales_config_item_supplier_edit).setOnClickListener(this);
                itemView.findViewById(R.id.btn_sales_config_item_supplier_procure).setOnClickListener(this);

                //Registering Focus Listener on EditText
                mEditTextAvailableQuantity.setOnFocusChangeListener(this);
            }

            @Override
            public void onClick(View view) {
                //Get the current adapter position
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the item position is valid

                    //Get the data at the position
                    ProductSupplierSales productSupplierSales = getItem(adapterPosition);

                    //Taking action based on the view clicked
                    switch (view.getId()) {
                        case R.id.imgbtn_sales_config_item_supplier_increase_qty:
                            //For the Increase Quantity ImageButton
                        {
                            //Trigger focus loss before proceeding
                            triggerFocusLost();

                            //Grab the availability value from the EditText
                            String availableQtyStr = mEditTextAvailableQuantity.getText().toString().trim();

                            if (!TextUtils.isEmpty(availableQtyStr)) {
                                //When we have the Supplier available quantity

                                //Parse the current available quantity value
                                int currentAvailableQuantity = Integer.parseInt(availableQtyStr);

                                //Increment the value
                                int updatedAvailableQuantity = currentAvailableQuantity + 1;

                                //Update it to the EditText and to the data at the position
                                mEditTextAvailableQuantity.setText(String.valueOf(updatedAvailableQuantity));
                                productSupplierSales.setAvailableQuantity(updatedAvailableQuantity);

                                //Update the change in Available Quantity to the Listener
                                mActionsListener.onChangeInAvailability(updatedAvailableQuantity - currentAvailableQuantity);

                                //Set the "Out of Stock!" visibility based on the updated Available Quantity
                                setOutOfStockAlertVisibility(updatedAvailableQuantity);
                            }
                        }
                        break;
                        case R.id.imgbtn_sales_config_item_supplier_decrease_qty:
                            //For the Decrease Quantity ImageButton
                        {
                            //Trigger focus loss before proceeding
                            triggerFocusLost();

                            //Grab the availability value from the EditText
                            String availableQtyStr = mEditTextAvailableQuantity.getText().toString().trim();

                            if (!TextUtils.isEmpty(availableQtyStr)) {
                                //When we have the Supplier available quantity

                                //Parse the current available quantity value
                                int currentAvailableQuantity = Integer.parseInt(availableQtyStr);

                                //Decrement the value
                                int updatedAvailableQuantity = currentAvailableQuantity - 1;

                                //Update it to the EditText and to the data at the position
                                //when the updated value is valid (equal to or greater than 0)
                                if (updatedAvailableQuantity >= 0) {
                                    mEditTextAvailableQuantity.setText(String.valueOf(updatedAvailableQuantity));
                                    productSupplierSales.setAvailableQuantity(updatedAvailableQuantity);

                                    //Update the change in Available Quantity to the Listener
                                    mActionsListener.onChangeInAvailability(updatedAvailableQuantity - currentAvailableQuantity);

                                    //Set the "Out of Stock!" visibility based on the updated Available Quantity
                                    setOutOfStockAlertVisibility(updatedAvailableQuantity);
                                }
                            }
                        }
                        break;
                        case R.id.btn_sales_config_item_supplier_edit:
                            //For the "Edit" button

                            //Dispatch the event to the action listener
                            mActionsListener.onEditSupplier(adapterPosition, productSupplierSales);
                            break;
                        case R.id.btn_sales_config_item_supplier_procure:
                            //For the "Procure" button

                            //Dispatch the event to the action listener
                            mActionsListener.onProcure(adapterPosition, productSupplierSales);
                            break;
                    }
                }
            }


            private void setOutOfStockAlertVisibility(int availableQuantity) {
                if (availableQuantity > 0) {
                    //When we have the Quantity, hide the "Out of Stock!" alert
                    mTextViewOutOfStockAlert.setVisibility(View.GONE);
                } else {
                    //When there is NO Quantity, show the "Out of Stock!" alert
                    mTextViewOutOfStockAlert.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //Get the current adapter position
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the item position is valid

                    //Get the data at the position
                    ProductSupplierSales productSupplierSales = getItem(adapterPosition);

                    if (!hasFocus) {
                        //When the registered View has lost focus

                        //Clear the view reference
                        mLastFocusedView = null;

                        //Take action based on the id of the view which lost focus
                        switch (view.getId()) {
                            case R.id.edittext_sales_config_item_supplier_qty:
                                //For the EditText View that displays the Supplier Available Quantity

                                //Grab the value from EditText and update it to the current ProductSupplierSales
                                String availableQtyStr = mEditTextAvailableQuantity.getText().toString().trim();

                                //Get the previous Available Quantity value
                                int oldAvailableQuantity = productSupplierSales.getAvailableQuantity();

                                //Parse the updated Available Quantity value
                                int updatedAvailableQuantity = 0; //Initializing to 0
                                if (!TextUtils.isEmpty(availableQtyStr)) {
                                    //Read the value only when present
                                    updatedAvailableQuantity = Integer.parseInt(availableQtyStr);
                                } else {
                                    //Update the '0' Available Quantity value to EditText, when there was no value
                                    mEditTextAvailableQuantity.setText(String.valueOf(updatedAvailableQuantity));
                                }

                                //Resetting the updated Available Quantity value to 0 when negative
                                if (updatedAvailableQuantity < 0) {
                                    updatedAvailableQuantity = 0;
                                    mEditTextAvailableQuantity.setText(String.valueOf(updatedAvailableQuantity));
                                }

                                //Update it to the current ProductSupplierSales
                                productSupplierSales.setAvailableQuantity(updatedAvailableQuantity);

                                //Set the "Out of Stock!" visibility based on the updated Available Quantity
                                setOutOfStockAlertVisibility(updatedAvailableQuantity);

                                //Update the change in Available Quantity to the Listener
                                mActionsListener.onChangeInAvailability(updatedAvailableQuantity - oldAvailableQuantity);

                                break;
                        }

                    } else {
                        //When the registered view has gained focus

                        //Save the view reference
                        mLastFocusedView = view;
                    }
                }
            }


            void bind(ProductSupplierSales productSupplierSales) {
                if (productSupplierSales != null) {
                    //When we have the details

                    //Get the Resources
                    Resources resources = itemView.getContext().getResources();

                    //Bind the Supplier Name and Code
                    mTextViewSupplierNameCode.setText(resources.getString(R.string.sales_list_item_supplier_name_code_format,
                            productSupplierSales.getSupplierName(), productSupplierSales.getSupplierCode()));

                    //Bind the Supplier Selling Price
                    mTextViewSupplierPrice.setText(resources.getString(R.string.sales_config_item_supplier_selling_price,
                            productSupplierSales.getUnitPrice() + " " + Currency.getInstance(Locale.getDefault()).getCurrencyCode()));

                    //Bind the Available Quantity
                    int availableQuantity = productSupplierSales.getAvailableQuantity();
                    mEditTextAvailableQuantity.setText(String.valueOf(availableQuantity));

                    //Set the Visibility of "Out of Stock!" Alert based
                    //on the Available Quantity value
                    setOutOfStockAlertVisibility(availableQuantity);
                }
            }
        }
    }


    private class ProductSupplierItemUserActionsListener implements ProductSuppliersUserActionsListener {


        @Override
        public void onEditSupplier(int itemPosition, ProductSupplierSales productSupplierSales) {
            //Delegating to the Presenter to handle the event
            mPresenter.editSupplier(productSupplierSales.getSupplierId());
        }

        @Override
        public void onSwiped(int itemPosition, ProductSupplierSales productSupplierSales) {
            //Delegating to the Presenter to handle the event
            mPresenter.onProductSupplierSwiped(productSupplierSales.getSupplierCode());
        }


        @Override
        public void onProcure(int itemPosition, ProductSupplierSales productSupplierSales) {
            //Delegating to the Presenter to handle the event
            mPresenter.procureProduct(productSupplierSales);
        }


        @Override
        public void onUpdatedAvailability(int totalAvailableQuantity) {
            //Delegating to the Presenter to handle the event
            mPresenter.updateAvailability(totalAvailableQuantity);
        }

        @Override
        public void onChangeInAvailability(int changeInAvailableQuantity) {
            //Delegating to the Presenter to handle the event
            mPresenter.changeAvailability(changeInAvailableQuantity);
        }
    }
}
