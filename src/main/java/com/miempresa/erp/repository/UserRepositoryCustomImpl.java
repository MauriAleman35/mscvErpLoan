package com.miempresa.erp.repository;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> getBorrowerStatistics(Long borrowerId) {
        String sql =
            "WITH user_stats AS (" +
            "    SELECT" +
            "        u.id AS user_id," +
            "        COALESCE(u.adress_verified, FALSE) AS adress_verified," +
            "        COALESCE(u.identity_verified, FALSE) AS identity_verified" +
            "    FROM jhi_user u" +
            "    WHERE u.id = ?" +
            ")," +
            "" +
            "loan_stats AS (" +
            "    SELECT" +
            "        COUNT(l.id) AS loan_count," +
            "        COALESCE(SUM(COALESCE(l.late_payment_count, 0)), 0) AS late_payment_count," +
            "        COUNT(CASE WHEN l.current_status = 'al_dia' THEN 1 END) AS loans_al_dia" +
            "    FROM loan l" +
            "    INNER JOIN offer o ON l.id_offer = o.id" +
            "    INNER JOIN solicitude s ON o.id_solicitude = s.id" +
            "    WHERE s.borrower_id = ?" +
            ")," +
            "" +
            "payment_stats AS (" +
            "    SELECT" +
            "        COUNT(mp.id) AS total_payments," +
            "        COUNT(CASE WHEN mp.payment_status = 'completado' THEN 1 END) AS pagos_completados," +
            "        COALESCE(AVG(COALESCE(mp.days_late, 0)), 0) AS avg_days_late," +
            "        COALESCE(SUM(COALESCE(mp.penalty_amount, 0)), 0) AS total_penalty" +
            "    FROM monthly_payment mp" +
            "    INNER JOIN loan l ON mp.id_loan = l.id" +
            "    INNER JOIN offer o ON l.id_offer = o.id" +
            "    INNER JOIN solicitude s ON o.id_solicitude = s.id" +
            "    WHERE s.borrower_id = ?" +
            ")" +
            "" +
            "SELECT" +
            "    us.adress_verified::int as \"adressVerified\"," +
            "    us.identity_verified::int as \"identityVerified\"," +
            "    ls.loan_count as \"loanCount\"," +
            "    ls.late_payment_count as \"latePaymentCount\"," +
            "    ps.avg_days_late as \"avgDaysLate\"," +
            "    ps.total_penalty as \"totalPenalty\"," +
            "    CASE WHEN ps.total_payments > 0 THEN ps.pagos_completados::float / ps.total_payments ELSE 0 END as \"paymentCompletionRatio\"," +
            "    CASE WHEN COALESCE(ls.late_payment_count, 0) = 0 THEN 1 ELSE 0 END as \"hasNoLatePayments\"," +
            "    CASE WHEN ps.total_penalty > 0 THEN 1 ELSE 0 END as \"hasPenalty\"," +
            "    CASE WHEN ls.loan_count > 0 THEN ls.loans_al_dia::float / ls.loan_count ELSE 1 END as \"loansAlDiaRatio\"," +
            "    CASE WHEN ls.loan_count > 0 THEN ps.avg_days_late / ls.loan_count ELSE 0 END as \"daysLatePerLoan\"" +
            " FROM user_stats us" +
            " CROSS JOIN loan_stats ls" +
            " CROSS JOIN payment_stats ps";

        return jdbcTemplate.queryForMap(sql, borrowerId, borrowerId, borrowerId);
    }
}
