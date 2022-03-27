package com.programmers.java.baseball.engine;

import com.programmers.java.baseball.engine.io.Input;
import com.programmers.java.baseball.engine.io.NumberGenerator;
import com.programmers.java.baseball.engine.io.Output;
import com.programmers.java.baseball.engine.model.BallCount;
import com.programmers.java.baseball.engine.model.Numbers;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class BaseBall implements Runnable {
    private final int COUNT_OF_NUMBERS = 4;

    private NumberGenerator generator;
    private Input input;
    private Output output;

    @Override
    public void run() {        // 실행의 엔트리 포인트

        Numbers answer = generator.generate(COUNT_OF_NUMBERS);
        while (true) {
            String inputString = input.input("숫자를 맞춰보세요 : ");
            Optional<Numbers> inputNumbers = parse(inputString);
            if (inputNumbers.isEmpty()) {
                output.inputError();
                continue;
            }

            BallCount bc = ballCount(answer, inputNumbers.get());
            output.ballCount(bc);
            if (bc.getStrike() == COUNT_OF_NUMBERS) {
                output.correct();
                break;
            }
        }
    }

    private BallCount ballCount(Numbers answer, Numbers inputNumbers) {
        // 멀티스레드 환경에서 스레듣세이프하지않음..
        // 별도에 스레드에서 여기있는 하나의 값에 쓰려고 하면 레이스? 컨디션이 일어나게 된다.
        // 따라서 변수를 wrtie하는 경우는 제한적으로 사용하는것이 좋다. -> write해야 하는 변수에 동기화기능 추가해야 함
        AtomicInteger strike = new AtomicInteger();
        AtomicInteger ball = new AtomicInteger(); //만들어진 얘는 래퍼클래스임 따라서 가져올때 .get()을 해줌


        answer.indexedForEach((a, i) -> {
            inputNumbers.indexedForEach((n, j) -> {
                if (!a.equals(n)) return;
                if (i.equals(j))
                    strike.addAndGet(1);
                else
                    ball.addAndGet(1);
            });
        });
        return new BallCount(strike.get(), ball.get());
    }

    private Optional<Numbers> parse(String inputString) { // 이건 왜 게임엔진 내에서?
        //Optional이 잘못입력되거나, 잘못 파싱된 경우에는 아예 올바르게 입력되지 않도록
        if (inputString.length() != COUNT_OF_NUMBERS) return Optional.empty();
        long count = inputString.chars() // stream임 한글자한글자 떨어짐
                .filter(Character::isDigit)
                .map(Character::getNumericValue)
                .filter(i -> i > 0)
                .distinct()
                .count();
        if(count != COUNT_OF_NUMBERS) return Optional.empty();

        return Optional.of(new Numbers(
                inputString.chars()
                        .map(Character::getNumericValue)
                        .boxed()
                        .toArray(Integer[]::new)
        ));
    }

}
