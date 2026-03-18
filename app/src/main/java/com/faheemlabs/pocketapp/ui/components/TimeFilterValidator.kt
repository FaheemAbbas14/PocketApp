package com.faheemlabs.pocketapp.ui.components

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility class to validate and debug time filter functionality
 * This helps cross-check that all time filters are working correctly
 */
object TimeFilterValidator {

    private const val TAG = "TimeFilterValidator"

    /**
     * Validates all filter periods and logs the results
     */
    fun validateAllFilters(): ValidationReport {
        val report = ValidationReport()
        val now = System.currentTimeMillis()

        // Test ALL filter
        report.allFilterTest = validateAllFilter()

        // Test WEEK filter
        report.weekFilterTest = validateWeekFilter(now)

        // Test MONTH filter
        report.monthFilterTest = validateMonthFilter(now)

        // Test YEAR filter
        report.yearFilterTest = validateYearFilter(now)

        // Test CUSTOM filter
        report.customFilterTest = validateCustomFilter()

        // Log complete report
        logReport(report)

        return report
    }

    private fun validateAllFilter(): FilterTest {
        val test = FilterTest("ALL Filter", FilterPeriod.ALL)

        try {
            val dateRange = calculateDateRange(FilterPeriod.ALL)
            test.success = dateRange == null
            test.message = if (test.success) {
                "✅ ALL filter correctly returns null (no filtering)"
            } else {
                "❌ ALL filter should return null but returned: $dateRange"
            }
        } catch (e: Exception) {
            test.success = false
            test.message = "❌ Error: ${e.message}"
        }

        return test
    }

    private fun validateWeekFilter(now: Long): FilterTest {
        val test = FilterTest("WEEK Filter", FilterPeriod.WEEK)

        try {
            val dateRange = calculateDateRange(FilterPeriod.WEEK)

            if (dateRange == null) {
                test.success = false
                test.message = "❌ WEEK filter returned null"
                return test
            }

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = now
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val expectedStart = calendar.timeInMillis

            // Check if the range is approximately 7 days
            val daysDiff = (dateRange.endMillis - dateRange.startMillis) / (1000 * 60 * 60 * 24)

            test.success = daysDiff in 6..7 // Allow some tolerance
            test.message = if (test.success) {
                "✅ WEEK filter: ${formatDate(dateRange.startMillis)} to ${formatDate(dateRange.endMillis)} ($daysDiff days)"
            } else {
                "❌ WEEK filter: Expected ~7 days but got $daysDiff days"
            }
            test.startDate = formatDateTime(dateRange.startMillis)
            test.endDate = formatDateTime(dateRange.endMillis)

        } catch (e: Exception) {
            test.success = false
            test.message = "❌ Error: ${e.message}"
        }

        return test
    }

    private fun validateMonthFilter(now: Long): FilterTest {
        val test = FilterTest("MONTH Filter", FilterPeriod.MONTH)

        try {
            val dateRange = calculateDateRange(FilterPeriod.MONTH)

            if (dateRange == null) {
                test.success = false
                test.message = "❌ MONTH filter returned null"
                return test
            }

            // Check if the range is approximately 28-31 days
            val daysDiff = (dateRange.endMillis - dateRange.startMillis) / (1000 * 60 * 60 * 24)

            test.success = daysDiff in 28..31
            test.message = if (test.success) {
                "✅ MONTH filter: ${formatDate(dateRange.startMillis)} to ${formatDate(dateRange.endMillis)} ($daysDiff days)"
            } else {
                "❌ MONTH filter: Expected 28-31 days but got $daysDiff days"
            }
            test.startDate = formatDateTime(dateRange.startMillis)
            test.endDate = formatDateTime(dateRange.endMillis)

        } catch (e: Exception) {
            test.success = false
            test.message = "❌ Error: ${e.message}"
        }

        return test
    }

    private fun validateYearFilter(now: Long): FilterTest {
        val test = FilterTest("YEAR Filter", FilterPeriod.YEAR)

        try {
            val dateRange = calculateDateRange(FilterPeriod.YEAR)

            if (dateRange == null) {
                test.success = false
                test.message = "❌ YEAR filter returned null"
                return test
            }

            // Check if the range is approximately 365 days
            val daysDiff = (dateRange.endMillis - dateRange.startMillis) / (1000 * 60 * 60 * 24)

            test.success = daysDiff in 364..366 // Allow for leap years
            test.message = if (test.success) {
                "✅ YEAR filter: ${formatDate(dateRange.startMillis)} to ${formatDate(dateRange.endMillis)} ($daysDiff days)"
            } else {
                "❌ YEAR filter: Expected ~365 days but got $daysDiff days"
            }
            test.startDate = formatDateTime(dateRange.startMillis)
            test.endDate = formatDateTime(dateRange.endMillis)

        } catch (e: Exception) {
            test.success = false
            test.message = "❌ Error: ${e.message}"
        }

        return test
    }

