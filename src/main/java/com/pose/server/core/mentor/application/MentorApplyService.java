package com.pose.server.core.mentor.application;

import com.pose.server.core.member.domain.MemberEntity;
import com.pose.server.core.member.infrastructure.MemberRepository;
import com.pose.server.core.mentor.domain.MentorApplyEntity;
import com.pose.server.core.mentor.infrastructure.MentorApplyRepository;
import com.pose.server.core.mentor.payload.MentorApplyDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorApplyService {
    private final MentorApplyRepository mentorApplyRepository;
    private final MemberRepository memberRepository; // 회원 정보 조회용

    /**
     * 멘토 신청 처리 로직
     *
     * @param userId 로그인된 사용자 ID
     * @param dto    신청 폼에서 입력한 경력, 소속, 이력서파일
     */
    @Transactional
    public void applyForMentor(String userId, MentorApplyDto dto) {
        // 1. 사용자 조회
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 기존에 신청한 이력이 있는지 확인
        mentorApplyRepository.findByMember(member).ifPresent(apply -> {
            if (apply.getStatus() == MentorApplyEntity.Status.PENDING) {
                throw new IllegalStateException("이미 멘토 신청을 하셨습니다. 현재 대기 중 입니다.");
            }
            if (apply.getStatus() == MentorApplyEntity.Status.APPROVED) {
                throw new IllegalStateException("이미 멘토로 승인되었습니다.");
            }
        });


        // 3. 이력서 파일을 byte[]로 변환
        byte[] resumeBytes = null;
        String resumeFilename = null;
        if (dto.getResumeFile() != null && !dto.getResumeFile().isEmpty()) {
            try {
                resumeBytes = dto.getResumeFile().getBytes();
                resumeFilename = dto.getResumeFile().getOriginalFilename();
            } catch (IOException e) {
                throw new RuntimeException("이력서를 처리하는 중 오류가 발생했습니다.", e);
            }
        }

        // 4. 신청 엔티티 생성 및 저장
        MentorApplyEntity apply = MentorApplyEntity.builder()
                .member(member)
                .mentorCareer(dto.getMentorCareer())
                .affiliation(dto.getAffiliation())
                .resumeFile(resumeBytes) // 이력서 파일
                .resumeFilename(resumeFilename) // 파일명
                .status(MentorApplyEntity.Status.PENDING) // 기본 상태는 대기중
                .build();

        mentorApplyRepository.save(apply);
    }

    /**
     * 멘토 신청 이력서 다운로드
     */
    public byte[] downloadResume(Long applyId) {
        MentorApplyEntity apply = mentorApplyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멘토 신청 정보가 존재하지 않습니다."));

        return apply.getResumeFile();  // 파일 내용 반환
    }


    /**
     * 사용자의 멘토 신청 내역 조회
     * (마이페이지에서 신청 상태 확인용 등)
     */
    public Optional<MentorApplyEntity> getMyApplication(String userId) {
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return mentorApplyRepository.findByMember(member);
    }

    // 관리자 승인 / 거절
    // 두개의 엔티티를 다 바꿔서 dirty checking -> 추후 수정
    @Transactional
    public void approveApplication(Long applyId) {
        MentorApplyEntity apply = mentorApplyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 없습니다."));
        apply.setStatus(MentorApplyEntity.Status.APPROVED);
        // 멘토 권한 부여
        MemberEntity member = apply.getMember();
        member.setRole(MemberEntity.Role.MENTOR); // role = MENTOR
    }

    @Transactional
    public void rejectApplication(Long applyId) {
        MentorApplyEntity apply = mentorApplyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 없습니다."));
        apply.setStatus(MentorApplyEntity.Status.REJECTED);
    }

    // 멘토 신청 목록
    public List<MentorApplyDto> getAllApplications() {

        var list = mentorApplyRepository.findAllWithMember().stream()
                .map(entity -> {
                    MemberEntity member = entity.getMember();
                    String resumeFilename = entity.getResumeFilename(); // 이력서 파일명
                    // 파일 다운로드 URL 생성 (컨트롤러에서 다운로드 URL을 처리하도록 설정)
                    String resumeDownloadUrl = "/admin/mentor/" + entity.getApplyId() + "/download";
                    String statusText = switch (entity.getStatus()) {
                        case PENDING -> "대기중";
                        case APPROVED -> "승인";
                        case REJECTED -> "거절";
                    };

                    return MentorApplyDto.builder()
                            .applyId(entity.getApplyId())
                            .userId(member.getUserId())
                            .name(member.getName())
                            .email(member.getEmail())
                            .tel(member.getTel())
                            .affiliation(entity.getAffiliation())
                            .mentorCareer(entity.getMentorCareer())
                            .status(entity.getStatus().name())
                            .statusText(statusText)
                            .createdAt(entity.getCreatedAt())
                            .resumeFilename(resumeFilename) // 파일명 추가
                            .resumeDownloadUrl(resumeDownloadUrl) // 다운로드 URL 추가
                            .build();
                })
                .toList();
        log.info(list.toString());
        return list;
    }

    // 신청 삭제
    public void deleteApplication(Long id) {
        MentorApplyEntity entity = mentorApplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멘토 신청입니다."));
        if (entity.getStatus() == MentorApplyEntity.Status.APPROVED) {
            throw new IllegalStateException("승인된 신청은 삭제할 수 없습니다.");
        }
        mentorApplyRepository.delete(entity);
    }
}