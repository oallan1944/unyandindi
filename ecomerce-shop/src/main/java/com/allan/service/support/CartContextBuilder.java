package com.allan.service.support;

import com.allan.dto.CartContext;
import com.allan.dto.CartItem;
import com.allan.mapper.CartItemMapper;
import com.allan.dto.CartLineRequest;
import com.allan.model.Product;
import com.allan.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Builds a trusted {@link CartContext} for pre-order coupon validation from
 * a client-supplied list of {@code productId}/{@code quantity} pairs.
 *
 * <p>Every {@link CartItem} is constructed via {@code CartItemMapper} from a
 * freshly loaded {@link Product} — price, seller, and category always come
 * from the database, never from the client. This is the only place a
 * client-submitted cart shape is allowed to touch evaluation logic; the
 * redemption path (post-order) must instead rebuild from the persisted
 * {@code Order}, via {@code OrderContextService} — never from a
 * client-resubmitted item list.
 *
 * <p><strong>Integration note:</strong> assumes a {@code ProductRepository}
 * with {@code findById(Long)} returning {@code Product}, and
 * {@code Product} exposing the getters {@code CartItemMapper} depends on
 * ({@code getSellingPrice()}, {@code getSeller().getId()},
 * {@code getCategory().getId()}). Adjust {@code CartItemMapper} if your
 * actual entity's accessor names differ.
 */
@Component
@RequiredArgsConstructor
public class CartContextBuilder {

    private final ProductRepository productRepository;

    public CartContext buildFromLines(Long userId, String couponCode, List<CartLineRequest> lines) {
        List<CartItem> items = lines.stream()
                .map(line -> {
                    Product product = productRepository.findById(line.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.productId()));
                    return CartItemMapper.fromProduct(product, line.quantity(), 0L);
                })
                .toList();

        long subtotal = items.stream().mapToLong(CartItem::lineTotal).sum();
        return new CartContext(userId, items, subtotal, couponCode, LocalDateTime.now());
    }
}