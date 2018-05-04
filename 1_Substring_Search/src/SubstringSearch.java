import com.eaio.stringsearch.StringSearch;
import com.eaio.stringsearch.BoyerMooreHorspoolRaita;
// from https://github.com/johannburkard/StringSearch

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SubstringSearch {
	private SubstringSearch() {
	}

	class Result {
		String pattern;
		Integer cnt;
		ArrayList<Integer> firstTenOccurrences;
	}

	private String sequence;
	private ArrayList<String> allPatterns = new ArrayList<>();
	private CopyOnWriteArrayList<Result> resultsList = new CopyOnWriteArrayList<>();

	private ExecutorService executorService;

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
			if (isPattern) {
				allPatterns.add(stringBuilder.toString());
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
			sequence = stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void threadedSearcher(String pattern, String sequence) {
		int cnt = 0;
		int pos = 0;
		int start = 0;
		StringSearch bmhRaita = new BoyerMooreHorspoolRaita();
		ArrayList<Integer> first10 = new ArrayList<>(10);
		Result done = new Result();

		while (pos != -1) {
			pos = bmhRaita.searchString(sequence, start, pattern);
			if (pos != -1) {
				++cnt;
				if (cnt <= 10) {
					first10.add(pos + 1);
				}
				start = pos + 1;
			}
		}
		done.cnt = cnt;
		done.firstTenOccurrences = first10;
		done.pattern = pattern;

		resultsList.add(done);
	}

	private void startThreads(ArrayList<String> patterns) {
		for (int i = 0; i < patterns.size(); ++i) {
			final int x = i;
			executorService.execute(() -> threadedSearcher(patterns.get(x), sequence));
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
		ss.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
		ss.startThreads(ss.allPatterns);
		ss.executorService.shutdown();
		try {
			ss.executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		for (int i = 0; i < ss.resultsList.size(); ++i) {
			System.out.println(ss.resultsList.get(i).pattern + ": " + ss.resultsList.get(i).cnt);
			System.out.println(ss.resultsList.get(i).firstTenOccurrences);
		}
	}
}
