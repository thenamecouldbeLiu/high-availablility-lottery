package com.interview.lottory.controller.draw;

import com.interview.lottory.controller.draw.mapper.DrawControllerMapper;
import com.interview.lottory.infra.config.DrawProperties;
import com.interview.lottory.service.draw.DrawEventQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class DrawSseService {
    private final DrawEventQueryService queryService;
    private final DrawControllerMapper mapper;
    private final DrawProperties properties;
    private final TaskScheduler taskScheduler;

    public SseEmitter subscribe(UUID eventId, String userId) {
        SseEmitter emitter = new SseEmitter(properties.sseTimeout().toMillis());
        var initial = queryService.get(eventId, userId);
        send(emitter, initial);
        if (initial.terminal()) {
            emitter.complete();
            return emitter;
        }

        AtomicReference<ScheduledFuture<?>> taskRef = new AtomicReference<>();
        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                var status = queryService.get(eventId, userId);
                send(emitter, status);
                if (status.terminal()) {
                    cancel(taskRef);
                    emitter.complete();
                }
            } catch (RuntimeException exception) {
                cancel(taskRef);
                emitter.completeWithError(exception);
            }
        }, properties.ssePollInterval());
        taskRef.set(task);
        emitter.onCompletion(() -> cancel(taskRef));
        emitter.onTimeout(() -> cancel(taskRef));
        emitter.onError(error -> cancel(taskRef));
        return emitter;
    }

    private void send(SseEmitter emitter, com.interview.lottory.service.draw.dto.DrawEventStatusBo status) {
        try {
            emitter.send(SseEmitter.event()
                    .id(status.eventId() + ":" + status.status())
                    .name("draw-status")
                    .data(mapper.toVo(status)));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to send SSE event", exception);
        }
    }

    private void cancel(AtomicReference<ScheduledFuture<?>> taskRef) {
        ScheduledFuture<?> task = taskRef.get();
        if (task != null) {
            task.cancel(false);
        }
    }
}
