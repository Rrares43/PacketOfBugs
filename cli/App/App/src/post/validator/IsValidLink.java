package post.validator;

public class IsValidLink implements Validator<String>{

    @Override
    public boolean isValid(String link) {

        if (link == null || link.isBlank() || link.contains(" ")) {
            return false;
        }

        String firstPart;
        if(link.startsWith("www.")){
            firstPart = link.substring(4);
        }
        else if(link.startsWith("http://")){
            firstPart = link.substring(7);
        }
        else if(link.startsWith("https://")){
            firstPart = link.substring(8);
        }
        else{
            return false;
        }

        int lastDotIndex = firstPart.lastIndexOf(".");
        if(lastDotIndex <= 0 || lastDotIndex == firstPart.length() - 1){
            return false;
        }

        String extension = firstPart.substring(lastDotIndex + 1);
        if(extension.length() < 2){
            return false;
        }

        for(int i = 0; i < extension.length(); i++){
            if(!Character.isLetter(extension.charAt(i))){
                return false;
            }
        }

        return true;
    }
}