    private fun validateCustomFilter(): FilterTest {
        val test = FilterTest("CUSTOM Filter", FilterPeriod.CUSTOM)

        try {
            val dateRange = calculateDateRange(FilterPeriod.CUSTOM)
            test.success = dateRange == null
            test.message = if (test.success) {
                "✅ CUSTOM filter correctly returns null (requires user input)"
            } else {
                "❌ CUSTOM filter should return null but returned: $dateRange"
            }
        } catch (e: Exception) {
            test.success = false
            test.message = "❌ Error: ${e.message}"
        }

        return test
    }

    /**
     * Test the filterByDateRange function with sample data
     */
    fun testFilteringLogic(): FilteringLogicReport {
        val report = FilteringLogicReport()
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Create test items spanning different time periods
        data class TestItem(val id: Int, val name: String, val timestamp: Long)

        val items = mutableListOf<TestItem>()

        // Today
        items.add(TestItem(1, "Today", now))

        // 3 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -3)
        items.add(TestItem(2, "3 days ago", calendar.timeInMillis))

        // 10 days ago
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -10)
        items.add(TestItem(3, "10 days ago", calendar.timeInMillis))

        // 20 days ago
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -20)
        items.add(TestItem(4, "20 days ago", calendar.timeInMillis))

        // 40 days ago
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -40)
        items.add(TestItem(5, "40 days ago", calendar.timeInMillis))

        // 200 days ago
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -200)
        items.add(TestItem(6, "200 days ago", calendar.timeInMillis))

        // 400 days ago
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -400)
        items.add(TestItem(7, "400 days ago", calendar.timeInMillis))

        report.totalItems = items.size

        // Test ALL filter
        val allFiltered = filterByDateRange(items, null) { it.timestamp }
        report.allFilterCount = allFiltered.size
        report.allFilterSuccess = allFiltered.size == items.size

        // Test WEEK filter
        val weekRange = calculateDateRange(FilterPeriod.WEEK)
        val weekFiltered = filterByDateRange(items, weekRange) { it.timestamp }
        report.weekFilterCount = weekFiltered.size
        report.weekFilterSuccess = weekFiltered.size == 2 // Today and 3 days ago
        report.weekFilterItems = weekFiltered.map { it.name }

        // Test MONTH filter
        val monthRange = calculateDateRange(FilterPeriod.MONTH)
        val monthFiltered = filterByDateRange(items, monthRange) { it.timestamp }
        report.monthFilterCount = monthFiltered.size
        report.monthFilterSuccess = monthFiltered.size == 4 // Up to 20 days ago
        report.monthFilterItems = monthFiltered.map { it.name }

        // Test YEAR filter
        val yearRange = calculateDateRange(FilterPeriod.YEAR)
        val yearFiltered = filterByDateRange(items, yearRange) { it.timestamp }
        report.yearFilterCount = yearFiltered.size
        report.yearFilterSuccess = yearFiltered.size == 6 // Up to 200 days ago
        report.yearFilterItems = yearFiltered.map { it.name }

        logFilteringReport(report)

        return report
    }

    /**
     * Test boundary conditions for filters
     */
    fun testBoundaryConditions(): BoundaryTestReport {
        val report = BoundaryTestReport()

        // Test with item exactly at start boundary
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekStart = calendar.timeInMillis

        data class TestItem(val timestamp: Long)

        val itemAtBoundary = listOf(TestItem(weekStart))
        val weekRange = DateRange(weekStart, now)
        val filtered = filterByDateRange(itemAtBoundary, weekRange) { it.timestamp }

        report.startBoundaryIncluded = filtered.isNotEmpty()
        report.startBoundarySuccess = report.startBoundaryIncluded

        // Test with item exactly at end boundary
        val itemAtEnd = listOf(TestItem(now))
        val filteredEnd = filterByDateRange(itemAtEnd, weekRange) { it.timestamp }

        report.endBoundaryIncluded = filteredEnd.isNotEmpty()
        report.endBoundarySuccess = report.endBoundaryIncluded

        // Test with item just before start boundary
        val itemBeforeBoundary = listOf(TestItem(weekStart - 1))
        val filteredBefore = filterByDateRange(itemBeforeBoundary, weekRange) { it.timestamp }

        report.beforeBoundaryExcluded = filteredBefore.isEmpty()
        report.beforeBoundarySuccess = report.beforeBoundaryExcluded

        // Test with item just after end boundary
        val itemAfterBoundary = listOf(TestItem(now + 1))
        val filteredAfter = filterByDateRange(itemAfterBoundary, weekRange) { it.timestamp }

        report.afterBoundaryExcluded = filteredAfter.isEmpty()
        report.afterBoundarySuccess = report.afterBoundaryExcluded

        report.allBoundariesCorrect = report.startBoundarySuccess &&
                                      report.endBoundarySuccess &&
                                      report.beforeBoundarySuccess &&
                                      report.afterBoundarySuccess

        logBoundaryReport(report)

        return report
    }

    /**
     * Calculate date range for a given filter period
     * Copied from DateRangeFilter.kt for testing
     */
    private fun calculateDateRange(period: FilterPeriod): DateRange? {
        val calendar = Calendar.getInstance()
        val endMillis = calendar.timeInMillis

        return when (period) {
            FilterPeriod.ALL -> null
            FilterPeriod.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                DateRange(calendar.timeInMillis, endMillis)
            }
            FilterPeriod.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                DateRange(calendar.timeInMillis, endMillis)
            }
            FilterPeriod.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                DateRange(calendar.timeInMillis, endMillis)
            }
            FilterPeriod.CUSTOM -> null
        }
    }

    private fun formatDate(millis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    private fun formatDateTime(millis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    private fun logReport(report: ValidationReport) {
        Log.d(TAG, "=== TIME FILTER VALIDATION REPORT ===")
        Log.d(TAG, report.allFilterTest.message)
        Log.d(TAG, report.weekFilterTest.message)
        Log.d(TAG, report.monthFilterTest.message)
        Log.d(TAG, report.yearFilterTest.message)
        Log.d(TAG, report.customFilterTest.message)
        Log.d(TAG, "Overall: ${if (report.allTestsPassed) "✅ ALL TESTS PASSED" else "❌ SOME TESTS FAILED"}")
        Log.d(TAG, "=====================================")
    }

    private fun logFilteringReport(report: FilteringLogicReport) {
        Log.d(TAG, "=== FILTERING LOGIC TEST REPORT ===")
        Log.d(TAG, "Total test items: ${report.totalItems}")
        Log.d(TAG, "ALL filter: ${report.allFilterCount} items ${if (report.allFilterSuccess) "✅" else "❌"}")
        Log.d(TAG, "WEEK filter: ${report.weekFilterCount} items ${if (report.weekFilterSuccess) "✅" else "❌"}")
        Log.d(TAG, "  Items: ${report.weekFilterItems}")
        Log.d(TAG, "MONTH filter: ${report.monthFilterCount} items ${if (report.monthFilterSuccess) "✅" else "❌"}")
        Log.d(TAG, "  Items: ${report.monthFilterItems}")
        Log.d(TAG, "YEAR filter: ${report.yearFilterCount} items ${if (report.yearFilterSuccess) "✅" else "❌"}")
        Log.d(TAG, "  Items: ${report.yearFilterItems}")
        Log.d(TAG, "===================================")
    }

    private fun logBoundaryReport(report: BoundaryTestReport) {
        Log.d(TAG, "=== BOUNDARY CONDITIONS TEST REPORT ===")
        Log.d(TAG, "Start boundary included: ${if (report.startBoundaryIncluded) "✅" else "❌"}")
        Log.d(TAG, "End boundary included: ${if (report.endBoundaryIncluded) "✅" else "❌"}")
        Log.d(TAG, "Before boundary excluded: ${if (report.beforeBoundaryExcluded) "✅" else "❌"}")
        Log.d(TAG, "After boundary excluded: ${if (report.afterBoundaryExcluded) "✅" else "❌"}")
        Log.d(TAG, "All boundaries correct: ${if (report.allBoundariesCorrect) "✅ PASS" else "❌ FAIL"}")
        Log.d(TAG, "========================================")
    }

    // Data classes for reports
    data class FilterTest(
        val name: String,
        val period: FilterPeriod,
        var success: Boolean = false,
        var message: String = "",
        var startDate: String? = null,
        var endDate: String? = null
    )

    data class ValidationReport(
        var allFilterTest: FilterTest = FilterTest("", FilterPeriod.ALL),
        var weekFilterTest: FilterTest = FilterTest("", FilterPeriod.WEEK),
        var monthFilterTest: FilterTest = FilterTest("", FilterPeriod.MONTH),
        var yearFilterTest: FilterTest = FilterTest("", FilterPeriod.YEAR),
        var customFilterTest: FilterTest = FilterTest("", FilterPeriod.CUSTOM)
    ) {
        val allTestsPassed: Boolean
            get() = allFilterTest.success && weekFilterTest.success &&
                   monthFilterTest.success && yearFilterTest.success &&
                   customFilterTest.success
    }

    data class FilteringLogicReport(
        var totalItems: Int = 0,
        var allFilterCount: Int = 0,
        var allFilterSuccess: Boolean = false,
        var weekFilterCount: Int = 0,
        var weekFilterSuccess: Boolean = false,
        var weekFilterItems: List<String> = emptyList(),
        var monthFilterCount: Int = 0,
        var monthFilterSuccess: Boolean = false,
        var monthFilterItems: List<String> = emptyList(),
        var yearFilterCount: Int = 0,
        var yearFilterSuccess: Boolean = false,
        var yearFilterItems: List<String> = emptyList()
    )

    data class BoundaryTestReport(
        var startBoundaryIncluded: Boolean = false,
        var startBoundarySuccess: Boolean = false,
        var endBoundaryIncluded: Boolean = false,
        var endBoundarySuccess: Boolean = false,
        var beforeBoundaryExcluded: Boolean = false,
        var beforeBoundarySuccess: Boolean = false,
        var afterBoundaryExcluded: Boolean = false,
        var afterBoundarySuccess: Boolean = false,
        var allBoundariesCorrect: Boolean = false
    )
}

