package com.example.wsreactor.reactor;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@Component
public class SimpleSocketHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(SimpleSocketHandler.class);

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

    private void onFinally(SignalType sign) {
        logger.warn("-> onFinally: {}", sign);
    }

    private String onMessage(WebSocketMessage message) {
        String msg = message.getPayloadAsText();
        logger.info("-> сообщение: {}", msg);
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
                .map(this::onMessage)
                .doOnSubscribe(this::onSubscribe)
                .doOnError(this::onError)
                .doOnComplete(this::onComplete)
                .doFirst(this::onFirst)
                .doOnCancel(this::onCancel)
                .doFinally(this::onFinally)
                .then();
    }
}
