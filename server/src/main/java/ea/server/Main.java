package ea.server;

import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        WebSocketHandler handler = new WebSocketHandler();

        Javalin.create(config -> config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost())))
            .ws("/evolution", ws -> {
                ws.onMessage(ctx -> handler.onMessage(ctx, ctx.message()));
                ws.onClose(ctx -> handler.onClose(ctx));
                ws.onError(ctx -> handler.onClose(ctx));
            })
            .start(port);

        System.out.println("Server started on port " + port);
    }
}
