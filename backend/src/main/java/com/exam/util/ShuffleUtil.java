package com.exam.util;

import com.exam.entity.Question;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ShuffleUtil {

    public List<Question> shuffleQuestions(List<Question> questions) {
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    public String shuffleOptions(String options) {
        if (options == null || options.isEmpty()) {
            return options;
        }

        if (options.startsWith("{")) {
            try {
                Map<String, String> map = new LinkedHashMap<>();
                String[] pairs = options.replaceAll("[{}\"]", "").split(",");
                List<String> keys = new ArrayList<>();
                for (String pair : pairs) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        keys.add(kv[0].trim());
                        map.put(kv[0].trim(), kv[1].trim());
                    }
                }
                Collections.shuffle(keys);
                StringBuilder sb = new StringBuilder("{");
                for (int i = 0; i < keys.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(keys.get(i)).append("\":\"")
                      .append(map.get(keys.get(i))).append("\"");
                }
                sb.append("}");
                return sb.toString();
            } catch (Exception e) {
                return options;
            }
        }

        String[] optionArray = options.split("\\|");
        List<String> optionList = new ArrayList<>(Arrays.asList(optionArray));
        Collections.shuffle(optionList);
        return String.join("|", optionList);
    }
}
