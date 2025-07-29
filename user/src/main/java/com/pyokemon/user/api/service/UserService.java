package com.pyokemon.user.api.service;

import java.util.List;
import java.util.Optional;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.user.api.dto.UserLoginRequestDto;
import com.pyokemon.user.api.dto.UserUpdateRequestDto;
import com.pyokemon.user.api.exception.UserException;
import com.pyokemon.user.api.secret.jwt.TokenGenerator;
import com.pyokemon.user.api.secret.jwt.dto.TokenDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.user.api.dto.UserRegisterRequestDto;
import com.pyokemon.user.api.entity.User;
import com.pyokemon.user.api.repository.UserRepository;
import com.pyokemon.user.api.validation.PasswordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateUser(UserUpdateRequestDto request) {
        if(request.getPassword() == null || request.getPassword().isEmpty()){
            throw new UserException("비밀번호는 필수입니다.", "USER_PASSWORD_REQUIRED");
        }
        if(request.getNewPassword() == null || request.getNewPassword().isEmpty()){
            throw new UserException("새 비밀번호는 필수입니다.", "USER_NEW_PASSWORD_REQUIRED");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(UserException::notFound);

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new UserException("기존 비밀번호가 다릅니다.", "PASSWORD_NOT_MATCHED");
        }

        validatePassword(request.getPassword());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.update(user);

        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw UserException.notFound();
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public User registerUser(UserRegisterRequestDto request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserException.duplicateEmail(request.getEmail());
        }
        
        // 2. 비밀번호 정책 검증
        validatePassword(request.getPassword());
        
        // 3. 사용자 엔티티 생성
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .birth(request.getBirth())
                .build();

        // 저장
        userRepository.insert(user);
        return user;
    }

    public TokenDto.AccessRefreshToken loginUser(UserLoginRequestDto request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()){
            throw new UserException("이메일은 필수입니다", "EMAIL_REQUIRED");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()){
            throw new UserException("비밀번호는 필수입니다.", "PASSWORD_REQUIRED");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(UserException::loginFailed);

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw UserException.loginFailed();
        }
        
        // Todo: device type 구분
        return tokenGenerator.generateAccessRefreshToken(user.getEmail(), "web");
    }

    /**
     * 리프레시 토큰을 검증하고 새 액세스 토큰을 발급합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새 액세스 토큰
     */
    public TokenDto.AccessToken refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 검증
        String userId = tokenGenerator.validateJwtToken(refreshToken);

        if (userId == null) {
            throw new UserException("유효하지 않은 토큰입니다.", "USER_TOKEN_INVALID");
        }

        // 사용자 존재 여부 확인
        Optional<User> userOpt = userRepository.findByEmail(userId);
        if (userOpt.isEmpty()) {
            throw UserException.notFound();
        }

        // 새 액세스 토큰 발급
        return tokenGenerator.generateAccessToken(userId, "web");
    }
    private void validateDuplicateUser(UserRegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserException.duplicateEmail(request.getEmail());
        }
    }

    private void validatePassword(String password) {
        if (!PasswordValidator.isValid(password)) {
            throw new UserException("유효하지 않은 비밀번호입니다.", "USER_PASSWORD_NOT_VALID");
        }
    }
}

