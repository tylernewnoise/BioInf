import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;

public class SubstringSearch {
	private SubstringSearch() {
	}

	private ArrayList<String> allPatterns = new ArrayList<>();
	//private HashMap<String, Integer> allSequences = new HashMap<>(); TODO
	private ArrayList<String> allSequences = new ArrayList<>();
	private ArrayList<Integer> firstTenOccurrences = new ArrayList<>(10);

	private void readPattern(String patternFile) {
		boolean isPattern = false;

		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(patternFile),
			StandardCharsets.ISO_8859_1))) {
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.startsWith(">")) {
					if (isPattern) {
						allPatterns.add(stringBuilder.toString());
						isPattern = false;
						stringBuilder = new StringBuilder();
					}
				} else {
					isPattern = true;
					stringBuilder.append(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void readSequences(String sequencesFile) {
		boolean isSequence = false;
		int sequenceLength;

		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sequencesFile),
			StandardCharsets.ISO_8859_1))) {
			String line;
			// TODO
			while ((line = reader.readLine()) != null) {
				//sequenceLength = line.length();
				if (line.startsWith(">")) {
					if (isSequence) {
						sequenceLength = stringBuilder.toString().length() - line.length();
						allSequences.add(stringBuilder.toString());
						//allSequences.putIfAbsent(stringBuilder.toString(), sequenceLength);
						isSequence = false;
						//sequenceLength = 0;
						stringBuilder = new StringBuilder();
					}
				} else {
					isSequence = true;
					stringBuilder.append(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private int searcher(String pattern, String sequence) {
		int cnt = 0;
		int pos = 0;

		if (cnt <= 10) {
			firstTenOccurrences.add(pos);
		}
		return 0;
	}

	private void printResult(String pattern) {
		int cnt;

		for (String sequence : allSequences) {
			cnt = searcher(pattern, sequence);
			System.out.println(pattern + " " + cnt);
			System.out.println(firstTenOccurrences);
			firstTenOccurrences.clear();
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
		ss.readSequences(args[1]);
		for (int i = 0; i < ss.allPatterns.size(); ++i) {
			ss.printResult(ss.allPatterns.get(i));
		}
	}
}
