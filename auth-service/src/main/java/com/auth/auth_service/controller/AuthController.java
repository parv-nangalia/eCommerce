package com.auth.auth_service.controller;


import com.auth.auth_service.dto.LoginResponseDto;
import com.auth.auth_service.dto.SignInRequestDto;
import com.auth.auth_service.entity.User;
import com.auth.auth_service.repo.UserRepository;
import com.auth.auth_service.service.UserDetailsServiceImpl;
import com.auth.auth_service.dto.LoginRequestDto;
import com.auth.auth_service.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsServiceImpl userDetailsService,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequestDto loginRequestDto) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(new LoginResponseDto(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignInRequestDto signinRequestDto) {

        Optional<User> user = userRepository.findByUserName(signinRequestDto.getUsername());
        if(user.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        User userNew = new User();
        userNew.setUserName(signinRequestDto.getUsername());
        userNew.setPassword(passwordEncoder.encode(signinRequestDto.getPassword()));
        userNew.setEmail(signinRequestDto.getEmail());
        User savedUser = userRepository.save(userNew);

        return ResponseEntity.ok("User registered successfully: " + savedUser.getUserName());
    }

    @GetMapping("/hello")

    public String hello() {
        return "Hello, authenticated user!";
    }
}
