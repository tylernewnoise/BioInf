package ue_bioinf_global_alignment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GlobalAlignment {
	private GlobalAlignment() {
	}

	// Data structure to store coordinates and the alignment type (insertion, deletion, match).
	private class Coordinates {
		int row;
		int column;
		char type;

		Coordinates(int r, int c, char t) {
			this.row = r;
			this.column = c;
			this.type = t;
		}
	}

	private class Cell {
		List<Coordinates> content = new ArrayList<>();
		int simscore = 0;
	}

	private Cell[][] similarityTable;
	// Last maximum we found is the score.
	private int scoreMax = 0;
	// Stores the two sequences.
	private char[] seq1;
	private char[] seq2;
	// Stores the alignment in printable form.
	private List<Character> alignmentList1 = new ArrayList<>();
	private List<Character> alignmentList2 = new ArrayList<>();
	private List<Character> alignmentList3 = new ArrayList<>();
	// 2-D int array for storing the matrix-values.
	private int[][] matrix;
	private String letters;
	private int allIns = 0;
	private int allReps = 0;
	private int allDels = 0;
	private int allMatches = 0;

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

		// Create new similarityTable of the size of the sequences (similarityTable[rows][cols]).
		similarityTable = new Cell[seq2.length + 1][seq1.length + 1];

		// Fill the table.
		for (int i = 0; i < seq2.length + 1; ++i) {
			for (int j = 0; j < seq1.length + 1; ++j) {
				similarityTable[i][j] = new Cell();
				similarityTable[i][j].simscore = j * -1;
			}
			similarityTable[i][0].simscore = i * -1;
		}
	}

	private void readMatrix(String matrixFile) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(matrixFile),
			StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (line.startsWith(" ")) {
						letters = line.replaceAll("\\s", "");
						matrix = new int[letters.length()][letters.length()];
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
	}

	private int getValueFromMatrix(char a, char b) {
		int index1 = letters.indexOf(a);
		int index2 = letters.indexOf(b);
		return matrix[index1][index2];
	}

	private void buildTable() {
		int gapPenalty = -8;
		int scoreFromMatrix;
		int tableCols = seq1.length + 1;
		int tableRows = seq2.length + 1;
		int upperLeftNb;
		int leftNb;
		int upperNb;
		int maxLocalScore;
		for (int row = 1; row < tableRows; row++) {
			System.out.print('\r');
			System.out.print("Processing similarityTable... Columns: " + tableCols + ", Rows: " + tableRows
				+ ", Processing Row: " + (row + 1) + "...");

			for (int col = 1; col < tableCols; col++) {
				// Get score of the given matrix: matrix[letter on actual row][letter on actual col].
				scoreFromMatrix = getValueFromMatrix(seq2[row - 1], seq1[col - 1]);

				// Calculate neighbours.
				leftNb = similarityTable[row][col - 1].simscore + gapPenalty;
				upperNb = similarityTable[row - 1][col].simscore + gapPenalty;
				upperLeftNb = similarityTable[row - 1][col - 1].simscore + scoreFromMatrix;

				// Calculate maximum.
				maxLocalScore = Math.max(leftNb, Math.max(upperNb, upperLeftNb));

				// Add maximum to list. Check if there are multiple candidates.
				if (leftNb == maxLocalScore) {
					similarityTable[row][col].simscore = maxLocalScore;
					similarityTable[row][col].content.add(new Coordinates(row, col - 1, 'l'));
				}
				if (upperNb == maxLocalScore) {
					similarityTable[row][col].simscore = maxLocalScore;
					similarityTable[row][col].content.add(new Coordinates(row - 1, col, 'u'));
				}
				if (upperLeftNb == maxLocalScore) {
					similarityTable[row][col].simscore = maxLocalScore;
					similarityTable[row][col].content.add(new Coordinates(row - 1, col - 1, 'd'));
				}
				if (maxLocalScore >= scoreMax) {
					scoreMax = maxLocalScore;
				}
			}
		}
	}

	private void traceBack() {
		// Start from (n,m).
		int row = seq2.length;
		int col = seq1.length;
		char type;

		do {
			// Simply get the first candidate in the list. We don't care about the other ways - shame on us.
			row = similarityTable[row][col].content.get(0).row;
			col = similarityTable[row][col].content.get(0).column;
			type = similarityTable[row][col].content.get(0).type;

			// Prepare list for output and count deletions, insertions, replacements and matches.
			if (type == 'l') {
				alignmentList1.add(seq1[col]);
				alignmentList2.add(' ');
				alignmentList3.add('_');
				allDels++;
			} else if (type == 'u') {
				alignmentList1.add('_');
				alignmentList2.add(' ');
				alignmentList3.add(seq2[row]);
				allIns++;
			} else if (type == 'd') {
				char s1 = seq1[col];
				char s2 = '|';
				char s3 = seq2[row];
				if (s1 != s3) {
					s2 = '.'; // Replacement.
					allReps++;
				} else allMatches++;
				alignmentList1.add(s1);
				alignmentList2.add(s2);
				alignmentList3.add(s3);
			}
		} while ((row - 1) != 0 && (col - 1) != 0);
	}

	private void printAlignment() {
		System.out.println("Length: \t\t" + alignmentList1.size());
		System.out.println("Score: \t\t\t" + scoreMax);
		System.out.println("Matches: \t\t" + allMatches);
		System.out.println("Replacements: \t" + allReps);
		System.out.println("Deletions: \t\t" + allDels);
		System.out.println("Insertions: \t" + allIns);
		System.out.println();
		System.out.println("Alignment: ");
		for (int i = alignmentList1.size() - 1; i >= 0; --i) {
			System.out.print(alignmentList1.get(i));
		}
		System.out.println();
		for (int i = alignmentList2.size() - 1; i >= 0; --i) {
			System.out.print(alignmentList2.get(i));
		}
		System.out.println();
		for (int i = alignmentList3.size() - 1; i >= 0; --i) {
			System.out.print(alignmentList3.get(i));
		}
		System.out.println();
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("usage: java -jar GlobalAlignment.jar <pairs.fasta> <matrix.txt> ");
			System.exit(-1);
		}
		GlobalAlignment la = new GlobalAlignment();
		long tic = System.nanoTime();
		System.out.print("Parsing pairs.fasta...");
		la.readPairs(args[0]);
		System.out.println("Done.");
		System.out.print("Parsing matrix.txt...");
		la.readMatrix(args[1]);
		System.out.println("Done.");
		System.out.print("Building Table...");
		la.buildTable();
		System.out.println("Done.");
		System.out.print("TraceBack...");
		la.traceBack();
		System.out.println("Done.");
		System.out.println();
		la.printAlignment();
		double tac = System.nanoTime() - tic;
		tac = tac / 1000000000;
		System.out.println("\nRuntime: " + tac + "s");
	}
}
