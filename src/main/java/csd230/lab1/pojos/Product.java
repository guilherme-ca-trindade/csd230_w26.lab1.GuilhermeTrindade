package csd230.lab1.pojos;

import java.io.Serializable;

// Marked abstract because it implements SaleableItem but doesn't implement getPrice()
// (Price is defined in children: Ticket and Publication)
public abstract class Product extends Editable implements SaleableItem, Serializable {
    private Long id;
    private String productId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
