package ue_bioinf_local_alignment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LocalAlignment {
	private LocalAlignment() {
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
	// Coordinates to the last maximum.
	private int rMax = 0;
	private int cMax = 0;
	// Stores the two sequences.
	private char[] seq1;
	private char[] seq2;
	// Stores the alignment in printable form.
	private List<Character> alignmentList1 = new ArrayList<>();
	private List<Character> alignmentList2 = new ArrayList<>();
	private List<Character> alignmentList3 = new ArrayList<>();
	// 2-D int array for storing the matrix-values. We assume the alphabet is only 4 (DNA).
	private int[][] matrix = new int[4][4];
	// Stores the position of the nucleobases from the matrix.txt.
	private HashMap<Character, Integer> matrixCol = new HashMap<>(4);
	private HashMap<Character, Integer> matrixRow = new HashMap<>(4);
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

		// Fill the table with zeros.
		Cell tmp = new Cell();
		for (int i = 0; i < seq2.length + 1; ++i) {
			for (int j = 0; j < seq1.length + 1; ++j) {
				similarityTable[i][j] = tmp;
			}
		}
	}

	private void readMatrix(String matrixFile) {
		int r = 0, c = 0; // For traversing rows and columns in the matrix.

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(matrixFile),
			StandardCharsets.UTF_8))) {
			String line;
			boolean isNeg = false; // Is the value negative?
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					// Get order of alphabet in the matrix.
					if (line.startsWith(" ")) {
						for (int k = 0; k < line.length(); k++) {
							if (Character.isLetter(line.charAt(k))) {
								matrixCol.putIfAbsent(line.charAt(k), c);
								c++;
							}
						}
					} else {
						c = 0;
						// Get values in matrix line by line, char by char.
						matrixRow.putIfAbsent(line.charAt(0), r);
						for (int k = 0; k < line.length(); k++) {
							if (line.charAt(k) == '-') {
								isNeg = true;
							} else if (Character.isDigit(line.charAt(k))) {
								matrix[r][c] = Character.getNumericValue(line.charAt(k));
								if (isNeg) {
									matrix[r][c] = matrix[r][c] * -1;
									isNeg = false;
								}
								c++;
							}
						}
						r++;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void buildTable() {
		int gapPenalty = -8;
		int localScore;
		int tableCols = seq1.length + 1;
		int tableRows = seq2.length + 1;
		int upperLeftNb;
		int leftNb;
		int upperNb;
		int maxNb;
		for (int row = 1; row < tableRows; row++) {
			System.out.print('\r');
			System.out.print("Processing similarityTable... Columns: " + tableCols + ", Rows: " + tableRows
				+ ", Processing Row: " + (row + 1) + "...");

			for (int col = 1; col < tableCols; col++) {
				// Get score of the given matrix: matrix[letter on actual row][letter on actual col].
				localScore = matrix[matrixRow.get(seq2[row - 1])][matrixCol.get(seq1[col - 1])];

				// Calculate neighbours. maxNb stores the maximum of all three, to distinguish if any
				// of them are 'worthy' to be pointed on.
				leftNb = similarityTable[row][col - 1].simscore + gapPenalty;
				upperNb = similarityTable[row - 1][col].simscore + gapPenalty;
				upperLeftNb = similarityTable[row - 1][col - 1].simscore + localScore;

				maxNb = leftNb;
				if (maxNb < upperNb) maxNb = upperNb;
				if (maxNb < upperLeftNb) maxNb = upperLeftNb;

				// Set pointers. Only if at least one of the above is > 0 aka maxNb > 0.
				if (maxNb > 0) {
					// Get maximum for actual position in similarityTable.
					Cell celltmp = new Cell();
					celltmp.simscore = Math.max(leftNb, Math.max(upperNb, Math.max(upperLeftNb, 0)));

					// Add only fields to list which are a maxScore.
					if (leftNb == maxNb) {
						celltmp.content.add(new Coordinates(row, col - 1, 'l'));
					}
					if (upperNb == maxNb) {
						celltmp.content.add(new Coordinates(row - 1, col, 'u'));
					}
					if (upperLeftNb == maxNb) {
						celltmp.content.add(new Coordinates(row - 1, col - 1, 'd'));
					}
					// Add the coordinates and the possible neighbours to map.
					similarityTable[row][col] = celltmp;
				}

				// Safe last maximum (aka score) and its coordinates for the traceback.
				if (similarityTable[row][col].simscore >= scoreMax) {
					scoreMax = similarityTable[row][col].simscore;
					rMax = row;
					cMax = col;
				}
			}
		}
	}

	private void traceBack() {
		List<Coordinates> tmp;
		int row = rMax;
		int col = cMax;
		int localScore = 0;
		char type = ' ';

		do {
			// Get list with possible candidates.
			tmp = similarityTable[row][col].content;

			// Run through list and get highest value and coordinates of fields.
			for (Coordinates entry : tmp) {
				if (similarityTable[entry.row][entry.column].simscore >= localScore) {
					row = entry.row;
					col = entry.column;
					type = entry.type;
					localScore = similarityTable[row][col].simscore;
				}
			}

			localScore = 0;

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
		} while (similarityTable[row][col].simscore != 0);
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
			System.err.println("usage: java -jar LocalAlignment.jar <pairs.fasta> <matrix.txt> ");
			System.exit(-1);
		}
		LocalAlignment la = new LocalAlignment();
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

