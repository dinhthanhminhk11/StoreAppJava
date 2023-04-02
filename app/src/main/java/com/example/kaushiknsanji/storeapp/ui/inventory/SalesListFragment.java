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

package com.example.kaushiknsanji.storeapp.ui.inventory;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.support.v4.content.ContextCompat;
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
import com.example.kaushiknsanji.storeapp.data.local.models.SalesLite;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.inventory.config.SalesConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.ColorUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;
import com.example.kaushiknsanji.storeapp.utils.TextAppearanceUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class SalesListFragment extends Fragment implements SalesListContract.View, SwipeRefreshLayout.OnRefreshListener {

    //Constant used for logs
    private static final String LOG_TAG = SalesListFragment.class.getSimpleName();

    //The Presenter interface for this View
    private SalesListContract.Presenter mPresenter;

    //References to the Views shown in this Fragment
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewContentList;
    private Group mGroupEmptyList;

    //Adapter of the RecyclerView
    private SalesListAdapter mAdapter;

    public SalesListFragment() {
    }

    public static SalesListFragment newInstance() {
        return new SalesListFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_main_content_page, container, false);

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_content_page);
        mRecyclerViewContentList = rootView.findViewById(R.id.recyclerview_content_page);
        TextView textViewEmptyList = rootView.findViewById(R.id.text_content_page_empty_list);
        ImageView imageViewStepNumber = rootView.findViewById(R.id.image_content_page_step_number);
        mGroupEmptyList = rootView.findViewById(R.id.group_content_page_empty);

        imageViewStepNumber.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_main_sales_page_number));

        textViewEmptyList.setText(getString(R.string.sales_list_empty_text));

        setupSwipeRefresh();

        setupRecyclerView();

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false) {


            @Override
            public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
                if (getChildCount() > 0 && itemCount == 1) {
                    //When there are some items visible and number of items added is 1

                    //Getting the last item position in the RecyclerView
                    int positionLast = getItemCount() - 1;
                    if (positionLast > positionStart) {
                        //When the last item position is more than the start position
                        for (int index = positionStart; index <= positionLast; index++) {
                            removeView(findViewByPosition(index));
                        }
                        recyclerView.smoothScrollToPosition(positionStart);
                    }
                }

            }
        };

        mRecyclerViewContentList.setLayoutManager(linearLayoutManager);

        mAdapter = new SalesListAdapter(requireContext(), new UserActionsListener());

        mRecyclerViewContentList.setAdapter(mAdapter);

        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.sales_list_items_spacing);

        mRecyclerViewContentList.addItemDecoration(new ListItemSpacingDecoration(itemSpacing, itemSpacing));

    }

    @Override
    public void onResume() {
        super.onResume();

        //Start loading the Products with Sales data
        mPresenter.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Dispatching the event to the Presenter to invalidate any critical resources
        mPresenter.releaseResources();
    }


    @Nullable
    @Override
    public SalesListContract.Presenter getPresenter() {
        return mPresenter;
    }


    @Override
    public void setPresenter(SalesListContract.Presenter presenter) {
        mPresenter = presenter;
    }


    @Override
    public void onRefresh() {
        //Forcefully start a new load
        mPresenter.triggerProductSalesLoad(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Delegating to the Presenter to handle
        mPresenter.onActivityResult(requestCode, resultCode, data);
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
    public void showError(@StringRes int messageId, @Nullable Object... args) {
        if (getView() != null) {

            String messageToBeShown;
            if (args != null && args.length > 0) {
                messageToBeShown = getString(messageId, args);
            } else {
                messageToBeShown = getString(messageId);
            }

            if (!TextUtils.isEmpty(messageToBeShown)) {

                new SnackbarUtility(Snackbar.make(getView(), messageToBeShown, Snackbar.LENGTH_INDEFINITE)).revealCompleteMessage().setDismissAction(R.string.snackbar_action_ok).showSnack();
            }
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
    public void loadSalesList(ArrayList<SalesLite> salesList) {
        //Submitting the new updated list to the Adapter
        mAdapter.submitList(salesList);
    }


    @Override
    public void showDeleteSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_delete_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public void showSellQuantitySuccess(String productSku, String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.sales_list_item_sell_success, productSku, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showUpdateInventorySuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.sales_list_item_update_inventory_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public void launchEditProductSales(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Creating the Intent to launch SalesConfigActivity
        Intent salesConfigIntent = new Intent(requireContext(), SalesConfigActivity.class);
        //Passing in the Product ID of the Product to be edited
        salesConfigIntent.putExtra(SalesConfigActivity.EXTRA_PRODUCT_ID, productId);
        //Starting the Activity with Result
        startActivityForResult(salesConfigIntent, SalesConfigActivity.REQUEST_EDIT_SALES, activityOptionsCompat.toBundle());
    }


    private static class SalesListAdapter extends ListAdapter<SalesLite, SalesListAdapter.ViewHolder> {


        private static DiffUtil.ItemCallback<SalesLite> DIFF_SALES = new DiffUtil.ItemCallback<SalesLite>() {

            @Override
            public boolean areItemsTheSame(SalesLite oldItem, SalesLite newItem) {
                //Returning the comparison of Item Id and Supplier Id
                return oldItem.getProductId() == newItem.getProductId() && oldItem.getSupplierId() == newItem.getSupplierId();
            }


            @Override
            public boolean areContentsTheSame(SalesLite oldItem, SalesLite newItem) {
                //Returning the comparison of entire SalesLite
                return oldItem.equals(newItem);
            }
        };
        private Typeface mProductSkuTypeface;
        private SalesListUserActionsListener mActionsListener;


        SalesListAdapter(Context context, SalesListUserActionsListener userActionsListener) {
            super(DIFF_SALES);
            mActionsListener = userActionsListener;
            mProductSkuTypeface = ResourcesCompat.getFont(context, R.font.libre_barcode_128_text_regular);
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_sales_list'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_list, parent, false);
            //Returning the Instance of ViewHolder for the inflated Item View
            return new ViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //Get the data at the position
            SalesLite salesLite = getItem(position);

            //Bind the Views with the data at the position
            holder.bind(position, salesLite);
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView mTextViewProductName;
            private ImageView mImageViewProductPhoto;
            private TextView mTextViewProductSku;
            private TextView mTextViewProductCategory;
            private TextView mTextViewTotalAvailable;
            private TextView mTextViewSupplierNameCode;
            private TextView mTextViewSupplierPrice;
            private TextView mTextViewSupplierAvailability;
            private Button mButtonDeleteProduct;
            private Button mButtonSell;
            private Group mGroupTopSupplier;
            private Typeface mTotalAvailableTypeface;


            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewProductName = itemView.findViewById(R.id.text_product_item_name);
                mImageViewProductPhoto = itemView.findViewById(R.id.image_product_item_photo);
                mTextViewProductSku = itemView.findViewById(R.id.text_product_item_sku);
                mTextViewProductCategory = itemView.findViewById(R.id.text_product_item_category);
                mTextViewTotalAvailable = itemView.findViewById(R.id.text_sales_list_item_total_available);
                mTextViewSupplierNameCode = itemView.findViewById(R.id.text_sales_list_item_supplier_name_code);
                mTextViewSupplierPrice = itemView.findViewById(R.id.text_sales_list_item_supplier_price);
                mTextViewSupplierAvailability = itemView.findViewById(R.id.text_sales_list_item_supplier_availability);
                mButtonDeleteProduct = itemView.findViewById(R.id.btn_sales_list_item_delete);
                mButtonSell = itemView.findViewById(R.id.btn_sales_list_item_sell);
                mGroupTopSupplier = itemView.findViewById(R.id.group_sales_list_item_top_supplier);

                mTotalAvailableTypeface = mTextViewTotalAvailable.getTypeface();

                mButtonDeleteProduct.setOnClickListener(this);
                mButtonSell.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }


            void bind(int position, SalesLite salesLite) {
                mTextViewProductName.setText(salesLite.getProductName());
                mTextViewProductSku.setText(salesLite.getProductSku());
                mTextViewProductSku.setTypeface(mProductSkuTypeface);
                ImageDownloaderFragment.newInstance(((FragmentActivity) mImageViewProductPhoto.getContext()).getSupportFragmentManager(), position).executeAndUpdate(mImageViewProductPhoto, salesLite.getDefaultImageUri(), position);
                mTextViewProductCategory.setText(salesLite.getCategoryName());

                Resources resources = itemView.getContext().getResources();
                int totalAvailableQuantity = salesLite.getTotalAvailableQuantity();

                if (totalAvailableQuantity > 0) {
                    TextAppearanceUtility.setHtmlText(mTextViewTotalAvailable, resources.getString(R.string.sales_list_item_total_available, totalAvailableQuantity));
                    mTextViewTotalAvailable.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.salesListItemTotalAvailableColor));
                    mTextViewTotalAvailable.setTypeface(mTotalAvailableTypeface);
                    mTextViewTotalAvailable.setAllCaps(false);
                    mGroupTopSupplier.setVisibility(View.VISIBLE);
                    mTextViewSupplierNameCode.setText(resources.getString(R.string.sales_list_item_supplier_name_code_format, salesLite.getTopSupplierName(), salesLite.getTopSupplierCode()));
                    mTextViewSupplierPrice.setText(resources.getString(R.string.sales_list_item_supplier_selling_price, Currency.getInstance(Locale.getDefault()).getSymbol() + " " + salesLite.getSupplierUnitPrice()));
                    mTextViewSupplierAvailability.setText(String.valueOf(salesLite.getSupplierAvailableQuantity()));
                } else {

                    mTextViewTotalAvailable.setText(resources.getString(R.string.sales_list_item_out_of_stock));
                    mTextViewTotalAvailable.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.salesListItemOutOfStockColor));
                    mTextViewTotalAvailable.setTypeface(mTotalAvailableTypeface, Typeface.BOLD);
                    mTextViewTotalAvailable.setAllCaps(true);

                    mGroupTopSupplier.setVisibility(View.GONE);
                }
            }


            @Override
            public void onClick(View view) {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    SalesLite salesLite = getItem(adapterPosition);

                    int clickedViewId = view.getId();

                    if (clickedViewId == itemView.getId()) {

                        mActionsListener.onEditSales(adapterPosition, salesLite, mImageViewProductPhoto);

                    } else if (clickedViewId == mButtonDeleteProduct.getId()) {
                        mActionsListener.onDeleteProduct(adapterPosition, salesLite);

                    } else if (clickedViewId == mButtonSell.getId()) {
                        mActionsListener.onSellOneQuantity(adapterPosition, salesLite);

                    }

                }
            }
        }
    }


    private class UserActionsListener implements SalesListUserActionsListener {
        @Override
        public void onEditSales(int itemPosition, SalesLite salesLite, ImageView imageViewProductPhoto) {
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), imageViewProductPhoto, TextUtils.isEmpty(salesLite.getDefaultImageUri()) ? getString(R.string.transition_name_product_photo) : salesLite.getDefaultImageUri());
            mPresenter.editProductSales(salesLite.getProductId(), activityOptionsCompat);
        }

        @Override
        public void onDeleteProduct(int itemPosition, SalesLite salesLite) {
            mPresenter.deleteProduct(salesLite.getProductId(), salesLite.getProductSku());
        }

        @Override
        public void onSellOneQuantity(int itemPosition, SalesLite salesLite) {
            //Delegating to the Presenter to handle the event
            mPresenter.sellOneQuantity(salesLite);
        }
    }

}
