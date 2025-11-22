package com.example.multithreading_final.repository;

import com.example.multithreading_final.model.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {

    // На будущее: получать все контакты по стартовому URL
    List<ContactInfo> findBySourceUrl(String sourceUrl);
}
