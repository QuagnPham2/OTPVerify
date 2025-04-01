package vn.gtel.srsvertifyotp.dto.Request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PasswordRequest {
    private String phone;
    private String password;
}
