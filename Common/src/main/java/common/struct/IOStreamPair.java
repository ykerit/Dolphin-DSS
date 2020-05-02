package common.struct;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOStreamPair implements Closeable {
    private final InputStream in;
    private final OutputStream out;

    public IOStreamPair(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }
    @Override
    public void close() throws IOException {
        if (in != null && out != null) {
            in.close();
            out.close();
        }
    }
}
