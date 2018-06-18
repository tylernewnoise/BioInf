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

	/**
	 * Reads sequences from a file in fasta-format.
	 *
	 * @param fastaFile String with path to file in fasta-format.
	 * @return An array list of sequences.
	 * @throws IOException if file is not found.
	 */
	private static ArrayList<String> readSequences(String fastaFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fastaFile));
		ArrayList<String> sequences = new ArrayList<>();
		String line = reader.readLine();

		do {
			StringBuilder buffer = new StringBuilder();

			while (line != null && (line.length() == 0 || line.charAt(0) != '>')) {
				line = reader.readLine();
			}
			while ((line = reader.readLine()) != null && line.length() != 0 && line.charAt(0) != '>') {
				buffer.append(line);
			}

			if (buffer.length() > 0) {
				sequences.add(buffer.toString());
			}

		} while (reader.ready());

		reader.close();

		return sequences;
	}

	/**
	 * Reads the species names of the fasta file.
	 *
	 * @param fastaFile String with path to file in fasta-format.
	 * @return A hash map with entries of the sequences mapped to their species names.
	 * @throws IOException if file is not found.
	 */
	private static HashMap<Integer, String> readSpecies(String fastaFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fastaFile));
		HashMap<Integer, String> species = new HashMap<>();
		String line;
		int i = 0;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {
				species.put(i, line.substring(1));
				i++;
			}
		}
		reader.close();
		return species;
	}

	/**
	 * Reads the alphabet (aka nucleobases) from the matrix file.
	 *
	 * @param matrixFile Path to the matrix file.
	 * @return The alphabet of the substitution matrix.
	 * @throws IOException if matrix file is not found.
	 */
	private static String readAlphabetFromMatrix(String matrixFile) throws IOException {
		String alphabet = "";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(matrixFile),
			StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (line.startsWith(" ")) {
						alphabet = line.replaceAll("\\s", "");
						break;
					}
				}
			}
		}
		return alphabet;
	}

	/**
	 * Reads the substitution matrix form a txt file.
	 *
	 * @param matrixFile Path to the matrix file.
	 * @param alphabet   The alphabet from substitution matrix.
	 * @return An integer array containing the substitution matrix.
	 * @throws IOException if matrix file is not found.
	 */
	private static int[][] readMatrix(String matrixFile, String alphabet) throws IOException {
		int j = 0;
		int[][] matrix = new int[alphabet.length()][alphabet.length()];
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(matrixFile),
			StandardCharsets.UTF_8));

		while ((line = reader.readLine()) != null) {
			if (!line.startsWith("#") && !line.startsWith(" ")) {

				String[] values = line.trim().replaceAll(" +", " ").split(" ");
				for (int i = 1; i < values.length; i++) {
					matrix[i - 1][j] = Integer.parseInt(values[i]);
				}
				j++;
			}
		}
		return matrix;
	}

	/**
	 * Calculates the maximum score of two global aligned sequences. Therefor we create a similarity table. The table
	 * is length of sequence1 times length of sequence2. We go through the table from left to right, top to bottom and
	 * have to fill the table with values first: first row from -1 to -n and first column from -1 to -m.
	 * The score for each cell is calculated by getting the maximum of three equations: left neighbour + gapPenalty,
	 * upper neighbour + gabPenalty and upper left neighbour (diagonal) + the score from the substitution matrix. The
	 * last cell (table[n][m]) is the score we've looked for.
	 *
	 * @param seq1     Char array containing a sequence.
	 * @param seq2     Char array containing a sequence.
	 * @param alphabet The alphabet from the substitution matrix.
	 * @param matrix   The substitution matrix.
	 * @return The maximum score of two global aligned sequences.
	 */
	private static int getMaxScore(char[] seq1, char[] seq2, String alphabet, int[][] matrix) {
		int[][] similarityTable = new int[seq2.length + 1][seq1.length + 1];
		// Fill the table with -1 to -n/-m.
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

		// Run through table form left to right, top to bottom.
		for (int row = 1; row < tableRows; row++) {
			for (int col = 1; col < tableCols; col++) {
				// Get the score from the substitution matrix.
				scoreFromMatrix = matrix[alphabet.indexOf(seq2[row - 1])][alphabet.indexOf(seq1[col - 1])];

				// Calculate the neighbours.
				leftNb = similarityTable[row][col - 1] + gapPenalty;
				upperNb = similarityTable[row - 1][col] + gapPenalty;
				upperLeftNb = similarityTable[row - 1][col - 1] + scoreFromMatrix;

				maxLocalScore = Math.max(leftNb, Math.max(upperNb, upperLeftNb));
				similarityTable[row][col] = maxLocalScore;

			}
		}
		return similarityTable[tableRows - 1][tableCols - 1];
	}


	/**
	 * This calculates the hierachical clustering. We first get all the scores for all pairs of sequences. We then run
	 * through the table as long as it contains any data. Search for the pair of sequences with the smallest score,
	 * print it and remove the rows and columns of the pair, then add a new entry for the merged pair and calculate
	 * the score by its average.
	 *
	 * @param sequences An array list of sequences.
	 * @param alphabet  The alphabet of the substitution matrix.
	 * @param matrix    Substitution matrix.
	 */
	private static void calculateAndPrintTree(ArrayList<String> sequences, String alphabet, int[][] matrix) {
		HashSet<String> species = new HashSet<>();
		for (int i = 0; i < sequences.size(); i++) {
			species.add(Integer.toString(i));
		}

		// Build table, represented as a hash map.
		HashMap<String, Integer> Distance = new HashMap<>();
		for (String s1 : species) {
			for (String s2 : species) {
				if (!s1.equals(s2)) {
					Distance.put(s1 + "," + s2, getMaxScore(sequences.get(Integer.parseInt(s1)).toCharArray(),
						sequences.get(Integer.parseInt(s2)).toCharArray(), alphabet, matrix));
					Distance.put(s2 + "," + s1, Distance.get(s1 + "," + s2));
				}
			}
		}

		System.out.println();
		System.out.println("Tree:");
		while (Distance.size() > 0) {
			// Get the closest two species.
			int bestScore = Integer.MIN_VALUE;
			String bestPair = null;
			for (String s : Distance.keySet()) {
				if (Distance.get(s) > bestScore) {
					bestPair = s;
					bestScore = Distance.get(bestPair);
				}
			}

			// Print em.
			System.out.print("(" + bestPair + ") ");

			assert bestPair != null;
			// Updating the table by merging the two species.
			String a = bestPair.split(",")[0];
			String b = bestPair.split(",")[1];
			String ancestor = a + b;
			for (String s : species) {
				if (!s.equals(a) && !s.equals(b)) {
					// Add new score/distance from merged species.
					Distance.put(s + "," + ancestor, (Distance.get(s + "," + a) + Distance.get(s + "," + b)) / 2);
					Distance.put(ancestor + "," + s, Distance.get(s + "," + ancestor));
					// And remove the old scores.
					Distance.remove(s + "," + a);
					Distance.remove(a + "," + s);
					Distance.remove(s + "," + b);
					Distance.remove(b + "," + s);
				}
			}

			// Remove the scores from the "original" species.
			Distance.remove(a + "," + b);
			Distance.remove(b + "," + a);
			// Remove the old two species and add the "new" one.
			species.remove(a);
			species.remove(b);
			species.add(ancestor);
		}
	}

	/**
	 * This prints the table in a viewable form. We cut the the names of the species to a limit of four to make the
	 * formatting easier. We're also aware that we're doing the same work twice. We calculate the maximum score in this
	 * function as well as in the function calculateAndPrintTree.
	 *
	 * @param sequences  An array list of sequences.
	 * @param speciesMap The list of species mapped to their names.
	 * @param alphabet   The alphabet of the substitution matrix.
	 * @param matrix     Substitution matrix.
	 */
	private static void printTable(ArrayList<String> sequences, HashMap<Integer, String> speciesMap, String alphabet,
								   int[][] matrix) {
		System.out.println("Pairwise scores: ");
		System.out.print("\t\t");
		int c;
		String s;
		// Print first row
		for (int i = 1; i < sequences.size(); i++) {
			s = speciesMap.get(i);
			c = s.length() - 4;
			System.out.print(s.substring(0, s.length() - c) + "\t");
		}
		int j = 0;
		System.out.println();
		s = speciesMap.get(j);
		c = s.length() - 4;
		// Print first cell of first column.
		System.out.print(s.substring(0, s.length() - c) + "\t");
		// Print the rest.
		for (int i = 1; ; i++) {
			System.out.print(getMaxScore(sequences.get(i).toCharArray(), sequences.get(j).toCharArray(), alphabet,
				matrix) + "\t");
			if (i == sequences.size() - 1) {
				if (j == sequences.size() - 2) {
					break;
				}
				i = j + 1;
				j++;
				s = speciesMap.get(j);
				c = s.length() - 4;
				System.out.println();
				System.out.print(s.substring(0, s.length() - c) + "\t");
				for (int k = 0; k < j; ++k) {
					System.out.print("\t\t");
				}
			}
		}
		System.out.println();
	}


	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("usage: java -jar HClustering.jar <pairs.fasta> <matrix.txt> ");
			System.exit(-1);
		}

		System.out.println("Calculating hierachical clustering. Gap penalty is -8!");
		System.out.print("Reading sequences...");
		ArrayList<String> sequences = readSequences(args[0]);
		HashMap<Integer, String> species = readSpecies(args[0]);
		System.out.println("...Done.");
		System.out.print("Reading substitution matrix...");
		String alphabet = readAlphabetFromMatrix(args[1]);
		int[][] matrix = readMatrix(args[1], alphabet);
		System.out.println("...Done.");
		System.out.println("Calculating table and tree...");
		printTable(sequences, species, alphabet, matrix);
		calculateAndPrintTree(sequences, alphabet, matrix);
		System.out.println();
	}
}
