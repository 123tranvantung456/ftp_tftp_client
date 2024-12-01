package com.java.client.tftp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NetAsciiOutputStream extends FilterOutputStream {
    public NetAsciiOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            out.write('\r');
            out.write('\n');
        } else if (b != '\r') {
            out.write(b);
        }
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }
}
