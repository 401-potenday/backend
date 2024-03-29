package potenday.app.domain.cat.comment;

import java.util.List;
import org.springframework.stereotype.Service;
import potenday.app.domain.auth.AppUser;
import potenday.app.domain.cat.content.CatContentRepository;
import potenday.app.domain.user.User;
import potenday.app.domain.user.UserRepository;
import potenday.app.event.action.CommentAddEvent;
import potenday.app.event.publisher.CommentEventPublisher;
import potenday.app.global.error.ErrorCode;
import potenday.app.global.error.PotendayException;

@Service
public class CatCommentService {

  private final CatCommentRepository catCommentRepository;
  private final CatCommentImageRepository catCommentImageRepository;
  private final CatContentRepository catContentRepository;
  private final UserRepository userRepository;
  private final CommentEventPublisher commentEventPublisher;

  public CatCommentService(CatCommentRepository catCommentRepository,
      CatCommentImageRepository catCommentImageRepository,
      CatContentRepository catContentRepository, UserRepository userRepository,
      CommentEventPublisher commentEventPublisher) {
    this.catCommentRepository = catCommentRepository;
    this.catCommentImageRepository = catCommentImageRepository;
    this.catContentRepository = catContentRepository;
    this.userRepository = userRepository;
    this.commentEventPublisher = commentEventPublisher;
  }

  public long addComment(AppUser appUser, AddCatComment addCatComment, AddCatCommentImages addCatCommentImages) {
    User user = findUser(appUser);
    checkExisted(addCatComment.contentId());
    CatComment catComment = catCommentRepository.save(createCatComment(user, addCatComment));
    catCommentImageRepository.saveAllAndFlush(createCommentImages(addCatComment.contentId(), catComment.getId(), user, addCatCommentImages));
    commentEventPublisher.publishEvent(new CommentAddEvent(catComment.getCatContentId()));
    return catComment.getId();
  }

  private CatComment createCatComment(User user, AddCatComment addCatComment) {
    return addCatComment.toCommentWithOwner(user);
  }

  private void checkExisted(Long contentId) {
    if (!catContentRepository.existsById(contentId)) {
      throw new PotendayException(ErrorCode.C004);
    }
  }

  private User findUser(AppUser appUser) {
    User user = userRepository.findById(appUser.id())
        .orElseThrow(() -> new PotendayException(ErrorCode.A001));
    user.authorizationCheck();
    return user;
  }

  private List<CatCommentImage> createCommentImages(
      Long contentId,
      long commentId,
      User user,
      AddCatCommentImages addCatCommentImages
  ) {
    CatCommentImages commentImages = addCatCommentImages.toContentImages();
    return commentImages.toTargetImages(contentId, commentId, user);
  }
}
