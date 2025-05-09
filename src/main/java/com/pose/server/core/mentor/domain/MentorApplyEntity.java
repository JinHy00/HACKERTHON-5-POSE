package com.pose.server.core.mentor.domain;

import com.pose.server.core.member.domain.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table(name = "mentor_apply")
public class MentorApplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applyId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private MemberEntity member;

    @Column(name = "mentor_career", length = 250)
    private String mentorCareer;

    @Column(name = "affiliation", length = 20)
    private String affiliation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 이력서 파일을 BLOB 형식으로 저장
    @Lob
    @Column(name = "resume_file", columnDefinition = "MEDIUMBLOB")
    private byte[] resumeFile; // 파일

    @Column(name = "resume_filename")
    private String resumeFilename; // 파일명


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

}