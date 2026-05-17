package com.pokectfree.workspace.dto;

import com.pokectfree.workspace.domain.WorkspaceMember;
import lombok.Getter;

@Getter
public class WorkspaceMemberResponseDto {

    /** 멤버 유저 ID */
    private final Long userId;

    /** 멤버 닉네임 */
    private final String nickname;

    /** 멤버 이메일 */
    private final String email;

    /** 워크스페이스 내 권한 */
    private final String role;

    /**
     * @param member 응답으로 변환할 워크스페이스 멤버십 엔티티
     */
    public WorkspaceMemberResponseDto(WorkspaceMember member) {
        this.userId = member.getUser().getId();
        this.nickname = member.getUser().getNickname();
        this.email = member.getUser().getEmail();
        this.role = member.getRole().name();
    }
}
