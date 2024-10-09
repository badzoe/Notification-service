package za.co.invest.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import za.co.invest.notification_service.controller.AsyncController;

import java.io.IOException;

@Configuration
public class RabbitMQConfig {

    private final CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    public RabbitMQConfig(CachingConnectionFactory cachingConnectionFactory) {
        this.cachingConnectionFactory = cachingConnectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin() throws IOException {
        RabbitAdmin admin = new RabbitAdmin(cachingConnectionFactory);
        admin.declareQueue(createNotificationsQueue());
        admin.declareExchange(notificationsServiceExchange());
        admin.declareBinding(notificationsBinding());
        admin.initialize(); // Might throw IOException if RabbitMQ is unreachable
        return admin;
    }

    @Bean
    public Queue createNotificationsQueue() {
        return new Queue("ZW.TMS.OPENIT" + "." + "EMAILNOTIFICATIONS", true, false, false);
    }

    @Bean
    public Binding notificationsBinding() {
        return BindingBuilder.
                bind(createNotificationsQueue()).
                to(notificationsServiceExchange()).
                with("ZW.TMS.OPENIT" + "." + "EMAILNOTIFICATIONS");
    }

    @Bean
    public TopicExchange notificationsServiceExchange() {
        return new TopicExchange("MUTANGABENDETECHNOLOGIES" + "." + "ZW.TMS.OPENIT" + "." + "EMAILNOTIFICATIONS");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleMessageListenerContainer listenerContainer(AsyncController asyncController) {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
        listenerContainer.setConnectionFactory(cachingConnectionFactory);
        listenerContainer.setQueueNames("ZW.TMS.OPENIT" + "." + "EMAILNOTIFICATIONS");
        listenerContainer.setMessageListener(asyncController);
        listenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        listenerContainer.setConcurrency("1");
        listenerContainer.setPrefetchCount(1);
        return listenerContainer;
    }
}
