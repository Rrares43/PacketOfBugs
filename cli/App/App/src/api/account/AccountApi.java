package api.account;

import account.dto.AccountApiDtos;

/** Boundary used by account operations to communicate with the backend. */
public interface AccountApi {
    AccountApiResult<Void> register(String username, String email, String password);

    AccountApiResult<AccountApiDtos.AccountResponse> login(String username, String password);

    AccountApiResult<Void> changePassword(String username, String email, String newPassword);

    AccountApiResult<AccountApiDtos.AccountResponse> findByUsername(String username);

    AccountApiResult<Void> delete(String username);
}
