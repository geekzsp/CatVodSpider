package com.github.catvod.debug;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import com.github.catvod.R;
import com.github.catvod.crawler.Spider;
import com.github.catvod.spider.AList;
import com.github.catvod.spider.Doll;
import com.github.catvod.spider.Init;
import com.github.catvod.spider.MyYouTube;
import com.github.catvod.spider.Push;
import com.github.catvod.spider.WebDAV;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private ExecutorService executor;
    private Spider spider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button homeContent = findViewById(R.id.homeContent);
        Button homeVideoContent = findViewById(R.id.homeVideoContent);
        Button categoryContent = findViewById(R.id.categoryContent);
        Button detailContent = findViewById(R.id.detailContent);
        Button playerContent = findViewById(R.id.playerContent);
        Button searchContent = findViewById(R.id.searchContent);
        homeContent.setOnClickListener(view -> executor.execute(this::homeContent));
        homeVideoContent.setOnClickListener(view -> executor.execute(this::homeVideoContent));
        categoryContent.setOnClickListener(view -> executor.execute(this::categoryContent));
        detailContent.setOnClickListener(view -> executor.execute(this::detailContent));
        playerContent.setOnClickListener(view -> executor.execute(this::playerContent));
        searchContent.setOnClickListener(view -> executor.execute(this::searchContent));
        Logger.addLogAdapter(new AndroidLogAdapter());
        executor = Executors.newCachedThreadPool();
        executor.execute(this::initSpider);
    }

    private void initSpider() {
        try {
            Init.init(getApplicationContext());
            spider = new MyYouTube();
            spider.init(this,  "");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void homeContent() {
        try {
            Logger.t("homeContent").d(spider.homeContent(true));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void homeVideoContent() {
        try {
            Logger.t("homeVideoContent").d(spider.homeVideoContent());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void categoryContent() {
        try {
            Logger.t("categoryContent").d(spider.categoryContent("movie", "1", true, new HashMap<>()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void detailContent() {
        try {
            Logger.t("detailContent").d(spider.detailContent(Arrays.asList("playlist_PLIj4BzSwQ-_v7SIS5pseLhvRQ5hlAEArA")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void playerContent() {
        try {
            Logger.t("playerContent").d(spider.playerContent("直連", "playlist_PLIj4BzSwQ-_v7SIS5pseLhvRQ5hlAEArA", new ArrayList<>()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void searchContent() {
        try {
            Logger.t("searchContent").d(spider.searchContent("杨善洲", false));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}