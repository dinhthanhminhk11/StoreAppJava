
package com.example.kaushiknsanji.storeapp.ui.suppliers.product;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.util.ArrayList;
import java.util.List;


public class SupplierProductPickerActivityFragment extends Fragment implements SupplierProductPickerContract.View {

    //Constant used for logs
    private static final String LOG_TAG = SupplierProductPickerActivityFragment.class.getSimpleName();

    //The Bundle argument constant of this Fragment
    private static final String ARGUMENT_LIST_SUPPLIER_PRODUCTS = "argument.SUPPLIER_PRODUCTS";

    //Bundle constants for persisting the data through System config changes
    private static final String BUNDLE_REMAINING_PRODUCTS_LIST_KEY = "SupplierProductPicker.RemainingProducts";
    private static final String BUNDLE_SELECTED_PRODUCTS_LIST_KEY = "SupplierProductPicker.SelectedProducts";

    //The Presenter for this View
    private SupplierProductPickerContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private RecyclerView mRecyclerViewProducts;
    private TextView mTextViewEmptyList;

    //The RecyclerView Adapter to display the Products
    private ProductListAdapter mProductListAdapter;

    //Stores the list of Products already registered by the Supplier for selling
    private ArrayList<ProductLite> mRegisteredProducts;

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
                    //Start saving the selected list of Products
                    saveSelectedProducts();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //For "Discard" button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Dispatch to the Presenter to finish the Activity
                    mPresenter.finishActivity();
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    //For "Stay Here" button

