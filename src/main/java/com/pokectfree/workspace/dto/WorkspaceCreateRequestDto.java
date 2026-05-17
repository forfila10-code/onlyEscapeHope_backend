package com.pokectfree.workspace.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkspaceCreateRequestDto {

    /** 생성할 워크스페이스 이름 */
    private String name;
}
