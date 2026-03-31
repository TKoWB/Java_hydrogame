package com.hydrogame.database;

import com.hydrogame.hibernate_util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class RevenueService {

        // Get revenue grouped by day for a specific game (for chart)
        public static Map<String, BigDecimal> getDailyRevenueForGame(String gameId) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                YearMonth ym = YearMonth.now();
                LocalDateTime start = ym.atDay(1).atStartOfDay();
                LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
                Query<Object[]> q = session.createQuery(
                    "SELECT DAY(r.reciptDate), COALESCE(SUM(ri.amount * g.price), 0) " +
                    "FROM Recipt r JOIN ReciptItem ri ON r.reciptId = ri.reciptId " +
                    "JOIN Game g ON ri.gameId = g.gameId " +
                    "WHERE r.reciptDate >= :start AND r.reciptDate < :end AND g.gameId = :gameId " +
                    "GROUP BY DAY(r.reciptDate)",
                    Object[].class);
                q.setParameter("start", start);
                q.setParameter("end", end);
                q.setParameter("gameId", Integer.parseInt(gameId));
                List<Object[]> results = q.list();
                Map<String, BigDecimal> map = new HashMap<>();
                for (Object[] row : results) {
                    map.put(String.format("%02d", row[0]), (BigDecimal) row[1]);
                }
                return map;
            }
        }
    // Get total revenue for today
    public static BigDecimal getTodayRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.plusDays(1).atStartOfDay();
            Query<BigDecimal> q = session.createQuery(
                "SELECT COALESCE(SUM(r.totalPrice), 0) FROM Recipt r WHERE r.reciptDate >= :start AND r.reciptDate < :end",
                BigDecimal.class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            return q.uniqueResult();
        }
    }

    // Get total revenue for current month
    public static BigDecimal getMonthlyRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            YearMonth ym = YearMonth.now();
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
            Query<BigDecimal> q = session.createQuery(
                "SELECT COALESCE(SUM(r.totalPrice), 0) FROM Recipt r WHERE r.reciptDate >= :start AND r.reciptDate < :end",
                BigDecimal.class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            return q.uniqueResult();
        }
    }

    // Get total revenue for current year
    public static BigDecimal getYearlyRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int year = LocalDate.now().getYear();
            LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
            LocalDateTime end = LocalDate.of(year + 1, 1, 1).atStartOfDay();
            Query<BigDecimal> q = session.createQuery(
                "SELECT COALESCE(SUM(r.totalPrice), 0) FROM Recipt r WHERE r.reciptDate >= :start AND r.reciptDate < :end",
                BigDecimal.class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            return q.uniqueResult();
        }
    }

    // Get revenue grouped by day for current month (for chart)
    public static Map<String, BigDecimal> getDailyRevenueForCurrentMonth() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            YearMonth ym = YearMonth.now();
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
            Query<Object[]> q = session.createQuery(
                "SELECT DAY(r.reciptDate), COALESCE(SUM(r.totalPrice), 0) FROM Recipt r WHERE r.reciptDate >= :start AND r.reciptDate < :end GROUP BY DAY(r.reciptDate)",
                Object[].class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            List<Object[]> results = q.list();
            Map<String, BigDecimal> map = new HashMap<>();
            for (Object[] row : results) {
                map.put(String.format("%02d", row[0]), (BigDecimal) row[1]);
            }
            return map;
        }
    }

    // Get revenue grouped by month for current year (for chart)
    public static Map<String, BigDecimal> getMonthlyRevenueForCurrentYear() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int year = LocalDate.now().getYear();
            LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
            LocalDateTime end = LocalDate.of(year + 1, 1, 1).atStartOfDay();
            Query<Object[]> q = session.createQuery(
                "SELECT MONTH(r.reciptDate), COALESCE(SUM(r.totalPrice), 0) FROM Recipt r WHERE r.reciptDate >= :start AND r.reciptDate < :end GROUP BY MONTH(r.reciptDate)",
                Object[].class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            List<Object[]> results = q.list();
            Map<String, BigDecimal> map = new HashMap<>();
            for (Object[] row : results) {
                map.put(String.format("%02d", row[0]), (BigDecimal) row[1]);
            }
            return map;
        }
    }

    // Get revenue grouped by year (for chart, last 5 years)
    public static Map<String, BigDecimal> getYearlyRevenueForChart(int yearsBack) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int currentYear = LocalDate.now().getYear();
            int startYear = currentYear - yearsBack + 1;
            LocalDateTime start = LocalDate.of(startYear, 1, 1).atStartOfDay();
            LocalDateTime end = LocalDate.of(currentYear + 1, 1, 1).atStartOfDay();
            Query<Object[]> q = session.createQuery(
                "SELECT YEAR(r.reciptDate), COALESCE(SUM(r.totalPrice), 0) FROM Recipt r WHERE r.reciptDate >= :start AND r.reciptDate < :end GROUP BY YEAR(r.reciptDate)",
                Object[].class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            List<Object[]> results = q.list();
            Map<String, BigDecimal> map = new HashMap<>();
            for (Object[] row : results) {
                map.put(String.valueOf(row[0]), (BigDecimal) row[1]);
            }
            return map;
        }
    }
}
