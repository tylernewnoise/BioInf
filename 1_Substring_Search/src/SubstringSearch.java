import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//TODO positions, first 10, correct output, trove libs

public class SubstringSearch {
	private SubstringSearch() {
	}

	private String sequence;
	private ArrayList<String> allPatterns = new ArrayList<>();
	private HashMap<Integer, HashMap<Integer, String>> patternsWitTheirLength3 = new HashMap<>();
	private HashMap<String, Integer> results = new HashMap<>();

	private void readPattern(String patternFile) {
		boolean isPattern = false;

		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(patternFile),
			StandardCharsets.ISO_8859_1))) {
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.startsWith(">")) {
					if (isPattern) {
						// packe die patterns der Reihe nach in eine liste
						allPatterns.add(stringBuilder.toString());
						// berechne länge des pattern
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
				// packe die patterns der Reihe nach in eine liste
				allPatterns.add(stringBuilder.toString());
				addPatternToList(stringBuilder.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void addPatternToList(String pattern) {
		// erstelle hashmap mit länge, pattern die zu der länge passsen
		if (patternsWitTheirLength3.containsKey(pattern.length())) {
			patternsWitTheirLength3.get(pattern.length()).put(pattern.hashCode(), pattern);
		} else {
			HashMap<Integer, String> tmp = new HashMap<>();
			tmp.put(pattern.hashCode(), pattern);
			patternsWitTheirLength3.put(pattern.length(), tmp);
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
		int seqHash;
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < sequence.length() - patternLength; ++i) {
			for (int j = i; j < i + patternLength; ++j) {
				stringBuilder.append(sequence.charAt(j));
			}
			seqHash = stringBuilder.toString().hashCode();
			if (patternsRK.containsKey(seqHash)) {
				if (results.containsKey(patternsRK.get(seqHash))) {
					int x = results.get(patternsRK.get(seqHash));
					results.put(patternsRK.get(seqHash), ++x);
				} else {
					results.put(patternsRK.get(seqHash), 1);
				}
			}
			stringBuilder = new StringBuilder();
		}
	}

	public static void main(String[] args) {
		SubstringSearch ss = new SubstringSearch();
		if (args.length < 2) {
			System.err
				.println("usage: java -jar SubstringSearch.jar <pattern.fasta> <sequence.fasta> ");
			System.exit(-1);
		}
		ss.readPattern(args[0]);
		ss.readSequence(args[1]);
		System.out.println(ss.allPatterns);
		System.out.println(ss.patternsWitTheirLength3);
		for (Map.Entry<Integer, HashMap<Integer, String>> entry : ss.patternsWitTheirLength3.entrySet()) {
			ss.searchRK(entry.getValue(), entry.getKey());
		}
		System.out.println(ss.results);
	}
}
