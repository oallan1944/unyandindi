package com.allan.mapper;

import com.allan.model.Product;
import com.allan.dto.CartItem;


/**
 * Builds a trusted {@link CartItem} from a server-loaded {@link Product}.
 *
 * <p><strong>Security:</strong> this is the only place a {@link CartItem}
 * should be constructed for evaluation purposes. {@code unitPrice},
 * {@code sellerId}, and {@code categoryId} always come from the freshly
 * loaded {@code Product} entity — never from client-submitted JSON. Only
 * {@code quantity} (and indirectly, via {@code productId}, which product to
 * look up) originates from the client. See {@link CartContextBuilder}.
 *
 * <p><strong>Integration note:</strong> assumes {@code Product} exposes
 * {@code getId()}, {@code getSellingPrice()}, {@code getSeller().getId()},
 * and {@code getCategory().getId()}. Adjust here if your entity's accessor
 * names differ.
 */
public final class CartItemMapper {

    private CartItemMapper() {
        // static utility — no instances
    }

    /**
     * @param product     authoritative, server-loaded product
     * @param quantity    client-supplied quantity (already validated > 0)
     * @param lineDiscount any per-line discount already applied (whole UGX shillings, may be 0)
     */
    public static CartItem fromProduct(Product product, int quantity, long lineDiscount) {
        long unitPrice = product.getSellingPrice();
        long lineTotal = (unitPrice * quantity) - lineDiscount;

        if (lineTotal < 0) {
            throw new IllegalArgumentException(
                    "lineDiscount (" + lineDiscount + ") exceeds line subtotal for product " + product.getId());
        }

        return new CartItem(
                product.getId(),
                product.getSeller().getId(),
                product.getCategory().getId(),
                quantity,
                unitPrice,
                lineTotal
        );
    }
}