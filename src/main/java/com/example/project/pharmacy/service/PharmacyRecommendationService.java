package com.example.project.pharmacy.service;

import com.example.project.api.dto.DocumentDto;
import com.example.project.api.dto.KakaoApiResponseDto;
import com.example.project.api.service.KakaoAddressSearchService;
import com.example.project.direction.dto.OutputDto;
import com.example.project.direction.entity.Direction;
import com.example.project.direction.service.DirectionService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;


    public List<OutputDto> recommendPharmacyList(String address){
        // 주소입력 -> 위치기반데이터(위도, 경도값)으로 변환
        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        if(Objects.isNull(kakaoApiResponseDto) || Objects.isNull(kakaoApiResponseDto.getDocumentList()) || kakaoApiResponseDto.getDocumentList().isEmpty()) {
            log.error("[PharmacyRecommendationService recommendPharmacyList fail] Input address: " + address);
            return Collections.emptyList();
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);

        // 위치기반데이터로 가까운 약국들을 찾는다.
        List<Direction> directionList = directionService.buildDirectionList(documentDto);

        // 저장 및 반환
        return directionService.saveAll(directionList)
            .stream()
            .map(this::convertToOutputDto)
            .collect(Collectors.toList());
    }

    public void recommendPharmacyListByByCategorySearchApi(String address){
        // 주소입력 -> 위치기반데이터(위도, 경도값)으로 변환
        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        if(Objects.isNull(kakaoApiResponseDto) || Objects.isNull(kakaoApiResponseDto.getDocumentList()) || kakaoApiResponseDto.getDocumentList().isEmpty()) {
            log.error("[PharmacyRecommendationService recommendPharmacyList fail] Input address: " + address);
            return;
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);

        // 위치기반데이터로 가까운 약국들을 찾는다. (카카오 카테고리 검색 api 로)
        List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);

        // 저장.
        directionService.saveAll(directionList);

    }

    private OutputDto convertToOutputDto(Direction direction) {
        OutputDto outputDto = OutputDto.builder()
            .pharmacyName(direction.getTargetPharmacyName())
            .pharmacyAddress(direction.getTargetAddress())
            .directionUrl("todo")
            .roadViewUrl("todo")
            .distance(String.format("%.2f km", direction.getDistance()))
            .build();

        return outputDto;
    }
}
