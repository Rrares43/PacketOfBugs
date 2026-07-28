package post.validator;

public interface Validator<T> {
    boolean isValid(T input);
}
