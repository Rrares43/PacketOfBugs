package bootstrap;

import post.repository.PostMenu;
import post.repository.PostRepo;
import post.service.CommentService;
import post.service.CommentServiceImpl;
import post.service.CommentVoteService;
import post.service.CommentVoteServiceImpl;
import post.service.PostDeleteService;
import post.service.PostDeleteServiceImpl;
import post.service.PostEditServiceImpl;
import post.service.PostVoteService;
import post.service.PostVoteServiceImpl;
import logger.Logger;
import io.IntReader;
import io.OutputWriter;
import post.PostInteractionController;
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
                           PostRepo postRepo,
                           Logger logger) {
        PostVoteService postVoteService = new PostVoteServiceImpl(postRepo, logger);
        PostEditServiceImpl postEditService = new PostEditServiceImpl(postRepo);
        PostDeleteService postDeleteService = new PostDeleteServiceImpl(postRepo);
        CommentService commentService = new CommentServiceImpl(postRepo, logger);
        CommentVoteService commentVoteService = new CommentVoteServiceImpl(postRepo, logger);

        PostInteractionController interactionController = new PostInteractionController(
                stringReader, intReader, output, postView, commentService, commentVoteService, postRepo
        );

        CommentActionCommand upvoteComm = new UpvoteCommentCommand(commentVoteService, stringReader, output);
        CommentActionCommand downvoteComm = new DownvoteCommentCommand(commentVoteService, stringReader, output);

        interactionController.registerPostCommand("1", new VoteCommand(postVoteService, intReader, output, true));
        interactionController.registerPostCommand("2", new VoteCommand(postVoteService, intReader, output, false));
        interactionController.registerPostCommand("3", new AddCommentCommand(commentService, stringReader, output));
        interactionController.registerPostCommand("4", new EditPostCommand(stringReader, postEditService, postRepo));
        interactionController.registerPostCommand("5", new DeletePostCommand(postDeleteService, output));
        interactionController.registerPostCommand("6", interactionController::manageCommentInteraction);

        interactionController.registerCommentCommand("1", upvoteComm);
        interactionController.registerCommentCommand("2", downvoteComm);
        interactionController.registerCommentCommand("3", new ReplyCommentCommand(commentService, stringReader, output));
        interactionController.registerCommentCommand("4", new EditCommentCommand(commentService, stringReader, output));
        interactionController.registerCommentCommand("5", new DeleteCommentCommand(commentService, output));

        return new PostMenu(interactionController);
    }
}
