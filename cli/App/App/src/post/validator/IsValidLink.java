package post.validator;

import java.net.URI;
import java.net.URISyntaxException;

public class IsValidLink implements Validator<String> {

    @Override
    public boolean isValid(String link) {
        if (link == null || link.isBlank() || link.contains(" ")) {
            return false;
        }

        String candidate = link.startsWith("www.") ? "https://" + link : link;

        try {
            URI uri = new URI(candidate);
            if (!("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))) {
                return false;
            }

            String host = extractHost(uri);
            int lastDotIndex = host.lastIndexOf('.');
            if (lastDotIndex <= 0 || lastDotIndex == host.length() - 1) {
                return false;
            }

            String topLevelDomain = host.substring(lastDotIndex + 1);
            return topLevelDomain.length() >= 2
                    && topLevelDomain.chars().allMatch(Character::isLetter);
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    private String extractHost(URI uri) {
        if (uri.getHost() != null) {
            return uri.getHost();
        }

        String authority = uri.getRawAuthority();
        if (authority == null || authority.isBlank() || authority.contains("@")) {
            return "";
        }

        int portSeparator = authority.lastIndexOf(':');
        if (portSeparator >= 0) {
            String port = authority.substring(portSeparator + 1);
            if (port.isEmpty() || !port.chars().allMatch(Character::isDigit)) {
                return "";
            }
            try {
                int portNumber = Integer.parseInt(port);
                if (portNumber > 65535) {
                    return "";
                }
            } catch (NumberFormatException exception) {
                return "";
            }
            authority = authority.substring(0, portSeparator);
        }
        return authority;
    }
}
