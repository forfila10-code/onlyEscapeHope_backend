package com.pokectfree.workspace.dto;

import com.pokectfree.workspace.domain.WorkspaceMember;
import lombok.Getter;

@Getter
public class WorkspaceResponseDto {

    /** 워크스페이스 ID */
    private final Long id;

    /** 화면에 표시할 워크스페이스 이름 */
    private final String name;

    /** 현재 로그인 유저의 워크스페이스 내 권한 */
    private final String role;

    /**
     * @param member 현재 로그인 유저의 워크스페이스 멤버십 엔티티
     */
    public WorkspaceResponseDto(WorkspaceMember member) {
        this.id = member.getWorkspace().getId();
        this.name = member.getWorkspace().getName();
        this.role = member.getRole().name();
    }
}
