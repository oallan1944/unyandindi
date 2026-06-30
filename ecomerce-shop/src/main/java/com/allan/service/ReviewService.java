package com.allan.service;

import java.util.List;

import com.allan.model.Product;
import com.allan.model.Review;
import com.allan.model.User;
import com.allan.request.CreateReviewRequest;

public interface ReviewService {

    Review createReview(
            CreateReviewRequest req,
            User user,
            Product product);

    List<Review> getReviewByProductId(Long productId);

    Review updateReview(Long reviewId, String reviewText, double rating, Long userId) throws Exception;

    void deleteReview(Long reviewId, Long userId) throws Exception;

    Review getReviewById(Long reviewId) throws Exception;

}
