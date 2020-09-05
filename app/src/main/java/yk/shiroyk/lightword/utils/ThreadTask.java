package yk.shiroyk.lightword.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;
import androidx.core.util.Supplier;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadTask {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void runOnThread(Runnable runnable) {
        executor.execute(runnable);
    }

    public static <X> void runOnThread(Supplier<X> supplier, Consumer<X> consumer) {
        executor.execute(() -> {
            final X result;
            try {
                result = supplier.get();
                handler.post(() -> {
                    consumer.accept(result);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
