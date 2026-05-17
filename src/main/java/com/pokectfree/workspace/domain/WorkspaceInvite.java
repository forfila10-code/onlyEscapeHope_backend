package com.pokectfree.workspace.domain;

import com.pokectfree.login.domain.BaseTimeEntity;
import com.pokectfree.login.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workspace_invites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceInvite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /** URL 경로에 쓰이는 고유 토큰 (UUID 기반) */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /** 토큰 만료 시각 (생성 시각 + 48시간) */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public WorkspaceInvite(Workspace workspace, User createdBy, String token, LocalDateTime expiresAt) {
        this.workspace = workspace;
        this.createdBy = createdBy;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
