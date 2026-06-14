package com.school.exhibition.modules.user;

import com.school.exhibition.common.result.R;
import com.school.exhibition.modules.user.dto.LoginRequest;
import com.school.exhibition.modules.user.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @GetMapping("/me")
    public R<LoginResponse> me() {
        return R.ok(authService.currentUser());
    }
}
