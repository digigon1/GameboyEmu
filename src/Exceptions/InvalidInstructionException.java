package Exceptions;

public class InvalidInstructionException extends Exception {
    public InvalidInstructionException(String format) {
        super(format);
    }
}
