package com.faheem.pocketapp

import com.faheem.pocketapp.ui.components.DateRange
import com.faheem.pocketapp.ui.components.FilterPeriod
import com.faheem.pocketapp.ui.components.filterByDateRange
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for DateRangeFilter functionality
 * This tests all time filter periods to ensure they work correctly
 */
class DateRangeFilterTest {

    data class TestItem(val id: Int, val timestamp: Long)

    @Test
    fun `test ALL filter returns all items`() {
        val items = createTestItems()
        val filtered = filterByDateRange(items, null) { it.timestamp }
        assertEquals(items.size, filtered.size)
        assertEquals(items, filtered)
    }

    @Test
    fun `test WEEK filter returns items from last 7 days`() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Create items with different timestamps
        calendar.add(Calendar.DAY_OF_YEAR, -5) // 5 days ago (should be included)
        val fiveDaysAgo = calendar.timeInMillis

        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -10) // 10 days ago (should be excluded)
        val tenDaysAgo = calendar.timeInMillis

        val items = listOf(
            TestItem(1, now),
            TestItem(2, fiveDaysAgo),
            TestItem(3, tenDaysAgo)
        )

        // Calculate week range
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekStartMillis = calendar.timeInMillis
        val dateRange = DateRange(weekStartMillis, now)

        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == 1 })
        assertTrue(filtered.any { it.id == 2 })
        assertFalse(filtered.any { it.id == 3 })
    }

    @Test
    fun `test MONTH filter returns items from last 30 days`() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Create items with different timestamps
        calendar.add(Calendar.DAY_OF_YEAR, -15) // 15 days ago (should be included)
        val fifteenDaysAgo = calendar.timeInMillis

        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -35) // 35 days ago (should be excluded)
        val thirtyFiveDaysAgo = calendar.timeInMillis

        val items = listOf(
            TestItem(1, now),
            TestItem(2, fifteenDaysAgo),
            TestItem(3, thirtyFiveDaysAgo)
        )

        // Calculate month range
        calendar.timeInMillis = now
        calendar.add(Calendar.MONTH, -1)
        val monthStartMillis = calendar.timeInMillis
        val dateRange = DateRange(monthStartMillis, now)

        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == 1 })
        assertTrue(filtered.any { it.id == 2 })
        assertFalse(filtered.any { it.id == 3 })
    }

    @Test
    fun `test YEAR filter returns items from last 365 days`() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Create items with different timestamps
        calendar.add(Calendar.MONTH, -6) // 6 months ago (should be included)
        val sixMonthsAgo = calendar.timeInMillis

        calendar.timeInMillis = now
        calendar.add(Calendar.YEAR, -2) // 2 years ago (should be excluded)
        val twoYearsAgo = calendar.timeInMillis

        val items = listOf(
            TestItem(1, now),
            TestItem(2, sixMonthsAgo),
            TestItem(3, twoYearsAgo)
        )

        // Calculate year range
        calendar.timeInMillis = now
        calendar.add(Calendar.YEAR, -1)
        val yearStartMillis = calendar.timeInMillis
        val dateRange = DateRange(yearStartMillis, now)

        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == 1 })
        assertTrue(filtered.any { it.id == 2 })
        assertFalse(filtered.any { it.id == 3 })
    }

    @Test
    fun `test CUSTOM filter with specific date range`() {
        val calendar = Calendar.getInstance()

        // Set custom range: Jan 1, 2024 to Feb 1, 2024
        calendar.set(2024, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis

        calendar.set(2024, Calendar.FEBRUARY, 1, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endMillis = calendar.timeInMillis

        // Create items with different timestamps
        calendar.set(2024, Calendar.JANUARY, 15, 12, 0, 0) // Inside range
        val insideRange = calendar.timeInMillis

        calendar.set(2023, Calendar.DECEMBER, 15, 12, 0, 0) // Before range
        val beforeRange = calendar.timeInMillis

        calendar.set(2024, Calendar.MARCH, 1, 12, 0, 0) // After range
        val afterRange = calendar.timeInMillis

        val items = listOf(
            TestItem(1, insideRange),
            TestItem(2, beforeRange),
            TestItem(3, afterRange)
        )

        val dateRange = DateRange(startMillis, endMillis)
        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertEquals(1, filtered.size)
        assertEquals(1, filtered[0].id)
    }

    @Test
    fun `test filter with boundary values`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis

        calendar.set(2024, Calendar.JANUARY, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endMillis = calendar.timeInMillis

        // Items exactly on boundaries
        val items = listOf(
            TestItem(1, startMillis), // Exact start
            TestItem(2, endMillis),    // Exact end
            TestItem(3, startMillis - 1), // Just before start
            TestItem(4, endMillis + 1)    // Just after end
        )

        val dateRange = DateRange(startMillis, endMillis)
        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == 1 })
        assertTrue(filtered.any { it.id == 2 })
        assertFalse(filtered.any { it.id == 3 })
        assertFalse(filtered.any { it.id == 4 })
    }

    @Test
    fun `test empty list returns empty result`() {
        val items = emptyList<TestItem>()
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val dateRange = DateRange(calendar.timeInMillis, now)

        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `test filter with all items outside range`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 1, 0, 0, 0)
        val startMillis = calendar.timeInMillis

        calendar.set(2024, Calendar.JANUARY, 31, 23, 59, 59)
        val endMillis = calendar.timeInMillis

        // All items outside the range
        calendar.set(2023, Calendar.DECEMBER, 1, 12, 0, 0)
        val beforeRange = calendar.timeInMillis

        calendar.set(2024, Calendar.MARCH, 1, 12, 0, 0)
        val afterRange = calendar.timeInMillis

        val items = listOf(
            TestItem(1, beforeRange),
            TestItem(2, afterRange)
        )

        val dateRange = DateRange(startMillis, endMillis)
        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `test filter preserves item order`() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val oneDayAgo = calendar.timeInMillis

        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgo = calendar.timeInMillis

        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -3)
        val threeDaysAgo = calendar.timeInMillis

        val items = listOf(
            TestItem(1, now),
            TestItem(2, oneDayAgo),
            TestItem(3, twoDaysAgo),
            TestItem(4, threeDaysAgo)
        )

        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val dateRange = DateRange(calendar.timeInMillis, now)

        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertEquals(4, filtered.size)
        assertEquals(1, filtered[0].id)
        assertEquals(2, filtered[1].id)
        assertEquals(3, filtered[2].id)
        assertEquals(4, filtered[3].id)
    }

    @Test
    fun `test FilterPeriod enum values`() {
        assertEquals("All Time", FilterPeriod.ALL.displayName)
        assertEquals("This Week", FilterPeriod.WEEK.displayName)
        assertEquals("This Month", FilterPeriod.MONTH.displayName)
        assertEquals("This Year", FilterPeriod.YEAR.displayName)
        assertEquals("Custom Range", FilterPeriod.CUSTOM.displayName)

        assertEquals("📅", FilterPeriod.ALL.emoji)
        assertEquals("📆", FilterPeriod.WEEK.emoji)
        assertEquals("🗓️", FilterPeriod.MONTH.emoji)
        assertEquals("📊", FilterPeriod.YEAR.emoji)
        assertEquals("🔍", FilterPeriod.CUSTOM.emoji)
    }

    @Test
    fun `test DateRange data class`() {
        val startMillis = 1000L
        val endMillis = 2000L
        val dateRange = DateRange(startMillis, endMillis)

        assertEquals(startMillis, dateRange.startMillis)
        assertEquals(endMillis, dateRange.endMillis)
    }

    @Test
    fun `test filter with current time boundary`() {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        // Item in the future
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.timeInMillis

        // Item in the past
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis

        val items = listOf(
            TestItem(1, now),
            TestItem(2, tomorrow),
            TestItem(3, yesterday)
        )

        // Filter from yesterday to now
        val dateRange = DateRange(yesterday, now)
        val filtered = filterByDateRange(items, dateRange) { it.timestamp }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == 1 })
        assertTrue(filtered.any { it.id == 3 })
        assertFalse(filtered.any { it.id == 2 })
    }

    private fun createTestItems(): List<TestItem> {
        val calendar = Calendar.getInstance()
        return listOf(
            TestItem(1, calendar.timeInMillis),
            TestItem(2, calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis),
            TestItem(3, calendar.apply { add(Calendar.DAY_OF_YEAR, -2) }.timeInMillis),
            TestItem(4, calendar.apply { add(Calendar.DAY_OF_YEAR, -5) }.timeInMillis)
        )
    }
}

