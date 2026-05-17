package com.pokectfree.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkspaceJoinInfoDto {

    /** 가입 대상 워크스페이스 이름 */
    private String workspaceName;

    /** 현재 참여 중인 멤버 수 */
    private int memberCount;

    /** 요청자가 이미 해당 워크스페이스 멤버인지 여부 */
    private boolean alreadyJoined;
}
