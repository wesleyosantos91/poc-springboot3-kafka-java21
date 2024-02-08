package io.github.wesleyosantos91.consumer;

import io.github.wesleyosantos91.domain.event.Person;
import io.github.wesleyosantos91.domain.exception.BusinessException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class PersonConsumer {

    private static final Log logger = LogFactory.getLog(PersonConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic}", containerFactory = "listenerContainerFactory")
    public void consumer(@Payload ConsumerRecord<String, Person> consumerRecord, Acknowledgment ack) {
        try {
            logger.info(MessageFormat.format("process record {0}", consumerRecord.value()));

            logger.info(MessageFormat.format("key: {0}", consumerRecord));
            logger.info(MessageFormat.format("Headers: {0}", consumerRecord.headers()));
            logger.info(MessageFormat.format("topic: {0}", consumerRecord.topic()));
            logger.info(MessageFormat.format("Partion: {0}", consumerRecord.partition()));
            logger.info(MessageFormat.format("Person: {0}", consumerRecord.value()));
            ack.acknowledge();
        } catch (Exception e) {
            logger.error("Erro ao processar o evento.");
        }
    }
}