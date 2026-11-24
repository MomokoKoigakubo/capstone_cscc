package cscc.edu.feedback.controller;

import cscc.edu.feedback.WordFrequency;
import cscc.edu.feedback.model.FeedbackLog;
import cscc.edu.feedback.model.FeedbackSearchResult;
import cscc.edu.feedback.model.SortOrder;
import cscc.edu.feedback.repository.FeedbackRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/*
* @Get Mapping   = HTTP:Get    -> read data
* @PostMapping   = HTTP:POST   -> Create or submits new data
* @PutMapping    = HTTP:PUT    -> update data
* @DeleteMapping = HTTP:DELETE -> delete data
* */

//This tells spring to turn return values into JSON
@RestController
@RequestMapping("/api/feedback")//This is an endpoint
public class FeedbackController {
    private static final int MaxKeywordLength = 100;
    private static final int TopWordLimit = 15;

    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping
    List<FeedbackLog> getAllFeedback() {
        return feedbackRepository.returnAllFeedbackRows();
    }

    /*
    * GET /api/feedback/search?keyword=late&from=2026-11-01&to=2026-11-18&sort=newest
    * Every parameter is optional, with none of them this is every row newest first.
    * Dates only take the ISO yyyy-MM-dd form (what an <input type="date"> sends), anything
    * else is a 400 instead of a guess at what the owner meant.
    * */
    @GetMapping("/search")
    FeedbackSearchResult searchFeedback(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "newest") String sort) {

        if (keyword != null && keyword.length() > MaxKeywordLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyword is limited to " + MaxKeywordLength + " characters");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from date must be on or before the to date");
        }

        List<FeedbackLog> feedback = feedbackRepository.searchFeedback(keyword, from, to, parseSort(sort));
        return new FeedbackSearchResult(feedback, WordFrequency.topWords(feedback, TopWordLimit));
    }

    @PostMapping
    Integer saveFeedback(@Valid @RequestBody FeedbackLog feedbackLog) {
        return feedbackRepository.saveFeedback(feedbackLog);
    }

    private static SortOrder parseSort(String sort) {
        try {
            return SortOrder.valueOf(sort.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort must be newest, oldest, rating_low, or rating_high");
        }
    }
}
