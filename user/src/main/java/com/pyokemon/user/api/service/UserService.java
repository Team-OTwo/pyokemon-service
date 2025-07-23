package com.pyokemon.user.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.user.api.dto.UserRegisterRequest;
import com.pyokemon.user.api.entity.User;
import com.pyokemon.user.api.exception.InvalidPasswordException;
import com.pyokemon.user.api.repository.UserRepository;
import com.pyokemon.user.api.util.PasswordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String DUPLICATE_EMAIL = "DUPLICATE_EMAIL";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByName(String username) {
        return userRepository.findByName(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createUser(User user) {
        validateNewUser(user);
        validatePassword(user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.insert(user);
        return user;
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("사용자를 찾을 수 없습니다: " + id, USER_NOT_FOUND);
        }
        if (userDetails.getPassword() != null) {
            validatePassword(userDetails.getPassword());
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        userRepository.update(userDetails);
        return userDetails;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("사용자를 찾을 수 없습니다: " + id, USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public User registerUser(UserRegisterRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("이미 존재하는 이메일입니다: " + request.getEmail(), "DUPLICATE_EMAIL");
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

    private void validateDuplicateUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("이미 존재하는 이메일입니다: " + request.getEmail(), DUPLICATE_EMAIL);
        }
    }

    private void validateNewUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("이미 존재하는 이메일입니다: " + user.getEmail(), DUPLICATE_EMAIL);
        }
    }

    private void validatePassword(String password) {
        if (!PasswordValidator.isValid(password)) {
            throw new InvalidPasswordException(PasswordValidator.getPasswordPolicy());
        }
    }
}

