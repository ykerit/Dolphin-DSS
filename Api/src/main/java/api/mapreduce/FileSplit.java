package api.mapreduce;

import java.io.IOException;

public class FileSplit implements InputSplit {
    @Override
    public long getLength() throws IOException {
        return 0;
    }

    @Override
    public String[] getLocations() throws IOException {
        return new String[0];
    }
}
