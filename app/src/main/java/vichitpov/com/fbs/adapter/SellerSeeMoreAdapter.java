package vichitpov.com.fbs.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import vichitpov.com.fbs.R;
import vichitpov.com.fbs.base.Convert;
import vichitpov.com.fbs.base.IntentData;
import vichitpov.com.fbs.base.Retrofit;
import vichitpov.com.fbs.callback.MyOnClickListener;
import vichitpov.com.fbs.callback.OnClickDelete;
import vichitpov.com.fbs.callback.OnLoadMore;
import vichitpov.com.fbs.preference.UserInformationManager;
import vichitpov.com.fbs.retrofit.response.ProductResponse;
import vichitpov.com.fbs.ui.activities.DetailProductActivity;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by VichitPov on 2/27/18.
 */

public class SellerSeeMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_ITEM = 1;
    private static final int VIEW_PROGRESS = 0;

    public static final int gridLayoutManager = 10;
    public static final int linearLayoutManager = 15;

    private List<ProductResponse.Data> productList;
    private ProductResponse.Data productResponse;
    private Context context;
    private OnLoadMore onLoadMore;
    private MyOnClickListener myOnClickListener;
    private int checkLayoutManager;

    private int visibleThreshold = 3;
    private boolean loading = false;
    private OnClickDelete onClickDelete;

    public SellerSeeMoreAdapter(Context context, RecyclerView recyclerView, int checkLayoutCheck) {
        this.productList = new ArrayList<>();
        this.context = context;
        this.checkLayoutManager = checkLayoutCheck;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //get position items in LayoutManager
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        loading = true;
                        onLoadMore.setOnLoadMore();

                    }
                }
            });
        } else if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //get position items in LayoutManager
                    int totalItemCount = gridLayoutManager.getItemCount();
                    int lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();

                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        loading = true;
                        onLoadMore.setOnLoadMore();

                    }
                }
            });
        }
    }

    public void onLoaded() {
        loading = false;
    }

    public boolean isLoading() {
        return loading;
    }

    public void addProgressBar() {
        productList.add(null);
        notifyItemInserted(productList.size() - 1);
    }


    public void onLoadMore(OnLoadMore onLoadMore) {
        this.onLoadMore = onLoadMore;
    }

    //after progressBar show, it will remove progressBar
    public void removeProgressBar() {
        productList.remove(productList.size() - 1);
        notifyItemRemoved(productList.size() - 1);
    }


    public void addMoreItems(List<ProductResponse.Data> productList) {
        this.productList.addAll(productList);
        notifyDataSetChanged();
    }

    public void notifyItem(){
    }

    public void refreshData(int position) {
        this.productList.remove(position);
        this.notifyItemRemoved(position);
        this.notifyItemRangeChanged(position, productList.size());
    }

    @Override
    public int getItemViewType(int position) {
        return (productList.get(position) != null) ? VIEW_ITEM : VIEW_PROGRESS;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;

        if (viewType == VIEW_ITEM) {
            if (checkLayoutManager == linearLayoutManager) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_seller_see_more, parent, false);
                vh = new ProductViewHolder(view);
            } else if (checkLayoutManager == gridLayoutManager) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_user_post_sold, parent, false);
                vh = new ProductGridViewHolder(view);
            }

        } else if (viewType == VIEW_PROGRESS) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layour_progress_pagination, parent, false);
            vh = new ProgressPagination(view);

        }
        return vh;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        productResponse = productList.get(position);


        if (holder instanceof ProductViewHolder) {

            ProductViewHolder productViewHolder = (ProductViewHolder) holder;
            productViewHolder.title.setText(productResponse.getTitle());

            String priceFrom = productResponse.getPrice().get(0).getMin().substring(0, productResponse.getPrice().get(0).getMin().indexOf("."));
            String priceTo = productResponse.getPrice().get(0).getMax().substring(0, productResponse.getPrice().get(0).getMax().indexOf("."));

            productViewHolder.price.setText(priceFrom + "$" + " - " + priceTo + "$");

            if (productResponse.getCreateddate() != null) {
                String subDate = productResponse.getCreateddate().getDate().substring(0, 10);
                String dateConverted = Convert.dateConverter(subDate);
                productViewHolder.date.setText("Posted: " + dateConverted);
            }

            if (productResponse.getContactaddress() != null) {
                productViewHolder.address.setText(productResponse.getContactaddress());
            }
            if (productResponse.getProductimages().size() != 0) {
                Picasso.with(context)
                        .load(productResponse.getProductimages().get(0))
                        .resize(200, 200)
                        .centerCrop()
                        .error(R.drawable.ic_unavailable)
                        .into(productViewHolder.thumbnail);

            }


        } else if (holder instanceof ProductGridViewHolder) {
            ProductGridViewHolder productGridViewHolder = (ProductGridViewHolder) holder;

            String priceFrom = productResponse.getPrice().get(0).getMin().substring(0, productResponse.getPrice().get(0).getMin().indexOf("."));
            String priceTo = productResponse.getPrice().get(0).getMax().substring(0, productResponse.getPrice().get(0).getMax().indexOf("."));

            productGridViewHolder.textTitle.setText(productResponse.getTitle());
            productGridViewHolder.textPrice.setText(priceFrom + "$" + " - " + priceTo + "$");
            productGridViewHolder.textStatus.setText(productResponse.getStatus());
            productGridViewHolder.textAddress.setText(productResponse.getContactaddress());

            if (productResponse.getProductimages().size() != 0) {
                Picasso.with(context)
                        .load(productResponse.getProductimages().get(0))
                        .resize(200, 200)
                        .centerCrop()
                        .error(R.drawable.ic_unavailable)
                        .into(productGridViewHolder.thumbnail);
            }

        } else if (holder instanceof ProgressPagination) {
            ProgressPagination progressPagination = (ProgressPagination) holder;
            progressPagination.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        if (productList != null) {
            return productList.size();
        }
        return 0;
    }

    public void mySetOnClick(MyOnClickListener myOnClickListener) {
        this.myOnClickListener = myOnClickListener;

    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        private TextView title, address, price, date;
        private ImageView more, thumbnail;

        ProductViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textTitle);
            address = itemView.findViewById(R.id.textAddress);
            price = itemView.findViewById(R.id.textPrice);
            date = itemView.findViewById(R.id.textDate);
            thumbnail = itemView.findViewById(R.id.imageThumbnail);
            more = itemView.findViewById(R.id.imageMore);

            itemView.setOnClickListener(view -> {

                Intent intent = new Intent(context, DetailProductActivity.class);
                intent.putExtra("productList", productResponse);
                context.startActivity(intent);

                UserInformationManager userInformationManager = UserInformationManager.getInstance(context.getSharedPreferences(UserInformationManager.PREFERENCES_USER_INFORMATION, MODE_PRIVATE));
                if (!userInformationManager.getUser().getAccessToken().equals("N/A")) {
                    Retrofit.countView(userInformationManager.getUser().getAccessToken(), productList.get(getAdapterPosition()).getId());
                }

            });

            more.setOnClickListener(view -> {
                myOnClickListener.setOnViewClick(getAdapterPosition(), productList.get(getAdapterPosition()).getId(), view);


            });
        }
    }

    public void setOnDeleteClickListener(OnClickDelete onClickDelete) {
        this.onClickDelete = onClickDelete;
    }


    class ProductGridViewHolder extends RecyclerView.ViewHolder {

        private ImageView thumbnail;
        private TextView textTitle, textAddress, textPrice, textEdit, textDelete, textStatus;

        ProductGridViewHolder(View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textTitle);
            textAddress = itemView.findViewById(R.id.textAddress);
            textPrice = itemView.findViewById(R.id.textPrice);
            textEdit = itemView.findViewById(R.id.textEdit);
            textDelete = itemView.findViewById(R.id.textDelete);
            textStatus = itemView.findViewById(R.id.textStatus);
            thumbnail = itemView.findViewById(R.id.imageThumbnail);

            textEdit.setOnClickListener(view -> {
                IntentData.sendProduct(context, productList.get(getAdapterPosition()));
            });

            textDelete.setOnClickListener(view ->
                    onClickDelete.setOnClick(productList.get(getAdapterPosition()).getId(), getAdapterPosition()));
        }
    }

    class ProgressPagination extends RecyclerView.ViewHolder {

        private ProgressBar progressBar;

        ProgressPagination(View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.progressBar);

        }
    }

}
