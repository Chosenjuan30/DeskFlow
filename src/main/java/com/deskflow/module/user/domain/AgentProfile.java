package com.deskflow.module.user.domain;

import com.deskflow.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "agent_profiles")
@Getter
@Setter
@NoArgsConstructor
public class AgentProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "agent_profile_expertise",
            joinColumns = @JoinColumn(name = "agent_profile_id")
    )
    @Column(name = "expertise", length = 100)
    private Set<String> expertise = new HashSet<>();

    @Column(name = "current_load", nullable = false)
    private int currentLoad = 0;

    @Column(nullable = false)
    private boolean available = true;
}