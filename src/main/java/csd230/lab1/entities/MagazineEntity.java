package csd230.lab1.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity @DiscriminatorValue("MAGAZINE")
public class MagazineEntity extends PublicationEntity {
    private int orderQty;
    private LocalDateTime currentIssue;

    public MagazineEntity() {}
    public MagazineEntity(String t, double p, int c, int o, LocalDateTime d) { super(t, p, c); this.orderQty = o; this.currentIssue = d; }

    public int getOrderQty() { return orderQty; }
    public void setOrderQty(int o) { this.orderQty = o; }
    public void setCurrentIssue(LocalDateTime d) { this.currentIssue = d; }
    public LocalDateTime getCurrentIssue() { return currentIssue; }

    @Override public String toString() { return "Mag{orderQty=" + orderQty + ", issue=" + currentIssue + ", " + super.toString() + "}"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MagazineEntity other)) return false;
        if (getId() != null || other.getId() != null) return super.equals(o);
        return orderQty == other.orderQty && Objects.equals(currentIssue, other.currentIssue) && super.equals(o);
    }

    @Override public int hashCode() { return super.hashCode(); }
}
