package com.pokectfree.login.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // Entity들이 이 클래스를 상속받을 때, 아래 필드들도 컬럼으로 인식하게 합니다.
@EntityListeners(AuditingEntityListener.class) // 시간에 대한 자동 감시(Auditing)를 시작합니다.
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; // 생성 시간 (한 번 만들어지면 수정 불가)

    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정 시간
}