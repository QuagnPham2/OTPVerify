package vn.gtel.srsvertifyotp.dto.Request;

import lombok.Data;

@Data
public class OTPRequest {
    private String phone;
    private String otp;
}
