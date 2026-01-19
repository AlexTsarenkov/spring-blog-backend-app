package ru.yandex.practicum.utils;

import java.math.BigDecimal;
import java.util.UUID;

public class Utility {
    public static Long getIdForEntity() {
        return new BigDecimal(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .setScale(-5, BigDecimal.ROUND_UP)
                .longValue();
    }
}
