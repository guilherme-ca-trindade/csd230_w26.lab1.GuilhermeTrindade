package csd230.lab1.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Lab 2: a completed purchase - the cart at the moment of checkout, so the cart can be emptied later
 * without losing the record of the sale.
 */
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    // owning side, like CartEntity but its own join table, so ProductEntity has no matching "orders" field
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<ProductEntity> products = new LinkedHashSet<>();

    public OrderEntity() {}

    public OrderEntity(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public void addProduct(ProductEntity product) {
        this.products.add(product); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public Set<ProductEntity> getProducts() { return products; }
    public void setProducts(Set<ProductEntity> products) { this.products = products; }

    public int getItemCount() { return products.size(); }

    @Override
    public String toString() {
        return "OrderEntity{id=" + id + ", totalAmount=" + totalAmount
                + ", orderDate=" + orderDate + ", products=" + products.size() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderEntity other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() { return Objects.hash(getClass().getSimpleName()); }
}
