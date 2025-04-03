package com.mygo.utils;

public class Context {

    private static final ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void saveId(Long userId) {
        tl.set(userId);
    }

    public static Long getId() {
        return tl.get();
    }

    public static void removeId() {
        tl.remove();
    }
}
