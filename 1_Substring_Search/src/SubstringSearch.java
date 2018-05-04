import com.eaio.stringsearch.StringSearch;
import com.eaio.stringsearch.BoyerMooreHorspoolRaita;
// from https://github.com/johannburkard/StringSearch

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
	private ArrayList<Integer> firstTenOccurrences = new ArrayList<>(10);
	//private Integer sequenceLength = 0;
	private String sequence;

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

	private void readSequence(String sequenceFile) {

		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sequenceFile),
			StandardCharsets.ISO_8859_1))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith(">")) {
					//sequenceLength += line.length();
					stringBuilder.append(line);
				}
			}
			sequence = stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private int searcher(String pattern, String sequence) {
		int cnt = 0;
		int res = 0;
		int start = 0;
		StringSearch bmhRaita = new BoyerMooreHorspoolRaita();

		while (res != -1){
			res = bmhRaita.searchString(sequence, start, pattern);
			if (res != -1) {
				++cnt;
				if (cnt <= 10) {
					firstTenOccurrences.add(res + 1);
				}
				start = res + 1;
			}
		}

		return cnt;
	}

	private void printResult(String pattern) {
		int cnt;

		cnt = searcher(pattern, sequence);
		System.out.println(pattern + " " + cnt);
		System.out.println(firstTenOccurrences);
		firstTenOccurrences.clear();
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
		for (int i = 0; i < ss.allPatterns.size(); ++i) {
			ss.printResult(ss.allPatterns.get(i));
		}
	}
}
