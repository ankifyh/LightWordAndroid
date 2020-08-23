package yk.shiroyk.lightword.db.entity;

import androidx.annotation.Nullable;

import java.util.NoSuchElementException;

public class Optional<X> {

    private final X optional;

    public Optional(@Nullable X optional) {
        this.optional = optional;
    }

    public static <X> Optional<X> of(@Nullable X optional) {
        return new Optional<>(optional);
    }

    public boolean isEmpty() {
        return this.optional == null;
    }

    public X get() {
        if (optional == null) {
            throw new NoSuchElementException("No value present");
        }
        return optional;
    }
}