package com.allan.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.allan.domain.HomeCategorySection;
import com.allan.model.Deal;
import com.allan.model.Home;
import com.allan.model.HomeCategory;
import com.allan.repository.DealRepository;
import com.allan.service.HomeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final DealRepository dealRepository;

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

    List<HomeCategory> dealCategories = allCategories.stream()
            .filter(c -> c.getSection() == HomeCategorySection.DEALS)
            .collect(Collectors.toList());

    // ✅ fetch once — avoids calling findAll() twice
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
