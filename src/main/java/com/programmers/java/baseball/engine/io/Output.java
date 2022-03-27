package com.programmers.java.baseball.engine.io;

import com.programmers.java.baseball.engine.model.BallCount;

public interface Output {
    void ballCount(BallCount bc);
    void inputError();
    void correct();
}
