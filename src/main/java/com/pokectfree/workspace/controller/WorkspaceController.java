package com.pokectfree.workspace.controller;

import com.pokectfree.login.dto.CustomOAuth2User;
import com.pokectfree.workspace.dto.WorkspaceCreateRequestDto;
import com.pokectfree.workspace.dto.WorkspaceInviteRequestDto;
import com.pokectfree.workspace.dto.WorkspaceMemberResponseDto;
import com.pokectfree.workspace.dto.WorkspaceResponseDto;
import com.pokectfree.workspace.service.WorkspaceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    /**
     * 로그인 유저가 참여 중인 워크스페이스 목록을 반환합니다.
     *
     * @param principal JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 현재 유저가 접근 가능한 워크스페이스 목록
     */
    @GetMapping
    public ResponseEntity<List<WorkspaceResponseDto>> getMyWorkspaces(
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        workspaceService.ensurePersonalWorkspace(principal.getUser());
        return ResponseEntity.ok(workspaceService.getMyWorkspaces(userId));
    }

    /**
     * 새 공유 워크스페이스를 생성하고 요청자를 OWNER로 등록합니다.
     *
     * @param requestDto 프론트에서 전달한 워크스페이스 이름 데이터
     * @param principal JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 생성된 워크스페이스 정보
     */
    @PostMapping
    public ResponseEntity<WorkspaceResponseDto> createWorkspace(
            @RequestBody WorkspaceCreateRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        WorkspaceResponseDto result = workspaceService.createWorkspace(userId, requestDto);
        return ResponseEntity.status(201).body(result);
    }

    /**
     * 결제자 선택 등에 사용할 워크스페이스 멤버 목록을 조회합니다.
     *
     * @param workspaceId 멤버 목록을 조회할 워크스페이스 ID
     * @param principal JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 워크스페이스 멤버 목록
     */
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<WorkspaceMemberResponseDto>> getMembers(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(workspaceService.getMembers(userId, workspaceId));
    }

    /**
     * 이메일로 기존 유저를 워크스페이스 멤버로 추가합니다.
     *
     * @param workspaceId 멤버를 추가할 워크스페이스 ID
     * @param requestDto 초대할 유저 이메일과 역할(role)을 담은 요청 데이터
     * @param principal JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 새로 추가된 멤버 정보
     */
    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<WorkspaceMemberResponseDto> addMember(
            @PathVariable Long workspaceId,
            @RequestBody WorkspaceInviteRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        WorkspaceMemberResponseDto result = workspaceService.addMember(userId, workspaceId, requestDto);
        return ResponseEntity.status(201).body(result);
    }
}
