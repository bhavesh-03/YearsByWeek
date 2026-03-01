 package com.example.yearbyweeks.data

import java.time.LocalDate

/**
 * Motivational quotes about time - changes daily/weekly
 */
object TimeQuotes {
    private val quotes = listOf(
        "Time is what we want most, but what we use worst." to "William Penn",
        "The two most powerful warriors are patience and time." to "Leo Tolstoy",
        "Lost time is never found again." to "Benjamin Franklin",
        "Time flies over us, but leaves its shadow behind." to "Nathaniel Hawthorne",
        "The key is not to prioritize what's on your schedule, but to schedule your priorities." to "Stephen Covey",
        "Don't count the days, make the days count." to "Muhammad Ali",
        "Time is the most valuable thing a man can spend." to "Theophrastus",
        "Yesterday is gone. Tomorrow has not yet come. We have only today." to "Mother Teresa",
        "Time is a created thing. To say 'I don't have time' is to say 'I don't want to.'" to "Lao Tzu",
        "The bad news is time flies. The good news is you're the pilot." to "Michael Altshuler",
        "Time is the wisest counselor of all." to "Pericles",
        "Every moment is a fresh beginning." to "T.S. Eliot",
        "Time and tide wait for no man." to "Geoffrey Chaucer",
        "Your time is limited, don't waste it living someone else's life." to "Steve Jobs",
        "The only time you really live fully is from thirty to sixty." to "Hervey Allen",
        "Time is money." to "Benjamin Franklin",
        "Forever is composed of nows." to "Emily Dickinson",
        "Time you enjoy wasting is not wasted time." to "Marthe Troly-Curtin",
        "Better three hours too soon than a minute too late." to "William Shakespeare",
        "The future is something which everyone reaches at the rate of sixty minutes an hour." to "C.S. Lewis",
        "Time brings all things to pass." to "Aeschylus",
        "One day or day one. You decide." to "Paulo Coelho",
        "Make each day your masterpiece." to "John Wooden",
        "Time waits for no one." to "Folklore",
        "Today is the first day of the rest of your life." to "Charles Dederich",
        "Seize the day, put very little trust in tomorrow." to "Horace",
        "Time is the coin of your life. Be careful lest you let other people spend it for you." to "Carl Sandburg",
        "A year from now you may wish you had started today." to "Karen Lamb",
        "The present time has one advantage over every other – it is our own." to "Charles Caleb Colton",
        "Time is the longest distance between two places." to "Tennessee Williams",
        "How we spend our days is how we spend our lives." to "Annie Dillard",
        "Time flies when you're having fun." to "Albert Einstein",
        "In time, even a bear can learn to dance." to "Yiddish Proverb",
        "Time is a great teacher, but unfortunately it kills all its pupils." to "Hector Berlioz",
        "Use time wisely. Today is the day you've been waiting for." to "Unknown",
        "The future starts today, not tomorrow." to "Pope John Paul II",
        "Time passes whether you act or not." to "Robin Sharma",
        "An inch of time is an inch of gold." to "Chinese Proverb",
        "The best time to plant a tree was 20 years ago. The second best time is now." to "Chinese Proverb",
        "Time is the school in which we learn." to "Joan Didion",
        "They always say time changes things, but you actually have to change them yourself." to "Andy Warhol",
        "Time is a gift. Spend it wisely." to "Unknown",
        "Live as if you were to die tomorrow. Learn as if you were to live forever." to "Mahatma Gandhi",
        "Time is an illusion. Lunchtime doubly so." to "Douglas Adams",
        "The only reason for time is so that everything doesn't happen at once." to "Albert Einstein",
        "Waste your money and you're only out of money, but waste your time and you've lost a part of your life." to "Michael LeBoeuf",
        "You may delay, but time will not." to "Benjamin Franklin",
        "Time is the scarcest resource." to "Peter Drucker",
        "Don't wait. The time will never be just right." to "Napoleon Hill",
        "The time is always right to do what is right." to "Martin Luther King Jr.",
        "Time well spent is life well lived." to "Unknown"
    )

    /**
     * Get a quote that changes daily based on the day of the year
     */
    fun getDailyQuote(dayOfYear: Int): Pair<String, String> {
        val index = dayOfYear % quotes.size
        return quotes[index]
    }

    /**
     * Get a quote that changes weekly based on the week of the year
     */
    fun getWeeklyQuote(weekOfYear: Int): Pair<String, String> {
        val index = weekOfYear % quotes.size
        return quotes[index]
    }

    /**
     * Get today's motivational quote
     */
    fun getTodayQuote(): Pair<String, String> {
        val dayOfYear = LocalDate.now().dayOfYear
        return getDailyQuote(dayOfYear)
    }
}

