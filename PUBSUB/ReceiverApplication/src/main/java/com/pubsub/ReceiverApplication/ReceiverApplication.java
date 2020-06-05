package com.pubsub.ReceiverApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class ReceiverApplication {

	private static final Log LOGGER = LogFactory.getLog(ReceiverApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ReceiverApplication.class, args);
	}

	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(@Qualifier("myInputChannel")MessageChannel inputChannel, PubSubTemplate pubSubTemplate){
		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "mySubscription");
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.MANUAL);
		return adapter;
	}

	@Bean
	public MessageChannel myInputChannel(){
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "myInputChannel")
//	public void messageReceiver(String payload){
//		LOGGER.info("Message Arrived in PubSub! Payload: "  +payload);
//	}
	public MessageHandler messageReceiver() {
		return message -> {
			LOGGER.info("Message arrived! Payload: " + new String((byte[]) message.getPayload()));
			BasicAcknowledgeablePubsubMessage originalMessage =
					message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
			originalMessage.ack();
		};
	}

}
