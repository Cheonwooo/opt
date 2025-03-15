package com.opt.ssafy.optback.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Table(name = "interest")
@Entity
@Getter
public class Interest {

    @Id
    private Integer id;

    @Column(name = "display_name")
    private String displayName;
}
