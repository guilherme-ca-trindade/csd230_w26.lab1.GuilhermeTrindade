package csd230.lab1.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity @DiscriminatorValue("MAGAZINE")
public class MagazineEntity extends PublicationEntity {
    // Lab 4: Integer, for the same reason PublicationEntity.copies is - a REST client may
    // leave the order quantity out, or send it as an explicit null
    private Integer orderQty;
    private LocalDateTime currentIssue;

    public MagazineEntity() {}

    /**
     * Lab 4: the @JsonProperty names are not decoration. Spring Boot compiles with
     * -parameters, and Jackson 3 treats a class's single non-default constructor as a
     * properties-based creator, binding it by those parameter names - so without these
     * annotations a POST of {"title":...,"price":...} bound nothing and died on the old
     * primitive "double p". Naming the parameters makes the creator bind the real fields.
     */
    public MagazineEntity(@JsonProperty("title") String t,
                          @JsonProperty("price") Double p,
                          @JsonProperty("copies") Integer c,
                          @JsonProperty("orderQty") Integer o,
                          @JsonProperty("currentIssue") LocalDateTime d) {
        super(t, p, c);
        this.orderQty = o;
        this.currentIssue = d;
    }

    public Integer getOrderQty() { return orderQty; }
    public void setOrderQty(Integer o) { this.orderQty = o; }
    public void setCurrentIssue(LocalDateTime d) { this.currentIssue = d; }
    public LocalDateTime getCurrentIssue() { return currentIssue; }

    @Override public String toString() { return "Mag{orderQty=" + orderQty + ", issue=" + currentIssue + ", " + super.toString() + "}"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MagazineEntity other)) return false;
        if (getId() != null || other.getId() != null) return super.equals(o);
        // Objects.equals and not ==, now that orderQty is a wrapper
        return Objects.equals(orderQty, other.orderQty)
                && Objects.equals(currentIssue, other.currentIssue) && super.equals(o);
    }

    @Override public int hashCode() { return super.hashCode(); }
}
