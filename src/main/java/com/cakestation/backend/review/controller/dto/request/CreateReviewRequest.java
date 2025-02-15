package com.cakestation.backend.review.controller.dto.request;

import com.cakestation.backend.review.domain.DesignSatisfaction;
import com.cakestation.backend.review.domain.Distance;
import com.cakestation.backend.review.domain.Tag;
import com.cakestation.backend.review.service.dto.CreateReviewDto;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    private List<String> reviewImages = new ArrayList<>(); // 리뷰 이미지 base64 리스트
    private int cakeNumber; // 케이크 호수
    private String sheetType; // 시트 종류
    private String requestOption; // 추가 옵션
    private DesignSatisfaction designSatisfaction; // 만족도
    private int score; // 별점
    private List<Tag> tags;
    private String content; // 하고 싶은 말

    public CreateReviewDto toServiceDto(Long storeId, CreateReviewRequest createReviewRequest) {
        return CreateReviewDto.builder()
                .storeId(storeId)
                .reviewImages(createReviewRequest.getReviewImages())
                .cakeNumber(createReviewRequest.getCakeNumber())
                .sheetType(createReviewRequest.getSheetType())
                .requestOption(createReviewRequest.getRequestOption())
                .designSatisfaction(createReviewRequest.getDesignSatisfaction())
                .score(createReviewRequest.getScore())
                .tags(createReviewRequest.getTags())
                .content(createReviewRequest.getContent())
                .build();
    }
}
