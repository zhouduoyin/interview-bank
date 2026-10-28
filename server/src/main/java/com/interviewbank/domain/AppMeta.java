package com.interviewbank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 应用级键值元数据，目前用于记录示例数据是否已经初始化。
 * 存在数据库里，而不是文件里，这样 H2 / MySQL、不同数据库之间不会互相影响。
 */
@Entity
@Table(name = "app_meta")
@Getter
@Setter
@NoArgsConstructor
public class AppMeta {

    @Id
    @Column(name = "meta_key", nullable = false, length = 64)
    private String key;

    @Column(name = "meta_value", length = 255)
    private String value;

    public AppMeta(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
