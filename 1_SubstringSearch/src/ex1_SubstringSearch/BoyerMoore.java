package ex1_SubstringSearch;

/**
 *  The {@code BoyerMoore} class finds all occurrences of a pattern string
 *  in a text string.
 *  <p>
 *  This implementation uses the Boyer-Moore algorithm (with the bad-character
 *  rule, but not the strong good suffix rule).
 */
class BoyerMoore {
    private final int R;     // the radix
    private int[] right;     // the bad-character skip array
    private char[] pattern;  // store the pattern as a character array

    /**
     * Preprocesses the pattern string.
     *
     * @param pattern the pattern string
     */
    BoyerMoore(char[] pattern) {
        this.R = 127; // ASCII SIZE
        this.pattern = new char[pattern.length];
        System.arraycopy(pattern, 0, this.pattern, 0, pattern.length);

        // position of rightmost occurrence of c in the pattern
        right = new int[R];
        for (int c = 0; c < R; c++)
            right[c] = -1;
        for (int j = 0; j < pattern.length; j++)
            right[pattern[j]] = j;
    }

    /**
     * Returns the index of the first occurrence of the pattern string
     * in the text string starting at index offset.
     *
     * @param  text the text or sequence string
     * @param  offset the index of the sequence from which will be searched
     * @return the index of the first occurrence of the pattern string
     *         in the text string started at the offset; n if no such match
     */
    int search(char[] text, int offset) {
        int m = pattern.length;
        int n = text.length;
        int skip;
        for (int i = offset; i <= n - m; i += skip) {
            skip = 0;
            for (int j = m-1; j >= 0; j--) {
                if (pattern[j] != text[i+j]) {
                    skip = Math.max(1, j - right[text[i+j]]);
                    break;
                }
            }
            if (skip == 0) return i;    // found
        }
        return n;                       // not found
    }
}