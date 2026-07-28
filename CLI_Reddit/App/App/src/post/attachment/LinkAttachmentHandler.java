package post.attachment;

import io.OutputWriter;
import io.StringReader;
import post.validator.Validator;

public class LinkAttachmentHandler implements AttachmentHandler {
    private final StringReader stringReader;
    private final OutputWriter output;
    private final Validator<String> linkValidator;

    public LinkAttachmentHandler(StringReader stringReader, OutputWriter output, Validator<String> linkValidator) {
        this.stringReader = stringReader;
        this.output = output;
        this.linkValidator = linkValidator;
    }

    @Override
    public String handle() {
        while (true) {
            String link = stringReader.readString("Enter the link (e.g. www.google.com):");
            if (!linkValidator.isValid(link)) {
                output.write("Error: Invalid link!");
            } else {
                return "\n[Link: " + link + "]";
            }
        }
    }
}