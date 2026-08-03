package api.account;

import account.dto.AccountApiDtos;
import api.RedditApiClient;
import com.google.gson.Gson;

import java.net.http.HttpResponse;
import java.util.function.Supplier;

/** HTTP implementation of the account API boundary. */
public final class HttpAccountApi implements AccountApi {
    private final Gson gson;

    public HttpAccountApi() {
        this.gson = RedditApiClient.gson();
    }

    @Override
    public AccountApiResult<Void> register(String username, String email, String password) {
        return execute(() -> RedditApiClient.registerAccountRaw(username, email, password), null);
    }

    @Override
    public AccountApiResult<AccountApiDtos.AccountResponse> login(String username, String password) {
        return execute(() -> RedditApiClient.login(username, password), AccountApiDtos.AccountResponse.class);
    }

    @Override
    public AccountApiResult<Void> changePassword(String username, String email, String newPassword) {
        return execute(() -> RedditApiClient.changePasswordRaw(username, email, newPassword), null);
    }

    @Override
    public AccountApiResult<AccountApiDtos.AccountResponse> findByUsername(String username) {
        return execute(() -> RedditApiClient.getAccountRaw(username), AccountApiDtos.AccountResponse.class);
    }

    @Override
    public AccountApiResult<Void> delete(String username) {
        return execute(() -> RedditApiClient.deleteAccountRaw(username), null);
    }

    private <T> AccountApiResult<T> execute(Supplier<HttpResponse<String>> request, Class<T> responseType) {
        try {
            HttpResponse<String> response = request.get();
            String body = response.body() == null ? "" : response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return AccountApiResult.failure(response.statusCode(), body);
            }
            T data = responseType == null ? null : gson.fromJson(body, responseType);
            return AccountApiResult.success(response.statusCode(), data, body);
        } catch (Exception exception) {
            return AccountApiResult.failure(0, "Connection error: " + exception.getMessage());
        }
    }
}
