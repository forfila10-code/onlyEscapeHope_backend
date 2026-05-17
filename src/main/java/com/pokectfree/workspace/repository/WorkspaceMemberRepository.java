package com.pokectfree.workspace.repository;

import com.pokectfree.workspace.domain.WorkspaceMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    boolean existsByWorkspace_IdAndUser_Id(Long workspaceId, Long userId);

    List<WorkspaceMember> findByUser_IdOrderByWorkspace_NameAsc(Long userId);

    List<WorkspaceMember> findByWorkspace_IdOrderByUser_NicknameAsc(Long workspaceId);

    Optional<WorkspaceMember> findFirstByUser_IdOrderByIdAsc(Long userId);

    void deleteByWorkspace_Id(Long workspaceId);

    Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(Long workspaceId, Long userId);
}
