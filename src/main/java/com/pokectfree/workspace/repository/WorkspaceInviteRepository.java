package com.pokectfree.workspace.repository;

import com.pokectfree.workspace.domain.WorkspaceInvite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Long> {

    Optional<WorkspaceInvite> findByToken(String token);

    void deleteByWorkspace_Id(Long workspaceId);
}
