package csd230.lab1.pojos;

public interface SaleableItem {
    void sellItem();

    // Lecture 2.9: Double and not double. A REST client is allowed to leave a price out
    // of its JSON, and a primitive has nowhere to put that missing value.
    Double getPrice();
}
