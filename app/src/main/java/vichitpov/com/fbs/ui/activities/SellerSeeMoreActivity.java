package vichitpov.com.fbs.ui.activities;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vichitpov.com.fbs.R;
import vichitpov.com.fbs.adapter.SellerSeeMoreAdapter;
import vichitpov.com.fbs.callback.MyOnClickListener;
import vichitpov.com.fbs.callback.OnLoadMore;
import vichitpov.com.fbs.preference.UserInformationManager;
import vichitpov.com.fbs.retrofit.response.FavoriteResponse;
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
    private List<Integer> productListFavorite = new ArrayList<>();
    private int pageFavorite = 1;
    private int lastPageFavorite = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_see_more);

        initView();
        apiService = ServiceGenerator.createService(ApiService.class);
        userInformationManager = UserInformationManager.getInstance(getSharedPreferences(UserInformationManager.PREFERENCES_USER_INFORMATION, MODE_PRIVATE));

        getAllFavorite();

        setRecyclerView();
        loadMoreBuyerPagination(page);

        adapter.onLoadMore(this);
        adapter.mySetOnClick(this);
        refreshLayout.setOnRefreshListener(this);
        imageBack.setOnClickListener(view -> finish());

    }

    private List<Integer> getAllFavorite() {
        Call<ProductResponse> call = apiService.getAllUserFavorite(userInformationManager.getUser().getAccessToken(), pageFavorite);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful()) {
                    pageFavorite = response.body().getMeta().getCurrentPage();
                    lastPageFavorite = response.body().getMeta().getLastPage();
                    if (pageFavorite <= lastPageFavorite) {
                        for (int i = 0; i < response.body().getData().size(); i++) {
                            favoriteList.add(response.body().getData().get(i).getId());
                        }

                        pageFavorite = pageFavorite + 1;
                        getAllFavorite();
                    } else if (pageFavorite > lastPageFavorite) {
                        return;
                    }
                } else {
                    Log.e("pppp", response.message() + " = " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                t.printStackTrace();
                Log.e("pppp", t.getMessage());

            }
        });

        return favoriteList;
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
            Log.e("pppp", id + " = " + favoriteList.get(i));
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
            if (item.getItemId() == R.id.popFavorite) {
                String accessToken = userInformationManager.getUser().getAccessToken();
                if (accessToken.equals("N/A")) {
                    startActivity(new Intent(getApplicationContext(), StartLoginActivity.class));
                    finish();
                } else {
                    addFavorite(accessToken, id);


                }
            } else if (item.getItemId() == R.id.popRemoveFavorite) {
                Toast.makeText(this, "Remove favorite", Toast.LENGTH_SHORT).show();

            } else if (item.getItemId() == R.id.popNotification) {
                Toast.makeText(this, "Send notification to user", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        popup.show();
    }

    private void addFavorite(String accessToken, int id) {
        Call<FavoriteResponse> call = apiService.addFavorite(accessToken, id);
        call.enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(@NonNull Call<FavoriteResponse> call, @NonNull Response<FavoriteResponse> response) {
                if (response.isSuccessful()) {
                    Log.e("pppp success", response.body().getData().toString());
                    Toast.makeText(SellerSeeMoreActivity.this, "Added favorite", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("pppp else", response.code() + " = " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<FavoriteResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                Log.e("pppp", "onFailure: " + t.getMessage());

            }
        });

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
                        for (int i = 0; i < response.body().getData().size(); i++) {
                            productListFavorite.add(response.body().getData().get(i).getId());
                        }
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

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    private void initView() {

        imageBack = findViewById(R.id.imageBack);
        recyclerView = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progressBar);
        refreshLayout = findViewById(R.id.swipeRefresh);

    }


}
