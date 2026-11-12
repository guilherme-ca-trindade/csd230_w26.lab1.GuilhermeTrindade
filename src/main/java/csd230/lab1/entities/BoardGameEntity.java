package csd230.lab1.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Lab 5: the niche product for this store.
 *
 * It extends PublicationEntity rather than ProductEntity directly because a boxed game is
 * sold exactly like a publication is - it has a title, a price and a number of copies on the
 * shelf - so those three shared fields are inherited instead of being declared again.
 *
 * Shared fields:  id, productId, productType (ProductEntity) + title, price, copies
 *                 (PublicationEntity).
 * Unique fields:  designer, minPlayers, maxPlayers, playTimeMinutes, cooperative, releaseDate.
 *
 * Every field a REST client can set is a wrapper type. Jackson 3 refuses to map an explicit
 * JSON null onto a primitive, so {"cooperative": null} would be a 400 with boolean here.
 */
@Entity
@DiscriminatorValue("BOARDGAME")
public class BoardGameEntity extends PublicationEntity {

    private String designer;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer playTimeMinutes;
    private Boolean cooperative;
    private LocalDate releaseDate;

    public BoardGameEntity() {}

    /**
     * The @JsonProperty names are load-bearing, not decoration. Spring Boot compiles with
     * -parameters and Jackson 3 treats a class's single non-default constructor as a
     * properties-based creator, binding it by those parameter names - so without them a POST
     * would bind nothing through the creator. Same fix as MagazineEntity.
     */
    public BoardGameEntity(@JsonProperty("title") String title,
                           @JsonProperty("price") Double price,
                           @JsonProperty("copies") Integer copies,
                           @JsonProperty("designer") String designer,
                           @JsonProperty("minPlayers") Integer minPlayers,
                           @JsonProperty("maxPlayers") Integer maxPlayers,
                           @JsonProperty("playTimeMinutes") Integer playTimeMinutes,
                           @JsonProperty("cooperative") Boolean cooperative,
                           @JsonProperty("releaseDate") LocalDate releaseDate) {
        super(title, price, copies);
        this.designer = designer;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.playTimeMinutes = playTimeMinutes;
        this.cooperative = cooperative;
        this.releaseDate = releaseDate;
    }

    public String getDesigner() { return designer; }
    public void setDesigner(String designer) { this.designer = designer; }

    public Integer getMinPlayers() { return minPlayers; }
    public void setMinPlayers(Integer minPlayers) { this.minPlayers = minPlayers; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public Integer getPlayTimeMinutes() { return playTimeMinutes; }
    public void setPlayTimeMinutes(Integer playTimeMinutes) { this.playTimeMinutes = playTimeMinutes; }

    public Boolean isCooperative() { return cooperative; }
    public void setCooperative(Boolean cooperative) { this.cooperative = cooperative; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    @Override
    public String toString() {
        return "BoardGame{designer='" + designer + "', players=" + minPlayers + "-" + maxPlayers
                + ", minutes=" + playTimeMinutes + ", coop=" + cooperative
                + ", released=" + releaseDate + ", " + super.toString() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardGameEntity other)) return false;
        if (getId() != null || other.getId() != null) return super.equals(o);
        // Objects.equals, not ==, because these are all wrapper types
        return Objects.equals(designer, other.designer)
                && Objects.equals(minPlayers, other.minPlayers)
                && Objects.equals(maxPlayers, other.maxPlayers)
                && Objects.equals(playTimeMinutes, other.playTimeMinutes)
                && Objects.equals(cooperative, other.cooperative)
                && Objects.equals(releaseDate, other.releaseDate)
                && super.equals(o);
    }

    @Override public int hashCode() { return super.hashCode(); }
}
