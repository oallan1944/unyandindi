// src/main/java/com/allan/mapper/SellerMapper.java
package com.allan.mapper;

import com.allan.dto.*;
import com.allan.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SellerMapper {

    // ── Public storefront view ──────────────────────────
    public SellerPublicDTO toPublicDTO(Seller seller) {
        SellerPublicDTO dto = new SellerPublicDTO();
        dto.setId(seller.getId());
        dto.setSellerName(seller.getSellerName());

        if (seller.getBusinessDetails() != null) {
            dto.setBusinessName(seller.getBusinessDetails().getBusinessName());
            dto.setLogo(seller.getBusinessDetails().getLogo());
            dto.setBanner(seller.getBusinessDetails().getBanner());
        }
        return dto;
    }

    // ── Private profile view — seller themselves only ───
    public SellerProfileDTO toProfileDTO(Seller seller) {
        SellerProfileDTO dto = new SellerProfileDTO();
        dto.setId(seller.getId());
        dto.setSellerName(seller.getSellerName());
        dto.setEmail(seller.getEmail());
        dto.setMobile(seller.getMobile());
        dto.setGSTIN(seller.getGSTIN());
        dto.setRole(seller.getRole());
        dto.setEmailVerified(seller.isEmailVerified());
        dto.setAccountStatus(seller.getAccountStatus());
        dto.setCreatedAt(seller.getCreatedAt());
        dto.setUpdatedAt(seller.getUpdatedAt());

        if (seller.getBusinessDetails() != null) {
            dto.setBusinessDetails(toBusinessDetailsDTO(seller.getBusinessDetails()));
        }
        if (seller.getBankDetails() != null) {
            dto.setBankDetails(toBankDetailsDTO(seller.getBankDetails()));
        }
        if (seller.getPickupAddress() != null) {
            dto.setPickupAddress(toAddressDTO(seller.getPickupAddress()));
        }
        return dto;
    }

    // ── Admin listing view ───────────────────────────────
    public SellerSummaryDTO toSummaryDTO(Seller seller) {
        SellerSummaryDTO dto = new SellerSummaryDTO();
        dto.setId(seller.getId());
        dto.setSellerName(seller.getSellerName());
        dto.setEmail(seller.getEmail());
        dto.setMobile(seller.getMobile());
        dto.setAccountStatus(seller.getAccountStatus());
        dto.setEmailVerified(seller.isEmailVerified());
        dto.setCreatedAt(seller.getCreatedAt());
        return dto;
    }

    public List<SellerSummaryDTO> toSummaryDTOList(List<Seller> sellers) {
        return sellers.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ── Seller report view ───────────────────────────────
    public SellerReportDTO toReportDTO(SellerReport report) {
        SellerReportDTO dto = new SellerReportDTO();
        dto.setSeller(toSummaryDTO(report.getSeller()));
        dto.setTotalOrders(report.getTotalOrders());
        dto.setCompletedOrders(report.getCompletedOrders());
        dto.setPendingOrders(report.getPendingOrders());
        dto.setCanceledOrders(report.getCanceledOrders());
        dto.setTotalRevenue(report.getTotalRevenue());
        dto.setTotalProducts(report.getTotalProducts());
        dto.setGeneratedAt(report.getGeneratedAt());
        return dto;
    }

    // ── Helpers ───────────────────────────────────────────
    private BusinessDetailsDTO toBusinessDetailsDTO(BusinessDetails b) {
        BusinessDetailsDTO dto = new BusinessDetailsDTO();
        dto.setBusinessName(b.getBusinessName());
        dto.setBusinessEmail(b.getBusinessEmail());
        dto.setBusinessMobile(b.getBusinessMobile());
        dto.setBusinessAddress(b.getBusinessAddress());
        dto.setLogo(b.getLogo());
        dto.setBanner(b.getBanner());
        return dto;
    }

    private BankDetailsDTO toBankDetailsDTO(BankDetails b) {
        BankDetailsDTO dto = new BankDetailsDTO();
        dto.setAccountHolderName(b.getAccountHolderName());
        dto.setAccountNumber(b.getAccountNumber());
        dto.setIfscCode(b.getIfscCode());
        return dto;
    }

    private AddressDTO toAddressDTO(Address a) {
        AddressDTO dto = new AddressDTO();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setAddress(a.getAddress());
        dto.setCity(a.getCity());
        dto.setState(a.getState());
        dto.setPinCode(a.getPinCode());
        dto.setMobile(a.getMobile());
        dto.setLocality(a.getLocality());
        return dto;
    }
}