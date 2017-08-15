package com.dmdirc.x.parser;

import com.dmdirc.parser.events.ParserEvent;
import com.dmdirc.parser.interfaces.Parser;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ObservableParser {

    private static final Logger logger = LogManager.getLogger(ObservableParser.class);

    private final Parser parser;
    private Observable<ParserEvent> observable;

    public ObservableParser(final Parser parser) {
        this.parser = parser;
    }

    public Observable<ParserEvent> eventObservable() {
        if (observable == null) {
            observable = Observable.create(e -> {
                logger.info("Creating new observable!");
                CallbackListener listener = new CallbackListener(e);
                parser.getCallbackManager().subscribe(listener);
                e.setCancellable(() -> parser.getCallbackManager().unsubscribe(listener));
            });
        }
        return observable;
    }

    @Listener(references = References.Strong)
    private static final class CallbackListener {

        private final ObservableEmitter<ParserEvent> emitter;

        private CallbackListener(final ObservableEmitter<ParserEvent> emitter) {
            this.emitter = emitter;
        }

        @Handler
        public void handleEvent(final ParserEvent parserEvent) {
            logger.trace("Emitting event of type {}", parserEvent.getClass().getSimpleName());
            emitter.onNext(parserEvent);
        }

    }

}
