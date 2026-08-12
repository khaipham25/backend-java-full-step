package vn.tayjava.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.tayjava.service.EmailService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-CONTROLLER")
public class EmailController {
    private final EmailService emailService;

    @GetMapping("/send-email")
    public void send(@RequestParam String to, @RequestParam String subject, @RequestParam String content) {
        log.info("Sending email to: " + to);
        emailService.send(to, subject, content);
        log.info("Email sent");
    }

    @GetMapping("/verify-email")
    public void emailVerification(@RequestParam String to, @RequestParam String name) throws IOException {
        log.info("Verifying email to: " + to);
        emailService.emailVerification(to, name);
    }
}
