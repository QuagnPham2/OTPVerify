package vn.gtel.srsvertifyotp.service;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.gtel.srsvertifyotp.dto.Request.OTPRequest;
import vn.gtel.srsvertifyotp.dto.Request.PasswordRequest;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OTPService {
    private final String OTP_SAVE = "OTP_";
    private final String ATTEMPT_NUMBER = "ATTEMPT_";
    private final String TIME_RESEND = "RESEND_";
    private final String DAILY_LIMIT = "DAILY_";

    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private final PasswordEncoder passwordEncoder;

    private String normalizePhone(String phone) {
        return phone.startsWith("0") ? "84" + phone.substring(1) : phone.replace("+", "");
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$*%^&+=!]).{8,}$");
    }

    public String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendOTP(String phone) {
        if((phone.length() != 10 || !phone.startsWith("0")) && (phone.length() != 12 || !phone.startsWith("+84"))) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }

        phone = normalizePhone(phone);

        String otp = generateOTP();
        redisTemplate.opsForValue().set(OTP_SAVE + phone, otp, Duration.ofMinutes(3));
        redisTemplate.opsForValue().set(TIME_RESEND + phone, "1", Duration.ofMinutes(2));

        String dailyCountStr = redisTemplate.opsForValue().get(DAILY_LIMIT + phone);
        int dailyCount = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;
        if (dailyCount >= 5) {
            throw new RuntimeException("Quá giới hạn gửi OTP trong ngày.");
        }
        redisTemplate.opsForValue().increment(DAILY_LIMIT + phone);

        rabbitTemplate.convertAndSend("otpQueue", phone + ":" + otp);
    }

    public boolean verifyOTP(String phone, String otp) {
        phone = normalizePhone(phone);
        String storedOtp = redisTemplate.opsForValue().get(OTP_SAVE + phone);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            String attemptStr = redisTemplate.opsForValue().get(ATTEMPT_NUMBER + phone);
            int attempt = attemptStr != null ? Integer.parseInt(attemptStr) : 0;
            if (attempt >= 4) {
                redisTemplate.delete(OTP_SAVE + phone);
                redisTemplate.delete(ATTEMPT_NUMBER + phone);
                throw new RuntimeException("Nhập sai OTP quá số lần cho phép.");
            }
            redisTemplate.opsForValue().increment(ATTEMPT_NUMBER + phone);
            return false;
        }
        redisTemplate.delete(OTP_SAVE + phone);
        redisTemplate.delete(ATTEMPT_NUMBER + phone);
        return true;
    }

    public String setPassword(PasswordRequest request){
        if(!isValidPassword(request.getPassword())){
            throw new IllegalArgumentException("Mật khẩu không hợp lệ");
        }
        String password = passwordEncoder.encode(request.getPassword());
        return password;
    }

}
