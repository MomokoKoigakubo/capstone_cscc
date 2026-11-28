package cscc.edu.feedback;

import cscc.edu.feedback.model.FeedbackLog;

import java.util.List;

/*
 * If a value is a comma, double quote, or new line, wrap the value in double quotes
 * Any " inside the value must be doubled to ""
 * otherwise return as given
 */
public class FeedbackCSV {
    static String escape(String value) {
        //Prevents formula CSV injection
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            value = "'" + value;
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return ("\"" + value + "\"");
        }
        return value;
    }

    public static String toCSV(List<FeedbackLog> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("feedbackID,submitted,rating,customerID,feedbackContent\r\n");
        for (FeedbackLog row : rows) {
            sb.append(row.feedbackID());
            sb.append(",");
            sb.append(row.submitted());
            sb.append(",");
            sb.append(row.rating());
            sb.append(",");
            sb.append(row.customerID());
            sb.append(",");
            sb.append(escape(row.feedbackContent()));
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
