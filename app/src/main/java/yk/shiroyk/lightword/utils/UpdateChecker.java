package yk.shiroyk.lightword.utils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import yk.shiroyk.lightword.BuildConfig;

public class UpdateChecker {
    private final static String API = "https://api.github.com/repos/shiroyk/LightWordAndroid/releases/latest";

    private String getApiData() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(API);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        return null;
    }

    private Integer parseVersion(String s) {
        return Integer.parseInt(
                s.replaceAll("[^0-9]", ""));
    }

    private LatestReleases getLatestReleases(String s) {
        Gson gson = new Gson();
        return gson.fromJson(s, LatestReleases.class);
    }

    public String[] checkUpdate() {
        String s = getApiData();
        if (s != null) {
            UpdateChecker.LatestReleases latest = getLatestReleases(s);
            if (parseVersion(BuildConfig.VERSION_NAME)
                    < parseVersion(latest.tag_name)) {
                return new String[]{latest.name, latest.body, latest.html_url};
            } else {
                return new String[]{"已是最新版本"};
            }
        } else {
            return new String[]{"获取信息超时"};
        }
    }

    private static class LatestReleases {
        public String html_url;
        public String tag_name;
        public String name;
        public String body;
        public List<Assets> assets;

        private static class Assets {
            public String name;
            public String size;
            public String created_at;
            public String updated_at;
            public String browser_download_url;
        }
    }
}
