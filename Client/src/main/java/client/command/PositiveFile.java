package client.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.io.File;

public class PositiveFile implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        File file = new File(value);
        if (!file.exists()) {
            throw new ParameterException("file: " + value + "is not exists");
        }
    }
}
