package io.github.wesleyosantos91.core.metric.service;

import io.github.wesleyosantos91.core.metric.tag.counter.MultiTaggedCounter;
import io.github.wesleyosantos91.core.metric.tag.timer.MultiTaggedTimer;
import io.github.wesleyosantos91.message.PersonConsumer;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private static final Log logger = LogFactory.getLog(MetricsService.class);

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementCounter(String name, String... tags) {
        try {
            meterRegistry.counter(name, tags).increment();
        } catch (Exception e) {
            logger.error("Erro ao incrementar o contador.", e);
        }
    }

    public long summary(String name, OffsetDateTime dateTime, String... tags) {
        long delay = Duration.between(dateTime, OffsetDateTime.now()).toMillis();

        try {
            DistributionSummary summary = meterRegistry.summary(name, tags);
            summary.record(delay);
        } catch (Exception e) {
            logger.error("Erro ao obter o resumo.", e);
        }

        return delay;
    }

    public void summary(String name, long delay, String... tags) {

        try {
            DistributionSummary summary = meterRegistry.summary(name, tags);
            summary.record(delay);
        } catch (Exception e) {
            logger.error("Erro ao obter o resumo.", e);
        }
    }

    public void incrementMultiTaggedCounter(String name, String[] tagNames, String... tagValues) {
        try {
            MultiTaggedCounter counter = new MultiTaggedCounter(name, meterRegistry, tagNames);
            counter.increment(tagValues);
        } catch (Exception e) {
            logger.error("Erro ao incrementar MultiTaggedCounter.", e);
        }
    }

    public void recordMultiTaggedTimer(String name, String[] tagNames, long durationMillis, String... tagValues) {
        try {
            MultiTaggedTimer timer = new MultiTaggedTimer(name, meterRegistry, tagNames);
            Timer t = timer.getTimer(tagValues);
            t.record(Duration.ofMillis(durationMillis));
        } catch (Exception e) {
            logger.error("Erro ao registrar MultiTaggedTimer.", e);
        }
    }
}
