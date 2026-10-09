package cscc.edu.feedback;

import cscc.edu.feedback.model.Customer;
import cscc.edu.feedback.model.FeedbackLog;
import cscc.edu.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest
class FeedbackApplicationTests {

    @Autowired
    FeedbackRepository repository;

    @Test
    void saveCustomerReturnsId() {
        Customer customer = new Customer(null, "Gabriel",
                "Clifton", "gclifton@student.cscc.edu");
        Integer ID = repository.saveCustomer(customer);
        assertNotNull(ID);
    }

    @Test
    void saveFeedback() {
        Integer ID = null;
        Customer customer2 = new Customer(null,
                "Ruth", "bob", "ruthbob@gmail.com");
        ID = repository.saveCustomer(customer2);
        FeedbackLog feedback = new FeedbackLog(null,
                "I really liked the hotel", null, ID);
        repository.saveFeedback(feedback);
    }

	@Test
	void contextLoads() {
	}

}
