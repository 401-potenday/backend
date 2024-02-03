package potenday.app.query.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potenday.app.api.content.CatContentEngagementSummary;
import potenday.app.api.content.search.ContentSearchCondition;
import potenday.app.domain.auth.AppUser;
import potenday.app.domain.cat.content.CatContent;
import potenday.app.domain.cat.content.CatContentImage;
import potenday.app.domain.cat.support.CatCommentEngagementsCalculator;
import potenday.app.domain.cat.support.CatContentFollowingCountCalculator;
import potenday.app.global.error.ErrorCode;
import potenday.app.global.error.PotendayException;
import potenday.app.query.model.content.CatContentDetails;
import potenday.app.query.model.content.CatContentSummariesResponse;
import potenday.app.query.model.user.UserNickname;
import potenday.app.query.repository.CatContentQuery;
import potenday.app.query.repository.UserQuery;

@Service
public class ReadCatContentService {

  private final CatContentQuery catContentQuery;
  private final UserQuery userQuery;
  private final CatCommentEngagementsCalculator catCommentEngagementsCalculator;
  private final CatContentFollowingCountCalculator catContentFollowingCountCalculator;

  public ReadCatContentService(CatContentQuery catContentQuery, UserQuery userQuery,
      CatCommentEngagementsCalculator catCommentEngagementsCalculator,
      CatContentFollowingCountCalculator catContentFollowingCountCalculator) {
    this.catContentQuery = catContentQuery;
    this.userQuery = userQuery;
    this.catCommentEngagementsCalculator = catCommentEngagementsCalculator;
    this.catContentFollowingCountCalculator = catContentFollowingCountCalculator;
  }

  @Transactional(readOnly = true)
  public CatContentDetails fetchContentDetails(long contentId) {
    // TODO 개선 요망. 매우 느립니다.
    CatContent catContent = catContentQuery.fetchContent(contentId);
    if (catContent == null) {
      throw new PotendayException(ErrorCode.C004);
    }
    List<CatContentImage> catContentImages = catContentQuery.fetchContentImages(contentId);
    UserNickname userNickname = userQuery.fetchUserSimpleInfo(catContent.getUserId());

    return CatContentDetails.of(
        catContent,
        catContentImages,
        userNickname,
        createEngagementSummary(contentId)
    );
  }

  private CatContentEngagementSummary createEngagementSummary(long contentId) {
    long countsFollowers = catContentFollowingCountCalculator.getFollowerCount(contentId);
    long countsComments = catCommentEngagementsCalculator.countCommentsByContentId(contentId);
    return new CatContentEngagementSummary(countsFollowers, countsComments);
  }

  @Transactional(readOnly = true)
  public CatContentSummariesResponse findContentsWithSearchCondition(AppUser appUser, ContentSearchCondition searchCondition, Pageable pageable) {
    Page<CatContent> catContents = catContentQuery.fetchContentsBySearchCondition(searchCondition, pageable);
    return CatContentSummariesResponse.of(catContents);
  }
}
