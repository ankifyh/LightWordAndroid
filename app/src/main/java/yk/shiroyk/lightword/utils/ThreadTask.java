package yk.shiroyk.lightword.utils;

import androidx.core.util.Consumer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadTask {
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static <X> void runOnThread(X x, Consumer<X> consumer) {
        executor.execute(() -> consumer.accept(x));
    }
}
