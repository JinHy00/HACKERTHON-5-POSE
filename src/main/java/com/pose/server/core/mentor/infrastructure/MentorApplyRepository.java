package com.pose.server.core.mentor.infrastructure;

import com.pose.server.core.member.domain.MemberEntity;
import com.pose.server.core.mentor.domain.MentorApplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorApplyRepository extends JpaRepository<MentorApplyEntity, Long> {
    Optional<MentorApplyEntity> findByMember(MemberEntity member);

    @Query("SELECT m FROM MentorApplyEntity m JOIN FETCH m.member")
    List<MentorApplyEntity> findAllWithMember();
}