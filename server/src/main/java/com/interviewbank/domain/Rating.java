package com.interviewbank.domain;

/** 复习反馈：忘记了 / 有点难 / 记得 / 很简单 */
public enum Rating {
    FORGOT(0), HARD(1), GOOD(2), EASY(3);

    private final int score;

    Rating(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}
