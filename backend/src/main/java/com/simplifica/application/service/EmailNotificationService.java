package com.simplifica.application.service;

import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for sending email notifications.
 *
 * This is a stub implementation that logs email notifications instead of sending them.
 * Full email functionality will be implemented in Phase 6 of the multi-tenant feature.
 *
 * When email notifications are enabled (EMAIL_NOTIFICATIONS_ENABLED=true), this service
 * will send emails for events like user assignments and removals from institutions.
 */
@Service
public class EmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    /**
     * Sends an email notification when a user is assigned to an institution.
     *
     * @param user the user being assigned
     * @param institution the institution
     * @param roles the roles granted to the user
     */
    public void sendUserAssignedToInstitutionEmail(User user, Institution institution, Set<InstitutionRole> roles) {
        LOGGER.info("TODO: Send email to {} - Assigned to institution {} with roles {}",
                    user.getEmail(), institution.getName(), roles);
        // TODO: Implement email sending (Phase 6)
        // - Load email template (Thymeleaf)
        // - Populate template with user, institution and roles data
        // - Send email via JavaMailSender
        // - Handle async processing with @Async
        // - Implement retry logic for failed sends
    }

    /**
     * Sends an email notification when a user is removed from an institution.
     *
     * @param user the user being removed
     * @param institution the institution
     */
    public void sendUserRemovedFromInstitutionEmail(User user, Institution institution) {
        LOGGER.info("TODO: Send email to {} - Removed from institution {}",
                    user.getEmail(), institution.getName());
        // TODO: Implement email sending (Phase 6)
        // - Load email template (Thymeleaf)
        // - Populate template with user and institution data
        // - Send email via JavaMailSender
        // - Handle async processing with @Async
        // - Implement retry logic for failed sends
    }
}
