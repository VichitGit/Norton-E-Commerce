package vichitpov.com.fbs.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetMenuDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.RemoteBanner;
import ss.com.bannerslider.views.BannerSlider;
import vichitpov.com.fbs.R;
import vichitpov.com.fbs.adapter.CategoryHeaderAdapter;
import vichitpov.com.fbs.adapter.FavoriteAdapter;
import vichitpov.com.fbs.adapter.RecentlySingleBuyerAdapter;
import vichitpov.com.fbs.adapter.RecentlySingleSellerAdapter;
import vichitpov.com.fbs.base.BaseAppCompatActivity;
import vichitpov.com.fbs.base.InternetConnection;
import vichitpov.com.fbs.callback.OnClickSingle;
import vichitpov.com.fbs.constant.AnyConstant;
import vichitpov.com.fbs.model.CategoryHeaderModel;
import vichitpov.com.fbs.preference.UserInformationManager;
import vichitpov.com.fbs.retrofit.response.BannerResponse;
import vichitpov.com.fbs.retrofit.response.CategoriesResponse;
import vichitpov.com.fbs.retrofit.response.ProductPostedResponse;
import vichitpov.com.fbs.retrofit.response.ProductResponse;
import vichitpov.com.fbs.retrofit.response.UserInformationResponse;
import vichitpov.com.fbs.retrofit.service.ApiService;
import vichitpov.com.fbs.retrofit.service.ServiceGenerator;
import vichitpov.com.fbs.ui.activities.login.StartLoginActivity;
import vichitpov.com.fbs.ui.activities.post.PostToBuyActivity;
import vichitpov.com.fbs.ui.activities.post.PostToSellActivity;
import vichitpov.com.fbs.ui.activities.profile.FavoriteActivity;
import vichitpov.com.fbs.ui.activities.profile.UserProfileActivity;

public class MainActivity extends BaseAppCompatActivity implements OnClickSingle {

    private RecyclerView recyclerCategoryHeader, recyclerRecentlyBuyer, recyclerRecentSeller, recyclerFavorite, recyclerTopSell;
    private RecyclerView.LayoutManager layoutManager;
    private ApiService apiService;
    private SwipeRefreshLayout refreshLayout;
    private TextView textProfile, textHome, textSearch, seeMoreSeller, seeMoreBuyer, textUpload, textMoreFavorite, textNotification;
    private FloatingActionButton floatingScroll;
    private ScrollView scrollView;
    private RelativeLayout relativeRecentlySeller, relativeRecentlyBuyer, relativeFavorite, relativeTopSell;
    private ProgressBar progressBar;
    private LinearLayout linearInternetUnavailable;
    private RecentlySingleBuyerAdapter adapterBuyer;
    private RecentlySingleSellerAdapter adapterSeller;
    private FavoriteAdapter favoriteAdapter, adapterTopSell;
    private UserInformationManager userInformationManager;
    private ProgressDialog dialog;
    private BannerSlider bannerSlider;
    private CategoryHeaderAdapter categoryHeaderAdapter;
    private String accessToken;
    private RequestUserInformationBackground requestUserInformationBackground;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIntentNotification();

