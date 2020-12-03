/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.db.entity;

import java.util.Objects;

public class Triple<F, S, T> {
    public final F first;
    public final S second;
    public final T third;

    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static <A, B, C> Triple<A, B, C> create(A a, B b, C c) {
        return new Triple<>(a, b, c);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Triple)) {
            return false;
        }
        Triple<?, ?, ?> t = (Triple<?, ?, ?>) o;
        return Objects.equals(t.first, first) && Objects.equals(t.second, second) && Objects.equals(t.third, third);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode()) ^ (third == null ? 0 : third.hashCode());
    }

    @Override
    public String toString() {
        return "Triple{" + first + " " + second + " " + third + "}";
    }
}
