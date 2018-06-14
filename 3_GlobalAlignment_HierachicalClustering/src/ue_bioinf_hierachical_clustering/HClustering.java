package ue_bioinf_hierachical_clustering;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class HClustering {

	private static ArrayList<String> readSequences(String fastaFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fastaFile));
		ArrayList<String> sequences = new ArrayList<>();
		String line = reader.readLine();

		do {
			StringBuilder buffer = new StringBuilder();

			while (line != null && (line.length() == 0 || line.charAt(0) != '>'))
				line = reader.readLine();
			while ((line = reader.readLine()) != null && line.length() != 0 && line.charAt(0) != '>')
				buffer.append(line);

			if (buffer.length() > 0)
				sequences.add(buffer.toString());

		} while (reader.ready());

		reader.close();

		return sequences;
	}

	private static int maxScore(char[] seq1, char[] seq2, String matrixFile) {
		String alphabet = "";
		int[][] matrix = new int[0][0];
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(matrixFile),
			StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (line.startsWith(" ")) {
						alphabet = line.replaceAll("\\s", "");
						matrix = new int[alphabet.length()][alphabet.length()];
						break;
					}
				}
			}

			int j = 0;
			while ((line = reader.readLine()) != null) {
				String[] values = line.trim().replaceAll(" +", " ").split(" ");
				for (int i = 1; i < values.length; i++) {
					matrix[i - 1][j] = Integer.parseInt(values[i]);
				}
				j++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		int[][] similarityTable = new int[seq2.length + 1][seq1.length + 1];
		for (int i = 0; i < seq2.length + 1; ++i) {
			for (int j = 0; j < seq1.length + 1; ++j) {
				similarityTable[i][j] = j * -1;
			}
			similarityTable[i][0] = i * -1;
		}
		int gapPenalty = -8;
		int scoreFromMatrix;
		int tableCols = seq1.length + 1;
		int tableRows = seq2.length + 1;
		int upperLeftNb;
		int leftNb;
		int upperNb;
		int maxLocalScore;
		for (int row = 1; row < tableRows; row++) {
			for (int col = 1; col < tableCols; col++) {
				scoreFromMatrix = matrix[alphabet.indexOf(seq2[row - 1])][alphabet.indexOf(seq1[col - 1])];

				leftNb = similarityTable[row][col - 1] + gapPenalty;
				upperNb = similarityTable[row - 1][col] + gapPenalty;
				upperLeftNb = similarityTable[row - 1][col - 1] + scoreFromMatrix;

				maxLocalScore = Math.max(leftNb, Math.max(upperNb, upperLeftNb));
				similarityTable[row][col] = maxLocalScore;

			}
		}
		return similarityTable[tableRows - 1][tableCols - 1];
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("usage: java -jar HClustering.jar <pairs.fasta> <matrix.txt> ");
			System.exit(-1);
		}

		ArrayList<String> sequences = readSequences(args[0]);
		HashSet<String> species = new HashSet<>();
		for (int i = 0; i < sequences.size(); i++) {
			species.add(Integer.toString(i));
		}

		HashMap<String, Integer> Distance = new HashMap<>();
		for (String s1 : species) {
			for (String s2 : species) {
				if (!s1.equals(s2)) {
					Distance.put(s1 + "," + s2, maxScore(sequences.get(Integer.parseInt(s1)).toCharArray(),
						sequences.get(Integer.parseInt(s2)).toCharArray(), args[1]));
					Distance.put(s2 + "," + s1, Distance.get(s1 + "," + s2));
				}
			}
		}

		while (Distance.size() > 0) {
			int bestScore = Integer.MIN_VALUE;
			String bestPair = null;
			for (String s : Distance.keySet()) {
				if (Distance.get(s) > bestScore) {
					bestPair = s;
					bestScore = Distance.get(bestPair);
				}
			}

			System.out.print("(" + bestPair + ") ");

			assert bestPair != null;
			String a = bestPair.split(",")[0];
			String b = bestPair.split(",")[1];
			String ancestor = a + b;
			for (String s : species) {
				if (!s.equals(a) && !s.equals(b)) {
					Distance.put(s + "," + ancestor, (Distance.get(s + "," + a) + Distance.get(s + "," + b)) / 2);
					Distance.put(ancestor + "," + s, Distance.get(s + "," + ancestor));
					Distance.remove(s + "," + a);
					Distance.remove(a + "," + s);
					Distance.remove(s + "," + b);
					Distance.remove(b + "," + s);
				}
			}

			Distance.remove(a + "," + b);
			Distance.remove(b + "," + a);

			species.remove(a);
			species.remove(b);
			species.add(ancestor);
		}
	}
}
