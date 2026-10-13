package csd230.lab1.entities;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Owning side of the Cart to Product many-to-many: it declares the cart_products join table.
 * A cart holds many products and a product can sit in many carts.
 */
@Entity
@Table(name = "cart_entity")
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // LinkedHashSet for NO duplicate items, insertion order kept
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "cart_products",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<ProductEntity> products = new LinkedHashSet<>();

    // Lab 3: the cart's owner. One cart per user, so the hardcoded "default cart"
    // from Lecture 2.5.1 is gone. @OneToOne defaults to EAGER, so the user comes back
    // with the cart and there is no lazy problem when the template reads it.
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public CartEntity() {}

    public CartEntity(Set<ProductEntity> products) { this.products = products; }

    public void addProduct(ProductEntity product) {
        this.products.add(product);
        product.getCarts().add(this); // Maintain the link on both sides
    }

    public void removeProduct(ProductEntity product) {
        this.products.remove(product);
        product.getCarts().remove(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Set<ProductEntity> getProducts() { return products; }
    public void setProducts(Set<ProductEntity> products) { this.products = products; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    // deliberately prints the username and not the user object: if UserEntity ever
    // printed its cart back, the two toString() calls would loop until StackOverflowError
    @Override
    public String toString() {
        return "CartEntity{id=" + id + ", products=" + products.size()
                + ", user=" + (user == null ? "none" : user.getUsername()) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartEntity other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() { return Objects.hash(getClass().getSimpleName()); }
}
