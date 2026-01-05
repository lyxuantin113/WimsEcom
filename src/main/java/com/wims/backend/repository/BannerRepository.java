package com.wims.backend.repository;

import com.wims.backend.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findAllByActiveTrueOrderByPriorityAsc();
}
