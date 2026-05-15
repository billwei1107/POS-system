/**
 * @file MemberRepository.java
 * @description 會員資料存取層 / Member repository
 */
package com.enterprise.crm.repository;

import com.enterprise.crm.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    boolean existsByMemberNo(String memberNo);

    boolean existsByPhone(String phone);

    Optional<Member> findByMemberNoAndActiveTrue(String memberNo);

    @Query("""
            SELECT m FROM Member m
            WHERE m.active = true
              AND (
                LOWER(m.memberNo) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(m.phone) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(m.cardNo) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(m.barcode) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY m.updatedAt DESC
            """)
    List<Member> search(@Param("query") String query, Pageable pageable);
}
