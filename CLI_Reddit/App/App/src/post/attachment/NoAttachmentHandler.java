package post.attachment;

import io.OutputWriter;

public class NoAttachmentHandler implements AttachmentHandler {
    private final OutputWriter output;

    public NoAttachmentHandler(OutputWriter output) {
        this.output = output;
    }

    @Override
    public String handle() {
        output.write("Proceeding text-only post");
        return "";
    }
}