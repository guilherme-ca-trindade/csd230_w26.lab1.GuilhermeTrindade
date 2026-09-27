package csd230.lab1.entities;

import csd230.lab1.pojos.SaleableItem;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Root of the entity hierarchy. SINGLE_TABLE inheritance: every subclass row lives in
 * the "products" table and is told apart by the product_type discriminator column.
 * The inverse (non owning) side of the Cart to Product many-to-many.
 */
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type", discriminatorType = DiscriminatorType.STRING)
public abstract class ProductEntity implements Serializable, SaleableItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private String productId;

    // inverse side: CartEntity.products owns the cart_products join table
    @ManyToMany(mappedBy = "products")
    private Set<CartEntity> carts = new HashSet<>();

    public ProductEntity() {}

    public ProductEntity(String productId) { this.productId = productId; }

    public Set<CartEntity> getCarts() { return carts; }
    public void setCarts(Set<CartEntity> carts) { this.carts = carts; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    @Override public String toString() { return "ProductEntity{id=" + id + ", productId='" + productId + "'}"; }

    /**
     * Identity based equality. Two managed entities are the same row only when both have
     * the same non-null id; a transient entity is only equal to itself. Deliberately NOT
     * comparing the carts collection, which would recurse through CartEntity.equals().
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ProductEntity other)) return false;
        return id != null && id.equals(other.getId());
    }

    /**
     * Constant per class so the hash does not change when Hibernate assigns the id
     * after the entity has already been put in a HashSet / LinkedHashSet.
     */
    @Override
    public int hashCode() { return Objects.hash(getClass().getSimpleName()); }
}
