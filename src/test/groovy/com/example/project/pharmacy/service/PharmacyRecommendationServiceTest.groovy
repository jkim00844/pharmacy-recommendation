package com.example.project.pharmacy.service

import com.example.project.AbstractIntegrationContainerBaseTest
import org.springframework.beans.factory.annotation.Autowired

class PharmacyRecommendationServiceTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    PharmacyRecommendationService pharmacyRecommendationService

    def "정상적인 주소를 입력했을 경우, 약국 3개 추천"() {

        given:
        boolean actualResult = false

        when:
        def searchResult = pharmacyRecommendationService.recommendPharmacyList(inputAddress)

        then:
        if(searchResult == null) actualResult = false
        else actualResult = searchResult.size() == 3

        where:
        inputAddress                            | expectedResult
        "서울 특별시 성북구 종암동"                   | true
        "서울 성북구 종암동 91"                     | true
        "서울 대학로"                             | true
        "서울 성북구 종암동 잘못된 주소"               | false
        "광진구 구의동 251-45"                     | true
        "광진구 구의동 251-455555"                 | false
        ""                                      | false
    }

    def "정상적인 주소를 입력했을 경우, 약국 3개 추천 by 카카오 카테고리 API"() {

        given:
        boolean actualResult = false

        when:
        def searchResult = pharmacyRecommendationService.recommendPharmacyListByByCategorySearchApi(inputAddress)

        then:
        if(searchResult == null) actualResult = false
        else actualResult = searchResult.getDocumentList().size() == 3

        where:
        inputAddress                            | expectedResult
        "서울 특별시 성북구 종암동"                   | true
        "서울 성북구 종암동 91"                     | true
        "서울 대학로"                             | true
        "서울 성북구 종암동 잘못된 주소"               | false
        "광진구 구의동 251-45"                     | true
        "광진구 구의동 251-455555"                 | false
        ""                                      | false
    }
}
