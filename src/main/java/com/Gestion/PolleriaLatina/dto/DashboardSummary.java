package com.Gestion.PolleriaLatina.dto;

import java.util.List;

public record DashboardSummary(
    Overview overview,
    List<MonthlyRevenuePoint> monthlyRevenue,
    List<StatusPoint> orderStatus,
    List<TopProduct> topProducts,
    List<RecentOrder> recentOrders,
    List<LowStockItem> lowStockItems,
    List<RecentIncident> recentIncidents) {

  public record Overview(
      String generatedAt,
      String currentMonth,
      String revenueToday,
      String revenueMonth,
      String revenueGrowth,
      long closedOrdersToday,
      long closedOrdersMonth,
      long activeOrders,
      long deliveryInRoute,
      long occupiedTables,
      long activeProducts,
      long lowStockItems,
      long pendingIncidents,
      long issuedReceipts,
      String averageTicket) {
  }

  public record MonthlyRevenuePoint(String month, double amount) {
  }

  public record StatusPoint(String status, long count) {
  }

  public record TopProduct(String name, long quantity, String total, double progress) {
  }

  public record RecentOrder(
      String client,
      String modality,
      String statusLabel,
      String statusClass,
      String date,
      String place,
      String total) {
  }

  public record LowStockItem(
      String name,
      String unit,
      double stockActual,
      double stockMinimo,
      String alertLabel,
      String badgeClass) {
  }

  public record RecentIncident(
      String title,
      String type,
      String severityLabel,
      String severityClass,
      String statusLabel,
      String statusClass,
      String date,
      String reportedBy) {
  }
}
