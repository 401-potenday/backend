package potenday.app.api.content;

import jakarta.validation.Valid;
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
import potenday.app.domain.cat.AddCatContentService;
import potenday.app.query.model.content.CatContentDetails;
import potenday.app.query.model.content.CatContentSummariesResponse;
import potenday.app.query.repository.CoordinationCondition;
import potenday.app.query.service.ReadCatContentService;

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
  public ApiResponse<CatContentDetails> getCatContent(
      @OptionalAuthenticationPrincipal AppUser appUser,
      @PathVariable long contentId) {
    if (appUser == null) {
      return ApiResponse.success(readCatContentService.fetchContentDetails(contentId));
    }
    System.out.println(appUser.id());
    return ApiResponse.success(readCatContentService.fetchContentDetails(contentId));
  }

  @GetMapping("/contents")
  public ApiResponse<CatContentSummariesResponse> getCatContents(
      @OptionalAuthenticationPrincipal AppUser appUser,
      @RequestParam(name = "lat") Double centerLat,
      @RequestParam(name = "lon") Double centerLon,
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
    CatContentSummariesResponse contentSummariesResponse = readCatContentService.findContentsWithSearchCondition(
        appUser, searchCondition, PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize()));
    return ApiResponse.success(contentSummariesResponse);
  }
}