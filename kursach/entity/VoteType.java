// src/main/java/com/example/kursach/entity/VoteType.java
package com.example.kursach.entity;

public enum VoteType {
    UPVOTE(1),    // Лайк
    DOWNVOTE(-1); // Дизлайк

    private final int value;

    VoteType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}