package com.vasimvahabov.stockmarketsimulator.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaConstants {

    // Ack mode values for @KafkaListener compile-time constants
    public static final String ACK_MODE_BATCH = "BATCH";

    public static final String ACK_MODE_RECORD = "RECORD";

    public static final String ACK_MODE_MANUAL = "MANUAL";

    public static final String ACK_MODE_COUNT = "COUNT";

    public static final String ACK_MODE_TIME = "TIME";

    public static final String ACK_MODE_COUNT_TIME = "COUNT_TIME";


    // Batch processing values for @KafkaListener compile-time constants
    public static final String BATCH_ENABLED = "true";

    public static final String BATCH_DISABLED = "false";

}
