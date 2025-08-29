package com.example.sqlwebhook;

import com.example.sqlwebhook.db.QueryExecutor;
import com.example.sqlwebhook.dto.WebhookResponse;
import com.example.sqlwebhook.service.WebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.Map;

@SpringBootApplication
public class SqlWebhookApplication {
    public static void main(String[] args) {
        SpringApplication.run(SqlWebhookApplication.class, args);
    }
}

/**
 * This runner fires on startup and performs:
 * 1) call generateWebhook
 * 2) run the SQL on H2
 * 3) send finalQuery to webhook with JWT
 */
@Component
class StartupRunner implements CommandLineRunner {

    private final WebhookService webhookService;
    private final QueryExecutor queryExecutor;

    // you can override these in application.properties or pass as env vars if you prefer
    @Value("${app.submitter.name:John Doe}")
    private String submitterName;

    @Value("${app.submitter.regNo:REG12347}")
    private String regNo;

    @Value("${app.submitter.email:john@example.com}")
    private String submitterEmail;

    public StartupRunner(WebhookService webhookService, QueryExecutor queryExecutor) {
        this.webhookService = webhookService;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting generateWebhook call...");
        WebhookResponse resp = webhookService.generateWebhook(submitterName, regNo, submitterEmail);
        if (resp == null || resp.getWebhook() == null || resp.getAccessToken() == null) {
            throw new IllegalStateException("Invalid response from generateWebhook");
        }

        System.out.println("Webhook URL: " + resp.getWebhook());
        System.out.println("Access Token: " + (resp.getAccessToken().length() > 20 ? resp.getAccessToken().substring(0,20) + "..." : resp.getAccessToken()));

        // final SQL: (keep exactly same text as in finalQuery variable)
        String finalQuery =
            "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, DATEDIFF('YEAR', e.DOB, CURRENT_DATE) AS AGE, d.DEPARTMENT_NAME AS DEPARTMENT_NAME " +
            "FROM PAYMENTS p " +
            "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
            "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
            "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
            "AND p.AMOUNT = (SELECT MAX(AMOUNT) FROM PAYMENTS WHERE DAY(PAYMENT_TIME) <> 1)";

        // execute the SQL locally on the H2 database (the schema and data are initialized from schema.sql/data.sql)
        System.out.println("Executing SQL on local H2 DB...");
        Map<String, Object> result = queryExecutor.runFinalQuery(finalQuery);
        System.out.println("Query result: " + result);

        // submit finalQuery to webhook URL
        System.out.println("Submitting finalQuery to webhook...");
        var postResp = webhookService.submitFinalQuery(resp.getWebhook(), resp.getAccessToken(), finalQuery);
        System.out.println("Submission response status: " + postResp.getStatusCode());
        System.out.println("Submission response body: " + postResp.getBody());
    }
}
