package com.example.project.pharmacy.service;

import com.example.project.api.dto.DocumentDto;
import com.example.project.api.dto.KakaoApiResponseDto;
import com.example.project.api.service.KakaoAddressSearchService;
import com.example.project.direction.entity.Direction;
import com.example.project.direction.service.DirectionService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;

    public void recommendPharmacyList(String address){
        // 주소입력 -> 위치기반데이터(위도, 경도값)으로 변환
        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        if(Objects.isNull(kakaoApiResponseDto) || Objects.isNull(kakaoApiResponseDto.getDocumentList())) {
            log.error("[PharmacyRecommendationService recommendPharmacyList fail] Input address: " + address);
            return;
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);

        // 위치기반데이터로 가까운 약국들을 찾는다.
        List<Direction> directionList = directionService.buildDirectionList(documentDto);

        // 저장.
        directionService.saveAll(directionList);

    }
}
