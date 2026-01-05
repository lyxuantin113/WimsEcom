package com.wims.backend.mapper;

import com.wims.backend.dto.request.BannerRequest;
import com.wims.backend.dto.response.BannerResponse;
import com.wims.backend.entity.Banner;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BannerMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "imageUrl", ignore = true)
    Banner toBanner(BannerRequest request);
    BannerResponse toBannerResponse(Banner banner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "imageUrl", ignore = true) // Tự xử lý ảnh thủ công ở Service rồi
    void updateBanner(@MappingTarget Banner banner, BannerRequest request);
}