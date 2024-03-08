package com.shdwfghtr.asset;

public class ConversionService {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String toString(long l) {
        StringBuilder result = new StringBuilder();
        long tmp = l;
        while (tmp != 0) {
            long module = tmp % ALPHABET.length();
            result.insert(0, ALPHABET.charAt((int) Math.abs(module)));
            tmp /= ALPHABET.length();
        }
        return result.toString();
    }

    public static long toLong(String s) {
        long result = 0;
        int power = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            int mantissa = ALPHABET.indexOf(s.charAt(i));
            result += mantissa * Math.pow(ALPHABET.length(), power++);
        }
        return result;
    }
}
