package com.pokectfree.workspace.domain;

import com.pokectfree.login.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workspaces")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workspace extends BaseTimeEntity {

    /** 워크스페이스 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 화면에 표시할 워크스페이스 이름 (예: 나의 가계부, 이탈리아 여행 모임) */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * @param name 생성할 워크스페이스 이름
     */
    @Builder
    public Workspace(String name) {
        this.name = name;
    }
}
