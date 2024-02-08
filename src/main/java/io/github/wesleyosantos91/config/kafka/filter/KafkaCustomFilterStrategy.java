package io.github.wesleyosantos91.config.kafka.filter;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static io.github.wesleyosantos91.domain.event.header.HeaderKafkaContant.PRODUCT;

@Component
public class KafkaCustomFilterStrategy implements RecordFilterStrategy<String, GenericRecord> {

    @Value("#{'${spring.kafka.consumer.valid.product}'.split(',')}")
    private List<String> productsValids;

    @Override
    public boolean filter(ConsumerRecord consumerRecord) {

        Headers headers = consumerRecord.headers();

        Header header = headers.lastHeader(PRODUCT);

        if (Objects.isNull(header)) {
            return true;
        }

        String value = new String(header.value());
        return !productsValids.contains(value);
    }
}
