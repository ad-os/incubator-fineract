package org.apache.fineract.notification.eventandlistener;

import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.service.NotificationWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

@Service
public class NotificationEventListener implements SessionAwareMessageListener {

    private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;

    private final NotificationWritePlatformService notificationWritePlatformService;

    @Autowired
    public NotificationEventListener(BasicAuthTenantDetailsService basicAuthTenantDetailsService,
                                     NotificationWritePlatformService notificationWritePlatformService) {
        this.basicAuthTenantDetailsService = basicAuthTenantDetailsService;
        this.notificationWritePlatformService = notificationWritePlatformService;
    }

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        if (message instanceof ObjectMessage) {
            NotificationData notificationData = (NotificationData) ((ObjectMessage) message).getObject();

            final FineractPlatformTenant tenant = this.basicAuthTenantDetailsService
                    .loadTenantById(notificationData.getTenantIdentifier(), false);
            ThreadLocalContextUtil.setTenant(tenant);

            if (notificationData.getUserId() == null) {
                notificationWritePlatformService.notify(
                        notificationData.getUserIds(),
                        notificationData.getObjectType(),
                        notificationData.getObjectIdentfier(),
                        notificationData.getAction(),
                        notificationData.getActor(),
                        notificationData.getContent(),
                        notificationData.isSystemGenerated()
                );
            } else {
                notificationWritePlatformService.notify(
                        notificationData.getUserId(),
                        notificationData.getObjectType(),
                        notificationData.getObjectIdentfier(),
                        notificationData.getAction(),
                        notificationData.getActor(),
                        notificationData.getContent(),
                        notificationData.isSystemGenerated()
                );
            }
        }
    }
}
