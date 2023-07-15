package com.example.sasnm;

public class productDTO {
    String id, name, des, img, price;

    public productDTO(String id, String name, String des, String img, String priceInString) {
        this.id = id;
        this.name = name;
        this.des = des;
        this.img = img;
        this.price = priceInString;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImg() {
        return img;
    }

    public String getDes() {
        return des;
    }

    public String getPrice() {
        return price;
    }
}