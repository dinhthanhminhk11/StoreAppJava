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

package com.example.kaushiknsanji.storeapp.ui.inventory.procure;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.utils.IntentUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;

import java.util.ArrayList;
import java.util.Locale;


public class SalesProcurementActivityFragment extends Fragment implements SalesProcurementContract.View, View.OnClickListener {

    //Constant used for logs
    private static final String LOG_TAG = SalesProcurementActivityFragment.class.getSimpleName();

    //Bundle constants for persisting the data throughout System config changes
    private static final String BUNDLE_SUPPLIER_CONTACTS_LIST_KEY = "SalesProcurement.SupplierContacts";
    private static final String BUNDLE_CONTACTS_RESTORED_BOOL_KEY = "SalesProcurement.AreContactsRestored";

    //The Presenter for this View
    private SalesProcurementContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private TextView mTextViewProductSupplierTitle;
    private TextView mTextViewProductSupplierAvailabilityTotal;
    private EditText mEditTextProductSupplierReqdQty;
    private RecyclerView mRecyclerViewPhoneContacts;
    private CardView mCardViewProcurementViaPhoneContacts;
    private CardView mCardViewProcurementViaEmailContacts;
    private CardView mCardViewProcurementNoContacts;

    //RecyclerView Adapter for Supplier's Phone Contacts
    private SupplierPhoneContactsAdapter mPhoneContactsAdapter;

    //Stores the List of Supplier Contacts of Phone Contact Type
    private ArrayList<SupplierContact> mPhoneContacts;
    //Stores the List of Supplier Contacts of Email Contact Type
    private ArrayList<SupplierContact> mEmailContacts;

    //Stores the state of Supplier Contacts restored,
    //to prevent updating the fields every time during System config change
    private boolean mAreSupplierContactsRestored;


    public SalesProcurementActivityFragment() {
    }


    public static SalesProcurementActivityFragment newInstance() {
        //Instantiating and Returning the fragment instance
        return new SalesProcurementActivityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout 'R.layout.fragment_sales_procurement' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_sales_procurement, container, false);

        //Finding the views to initialize
        mTextViewProductSupplierTitle = rootView.findViewById(R.id.text_sales_procurement_product_supplier_title);
        mTextViewProductSupplierAvailabilityTotal = rootView.findViewById(R.id.text_sales_procurement_supplier_availability_total);
        mEditTextProductSupplierReqdQty = rootView.findViewById(R.id.edittext_sales_procurement_reqd_product_qty);
        mRecyclerViewPhoneContacts = rootView.findViewById(R.id.recyclerview_sales_procurement_phone_list);
        mCardViewProcurementNoContacts = rootView.findViewById(R.id.card_sales_procurement_no_contacts);
        mCardViewProcurementViaEmailContacts = rootView.findViewById(R.id.card_sales_procurement_email);
        mCardViewProcurementViaPhoneContacts = rootView.findViewById(R.id.card_sales_procurement_phone);

        //Registering Click listener on "Send Mail" Image Button
        rootView.findViewById(R.id.imgbtn_sales_procurement_send_mail).setOnClickListener(this);

        //Initialize RecyclerView for Supplier's Phone Contacts
        setupSupplierPhoneContactsRecyclerView();

        //Returning the prepared layout
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        //Start the work
        mPresenter.start();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //On Subsequent launch

            //Restoring the Supplier's Contacts
            mPresenter.updateSupplierContacts(savedInstanceState.getParcelableArrayList(BUNDLE_SUPPLIER_CONTACTS_LIST_KEY));
            //Restoring the state of Supplier's Contacts restored
            mPresenter.updateAndSyncContactsState(savedInstanceState.getBoolean(BUNDLE_CONTACTS_RESTORED_BOOL_KEY, false));
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Saving the state
        //Saving all the Contacts
        ArrayList<SupplierContact> supplierContactsList = new ArrayList<>();
        supplierContactsList.addAll(mPhoneContacts);
        supplierContactsList.addAll(mEmailContacts);
        outState.putParcelableArrayList(BUNDLE_SUPPLIER_CONTACTS_LIST_KEY, supplierContactsList);

