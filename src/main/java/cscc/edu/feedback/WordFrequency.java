package cscc.edu.feedback;

import cscc.edu.feedback.model.FeedbackLog;
import cscc.edu.feedback.model.WordCount;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*
 * Counts the words across a set of feedback so the owner can see what customers keep
 * bringing up. Filler words are dropped or they would always win.
 * */
public final class WordFrequency {
    private static final int MinWordLength = 3;

    private static final Set<String> StopWords = Set.of(
            "the", "and", "for", "are", "but", "not", "you", "your", "all", "any", "can",
            "had", "has", "have", "her", "his", "him", "she", "they", "them", "their", "was",
            "were", "will", "with", "this", "that", "these", "those", "there", "then", "than",
            "what", "when", "where", "which", "who", "why", "how", "from", "into", "out",
            "our", "ours", "its", "it's", "too", "very", "just", "also", "been", "being",
            "did", "does", "doing", "would", "could", "should", "about", "again", "more",
            "most", "some", "such", "only", "own", "same", "over", "under", "off", "get",
            "got", "one", "really", "i'm", "i've", "don't", "didn't", "wasn't", "isn't"
    );

    private WordFrequency() {
    }

    public static List<WordCount> topWords(List<FeedbackLog> feedback, int limit) {
        Map<String, Long> counts = new HashMap<>();
        for (FeedbackLog row : feedback) {
            if (row.feedbackContent() == null) continue;
            //letters and apostrophes, so "don't" stays one word and punctuation falls away
            for (String token : row.feedbackContent().toLowerCase(Locale.ROOT).split("[^a-z']+")) {
                String word = token.replaceAll("^'+|'+$", "");
                if (word.length() < MinWordLength || StopWords.contains(word)) continue;
                counts.merge(word, 1L, Long::sum);
            }
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
                .toList();
    }
}
