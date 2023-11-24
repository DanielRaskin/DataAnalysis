package dataanalysis;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

/**
 * Web server
 */
public class Server {
    public static void main(String[] args) {
        var dataProcessor = new DataProcessor();
        var webData = dataProcessor.process();
        Javalin.create(config ->
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/";
                staticFileConfig.directory = "/web";
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.precompress = true;
                staticFileConfig.skipFileFunction = req -> false;
            }))
            .get("/events", ctx -> ctx.json(webData.getEvents()))
            .get("/ctr", ctx -> ctx.json(webData.getCtr(getStepInHours(ctx))))
            .get("/evpm", ctx -> ctx.json(webData.getEvpm(getEvent(ctx), getStepInHours(ctx))))
            .get("/dma", ctx -> ctx.json(webData.getDmaData(getEvent(ctx))))
            .get("/siteid", ctx -> ctx.json(webData.getSiteIdData(getEvent(ctx))))
            .start(8080);
    }

    private static int getStepInHours(Context ctx) {
        return ctx.queryParamAsClass("stepInHours", Integer.class)
                .check(s -> ((s >= 1) && (s <= 48)), "step in hours should be from 1 to 48")
                .get();
    }

    private static String getEvent(Context ctx) {
        return ctx.queryParamAsClass("event", String.class)
                .check(s -> (s != null) && (! s.isBlank()), "event should not be blank")
                .get().trim();
    }
}