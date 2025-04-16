package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.domain.Order;
import com.psu.rouen.cphbox.domain.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.psu.rouen.cphbox.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

/**
 * Service for sending emails.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String ORDER= "order";


    private static final String BASE_URL = "baseUrl";

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;
    private final UserRepository userRepository;

    public MailService(
        JHipsterProperties jHipsterProperties,
        JavaMailSender javaMailSender,
        MessageSource messageSource,
        SpringTemplateEngine templateEngine,
        UserRepository userRepository) {
        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.userRepository = userRepository;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug(
            "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart,
            isHtml,
            to,
            subject,
            content
        );

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            log.warn("Email could not be sent to user '{}'", to, e);
        }
    }





    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendEmailOrderFromTemplate(Order order, String templateName, String titleKey) {

        Optional<User> user= userRepository.findById(order.getUser().getId());
        if (user.isPresent()) {

            Locale locale = Locale.forLanguageTag(user.get().getLangKey());
            Context context = new Context(locale);
            context.setVariable(ORDER, order);
            context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
            double priceTotal = order.getOrderItems()
                .stream()
                .mapToDouble(orderItemDTO -> orderItemDTO.getOrderItemQuantityInitialWanted() * orderItemDTO.getBoxCatalog().getCatalog().getPrice())
                .sum();

            context.setVariable("priceTotal",priceTotal);

            Instant createdDate = order.getCreatedDate();


            LocalDateTime dateTime = createdDate.atZone(ZoneId.of("UTC")).toLocalDateTime();

            // Formatter la date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String orderDate = dateTime.format(formatter);
            context.setVariable("orderDate",orderDate);

            String content = templateEngine.process(templateName, context);
            String subject = messageSource.getMessage(titleKey, null, locale);
            sendEmail(user.get().getEmail(), subject, content, false, true);


        }

    }


    @Async
    public void sendEmailToAdminFromTemplate(User admin, User user, String templateName, String titleKey) {
        if (admin == null || admin.getEmail() == null) {
            log.debug("Email doesn't exist for user admin '{}'", admin.getLogin());
            return;
        }

        if (user == null || user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(admin.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendCreationEmail(User user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmailToUser", "email.creation.title");
    }

    @Async
    public void sendActivationConfirmEmail(User user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationConfirmEmail", "email.activation.title");
    }

    @Async
    public void sendActivationRequestEmail(User admin, User user) {
        log.debug("Sending information email to admin user '{}' for user {}", admin.getEmail(), user.getEmail());
        sendEmailToAdminFromTemplate(admin, user, "mail/activationRequestEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "order/passwordResetEmail", "email.reset.title");
    }


    @Async
    public void sendConfirmationOrderMail(Order order) {
        log.debug("Sending confirmation order email to '{}'", order.getUser().getEmail());
        sendEmailOrderFromTemplate(order, "order/orderEmailConfirmation", "email.confirmation.order");
    }

    @Async
    public void sendConfirmationModificationOrderMail(Order order) {
        log.debug("Sending confirmation order email to '{}'", order.getUser().getEmail());
        sendEmailOrderFromTemplate(order, "order/orderEmailConfirmationModification", "email.confirmation.modification.order");
    }


    @Async
    public void sendLivraisonEnCoursOrderMail(Order order) {
        log.debug("Sending confirmation order email to '{}'", order.getUser().getEmail());
        sendEmailOrderFromTemplate(order, "order/orderEmailLivraisonEnCours", "email.livraison.encours.order");
    }

    @Async
    public void sendLivrerOrderMail(Order order) {
        log.debug("Sending confirmation order email to '{}'", order.getUser().getEmail());
        sendEmailOrderFromTemplate(order, "order/orderEmailLivrer", "email.livrer.order");
    }
    @Async
    public void sendPriseEnChargeOrderMail(Order order) {
        log.debug("Sending confirmation order email to '{}'", order.getUser().getEmail());
        sendEmailOrderFromTemplate(order, "order/orderEmailPriseEnCharge", "email.prise.encharge.order");
    }


}
