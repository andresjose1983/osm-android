package com.pskloud.osm;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.pskloud.osm.adapter.ProductAdapter;
import com.pskloud.osm.model.Product;
import com.pskloud.osm.util.Functions;
import com.pskloud.osm.util.ProductsSqlHelper;

import java.util.List;

public class ProductsActivity extends DefaultActivity implements SearchView.OnQueryTextListener {

    private SearchView mSearchView;
    private RecyclerView mRvProducts;
    private ProductAdapter mProductAdapter;
    private ProductsSqlHelper productsSqlHelper;
    private List<Product> mProducts;
    private boolean isViewWithCatalog = true;

    public static void show(Context context) {
        context.startActivity(new Intent(context, ProductsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_products);
    }

    @Override
    public void setUp() {
        changeView(new LinearLayoutManager(getApplicationContext()), R.layout.item_product, -1);
    }

    @Override
    public void init() {
        productsSqlHelper = new ProductsSqlHelper(this);

        mRvProducts = (RecyclerView) findViewById(R.id.rv_products);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_product, menu);
        // Associate searchable configuration with the SearchView
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setOnQueryTextListener(this);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_view:
                isViewWithCatalog = !isViewWithCatalog;
                if(isViewWithCatalog)
                    changeView(new LinearLayoutManager(this), R.layout.item_product, -1);
                else
                    changeView(new GridLayoutManager(this,2), R.layout.item_grid_product,
                            getResources().getDisplayMetrics().densityDpi);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeView(final RecyclerView.LayoutManager layoutManage, final int view,
                            final int dpi) {

        mProducts = productsSqlHelper.GET.execute();

        mRvProducts.setLayoutManager(layoutManage);
        mRvProducts.setItemAnimator(new DefaultItemAnimator());

        mProductAdapter = new ProductAdapter(mProducts, view, dpi);
        mRvProducts.setAdapter(mProductAdapter);
        mRvProducts.setHasFixedSize(true);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        filterProduct(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filterProduct(newText);
        return false;
    }

    private void filterProduct(String query) {
        mProductAdapter.getFilter().filter(query.toUpperCase());
    }

}
