package com.pokectfree.workspace.controller;

import com.pokectfree.login.dto.CustomOAuth2User;
import com.pokectfree.workspace.dto.WorkspaceCreateRequestDto;
import com.pokectfree.workspace.dto.WorkspaceInviteRequestDto;
import com.pokectfree.workspace.dto.WorkspaceInviteTokenResponseDto;
import com.pokectfree.workspace.dto.WorkspaceJoinInfoDto;
import com.pokectfree.workspace.dto.WorkspaceMemberResponseDto;
import com.pokectfree.workspace.dto.WorkspaceResponseDto;
import com.pokectfree.workspace.dto.WorkspaceUpdateRequestDto;
import com.pokectfree.workspace.service.WorkspaceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    /**
     * OWNER 권한을 가진 유저가 워크스페이스 이름을 수정합니다.
     *
     * @param workspaceId 수정할 워크스페이스 ID
     * @param requestDto  변경할 이름을 담은 요청 데이터
     * @param principal   JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 수정된 워크스페이스 정보
     */
    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponseDto> updateWorkspace(
            @PathVariable Long workspaceId,
            @RequestBody WorkspaceUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        WorkspaceResponseDto result = workspaceService.updateWorkspaceName(userId, workspaceId, requestDto);
        return ResponseEntity.ok(result);
    }

    /**
     * OWNER만 워크스페이스를 삭제할 수 있습니다.
     * 연관된 거래 내역과 멤버 정보를 모두 함께 제거합니다.
     *
     * @param workspaceId 삭제할 워크스페이스 ID
     * @param principal   JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 204 No Content
     */
    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        workspaceService.deleteWorkspace(userId, workspaceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 워크스페이스 초대 토큰을 생성합니다. (OWNER 전용)
     * 기존 토큰을 폐기하고 새 토큰(48시간 유효)을 발급합니다.
     *
     * @param workspaceId 초대 대상 워크스페이스 ID
     * @param principal   JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 토큰 값과 만료 시각
     */
    @PostMapping("/{workspaceId}/invite-token")
    public ResponseEntity<WorkspaceInviteTokenResponseDto> generateInviteToken(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        WorkspaceInviteTokenResponseDto result = workspaceService.generateInviteToken(userId, workspaceId);
        return ResponseEntity.status(201).body(result);
    }

    /**
     * 초대 토큰으로 워크스페이스 기본 정보를 조회합니다. (가입 페이지 미리보기용)
     *
     * @param token     초대 URL에 포함된 토큰
     * @param principal JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 워크스페이스 이름, 멤버 수, 이미 가입 여부
     */
    @GetMapping("/join/{token}")
    public ResponseEntity<WorkspaceJoinInfoDto> getJoinInfo(
            @PathVariable String token,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        WorkspaceJoinInfoDto result = workspaceService.getJoinInfo(userId, token);
        return ResponseEntity.ok(result);
    }

    /**
     * 초대 토큰으로 워크스페이스에 MEMBER로 가입합니다.
     *
     * @param token     초대 URL에 포함된 토큰
     * @param principal JWT 인증 필터에서 주입한 로그인 유저 정보
     * @return 가입된 워크스페이스 정보
     */
    @PostMapping("/join/{token}")
    public ResponseEntity<WorkspaceResponseDto> joinByToken(
            @PathVariable String token,
            @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = principal.getUser().getId();
        WorkspaceResponseDto result = workspaceService.joinByToken(userId, token);
        return ResponseEntity.status(201).body(result);
    }
}
