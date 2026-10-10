package cscc.edu.feedback.controller;

import cscc.edu.feedback.model.Customer;
import cscc.edu.feedback.repository.FeedbackRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final FeedbackRepository feedbackRepository;

    public CustomerController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @PostMapping
    Integer saveCustomer(@RequestBody Customer customer) {
       Optional<Customer> exists = feedbackRepository.findCustomerByEmail(customer.email());
       if(exists.isPresent()) {
           Customer existingCustomer = exists.get();
           feedbackRepository.updateCustomerName(
                   existingCustomer.customerID(),
                   customer.firstName(),
                   customer.lastName());
           return existingCustomer.customerID();
       }

        return feedbackRepository.saveCustomer(customer);
    }
}
