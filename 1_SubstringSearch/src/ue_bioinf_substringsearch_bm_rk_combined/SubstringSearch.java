package ue_bioinf_substringsearch_bm_rk_combined;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SubstringSearch {
	private SubstringSearch() {
	}

	private class resultsTuple {
		int count;
		ArrayList<Integer> first10;

		resultsTuple(int count, ArrayList<Integer> first10) {
			this.count = count;
			this.first10 = first10;
		}
	}

	private String sequence;
	private char[] sequCharAr;
	private ArrayList<String> allPatternsList = new ArrayList<>();
	private HashMap<Integer, HashMap<Integer, String>> lengthPatternMap = new HashMap<>();
	private HashMap<String, resultsTuple> results = new HashMap<>();

	private void readPattern(String patternFile) {
		boolean isPattern = false;
		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(patternFile),
			StandardCharsets.ISO_8859_1))) {
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.startsWith(">")) {
					if (isPattern) {
						allPatternsList.add(stringBuilder.toString());
						addPatternToList(stringBuilder.toString());
						isPattern = false;
						stringBuilder = new StringBuilder();
					}
				} else {
					isPattern = true;
					stringBuilder.append(line);
				}
			}
			if (isPattern) {
				allPatternsList.add(stringBuilder.toString());
				addPatternToList(stringBuilder.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void addPatternToList(String pattern) {
		if (lengthPatternMap.containsKey(pattern.length())) {
			lengthPatternMap.get(pattern.length()).put(pattern.hashCode(), pattern);
		} else {
			HashMap<Integer, String> tmp = new HashMap<>();
			tmp.put(pattern.hashCode(), pattern);
			lengthPatternMap.put(pattern.length(), tmp);
		}
	}

	private void readSequence(String sequenceFile) {
		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sequenceFile),
			StandardCharsets.ISO_8859_1))) {
			String line;

			while ((line = reader.readLine()) != null) {
				if (!line.startsWith(">")) {
					stringBuilder.append(line);
				}
			}
			sequence = stringBuilder.toString();
			sequCharAr = sequence.toCharArray();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void searchRK(HashMap<Integer, String> patternsRK, int patternLength) {
		int seqHash, pos;
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < sequence.length() - patternLength; ++i) {
			pos = i + 1;
			for (int j = i; j < i + patternLength; ++j) {
				stringBuilder.append(sequence.charAt(j));
			}

			seqHash = stringBuilder.toString().hashCode();
			if (patternsRK.containsKey(seqHash)) {
				if (results.containsKey(patternsRK.get(seqHash))) {
					int cnt = results.get(patternsRK.get(seqHash)).count;
					ArrayList<Integer> first10 = results.get(patternsRK.get(seqHash)).first10;
					if (first10.size() < 10) {
						first10.add(pos);
					}
					results.put(patternsRK.get(seqHash), new resultsTuple(++cnt, first10));
				} else {
					ArrayList<Integer> first10 = new ArrayList<>();
					first10.add(pos);
					results.put(patternsRK.get(seqHash), new resultsTuple(1, first10));
				}
			}
			stringBuilder = new StringBuilder();
		}
	}

	/* thx to en.wikipedia.org */

	/**
	 * Returns the index within this string of the first occurrence of the
	 * specified substring. If it is not a substring, return -1.
	 *
	 * @param haystack The string to be scanned
	 * @param p        The target string to search
	 */
	private void boyerMoore(char[] haystack, String p) {
		char[] needle = p.toCharArray();
		int pos;
		int charTable[] = makeCharTable(needle);
		int offsetTable[] = makeOffsetTable(needle);
		for (int i = needle.length - 1, j; i < haystack.length; ) {
			for (j = needle.length - 1; needle[j] == haystack[i]; --i, --j) {
				if (j == 0) {
					pos = i + 1;
					if (results.containsKey(p)) {
						int cnt = results.get(p).count;
						ArrayList<Integer> first10 = results.get(p).first10;
						if (first10.size() < 10) {
							first10.add(pos);
						}
						results.put(p, new resultsTuple(++cnt, first10));
					} else {
						ArrayList<Integer> first10 = new ArrayList<>();
						first10.add(pos);
						results.put(p, new resultsTuple(1, first10));
					}
					break;
				}
			}
			i += Math.max(offsetTable[needle.length - 1 - j], charTable[haystack[i]]);
		}
	}

	/**
	 * Makes the jump table based on the mismatched character information.
	 */
	private static int[] makeCharTable(char[] needle) {
		final int ALPHABET_SIZE = 256;
		int[] table = new int[ALPHABET_SIZE];
		for (int i = 0; i < table.length; ++i) {
			table[i] = needle.length;
		}
		for (int i = 0; i < needle.length - 1; ++i) {
			table[needle[i]] = needle.length - 1 - i;
		}
		return table;
	}

	/**
	 * Makes the jump table based on the scan offset which mismatch occurs.
	 */
	private static int[] makeOffsetTable(char[] needle) {
		int[] table = new int[needle.length];
		int lastPrefixPosition = needle.length;
		for (int i = needle.length - 1; i >= 0; --i) {
			if (isPrefix(needle, i + 1)) {
				lastPrefixPosition = i + 1;
			}
			table[needle.length - 1 - i] = lastPrefixPosition - i + needle.length - 1;
		}
		for (int i = 0; i < needle.length - 1; ++i) {
			int slen = suffixLength(needle, i);
			table[slen] = needle.length - 1 - i + slen;
		}
		return table;
	}

	/**
	 * Is needle[p:end] a prefix of needle?
	 */
	private static boolean isPrefix(char[] needle, int p) {
		for (int i = p, j = 0; i < needle.length; ++i, ++j) {
			if (needle[i] != needle[j]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the maximum length of the substring ends at p and is a suffix.
	 */
	private static int suffixLength(char[] needle, int p) {
		int len = 0;
		for (int i = p, j = needle.length - 1;
		     i >= 0 && needle[i] == needle[j]; --i, --j) {
			len += 1;
		}
		return len;
	}
	/* ******** Boyer Moore ******** */

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("usage: java -jar SubstringSearch.jar <pattern.fasta> <sequence.fasta> ");
			System.exit(-1);
		}
		SubstringSearch ss = new SubstringSearch();
		System.out.println("Parsing pattern.fasta...");
		ss.readPattern(args[0]);
		System.out.println("Parsing sequence.fasta...");
		ss.readSequence(args[1]);
		System.out.println("Searching for substrings...");
		System.out.println();
		long tic = System.nanoTime();
		for (Map.Entry<Integer, HashMap<Integer, String>> entry : ss.lengthPatternMap.entrySet()) {
			if (entry.getValue().size() == 1) {
				ss.boyerMoore(ss.sequCharAr, entry.getValue().values().toArray()[0].toString());
			} else {
				ss.searchRK(entry.getValue(), entry.getKey());
			}
		}
		System.out.println();
		for (String pattern : ss.allPatternsList) {
			System.out.println(pattern + ": " + ss.results.get(pattern).count);
			System.out.println(ss.results.get(pattern).first10);
		}
		double tac = System.nanoTime() - tic;
		tac = tac / 1000000000;
		System.out.println("Time: " + tac + "s");
	}
}
