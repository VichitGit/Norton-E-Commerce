package vichitpov.com.fbs.model;

/**
 * Created by VichitPov on 1/1/2018.
 */

public class UserModel {

    private String title;
    private String address;
    private String category;
    private String status;
    private int price;


    public UserModel(String title, String address, int price) {
        this.title = title;
        this.address = address;
        this.price = price;

    }

    public UserModel(String title, String address, int price, String category) {
        this.title = title;
        this.address = address;
        this.price = price;
        this.category = category;
    }

    public UserModel(String title, String address, int price, String category, String status) {
        this.title = title;
        this.address = address;
        this.price = price;
        this.category = category;
        this.status = status;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}