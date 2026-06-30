package com.allan.model;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // one user has one cart
    @OneToOne
    private User user;
    // one cart has multiple cart items
    // however cart items are related to one cart

    // cascade: changes in cart items reflect here
    // delete item, deletes in this set as well
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    private double totalSellingPrice;

    private int totalItem;

    private int totalMrpPrice;

    private int discount;

    private String couponCode;

}
