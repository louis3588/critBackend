package com.lp.criticabackend.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity(name = "follows")
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id", referencedColumnName = "idusers")
    private User follower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "following_id", referencedColumnName = "idusers")
    private User following;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowStatus status;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public Follow(User follower, User following, FollowStatus status, LocalDate createdAt) {
        this.follower = follower;
        this.following = following;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Follow() {

    }


    public User getFollower() { return follower; }
    public User getFollowing() { return following; }
    public FollowStatus getStatus() { return status; }
    public LocalDate getCreatedAt() { return createdAt; }


    public void setFollower(User follower) { this.follower = follower; }
    public void setFollowing(User following) { this.following = following; }
    public void setStatus(FollowStatus status) { this.status = status; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
