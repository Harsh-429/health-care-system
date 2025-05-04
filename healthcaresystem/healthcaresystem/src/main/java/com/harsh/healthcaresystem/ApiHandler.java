package com.harsh.healthcaresystem;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ApiHandler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/ping")
    public String ping() {
        return "Hello, API";
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody @Valid User user) {
        Map<String, Object> response = new HashMap<>();

        int randomPin = (int) (Math.random() * 900000) + 100000;
        String otp = String.valueOf(randomPin);

        try {
            // INSERT DATA into database
            String sql = "INSERT INTO unverified_users (username, email, mobile, password, otp, gender) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, user.getUsername(), user.getEmail(), user.getMobile(), user.getPassword(), otp,
                    user.getGender());

            emailService.sendEmail(user.getEmail(), otp);
            // Prepare success response
            response.put("status", "success");
            response.put("message", "OTP sent successfully!");
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            e.printStackTrace();

            // Prepare error response
            response.put("status", "error");
            response.put("message", "Failed to send OTP. Try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage();
            String duplicateField = "entry"; // Default generic message

            // Extract the exact field name from the error message
            Pattern pattern = Pattern.compile("for key 'unverified_users\\.(\\w+)'");
            Matcher matcher = pattern.matcher(errorMessage);
            String message = "";
            if (matcher.find()) {
                duplicateField = matcher.group(1); // Extracts "email" or "mobile"
            }

            // Map database column name to user-friendly message
            switch (duplicateField) {
                case "email":
                    message = "DUPLICATE EMAIL";
                    break;
                case "mobile":
                    message = "DUPLICATE MOBILE";
                    break;
                case "username":
                    message = "DUPLICATE USERNAME";
                    break;
                default:
                    response.put("message", "Duplicate entry found.");
                    break;
            }

            response.put("status", "error");
            response.put("message", message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/verifyUser")
    public ResponseEntity<Map<String, Object>> verifyUser(@RequestBody @Valid OTPEntity otpEntity) {
        Map<String, Object> resData = new HashMap<>();

        try {
            // Retrieve OTP from database
            String sql = "SELECT otp FROM unverified_users WHERE email = ?";
            String otp = jdbcTemplate.queryForObject(sql, String.class, otpEntity.getEmail());

            if (otp.equals(otpEntity.getOtp())) {
                // ✅ OTP is correct → Move user to 'users' table
                String moveUserSQL = "INSERT INTO users (username, email, mobile, password, gender, created_at) " +
                        "SELECT username, email, mobile, password, gender, created_at " +
                        "FROM unverified_users WHERE email = ?";
                jdbcTemplate.update(moveUserSQL, otpEntity.getEmail());

                // 🔥 Delete user from 'unverified_users'
                String deleteSQL = "DELETE FROM unverified_users WHERE email = ?";
                jdbcTemplate.update(deleteSQL, otpEntity.getEmail());

                String token = jwtUtil.generateToken(otpEntity.getEmail());
                resData.put("success", true);
                resData.put("token", token);
                resData.put("message", "OTP verified! User registered successfully.");
            } else {
                resData.put("success", false);
                resData.put("message", "Invalid OTP. Please try again.");
            }
        } catch (EmptyResultDataAccessException e) {
            resData.put("success", false);
            resData.put("message", "No user found with this email.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(resData);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Map<String, Object> resData = new HashMap<>();
        try {
            // Fetch user from database
            String sql = "SELECT password FROM users WHERE email = ?";
            String storedPassword = jdbcTemplate.queryForObject(sql, String.class, request.getEmail());

            // Compare passwords using BCrypt
            if (storedPassword.equals(request.getPassword())) {
                // Generate JWT if passwords match
                String token = jwtUtil.generateToken(request.getEmail());
                resData.put("success", true);
                resData.put("token", token);
                return ResponseEntity.ok(resData);
            } else {
                resData.put("success", false);
                resData.put("message", "INVALID_CREDENTIALS");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(resData);
            }
        } catch (EmptyResultDataAccessException e) {
            resData.put("success", false);
            resData.put("message", "NO_USER");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(resData);
        }
    }

}
