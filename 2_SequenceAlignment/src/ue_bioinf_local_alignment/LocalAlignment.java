package ue_bioinf_local_alignment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class LocalAlignment {
	private LocalAlignment() {
	}

	// Data structure to store coordinates and the alignment type (insertion, deletion, match).
	private class Coordinates<R, C, T> {
		R row;
		C column;
		T type;

		Coordinates(R r, C c, T t) {
			this.row = r;
			this.column = c;
			this.type = t;
		}

		public int hashCode() {
			return this.row.hashCode() + this.column.hashCode() + this.type.hashCode();
		}

		@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
		public boolean equals(Object obj) {
			return this.row.equals(((Coordinates<?, ?, ?>) obj).row)
				&& this.column.equals(((Coordinates<?, ?, ?>) obj).column)
				&& this.type.equals(((Coordinates<?, ?, ?>) obj).type);
		}
	}

	private int[][] matrix = new int[4][4]; // 2-D int array for storing the matrix-values. We assume the alphabet
	// is only 4 (DNA).
	private int[][] table; // 2-D int array for calculating the alignment.
	private int score = 0; // Last maximum we found is the score.
	private int rMax = 0; // Coordinates to the last maximum.
	private int cMax = 0; // Coordinates to the last maximum.
	private ArrayList<char[]> pairs = new ArrayList<>(2); // Stores the two sequences.
	// Stores the alignment in printable form.
	private ArrayList<ArrayList<Character>> alignment = new ArrayList<>(3);
	// Stores the position of the nucleobases from the matrix.txt.
	private HashMap<Character, Integer> matrixCol = new HashMap<>(4);
	private HashMap<Character, Integer> matrixRow = new HashMap<>(4);
	// Safes the coordinates from fields as key and keeps a list with pointers to possible neighbours as value.
	private HashMap<Coordinates<Integer, Integer, Character>, ArrayList<Coordinates<Integer, Integer, Character>>>
		trace = new HashMap<>();
	private int overAllIns = 0;
	private int overAllReps = 0;
	private int overAllDels = 0;
	private int overAllMatches = 0;

	private void readPairs(String pairsFile) {
		int seqCount = 0;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pairsFile),
			StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith(">") && seqCount <= 2) {
					pairs.add(line.toCharArray());
					seqCount++;
					if (seqCount == 2) break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Create new table of the size of the sequences (table[rows][cols]).
		table = new int[pairs.get(1).length + 1][pairs.get(0).length + 1];

		// Fill first row with zeros.
		for (int i = 0; i < pairs.get(1).length; ++i) {
			table[i][0] = 0;
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
		int tableCols = pairs.get(0).length + 1;
		int tableRows = pairs.get(1).length + 1;
		int upperLeftNb;
		int leftNb;
		int upperNb;
		int maxNb;
		for (int row = 1; row < tableRows; row++) {
			for (int col = 1; col < tableCols; col++) {
				// Get score of the given matrix: matrix[letter on actual row][letter on actual col].
				localScore = matrix[matrixRow.get(pairs.get(1)[row - 1])]
					[matrixCol.get(pairs.get(0)[col - 1])];

				// Calculate neighbours. maxNb stores the maximum of all three, to distinguish if any
				// of them are 'worthy' to be pointed on.
				leftNb = table[row][col - 1] + gapPenalty;
				upperNb = table[row - 1][col] + gapPenalty;
				upperLeftNb = table[row - 1][col - 1] + localScore;
				maxNb = leftNb;
				if (maxNb < upperNb) maxNb = upperNb;
				if (maxNb < upperLeftNb) maxNb = upperLeftNb;

				// Set pointers. Only if at least one of the above is > 0.
				if (leftNb > 0 || upperNb > 0 || upperLeftNb > 0) {
					// Get maximum for actual position in table.
					table[row][col] = Math.max(leftNb, Math.max(upperNb, Math.max(upperLeftNb, 0)));
					ArrayList<Coordinates<Integer, Integer, Character>> tmp = new ArrayList<>();
					// Add only fields to list which are score.
					if (leftNb == maxNb) {
						tmp.add(new Coordinates<>(row, col - 1, 'l')); // left
					}
					if (upperNb == maxNb) {
						tmp.add(new Coordinates<>(row - 1, col, 'u')); // up
					}
					if (upperLeftNb == maxNb) {
						tmp.add(new Coordinates<>(row - 1, col - 1, 'd')); // diagonal
					}
					// Add the coordinates and the possible neighbours to map.
					trace.put(new Coordinates<>(row, col, ' '), tmp);
				} else table[row][col] = 0; // If not, set field to zero.

				// Safe last maximum (aka score) and its coordinates for the traceback.
				if (table[row][col] >= this.score) {
					this.score = table[row][col];
					rMax = row;
					cMax = col;
				}
			}
		}
	}

	private void traceBack() {
		ArrayList<Coordinates<Integer, Integer, Character>> tmp;
		ArrayList<Character> tmpal;
		Coordinates<Integer, Integer, Character> c;
		int row = rMax;
		int col = cMax;
		int localScore = 0;
		char type = ' ';

		do {
			// Set coordinates for field to backtrace.
			c = new Coordinates<>(row, col, ' ');
			// Get list with possible candidates.
			tmp = new ArrayList<>(trace.get(c));

			// Run through list and get highest value and coordinates of fields.
			for (Coordinates<Integer, Integer, Character> list : tmp) {
				if (table[list.row][list.column] >= localScore) {
					row = list.row;
					col = list.column;
					type = list.type;
					localScore = table[row][col];
				}
			}
			localScore = 0;

			// Prepare list for output and count deletions, insertions, replacements and matches.
			if (type == 'l') {
				tmpal = new ArrayList<>(3);
				tmpal.add(0, pairs.get(0)[row]);
				tmpal.add(1, ' ');
				tmpal.add(2, '_');
				overAllDels++;
				alignment.add(tmpal);
			} else if (type == 'u') {
				tmpal = new ArrayList<>(3);
				tmpal.add(0, '_');
				tmpal.add(1, ' ');
				tmpal.add(2, pairs.get(1)[col]);
				overAllIns++;
				alignment.add(tmpal);
			} else if (type == 'd') {
				tmpal = new ArrayList<>(3);
				char s1 = pairs.get(0)[col];
				char s2 = pairs.get(1)[row];
				char s3 = '|';
				if (s1 != s2) {
					s3 = '.'; // Replacement.
					overAllReps++;
				} else overAllMatches++;
				tmpal.add(0, s1);
				tmpal.add(1, s3);
				tmpal.add(2, s2);
				alignment.add(tmpal);
			}
		} while (table[row][col] != 0);
	}

	private void printAlignment() {
		System.out.println("Length: " + alignment.size());
		System.out.println("Score: " + score);
		System.out.println("Matches: " + overAllMatches);
		System.out.println("Replacements: " + overAllReps);
		System.out.println("Deletions: " + overAllDels);
		System.out.println("Insertions: " + overAllIns);
		System.out.println("Alignment: ");
		System.out.println();

		for (int j = 0; j < alignment.get(0).size(); ++j) {
			for (int i = alignment.size() - 1; i >= 0; --i) {
				System.out.print(alignment.get(i).get(j));
			}
			System.out.println();
		}
	}

	private void printTable() {
		System.out.println("Table: ");
		for (int i = 0; i < pairs.get(1).length + 1; ++i) {
			for (int j = 0; j < pairs.get(0).length + 1; ++j) {
				if (table[i][j] >= 0) {
					System.out.print(" ");
				}
				System.out.print(" " + table[i][j]);
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("usage: java -jar LocalAlignment.jar <pairs.fasta> <matrix.txt> ");
			System.exit(-1);
		}
		LocalAlignment la = new LocalAlignment();
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
		//la.printTable();
	}
}
