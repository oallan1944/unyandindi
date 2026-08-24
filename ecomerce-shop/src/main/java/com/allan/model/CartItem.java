package com.allan.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // one cart can have many items
    @ManyToOne
    @JsonIgnore
    private Cart cart;

    @ManyToOne
    private Product product;

    private String size;

    private int quantity = 1;

    // Widened from Integer to Long: at UGX magnitudes (~3,700x USD), a
    // moderately priced item at a bulk quantity can exceed Integer's
    // ~2.1 billion ceiling. Int overflow here would silently wrap to a
    // garbage or negative value rather than failing loudly.
    private Long mrpPrice;

    private Long sellingPrice;

    private Long userId;

}