package com.pose.server.core.member.infrastructure;

import com.pose.server.core.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    boolean existsByUserId(String userId);


    Optional<MemberEntity> findByUserId(String userId);
}