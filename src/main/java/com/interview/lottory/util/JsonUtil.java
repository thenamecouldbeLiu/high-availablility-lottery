package com.interview.lottory.util;

import com.interview.lottory.infra.Constants;
import com.interview.lottory.infra.exception.ErrorCode;
import com.interview.lottory.infra.exception.InterviewException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JsonUtil {
    private final ObjectMapper objectMapper;

    public String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new InterviewException(ErrorCode.INTERNAL_ERROR, exception,
                    Constants.MessageKey.DRAW_SERIALIZATION_FAILED);
        }
    }

    public <T> T readJson(String value, Class<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (JacksonException exception) {
            throw new InterviewException(ErrorCode.INTERNAL_ERROR, exception,
                    Constants.MessageKey.DRAW_DESERIALIZATION_FAILED);
        }
    }
}
