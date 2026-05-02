package com.lp.criticabackend.util;

import com.lp.criticabackend.AppLogger;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.logging.log4j.Logger;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WebUtil {

    private static final AppLogger log = AppLogger.getLogger(WebUtil.class);

    public static HttpClient httpClient(){
        int timeout = 15000;
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn -> {
                    conn
                            .addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                });
    }



}
