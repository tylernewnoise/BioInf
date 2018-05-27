package ue_bioinf_local_alignment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LocalAlignment {
	private LocalAlignment() {
	}

	private class Coordinates {
		int row;
		int column;

		Coordinates(int col, int row) {
			this.column = col;
			this.row = row;
		}
	}

	private char[] alphabet = new char[4];
	private int[][] matrix = new int[4][4];
	private int[][] table;
	private int max = 0;
	private int r_max = 0;
	private int c_max = 0;
	private HashMap<Coordinates, String> trace = new HashMap<>();
	private ArrayList<char[]> pairs = new ArrayList<>(2);

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

		table = new int[pairs.get(1).length + 1][pairs.get(0).length + 1]; // table[rows][cols]

		// Fill table with zeros.
		for (int i = 0; i < pairs.get(1).length; ++i) { // Go through rows.
			for (int j = 0; j < pairs.get(0).length; ++j) { // Go through columns.
				table[i][j] = 0;
			}
		}
	}

	private void readMatrix(String matrixFile) {
		int r = 0, c = 0; // For traversing rows and columns in the matrix.

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(matrixFile),
			StandardCharsets.UTF_8))) {
			String line;
			boolean isNeg = false;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					// Get order of alphabet in the matrix.
					if (line.startsWith(" ")) {
						for (int k = 0; k < line.length(); k++) {
							if (Character.isLetter(line.charAt(k))) {
								alphabet[c] = line.charAt(k);
								c++;
							}
						}
					} else {
						c = 0;
						// Get values in matrix line by line, char by char.
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
		int gap_penalty = -2;
		int score;
		int pos_of_char_in_matrix_row;
		int pos_of_char_in_matrix_col;
		int table_cols = pairs.get(0).length + 1;
		int table_rows = pairs.get(1).length + 1;
		int up_left = 0;
		int lef = 0;
		int up_neigh = 0;

		for (int row = 1; row < table_rows; row++) {
			for (int col = 1; col < table_cols; col++) {
				// Calculate Score (This is ugly, maybe a hashmap?)
				for (pos_of_char_in_matrix_col = 0; pos_of_char_in_matrix_col < alphabet.length; pos_of_char_in_matrix_col++) {
					if (pairs.get(0)[col - 1] == alphabet[pos_of_char_in_matrix_col]) break;
				}
				for (pos_of_char_in_matrix_row = 0; pos_of_char_in_matrix_row < alphabet.length; pos_of_char_in_matrix_row++) {
					if (pairs.get(1)[row - 1] == alphabet[pos_of_char_in_matrix_row]) break;
				}
				score = matrix[pos_of_char_in_matrix_row][pos_of_char_in_matrix_col];

				up_neigh = table[row - 1][col] + gap_penalty;
				lef = table[row][col - 1] + gap_penalty;
				up_left = table[row - 1][col - 1] + score;
				System.out.println("#############");
				System.out.println("Row: " + row + " Col: " + col);
				System.out.println("left Neighbour: " + lef);
				System.out.println("up_neigh Neighbour: " + up_neigh);
				System.out.println("up_left Neighbour: " + up_left);
				System.out.println("#############");
				System.out.println();

				// Calculate Max of left neighbour + gap, upper neighbour + gap, upper left neighbour + gap
				table[row][col] = Math.max(table[row - 1][col] + gap_penalty,
					Math.max(table[row][col - 1] + gap_penalty,
						Math.max(table[row - 1][col - 1] + score, 0)));

				if (table[row][col] >= max) {
					max = table[row][col];
					r_max = row;
					c_max = col;
				}
			}
		}
		//System.out.println(max);
	}


	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("usage: java -jar LocalAlignment.jar <pairs.fasta> <matrix.txt> ");
			System.exit(-1);
		}
		LocalAlignment la = new LocalAlignment();
		System.out.println("Parsing pairs.fasta...");
		la.readPairs(args[0]);
		System.out.println("Parsing matrix.txt...");
		la.readMatrix(args[1]);
		System.out.println();
		System.out.println("Alphabet: " + Arrays.toString(la.alphabet));
		la.buildTable();
		// print table
		System.out.println();
		for (int i = 0; i < la.pairs.get(1).length + 1; ++i) {
			for (int j = 0; j < la.pairs.get(0).length + 1; ++j) {
				if (la.table[i][j] >= 0) {
					System.out.print(" ");
				}
				System.out.print(" " + la.table[i][j]);
			}
			System.out.println();
		}
		/*for (int i = 0; i < la.matrix[0].length; ++i) {
			for (int j = 0; j < la.matrix[1].length; ++j) {
				if (la.matrix[i][j] >= 0) {
					System.out.print(" ");
				}
				System.out.print(" " + la.matrix[i][j]);
			}
			System.out.println();
		}*/
	}
}
