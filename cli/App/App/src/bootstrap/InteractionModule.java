package bootstrap;

import account.SessionService;
import api.CommentClient;
import api.PostClient;
import io.IntReader;
import io.OutputWriter;
import post.PostInteractionController;
import post.PostMenu;
import post.PostView;
import io.StringReader;
import post.command.AddCommentCommand;
import post.command.CommentActionCommand;
import post.command.DeleteCommentCommand;
import post.command.DeletePostCommand;
import post.command.DownvoteCommentCommand;
import post.command.EditCommentCommand;
import post.command.EditPostCommand;
import post.command.ReplyCommentCommand;
import post.command.UpvoteCommentCommand;
import post.command.VoteCommand;

final class InteractionModule {

    static PostMenu create(StringReader stringReader,
                           IntReader intReader,
                           OutputWriter output,
                           PostView postView,
                           SessionService sessionService,
                           PostClient postClient) {
        CommentClient commentClient = new CommentClient(sessionService);

        PostInteractionController interactionController = new PostInteractionController(
                stringReader, intReader, output, postView, postClient
        );

        CommentActionCommand upvoteComm = new UpvoteCommentCommand(commentClient, stringReader, output);
        CommentActionCommand downvoteComm = new DownvoteCommentCommand(commentClient, stringReader, output);

        interactionController.registerPostCommand("1", new VoteCommand(postClient, intReader, output, true));
        interactionController.registerPostCommand("2", new VoteCommand(postClient, intReader, output, false));
        interactionController.registerPostCommand("3", new AddCommentCommand(commentClient, stringReader, output));
        interactionController.registerPostCommand("4", new EditPostCommand(stringReader, postClient));
        interactionController.registerPostCommand("5", new DeletePostCommand(postClient, output));
        interactionController.registerPostCommand("6", interactionController::manageCommentInteraction);

        interactionController.registerCommentCommand("1", upvoteComm);
        interactionController.registerCommentCommand("2", downvoteComm);
        interactionController.registerCommentCommand("3", new ReplyCommentCommand(commentClient, stringReader, output));
        interactionController.registerCommentCommand("4", new EditCommentCommand(commentClient, stringReader, output));
        interactionController.registerCommentCommand("5", new DeleteCommentCommand(commentClient, output));

        return new PostMenu(interactionController);
    }
}
