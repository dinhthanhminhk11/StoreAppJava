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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.LoaderProvider;
import com.example.kaushiknsanji.storeapp.ui.inventory.SalesListFragment;
import com.example.kaushiknsanji.storeapp.ui.inventory.SalesListPresenter;
import com.example.kaushiknsanji.storeapp.ui.products.ProductListFragment;
import com.example.kaushiknsanji.storeapp.ui.products.ProductListPresenter;
import com.example.kaushiknsanji.storeapp.ui.suppliers.SupplierListFragment;
import com.example.kaushiknsanji.storeapp.ui.suppliers.SupplierListPresenter;
import com.example.kaushiknsanji.storeapp.utils.InjectorUtility;

import java.util.Set;


public class MainPagerAdapter extends FragmentStatePagerAdapter {

    //Constants for the Pages shown in the ViewPager
    private static final int PRODUCTS_PAGE_POSITION = 0;
    private static final int SUPPLIERS_PAGE_POSITION = 1;
    private static final int SALES_PAGE_POSITION = 2;

    //Constant for the number of views available
    private static final int TOTAL_VIEW_COUNT = 3;

    //Saves the reference to Application Context
    private Context mContext;

    //Sparse Array to keep track of the registered fragments in memory
    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();

    //Stores a reference to the FragmentManager used
    private FragmentManager mFragmentManager;

    MainPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
        mContext = context;
    }
    @Override
    public Fragment getItem(int position) {
        //Selecting the Fragment based on position
        switch (position) {
            case PRODUCTS_PAGE_POSITION: //For Product List
                return provideProductListFragment();
            case SUPPLIERS_PAGE_POSITION: //For Supplier List
                return provideSupplierListFragment();
            case SALES_PAGE_POSITION: //For Sales List
                return provideSalesListFragment();
            default:
                return null;
        }
    }

    private Fragment provideProductListFragment() {
        //Creating the Fragment
        ProductListFragment fragment = ProductListFragment.newInstance();
        //Creating the Fragment's Presenter
        initPresenter(fragment);
        //Returning the ProductListFragment
        return fragment;
    }

    private Fragment provideSupplierListFragment() {
        //Creating the Fragment
        SupplierListFragment fragment = SupplierListFragment.newInstance();
        //Creating the Fragment's Presenter
        initPresenter(fragment);
        //Returning the SupplierListFragment
        return fragment;
    }

    private Fragment provideSalesListFragment() {
        //Creating the Fragment
        SalesListFragment fragment = SalesListFragment.newInstance();
        //Creating the Fragment's Presenter
        initPresenter(fragment);
        //Returning the SalesListFragment
        return fragment;
    }

    private void initPresenter(Fragment fragment) {
        if (fragment instanceof ProductListFragment) {
            //Creating the ProductListFragment's Presenter
            ProductListPresenter presenter = new ProductListPresenter(
                    LoaderProvider.getInstance(mContext),
                    ((FragmentActivity) mContext).getSupportLoaderManager(),
                    InjectorUtility.provideStoreRepository(mContext),
                    (ProductListFragment) fragment
            );
        } else if (fragment instanceof SupplierListFragment) {
            //Creating the SupplierListFragment's Presenter
            SupplierListPresenter presenter = new SupplierListPresenter(
                    LoaderProvider.getInstance(mContext),
                    ((FragmentActivity) mContext).getSupportLoaderManager(),
                    InjectorUtility.provideStoreRepository(mContext),
                    (SupplierListFragment) fragment
            );
        } else if (fragment instanceof SalesListFragment) {
            //Creating the SalesListFragment's Presenter
            SalesListPresenter presenter = new SalesListPresenter(
                    LoaderProvider.getInstance(mContext),
                    ((FragmentActivity) mContext).getSupportLoaderManager(),
                    InjectorUtility.provideStoreRepository(mContext),
                    (SalesListFragment) fragment
            );
        }
    }

    @Override
    public int getCount() {
        return TOTAL_VIEW_COUNT;
    }

    @Override
    @NonNull
    public Object instantiateItem(ViewGroup container, int position) {
        //Registers the Fragment when the item is instantiated (for the first time) using #getItem
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //Unregisters the Fragment when the item is inactive
        mRegisteredFragments.delete(position);
        super.destroyItem(container, position, object);
    }

    @Nullable
    Fragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
        if (state != null) {
            //When the state is present
            Bundle bundle = (Bundle) state;
            //Setting the ClassLoader passed, onto the Bundle
            bundle.setClassLoader(loader);

            //Retrieving the keys used in Bundle
            Set<String> keyStringSet = bundle.keySet();
            //Iterating over the Keys to find the Fragments
            for (String keyString : keyStringSet) {
                if (keyString.startsWith("f")) {
                    //Fragment keys starts with 'f' followed by its position index
                    int position = Integer.parseInt(keyString.substring(1));
                    //Getting the Fragment from the Bundle using the Key through the FragmentManager
                    Fragment fragment = mFragmentManager.getFragment(bundle, keyString);
                    if (fragment != null) {
                        //If Fragment is valid, then update the Sparse Array of Registered Fragments
                        mRegisteredFragments.put(position, fragment);
                        //Create the Fragment's Presenter
                        initPresenter(fragment);
                    }
                }
            }
        }
    }

    @NonNull
    View getTabView(ViewGroup container, int position) {
        //Inflating the template Tab Layout ('R.layout.layout_main_tab') at position
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.layout_main_tab, container, false);

        //Finding the Icon ImageViews to set its icon
        ImageView imageViewTabIcon = rootView.findViewById(R.id.image_main_tab_icon);
        ImageView imageViewTabIconTemp = rootView.findViewById(R.id.image_main_tab_icon_temp);

        //Finding the TextView to set its Title
        TextView textViewTabTitle = rootView.findViewById(R.id.text_main_tab_title);

        //Setting the Icon and the Text based on the current position
        switch (position) {
            case PRODUCTS_PAGE_POSITION: //For Product List
                imageViewTabIcon.setImageResource(R.drawable.state_main_tab_product_material);
                imageViewTabIconTemp.setImageResource(R.drawable.state_main_tab_product_material);
                textViewTabTitle.setText(mContext.getString(R.string.main_tab_title_products));
                break;
            case SUPPLIERS_PAGE_POSITION: //For Supplier List
                imageViewTabIcon.setImageResource(R.drawable.state_main_tab_supplier);
                imageViewTabIconTemp.setImageResource(R.drawable.state_main_tab_supplier);
                textViewTabTitle.setText(mContext.getString(R.string.main_tab_title_suppliers));
                break;
            case SALES_PAGE_POSITION: //For Sales List
                imageViewTabIcon.setImageResource(R.drawable.state_main_tab_cart);
                imageViewTabIconTemp.setImageResource(R.drawable.state_main_tab_cart);
                textViewTabTitle.setText(mContext.getString(R.string.main_tab_title_sales));
                break;
        }

        //Returning the prepared Tab Item Layout
        return rootView;
    }

    void changeTabView(TabLayout.Tab tab, boolean visibility) {
        //Retrieving the Custom View set for the tab
        View rootView = tab.getCustomView();

        if (rootView != null) {
            //When we have the Custom View

            //Finding the Temp Icon ImageView
            ImageView imageViewTabIconTemp = rootView.findViewById(R.id.image_main_tab_icon_temp);

            //Finding the Group that has Icon with Title
            Group groupIconTitle = rootView.findViewById(R.id.group_main_tab_icon_title);

            if (visibility) {
                //When TRUE, show the Group and hide the temporary Icon
                groupIconTitle.setVisibility(View.VISIBLE);
                imageViewTabIconTemp.setVisibility(View.GONE);
            } else {
                //When FALSE, hide the Group and show the temporary Icon
                groupIconTitle.setVisibility(View.INVISIBLE);
                imageViewTabIconTemp.setVisibility(View.VISIBLE);
            }

        }
    }

}
