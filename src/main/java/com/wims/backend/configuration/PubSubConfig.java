package com.wims.backend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class PubSubConfig {

    @Bean
    public PubSubMessageConverter pubSubMessageConverter(ObjectMapper objectMapper) {
        return new JacksonPubSubMessageConverter(objectMapper);
    }

    @Bean
    public MessageChannel orderNotificationInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter messageChannelAdapter(
            @Qualifier("orderNotificationInputChannel") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate,
                "order-notifications-sub");
        adapter.setOutputChannel(inputChannel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(com.wims.backend.dto.OrderNotificationMessage.class);
        return adapter;
    }
}
