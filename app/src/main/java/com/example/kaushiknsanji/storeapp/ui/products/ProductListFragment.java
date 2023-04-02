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

package com.example.kaushiknsanji.storeapp.ui.products;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.ColorUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.util.ArrayList;


public class ProductListFragment extends Fragment
        implements ProductListContract.View, SwipeRefreshLayout.OnRefreshListener {

    //Constant used for logs
    private static final String LOG_TAG = ProductListFragment.class.getSimpleName();

    //The Presenter interface for this View
    private ProductListContract.Presenter mPresenter;

    //References to the Views shown in this Fragment
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewContentList;
    private Group mGroupEmptyList;

    //Adapter of the RecyclerView
    private ProductListAdapter mAdapter;

    public ProductListFragment() {
    }


    @NonNull
    public static ProductListFragment newInstance() {
        return new ProductListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflating the layout 'R.layout.layout_main_content_page'
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.layout_main_content_page, container, false);

        //Finding the Views
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_content_page);
        mRecyclerViewContentList = rootView.findViewById(R.id.recyclerview_content_page);
        TextView textViewEmptyList = rootView.findViewById(R.id.text_content_page_empty_list);
        ImageView imageViewStepNumber = rootView.findViewById(R.id.image_content_page_step_number);
        mGroupEmptyList = rootView.findViewById(R.id.group_content_page_empty);

        //Initialize the ImageView with the proper step number drawable
        imageViewStepNumber.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_main_product_page_number));

        //Initialize the Empty TextView with Text
        textViewEmptyList.setText(getString(R.string.product_list_empty_text));

        //Initialize SwipeRefreshLayout
        setupSwipeRefresh();

        //Initialize RecyclerView
        setupRecyclerView();

        //Returning the prepared layout
        return rootView;
    }

    private void setupSwipeRefresh() {
        //Registering the refresh listener which triggers a new load on swipe to refresh
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //Configuring the Colors for Swipe Refresh Progress Indicator
        mSwipeRefreshLayout.setColorSchemeColors(ColorUtility.obtainColorsFromTypedArray(requireContext(), R.array.swipe_refresh_colors, R.color.colorPrimary));
    }

    private void setupRecyclerView() {
        //Creating a Vertical Linear Layout Manager with the default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false) {

            @Override
            public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
                if (getChildCount() > 0 && itemCount == 1) {
                    //When there are some items visible and number of items added is 1

                    if (positionStart == getItemCount() - 1 && getItemCount() > 1) {
                        //When there are more than one Item View and the Item View
                        //added is in the last position

                        //Remove the previous Item View cache from RecyclerView to reload the Item View
                        //with proper item decoration height
                        removeView(findViewByPosition(positionStart - 1));
                    }
                }
            }


            @Override
            public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
                if (getChildCount() > 0 && itemCount == 1) {
                    //When there are some items visible and number of items added is 1

                    if (positionStart == getItemCount() && getItemCount() > 1) {
                        //When there are more than one Item View and the Item View
                        //removed is from the last position

                        //Remove the previous Item View cache from RecyclerView to reload the Item View
                        //with proper item decoration height
                        removeView(findViewByPosition(positionStart - 1));
                    }
                }

            }
        };

        //Setting the Layout Manager to use
        mRecyclerViewContentList.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter for the RecyclerView
        mAdapter = new ProductListAdapter(requireContext(), new UserActionsListener());

        //Setting the Adapter for RecyclerView
        mRecyclerViewContentList.setAdapter(mAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.product_list_items_spacing);

        //Setting Item offsets using Item Decoration
        mRecyclerViewContentList.addItemDecoration(new ListItemSpacingDecoration(
                itemSpacing, itemSpacing, true
        ));

    }


    @Override
    public void onResume() {
        super.onResume();

        //Start loading the Products
        mPresenter.start();
    }


    @Nullable
    @Override
    public ProductListContract.Presenter getPresenter() {
        return mPresenter;
    }


    @Override
    public void setPresenter(ProductListContract.Presenter presenter) {
        mPresenter = presenter;
    }


    @Override
    public void showProgressIndicator() {
        //Enabling the Swipe to Refresh if disabled prior to showing the Progress indicator
        if (!mSwipeRefreshLayout.isEnabled()) {
            mSwipeRefreshLayout.setEnabled(true);
        }
        //Displaying the Progress Indicator only when not already shown
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }


    @Override
    public void hideProgressIndicator() {
        //Hiding the Progress indicator
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void loadProducts(ArrayList<ProductLite> productList) {
        //Submitting the new updated list to the Adapter
        mAdapter.submitList(productList);
    }

    @Override
    public void launchEditProduct(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Creating the Intent to launch ProductConfigActivity
        Intent productConfigIntent = new Intent(requireContext(), ProductConfigActivity.class);
        //Passing in the Product ID of the Product to be edited
        productConfigIntent.putExtra(ProductConfigActivity.EXTRA_PRODUCT_ID, productId);
        //Starting the Activity with Result
        startActivityForResult(productConfigIntent, ProductConfigActivity.REQUEST_EDIT_PRODUCT, activityOptionsCompat.toBundle());
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
    public void showAddSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_add_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showUpdateSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_update_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showDeleteSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_delete_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showEmptyView() {
        //Hiding the RecyclerView
        mRecyclerViewContentList.setVisibility(View.INVISIBLE);
        //Displaying the Empty List TextView and Step Number Drawable
        mGroupEmptyList.setVisibility(View.VISIBLE);
        //Disabling the Swipe to Refresh
        mSwipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void hideEmptyView() {
        //Displaying the RecyclerView
        mRecyclerViewContentList.setVisibility(View.VISIBLE);
        //Hiding the Empty List TextView and Step Number Drawable
        mGroupEmptyList.setVisibility(View.GONE);
    }

    @Override
    public void launchAddNewProduct() {
        //Creating the Intent to launch ProductConfigActivity
        Intent productConfigIntent = new Intent(requireContext(), ProductConfigActivity.class);
        //Starting the Activity with Result
        startActivityForResult(productConfigIntent, ProductConfigActivity.REQUEST_ADD_PRODUCT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Delegating to the Presenter to handle
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRefresh() {
        //Forcefully start a new load
        mPresenter.triggerProductsLoad(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Dispatching the event to the Presenter to invalidate any critical resources
        mPresenter.releaseResources();
    }

    private static class ProductListAdapter extends ListAdapter<ProductLite, ProductListAdapter.ViewHolder> {

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
        private ProductListUserActionsListener mActionsListener;

        ProductListAdapter(Context context, ProductListUserActionsListener userActionsListener) {
            super(DIFF_PRODUCTS);
            //Registering the User Actions Listener
            mActionsListener = userActionsListener;
            //Reading the Typeface for Product SKU
            mProductSkuTypeface = ResourcesCompat.getFont(context, R.font.libre_barcode_128_text_regular);
        }

        @NonNull
        @Override
        public ProductListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_product_list'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_list, parent, false);
            //Returning the Instance of ViewHolder for the inflated Item View
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductListAdapter.ViewHolder holder, int position) {
            //Get the data at the position
            ProductLite productLite = getItem(position);

            //Bind the Views with the data at the position
            holder.bind(position, productLite);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //References to the Views required, in the Item View
            private TextView mTextViewProductName;
            private ImageView mImageViewProductPhoto;
            private TextView mTextViewProductSku;
            private TextView mTextViewProductCategory;
            private Button mButtonDelete;
            private Button mButtonEdit;

            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewProductName = itemView.findViewById(R.id.text_product_item_name);
                mImageViewProductPhoto = itemView.findViewById(R.id.image_product_item_photo);
                mTextViewProductSku = itemView.findViewById(R.id.text_product_item_sku);
                mTextViewProductCategory = itemView.findViewById(R.id.text_product_item_category);
                mButtonDelete = itemView.findViewById(R.id.btn_product_list_item_delete);
                mButtonEdit = itemView.findViewById(R.id.btn_product_list_item_edit);

                //Registering the Click listeners on the required views
                mButtonDelete.setOnClickListener(this);
                mButtonEdit.setOnClickListener(this);
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

                    //Taking action based on the view clicked
                    if (clickedViewId == itemView.getId()
                            || clickedViewId == R.id.btn_product_list_item_edit) {
                        //When the entire Item View or the "Edit" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onEditProduct(adapterPosition, productLite, mImageViewProductPhoto);

                    } else if (clickedViewId == R.id.btn_product_list_item_delete) {
                        //When the "Delete" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDeleteProduct(adapterPosition, productLite);
                    }
                }
            }
        }
    }

    private class UserActionsListener implements ProductListUserActionsListener {


        @Override
        public void onEditProduct(final int itemPosition, ProductLite product, ImageView imageViewProductPhoto) {
            //Creating ActivityOptions for Shared Element Transition
            //where the ImageView is the Shared Element
            ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(),
                            imageViewProductPhoto,
                            TextUtils.isEmpty(product.getDefaultImageUri()) ? getString(R.string.transition_name_product_photo) : product.getDefaultImageUri()
                    );
            //Delegating to the Presenter to handle the event
            mPresenter.editProduct(product.getId(), activityOptionsCompat);
        }

        @Override
        public void onDeleteProduct(final int itemPosition, ProductLite product) {
            //Delegating to the Presenter to handle the event
            mPresenter.deleteProduct(product);
        }
    }

}
