package io.github.wesleyosantos91.message;

import io.github.wesleyosantos91.core.metric.service.MetricsService;
import io.github.wesleyosantos91.domain.event.Person;
import io.github.wesleyosantos91.domain.exception.BusinessException;
import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class PersonConsumer {

    public static final String[] TAG_NAMES = {"className", "topic", "status", "message"};
    public static final String CLASS_SIMPLE_NAME = PersonConsumer.class.getSimpleName();
    private static final Log logger = LogFactory.getLog(CLASS_SIMPLE_NAME);
    public static final String SUCCESS = "SUCCESS";
    public static final String SUCCESSFUL_EXECUTIONS = "Successful executions";
    public static final String ERROR = "ERROR";

    private final MetricsService metricsService;

    public PersonConsumer(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "listenerContainerFactory")
    public void consumer(@Payload ConsumerRecord<String, Person> consumerRecord, Acknowledgment ack) throws BusinessException {

        final StopWatch stopWatch = new StopWatch();

        try {

            stopWatch.start();

            logger.info(MessageFormat.format("process record {0}", consumerRecord.value()));

            logger.info(MessageFormat.format("key: {0}", consumerRecord));
            logger.info(MessageFormat.format("Headers: {0}", consumerRecord.headers()));
            logger.info(MessageFormat.format("topic: {0}", consumerRecord.topic()));
            logger.info(MessageFormat.format("Partion: {0}", consumerRecord.partition()));
            logger.info(MessageFormat.format("Person: {0}", consumerRecord.value()));
            ack.acknowledge();
            stopWatch.stop();

            metricsService.incrementMultiTaggedCounter("kafka.consumer.person.messages.count", TAG_NAMES,
                    CLASS_SIMPLE_NAME, consumerRecord.topic(), SUCCESS, SUCCESSFUL_EXECUTIONS);
            metricsService.recordMultiTaggedTimer("kafka.consumer.person.messages.timer", TAG_NAMES, stopWatch.getTotalTimeMillis(),
                    CLASS_SIMPLE_NAME, consumerRecord.topic(), SUCCESS, SUCCESSFUL_EXECUTIONS);

        } catch (Exception e) {
            logger.error("Erro ao processar o evento.");
            ack.acknowledge();
            stopWatch.stop();

            metricsService.incrementMultiTaggedCounter("kafka.consumer.person.messages.count", TAG_NAMES,
                    CLASS_SIMPLE_NAME, consumerRecord.topic(), ERROR, e.getMessage());

            metricsService.recordMultiTaggedTimer("kafka.consumer.person.messages.timer", TAG_NAMES, stopWatch.getTotalTimeMillis(),
                    CLASS_SIMPLE_NAME, consumerRecord.topic(), ERROR, MessageFormat.format("Executions with errors {0}", e.getMessage()));

        }
    }
}