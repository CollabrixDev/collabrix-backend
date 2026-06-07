package com.syncspace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Workspace entity representing a collaborative workspace.
 * 
 * Each workspace has:
 * - Owner (the user who created it)
 * - Members (users who have access)
 * - Metadata (name, description, creation/update timestamps)
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Entity
@Table(name = "workspaces", indexes = {
    @Index(name = "idx_workspace_owner", columnList = "owner_id"),
    @Index(name = "idx_workspace_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    /**
     * Members of the workspace (many-to-many relationship).
     * Includes the owner.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "workspace_members",
        joinColumns = @JoinColumn(name = "workspace_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Add a member to the workspace.
     */
    public void addMember(User user) {
        this.members.add(user);
    }
    
    /**
     * Remove a member from the workspace.
     */
    public void removeMember(User user) {
        this.members.remove(user);
    }
    
    /**
     * Check if user is a member of the workspace.
     */
    public boolean isMember(User user) {
        return this.members.contains(user);
    }
    
    /**
     * Check if user is the owner.
     */
    public boolean isOwner(User user) {
        return this.owner.getId().equals(user.getId());
    }
}
