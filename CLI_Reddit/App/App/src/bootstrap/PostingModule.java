package bootstrap;

import account.SessionService;
import post.repository.PostRepo;
import post.service.PostService;
import post.service.PostServiceImpl;
import io.OutputWriter;
import post.PostView;
import io.StringReader;
import post.attachment.AttachmentHandler;
import post.attachment.LinkAttachmentHandler;
import post.attachment.NoAttachmentHandler;
import post.attachment.PhotoAttachmentHandler;
import post.command.CreatePostCommand;
import post.validator.IsNotBlank;
import post.validator.IsValidLength;
import post.validator.IsValidLink;
import post.validator.Validator;

import java.util.HashMap;
import java.util.Map;

final class PostingModule {
    private PostingModule() {
    }

    static PostView createPostView(StringReader stringReader, OutputWriter output) {
        Validator<String> notBlankValidator = new IsNotBlank();
        Validator<String> linkValidator = new IsValidLink();
        Validator<String> titleLengthValidator = new IsValidLength(300);
        Validator<String> contentLengthValidator = new IsValidLength(3000);

        Map<String, AttachmentHandler> attachmentHandlers = new HashMap<>();
        attachmentHandlers.put("photo", new PhotoAttachmentHandler(stringReader, output, notBlankValidator));
        attachmentHandlers.put("link", new LinkAttachmentHandler(stringReader, output, linkValidator));
        attachmentHandlers.put("no", new NoAttachmentHandler(output));

        return new PostView(
                stringReader,
                output,
                notBlankValidator,
                titleLengthValidator,
                contentLengthValidator,
                attachmentHandlers
        );
    }

    static CreatePostCommand createCreatePostCommand(PostView postView,
                                                     PostRepo postRepo,
                                                     SessionService sessionService) {
        PostService postService = new PostServiceImpl(postRepo);
        return new CreatePostCommand(postView, postService, sessionService);
    }
}
