package util;

/**
 * Normalizes subreddit names to the {@code r/name} form expected by the Spring API.
 */
public final class SubredditNames {
    private SubredditNames() {
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (trimmed.regionMatches(true, 0, "r/", 0, 2)) {
            return "r/" + trimmed.substring(2);
        }
        return "r/" + trimmed;
    }

    public static String stripPrefix(String name) {
        String normalized = normalize(name);
        return normalized.startsWith("r/") ? normalized.substring(2) : normalized;
    }
}
