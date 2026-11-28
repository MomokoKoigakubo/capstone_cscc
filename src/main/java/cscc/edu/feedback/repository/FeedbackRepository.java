package cscc.edu.feedback.repository;

import cscc.edu.feedback.model.Customer;
import cscc.edu.feedback.model.FeedbackLog;
import cscc.edu.feedback.model.SortOrder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

        String sql = "INSERT INTO dbo.feedbacklog (feedbackContent, rating, customerID) values (" +
                " :feedbackContent, :rating ,:customerID) ";
        this.jdbcClient.sql(sql)
                .param("feedbackContent",  feedbackLog.feedbackContent())
                .param("rating", feedbackLog.rating())
                .param("customerID", feedbackLog.customerID())
                .update(keyHolder, "feedbackID");
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public List<FeedbackLog> returnAllFeedbackRows() {
        String sql = "SELECT * FROM dbo.feedbacklog";
        return jdbcClient.sql(sql).query(FeedbackLog.class).list();
    }

    /*
    * Every filter is optional. The WHERE clause is only ever built from the fixed
    * fragments below and every value the owner typed goes in as a bound parameter, so
    * the text of a search can never become part of the SQL itself.
    *
    * Dates: submitted is a datetime2, so "to = 2026-11-18" can't be compared as
    * submitted <= '2026-11-18', that means midnight and drops everything sent later that
    * day. The range is start of the from day up to (not including) the start of the day
    * after to, which catches every time stamp inside the days the owner picked.
    * */
    public List<FeedbackLog> searchFeedback(String keyword, LocalDate from, LocalDate to, SortOrder sort) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.feedbackID, f.feedbackContent, f.rating, f.submitted, f.customerID " +
                "FROM dbo.feedbacklog f LEFT JOIN dbo.customers c ON c.customerID = f.customerID " +
                "WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (f.feedbackContent LIKE :pattern ESCAPE '\\'" +
                       " OR c.firstName LIKE :pattern ESCAPE '\\'" +
                       " OR c.lastName LIKE :pattern ESCAPE '\\'" +
                       " OR c.email LIKE :pattern ESCAPE '\\')");
            params.put("pattern", "%" + escapeLike(keyword.trim()) + "%");
        }
        if (from != null) {
            sql.append(" AND f.submitted >= :fromStart");
            params.put("fromStart", from.atStartOfDay());
        }
        if (to != null) {
            sql.append(" AND f.submitted < :toEnd");
            params.put("toEnd", to.plusDays(1).atStartOfDay());
        }
        //orderBy() comes from the enum, not the request
        sql.append(" ORDER BY ").append(sort.orderBy());

        return jdbcClient.sql(sql.toString()).params(params).query(FeedbackLog.class).list();
    }

    /*
    * Parameters stop injection, but inside LIKE the characters % _ and [ are still
    * wildcards. Escaping them means a search for "100%" looks for that text and does
    * not turn into "100 followed by anything".
    * */
    static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
    }

    // Data security this end point needs protected or we remove the function from public app
    public List<Customer> returnAllCustomerRows() {
        String sql = "SELECT * FROM dbo.customers";
        return jdbcClient.sql(sql).query(Customer.class).list();
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        String sql = "SELECT * FROM dbo.customers WHERE email = :email";
        return this.jdbcClient.sql(sql).param("email", email).query(Customer.class).optional();
    }

    public void updateCustomerName(Integer customerID, String firstName, String lastName) {
        String sql = "UPDATE dbo.customers SET firstName = :firstName, lastName = :lastName WHERE customerID = :customerID";
        this.jdbcClient.sql(sql).param("customerID", customerID).param("firstName", firstName).param( "lastName", lastName).update();
    }
}
