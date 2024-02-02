package potenday.app.domain.cat.commentlikes;

import org.springframework.stereotype.Service;
import potenday.app.domain.auth.AppUser;
import potenday.app.domain.cat.comment.CatComment;
import potenday.app.domain.cat.comment.CatCommentRepository;
import potenday.app.domain.user.User;
import potenday.app.domain.user.UserRepository;
import potenday.app.global.error.ErrorCode;
import potenday.app.global.error.PotendayException;

@Service
public class CatCommentLikeService {

  private final CatCommentLikeRepository catCommentLikeRepository;
  private final CatCommentRepository catCommentRepository;
  private final UserRepository userRepository;

  public CatCommentLikeService(CatCommentLikeRepository catCommentLikeRepository,
      CatCommentRepository catCommentRepository,
      UserRepository userRepository) {
    this.catCommentLikeRepository = catCommentLikeRepository;
    this.catCommentRepository = catCommentRepository;
    this.userRepository = userRepository;
  }

  public void addCommentLike(AppUser appUser, AddCatCommentLike addCatCommentLike) {
    User user = findUser(appUser);
    CatComment catComment = findComment(addCatCommentLike);

    CatCommentLikeId commentLikeId = createCommentId(user.getId(), catComment.getId());
    if (alreadyLikedComment(commentLikeId)) {
      throw new PotendayException(ErrorCode.C009);
    }

    CatCommentLike catCommentLike = new CatCommentLike(commentLikeId);
    saveLike(catCommentLike);
  }

  private void saveLike(CatCommentLike catCommentLike) {
    catCommentLikeRepository.save(catCommentLike);
  }

  private CatComment findComment(AddCatCommentLike addCatCommentLike) {
    return catCommentRepository.findById(addCatCommentLike.commentId())
        .orElseThrow(() -> new PotendayException(ErrorCode.C006));
  }

  private static CatCommentLikeId createCommentId(long userId, long commentId) {
    return new CatCommentLikeId(userId, commentId);
  }

  private boolean alreadyLikedComment(CatCommentLikeId catCommentLikeId) {
    return catCommentLikeRepository.existsByCatCommentLikeId(catCommentLikeId);
  }

  private User findUser(AppUser appUser) {
    User user = userRepository.findById(appUser.id())
        .orElseThrow(() -> new PotendayException(ErrorCode.A004));
    user.authorizationCheck();
    return user;
  }
}
