package com.cakestation.backend.review.controller;

import com.cakestation.backend.common.ApiResponse;
import com.cakestation.backend.review.dto.request.CreateReviewDto;
import com.cakestation.backend.review.service.ReviewService;
import com.cakestation.backend.user.service.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/stores/{storeId}/reviews")
    public ResponseEntity<ApiResponse<Long>> uploadReview(@PathVariable Long storeId,
                                                          @ModelAttribute List<MultipartFile> reviewImages,
                                                          @ModelAttribute CreateReviewDto createReviewDto,
                                                          HttpServletRequest req){
        Long reviewId = reviewService.saveReview(storeId, createReviewDto, reviewImages, req);
        return ResponseEntity.ok().body(
                new ApiResponse<Long>(HttpStatus.CREATED.value(),"리뷰 등록 성공",reviewId));
    }
}
