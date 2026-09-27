package csd230.lab1.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity @DiscriminatorValue("DISCMAG")
public class DiscMagEntity extends MagazineEntity {
    private boolean hasDisc;

    public DiscMagEntity() {}
    public DiscMagEntity(String t, double p, int c, int o, LocalDateTime d, boolean h) { super(t, p, c, o, d); this.hasDisc = h; }

    public boolean isHasDisc() { return hasDisc; }
    public void setHasDisc(boolean h) { this.hasDisc = h; }

    @Override public String toString() { return "DiscMag{disc=" + hasDisc + ", " + super.toString() + "}"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscMagEntity other)) return false;
        if (getId() != null || other.getId() != null) return super.equals(o);
        return hasDisc == other.hasDisc && super.equals(o);
    }

    @Override public int hashCode() { return super.hashCode(); }
}
