package potenday.app.query.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potenday.app.global.error.ErrorCode;
import potenday.app.global.error.PotendayException;
import potenday.app.query.model.comment.CatCommentWithUserNicknameAndImages;
import potenday.app.query.repository.CatCommentQuery;
import potenday.app.query.repository.CatContentQuery;

@Service
public class ReadCatCommentService {

  private final CatCommentQuery catCommentQuery;
  private final CatContentQuery catContentQuery;

  public ReadCatCommentService(CatCommentQuery catCommentQuery, CatContentQuery catContentQuery) {
    this.catCommentQuery = catCommentQuery;
    this.catContentQuery = catContentQuery;
  }

  @Transactional(readOnly = true)
  public Page<CatCommentWithUserNicknameAndImages> findCatComments(Long contentId, Pageable pageable) {
    boolean existed = catContentQuery.existsByContentId(contentId);
    if (!existed) {
      throw new PotendayException(ErrorCode.C004);
    }
    return catCommentQuery.findAllCommentWithPaging(contentId, pageable);
  }
}
