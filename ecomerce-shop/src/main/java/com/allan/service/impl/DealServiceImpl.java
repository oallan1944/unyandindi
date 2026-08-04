package com.allan.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.model.Deal;
import com.allan.model.HomeCategory;
import com.allan.repository.DealRepository;
import com.allan.repository.HomeCategoryRepository;
import com.allan.service.DealService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final HomeCategoryRepository homeCategoryRepository;

    @Override
    public List<Deal> getDeals() {
        return dealRepository.findAllWithCategory();
    }

    @Override
    @Transactional
    public Deal createDeal(Deal deal) {
        // Resolve the managed HomeCategory BEFORE constructing/saving the
        // Deal — deal.getCategory() at this point is a transient, partial
        // object deserialized straight from the frontend's JSON body
        // ({ id: X }, everything else null). Saving with that raw reference
        // directly (as the old code did) is what produced the broken link.
        HomeCategory category = homeCategoryRepository.findById(deal.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No category found with id: " + deal.getCategory().getId()));

        Deal newDeal = new Deal();
        newDeal.setDiscount(deal.getDiscount());
        newDeal.setCategory(category);

        // Single save, single transaction — no window for a partial/orphaned
        // write, unlike the old two-save version.
        return dealRepository.save(newDeal);
    }

    @Override
    @Transactional
    public Deal updateDeal(Deal deal, Long id) throws Exception {
        Deal existingDeal = dealRepository.findById(id)
                .orElseThrow(() -> new Exception("Deal not found"));

        if (deal.getDiscount() != null) {
            existingDeal.setDiscount(deal.getDiscount());
        }

        if (deal.getCategory() != null && deal.getCategory().getId() != null) {
            HomeCategory category = homeCategoryRepository.findById(deal.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No category found with id: " + deal.getCategory().getId()));
            existingDeal.setCategory(category);
        }

        return dealRepository.save(existingDeal);
    }

    @Override
    @Transactional
    public void deleteDeal(Long id) throws Exception {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new Exception("Deal not found"));
        dealRepository.delete(deal);
    }
}