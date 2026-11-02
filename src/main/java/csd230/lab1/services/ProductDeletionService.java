package csd230.lab1.services;

import csd230.lab1.controllers.ProductInUseException;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.OrderEntity;
import csd230.lab1.entities.ProductEntity;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.OrderEntityRepository;
import csd230.lab1.repositories.ProductEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lab 4: deleting a product is the one CRUD operation that is not a one-liner. Every product
 * is the inverse side of the cart_products many-to-many, so a plain deleteById() on a product
 * that is in somebody's cart fails with a referential integrity violation (a 500 to the
 * caller). The rules are the same ones BookController.deleteBook applies on the HTML side, so
 * they live here once instead of being copied into every REST controller.
 */
@Service
public class ProductDeletionService {

    private final ProductEntityRepository productRepository;
    private final CartEntityRepository cartRepository;
    private final OrderEntityRepository orderRepository;

    public ProductDeletionService(ProductEntityRepository productRepository,
                                  CartEntityRepository cartRepository,
                                  OrderEntityRepository orderRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * @throws ProductInUseException if the product already appears on an order
     */
    @Transactional
    public void deleteProduct(Long id) {
        // loaded inside this @Transactional, so the lazy carts collection below still has a
        // session to load from
        ProductEntity product = productRepository.findById(id).orElse(null);

        if (product == null) {
            return;   // already gone - deleting twice is not an error
        }

        List<OrderEntity> ordersWithProduct = orderRepository.findAllContainingProduct(id);
        if (!ordersWithProduct.isEmpty()) {
            throw new ProductInUseException(id, ordersWithProduct.size());
        }

        // not cart.removeProduct(product) - that also edits product.getCarts(), the collection
        // this loop is walking, which would throw a ConcurrentModificationException
        for (CartEntity cart : product.getCarts()) {
            cart.getProducts().remove(product);
            cartRepository.save(cart);
        }
        productRepository.delete(product);
    }
}
