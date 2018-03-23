package vichitpov.com.fbs.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.RemoteBanner;
import ss.com.bannerslider.views.BannerSlider;
import vichitpov.com.fbs.R;
import vichitpov.com.fbs.adapter.ContactAdapter;
import vichitpov.com.fbs.base.Retrofit;
import vichitpov.com.fbs.preference.UserInformationManager;
import vichitpov.com.fbs.retrofit.response.FavoriteResponse;
import vichitpov.com.fbs.retrofit.response.ProductResponse;
import vichitpov.com.fbs.retrofit.service.ApiService;
import vichitpov.com.fbs.retrofit.service.ServiceGenerator;
import vichitpov.com.fbs.ui.activities.login.StartLoginActivity;
import vichitpov.com.fbs.ui.fragment.ShowMapFragment;

public class DetailProductActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageBack, imageContact, imageFavorite;
    private BannerSlider bannerSlider;
    private Button buttonCall;
    private TextView textTitle, textDescription, textPrice, textName, textPhone, textEmail, textAddress;
    private String getPhone, accessToken;
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private ApiService apiService;
    private UserInformationManager userInformationManager;
    private int page = 1;
    private int totalPage;
    private int productId;
    private boolean isFavorite;
    private List<Integer> favoriteIdList;
    private SpotsDialog dialogRemove, dialogAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);

        favoriteIdList = new ArrayList<>();
        dialogRemove = new SpotsDialog(this, "Removing favorite!");
        dialogAdd = new SpotsDialog(this, "Adding favorite!");
        apiService = ServiceGenerator.createService(ApiService.class);
        userInformationManager = UserInformationManager.getInstance(getSharedPreferences(UserInformationManager.PREFERENCES_USER_INFORMATION, MODE_PRIVATE));
        accessToken = userInformationManager.getUser().getAccessToken();

        initView();
        setUpRecycler();
        getIntentFromAnotherActivity();
        checkExistUserFavorite();


        imageBack.setOnClickListener(this);
        buttonCall.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + getPhone));
            startActivity(intent);
        });

        imageFavorite.setOnClickListener(view -> {
            if (!accessToken.equals("N/A")) {
                if (isFavorite) {
                    removeFavorite();
                } else {
                    addFavorite();
                }
            } else {
                startActivity(new Intent(getApplicationContext(), StartLoginActivity.class));
            }
        });
    }

    //add favorite to user's list.
    private void addFavorite() {
        Call<FavoriteResponse> call = apiService.addFavorite(accessToken, productId);
        dialogAdd.show();
        call.enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(@NonNull Call<FavoriteResponse> call, @NonNull Response<FavoriteResponse> response) {
                if (response.isSuccessful()) {
                    isFavorite = true;
                    imageFavorite.setImageResource(R.drawable.ic_favorite_selected);
                    Toast.makeText(getApplicationContext(), "Added favorite", Toast.LENGTH_SHORT).show();
                    dialogAdd.dismiss();

                } else {
                    isFavorite = false;
                    imageFavorite.setImageResource(R.drawable.ic_favorite_un_select);
                    dialogAdd.dismiss();
                    Toast.makeText(getApplicationContext(), "Error connection. please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FavoriteResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                isFavorite = false;
                imageFavorite.setImageResource(R.drawable.ic_favorite_un_select);
                dialogAdd.dismiss();
                Toast.makeText(getApplicationContext(), "Server problem!", Toast.LENGTH_SHORT).show();
                //Log.e("pppp", "onFailure: " + t.getMessage());
            }
        });
    }

    //remove favorite.
    private void removeFavorite() {
        dialogRemove.show();
        Call<String> call = apiService.removeFavorite(accessToken, productId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.code() == 204) {
                        isFavorite = false;
                        imageFavorite.setImageResource(R.drawable.ic_favorite_un_select);
                        Toast.makeText(getApplicationContext(), "Removed favorite", Toast.LENGTH_SHORT).show();
                        dialogRemove.dismiss();
                    }
                } else {
                    isFavorite = true;
                    imageFavorite.setImageResource(R.drawable.ic_favorite_selected);
                    dialogRemove.dismiss();
                    Toast.makeText(getApplicationContext(), "Error connection. please try again", Toast.LENGTH_SHORT).show();
                    //Log.e("pppp", "Remove Else: " + response.code() + " = " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                t.printStackTrace();
                imageFavorite.setImageResource(R.drawable.ic_favorite_selected);
                dialogRemove.dismiss();
                Toast.makeText(getApplicationContext(), "Server problem!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //check exist user favorite
    private void checkExistUserFavorite() {
        if (!accessToken.equals("N/A")) {
            Call<ProductResponse> call = apiService.getAllUserFavorite(accessToken, page);
            call.enqueue(new Callback<ProductResponse>() {
                @Override
                public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                    if (response.isSuccessful()) {
                        totalPage = response.body().getMeta().getLastPage();
                        for (int i = 0; i < response.body().getData().size(); i++) {
                            favoriteIdList.add(response.body().getData().get(i).getId());
                        }

                        setUpFavoriteIcon();

                        page = page + 1;
                        if (page <= totalPage) {
                            checkExistUserFavorite();
                        }

                    } else {
                        Log.e("pppp", "else: " + response.code() + " = " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ProductResponse> call, Throwable t) {
                    t.printStackTrace();
                    Log.e("pppp", "onFailure: " + t.getMessage());
                }
            });


        }
    }

    private void setUpFavoriteIcon() {
        if (favoriteIdList.size() != 0) {
            Log.e("pppp", "productId: " + productId);
            for (int i = 0; i < favoriteIdList.size(); i++) {
                if (favoriteIdList.get(i) == productId) {
                    Log.e("pppp", "if productId: " + favoriteIdList.get(i));
                    isFavorite = true;
                    imageFavorite.setImageResource(R.drawable.ic_favorite_selected);
                    return;
                } else {
                    Log.e("pppp", "else productId: " + favoriteIdList.get(i));
                    isFavorite = false;
                    imageFavorite.setImageResource(R.drawable.ic_favorite_un_select);
                }
            }
        }
    }

    private void setUpRecycler() {
        adapter = new ContactAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


    }

    @SuppressLint("SetTextI18n")
    private void getIntentFromAnotherActivity() {
        ProductResponse.Data productResponse = (ProductResponse.Data) getIntent().getSerializableExtra("productList");
        if (productResponse != null) {
            productId = productResponse.getId();
            countView(productId);
            getPhone = productResponse.getContactphone();

            if (productResponse.getContactme() != null) {
                adapter.addItem(productResponse.getContactme().getDatacontact());
            }

            List<Banner> imageSliderList = new ArrayList<>();
            if (productResponse.getProductimages().size() != 0) {
                for (int i = 0; i < productResponse.getProductimages().size(); i++) {
                    imageSliderList.add(new RemoteBanner(productResponse.getProductimages().get(i)));
                }
                bannerSlider.setBanners(imageSliderList);
            }

            String priceFrom = productResponse.getPrice().get(0).getMin().substring(0, productResponse.getPrice().get(0).getMin().indexOf("."));
            String priceTo = productResponse.getPrice().get(0).getMax().substring(0, productResponse.getPrice().get(0).getMax().indexOf("."));

            textTitle.setText(productResponse.getTitle());
            textPrice.setText("Price: " + priceFrom + "$ - " + priceTo + "$");
            textDescription.setText(productResponse.getDescription());
            textAddress.setText("Contact Address: " + productResponse.getContactaddress());
            textName.setText("Contact Name: " + productResponse.getContactname());
            textPhone.setText("Contact Phone: " + productResponse.getContactphone());

            if (productResponse.getContactemail().equals("norton@null.com") || productResponse.getContactemail() == null) {
                textEmail.setVisibility(View.GONE);
            } else {
                textEmail.setVisibility(View.VISIBLE);
                textEmail.setText("Contact Email: " + productResponse.getContactemail());
            }

            if (productResponse.getContactmapcoordinate() != null) {
//                Bundle bundle = new Bundle();
//                bundle.putString("LatLng", productResponse.getContactmapcoordinate());
                ShowMapFragment showMapFragment = new ShowMapFragment();
//                showMapFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.layoutShowMap, showMapFragment)
                        .commit();
            }

        }
    }

    private void countView(int id) {
        UserInformationManager userInformationManager = UserInformationManager.getInstance(getSharedPreferences(UserInformationManager.PREFERENCES_USER_INFORMATION, MODE_PRIVATE));
        if (!userInformationManager.getUser().getAccessToken().equals("N/A")) {
            Retrofit.countView(userInformationManager.getUser().getAccessToken(), id);
        }
    }

    private void initView() {
        imageBack = findViewById(R.id.image_back);
        imageContact = findViewById(R.id.image_contact);
        imageFavorite = findViewById(R.id.image_favorite);
        textTitle = findViewById(R.id.textTitle);
        textDescription = findViewById(R.id.textDescription);
        textPrice = findViewById(R.id.textPrice);
        textName = findViewById(R.id.textName);
        textPhone = findViewById(R.id.textPhone);
        textEmail = findViewById(R.id.textEmail);
        textAddress = findViewById(R.id.textAddress);
        bannerSlider = findViewById(R.id.bannerSlider);
        buttonCall = findViewById(R.id.buttonCall);
        recyclerView = findViewById(R.id.recyclerView);
    }


    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
