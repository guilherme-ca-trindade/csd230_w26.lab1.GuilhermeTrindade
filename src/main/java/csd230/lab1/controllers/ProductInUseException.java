package csd230.lab1.controllers;

/**
 * Lab 4: a product that has already been sold cannot be deleted - an order is a receipt and
 * must not change. The same rule BookController enforces with a flash message, reported here
 * as 409 Conflict.
 */
public class ProductInUseException extends RuntimeException {
    public ProductInUseException(Long id, int orderCount) {
        super("Could not delete product with ID: " + id
                + " - it already appears on " + orderCount + " order(s)");
    }
}
