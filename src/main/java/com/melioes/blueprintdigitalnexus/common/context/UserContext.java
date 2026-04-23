package com.melioes.blueprintdigitalnexus.common.context;

public class UserContext {

    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(Long userId) {
        THREAD_LOCAL.set(userId);
    }

    public static Long get() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}