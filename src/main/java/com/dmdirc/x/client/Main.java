package com.dmdirc.x.client;

import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.events.ChannelEvent;
import com.dmdirc.parser.events.ChannelMessageEvent;
import com.dmdirc.parser.events.ParserEvent;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.x.parser.ObservableParser;
import io.reactivex.Observable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import java.net.URI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.richtext.StyleClassedTextArea;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String... args) {
        Application.launch(HelloWorld.class, args);
    }

    public static class HelloWorld extends Application {

        private StyleClassedTextArea area;

        @Override
        public void start(Stage primaryStage) {
            area = new StyleClassedTextArea();
            area.insertText(0, "Hello");

            StackPane root = new StackPane();
            root.getChildren().add(area);

            Scene scene = new Scene(root, 300, 250);

            primaryStage.setTitle("Hello World!");
            primaryStage.setScene(scene);
            primaryStage.show();

            MyInfo info = new MyInfo();
            info.setNickname(getParameters().getNamed().getOrDefault("nick", "luser"));
            IRCParser parser = new IRCParser(info, URI.create(getParameters().getNamed().get("uri")));
            ObservableParser observableParser = new ObservableParser(parser);
            parser.connect();

            observableParser.eventObservable().subscribe(this::handleEvent);
            observableParser.eventObservable()
                    .filter(e -> e instanceof ChannelEvent)
                    .map(e -> (ChannelEvent) e)
                    .filter(c -> c.getChannel().getName().equalsIgnoreCase(
                            getParameters().getNamed().getOrDefault("channel", "#dmdirc")))
                    .filter(e -> e instanceof ChannelMessageEvent)
                    .map(e -> (ChannelMessageEvent) e)
                    .map(e -> e.getClient().getClient().getNickname() + "> " + e.getMessage())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(this::handleChannelEvent);

            Observable.range(0, 250000)
                    .map(String::valueOf)
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(this::handleChannelEvent);
        }


        private void handleEvent(ParserEvent event) {
            //logger.debug("Received event {}", event.getClass().getName());
        }

        private void handleChannelEvent(String line) {
            int length = area.getLength();
            area.insertText(length, "\n" + line);
        }

    }

}
