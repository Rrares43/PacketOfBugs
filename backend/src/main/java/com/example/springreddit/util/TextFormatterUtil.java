package com.example.springreddit.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatterUtil {

    public static String formatText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 1. Bold: /b{text}
        text = applyRegex(text, "/b\\{(.*?)\\}", "bold");

        // 2. Italic: /i{text}
        text = applyRegex(text, "/i\\{(.*?)\\}", "italic");

        // 3. Monospace: /m{text}
        text = applyRegex(text, "/m\\{(.*?)\\}", "mono");

        // 4. Strikethrough: /s{text}
        text = applyRegex(text, "/s\\{(.*?)\\}", "strike");

        // 5. Underline: /u{text}
        text = applyRegex(text, "/u\\{(.*?)\\}", "underline");

        // 6. Spoiler: /spoiler{text}
        text = applyRegex(text, "/spoiler\\{(.*?)\\}", "spoiler");

        return text;
    }

    private static String applyRegex(String text, String regex, String type) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String innerText = matcher.group(1);
            String replacement = switch (type) {
                case "bold" -> toBoldUnicode(innerText);
                case "italic" -> toItalicUnicode(innerText);
                case "mono" -> toMonospaceUnicode(innerText);
                case "strike" -> toStrikethrough(innerText);
                case "underline" -> toUnderline(innerText);
                case "spoiler" -> "[SPOILER: " + toUpsideDown(innerText) + "]";
                default -> innerText;
            };

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String toBoldUnicode(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.appendCodePoint(0x1D400 + (c - 'A'));
            } else if (c >= 'a' && c <= 'z') {
                sb.appendCodePoint(0x1D41A + (c - 'a'));
            } else if (c >= '0' && c <= '9') {
                sb.appendCodePoint(0x1D7CE + (c - '0'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String toItalicUnicode(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.appendCodePoint(0x1D434 + (c - 'A'));
            } else if (c >= 'a' && c <= 'z') {
                sb.appendCodePoint(0x1D44E + (c - 'a'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String toMonospaceUnicode(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.appendCodePoint(0x1D670 + (c - 'A'));
            } else if (c >= 'a' && c <= 'z') {
                sb.appendCodePoint(0x1D68A + (c - 'a'));
            } else if (c >= '0' && c <= '9') {
                sb.appendCodePoint(0x1D7F6 + (c - '0'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String toStrikethrough(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(c).append('\u0336');
        }
        return sb.toString();
    }

    private static String toUnderline(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(c).append('\u0332');
        }
        return sb.toString();
    }

    private static String toUpsideDown(String text) {
        String normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?.,";
        String upside  = "ɐqɔpǝɟƃɥᴉɾʞlɯuodbɹsʇnʌʍxʎz∀qƆpƎℲפHIſʞ˥WNOԀQᴚS┴∩ΛMX⅄Z0ƖᄅƐㄣϛ9ㄥ86¡¿'˙";

        StringBuilder sb = new StringBuilder();
        for (int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            int index = normal.indexOf(c);
            if (index != -1) {
                sb.append(upside.charAt(index));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}