package com.meraki.utils;

import com.meraki.workerNode.dto.TaskHttpRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HttpUtils {

    public static TaskHttpRequest parse(String curl) {

        TaskHttpRequest req = new TaskHttpRequest();
        List<String> tokens = tokenize(curl);
        Iterator<String> it = tokens.iterator();
        req.setMethod("GET"); // default

        while (it.hasNext()) {
            String token = it.next();

            switch (token) {

                case "curl":
                    break;

                case "-X":
                case "--request":
                    req.setMethod(it.next().toUpperCase());
                    break;

                case "-H":
                case "--header":
                    parseHeader(it.next(), req);
                    break;

                case "-d":
                case "--data":
                case "--data-raw":
                    req.setBody(it.next());
                    if ("GET".equals(req.getMethod())) {
                        req.setMethod("POST"); // curl default behavior
                    }
                    break;

                case "--max-time":
                    req.setTimeoutSeconds(Integer.parseInt(it.next()));
                    break;

                default:
                    if (token.startsWith("http") || token.startsWith("https")) {
                        req.setUrl(token);
                    }
            }
        }

        return req;
    }

    private static void parseHeader(String header, TaskHttpRequest req) {
        String[] parts = header.split(":", 2);
        req.getHeaders().put(parts[0].trim(), parts[1].trim());
    }

    /**
     * Tokenize while respecting quotes
     */
    private static List<String> tokenize(String curl) {

        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(curl);
        while (m.find()) {
            tokens.add(m.group(1).replace("\"", "").replace("'", ""));
        }
        return tokens;

    }

}
