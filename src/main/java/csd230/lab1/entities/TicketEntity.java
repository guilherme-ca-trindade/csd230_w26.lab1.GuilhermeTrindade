package csd230.lab1.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.Objects;

@Entity @DiscriminatorValue("TICKET")
public class TicketEntity extends ProductEntity {
    private String description;
    // Lecture 2.9: wrapper type, so a null price survives the round trip through JSON
    @Column(name = "ticket_price") private Double price;

    public TicketEntity() {}
    public TicketEntity(String d, Double p) { this.description = d; this.price = p; }

    @Override public void sellItem() { System.out.println("Selling Ticket: " + description + " for $" + price); }
    @Override public Double getPrice() { return price; }

    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public void setPrice(Double p) { this.price = p; }

    @Override public String toString() { return "Ticket{desc='" + description + "', price=" + price + ", " + super.toString() + "}"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketEntity other)) return false;
        if (getId() != null || other.getId() != null) return super.equals(o);
        return Objects.equals(other.price, price) && Objects.equals(description, other.description);
    }

    @Override public int hashCode() { return super.hashCode(); }
}
