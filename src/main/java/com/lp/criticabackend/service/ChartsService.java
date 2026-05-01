package com.lp.criticabackend.service;

import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.Song;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ChartsService {

    private final WebClient webClient;

    private static final AppLogger log = AppLogger.getLogger(ChartsService.class);

    public ChartsService() {
        this.webClient = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient()))
                .build();
    }

    private static HttpClient httpClient(){
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

    public List<Song> fetchCharts(String country){

        String url = "https://kworb.net/spotify/country/" + country + "_weekly.html";

        String html = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if(html == null || html.isEmpty()){
            log.warn("Failed to fetch charts");
        }

        return parseCharts(html);
    }

    private List<Song> parseCharts(String html){
        List<Song> songs = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Element table = doc.selectFirst("table");

        if(table == null){
            log.error("Charts table not found");
            return songs;
        } else {
            Elements rows = table.select("tr");

            for (Element row : rows) {
                Element posEl = row.selectFirst("td.np");
                Element textEl = row.selectFirst("td.text.mp");

                if (posEl == null || textEl == null) continue;

                int position = Integer.parseInt(posEl.text());

                Element titleEl = textEl.selectFirst("a[href*=track]");
                String title = titleEl != null ? titleEl.text() : "Unknown Title";

                Elements artistLinks = textEl.select("a");
                List<String> artistNames = artistLinks.stream()
                        .map(Element::text)
                        .toList();

                String artists = String.join(", ", artistNames);

                Song song = new Song(position, title,
                        artists.isEmpty() ? "Unknown Artist" : artists);

                songs.add(new Song(position, title,
                        artists.isEmpty() ? "Unknown Artist" : artists));
            }
        }

        return songs;
    }
}
