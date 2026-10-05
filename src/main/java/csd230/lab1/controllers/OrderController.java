package csd230.lab1.controllers;

import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.OrderEntity;
import csd230.lab1.entities.ProductEntity;
import csd230.lab1.entities.PublicationEntity;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.OrderEntityRepository;
import csd230.lab1.repositories.ProductEntityRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lab 2: turning a cart into a sale. No class level @RequestMapping because the two URLs
 * live under different paths (/cart and /orders).
 */
@Controller
public class OrderController {

    private final OrderEntityRepository orderRepository;
    private final CartEntityRepository cartRepository;
    private final ProductEntityRepository productRepository;

    public OrderController(OrderEntityRepository orderRepository,
                           CartEntityRepository cartRepository,
                           ProductEntityRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    /**
     * POST because this changes data - a GET link could be pre-fetched or replayed by the
     * browser and charge the customer twice. @Transactional so the order, the new stock
     * counts and the emptied cart all commit together or not at all.
     */
    @PostMapping("/cart/checkout")
    @Transactional
    public String checkout(RedirectAttributes flash) {

        // ------------------------------------------------------------------
        // THE CART - fetch join so its products are loaded
        // ------------------------------------------------------------------
        CartEntity cart = cartRepository.findByIdWithProducts(CartController.DEFAULT_CART_ID)
                .orElse(null);

        if (cart == null || cart.getProducts().isEmpty()) {
            flash.addFlashAttribute("error", "Your cart is empty - there is nothing to check out.");
            return "redirect:/cart";
        }

        // ------------------------------------------------------------------
        // THE ORDER - price it up, take the stock, fill in the items
        // ------------------------------------------------------------------
        OrderEntity order = new OrderEntity();
        order.setOrderDate(LocalDateTime.now());

        double total = 0.0;
        List<ProductEntity> purchased = new ArrayList<>();
        List<String> outOfStock = new ArrayList<>();

        for (ProductEntity product : cart.getProducts()) {

            // only publications have copies - a ticket never runs out
            if (product instanceof PublicationEntity publication) {

                if (publication.getCopies() <= 0) {
                    outOfStock.add(publication.getTitle());
                    continue;   // skip it rather than failing the whole order
                }
                // sellItem() does the same decrement but reports with System.out,
                // which is no use to a browser
                publication.setCopies(publication.getCopies() - 1);
            }

            total += product.getPrice();
            order.addProduct(product);
            purchased.add(product);
        }

        if (purchased.isEmpty()) {
            flash.addFlashAttribute("error", "Everything in your cart is out of stock, so no order was created.");
            return "redirect:/cart";
        }

        // round to cents - 24.99 + 29.99 in doubles gives 54.980000000000004
        order.setTotalAmount(Math.round(total * 100.0) / 100.0);

        // ------------------------------------------------------------------
        // PERSIST - order first, then the stock, then the emptied cart
        // ------------------------------------------------------------------
        OrderEntity savedOrder = orderRepository.save(order);   // owns the join table
        productRepository.saveAll(purchased);                   // stores the new copies counts

        // only remove what was actually bought - out of stock items stay in the cart
        for (ProductEntity bought : purchased) {
            cart.removeProduct(bought);
        }
        cartRepository.save(cart);   // deletes the cart_products rows

        if (!outOfStock.isEmpty()) {
            flash.addFlashAttribute("error", "Out of stock, left in your cart: "
                    + String.join(", ", outOfStock));
        }

        return "redirect:/orders/" + savedOrder.getId();
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        OrderEntity order = orderRepository.findByIdWithProducts(id).orElse(null);
        model.addAttribute("order", order);
        return "orderDetails";
    }
}
