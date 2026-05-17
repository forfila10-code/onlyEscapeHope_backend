package com.pokectfree.workspace.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkspaceUpdateRequestDto {

    /** 변경할 워크스페이스 이름 */
    private String name;
}
