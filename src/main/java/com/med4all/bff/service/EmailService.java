package com.med4all.bff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp + "\nIt expires in 10 minutes.");
        mailSender.send(message);
    }

    public void sendApprovalNotification(String to, String fullName, String role) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Account Approved");
        message.setText("Dear " + fullName + ",\n\n" +
                "Congratulations! Your account as a " + role + " has been approved. " +
                "You can now log in to the system.\n\n" +
                "Best regards,\nMed4All Team");
        mailSender.send(message);
    }

    public void sendRejectionNotification(String to, String fullName, String rejectionReason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Account Rejected");
        message.setText("Dear " + fullName + ",\n\n" +
                "We regret to inform you that your account has been rejected due to the following reason:\n" +
                rejectionReason + "\n\n" +
                "If you have any questions, please contact our support team.\n\n" +
                "Best regards,\nMed4All Team");
        mailSender.send(message);
    }
}

