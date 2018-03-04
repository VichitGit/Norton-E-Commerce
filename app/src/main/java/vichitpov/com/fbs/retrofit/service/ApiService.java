package vichitpov.com.fbs.retrofit.service;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import vichitpov.com.fbs.retrofit.response.CategoriesResponse;
import vichitpov.com.fbs.retrofit.response.ProductPostedResponse;
import vichitpov.com.fbs.retrofit.response.ProductResponse;
import vichitpov.com.fbs.retrofit.response.UserInformationResponse;

/**
 * Created by VichitPov on 2/25/18.
 */

public interface ApiService {

    @GET("buys")
    Call<ProductResponse> singlePageBuyer();

    @GET("sells")
    Call<ProductResponse> singlePageSeller();

    @GET("buys")
    Call<ProductResponse> seeMoreBuyerLoadByPagination(@Query("page") int page);

    @GET("sells")
    Call<ProductResponse> seeMoreSellerLoadByPagination(@Query("page") int page);

    @GET("user")
    Call<UserInformationResponse> getUserInformation(@Header("access-token") String accessToken);

    @GET("user/sells")
    Call<ProductResponse> getProductSellSpecificUser(@Header("access-token") String accessToken, @Query("page") int page);

    @GET("user/buys")
    Call<ProductResponse> getProductBuySpecificUser(@Header("access-token") String accessToken, @Query("page") int page);

    @GET("categoies/parents")
    Call<CategoriesResponse> getAllCategories();

    @PUT("user")
    @FormUrlEncoded
    Call<UserInformationResponse> updateUser(@Header("access-token") String accessToken, @Field("first_name") String firstName,
                                             @Field("last_name") String lastName, @Field("gender") String gender,
                                             @Field("address") String address, @Field("city") String city);

    @POST("user/post")
    @FormUrlEncoded
    Call<ProductPostedResponse> postToBuy(@Header("access-token") String accessToken, @Field("post_type") String postType, @Field("title") String title,
                                          @Field("category_id") int category, @Field("description") String description, @Field("price_from") int priceFrom,
                                          @Field("price_to") int priceTo, @Field("contact_name") String contactName,
                                          @Field("contact_phone") String contactPhone, @Field("contact_email") String contactEmail,
                                          @Field("contact_address") String contactAddress, @Field("contact_address_map_coordinate") String addressMap);

    @Multipart
    @POST("images/profile")
    Call<UserInformationResponse> updateUserImageProfile(@Header("access-token") String accessToken,
                                                         @Part MultipartBody.Part imageProfile);

}
