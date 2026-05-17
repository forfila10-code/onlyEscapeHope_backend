package com.pokectfree.workspace.service;

import com.pokectfree.login.domain.User;
import com.pokectfree.login.repository.UserRepository;
import com.pokectfree.transaction.repository.TransactionRepository;
import com.pokectfree.workspace.domain.Workspace;
import com.pokectfree.workspace.domain.WorkspaceInvite;
import com.pokectfree.workspace.domain.WorkspaceMember;
import com.pokectfree.workspace.domain.WorkspaceRole;
import com.pokectfree.workspace.dto.WorkspaceCreateRequestDto;
import com.pokectfree.workspace.dto.WorkspaceInviteRequestDto;
import com.pokectfree.workspace.dto.WorkspaceInviteTokenResponseDto;
import com.pokectfree.workspace.dto.WorkspaceJoinInfoDto;
import com.pokectfree.workspace.dto.WorkspaceMemberResponseDto;
import com.pokectfree.workspace.dto.WorkspaceResponseDto;
import com.pokectfree.workspace.dto.WorkspaceUpdateRequestDto;
import com.pokectfree.workspace.repository.WorkspaceInviteRepository;
import com.pokectfree.workspace.repository.WorkspaceMemberRepository;
import com.pokectfree.workspace.repository.WorkspaceRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
    private final TransactionRepository transactionRepository;
    private final WorkspaceInviteRepository workspaceInviteRepository;

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
     * OWNER 권한을 가진 유저가 워크스페이스 이름을 수정합니다.
     *
     * @param userId      요청을 보낸 로그인 유저의 DB PK
     * @param workspaceId 수정할 워크스페이스 ID
     * @param requestDto  변경할 이름을 담은 요청 데이터
     * @return 수정된 워크스페이스 정보
     */
    @Transactional
    public WorkspaceResponseDto updateWorkspaceName(Long userId, Long workspaceId, WorkspaceUpdateRequestDto requestDto) {
        assertOwner(workspaceId, userId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없습니다."));

        String newName = normalizeWorkspaceName(requestDto != null ? requestDto.getName() : null);
        workspace.updateName(newName);

        WorkspaceMember ownerMember = workspaceMemberRepository
                .findByWorkspace_IdAndUser_Id(workspaceId, userId)
                .orElseThrow(() -> new IllegalStateException("소유자 멤버 정보를 찾을 수 없습니다."));
        return new WorkspaceResponseDto(ownerMember);
    }

    /**
     * OWNER만 워크스페이스를 삭제할 수 있습니다.
     * 연관된 거래 내역과 멤버 정보를 모두 제거한 뒤 워크스페이스를 삭제합니다.
     *
     * @param userId      요청을 보낸 로그인 유저의 DB PK
     * @param workspaceId 삭제할 워크스페이스 ID
     */
    @Transactional
    public void deleteWorkspace(Long userId, Long workspaceId) {
        assertOwner(workspaceId, userId);

        workspaceInviteRepository.deleteByWorkspace_Id(workspaceId);
        transactionRepository.deleteByWorkspace_Id(workspaceId);
        workspaceMemberRepository.deleteByWorkspace_Id(workspaceId);
        workspaceRepository.deleteById(workspaceId);
    }

    /**
     * 워크스페이스에 대한 48시간 유효 초대 토큰을 생성합니다.
     * 호출할 때마다 기존 토큰을 삭제하고 새 토큰을 발급합니다.
     *
     * @param userId      초대 링크를 생성하는 로그인 유저의 DB PK
     * @param workspaceId 초대 대상 워크스페이스 ID
     * @return 생성된 토큰 값과 만료 시각
     */
    @Transactional
    public WorkspaceInviteTokenResponseDto generateInviteToken(Long userId, Long workspaceId) {
        assertOwner(workspaceId, userId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없습니다."));
        User user = getUser(userId);

        workspaceInviteRepository.deleteByWorkspace_Id(workspaceId);

        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(48);

        workspaceInviteRepository.save(WorkspaceInvite.builder()
                .workspace(workspace)
                .createdBy(user)
                .token(token)
                .expiresAt(expiresAt)
                .build());

        return new WorkspaceInviteTokenResponseDto(token, expiresAt);
    }

    /**
     * 초대 토큰으로 워크스페이스 기본 정보를 조회합니다. (가입 페이지 미리보기용)
     *
     * @param userId 요청을 보낸 로그인 유저의 DB PK
     * @param token  초대 URL에 포함된 토큰
     * @return 워크스페이스 이름, 멤버 수, 이미 가입 여부
     */
    @Transactional(readOnly = true)
    public WorkspaceJoinInfoDto getJoinInfo(Long userId, String token) {
        WorkspaceInvite invite = findValidInvite(token);
        Workspace workspace = invite.getWorkspace();
        int memberCount = workspaceMemberRepository
                .findByWorkspace_IdOrderByUser_NicknameAsc(workspace.getId()).size();
        boolean alreadyJoined = workspaceMemberRepository
                .existsByWorkspace_IdAndUser_Id(workspace.getId(), userId);
        return new WorkspaceJoinInfoDto(workspace.getName(), memberCount, alreadyJoined);
    }

    /**
     * 초대 토큰으로 워크스페이스에 MEMBER로 가입합니다.
     *
     * @param userId 가입을 요청하는 로그인 유저의 DB PK
     * @param token  초대 URL에 포함된 토큰
     * @return 가입된 워크스페이스 정보
     */
    @Transactional
    public WorkspaceResponseDto joinByToken(Long userId, String token) {
        WorkspaceInvite invite = findValidInvite(token);
        Workspace workspace = invite.getWorkspace();
        User user = getUser(userId);

        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspace.getId(), userId)) {
            WorkspaceMember existing = workspaceMemberRepository
                    .findByWorkspace_IdAndUser_Id(workspace.getId(), userId)
                    .orElseThrow(() -> new IllegalStateException("멤버 정보를 찾을 수 없습니다."));
            return new WorkspaceResponseDto(existing);
        }

        WorkspaceMember newMember = workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.MEMBER)
                .build());

        return new WorkspaceResponseDto(newMember);
    }

    /**
     * 거래 조회/저장 시 사용할 워크스페이스를 확정하고 접근 권한을 검증합니다.     *
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

    /**
     * 토큰으로 유효한 초대를 조회합니다. 존재하지 않거나 만료된 경우 예외를 발생시킵니다.
     */
    private WorkspaceInvite findValidInvite(String token) {
        WorkspaceInvite invite = workspaceInviteRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 링크입니다."));
        if (invite.isExpired()) {
            throw new IllegalArgumentException("만료된 초대 링크입니다. 워크스페이스 관리자에게 새 링크를 요청하세요.");
        }
        return invite;
    }

    /**
     * 유저가 워크스페이스 OWNER가 아니면 수정/삭제 작업을 차단합니다.
     */
    private void assertOwner(Long workspaceId, Long userId) {
        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspace_IdAndUser_Id(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스 접근 권한이 없습니다."));
        if (member.getRole() != WorkspaceRole.OWNER) {
            throw new IllegalArgumentException("워크스페이스 소유자만 이 작업을 수행할 수 있습니다.");
        }
    }
}
