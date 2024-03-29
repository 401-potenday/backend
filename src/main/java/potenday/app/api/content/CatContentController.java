package potenday.app.api.content;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import potenday.app.api.common.ApiResponse;
import potenday.app.api.content.search.ContentSearchCondition;
import potenday.app.api.content.search.CreateTimeOrder;
import potenday.app.api.content.search.DistanceOrder;
import potenday.app.domain.auth.AppUser;
import potenday.app.domain.auth.AuthenticationPrincipal;
import potenday.app.domain.auth.OptionalAuthenticationPrincipal;
import potenday.app.domain.cat.content.AddCatContentService;
import potenday.app.query.model.content.CatContentDetails;
import potenday.app.query.model.content.CatContentSummaries;
import potenday.app.query.repository.CoordinationCondition;
import potenday.app.query.service.ReadCatContentService;

@Slf4j
@RestController
public class CatContentController {

  private final AddCatContentService addCatContentService;
  private final ReadCatContentService readCatContentService;

  public CatContentController(AddCatContentService addCatContentService,
      ReadCatContentService readCatContentService) {
    this.addCatContentService = addCatContentService;
    this.readCatContentService = readCatContentService;
  }

  @PostMapping("/contents")
  public ApiResponse<AddCatContentResponse> addCatContent(
      @AuthenticationPrincipal AppUser appUser,
      @Valid @RequestBody AddCatContentRequest catContentRequest
  ) {
    long contentId = addCatContentService.addContent(
        appUser,
        catContentRequest.toAddCatContent(),
        catContentRequest.toAddCatImages()
    );
    return ApiResponse.success(new AddCatContentResponse(contentId));
  }

  @GetMapping("/contents/{contentId}")
  public ApiResponse<CatContentResponse> getCatContent(
      @OptionalAuthenticationPrincipal AppUser appUser,
      @PathVariable long contentId) {
    if (appUser == null) {
      CatContentDetails contentDetail = readCatContentService.findContent(contentId);
      return ApiResponse.success(CatContentResponse.from(contentDetail));
    }
    CatContentDetails content = readCatContentService.findContent(appUser, contentId);
    return ApiResponse.success(CatContentResponse.from(content));
  }

  @GetMapping("/contents")
  public ApiResponse<CatContentSummaries> getCatContents(
      @OptionalAuthenticationPrincipal AppUser appUser,
      @RequestParam(name = "lat") Double centerLat,
      @RequestParam(name = "lng") Double centerLon,
      @RequestParam(name = "follow", required = false, defaultValue = "false") Boolean follow,
      @RequestParam(name = "distance_order", required = false) DistanceOrder distanceOrder,
      @RequestParam(name = "range", required = false, defaultValue = "1000") Double range,
      @RequestParam(name = "created_order", required = false, defaultValue = "desc") CreateTimeOrder createTimeOrder,
      @PageableDefault(page = 1) Pageable pageable
  ) {
    ContentSearchCondition searchCondition = ContentSearchCondition.builder()
        .follow(follow)
        .distanceOrder(distanceOrder)
        .createTimeOrder(createTimeOrder)
        .coordinationCondition(CoordinationCondition.builder()
            .centerLat(centerLat)
            .centerLon(centerLon)
            .range(range)
            .build())
        .build();
    CatContentSummaries contentSummariesResponse = readCatContentService.findContentsWithSearchCondition(
        appUser,
        searchCondition,
        generatePageable(pageable)
    );
    return ApiResponse.success(contentSummariesResponse);
  }

  private PageRequest generatePageable(Pageable pageable) {
    return PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
  }
}
