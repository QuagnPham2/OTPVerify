package vn.gtel.srsvertifyotp.consumer;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OTPConsumer {
    @RabbitListener(queues = "otpQueue")
    public void receiveMessage(String message) {
        String[] parts = message.split(":");
        String phone = parts[0];
        String otp = parts[1];

        System.out.println("Gửi OTP " + otp + " đến số " + phone);
    }
}
