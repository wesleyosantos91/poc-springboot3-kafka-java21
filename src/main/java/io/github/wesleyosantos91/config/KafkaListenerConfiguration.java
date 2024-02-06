package io.github.wesleyosantos91.config;

import io.github.wesleyosantos91.domain.event.Person;
import io.github.wesleyosantos91.domain.event.header.HeaderKafkaContant;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.Header;
import org.graalvm.nativeimage.c.CHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import static io.github.wesleyosantos91.domain.event.header.HeaderKafkaContant.PRODUCT;

@Configuration
public class KafkaListenerConfiguration {

    @Value("${spring.kafka.listener.ack-mode}")
    private String ackMode;

    @Value("${spring.kafka.consumer.concurrency}")
    private Integer concurrency;

    @Value("${spring.kafka.consumer.max-poll-records}")
    private Long maxPollRecords;

    @Value("${spring.kafka.listener.observation-enabled}")
    private Boolean observationEnabled;

    @Value("#{'${spring.kafka.consumer.valid.product}'.split(',')}")
    private List<String> productsValids;


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Person> listenerContainerFactory(ConsumerFactory consumerFactory) {

        DefaultKafkaConsumerFactory<String, Person> consumerFactoryEdited = new DefaultKafkaConsumerFactory<>(consumerFactory.getConfigurationProperties());
        ConcurrentKafkaListenerContainerFactory<String, Person> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(concurrency);
        factory.setAckDiscarded(true);
        factory.setRecordFilterStrategy(recordFilterDefaultStrategy()); // TODO IMPLEMENTAR FILTRO PELO HEADER
        factory.setConsumerFactory(consumerFactoryEdited);
        factory.getContainerProperties().setPollTimeout(maxPollRecords);
        factory.getContainerProperties().setAckMode(AckMode.valueOf(ackMode));
        factory.getContainerProperties().setObservationEnabled(observationEnabled);
        factory.getContainerProperties().setListenerTaskExecutor(new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor()));
        return factory;
    }

    private RecordFilterStrategy<String, GenericRecord> recordFilterDefaultStrategy() {
        return filterEvents(productsValids);
    }

    private RecordFilterStrategy<String, GenericRecord> filterEvents(List<String> productsValids) {
        return consumerRecord -> isProductValid(consumerRecord.headers().lastHeader(PRODUCT), productsValids);
    }

    private boolean isProductValid(Header header, List<String> productsValids) {
        if (Objects.isNull(header)) {
            return true;
        }

        String value = new String(header.value());

        return !productsValids.contains(value);
    }
}
