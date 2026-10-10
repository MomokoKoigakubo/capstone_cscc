package cscc.edu.feedback;

import cscc.edu.feedback.model.Customer;
import cscc.edu.feedback.model.FeedbackLog;
import cscc.edu.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

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
        Customer customer = new Customer(null,
                "Ruth", "bob", "ruthbob@gmail.com");
        ID = repository.saveCustomer(customer);
        FeedbackLog feedback = new FeedbackLog(null,
                "I really liked the hotel", null, ID);
        repository.saveFeedback(feedback);
    }

    @Test
    void returnAllFeedbackRows() {
        Integer CustomerID = null;
        Customer customer2 = new Customer(null,
                "Jane", "Roe", "JaneRoe@gmail.com");
        CustomerID = repository.saveCustomer(customer2);
        FeedbackLog feedback = new FeedbackLog(null,
                "The showers were cold", null, CustomerID);
        repository.saveFeedback(feedback);
        FeedbackLog feedback2 = new FeedbackLog(null,
                "The breakfast bar had solid bagels", null, CustomerID);
        repository.saveFeedback(feedback2);

        assertTrue(repository.returnAllFeedbackRows().size() >= 2);
    }

    @Test
    void returnAllCustomerRows() {
        Customer customer1 = new Customer(null,
                "Mimi", "Salgado", "MimiS@gmail.com");
        repository.saveCustomer(customer1);
        Customer customer2 = new Customer(null,
                "Razz", "Graham", "RazzG@gmail.com");
        repository.saveCustomer(customer2);
        assertTrue(repository.returnAllCustomerRows().size() >= 2);
    }

	@Test
	void contextLoads() {
	}

}
