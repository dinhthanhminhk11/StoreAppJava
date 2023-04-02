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

package com.example.kaushiknsanji.storeapp.ui.suppliers;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierLite;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.ColorUtility;
import com.example.kaushiknsanji.storeapp.utils.IntentUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;

import java.util.ArrayList;
import java.util.Locale;


public class SupplierListFragment extends Fragment
        implements SupplierListContract.View, SwipeRefreshLayout.OnRefreshListener {

    //Constant used for logs
    private static final String LOG_TAG = SupplierListFragment.class.getSimpleName();

    //The Presenter interface for this View
    private SupplierListContract.Presenter mPresenter;

    //References to the Views shown in this Fragment
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewContentList;
    private Group mGroupEmptyList;

    //Adapter of the RecyclerView
    private SupplierListAdapter mAdapter;


    public SupplierListFragment() {
    }

    public static SupplierListFragment newInstance() {
        return new SupplierListFragment();
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
        imageViewStepNumber.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_main_supplier_page_number));

        //Initialize the Empty TextView with Text
        textViewEmptyList.setText(getString(R.string.supplier_list_empty_text));

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
        mAdapter = new SupplierListAdapter(new UserActionsListener());

        //Setting the Adapter on the RecyclerView
        mRecyclerViewContentList.setAdapter(mAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.supplier_list_items_spacing);

        //Setting Item offsets using Item Decoration
        mRecyclerViewContentList.addItemDecoration(new ListItemSpacingDecoration(
                itemSpacing, itemSpacing, true
        ));

    }

    @Override
    public void onResume() {
        super.onResume();

        //Start loading the Suppliers
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
    public SupplierListContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPresenter(SupplierListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onRefresh() {
        //Forcefully start a new load
        mPresenter.triggerSuppliersLoad(true);
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
    public void loadSuppliers(ArrayList<SupplierLite> supplierList) {
        //Submitting the new updated list to the Adapter
        mAdapter.submitList(supplierList);
    }

    @Override
    public void launchAddNewSupplier() {
        //Creating the Intent to launch SupplierConfigActivity
        Intent supplierConfigIntent = new Intent(requireContext(), SupplierConfigActivity.class);
        //Starting the Activity with Result
        startActivityForResult(supplierConfigIntent, SupplierConfigActivity.REQUEST_ADD_SUPPLIER);
    }

    @Override
    public void launchEditSupplier(int supplierId) {
        //Creating the Intent to launch SupplierConfigActivity
        Intent supplierConfigIntent = new Intent(requireContext(), SupplierConfigActivity.class);
        //Passing in the Supplier ID of the Supplier to be edited
        supplierConfigIntent.putExtra(SupplierConfigActivity.EXTRA_SUPPLIER_ID, supplierId);
        //Starting the Activity with Result
        startActivityForResult(supplierConfigIntent, SupplierConfigActivity.REQUEST_EDIT_SUPPLIER);
    }

    @Override
    public void showAddSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_add_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showUpdateSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_update_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showDeleteSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_delete_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void dialPhoneNumber(String phoneNumber) {
        //Launching the Dialer passing in the Phone Number
        IntentUtility.dialPhoneNumber(requireActivity(), phoneNumber);
    }

    @Override
    public void composeEmail(String toEmailAddress) {
        //Launching an Email Activity passing in the Email Address
        IntentUtility.composeEmail(
                requireActivity(),
                new String[]{toEmailAddress},
                null,
                null,
                null
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Delegating to the Presenter to handle
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    private static class SupplierListAdapter extends ListAdapter<SupplierLite, SupplierListAdapter.ViewHolder> {


        private static DiffUtil.ItemCallback<SupplierLite> DIFF_SUPPLIERS
                = new DiffUtil.ItemCallback<SupplierLite>() {

            @Override
            public boolean areItemsTheSame(SupplierLite oldItem, SupplierLite newItem) {
                //Returning the comparison of the Supplier's Id
                return oldItem.getId() == newItem.getId();
            }


            @Override
            public boolean areContentsTheSame(SupplierLite oldItem, SupplierLite newItem) {
                //Returning the comparison of entire Supplier
                return oldItem.equals(newItem);
            }
        };

        //Listener for the User actions on the Supplier List Items
        private SupplierListUserActionsListener mActionsListener;

        SupplierListAdapter(SupplierListUserActionsListener userActionsListener) {
            super(DIFF_SUPPLIERS);
            //Registering the User Actions Listener
            mActionsListener = userActionsListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_supplier_list'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier_list, parent, false);
            //Returning the Instance of ViewHolder for the inflated Item View
            return new ViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //Get the data at the position
            SupplierLite supplierLite = getItem(position);

            //Bind the Views with the data at the position
            holder.bind(supplierLite);
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //References to the Views required, in the Item View
            private TextView mTextViewSupplierName;
            private TextView mTextViewSupplierCode;
            private TextView mTextViewSupplierItemCount;
            private Button mButtonDefaultPhone;
            private Button mButtonDefaultEmail;
            private Button mButtonDelete;
            private Button mButtonEdit;


            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewSupplierName = itemView.findViewById(R.id.text_supplier_list_item_name);
                mTextViewSupplierCode = itemView.findViewById(R.id.text_supplier_list_item_code);
                mTextViewSupplierItemCount = itemView.findViewById(R.id.text_supplier_list_item_products_count);
                mButtonDefaultPhone = itemView.findViewById(R.id.btn_supplier_list_item_default_phone);
                mButtonDefaultEmail = itemView.findViewById(R.id.btn_supplier_list_item_default_email);
                mButtonDelete = itemView.findViewById(R.id.btn_supplier_list_item_delete);
                mButtonEdit = itemView.findViewById(R.id.btn_supplier_list_item_edit);

                //Registering the Click listeners on the required views
                mButtonDefaultPhone.setOnClickListener(this);
                mButtonDefaultEmail.setOnClickListener(this);
                mButtonDelete.setOnClickListener(this);
                mButtonEdit.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }


            void bind(SupplierLite supplierLite) {
                //Get the Resources
                Resources resources = itemView.getContext().getResources();

                //Setting the Name
                mTextViewSupplierName.setText(supplierLite.getName());
                //Setting the Code
                mTextViewSupplierCode.setText(supplierLite.getCode());
                //Setting the Item Count
                mTextViewSupplierItemCount.setText(resources.getString(R.string.supplier_list_item_products_count_desc, supplierLite.getItemCount()));

                //Reading the default phone and email
                String defaultPhone = supplierLite.getDefaultPhone();
                String defaultEmail = supplierLite.getDefaultEmail();
                if (!TextUtils.isEmpty(defaultPhone)) {
                    //When default phone is present, set the Phone Number with its formatting
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mButtonDefaultPhone.setText(PhoneNumberUtils.formatNumber(defaultPhone, Locale.getDefault().getCountry()));
                    } else {
                        mButtonDefaultPhone.setText(PhoneNumberUtils.formatNumber(defaultPhone));
                    }
                    //Ensuring the button is visible
                    mButtonDefaultPhone.setVisibility(View.VISIBLE);
                } else {
                    //When the default phone is absent, hide the element
                    mButtonDefaultPhone.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(defaultEmail)) {
                    //When the default email is present, set the email.
                    mButtonDefaultEmail.setText(defaultEmail);
                    //Ensuring the button is visible
                    mButtonDefaultEmail.setVisibility(View.VISIBLE);
                } else {
                    //When the default email is absent, hide the element
                    mButtonDefaultEmail.setVisibility(View.GONE);
                }

            }

            @Override
            public void onClick(View view) {
                //Checking if the adapter position is valid
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the adapter position is valid

                    //Get the data at the position
                    SupplierLite supplierLite = getItem(adapterPosition);

                    //Get the View Id clicked
                    int clickedViewId = view.getId();

                    //Taking action based on the view clicked
                    if (clickedViewId == itemView.getId()
                            || clickedViewId == R.id.btn_supplier_list_item_edit) {
                        //When the entire Item View or the "Edit" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onEditSupplier(adapterPosition, supplierLite);

                    } else if (clickedViewId == R.id.btn_supplier_list_item_delete) {
                        //When the "Delete" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDeleteSupplier(adapterPosition, supplierLite);

                    } else if (clickedViewId == R.id.btn_supplier_list_item_default_phone) {
                        //When the default phone button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDefaultPhoneClicked(adapterPosition, supplierLite);

                    } else if (clickedViewId == R.id.btn_supplier_list_item_default_email) {
                        //When the default email button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDefaultEmailClicked(adapterPosition, supplierLite);
                    }
                }
            }
        }
    }


    private class UserActionsListener implements SupplierListUserActionsListener {


        @Override
        public void onEditSupplier(int itemPosition, SupplierLite supplier) {
            //Delegating to the Presenter to handle the event
            mPresenter.editSupplier(supplier.getId());
        }


        @Override
        public void onDeleteSupplier(int itemPosition, SupplierLite supplier) {
            //Delegating to the Presenter to handle the event
            mPresenter.deleteSupplier(supplier);
        }


        @Override
        public void onDefaultPhoneClicked(int itemPosition, SupplierLite supplier) {
            //Delegating to the Presenter to handle the event
            mPresenter.defaultPhoneClicked(supplier.getDefaultPhone());
        }


        @Override
        public void onDefaultEmailClicked(int itemPosition, SupplierLite supplier) {
            //Delegating to the Presenter to handle the event
            mPresenter.defaultEmailClicked(supplier.getDefaultEmail());
        }

    }

}
