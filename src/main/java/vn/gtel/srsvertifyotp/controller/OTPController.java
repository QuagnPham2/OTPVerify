package vn.gtel.srsvertifyotp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.gtel.srsvertifyotp.dto.Request.OTPRequest;
import vn.gtel.srsvertifyotp.dto.Request.PasswordRequest;
import vn.gtel.srsvertifyotp.service.OTPService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/otp")
public class OTPController {
    private final OTPService otpService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody OTPRequest request) {
        otpService.sendOTP(request.getPhone());
        return ResponseEntity.ok("OTP đã gửi");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOTP(@RequestBody OTPRequest request) {
        if (otpService.verifyOTP(request.getPhone(), request.getOtp())) {
            return ResponseEntity.ok("Xác thực thành công");
        }
        return ResponseEntity.badRequest().body("OTP không chính xác");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOTP(@RequestBody OTPRequest request) {
        otpService.sendOTP(request.getPhone());
        return ResponseEntity.ok("OTP đã gửi lại");
    }

//    @PostMapping("/set-password")
//    public ResponseEntity<String> setPassword(@RequestBody PasswordRequest request) {
//        if (!isValidPassword(request.getPassword())) {
//            return ResponseEntity.badRequest().body("Mật khẩu không hợp lệ");
//        }
//        String encodedPassword = passwordEncoder.encode(request.getPassword());
//        return ResponseEntity.ok("Mật khẩu đã được cập nhật");
//    }

//    private boolean isValidPassword(String password) {
//        return password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$\n");
//    }
    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(@RequestBody PasswordRequest request) {
        String password = otpService.setPassword(request);
        return ResponseEntity.ok("Cập nhật mật khẩu thành công, password: " + password);
    }
}
