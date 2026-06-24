package com.datasheild.siem.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = {"anomaly.detected"})
@DirtiesContext
class SiemKafkaIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("siem-test", "true", embeddedKafkaBroker);
        consumer = new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "anomaly.detected");
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void shouldPublishAndConsumeAnomalyEvent() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
        template.send("anomaly.detected", "{\"tenantId\":\"tenant-a\",\"anomalyScore\":0.88}");
        template.flush();
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "anomaly.detected");
        assertThat(record.value()).contains("anomalyScore");
    }
}
