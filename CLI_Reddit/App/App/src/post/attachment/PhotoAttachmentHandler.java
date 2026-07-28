package post.attachment;

import io.OutputWriter;
import io.StringReader;
import post.validator.Validator;

public class PhotoAttachmentHandler implements AttachmentHandler {
    private final StringReader stringReader;
    private final OutputWriter output;
    private final Validator<String> notBlankValidator;

    public PhotoAttachmentHandler(StringReader stringReader, OutputWriter output, Validator<String> notBlankValidator) {
        this.stringReader = stringReader;
        this.output = output;
        this.notBlankValidator = notBlankValidator;
    }

    @Override
    public String handle() {
        while (true) {
            String photo = stringReader.readString("Enter the photo name:");
            if (notBlankValidator.isValid(photo)) return "\n[Image: " + photo + "]";
            output.write("Error: Photo name cannot be empty!");
        }
    }
}