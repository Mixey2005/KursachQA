package com.example.kursach.entity;

public enum VoteValue {
    UPVOTE(1),
    DOWNVOTE(-1);

    private final int score; // Store the numeric value

    VoteValue(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    // Optional: helper to get enum from score
    public static VoteValue fromScore(int score) {
        if (score == 1) return UPVOTE;
        if (score == -1) return DOWNVOTE;
        throw new IllegalArgumentException("Invalid score for VoteValue: " + score);
    }
}