                    //Just Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    break;
            }
        }
    };

    public SupplierProductPickerActivityFragment() {
    }

    public static SupplierProductPickerActivityFragment newInstance(ArrayList<ProductLite> supplierProducts) {
        //Saving the arguments passed, in a Bundle: START
        Bundle args = new Bundle(1);
        args.putParcelableArrayList(ARGUMENT_LIST_SUPPLIER_PRODUCTS, supplierProducts);
        //Saving the arguments passed, in a Bundle: END

        //Instantiating the Fragment
        SupplierProductPickerActivityFragment fragment = new SupplierProductPickerActivityFragment();
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
        //Inflate the layout 'R.layout.fragment_supplier_product_picker' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_supplier_product_picker, container, false);

        //Finding the Views to initialize
        mTextViewEmptyList = rootView.findViewById(R.id.text_supplier_product_picker_empty_list);
        mRecyclerViewProducts = rootView.findViewById(R.id.recyclerview_supplier_product_picker);

        //Reading the list of registered products from the Arguments Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            mRegisteredProducts = arguments.getParcelableArrayList(ARGUMENT_LIST_SUPPLIER_PRODUCTS);
        }

        //Initialize RecyclerView for Product List
        setupProductListRecyclerView();

        //Returning the prepared layout
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mProductListAdapter != null) {
            //Saving data when the Adapter is set

            //Save the Remaining list of Products data
            outState.putParcelableArrayList(BUNDLE_REMAINING_PRODUCTS_LIST_KEY, mProductListAdapter.getRemainingProducts());
            //Save the Selected list of Products data
            outState.putParcelableArrayList(BUNDLE_SELECTED_PRODUCTS_LIST_KEY, mProductListAdapter.getSelectedProducts());
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //On Subsequent launch

            //Restore the list of Remaining Products
            ArrayList<ProductLite> remainingProducts = savedInstanceState.getParcelableArrayList(BUNDLE_REMAINING_PRODUCTS_LIST_KEY);
            //Restore the list of Selected Products
            ArrayList<ProductLite> selectedProducts = savedInstanceState.getParcelableArrayList(BUNDLE_SELECTED_PRODUCTS_LIST_KEY);
            //Delegating to the Presenter to load the RecyclerView Adapter data
            mPresenter.loadProductsToPick(mRegisteredProducts, remainingProducts, selectedProducts);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Inflating the Menu options from 'R.menu.menu_fragment_supplier_product_picker'
        inflater.inflate(R.menu.menu_fragment_supplier_product_picker, menu);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Delegating to the Presenter to load the RecyclerView Adapter data
        mPresenter.loadProductsToPick(mRegisteredProducts, null, null);

    }

    @Override
    public void setPresenter(SupplierProductPickerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling based on the Menu item selected
        switch (item.getItemId()) {
            case R.id.action_save:
                //On click of "Save" menu

                //Start saving the selected list of Products
                saveSelectedProducts();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveSelectedProducts() {
        //Propagating the call to Presenter to begin the Save operation
        mPresenter.onSave(mRegisteredProducts, mProductListAdapter.getSelectedProducts());
    }

    private void setupProductListRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewProducts.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mProductListAdapter = new ProductListAdapter(requireContext(), new UserActionsListener());

        //Setting the Adapter on RecyclerView
        mRecyclerViewProducts.setAdapter(mProductListAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.supplier_product_list_items_spacing);

        //Setting Item offsets using Item Decoration
        mRecyclerViewProducts.addItemDecoration(new ListItemSpacingDecoration(itemSpacing, itemSpacing));

        //The size of Adapter items does not change the RecyclerView size
        mRecyclerViewProducts.setHasFixedSize(true);
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

    /**
     * Method invoked when we have the Product List. This should show the Product List and
     * hide the Empty List TextView.
     */
    @Override
    public void hideEmptyView() {
        //Displaying the RecyclerView
        mRecyclerViewProducts.setVisibility(View.VISIBLE);
        //Hiding the Empty List TextView
        mTextViewEmptyList.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyView(int emptyTextResId, @Nullable Object... args) {
        //Hiding the RecyclerView
        mRecyclerViewProducts.setVisibility(View.INVISIBLE);
        //Displaying the Empty List TextView
        mTextViewEmptyList.setVisibility(View.VISIBLE);

        //Evaluating the message to be shown
        String messageToBeShown;
        if (args != null && args.length > 0) {
            //For the String Resource with args
            messageToBeShown = getString(emptyTextResId, args);
        } else {
            //For the String Resource without args
            messageToBeShown = getString(emptyTextResId);
        }

        //Setting the Text to show
        mTextViewEmptyList.setText(messageToBeShown);
    }

    @Override
    public void submitDataToAdapter(ArrayList<ProductLite> remainingProducts, @Nullable ArrayList<ProductLite> selectedProducts) {
        //Submitting data to the Adapter
        mProductListAdapter.submitData(remainingProducts, selectedProducts);
    }

    @Override
    public void filterAdapterData(String searchQueryStr, Filter.FilterListener filterListener) {
        //Applying the filter on the Adapter to load results for "searchQueryStr"
        mProductListAdapter.getFilter().filter(searchQueryStr, filterListener);
    }

    @Override
    public void clearAdapterFilter(Filter.FilterListener filterListener) {
        //Clearing the filter applied on the Adapter
        mProductListAdapter.getFilter().filter(null, filterListener);
    }

    @Override
    public void showDiscardDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive, neutral and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.supplier_product_picker_unsaved_changes_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(R.string.supplier_product_picker_unsaved_changes_dialog_positive_text, mUnsavedDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(R.string.supplier_product_picker_unsaved_changes_dialog_negative_text, mUnsavedDialogOnClickListener);
        //Set the Neutral Button and its listener
        builder.setNeutralButton(R.string.supplier_product_picker_unsaved_changes_dialog_neutral_text, mUnsavedDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    private static class ProductListAdapter extends ListAdapter<ProductLite, ProductListAdapter.ViewHolder>
            implements Filterable {

        //Payload constants used to rebind the state of list items for the position stored here
        private static final String PAYLOAD_SELECTED_PRODUCT = "Payload.SelectedProductPosition";
        private static final String PAYLOAD_UNSELECTED_PRODUCT = "Payload.UnselectedProductPosition";

        private static DiffUtil.ItemCallback<ProductLite> DIFF_PRODUCTS
                = new DiffUtil.ItemCallback<ProductLite>() {

            @Override
            public boolean areItemsTheSame(ProductLite oldItem, ProductLite newItem) {
                //Returning the comparison of the Product's Id
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(ProductLite oldItem, ProductLite newItem) {
                //Returning the comparison of entire Product
                return oldItem.equals(newItem);
            }
        };
        //Stores the Typeface used for Product SKU text
        private Typeface mProductSkuTypeface;
        //Listener for the User actions on the Product List Items
        private SupplierProductPickerListUserActionsListener mActionsListener;
        //The Data of this Adapter
        private ArrayList<ProductLite> mRemainingProducts;
        //The List of Products selected by the Supplier for selling
        private ArrayList<ProductLite> mSelectedProducts;

        ProductListAdapter(Context context, SupplierProductPickerListUserActionsListener userActionsListener) {
            //Calling to Super with the DiffUtil to use
            super(DIFF_PRODUCTS);
            //Registering the User Actions Listener
            mActionsListener = userActionsListener;
            //Initializing the Selected Products List
            mSelectedProducts = new ArrayList<>();
            //Reading the Typeface for Product SKU
            mProductSkuTypeface = ResourcesCompat.getFont(context, R.font.libre_barcode_128_text_regular);
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_supplier_product_picker'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier_product_picker, parent, false);
            //Returning the Instance of ViewHolder for the inflated Item View
            return new ViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //Get the data at the position
            ProductLite productLite = getItem(position);

            //Bind the Views with the data at the position
            holder.bind(position, productLite);

            //Bind the item selection state based on whether
            //this Product is part of the selected product list or not
            holder.setSelected(mSelectedProducts.contains(productLite));
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                //Propagating to super when there are no payloads
                super.onBindViewHolder(holder, position, payloads);
            } else {
                //When we have a payload for partial update

                //Get the Payload bundle
                Bundle bundle = (Bundle) payloads.get(0);
                //Iterate over the bundle keys
                for (String keyStr : bundle.keySet()) {
                    switch (keyStr) {
                        case PAYLOAD_SELECTED_PRODUCT:
                            //For the selected product position

                            //Get the position from the bundle
                            int selectedProductPosition = bundle.getInt(keyStr, RecyclerView.NO_POSITION);
                            if (selectedProductPosition > RecyclerView.NO_POSITION
                                    && selectedProductPosition == position) {
                                //When the position is for the selected product, show the selected item state
                                holder.setSelected(true);
                            }
                            break;
                        case PAYLOAD_UNSELECTED_PRODUCT:
                            //For the unselected product position

                            //Get the position from the bundle
                            int unselectedProductPosition = bundle.getInt(keyStr, RecyclerView.NO_POSITION);
                            if (unselectedProductPosition > RecyclerView.NO_POSITION
                                    && unselectedProductPosition == position) {
                                //When the position is for the unselected product, reset the selected item state
                                holder.setSelected(false);
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public Filter getFilter() {
            //Defining and Returning a New Custom Filter
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    //Holds the results of the Filtering operation
                    FilterResults filterResults = new FilterResults();

                    if (TextUtils.isEmpty(constraint)) {
                        //When the constraint filter is absent, pass in the original unfiltered data
                        filterResults.values = mRemainingProducts;
                        filterResults.count = mRemainingProducts.size();
                    } else {
                        //When the constraint filter is present

                        //Trimming the constraint passed
                        constraint = constraint.toString().trim();

                        //Initializing an ArrayList of Products to store the Products matching the constraint
                        ArrayList<ProductLite> filteredProductList = new ArrayList<>();
                        //Iterating over the original unfiltered data to build the filtered list
                        for (ProductLite product : mRemainingProducts) {
                            //Get the Product Name
                            String productName = product.getName();
                            //Get the Product SKU
                            String productSku = product.getSku();
                            //Get the Category
                            String category = product.getCategory();

                            if (productName.contains(constraint) || productSku.contains(constraint)
                                    || category.contains(constraint)) {
                                //When the constraint is present in the Name/SKU/Category,
                                //add the Product to the filtered list
                                filteredProductList.add(product);
                            }
                        }
                        //Load the filtered list built
                        filterResults.values = filteredProductList;
                        filterResults.count = filteredProductList.size();
                    }
                    //Returning the results of the filtering operation
                    return filterResults;
                }


                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //Casting the results into a List of Products
                    ArrayList<ProductLite> filteredProductList = (ArrayList<ProductLite>) results.values;
                    //Submitting the list to be loaded
                    submitList(filteredProductList);
                }
            };
        }


        void submitData(ArrayList<ProductLite> remainingProducts,
                        @Nullable ArrayList<ProductLite> selectedProducts) {

            if (selectedProducts != null) {
                //When new selected products are passed

                //Clear the adapter list of selected products
                mSelectedProducts.clear();
                //Load the new list
                mSelectedProducts.addAll(selectedProducts);
            }

            //Load the list of products to be shown
            mRemainingProducts = remainingProducts;
            //Submitting the list to be loaded
            submitList(mRemainingProducts);
        }

        ArrayList<ProductLite> getRemainingProducts() {
            return mRemainingProducts;
        }

        ArrayList<ProductLite> getSelectedProducts() {
            return mSelectedProducts;
        }

        private void updateSelectList(int position, ProductLite productLite) {
            if (mSelectedProducts.contains(productLite)) {
                //When the list already contains the Product, remove from the list
                mSelectedProducts.remove(productLite);

                //Creating a Bundle to do a partial update, to reset the selected state
                Bundle payloadBundle = new Bundle(1);
                //Put the position of the Product unselected, in the Bundle for update
                payloadBundle.putInt(PAYLOAD_UNSELECTED_PRODUCT, position);
                //Notify the change at the position to reset the selected state
                notifyItemChanged(position, payloadBundle);
            } else {
                //When the list does not contain the Product yet, add to the list
                mSelectedProducts.add(productLite);
                //Creating a Bundle to do a partial update, to show the selected state
                Bundle payloadBundle = new Bundle(1);
                //Put the position of the Product selected, in the Bundle for update
                payloadBundle.putInt(PAYLOAD_SELECTED_PRODUCT, position);
                //Notify the change at the position to show the selected state
                notifyItemChanged(position, payloadBundle);
            }
            //Invoking the User Actions Listener for updating the count of selected Products
            mActionsListener.onItemClicked(position);
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //The TextView that displays the Product Name
            private TextView mTextViewProductName;
            //The ImageView that displays the Product Photo
            private ImageView mImageViewProductPhoto;
            //The TextView that displays the Product SKU
            private TextView mTextViewProductSku;
            //The TextView that displays the Product Category
            private TextView mTextViewProductCategory;
            //The ImageView that displays the selected/unselected state of the View
            private ImageView mImageViewSelect;

            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewProductName = itemView.findViewById(R.id.text_product_item_name);
                mImageViewProductPhoto = itemView.findViewById(R.id.image_product_item_photo);
                mTextViewProductSku = itemView.findViewById(R.id.text_product_item_sku);
                mTextViewProductCategory = itemView.findViewById(R.id.text_product_item_category);
                mImageViewSelect = itemView.findViewById(R.id.image_supplier_product_picker_item_select);

                //Registering the Click listener on the entire view
                itemView.setOnClickListener(this);
            }

            void bind(int position, ProductLite productLite) {
                //Bind the Product Name
                mTextViewProductName.setText(productLite.getName());
                //Bind the Product SKU
                mTextViewProductSku.setText(productLite.getSku());
                //Set Barcode typeface for the SKU
                mTextViewProductSku.setTypeface(mProductSkuTypeface);
                //Download and Bind the Product Photo at the position
                ImageDownloaderFragment.newInstance(
                        ((FragmentActivity) mImageViewProductPhoto.getContext()).getSupportFragmentManager(), position)
                        .executeAndUpdate(mImageViewProductPhoto, productLite.getDefaultImageUri(), position);
                //Bind the Product Category
                mTextViewProductCategory.setText(productLite.getCategory());
            }

            @Override
            public void onClick(View view) {
                //Checking if the adapter position is valid
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the adapter position is valid

                    //Get the data at the position
                    ProductLite productLite = getItem(adapterPosition);

                    //Get the View Id clicked
                    int clickedViewId = view.getId();

                    //Taking action based on the Id of the View clicked
                    if (clickedViewId == itemView.getId()) {
                        //When the entire item view is clicked
                        //Add/Remove the product to/from the select list
                        updateSelectList(adapterPosition, productLite);
                    }
                }
            }


            public void setSelected(boolean selected) {
                //Update the selection state on the ImageView
                mImageViewSelect.setSelected(selected);
            }
        }

    }


    private class UserActionsListener implements SupplierProductPickerListUserActionsListener {

        @Override
        public void onItemClicked(int itemPosition) {
            //Delegate to the Presenter to update the count of Products selected
            mPresenter.updateSelectedProductCount(mProductListAdapter.getSelectedProducts().size());
        }
    }
}