        apiService = ServiceGenerator.createService(ApiService.class);
        adapterSeller = new RecentlySingleSellerAdapter(getApplicationContext());
        userInformationManager = UserInformationManager.getInstance(getSharedPreferences(UserInformationManager.PREFERENCES_USER_INFORMATION, MODE_PRIVATE));
        accessToken = userInformationManager.getUser().getAccessToken();

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.alert_dialog_updating));

        Log.e("pppp", userInformationManager.getUser().getAccessToken());

        initView();
        setUpSliderHeader();
        setUpCategoryHeader();

        if (InternetConnection.isNetworkConnected(this)) {
            linearInternetUnavailable.setVisibility(View.GONE);
            setFavorite();
            setUpRecentlyBuyer();
            setUpTopSeller();
            setUpRecentlySeller();

            if (!accessToken.equals("N/A")) {
                requestUserInformationBackground = new RequestUserInformationBackground();
                requestUserInformationBackground.execute();
            }

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            linearInternetUnavailable.setVisibility(View.VISIBLE);
        }

        eventListener();

        categoryHeaderAdapter.setOnClickListener(this);

    }

    //load to get information user, it's means that refresh user data and delete old share preference
    //and add new share preference(reason cuz when user upload product we cannot update share preference
    //so if we cannot upload share preference cannot upload too. so when we go to user profile, it's will
    //show old information. so we need to update share preference to get new information in local mobile:D
    private void getInformationUser() {
        if (!accessToken.equals("N/A")) {
            Call<UserInformationResponse> call = apiService.getUserInformation(accessToken);
            call.enqueue(new Callback<UserInformationResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserInformationResponse> call, @NonNull Response<UserInformationResponse> response) {
                    if (response.isSuccessful()) {
                        //userInformationManager.deleteUserInformation();
                        Log.e("pppp", "success");
                        userInformationManager.saveInformation(response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserInformationResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                    dialog.dismiss();
                }
            });
        }
    }

    //event click listener to category to activity category
    @Override
    public void setOnClick() {
        Intent intent = new Intent(this, ChooseCategoryActivity.class);
        intent.putExtra(AnyConstant.SEND_FROM_MAIN_ACTIVITY, AnyConstant.SEND_FROM_MAIN_ACTIVITY);
        startActivity(intent);

    }

    private void setUpSliderHeader() {
        RequestBannerBackgroundThread bannerBackgroundThread = new RequestBannerBackgroundThread();
        bannerBackgroundThread.execute();
    }

    private void eventListener() {

        textHome.setOnClickListener(view -> scrollView.fullScroll(ScrollView.FOCUS_UP));
        textProfile.setOnClickListener(view -> {
            if (!accessToken.equals("N/A")) {
                //request user information user background thread
                requestUserInformationBackground = new RequestUserInformationBackground();
                requestUserInformationBackground.execute();

                startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));

            } else {
                startActivity(new Intent(getApplicationContext(), StartLoginActivity.class));
            }
        });
        textSearch.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SearchProductActivity.class)));
        textUpload.setOnClickListener(view -> {
            if (isAccessTokenAvailable()) {
                dialogBottom();
            }
        });
        textMoreFavorite.setOnClickListener(view -> {
            if (accessToken.equals("N/A")) {
                startActivity(new Intent(getApplicationContext(), StartLoginActivity.class));
            } else {
                startActivity(new Intent(getApplicationContext(), FavoriteActivity.class));
            }
        });
        textNotification.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
            startActivity(intent);
            //overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        });

        floatingScroll.setOnClickListener(view -> scrollView.fullScroll(ScrollView.FOCUS_UP));
        seeMoreBuyer.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), BuyerSeeMoreActivity.class)));
        seeMoreSeller.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SellerSeeMoreActivity.class)));
        refreshLayout.setOnRefreshListener(() -> {

            refreshLayout.setRefreshing(false);
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);

        });
    }

    private boolean isAccessTokenAvailable() {
        if (!accessToken.equals("N/A")) {
            return true;
        } else {
            Intent intent = new Intent(this, StartLoginActivity.class);
            startActivity(intent);
            return false;
        }
    }

    //set choose type
    private void setUpCategoryHeader() {
        layoutManager = new GridLayoutManager(MainActivity.this, 3);
        recyclerCategoryHeader.setLayoutManager(layoutManager);

        List<CategoryHeaderModel> modelList = new ArrayList<>();
        modelList.add(new CategoryHeaderModel(getString(R.string.text_settings), R.drawable.ic_setting_background));
        modelList.add(new CategoryHeaderModel(getString(R.string.text_favorite), R.drawable.ic_star_background));
        modelList.add(new CategoryHeaderModel(getString(R.string.text_categories), R.drawable.ic_category_background));

        categoryHeaderAdapter = new CategoryHeaderAdapter(getApplicationContext(), modelList);
        recyclerCategoryHeader.setAdapter(categoryHeaderAdapter);

    }

    //create data with recently 14 item buy
    private void setUpRecentlyBuyer() {
        layoutManager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        recyclerRecentlyBuyer.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.VISIBLE);

        Call<ProductResponse> call = apiService.singlePageBuyer();
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful()) {

                    adapterBuyer = new RecentlySingleBuyerAdapter(getApplicationContext(), response.body().getData());
                    recyclerRecentlyBuyer.setAdapter(adapterBuyer);

                    progressBar.setVisibility(View.GONE);
                    relativeRecentlyBuyer.setVisibility(View.VISIBLE);

                } else {
                    Log.e("pppp", response.code() + " = " + response.message());
                    progressBar.setVisibility(View.GONE);
                    relativeRecentlyBuyer.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                t.printStackTrace();

                progressBar.setVisibility(View.GONE);
                relativeRecentlyBuyer.setVisibility(View.GONE);


            }
        });
    }

    //create data with recently 14 item sell
    private void setUpRecentlySeller() {

        layoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        recyclerRecentSeller.setLayoutManager(layoutManager);
        relativeRecentlySeller.setVisibility(View.GONE);

        Call<ProductResponse> call = apiService.singlePageSeller();
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful()) {

                    adapterSeller.addItem(response.body().getData());
                    recyclerRecentSeller.setAdapter(adapterSeller);
                    relativeRecentlySeller.setVisibility(View.VISIBLE);

                } else {
                    relativeRecentlySeller.setVisibility(View.GONE);
                    //Log.e("pppp", response.code() + " = " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {

                t.printStackTrace();
                //Log.e("pppp", t.getMessage());

                relativeRecentlySeller.setVisibility(View.GONE);

            }
        });
    }

    //create sell top data, validate by count view
    private void setUpTopSeller() {
        layoutManager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        recyclerTopSell.setLayoutManager(layoutManager);
        adapterTopSell = new FavoriteAdapter(this);
        recyclerTopSell.setAdapter(adapterTopSell);
        relativeTopSell.setVisibility(View.GONE);

        Call<ProductResponse> call = apiService.topSellList("sells");
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful()) {
                    adapterTopSell.addItem(response.body().getData());
                    relativeTopSell.setVisibility(View.VISIBLE);
                } else {
                    relativeTopSell.setVisibility(View.GONE);
                    //Log.e("pppp", "top: " + response.code() + " = " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                relativeTopSell.setVisibility(View.GONE);
                //Log.e("pppp", "onFailure: " + t.getMessage());
            }
        });
    }

    private void getIntentNotification() {
        ProductPostedResponse.Data productPostedResponse = (ProductPostedResponse.Data) getIntent().getSerializableExtra("NotificationList");
        if (productPostedResponse != null) {
            Intent intent = new Intent(getApplicationContext(), DetailProductActivity.class);
            intent.putExtra("NotificationList", productPostedResponse);
            startActivity(intent);
        }

    }

    //create user favorite, if user logged
    private void setFavorite() {
        if (!accessToken.equals("N/A")) {
            GridLayoutManager layoutManager = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);

            recyclerFavorite.setLayoutManager(layoutManager);
            favoriteAdapter = new FavoriteAdapter(this);

            Call<ProductResponse> call = apiService.getAllUserFavorite(accessToken, 1);
            call.enqueue(new Callback<ProductResponse>() {
                @Override
                public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getData().size() > 0) {
                                relativeFavorite.setVisibility(View.VISIBLE);
                                favoriteAdapter.addItem(response.body().getData());
                                recyclerFavorite.setAdapter(favoriteAdapter);

                            } else if (response.body().getData().size() == 0) {
                                relativeFavorite.setVisibility(View.GONE);

                            } else {
                                relativeFavorite.setVisibility(View.GONE);
                            }
                        } else {
                            relativeFavorite.setVisibility(View.GONE);
                        }
                    } else {
                        relativeFavorite.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                    relativeFavorite.setVisibility(View.GONE);
                }
            });
        } else {
            relativeFavorite.setVisibility(View.GONE);
        }
    }

    //dialog bottom
    public void dialogBottom() {
        @SuppressLint("ResourceAsColor") BottomSheetMenuDialog dialog = new BottomSheetBuilder(this, R.style.AppTheme_BottomSheetDialog)
                .setMode(BottomSheetBuilder.MODE_LIST)
                .setIconTintColorResource(R.color.colorPrimary)
                .setItemTextColor(R.color.colorPrimary)
                .setMenu(R.menu.menu_dialog_post)
                .setItemClickListener(item -> {
                    if (item.getItemId() == R.id.dialog_bottom_post_to_buy) {
                        startActivity(new Intent(getApplicationContext(), PostToBuyActivity.class));
                    } else if (item.getItemId() == R.id.dialog_bottom_post_to_sell) {
                        startActivity(new Intent(getApplicationContext(), PostToSellActivity.class));
                    }

                })
                .createDialog();
        dialog.show();
    }

    private void requestBanner() {
        List<Banner> bannersList = new ArrayList<>();
        bannersList.add(new RemoteBanner("http://nu-ecommerce.ml/images/banners/Banner_0.jpg"));
        bannersList.add(new RemoteBanner("http://nu-ecommerce.ml/images/banners/banner_1.jpg"));
        bannersList.add(new RemoteBanner("http://nu-ecommerce.ml/images/banners/Banner_2.jpg"));
        bannersList.add(new RemoteBanner("http://nu-ecommerce.ml/images/banners/Banner_3.jpg"));
        bannerSlider.setBanners(bannersList);

//        Call<BannerResponse> call = apiService.getBanner();
//
//        call.enqueue(new Callback<BannerResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<BannerResponse> call, @NonNull Response<BannerResponse> response) {
//                if (response.isSuccessful()) {
//                    for (int i = 0; i < response.body().getData().size(); i++) {
//                        bannersList.add(new RemoteBanner(response.body().getData().get(i).getBannerimage())
//                                .setPlaceHolder(getResources().getDrawable(R.drawable.ic_placeholder_banner))
//                                .setErrorDrawable(getResources().getDrawable(R.drawable.ic_error_banner)));
//                    }
//                } else {
//                    bannersList.add(new RemoteBanner("http://norton-u.com/public_file/images/about/new%20campus.jpg"));
//                }
//                bannerSlider.setBanners(bannersList);
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<BannerResponse> call, @NonNull Throwable t) {
//                t.printStackTrace();
//                bannersList.add(new RemoteBanner("http://norton-u.com/public_file/images/about/new%20campus.jpg"));
//                bannerSlider.setBanners(bannersList);
//
//            }
//        });

    }

    private void initView() {

        recyclerCategoryHeader = findViewById(R.id.recyclerCategories);
        recyclerRecentlyBuyer = findViewById(R.id.recyclerRecentlyBuyer);
        recyclerRecentSeller = findViewById(R.id.recyclerRecentlySeller);
        recyclerFavorite = findViewById(R.id.recyclerFavorite);
        recyclerTopSell = findViewById(R.id.recyclerTopSell);
        linearInternetUnavailable = findViewById(R.id.linearInternetUnavailable);
        refreshLayout = findViewById(R.id.swipeRefresh);

        textProfile = findViewById(R.id.textProfile);
        textSearch = findViewById(R.id.textSearch);
        seeMoreBuyer = findViewById(R.id.textSeeMoreBuyer);
        seeMoreSeller = findViewById(R.id.textSeeMoreSeller);
        textUpload = findViewById(R.id.textUpload);
        textMoreFavorite = findViewById(R.id.textSeeMoreFavorite);
        textHome = findViewById(R.id.textHome);
        textNotification = findViewById(R.id.textNotification);

        floatingScroll = findViewById(R.id.floatingScroll);
        scrollView = findViewById(R.id.scrollView);

        progressBar = findViewById(R.id.progressBar);
        relativeRecentlySeller = findViewById(R.id.relativeRecentlySeller);
        relativeRecentlyBuyer = findViewById(R.id.relativeRecentlyBuyer);
        relativeFavorite = findViewById(R.id.relativeFavorite);
        relativeTopSell = findViewById(R.id.relativeTopSell);
        bannerSlider = findViewById(R.id.bannerSlider);

        progressBar.setVisibility(View.GONE);
        relativeRecentlySeller.setVisibility(View.GONE);
        relativeRecentlyBuyer.setVisibility(View.GONE);
        relativeTopSell.setVisibility(View.GONE);
        relativeFavorite.setVisibility(View.GONE);
        floatingScroll.setVisibility(View.GONE);
        linearInternetUnavailable.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @SuppressLint("StaticFieldLeak")
    class RequestUserInformationBackground extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            getInformationUser();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);
            //Log.e("pppp", "onPostExecute");
        }
    }

    class RequestBannerBackgroundThread extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            requestBanner();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


}
















