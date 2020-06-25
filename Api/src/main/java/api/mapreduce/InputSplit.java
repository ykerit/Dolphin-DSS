package api.mapreduce;

import java.io.IOException;

public interface InputSplit {
    long getLength() throws IOException;

    String[] getLocations() throws IOException;
}
