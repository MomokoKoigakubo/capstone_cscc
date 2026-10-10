package cscc.edu.feedback.controller;

import cscc.edu.feedback.model.FeedbackLog;
import cscc.edu.feedback.repository.FeedbackRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping
    List<FeedbackLog> getAllFeedback() {
        return feedbackRepository.returnAllFeedbackRows();
    }

    @PostMapping
    Integer saveFeedback(@RequestBody FeedbackLog feedbackLog) {
        return feedbackRepository.saveFeedback(feedbackLog);
    }
}
