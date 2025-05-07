package io.github.wesleyosantos91.infrastructure.kafka;

import io.github.wesleyosantos91.infrastructure.kafka.filter.KafkaCustomFilterStrategy;
import io.github.wesleyosantos91.domain.event.Person;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;

import java.util.concurrent.Executors;

@Configuration
public class KafkaListenerConfig {

    @Value("${spring.kafka.listener.ack-mode}")
    private String ackMode;

    @Value("${spring.kafka.consumer.concurrency}")
    private Integer concurrency;

    @Value("${spring.kafka.consumer.max-poll-records}")
    private Long maxPollRecords;

    @Value("${spring.kafka.listener.observation-enabled}")
    private Boolean observationEnabled;

    private final KafkaCustomFilterStrategy kafkaCustomFilterStrategy;

    public KafkaListenerConfig(KafkaCustomFilterStrategy kafkaCustomFilterStrategy) {
        this.kafkaCustomFilterStrategy = kafkaCustomFilterStrategy;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Person> listenerContainerFactory(ConsumerFactory consumerFactory) {

        DefaultKafkaConsumerFactory<String, Person> consumerFactoryEdited = new DefaultKafkaConsumerFactory<>(consumerFactory.getConfigurationProperties());
        ConcurrentKafkaListenerContainerFactory<String, Person> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(concurrency);
        factory.setAckDiscarded(true);
        factory.setRecordFilterStrategy(kafkaCustomFilterStrategy);
        factory.setConsumerFactory(consumerFactoryEdited);
        factory.getContainerProperties().setPollTimeout(maxPollRecords);
        factory.getContainerProperties().setAckMode(AckMode.valueOf(ackMode));
        factory.getContainerProperties().setObservationEnabled(observationEnabled);
        factory.getContainerProperties().setListenerTaskExecutor(new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor()));
        return factory;
    }
}
