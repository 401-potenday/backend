package potenday.app.domain.cat.commentlikes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatCommentLikeId implements Serializable {

  @Column(name = "cat_comment_id", nullable = false, updatable = false, insertable = false)
  private long catCommentId;

  @Column(name = "user_id", nullable = false, updatable = false, insertable = false)
  private long userId;

  public CatCommentLikeId(final long userId, final long catCommentId) {
    this.userId = userId;
    this.catCommentId = catCommentId;
  }
}