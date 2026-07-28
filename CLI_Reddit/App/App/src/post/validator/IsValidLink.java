package post.validator;

public class IsValidLink implements Validator<String>{

    @Override
    public boolean isValid(String link) {

        if (link == null || link.isBlank()) {
            return false;
        }
        return !link.contains(" ")
                && (link.startsWith("www.") || link.startsWith("http://") || link.startsWith("https://"))
                && link.contains(".");
    }
}
