package com.cinewave.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin dashboard summary statistics.
 */
public class DashboardResponse {
    private long totalMovies;
    private long totalTheaters;
    private long totalUsers;
    private long totalShows;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private BigDecimal totalRevenue;
    private List<BookingResponse> recentBookings;

    public long getTotalMovies() { return totalMovies; }
    public void setTotalMovies(long totalMovies) { this.totalMovies = totalMovies; }
    public long getTotalTheaters() { return totalTheaters; }
    public void setTotalTheaters(long totalTheaters) { this.totalTheaters = totalTheaters; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalShows() { return totalShows; }
    public void setTotalShows(long totalShows) { this.totalShows = totalShows; }
    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(long confirmedBookings) { this.confirmedBookings = confirmedBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(long cancelledBookings) { this.cancelledBookings = cancelledBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public List<BookingResponse> getRecentBookings() { return recentBookings; }
    public void setRecentBookings(List<BookingResponse> recentBookings) { this.recentBookings = recentBookings; }
}
