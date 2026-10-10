package cscc.edu.feedback.controller;

import cscc.edu.feedback.model.Customer;
import cscc.edu.feedback.repository.FeedbackRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final FeedbackRepository feedbackRepository;

    public CustomerController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping
    List<Customer> getAllCustomers() {
        return feedbackRepository.returnAllCustomerRows();
    }

    @PostMapping
    Integer saveCustomer(@RequestBody Customer customer) {
        return feedbackRepository.saveCustomer(customer);
    }
}
