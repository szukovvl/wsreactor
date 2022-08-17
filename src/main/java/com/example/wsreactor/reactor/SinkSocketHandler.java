package com.example.wsreactor.reactor;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.*;

@Component
public class SinkSocketHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(SinkSocketHandler.class);

    private final Sinks.Many<String> eventPublisher = Sinks
            .many()
            .replay()
            .latest();
    private final Flux<String> events = eventPublisher
            .asFlux()
            .replay(0)
            .autoConnect();
    private final Flux<String> outputEvents = Flux
            .from(events)
            .map(e -> {
                logger.info("-> outputEvents: {}", e);
                return e;
            });

    private String sessionId;

    private void onError(Throwable error) {
        logger.error(error.getMessage());
    }

    private void onComplete() {
        logger.info("-> onComplete");
    }

    private void onFirst() {
        logger.info("-> onFirst");
    }

    private void onCancel() {
        logger.warn("-> onCancel");
    }

    private void onFinally(WebSocketSession session, SignalType sign) {
        logger.warn("-> onFinally: {}", sign);
        if (sessionId != null && sessionId.equals(session.getId())) {
            sessionId = null;
        }
    }

    private String onMessage(WebSocketSession session, WebSocketMessage message) {
        String msg = message.getPayloadAsText();
        logger.info("-> сообщение: {}", msg);
        if (msg.startsWith("admin") && sessionId == null) {
            sessionId = session.getId();
        }
        if (msg.startsWith("final") && sessionId != null && sessionId.equals(session.getId())) {
            sessionId = null;
        }
        pushEvent(msg);
        return msg;
    }

    private void onSubscribe(Subscription data) {
        logger.info("-> onSubscribe: {}", data);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        logger.info("-> handle: {}", session);

        return session
                .receive()
                .doOnSubscribe(this::onSubscribe)
                .map(e -> onMessage(session, e))
                .doOnError(this::onError)
                .doOnComplete(this::onComplete)
                .doFirst(this::onFirst)
                .doOnCancel(this::onCancel)
                .doFinally(e -> onFinally(session, e))
                .zipWith(session.send(outputEvents.map(e -> {
                    if ((sessionId != null) && (sessionId.equals(session.getId()))) {
                        e = "(АДМИНИСТРАТОР) " + e;
                    } else {
                        e = "(USER) " + e;
                    }
                    return session.textMessage(e);
                })))
                .then();
    }

    public synchronized void pushEvent(String event) {
        eventPublisher.tryEmitNext(event);
    }
}
