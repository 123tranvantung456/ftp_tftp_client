package com.java.gui;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class NetAsciiInputStream extends FilterInputStream {
    public NetAsciiInputStream(InputStream in) {
        super(new PushbackInputStream(in, 1));
    }
    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c == '\r') {
            int next = super.read();
            if (next == '\n') {
                return '\n';
            } else if (next == -1) {
                return '\r';
            } else {
                ((PushbackInputStream) in).unread(next);
            }
        }
        return c;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = 0;
        for (int i = 0; i < len; i++) {
            int c = read();
            if (c == -1) {
                return count == 0 ? -1 : count;
            }
            b[off + i] = (byte) c;
            count++;
        }
        return count;
    }
}