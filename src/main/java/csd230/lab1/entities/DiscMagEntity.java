package csd230.lab1.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity @DiscriminatorValue("DISCMAG")
public class DiscMagEntity extends MagazineEntity {
    // Lab 4: Boolean, so a REST payload that omits hasDisc is "unknown" rather than false
    private Boolean hasDisc;

    public DiscMagEntity() {}

    // see MagazineEntity for why the parameters are named with @JsonProperty
    public DiscMagEntity(@JsonProperty("title") String t,
                         @JsonProperty("price") Double p,
                         @JsonProperty("copies") Integer c,
                         @JsonProperty("orderQty") Integer o,
                         @JsonProperty("currentIssue") LocalDateTime d,
                         @JsonProperty("hasDisc") Boolean h) {
        super(t, p, c, o, d);
        this.hasDisc = h;
    }

    public Boolean isHasDisc() { return hasDisc; }
    public void setHasDisc(Boolean h) { this.hasDisc = h; }

    @Override public String toString() { return "DiscMag{disc=" + hasDisc + ", " + super.toString() + "}"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscMagEntity other)) return false;
        if (getId() != null || other.getId() != null) return super.equals(o);
        // Objects.equals and not ==, now that hasDisc is a wrapper
        return Objects.equals(hasDisc, other.hasDisc) && super.equals(o);
    }

    @Override public int hashCode() { return super.hashCode(); }
}
