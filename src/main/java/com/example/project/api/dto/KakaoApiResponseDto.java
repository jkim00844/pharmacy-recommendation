package com.example.project.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KakaoApiResponseDto {

    private MetaDto metaDto;

    private List<DocumentDto> documentDtoList;
}
