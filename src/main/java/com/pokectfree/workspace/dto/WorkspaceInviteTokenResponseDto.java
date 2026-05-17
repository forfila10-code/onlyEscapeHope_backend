package com.pokectfree.workspace.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkspaceInviteTokenResponseDto {

    /** 초대 URL에 포함될 고유 토큰 */
    private String token;

    /** 토큰 만료 시각 (KST) */
    private LocalDateTime expiresAt;
}
