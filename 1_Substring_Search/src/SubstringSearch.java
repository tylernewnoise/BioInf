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
		long tic = System.nanoTime();
		for (Map.Entry<Integer, HashMap<Integer, String>> entry : ss.lengthPatternMap.entrySet()) {
			if (entry.getValue().size() == 1) {
				System.out.println(entry.getValue() + " should be run with BMH"); // TODO implement BMH
				ss.searchRK(entry.getValue(), entry.getKey());
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
