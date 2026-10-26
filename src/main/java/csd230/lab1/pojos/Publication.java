package csd230.lab1.pojos;

import java.util.Objects;

public abstract class Publication extends Product {
    private String title = "";
    // Lecture 2.9: wrapper types so a REST payload that omits price or copies can be
    // represented as null instead of being rejected by Jackson
    private Double price = 0.0;
    private Integer copies = 0;

    public Publication() {
    }

    public Publication(String title, Double price, Integer copies) {
        this.title = title;
        this.price = price;
        this.copies = copies;
    }

    @Override
    public void initialize() {
        System.out.println("Enter Title:");
        this.title = getInput("Available Title"); // "Available Title" is default if empty
    }

    // Helper used by subclasses during initialize
    protected void initPriceCopies() {
        System.out.println("Enter copies:");
        this.copies = getInput(0);

        System.out.println("Enter price:");
        this.price = getInput(0.0);
    }

    @Override
    public void edit() {
        System.out.println("Edit Title [" + this.title + "]:");
        this.title = getInput(this.title);

        System.out.println("Edit Price [" + this.price + "]:");
        this.price = getInput(this.price);

        System.out.println("Edit Copies [" + this.copies + "]:");
        this.copies = getInput(this.copies);
    }

    @Override
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    @Override
    public String toString() {
        return "Publication{title='" + title + "', price=" + price + ", copies=" + copies + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Publication)) return false;
        Publication that = (Publication) o;
        // Objects.equals and not Double.compare - either side can be null now
        return Objects.equals(price, that.price) &&
                Objects.equals(copies, that.copies) &&
                Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, price, copies);
    }
}
