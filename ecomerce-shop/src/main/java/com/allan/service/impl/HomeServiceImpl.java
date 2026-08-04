package com.allan.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.allan.domain.HomeCategorySection;
import com.allan.model.Deal;
import com.allan.model.Home;
import com.allan.model.HomeCategory;
import com.allan.repository.DealRepository;
import com.allan.repository.ProductRepository;
import com.allan.service.HomeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final DealRepository dealRepository;
    private final ProductRepository productRepository;

    @Override
    public Home createHomePageData(List<HomeCategory> allCategories) {

        List<HomeCategory> gridCategories = allCategories.stream()
                .filter(c -> c.getSection() == HomeCategorySection.GRID)
                .collect(Collectors.toList());

        List<HomeCategory> shopByCategories = allCategories.stream()
                .filter(c -> c.getSection() == HomeCategorySection.SHOP_BY_CATEGORIES)
                .collect(Collectors.toList());

        List<HomeCategory> electricCategories = allCategories.stream()
                .filter(c -> c.getSection() == HomeCategorySection.ELECTRIC_CATEGORIES)
                .collect(Collectors.toList());

        // Attach one featured product per electric-category tile so the
        // frontend "Buy now" button has somewhere real to navigate to.
        // Categories with no matching products yet just get an empty list —
        // the frontend treats that as "Coming soon", not an error.
        //
        // NOTE: one query per category (N+1). Fine at current scale; if
        // electricCategories grows to dozens of tiles, batch this into a
        // single findFirstByCategoryIdIn(...)-style query instead.
        electricCategories.forEach(category -> {
            productRepository
                    .findFirstByCategory_CategoryIdOrderByCreatedAtDesc(category.getCategoryId())
                    .ifPresent(product -> category.setProducts(List.of(product)));
        });

        List<HomeCategory> dealCategories = allCategories.stream()
                .filter(c -> c.getSection() == HomeCategorySection.DEALS)
                .collect(Collectors.toList());

        List<Deal> existingDeals = dealRepository.findAllWithCategory();

        List<Deal> createdDeals;
        if (existingDeals.isEmpty()) {
            List<Deal> deals = dealCategories.stream()
                    .map(category -> new Deal(null, 10, category))
                    .collect(Collectors.toList());
            createdDeals = dealRepository.saveAll(deals);
        } else {
            createdDeals = existingDeals;
        }

        Home home = new Home();
        home.setGrid(gridCategories);
        home.setShopByCategories(shopByCategories);
        home.setElectricCategories(electricCategories);
        home.setDealCategories(dealCategories);
        home.setDeals(createdDeals);

        return home;
    }
}