package com.pokectfree.workspace.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkspaceInviteRequestDto {

    /** 초대할 기존 유저의 이메일 */
    private String email;

    /** 부여할 워크스페이스 권한: OWNER 또는 MEMBER */
    private String role;
}