        //Saving the Boolean state of Contacts download
        outState.putBoolean(BUNDLE_CONTACTS_RESTORED_BOOL_KEY, mAreSupplierContactsRestored);
    }

    @Override
    public void setPresenter(SalesProcurementContract.Presenter presenter) {
        mPresenter = presenter;
    }


    private void setupSupplierPhoneContactsRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewPhoneContacts.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mPhoneContactsAdapter = new SupplierPhoneContactsAdapter(new SupplierPhoneListItemUserActionsListener());

        //Setting the Adapter on RecyclerView
        mRecyclerViewPhoneContacts.setAdapter(mPhoneContactsAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.sales_procurement_item_phone_vertical_spacing);

        //Setting Item offsets using Item Decoration
        mRecyclerViewPhoneContacts.addItemDecoration(new ListItemSpacingDecoration(
                itemSpacing, itemSpacing
        ));
    }

    @Override
    public void syncContactsState(boolean areSupplierContactsRestored) {
        //Saving the state
        mAreSupplierContactsRestored = areSupplierContactsRestored;
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
    public void updatePhoneContacts(ArrayList<SupplierContact> phoneContacts) {
        //Saving the Phone Contacts
        mPhoneContacts = phoneContacts;
        //Submitting data to the Adapter
        mPhoneContactsAdapter.submitList(mPhoneContacts);
    }

    @Override
    public void hidePhoneContacts() {
        //Hiding the CardView that displays the "Procure via Phone Call"
        mCardViewProcurementViaPhoneContacts.setVisibility(View.GONE);
    }

    @Override
    public void updateEmailContacts(ArrayList<SupplierContact> emailContacts) {
        //Saving the Email Contacts
        mEmailContacts = emailContacts;
    }

    @Override
    public void hideEmailContacts() {
        //Hiding the CardView that displays the "Procure via Email"
        mCardViewProcurementViaEmailContacts.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyContactsView() {
        //Displaying the CardView with a preset message for "No contacts available for Product Procurement!"
        mCardViewProcurementNoContacts.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateProductSupplierTitle(int titleResId, String productName, String productSku, String supplierName, String supplierCode) {
        //Set the Title
        mTextViewProductSupplierTitle.setText(getString(titleResId, productName, productSku, supplierName, supplierCode));
    }

    @Override
    public void updateProductSupplierAvailability(int availableQuantity) {
        //Set the available quantity value
        mTextViewProductSupplierAvailabilityTotal.setText(String.valueOf(availableQuantity));
        //Set the Text Color
        mTextViewProductSupplierAvailabilityTotal.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
    }

    @Override
    public void showOutOfStockAlert() {
        //Set the Out of Stock message
        mTextViewProductSupplierAvailabilityTotal.setText(getString(R.string.sales_list_item_out_of_stock));
        //Set the Text Color
        mTextViewProductSupplierAvailabilityTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.salesListItemOutOfStockColor));
    }

    @Override
    public void dialPhoneNumber(String phoneNumber) {
        //Launching the Dialer passing in the Phone Number
        IntentUtility.dialPhoneNumber(requireActivity(), phoneNumber);
    }

    @Override
    public void composeEmail(String toEmailAddress, String[] ccAddresses, int subjectResId,
                             Object[] subjectArgs, int bodyResId, Object[] bodyArgs) {
        //Launching the Email Activity with the details passed
        IntentUtility.composeEmail(
                requireActivity(),
                new String[]{toEmailAddress},
                ccAddresses,
                getString(subjectResId, subjectArgs),
                getString(bodyResId, bodyArgs)
        );
    }

    @Override
    public void onClick(View view) {
        //Taking action based on the Id of the View clicked
        switch (view.getId()) {
            case R.id.imgbtn_sales_procurement_send_mail:
                //For the "Send Mail" Image Button

                //Delegating to the Presenter, to dispatch an Email to the Supplier
                //for Procuring more quantity of the Product
                mPresenter.sendMailClicked(mEditTextProductSupplierReqdQty.getText().toString(), mEmailContacts);
                break;
        }
    }

    private static class SupplierPhoneContactsAdapter extends ListAdapter<SupplierContact, SupplierPhoneContactsAdapter.ViewHolder> {

        private static DiffUtil.ItemCallback<SupplierContact> DIFF_CONTACTS
                = new DiffUtil.ItemCallback<SupplierContact>() {

            @Override
            public boolean areItemsTheSame(SupplierContact oldItem, SupplierContact newItem) {
                //Returning the comparison of SupplierContact's isDefault
                return oldItem.isDefault() == newItem.isDefault();
            }

            @Override
            public boolean areContentsTheSame(SupplierContact oldItem, SupplierContact newItem) {
                //Returning the comparison of SupplierContact's value
                return oldItem.getValue().equals(newItem.getValue());
            }
        };
        //Listener for User Actions on Supplier's list of Phone Contacts
        private SupplierPhoneListUserActionsListener mActionsListener;

        SupplierPhoneContactsAdapter(SupplierPhoneListUserActionsListener userActionsListener) {
            super(DIFF_CONTACTS);
            mActionsListener = userActionsListener;
        }
        @NonNull
        @Override
        public SupplierPhoneContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_sales_procurement_phone_contact'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_procurement_phone_contact, parent, false);
            //Returning the Instance of ViewHolder for the inflated Item View
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SupplierPhoneContactsAdapter.ViewHolder holder, int position) {
            //Get the data at the position
            SupplierContact supplierContact = getItem(position);
            //Binding the data at the position
            holder.bind(supplierContact);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //The TextView that displays the Phone Number
            private TextView mTextViewPhone;
            //The ImageView that shows whether the Phone Number
            //is the defaulted contact for the Supplier or not
            private ImageView mImageViewContactDefault;

            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewPhone = itemView.findViewById(R.id.text_sales_procurement_item_phone);
                mImageViewContactDefault = itemView.findViewById(R.id.image_sales_procurement_item_contact_default);

                //Registering Click Listener on the Item View
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                //Get the current adapter position
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the item position is valid

                    //Get the data at the position
                    SupplierContact supplierContact = getItem(adapterPosition);

                    if (view.getId() == itemView.getId()) {
                        //When the Item View is clicked, pass the event to the listener
                        mActionsListener.onPhoneClicked(adapterPosition, supplierContact);
                    }
                }
            }

            void bind(SupplierContact supplierContact) {
                if (supplierContact != null) {
                    //When we have the details

                    //Bind the Phone Number
                    String phoneNumber = supplierContact.getValue();
                    //Setting the Phone Number format based on the Build version
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //For Lollipop and above
                        mTextViewPhone.setText(PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().getCountry()));
                    } else {
                        //For below Lollipop
                        mTextViewPhone.setText(PhoneNumberUtils.formatNumber(phoneNumber));
                    }

                    //Set the Visibility of Defaulted Contact ImageView based on whether
                    //the contact is a default contact or not
                    if (supplierContact.isDefault()) {
                        //ImageView is shown when the contact is defaulted
                        mImageViewContactDefault.setVisibility(View.VISIBLE);
                    } else {
                        //ImageView is invisible when the contact is not defaulted
                        mImageViewContactDefault.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    private class SupplierPhoneListItemUserActionsListener implements SupplierPhoneListUserActionsListener {

        @Override
        public void onPhoneClicked(int itemPosition, SupplierContact supplierContact) {
            //Delegating to the Presenter to handle the event
            mPresenter.phoneClicked(supplierContact);
        }
    }
}
