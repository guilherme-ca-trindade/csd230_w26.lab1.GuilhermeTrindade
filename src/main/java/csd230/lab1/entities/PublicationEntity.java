package csd230.lab1.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.Objects;

@Entity
public abstract class PublicationEntity extends ProductEntity {
    private String title;
    // Lecture 2.9: Double/Integer so the REST model can carry a null price or copies count
    @Column(name = "pub_price") private Double price;
    private Integer copies;

    public PublicationEntity() {}
    public PublicationEntity(String t, Double p, Integer c) { this.title = t; this.price = p; this.copies = c; }

    @Override public void sellItem() {
        // copies can be null on a row created through the JSON API, so check before unboxing
        if (copies != null && copies > 0) { copies--; System.out.println("Sold '" + title + "'. Remaining copies: " + copies); }
        else { System.out.println("Cannot sell '" + title + "'. Out of stock."); }
    }
    @Override public Double getPrice() { return price; }

    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public void setPrice(Double p) { this.price = p; }
    public Integer getCopies() { return copies; }
    public void setCopies(Integer c) { this.copies = c; }

    @Override public String toString() {
        return "Pub{title='" + title + "', price=" + price + ", copies=" + copies + ", " + super.toString() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicationEntity other)) return false;
        if (getId() != null || other.getId() != null) return super.equals(o);
        // both still transient: fall back to the business key, null safe now that the
        // fields are wrapper types
        return Objects.equals(other.price, price) && Objects.equals(other.copies, copies)
                && Objects.equals(title, other.title);
    }

    @Override public int hashCode() { return super.hashCode(); }
}
