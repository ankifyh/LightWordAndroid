package yk.shiroyk.lightword.utils;

import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ThreadTask {
    private static ExecutorService service = Executors.newSingleThreadExecutor();

    public static <X> void runOnThread(X x, Consumer<X> consumer) {
        service.execute(() -> consumer.accept(x));
    }

    public static <X, Y> Y runOnThreadCall(X x, Function<X, Y> y) {
        try {
            return service.submit(() -> y.apply(x)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
