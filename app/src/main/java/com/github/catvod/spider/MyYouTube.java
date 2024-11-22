package com.github.catvod.spider;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Filter;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.bean.alist.Drive;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MyYouTube extends Spider {

    private static String API_KEY;
    private static final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/search";


    @Override
    public void init(Context context, String extend) {
        SpiderDebug.log("MyYouTube init 参数- >>> context = " + context + ", extend = " + extend);
        API_KEY = extend;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        SpiderDebug.log("MyYouTube homeContent 参数 >>> filter = " + filter);
        try {
            // 构建获取热门视频的请求URL
            String url = "https://www.googleapis.com/youtube/v3/videos" +
                    "?part=snippet" +
                    "&chart=mostPopular" +  // 获取热门视频
                    "&maxResults=20" +      // 返回20个结果
                    "&regionCode=HK" +      // 修改为香港地区
                    "&key=" + API_KEY;

            SpiderDebug.log("请求URL：" + url);

            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");

            String response = OkHttp.string(url, headers);
//            SpiderDebug.log("YouTube热门视频响应：" + response);

            JSONObject json = new JSONObject(response);
            List<Vod> videos = new ArrayList<>();

            if (json.has("items")) {
                JSONArray items = json.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    try {
                        JSONObject item = items.getJSONObject(i);
                        JSONObject snippet = item.getJSONObject("snippet");

                        String videoId = item.getString("id");
                        String title = snippet.getString("title");
                        String thumbnail = snippet.getJSONObject("thumbnails")
                                .getJSONObject("default")
                                .getString("url");

                        Vod vod = new Vod();
                        vod.setVodId(videoId);
                        vod.setVodName(title);
                        vod.setVodPic(thumbnail);
                        vod.setVodRemarks("热门视频");
                        
                        videos.add(vod);
                    } catch (Exception e) {
                        SpiderDebug.log("处理热门视频时出错：" + e.getMessage());
                    }
                }
            }

            return Result.string(videos);
        } catch (Exception e) {
            SpiderDebug.log("获取热门视频失败：" + e.getMessage());
            e.printStackTrace();
            return Result.string(new ArrayList<>());
        }
    }

    @Override
    public String searchContent(String keyword, boolean quick) throws Exception {
        SpiderDebug.log("MyYouTube searchContent 参数 >>> keyword = " + keyword + ", quick = " + quick);
        try {
            // 构建请求URL，添加playlist类型
            String url = YOUTUBE_API_URL +
                    "?part=snippet" +
                    "&maxResults=20" +
                    "&q=" + Uri.encode(keyword) +
                    "&type=video,playlist" + // 修改这里，同时搜索视频和播放列表
                    "&key=" + API_KEY;

            SpiderDebug.log("请求URL：" + url);

            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");

            String response = OkHttp.string(url, headers);
//            SpiderDebug.log("YouTube搜索响应：" + response);

            JSONObject json = new JSONObject(response);
            List<Vod> results = new ArrayList<>();

            if (json.has("items")) {
                JSONArray items = json.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    try {
                        JSONObject item = items.getJSONObject(i);
                        JSONObject id = item.getJSONObject("id");
                        JSONObject snippet = item.getJSONObject("snippet");
                        String kind = id.getString("kind");

                        Vod vod = new Vod();
                        String title = snippet.getString("title");
                        String thumbnail = snippet.getJSONObject("thumbnails")
                                .getJSONObject("default")
                                .getString("url");

                        if ("youtube#video".equals(kind)) {
                            // 处理视频
                            String videoId = id.getString("videoId");
                            vod.setVodId(videoId);
                            vod.setVodName(title);
                            vod.setVodPic(thumbnail);
                            vod.setVodRemarks("YouTube视频");
//                            SpiderDebug.log("处理视频：" + videoId + " - " + title);
                        } else if ("youtube#playlist".equals(kind)) {
                            // 处理播放列表
                            String playlistId = id.getString("playlistId");
                            vod.setVodId("playlist_" + playlistId); // 添加前缀以区分视频和播放列表
                            vod.setVodName("[播放列表]" + title);
                            vod.setVodPic(thumbnail);
                            vod.setVodRemarks("YouTube播放列表");
//                            SpiderDebug.log("处理播放列表：" + playlistId + " - " + title);
                        }

                        results.add(vod);
                    } catch (Exception e) {
                        SpiderDebug.log("处理搜索结果时出错：" + e.getMessage());
                    }
                }
            }

            return Result.string(results);
        } catch (Exception e) {
            SpiderDebug.log("YouTube搜索失败：" + e.getMessage());
            e.printStackTrace();
            return Result.string(new ArrayList<>());
        }
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        SpiderDebug.log("MyYouTube detailContent 参数 >>> ids = " + ids);
        try {
            String id = ids.get(0);
            boolean isPlaylist = id.startsWith("playlist_");
            String realId = isPlaylist ? id.substring("playlist_".length()) : id;

            // 构建API请求URL
            String apiUrl;
            if (isPlaylist) {
                apiUrl = "https://www.googleapis.com/youtube/v3/playlists" +
                        "?part=snippet,contentDetails" +
                        "&id=" + realId +
                        "&key=" + API_KEY;
            } else {
                apiUrl = "https://www.googleapis.com/youtube/v3/videos" +
                        "?part=snippet,contentDetails,statistics" +
                        "&id=" + realId +
                        "&key=" + API_KEY;
            }

            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");

            String response = OkHttp.string(apiUrl, headers);
//            SpiderDebug.log("YouTube详情响应：" + response);

            JSONObject json = new JSONObject(response);
            if (!json.has("items") || json.getJSONArray("items").length() == 0) {
                return "";
            }

            JSONObject item = json.getJSONArray("items").getJSONObject(0);
            JSONObject snippet = item.getJSONObject("snippet");

            Vod vod = new Vod();
            vod.setVodId(id);
            vod.setVodName(snippet.getString("title"));
            vod.setTypeName("YouTube" + (isPlaylist ? "播放列表" : "视频"));
            vod.setVodPic(snippet.getJSONObject("thumbnails")
                    .getJSONObject("high")
                    .getString("url"));

            // 设置发布时间为年份
            String publishedAt = snippet.getString("publishedAt").substring(0, 4);
            vod.setVodYear(publishedAt);

            // 设置地区（YouTube）
            vod.setVodArea("YouTube");

            // 设置描述
            vod.setVodContent(snippet.getString("description"));

            if (isPlaylist) {
                // 获取播放列表中的视频
                String playlistItemsUrl = "https://www.googleapis.com/youtube/v3/playlistItems" +
                        "?part=snippet" +
                        "&maxResults=50" +
                        "&playlistId=" + realId +
                        "&key=" + API_KEY;

                String playlistResponse = OkHttp.string(playlistItemsUrl, headers);
                JSONObject playlistJson = new JSONObject(playlistResponse);
                JSONArray playlistItems = playlistJson.getJSONArray("items");

                List<String> playUrls = new ArrayList<>();
                for (int i = 0; i < playlistItems.length(); i++) {
                    JSONObject playlistItem = playlistItems.getJSONObject(i);
                    JSONObject videoSnippet = playlistItem.getJSONObject("snippet");
                    String videoId = videoSnippet.getJSONObject("resourceId").getString("videoId");
                    String videoTitle = cleanTitle(videoSnippet.getString("title"));
                    playUrls.add(videoTitle + "$" + videoId);
                }

                vod.setVodPlayFrom("YouTube");
                vod.setVodPlayUrl(TextUtils.join("#", playUrls));
                vod.setVodRemarks("共" + playlistItems.length() + "个视频");
            } else {
                // 单个视频
                JSONObject statistics = item.getJSONObject("statistics");
                String viewCount = statistics.getString("viewCount");
                vod.setVodRemarks("播放量: " + viewCount);

                // 设置播放地址
                vod.setVodPlayFrom("YouTube");
                vod.setVodPlayUrl(cleanTitle(snippet.getString("title")) + "$" + id);
            }

            return Result.string(vod);
        } catch (Exception e) {
            SpiderDebug.log("获取详情失败：" + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }


    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        SpiderDebug.log("MyYouTube playerContent 参数 >>> flag = " + flag + ", id = " + id + ", vipFlags = " + vipFlags);
        try {
            String playUrl;
            if (id.startsWith("playlist_")) {
                // 处理播放列表
                String playlistId = id.substring("playlist_".length());
                playUrl = "https://www.youtube.com/playlist?list=" + playlistId;
            } else {
                // 处理单个视频
                playUrl = "https://www.youtube.com/watch?v=" + id;
            }

            return Result.get()
                    .url(playUrl)
                    .parse()
                    .string();
        } catch (Exception e) {
            SpiderDebug.log("获取播放地址失败：" + e.getMessage());
            return Result.get().string();
        }
    }

    private String cleanTitle(String title) {
        return title.replaceAll("[$#]", ""); // 使用正则表达式移除所有 $ 和 # 字符
    }

}