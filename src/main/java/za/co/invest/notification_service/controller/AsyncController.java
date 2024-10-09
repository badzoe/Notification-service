package za.co.invest.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import za.co.invest.notification_service.dto.MicroServiceRequest;
import za.co.invest.notification_service.entity.EmailList;
import za.co.invest.notification_service.repository.EmailListRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class AsyncController implements ChannelAwareMessageListener {

    private final EmailListRepository emailListRepository;
    private final ObjectMapper objectMapper;  // inject ObjectMapper
    private final JavaMailSender mailSender;  // inject JavaMailSender

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        log.info("Started Listening to the channel");

        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            // Deserialize the message
            MicroServiceRequest microServiceRequest = objectMapper.readValue(message.getBody(), MicroServiceRequest.class);
            log.info(microServiceRequest.toString());

            Optional<EmailList> email = emailListRepository.findById(microServiceRequest.id());

            // Check if the email exists and process accordingly
            email.ifPresentOrElse(e -> {
                try {
                    if (sendEmail(microServiceRequest)) {
                        updateEmailList(microServiceRequest.id(), "SUCCESS", channel, tag);
                    } else {
                        channel.basicNack(tag, false, true);  // Retry later
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    try {
                        channel.basicNack(tag, false, true);  // Retry later
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }, () -> {
                log.error("The message was not generated from our system: " + microServiceRequest.toString());
                try {
                    channel.basicNack(tag, false, false);  // Do not retry
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            channel.basicNack(tag, false, true);  // Retry later
        }
    }

    public void updateEmailList(Long id, String status, Channel channel, Long tag) throws IOException {
        if (emailListRepository.updateEmailListState(status, LocalDateTime.now(), id) > 0) {
            log.info("Successfully marked the transaction as done");
            channel.basicAck(tag, false);
        } else {
            log.info("Failed to mark the transaction as done");
            channel.basicNack(tag, false, false);
        }
    }

    private boolean sendEmail(MicroServiceRequest microServiceRequest) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(microServiceRequest.toEmail());
            message.setSubject(microServiceRequest.title());
            message.setText(microServiceRequest.bodyMessage());

            mailSender.send(message);
            log.info("Email sent successfully to: " + microServiceRequest.toEmail());
            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
