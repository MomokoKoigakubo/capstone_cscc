package cscc.edu.feedback.repository;

import cscc.edu.feedback.model.Customer;
import cscc.edu.feedback.model.FeedbackLog;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class FeedbackRepository {

    private final JdbcClient jdbcClient;

    public FeedbackRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Integer saveCustomer(Customer customer) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = "INSERT INTO dbo.customers (firstName, lastName, email) values (:firstName, :lastName, :email) ";
        this.jdbcClient.sql(sql)
                .param("firstName", customer.firstName())
                .param("lastName",  customer.lastName())
                .param("email",     customer.email())
                .update(keyHolder, "customerID");
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public Integer saveFeedback(FeedbackLog feedbackLog) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = "INSERT INTO dbo.feedbacklog (feedbackContent, customerID) values (" +
                " :feedbackContent, :customerID) ";
        this.jdbcClient.sql(sql)
                .param("feedbackContent",  feedbackLog.feedbackContent())
                .param("customerID", feedbackLog.customerID())
                .update(keyHolder, "feedbackID");
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public List<FeedbackLog> returnAllFeedbackRows() {
        String sql = "SELECT * FROM dbo.feedbacklog";
        return jdbcClient.sql(sql).query(FeedbackLog.class).list();
    }

    // Data security this end point needs protected or we remove the function from public app
    public List<Customer> returnAllCustomerRows() {
        String sql = "SELECT * FROM dbo.customers";
        return jdbcClient.sql(sql).query(Customer.class).list();
    }
}
