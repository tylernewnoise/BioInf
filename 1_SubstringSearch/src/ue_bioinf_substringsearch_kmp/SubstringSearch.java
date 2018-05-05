package ue_bioinf_substringsearch_kmp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SubstringSearch {
	private SubstringSearch() {
	}

	private class resultsTuple {
		String pattern;
		int count;
		ArrayList<Integer> first10;

		resultsTuple(String pattern, int count, ArrayList<Integer> first10) {
			this.pattern = pattern;
			this.count = count;
			this.first10 = first10;
		}
	}

	private char[] sequence;
	private ArrayList<String> patternsList = new ArrayList<>();
	private ArrayList<resultsTuple> results = new ArrayList<>();

	private void readPattern(String patternFile) {
		boolean isPattern = false;
		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(patternFile),
			StandardCharsets.ISO_8859_1))) {
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.startsWith(">")) {
					if (isPattern) {
						patternsList.add(stringBuilder.toString());
						isPattern = false;
						stringBuilder = new StringBuilder();
					}
				} else {
					isPattern = true;
					stringBuilder.append(line);
				}
			}
			if (isPattern) {
				patternsList.add(stringBuilder.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
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
			sequence = stringBuilder.toString().toCharArray();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void knuthMorrisPratt(char[] sequence, String p) {
		ArrayList<Integer> first10 = new ArrayList<>();
		int cnt = 0, i = 0, j = 0;
		char[] pattern = p.toCharArray();
		int[] table = calcTable(pattern);

		while (j < sequence.length) {
			while (i > -1 && sequence[j] != pattern[i]) {
				i = table[i];
			}
			i++;
			j++;
			if (i >= pattern.length) {
				++cnt;
				if (first10.size() < 10) {
					first10.add(j - i + 1);
				}
				i = table[i];
			}
		}
		results.add(new resultsTuple(p, cnt, first10));
	}

	private static int[] calcTable(char[] pattern) {
		int[] table = new int[pattern.length + 1];
		int i = 0;
		int j = table[0] = -1;

		while (i < pattern.length) {
			while (j > -1 && pattern[i] != pattern[j]) {
				j = table[j];
			}
			table[++i] = ++j;
		}

		return table;
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
		System.out.println();
		long tic = System.nanoTime();
		for (String p : ss.patternsList) {
			ss.knuthMorrisPratt(ss.sequence, p);
		}

		for (resultsTuple resultsTuple : ss.results) {
			System.out.println(resultsTuple.pattern + ": " + resultsTuple.count);
			System.out.println(resultsTuple.first10);
		}

		double tac = System.nanoTime() - tic;
		tac = tac / 1000000000;
		System.out.println("Time: " + tac + "s");
	}
}
