package za.co.invest.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import za.co.invest.notification_service.config.LocalDateTimeAdapter;

import java.time.LocalDateTime;

public record MicroServiceRequest(
        @JsonProperty("id") Long id,
        @JsonProperty("to") String toEmail,
        @JsonProperty("title") String title,
        @JsonProperty("body") String bodyMessage,
        @JsonProperty("status") String status,
        @JsonProperty("createDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) LocalDateTime createDate,
        @JsonProperty("processDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) LocalDateTime processDate
) {
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }
    @Override
    public String toString() {
        return createGson().toJson(this);
    }
}
