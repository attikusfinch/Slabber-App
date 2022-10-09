package com.amik.slabber.Widget;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Parser {

    private static final String slabber_api = "https://slabber.io/api/posts?page=1&withImage=1";

    private String lang = "ru-RU,ru;q=0.5";

    public String[] getLastPost() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://slabber.io/api/posts?page=1&withImage=1")
                .header("Accept-Language", lang)
                .build();

        Call call = client.newCall(request);

        JSONObject jsonObject = new JSONObject(call.execute().body().string());
        Document document = Jsoup.parse(jsonObject.get("html").toString());
        Elements elements = document.select("div.card.bPopularPosts__item.bCard");

        document = Jsoup.parse(elements.get(0).toString());
        String link = document.select("a").get(0).attr("href");
        link = "https://slabber.io" + link;
        Elements card_body = document.select("div.card-body");
        Elements data = card_body.select("a");
        Elements image_style = document.select("div.bCard__cover");
        String image = image_style.attr("style");
        String image_path = image.split("'")[1];
        String title = data.get(0).text();
        String description = data.get(1).text();

        return new String[]{title, description, image_path};
    }
}
