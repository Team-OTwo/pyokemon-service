package com.pyokemon.user.api.service;

import java.util.List;
import java.util.Optional;

import com.pyokemon.user.api.exception.UserException;
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
    public User updateUser(Long id, User userDetails) {
        if (!userRepository.existsById(id)) {
            throw UserException.notFound();
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

    private void validateDuplicateUser(UserRegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserException.duplicateEmail(request.getEmail());
        }
    }

    private void validateNewUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw UserException.duplicateEmail(user.getEmail());
        }
    }

    private void validatePassword(String password) {
        if (!PasswordValidator.isValid(password)) {
            throw UserException.loginFailed();
        }
    }
}

