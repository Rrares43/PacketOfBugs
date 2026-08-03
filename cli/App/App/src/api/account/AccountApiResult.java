package api.account;

/** Result of a backend request, independent of the HTTP client implementation. */
public record AccountApiResult<T>(int statusCode, T data, String message) {
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public static <T> AccountApiResult<T> success(int statusCode, T data, String message) {
        return new AccountApiResult<>(statusCode, data, message);
    }

    public static <T> AccountApiResult<T> failure(int statusCode, String message) {
        return new AccountApiResult<>(statusCode, null, message);
    }
}
