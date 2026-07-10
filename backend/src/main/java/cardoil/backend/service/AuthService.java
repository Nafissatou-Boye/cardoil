package cardoil.backend.service;


import cardoil.backend.dto.request.LoginRequest;
import cardoil.backend.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}