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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.ui.inventory.procure.SalesProcurementActivity;
import com.example.kaushiknsanji.storeapp.ui.products.config.DefaultPhotoChangeListener;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigContract;
import com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.InjectorUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;


public class SalesConfigActivity extends AppCompatActivity implements SalesConfigNavigator, DefaultPhotoChangeListener {

    //Request codes used by the activity that calls this activity for result
    public static final int REQUEST_EDIT_SALES = 60; //61 is reserved for the result of this request

    //Custom Result Codes for Edit operation (61)
    public static final int RESULT_EDIT_SALES = RESULT_FIRST_USER + REQUEST_EDIT_SALES;

    //Intent Extra constant for retrieving the Product ID from the Parent SalesListFragment
    public static final String EXTRA_PRODUCT_ID = SalesConfigActivity.class.getPackage() + "extra.PRODUCT_ID";

    //Intent Extra constant for passing the Result information of Product Id to the Calling Activity
    public static final String EXTRA_RESULT_PRODUCT_ID = SalesConfigActivity.class.getPackage() + "extra.PRODUCT_ID";
    //Intent Extra constant for passing the Result information of Product SKU to the Calling Activity
    public static final String EXTRA_RESULT_PRODUCT_SKU = SalesConfigActivity.class.getPackage() + "extra.PRODUCT_SKU";

    //The Presenter for this View's Content Fragment
    private SalesConfigContract.Presenter mPresenter;

    //The ImageView to show the default photo of the Product
    private ImageView mImageViewItemPhoto;

    //Boolean to postpone/start the Shared Element enter transition
    private boolean mIsEnterTransitionPostponed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Inflating the activity's layout
        setContentView(R.layout.activity_sales_config);
        //Delay the enter transition
        supportPostponeEnterTransition();

        //Find the ImageView for the default photo of the Product
        mImageViewItemPhoto = findViewById(R.id.image_sales_config_item_photo);

