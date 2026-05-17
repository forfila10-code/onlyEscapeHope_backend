package com.pokectfree.workspace.service;

import com.pokectfree.login.domain.User;
import com.pokectfree.login.repository.UserRepository;
import com.pokectfree.workspace.domain.Workspace;
import com.pokectfree.workspace.domain.WorkspaceMember;
import com.pokectfree.workspace.domain.WorkspaceRole;
import com.pokectfree.workspace.dto.WorkspaceCreateRequestDto;
import com.pokectfree.workspace.dto.WorkspaceInviteRequestDto;
import com.pokectfree.workspace.dto.WorkspaceMemberResponseDto;
import com.pokectfree.workspace.dto.WorkspaceResponseDto;
import com.pokectfree.workspace.repository.WorkspaceMemberRepository;
import com.pokectfree.workspace.repository.WorkspaceRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    /**
     * 유저에게 최소 1개의 개인 워크스페이스가 존재하도록 보장합니다.
     *
     * @param user 현재 로그인한 유저 엔티티
     * @return 기존 워크스페이스가 있으면 첫 번째 워크스페이스, 없으면 새로 만든 개인 워크스페이스
     */
    @Transactional
    public Workspace ensurePersonalWorkspace(User user) {
        return workspaceMemberRepository.findFirstByUser_IdOrderByIdAsc(user.getId())
                .map(WorkspaceMember::getWorkspace)
                .orElseGet(() -> createOwnedWorkspace(user, "나의 가계부"));
    }

    /**
     * 로그인 유저가 OWNER인 새 공유 워크스페이스를 생성합니다.
     *
     * @param userId 생성 요청을 보낸 로그인 유저의 DB PK
     * @param requestDto 프론트에서 전달한 워크스페이스 생성 요청 데이터(name)
     * @return 생성된 워크스페이스와 요청자의 역할 정보
     */
    @Transactional
    public WorkspaceResponseDto createWorkspace(Long userId, WorkspaceCreateRequestDto requestDto) {
        User user = getUser(userId);
        String name = normalizeWorkspaceName(requestDto != null ? requestDto.getName() : null);
        Workspace workspace = createOwnedWorkspace(user, name);
        WorkspaceMember owner = workspaceMemberRepository.findByWorkspace_IdOrderByUser_NicknameAsc(workspace.getId())
                .stream()
                .filter(member -> member.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("워크스페이스 소유자 정보를 찾을 수 없습니다."));

        return new WorkspaceResponseDto(owner);
    }

    /**
     * 로그인 유저가 속한 모든 워크스페이스를 조회합니다.
     *
     * @param userId 현재 로그인한 유저의 DB PK
     * @return 유저가 멤버로 참여 중인 워크스페이스 목록
     */
    @Transactional(readOnly = true)
    public List<WorkspaceResponseDto> getMyWorkspaces(Long userId) {
        return workspaceMemberRepository.findByUser_IdOrderByWorkspace_NameAsc(userId)
                .stream()
                .map(WorkspaceResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 워크스페이스의 멤버 목록을 조회합니다.
     *
     * @param userId 조회 요청을 보낸 로그인 유저의 DB PK
     * @param workspaceId 멤버 목록을 조회할 워크스페이스 ID
     * @return 워크스페이스에 참여 중인 유저 목록
     */
    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponseDto> getMembers(Long userId, Long workspaceId) {
        assertMember(workspaceId, userId);
        return workspaceMemberRepository.findByWorkspace_IdOrderByUser_NicknameAsc(workspaceId)
                .stream()
                .map(WorkspaceMemberResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 이메일로 기존 유저를 찾아 워크스페이스 멤버로 추가합니다.
     *
     * @param ownerUserId 멤버 추가 요청을 보낸 로그인 유저의 DB PK
     * @param workspaceId 멤버를 추가할 워크스페이스 ID
     * @param requestDto 초대할 유저 이메일과 부여할 역할(role)을 담은 요청 데이터
     * @return 새로 추가된 워크스페이스 멤버 정보
     */
    @Transactional
    public WorkspaceMemberResponseDto addMember(Long ownerUserId, Long workspaceId, WorkspaceInviteRequestDto requestDto) {
        assertMember(workspaceId, ownerUserId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없습니다."));
        String email = requestDto != null ? requestDto.getEmail() : null;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("초대할 유저를 찾을 수 없습니다."));

        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, user.getId())) {
            throw new IllegalArgumentException("이미 워크스페이스에 참여 중인 유저입니다.");
        }

        WorkspaceRole role = requestDto != null && "OWNER".equalsIgnoreCase(requestDto.getRole())
                ? WorkspaceRole.OWNER
                : WorkspaceRole.MEMBER;
        WorkspaceMember member = workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(role)
                .build());

        return new WorkspaceMemberResponseDto(member);
    }

    /**
     * 거래 조회/저장 시 사용할 워크스페이스를 확정하고 접근 권한을 검증합니다.
     *
     * @param userId 현재 로그인한 유저의 DB PK
     * @param workspaceId 프론트에서 선택한 워크스페이스 ID, null이면 개인 워크스페이스로 대체
     * @return 접근 가능한 워크스페이스 엔티티
     */
    @Transactional
    public Workspace resolveWorkspace(Long userId, Long workspaceId) {
        User user = getUser(userId);
        Workspace workspace = workspaceId == null
                ? ensurePersonalWorkspace(user)
                : workspaceRepository.findById(workspaceId)
                        .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없습니다."));

        assertMember(workspace.getId(), userId);
        return workspace;
    }

    /**
     * 결제자로 지정할 유저가 해당 워크스페이스 멤버인지 검증합니다.
     *
     * @param workspaceId 거래가 저장될 워크스페이스 ID
     * @param userId 결제자로 지정된 유저의 DB PK
     * @return 결제자로 사용할 유저 엔티티
     */
    @Transactional(readOnly = true)
    public User resolveMemberUser(Long workspaceId, Long userId) {
        User user = getUser(userId);
        assertMember(workspaceId, userId);
        return user;
    }

    /**
     * 워크스페이스와 OWNER 멤버십을 하나의 트랜잭션 안에서 함께 생성합니다.
     */
    private Workspace createOwnedWorkspace(User user, String name) {
        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .name(name)
                .build());

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build());

        return workspace;
    }

    /**
     * userId로 유저를 조회하고 없으면 명확한 예외를 발생시킵니다.
     */
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다. userId=" + userId));
    }

    /**
     * 워크스페이스 이름이 비어 있으면 기본 이름을 사용합니다.
     */
    private String normalizeWorkspaceName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "새 워크스페이스";
        }
        return name.trim();
    }

    /**
     * 유저가 워크스페이스 멤버가 아니면 조회/저장/초대 작업을 차단합니다.
     */
    private void assertMember(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, userId)) {
            throw new IllegalArgumentException("워크스페이스 접근 권한이 없습니다.");
        }
    }
}
