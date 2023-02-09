package com.example.project.direction.service;

import com.example.project.api.dto.DocumentDto;
import com.example.project.api.service.KakaoCategorySearchService;
import com.example.project.direction.direction.DirectionRepository;
import com.example.project.direction.entity.Direction;
import com.example.project.pharmacy.service.PharmacySearchService;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    // 약국은 최대 3개까지만
    // 반경 10km이내 약국까지만
    private static final int MAX_SEARCH_COUNT = 3;
    private static final double RADIUS_KM = 10.0;

    private final PharmacySearchService pharmacySearchService;
    private final DirectionRepository directionRepository;
    private final KakaoCategorySearchService kakaoCategorySearchService;
    private final Base62Service base62Service;

    public List<Direction> saveAll(List<Direction> directionList){
        // 추천 결과 저장
        if(CollectionUtils.isEmpty(directionList)) return Collections.emptyList();
        return directionRepository.saveAll(directionList);
    }

    public Direction findById(String encodedId){
        Long decodeId = base62Service.decodeDirectionId(encodedId);
        return directionRepository.findById(decodeId).orElse(null);
    }

    public List<Direction> buildDirectionList(DocumentDto documentDto) {
        // documentDto : 고객 주소의 위도, 경도값을 가짐

        if (Objects.isNull(documentDto)) {
            return Collections.emptyList();
        }

        // 약국 데이터 조회
        // 거리계산 알고리즘을 이용하여, 고객과 약국 사이의 거리를 계산하고 sort
        return pharmacySearchService.searchPharmacyDtoList()
            .stream().map(pharmacyDto -> Direction.builder()
                .inputAddress(documentDto.getAddressName())
                .inputLatitude(documentDto.getLatitude())
                .inputLongitude(documentDto.getLongitude())
                .targetAddress(pharmacyDto.getPharmacyAddress())
                .targetPharmacyName(pharmacyDto.getPharmacyName())
                .targetLatitude(pharmacyDto.getLatitude())
                .targetLongitude(pharmacyDto.getLongitude())
                .distance(
                    calculateDistance(documentDto.getLatitude(), documentDto.getLongitude()
                        , pharmacyDto.getLatitude(), pharmacyDto.getLongitude())
                )
                .build())
            .filter(direction -> direction.getDistance() <= RADIUS_KM)
            .sorted(Comparator.comparing(Direction::getDistance))
            .limit(MAX_SEARCH_COUNT)
            .collect(Collectors.toList());
    }

    // Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double earthRadius = 6371; //Kilometers
        return earthRadius * Math.acos(
            Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(
                lon1 - lon2));
    }

    // pharmacy search by category kakao api
    public List<Direction> buildDirectionListByCategoryApi(DocumentDto inputDocumentDto) {
        if(Objects.isNull(inputDocumentDto)) return Collections.emptyList();

        return kakaoCategorySearchService
            .requestPharmacyCategorySearch(inputDocumentDto.getLatitude(), inputDocumentDto.getLongitude(), RADIUS_KM)
            .getDocumentList()
            .stream().map(resultDocumentDto ->
                Direction.builder()
                    .inputAddress(inputDocumentDto.getAddressName())
                    .inputLatitude(inputDocumentDto.getLatitude())
                    .inputLongitude(inputDocumentDto.getLongitude())
                    .targetPharmacyName(resultDocumentDto.getPlaceName())
                    .targetAddress(resultDocumentDto.getAddressName())
                    .targetLatitude(resultDocumentDto.getLatitude())
                    .targetLongitude(resultDocumentDto.getLongitude())
                    .distance(resultDocumentDto.getDistance() * 0.001) // km 단위
                    .build())
            .limit(MAX_SEARCH_COUNT)
            .collect(Collectors.toList());
    }
}