        //Get the Product ID passed for editing
        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, ProductConfigContract.NEW_PRODUCT_INT);

        if (productId == ProductConfigContract.NEW_PRODUCT_INT) {
            //When the Product Id is not an existing Id, then finish the Activity
            doCancel();
        }

        //Initialize Toolbar
        setupToolbar();

        //Initialize Content Fragment
        SalesConfigActivityFragment contentFragment = obtainContentFragment(productId);

        //Initialize Presenter
        mPresenter = obtainPresenter(productId, contentFragment);
    }


    @NonNull
    private SalesConfigContract.Presenter obtainPresenter(int productId, SalesConfigActivityFragment contentFragment) {
        return new SalesConfigPresenter(productId,
                InjectorUtility.provideStoreRepository(this),
                contentFragment,
                this,
                this);
    }

    private SalesConfigActivityFragment obtainContentFragment(int productId) {
        //Retrieving the Fragment Manager
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        //Finding if the Content Fragment is present at the Id
        SalesConfigActivityFragment fragment
                = (SalesConfigActivityFragment) supportFragmentManager.findFragmentById(R.id.content_sales_config);
        if (fragment == null) {
            //Create and add the Fragment when not present
            fragment = SalesConfigActivityFragment.newInstance(productId);
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_sales_config, fragment)
                    .commit();
        }
        //Returning the Content Fragment instance
        return fragment;
    }

    private void setupToolbar() {
        //Find the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_sales_config);
        //Set the Title
        toolbar.setTitle(R.string.sales_config_title_edit_sales);

        //Set the custom toolbar as ActionBar
        setSupportActionBar(toolbar);

        //Enable the Up button navigation
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //Enabling the home button for Up Action
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void supportPostponeEnterTransition() {
        super.supportPostponeEnterTransition();
        //Marking that the transition has been postponed
        mIsEnterTransitionPostponed = true;
    }

    @Override
    public void supportStartPostponedEnterTransition() {
        super.supportStartPostponedEnterTransition();
        //Marking that the postponed transition has been started
        mIsEnterTransitionPostponed = false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling based on the Menu item selected
        switch (item.getItemId()) {
            case android.R.id.home:
                //For android home/up button

                //Propagating the call to the Presenter to do the required action
                mPresenter.onUpOrBackAction();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        //Propagating the call to the Presenter to do the required action
        mPresenter.onUpOrBackAction();
    }


    @Override
    public void doSetResult(int resultCode, int productId, @NonNull String productSku) {
        //Build the Result Intent and finish the Activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RESULT_PRODUCT_ID, productId);
        resultIntent.putExtra(EXTRA_RESULT_PRODUCT_SKU, productSku);
        setResult(resultCode, resultIntent);

        //Finish the current activity
        supportFinishAfterTransition();
    }


    @Override
    public void doCancel() {
        //Pass Result for Cancel and finish the Activity
        setResult(RESULT_CANCELED);

        //Finish the current activity
        supportFinishAfterTransition();
    }


    @Override
    public void launchEditProduct(int productId) {
        //Creating the Intent to launch ProductConfigActivity
        Intent productConfigIntent = new Intent(this, ProductConfigActivity.class);
        //Passing in the Product ID of the Product to be edited
        productConfigIntent.putExtra(ProductConfigActivity.EXTRA_PRODUCT_ID, productId);
        //Creating ActivityOptions for Shared Element Transition where the ImageView is the Shared Element
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                mImageViewItemPhoto,
                ViewCompat.getTransitionName(mImageViewItemPhoto)
        );
        //Starting the Activity with Result
        ActivityCompat.startActivityForResult(this, productConfigIntent, ProductConfigActivity.REQUEST_EDIT_PRODUCT, activityOptionsCompat.toBundle());
    }

    @Override
    public void launchProcureProduct(ProductSupplierSales productSupplierSales, ProductImage productImageToBeShown, String productName, String productSku) {
        //Creating the Intent to launch SalesProcurementActivity
        Intent salesConfigIntent = new Intent(this, SalesProcurementActivity.class);
        //Passing in the Product Name
        salesConfigIntent.putExtra(SalesProcurementActivity.EXTRA_PRODUCT_NAME, productName);
        //Passing in the Product SKU
        salesConfigIntent.putExtra(SalesProcurementActivity.EXTRA_PRODUCT_SKU, productSku);
        //Passing in the Product Image to be shown
        salesConfigIntent.putExtra(SalesProcurementActivity.EXTRA_PRODUCT_IMAGE_DEFAULT, productImageToBeShown);
        //Passing in the ProductSupplierSales data
        salesConfigIntent.putExtra(SalesProcurementActivity.EXTRA_PRODUCT_SUPPLIER_SALES, productSupplierSales);
        //Creating ActivityOptions for Shared Element Transition where the ImageView is the Shared Element
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                mImageViewItemPhoto,
                ViewCompat.getTransitionName(mImageViewItemPhoto)
        );
        //Starting the Activity
        ActivityCompat.startActivity(this, salesConfigIntent, activityOptionsCompat.toBundle());
    }


    @Override
    public void launchEditSupplier(int supplierId) {
        //Creating the Intent to launch SupplierConfigActivity
        Intent supplierConfigIntent = new Intent(this, SupplierConfigActivity.class);
        //Passing in the Supplier ID of the Supplier to be edited
        supplierConfigIntent.putExtra(SupplierConfigActivity.EXTRA_SUPPLIER_ID, supplierId);
        //Starting the Activity with Result
        startActivityForResult(supplierConfigIntent, SupplierConfigActivity.REQUEST_EDIT_SUPPLIER);
    }


    @Override
    public void showDefaultImage() {
        //Set the Default Image
        mImageViewItemPhoto.setImageResource(R.drawable.ic_all_product_default);
        //Setting the Transition Name on the ImageView for Shared Element Transition
        ViewCompat.setTransitionName(mImageViewItemPhoto, getString(R.string.transition_name_product_photo));
        if (mIsEnterTransitionPostponed) {
            //Start the Postponed transition if it was postponed
            supportStartPostponedEnterTransition();
        }
    }


    @Override
    public void showSelectedProductImage(String imageUri) {
        //Setting the Transition Name on the ImageView for Shared Element Transition
        ViewCompat.setTransitionName(mImageViewItemPhoto, imageUri);
        //Load the Selected Image for the Product
        ImageDownloaderFragment.newInstance(getSupportFragmentManager(), mImageViewItemPhoto.getId())
                .setOnSuccessListener(bitmap -> {
                    if (mIsEnterTransitionPostponed) {
                        //Start the Postponed transition if it was postponed
                        supportStartPostponedEnterTransition();
                    }
                })
                .executeAndUpdate(mImageViewItemPhoto, imageUri, mImageViewItemPhoto.getId(), getSupportLoaderManager());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Delegating to the Presenter to handle
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }
}
