package vichitpov.com.fbs.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vichitpov.com.fbs.R;
import vichitpov.com.fbs.base.Retrofit;
import vichitpov.com.fbs.adapter.SellerSeeMoreAdapter;
import vichitpov.com.fbs.callback.MyOnClickListener;
import vichitpov.com.fbs.callback.OnLoadMore;
import vichitpov.com.fbs.preference.UserInformationManager;
import vichitpov.com.fbs.retrofit.response.ProductResponse;
import vichitpov.com.fbs.retrofit.service.ApiService;
import vichitpov.com.fbs.retrofit.service.ServiceGenerator;
import vichitpov.com.fbs.ui.activities.login.StartLoginActivity;

import static vichitpov.com.fbs.adapter.SellerSeeMoreAdapter.linearLayoutManager;

public class SellerSeeMoreActivity extends AppCompatActivity implements OnLoadMore, SwipeRefreshLayout.OnRefreshListener, MyOnClickListener {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SellerSeeMoreAdapter adapter;
    private ImageView imageBack;
    private int totalPage;
    private int page = 1;
    private ApiService apiService;
    private UserInformationManager userInformationManager;
    private List<Integer> favoriteList = new ArrayList<>();
    private int pageFavorite = 1;
    private int lastPageFavorite = 1;
    private NiftyDialogBuilder dialogBuilder;
    private boolean isFavoriteSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_see_more);

        initView();
        apiService = ServiceGenerator.createService(ApiService.class);
        userInformationManager = UserInformationManager.getInstance(getSharedPreferences(UserInformationManager.PREFERENCES_USER_INFORMATION, MODE_PRIVATE));
        dialogBuilder = NiftyDialogBuilder.getInstance(this);


        getAllFavorite();

        setRecyclerView();
        loadMoreBuyerPagination(page);

        adapter.onLoadMore(this);
        adapter.mySetOnClick(this);
        refreshLayout.setOnRefreshListener(this);
        imageBack.setOnClickListener(view -> finish());

    }

    private void getAllFavorite() {
        Log.e("pppp", "getAllFavorite: " + favoriteList.size());
        if (isFavoriteSuccess) {
            pageFavorite = 1;
            favoriteList.clear();
        }

        Call<ProductResponse> call = apiService.getAllUserFavorite(userInformationManager.getUser().getAccessToken(), pageFavorite);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful()) {
                    isFavoriteSuccess = false;
                    pageFavorite = response.body().getMeta().getCurrentPage();
                    lastPageFavorite = response.body().getMeta().getLastPage();
                    if (pageFavorite <= lastPageFavorite) {
                        for (int i = 0; i < response.body().getData().size(); i++) {
                            favoriteList.add(response.body().getData().get(i).getId());
                        }

                        Log.e("pppp", "favoriteList: " + favoriteList.size());
                        pageFavorite = pageFavorite + 1;
                        getAllFavorite();
                    } else if (pageFavorite > lastPageFavorite) {
                        Log.e("pppp", "return: " + favoriteList.size());
                        return;
                    }
                } else {
                    Log.e("pppp", response.message() + " = " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                t.printStackTrace();
                Log.e("pppp onFailure", t.getMessage());

            }
        });

    }

    //click on Item
    @Override
    public void setOnItemClick(int position, ProductResponse.Data productResponse) {
    }

    //click on view
    @Override
    public void setOnViewClick(int position, int id, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_popup_menu);

        for (int i = 0; i < favoriteList.size(); i++) {
            if (id == favoriteList.get(i)) {
                popup.getMenu().findItem(R.id.popFavorite).setVisible(false);
                popup.getMenu().findItem(R.id.popRemoveFavorite).setVisible(true);
                break;
            } else {
                popup.getMenu().findItem(R.id.popFavorite).setVisible(true);
                popup.getMenu().findItem(R.id.popRemoveFavorite).setVisible(false);
            }
        }

        popup.setOnMenuItemClickListener(item -> {
            String accessToken = userInformationManager.getUser().getAccessToken();
            if (item.getItemId() == R.id.popFavorite) {
                if (accessToken.equals("N/A")) {
                    startActivity(new Intent(getApplicationContext(), StartLoginActivity.class));
                    finish();
                } else {
                    dialogBuilder
                            .withTitle("Add to favorite!")
                            .withMessage("Do you want to add this product to your favorite?")
                            .withTitleColor(Color.WHITE)
                            .withMessageColor(Color.WHITE)
                            .withDialogColor("#8cff3737")
                            .withButton1Text("YES")
                            .withButton2Text("NO")
                            .isCancelableOnTouchOutside(false)
                            .setButton1Click(view1 -> {
                                Retrofit.addFavorite(this, accessToken, id);
                                isFavoriteSuccess = true;
                                getAllFavorite();
                                dialogBuilder.dismiss();
                            })
                            .setButton2Click(view12 -> dialogBuilder.dismiss())
                            .show();
                }

            } else if (item.getItemId() == R.id.popRemoveFavorite) {
                if (accessToken.equals("N/A")) {
                    startActivity(new Intent(getApplicationContext(), StartLoginActivity.class));
                    finish();
                } else {
                    dialogBuilder
                            .withTitle("Remove favorite!")
                            .withMessage("Do you want to Remove this product from your favorite?")
                            .withTitleColor(Color.WHITE)
                            .withMessageColor(Color.WHITE)
                            .withDialogColor("#8cff3737")
                            .withButton1Text("YES")
                            .withButton2Text("NO")
                            .isCancelableOnTouchOutside(false)
                            .setButton1Click(view1 -> {
                                Retrofit.removeFavorite(this, accessToken, id);
                                isFavoriteSuccess = true;
                                getAllFavorite();
                                dialogBuilder.dismiss();
                            })
                            .setButton2Click(view12 -> dialogBuilder.dismiss())
                            .show();
                }

            } else if (item.getItemId() == R.id.popNotification) {
                Toast.makeText(this, "Send notification to user", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        popup.show();
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(false);
    }

    //when recycler scroll down, this method with invoke
    @Override
    public void setOnLoadMore() {
        if (page == totalPage) {
            return;
        }
        adapter.addProgressBar();
        loadMoreBuyerPagination(++page);
    }

    //request data
    private void loadMoreBuyerPagination(int page) {

        if (page == 1) {
            progressBar.setIndeterminate(true);
        } else {
            progressBar.setIndeterminate(false);
        }


        Call<ProductResponse> call = apiService.seeMoreSellerLoadByPagination(page);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull final Response<ProductResponse> response) {

                if (response.isSuccessful()) {
                    totalPage = response.body().getMeta().getLastPage();
                    if (adapter.isLoading()) {
                        if (adapter.getItemCount() > 0) {
                            adapter.removeProgressBar();
                        }
                    }

                    List<ProductResponse.Data> productList = response.body().getData();
                    if (productList != null) {
                        adapter.addMoreItems(productList);
                    }
                    adapter.onLoaded();
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void setRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SellerSeeMoreAdapter(this, recyclerView, linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    private void initView() {

        imageBack = findViewById(R.id.imageBack);
        recyclerView = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progressBar);
        refreshLayout = findViewById(R.id.swipeRefresh);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
