package com.epam.drill.auto.test.agent;

public class JByteArray {
    public byte[] bytes;
    public int size;

    public JByteArray(byte[] initial) {
        bytes = initial;
        size = bytes.length;
        System.out.println("BYTES: " + size);
    }
}
