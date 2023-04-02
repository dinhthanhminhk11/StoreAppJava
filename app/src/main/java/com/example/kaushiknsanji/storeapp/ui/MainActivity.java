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

package com.example.kaushiknsanji.storeapp.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.cache.BitmapImageCache;
import com.example.kaushiknsanji.storeapp.ui.about.AboutActivity;
import com.example.kaushiknsanji.storeapp.ui.inventory.SalesListFragment;
import com.example.kaushiknsanji.storeapp.ui.products.ProductListFragment;
import com.example.kaushiknsanji.storeapp.ui.suppliers.SupplierListFragment;


public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener, View.OnClickListener {

    //For the ViewPager that displays the fragments for Products, Suppliers and Sales information
    private ViewPager mViewPager;
    //The Adapter for the ViewPager
    private MainPagerAdapter mPagerAdapter;
    //The TabLayout for the ViewPager
    private TabLayout mTabLayout;
    //The Common FAB Add button for "Products" and "Suppliers" Tab
    private FloatingActionButton mFabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Inflating the Activity's UI
        setContentView(R.layout.activity_main);

        //Initialize Toolbar
        setupToolbar();

        //Initialize ViewPager and its Adapter
        setupViewPager();

        //Find and Initialize the FAB
        setupFab();

        //Find TabLayout
        mTabLayout = findViewById(R.id.tablayout_main);
        //Setup TabLayout with ViewPager
        mTabLayout.setupWithViewPager(mViewPager);

        //Iterate over the Tabs to set the custom Tab view for each Tab
        int tabCount = mTabLayout.getTabCount();
        for (int tabIndex = 0; tabIndex < tabCount; tabIndex++) {
            //Get the current position Tab
            TabLayout.Tab tab = mTabLayout.getTabAt(tabIndex);
            if (tab != null) {
                //Inflate and get the Tab View for the current Tab
                View tabView = mPagerAdapter.getTabView(mViewPager, tabIndex);
                //Set the custom Tab View for the Tab
                tab.setCustomView(tabView);
                if (savedInstanceState == null && tabIndex == 0) {
                    //On Initial load, preselect the first page
                    mViewPager.setCurrentItem(tabIndex);
                    //Making the Title visible manually since the OnTabSelectedListener
                    //is not registered at this lifecycle state
                    mPagerAdapter.changeTabView(tab, true);
                    //Setting the Fab Color to use for the ProductListFragment
                    mFabAdd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mainProductListFabColor)));
                }
            }
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Trigger the OnTabSelectedListener's selected event manually
        //to set the tab as selected
        onTabSelected(mTabLayout.getTabAt(mViewPager.getCurrentItem()));
    }


    private void setupToolbar() {
        //Finding the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        //Setting it as the Action Bar
        setSupportActionBar(toolbar);
    }

    private void setupViewPager() {
        //Finding Viewpager
        mViewPager = findViewById(R.id.viewpager_main);

        //Initializing Viewpager Adapter
        mPagerAdapter = new MainPagerAdapter(this, getSupportFragmentManager());

        //Attaching the Adapter to Viewpager
        mViewPager.setAdapter(mPagerAdapter);
    }

    private void setupFab() {
        //Finding the FAB
        mFabAdd = findViewById(R.id.fab_main_add);

        //Registering the click listener on FAB
        mFabAdd.setOnClickListener(this);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        //Fix added to correct the Position pointed by the ViewPager: START
        //(Directly touching the tab instead of swiping can result in this)
        int newPosition = tab.getPosition();
        if (mViewPager.getCurrentItem() != newPosition) {
            //When position is incorrect, restore the position using the tab's position
            mViewPager.setCurrentItem(newPosition);
        }
        //Fix added to correct the Position pointed by the ViewPager: END

        //Making the title visible for the New Tab selected
        mPagerAdapter.changeTabView(tab, true);

        //Get the registered Fragment for the Position
        Fragment pagerFragment = mPagerAdapter.getRegisteredFragment(newPosition);

        if (pagerFragment instanceof SalesListFragment) {
            //When the Fragment is SalesListFragment, hide the FAB as there is no purpose for it
            mFabAdd.hide();
        } else {
            //When the Fragment is other than SalesListFragment, show the FAB since we need it to configure Products/Suppliers
            mFabAdd.show();

            //Setting the FAB Background Color based on the Fragment
            if (pagerFragment instanceof ProductListFragment) {
                mFabAdd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mainProductListFabColor)));
            } else if (pagerFragment instanceof SupplierListFragment) {
                mFabAdd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mainSupplierListFabColor)));
            }
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        //Hiding the Title for the Tab unselected
        mPagerAdapter.changeTabView(tab, false);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        //no-op
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Registering the Listener on TabLayout
        mTabLayout.addOnTabSelectedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //UnRegistering the Listener on TabLayout
        mTabLayout.removeOnTabSelectedListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_main_add:
                //For the FAB Add button
                fabAddButtonClicked();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating the Menu options from 'R.menu.menu_main'
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Returning true for the Menu to be displayed
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling based on the Menu item selected
        switch (item.getItemId()) {
            case R.id.action_refresh:
                //On Click of Refresh Menu

                onRefreshMenuClicked();
                return true;
            case R.id.action_about:
                //On Click of About

                launchAboutActivity();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchAboutActivity() {
        //Creating an Intent to launch AboutActivity
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        //Starting the Activity
        startActivity(aboutIntent);
    }

    private void fabAddButtonClicked() {
        //Retrieving the Fragment currently shown
        Fragment pagerFragment = getCurrentPagerFragment();

        if (pagerFragment != null) {
            //When we have a Fragment

            //Get the Presenter for the Pager Fragment
            PagerPresenter presenter = ((PagerView) pagerFragment).getPresenter();

            if (presenter != null) {
                //When we have the Presenter, initiate the FAB click action via the interface method
                presenter.onFabAddClicked();
            }
        }
    }

    private void onRefreshMenuClicked() {
        //Retrieving the Fragment currently shown
        Fragment pagerFragment = getCurrentPagerFragment();

        if (pagerFragment != null) {
            //When we have a Fragment

            //Get the Presenter for the Pager Fragment
            PagerPresenter presenter = ((PagerView) pagerFragment).getPresenter();

            if (presenter != null) {
                //When we have the Presenter, initiate the Refresh Menu action via the interface method
                presenter.onRefreshMenuClicked();
            }
        }
    }

    private Fragment getCurrentPagerFragment() {
        //Get the position of the ViewPager
        int position = mViewPager.getCurrentItem();

        //Get the registered Fragment for the Position
        return mPagerAdapter.getRegisteredFragment(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Clearing the Bitmap Cache if any
        BitmapImageCache.clearCache();
    }
}
