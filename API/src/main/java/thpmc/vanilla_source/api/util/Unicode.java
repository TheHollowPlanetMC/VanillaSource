package thpmc.vanilla_source.api.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unicode {

    private static final Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

    public static String replace(String source) {
        Matcher matcher = pattern.matcher(source);

        String replacement = source;

        if (matcher.find()) {
            for (int i = 1; i < matcher.groupCount(); i++) {
                String group = matcher.group(i);
                int[] codePoints = new int[1];
                codePoints[0] = Integer.parseInt(group, 16);

                replacement = replacement.replace("\\\\u" + group, new String(codePoints, 0, 1));
            }
        }

        return replacement;
    }

}
