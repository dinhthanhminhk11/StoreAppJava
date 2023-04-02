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

package com.example.kaushiknsanji.storeapp.ui.products.config;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageActivity;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;

import java.util.ArrayList;
import java.util.List;


public class ProductConfigActivityFragment extends Fragment
        implements ProductConfigContract.View, View.OnClickListener, View.OnFocusChangeListener {

    //Constant used for logs
    private static final String LOG_TAG = ProductConfigActivityFragment.class.getSimpleName();

    //The Bundle argument constant of this Fragment
    private static final String ARGUMENT_INT_PRODUCT_ID = "argument.PRODUCT_ID";

    //Bundle constants for persisting the data throughout System config changes
    private static final String BUNDLE_PRODUCT_ID_INT_KEY = "ProductConfig.ProductId";
    private static final String BUNDLE_CATEGORY_SELECTED_STR_KEY = "ProductConfig.Category";
    private static final String BUNDLE_CATEGORY_OTHER_STR_KEY = "ProductConfig.CategoryOther";
    private static final String BUNDLE_PRODUCT_ATTRS_LIST_KEY = "ProductConfig.Attributes";
    private static final String BUNDLE_PRODUCT_IMAGES_LIST_KEY = "ProductConfig.Images";
    private static final String BUNDLE_EXISTING_PRODUCT_RESTORED_BOOL_KEY = "ProductConfig.IsExistingProductRestored";
    private static final String BUNDLE_PRODUCT_SKU_VALID_BOOL_KEY = "ProductConfig.IsProductSkuValid";
    private static final String BUNDLE_PRODUCT_NAME_ENTERED_BOOL_KEY = "ProductConfig.IsProductNameEntered";

    //The Presenter for this View
    private ProductConfigContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private EditText mEditTextProductName;
    private TextInputLayout mTextInputProductSku;
    private TextInputEditText mEditTextProductSku;
    private TextInputEditText mEditTextProductDescription;
    private Spinner mSpinnerProductCategory;
    private EditText mEditTextProductCategoryOther;
    private RecyclerView mRecyclerViewProductAttrs;

    //Saves the Focus Change Listener registered view that had focus before save operation
    private View mLastRegisteredFocusChangeView;

    //Adapter for the Category Spinner
    private ArrayAdapter<String> mCategorySpinnerAdapter;

    //RecyclerView Adapter for the Additional Product Attributes
    private ProductAttributesAdapter mProductAttributesAdapter;

    //Stores the Product ID for an Edit request, retrieved from Bundle arguments passed
    private int mProductId;
    //Stores the Category selected
    private String mCategoryLastSelected;
    //Stores the Text for Category Other option
    private String mCategoryOtherText;
    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores the state of Existing Product details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsExistingProductRestored;

    //Stores whether the Product SKU entered is valid or not
    private boolean mIsProductSkuValid;

    //Stores whether the Product Name was entered or not.
    //Used for monitoring unsaved progress
    private boolean mIsProductNameEntered;

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
                    //Start saving the Product Entry
                    saveProduct();
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


    public ProductConfigActivityFragment() {
    }


    public static ProductConfigActivityFragment newInstance(int productId) {
        //Saving the arguments passed, in a Bundle: START
        Bundle args = new Bundle(1);
        args.putInt(ARGUMENT_INT_PRODUCT_ID, productId);
        //Saving the arguments passed, in a Bundle: END

        //Instantiating the Fragment
        ProductConfigActivityFragment fragment = new ProductConfigActivityFragment();
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
        //Inflate the layout 'R.layout.fragment_product_config' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_product_config, container, false);

        //Finding the views to initialize
        mEditTextProductName = rootView.findViewById(R.id.edittext_product_config_name);
        mTextInputProductSku = rootView.findViewById(R.id.textinput_product_config_sku);
        mEditTextProductSku = rootView.findViewById(R.id.edittext_product_config_sku);
        mEditTextProductDescription = rootView.findViewById(R.id.edittext_product_config_description);
        mSpinnerProductCategory = rootView.findViewById(R.id.spinner_product_config_category);
        mEditTextProductCategoryOther = rootView.findViewById(R.id.edittext_product_config_category_other);
        mRecyclerViewProductAttrs = rootView.findViewById(R.id.recyclerview_product_config_attrs);

        //Registering the Focus Change Listener on the Product Name field
        mEditTextProductName.setOnFocusChangeListener(this);
        //Registering the Focus Change Listener on Product SKU field
        mEditTextProductSku.setOnFocusChangeListener(this);
        //Registering the Focus Change Listener on Category Other Text Field
        mEditTextProductCategoryOther.setOnFocusChangeListener(this);

        //Attaching a TextWatcher for the Product SKU field
        mEditTextProductSku.addTextChangedListener(new ProductSkuTextWatcher());

        //Retrieving the Product Id from the Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            mProductId = arguments.getInt(ARGUMENT_INT_PRODUCT_ID, ProductConfigContract.NEW_PRODUCT_INT);
        }

        //Setting Click Listeners for the Views
        rootView.findViewById(R.id.btn_product_config_add_attrs).setOnClickListener(this);

        //Initialize Category Spinner
        setupCategorySpinner();

        //Initialize RecyclerView for Product Attributes
        setupProductAttrsRecyclerView();

        //Returning the prepared view
        return rootView;
    }

    private void setupProductAttrsRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewProductAttrs.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mProductAttributesAdapter = new ProductAttributesAdapter();
        mPresenter.updateProductAttributes(null);

        //Setting the Adapter on RecyclerView
        mRecyclerViewProductAttrs.setAdapter(mProductAttributesAdapter);
    }

    private void setupCategorySpinner() {
        //Creating an Empty Category list
        List<String> categories = new ArrayList<>();

        //Creating Adapter for Spinner with a Spinner layout, passing in the empty list
        mCategorySpinnerAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_product_config_category_spinner, categories);
        //Specifying the Drop down layout to use for choices
        mCategorySpinnerAdapter.setDropDownViewResource(R.layout.item_product_config_category_spinner_dropdown);

        //Attaching Adapter to Spinner
        mSpinnerProductCategory.setAdapter(mCategorySpinnerAdapter);

        //Setting the Item selected listener
        mSpinnerProductCategory.setOnItemSelectedListener(new CategorySpinnerClickListener());
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Inflating the Menu options from 'R.menu.menu_fragment_product_config'
        inflater.inflate(R.menu.menu_fragment_product_config, menu);

        if (mProductId == ProductConfigContract.NEW_PRODUCT_INT) {
            //For a New Product Entry, "Delete" Action Menu needs to be hidden and disabled

            //Finding the Delete Action Menu Item
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            //Hiding the Menu Item
            deleteMenuItem.setVisible(false);
            //Disabling the Menu Item
            deleteMenuItem.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling based on the Menu item selected
        switch (item.getItemId()) {
            case R.id.action_delete:
                //On Click of Delete Menu (applicable for an Existing Product entry only)

                //Delegating to the Presenter to show the re-confirmation dialog
                mPresenter.showDeleteProductDialog();
                return true;
            case R.id.action_save:
                //On Click of Save Menu

                //Saving the Product and its details
                saveProduct();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveProduct() {
        //Delegating to the Presenter to trigger focus loss on listener registered Views,
        //in order to persist their data
        mPresenter.triggerFocusLost();

        //Retrieving the data from the views and the adapter
        String productName = mEditTextProductName.getText().toString().trim();
        String productSku = mEditTextProductSku.getText().toString().trim();
        String productDescription = mEditTextProductDescription.getText().toString().trim();
        ArrayList<ProductAttribute> productAttributes = mProductAttributesAdapter.getProductAttributes();
        mCategoryOtherText = mEditTextProductCategoryOther.getText().toString().trim();

        //Delegating to the Presenter to initiate the Save process
        mPresenter.onSave(productName,
                productSku,
                productDescription,
                mCategoryLastSelected,
                mCategoryOtherText,
                productAttributes
        );
    }

    public void triggerFocusLost() {
        //Clearing focus on the last registered view
        if (mLastRegisteredFocusChangeView != null) {
            mLastRegisteredFocusChangeView.clearFocus();
            mLastRegisteredFocusChangeView = null;
        }

        //Clearing focus on the last registered view in Product Attributes RecyclerView
        if (mProductAttributesAdapter != null) {
            mProductAttributesAdapter.triggerFocusLost();
        }
    }

    @Override
    public void showUpdateImagesSuccess() {
        if (getView() != null) {
            Snackbar.make(getView(),
                    getString(R.string.product_config_update_item_images_success, mEditTextProductSku.getText()),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showCategoryOtherEditTextField() {
        mEditTextProductCategoryOther.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideCategoryOtherEditTextField() {
        mEditTextProductCategoryOther.setVisibility(View.INVISIBLE);
    }

    @Override
    public void clearCategoryOtherEditTextField() {
        mEditTextProductCategoryOther.setText("");
    }

    @Override
    public void showEmptyFieldsValidationError() {
        showError(R.string.product_config_empty_fields_validation_error);
    }

    @Override
    public void showAttributesPartialValidationError() {
        showError(R.string.product_config_attrs_partial_empty_fields_validation_error);
    }

    @Override
    public void showAttributeNameConflictError(String attributeName) {
        showError(R.string.product_config_attrs_name_conflict_error, attributeName);
    }

    @Override
    public void showProgressIndicator(@StringRes int statusTextId) {
        ProgressDialogFragment.showDialog(getChildFragmentManager(), getString(statusTextId));
    }


    @Override
    public void hideProgressIndicator() {
        ProgressDialogFragment.dismissDialog(getChildFragmentManager());
    }


    @Override
    public void showError(@StringRes int messageId, @Nullable Object... args) {
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
    public void showProductSkuConflictError() {
        //Request the user to try a different SKU
        //Show error on the EditText
        mTextInputProductSku.setError(getString(R.string.product_config_sku_invalid_error));
    }

    @Override
    public void showProductSkuEmptyError() {
        //Show error on the EditText
        mTextInputProductSku.setError(getString(R.string.product_config_sku_empty_error));
    }


    @Override
    public void updateProductNameField(String name) {
        mEditTextProductName.setText(name);
    }

    @Override
    public void updateProductSkuField(String sku) {
        mEditTextProductSku.setText(sku);
    }


    @Override
    public void lockProductSkuField() {
        mTextInputProductSku.setEnabled(false);
    }


    @Override
    public void updateProductDescriptionField(String description) {
        mEditTextProductDescription.setText(description);
    }

    @Override
    public void updateCategorySelection(String selectedCategory, @Nullable String categoryOtherText) {
        //Saving the Category selected
        mCategoryLastSelected = selectedCategory;
        //Updating Category Spinner to show the Selected Product Category
        mSpinnerProductCategory.setSelection(mCategorySpinnerAdapter.getPosition(selectedCategory));
        if (!TextUtils.isEmpty(categoryOtherText)) {
            //If Manually entered Category is present,
            //then update the same to the corresponding EditText field
            mEditTextProductCategoryOther.setText(categoryOtherText);
        }
    }

    @Override
    public void syncExistingProductState(boolean isExistingProductRestored) {
        //Saving the state
        mIsExistingProductRestored = isExistingProductRestored;
    }


    @Override
    public void syncProductSkuValidity(boolean isProductSkuValid) {
        //Saving the state
        mIsProductSkuValid = isProductSkuValid;
    }


    @Override
    public void syncProductNameEnteredState(boolean isProductNameEntered) {
        //Saving the state
        mIsProductNameEntered = isProductNameEntered;
    }


    @Override
    public void showDiscardDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive, neutral and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.product_config_unsaved_changes_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(R.string.product_config_unsaved_changes_dialog_positive_text, mUnsavedDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(R.string.product_config_unsaved_changes_dialog_negative_text, mUnsavedDialogOnClickListener);
        //Set the Neutral Button and its listener
        builder.setNeutralButton(R.string.product_config_unsaved_changes_dialog_neutral_text, mUnsavedDialogOnClickListener);
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
        builder.setMessage(R.string.product_config_delete_product_confirm_dialog_message);
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
    public void updateProductAttributes(ArrayList<ProductAttribute> productAttributes) {
        mProductAttributesAdapter.replaceProductAttributes(productAttributes);
    }


    @Override
    public void updateProductImages(ArrayList<ProductImage> productImages) {
        //Saving the list of ProductImages
        mProductImages = productImages;
    }


    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            //When a View has lost focus

            //Clear the View reference since the View has lost focus
            mLastRegisteredFocusChangeView = null;

            //Taking action based on the Id of the View that has lost focus
            switch (view.getId()) {
                case R.id.edittext_product_config_name:
                    //Update to the Presenter to notify that the Product Name has been entered
                    mPresenter.updateAndSyncProductNameEnteredState(!TextUtils.isEmpty(mEditTextProductName.getText().toString().trim()));
                    break;
                case R.id.edittext_product_config_sku:
                    //Validate the Product SKU entered, only for a New Product Entry
                    if (mProductId == ProductConfigContract.NEW_PRODUCT_INT) {
                        mPresenter.validateProductSku(mEditTextProductSku.getText().toString().trim());
                    }
                    break;
                case R.id.edittext_product_config_category_other:
                    //Validate and Update the Category fields
                    mPresenter.updateCategorySelection(mCategoryLastSelected, mEditTextProductCategoryOther.getText().toString());
                    break;
            }

        } else {
            //When a View had gained focus

            //Save the reference of the View in focus
            mLastRegisteredFocusChangeView = view;
        }
    }


    @Override
    public void setPresenter(ProductConfigContract.Presenter presenter) {
        mPresenter = presenter;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //On Subsequent launch

            //Restoring Product Id which will be present if this is an Edit request
            mProductId = savedInstanceState.getInt(BUNDLE_PRODUCT_ID_INT_KEY);

            //Restoring Category related data
            //(Selection will be restored after Categories are downloaded)
            mCategoryLastSelected = savedInstanceState.getString(BUNDLE_CATEGORY_SELECTED_STR_KEY);
            mCategoryOtherText = savedInstanceState.getString(BUNDLE_CATEGORY_OTHER_STR_KEY);

            //Restoring Product Attributes
            mPresenter.updateProductAttributes(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_ATTRS_LIST_KEY));

            //Restoring Product Images
            mPresenter.updateProductImages(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_IMAGES_LIST_KEY));

            //Restoring the state of Product Name Entered
            mPresenter.updateAndSyncProductNameEnteredState(savedInstanceState.getBoolean(BUNDLE_PRODUCT_NAME_ENTERED_BOOL_KEY,
                    false));

            //Restoring the state of Existing Product data being last restored
            //if this was an Edit request
            mPresenter.updateAndSyncExistingProductState(savedInstanceState.getBoolean(BUNDLE_EXISTING_PRODUCT_RESTORED_BOOL_KEY,
                    false));

            //Restoring the state of Product SKU Validation
            mPresenter.updateAndSyncProductSkuValidity(savedInstanceState.getBoolean(BUNDLE_PRODUCT_SKU_VALID_BOOL_KEY,
                    false));

        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Trigger Focus Loss to capture any value partially entered
        mPresenter.triggerFocusLost();

        //Saving the state
        outState.putInt(BUNDLE_PRODUCT_ID_INT_KEY, mProductId);
        outState.putString(BUNDLE_CATEGORY_SELECTED_STR_KEY, mCategoryLastSelected);
        outState.putString(BUNDLE_CATEGORY_OTHER_STR_KEY, mEditTextProductCategoryOther.getText().toString());
        outState.putParcelableArrayList(BUNDLE_PRODUCT_ATTRS_LIST_KEY, mProductAttributesAdapter.getProductAttributes());
        outState.putParcelableArrayList(BUNDLE_PRODUCT_IMAGES_LIST_KEY, mProductImages);
        outState.putBoolean(BUNDLE_PRODUCT_NAME_ENTERED_BOOL_KEY, mIsProductNameEntered);
        outState.putBoolean(BUNDLE_EXISTING_PRODUCT_RESTORED_BOOL_KEY, mIsExistingProductRestored);
        outState.putBoolean(BUNDLE_PRODUCT_SKU_VALID_BOOL_KEY, mIsProductSkuValid);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Start the work
        mPresenter.start();
    }


    @Override
    public void updateCategories(List<String> categories) {
        //Update the Category Spinner with the new data
        mCategorySpinnerAdapter.clear();
        //Add the Prompt as the first item
        categories.add(0, mSpinnerProductCategory.getPrompt().toString());
        mCategorySpinnerAdapter.addAll(categories);
        //Trigger data change event
        mCategorySpinnerAdapter.notifyDataSetChanged();

        if (!TextUtils.isEmpty(mCategoryLastSelected)) {
            //Validate and Update the Category selection if previously selected
            mPresenter.updateCategorySelection(mCategoryLastSelected, mCategoryOtherText);
        }
    }

    @Override
    public void onClick(View view) {
        //Executing based on the View Id
        switch (view.getId()) {
            case R.id.btn_product_config_add_attrs:
                //For "Add More" button, present under Additional Attributes

                //Add an Empty row for capturing Additional Attribute
                mProductAttributesAdapter.addEmptyRecord();
                break;
        }
    }


    private static class ProductAttributesAdapter extends RecyclerView.Adapter<ProductAttributesAdapter.ViewHolder> {

        //Stores the EditText View that had last acquired focus
        private View mLastFocusedView;

        //The Data of this Adapter
        private ArrayList<ProductAttribute> mProductAttributes;


        ProductAttributesAdapter() {
            //Initialize the Product Attribute List
            mProductAttributes = new ArrayList<>();
        }


        @NonNull
        @Override
        public ProductAttributesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item Layout view 'R.layout.item_product_config_attr'
            //Passing False as we are attaching the View ourselves
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_config_attr, parent, false);

            //Instantiating and returning the ViewHolder to cache reference to the view components in the item layout
            return new ViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull ProductAttributesAdapter.ViewHolder holder, int position) {
            //Binding the data at the position
            holder.bind(mProductAttributes.get(position));
        }


        @Override
        public int getItemCount() {
            return mProductAttributes.size();
        }


        void addEmptyRecord() {
            //Create an Empty ProductAttribute
            ProductAttribute productAttribute = new ProductAttribute.Builder()
                    .createProductAttribute();
            //Add it to adapter list
            mProductAttributes.add(productAttribute);
            //Notify the new item added to the end of the list, to show the empty record
            notifyItemInserted(mProductAttributes.size() - 1);
        }


        void replaceProductAttributes(ArrayList<ProductAttribute> productAttributes) {
            if (productAttributes != null && productAttributes.size() >= 0) {
                //When there is data in the new list submitted

                //Clear the current list of Product Attributes
                mProductAttributes.clear();
                //Load the new list
                mProductAttributes.addAll(productAttributes);
                //Notify that there is a new list of Product Attributes to be shown
                notifyDataSetChanged();
            }

            if (mProductAttributes.size() == 0) {
                //Add an Empty Record to the list when there is no record present
                addEmptyRecord();
            }
        }

        ArrayList<ProductAttribute> getProductAttributes() {
            return mProductAttributes;
        }


        void triggerFocusLost() {
            if (mLastFocusedView != null) {
                mLastFocusedView.clearFocus();
                mLastFocusedView = null;
            }
        }


        void deleteRecord(int position) {
            //Validating the item position passed
            if (position > RecyclerView.NO_POSITION) {
                //When the item position passed is valid

                //Remove the ProductAttribute at the position specified
                mProductAttributes.remove(position);
                //Notify Item position removed
                notifyItemRemoved(position);
            }
        }


        class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnFocusChangeListener {
            //The EditText that records/displays the Product Attribute Name
            private TextInputEditText mEditTextAttrName;
            //The EditText that records/displays the Product Attribute Value
            private TextInputEditText mEditTextAttrValue;
            //The ImageButton for "Remove" Action
            private ImageButton mImageButtonRemoveAction;


            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mEditTextAttrName = itemView.findViewById(R.id.edittext_item_product_config_attr_name);
                mEditTextAttrValue = itemView.findViewById(R.id.edittext_item_product_config_attr_value);
                mImageButtonRemoveAction = itemView.findViewById(R.id.imgbtn_item_product_config_attr_remove);

                //Registering Focus Change Listeners on TextInput Fields to capture the updated value
                mEditTextAttrName.setOnFocusChangeListener(this);
                mEditTextAttrValue.setOnFocusChangeListener(this);

                //Setting Click Listener on ImageButton
                mImageButtonRemoveAction.setOnClickListener(this);
            }

            void bind(ProductAttribute productAttribute) {
                //Set the Attribute Name
                mEditTextAttrName.setText(productAttribute.getAttributeName());
                //Set the Attribute Value
                mEditTextAttrValue.setText(productAttribute.getAttributeValue());
            }


            @Override
            public void onClick(View view) {
                //Get the current adapter position
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the item position is valid

                    //Take action based on the id of the View being clicked
                    if (view.getId() == R.id.imgbtn_item_product_config_attr_remove) {
                        //For the Remove Action ImageButton

                        //Delete the record at the position clicked
                        deleteRecord(adapterPosition);
                    }
                }
            }

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //Get the current adapter position
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the item position is valid

                    //Get the data at the position
                    ProductAttribute productAttribute = mProductAttributes.get(adapterPosition);

                    if (!hasFocus) {
                        //When the registered View has lost focus

                        //Clear the view reference
                        mLastFocusedView = null;

                        //Take action based on the id of the view which lost focus
                        switch (view.getId()) {
                            case R.id.edittext_item_product_config_attr_name:
                                //For the EditText that records/displays the Attribute Name

                                //Grab the Attribute Name from the EditText and update it to the current ProductAttribute data
                                productAttribute.setAttributeName(mEditTextAttrName.getText().toString().trim());
                                break;
                            case R.id.edittext_item_product_config_attr_value:
                                //For the EditText that records/displays the Attribute Value

                                //Grab the Attribute Value from the EditText and update it to the current ProductAttribute data
                                productAttribute.setAttributeValue(mEditTextAttrValue.getText().toString().trim());
                                break;
                        }
                    } else {
                        //When the registered view has gained focus

                        //Save the view reference
                        mLastFocusedView = view;
                    }
                }
            }

        }

    }


    private class ProductSkuTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Clear the error on EditText if any
            mTextInputProductSku.setError(null);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //no-op
        }


        @Override
        public void afterTextChanged(Editable s) {
            //no-op
        }
    }


    private class CategorySpinnerClickListener implements Spinner.OnItemSelectedListener {


        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //NOTE: Position 0 is reserved for the manually added Prompt
            if (position > 0) {
                //Retrieving the Category Name of the selection
                mCategoryLastSelected = parent.getItemAtPosition(position).toString();
                //Calling the Presenter method to take appropriate action
                //(For showing/hiding the EditText field of Category OTHER)
                mPresenter.onCategorySelected(mCategoryLastSelected);
            } else {
                //On other cases, reset the Category Name saved
                mCategoryLastSelected = "";
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //No-op
        }
    }
}
