package gb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class gb {

	private char[] seq1;
	private char[] seq2;

	private void readPairs(String pairsFile) {
		boolean readSeq1 = false;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pairsFile),
			StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith(">")) {
					if (readSeq1) {
						seq2 = new char[line.length()];
						seq2 = line.toCharArray();
						break; // Only read two sequences.
					}
					seq1 = new char[line.length()];
					seq1 = line.toCharArray();
					readSeq1 = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static int maxScore(char[] seq1, char[] seq2, String matrixFile) {
		String alphabet = "";
		int[][] matrix = new int[0][0];
		// ############ READ MATRIX
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

		// ############ CalcScore
		int[][] similarityTable = new int[seq2.length + 1][seq1.length + 1];
		// Fill the table.
		for (int i = 0; i < seq2.length + 1; ++i) {
			for (int j = 0; j < seq1.length + 1; ++j) {
				similarityTable[i][j] = j * -1;
			}
			similarityTable[i][0] = i * -1;
		}
		int gapPenalty = -1;
		int scoreFromMatrix;
		int tableCols = seq1.length + 1;
		int tableRows = seq2.length + 1;
		int upperLeftNb;
		int leftNb;
		int upperNb;
		int maxLocalScore;
		int scoreMax = Integer.MIN_VALUE;
		for (int row = 1; row < tableRows; row++) {
			for (int col = 1; col < tableCols; col++) {
				// Get score of the given matrix: matrix[letter on actual row][letter on actual col].
				scoreFromMatrix = matrix[alphabet.indexOf(seq2[row - 1])][alphabet.indexOf(seq1[col - 1])];

				// Calculate neighbours.
				leftNb = similarityTable[row][col - 1] + gapPenalty;
				upperNb = similarityTable[row - 1][col] + gapPenalty;
				upperLeftNb = similarityTable[row - 1][col - 1] + scoreFromMatrix;

				// Calculate maximum and add it to list.
				maxLocalScore = Math.max(leftNb, Math.max(upperNb, upperLeftNb));
				similarityTable[row][col] = maxLocalScore;

				if (maxLocalScore >= scoreMax) {
					scoreMax = maxLocalScore;
				}
			}
		}
		System.out.println(similarityTable[tableRows-1][tableCols-1]);
		return scoreMax;
	}

	public static void main(String[] args) {
		//String seq1 = "CCCAGCAGCAGAAGTTATCACTGGCTATCAACGATTGAACTCCCAATGTGGCGAGCAACGGACGGCACAGCAGGCAGCCTTACTCCATGTTGTTCGACAATACTCAGTTCTACAGTCCAG";
		//String seq2 = "CTGAGCACCGCTTTTGCACTACAAGGATTCGAACCCCATTGTGCGAACAACGGACGCACAGCATTACACCTGTTTGCCGATATTCACCCTGATGTGGG";
		gb g = new gb();
		g.readPairs(args[1]);

		int i = maxScore(g.seq1, g.seq2, args[0]);
		System.out.println(i);
	}

}
