package post.validator;

public class IsValidLength implements Validator<String> {
    private final int maxLength;

    public IsValidLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean isValid(String text) {
        return text != null && text.length() <= maxLength;
    }
}
