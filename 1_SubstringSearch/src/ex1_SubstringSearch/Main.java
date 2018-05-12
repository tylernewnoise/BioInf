package ex1_SubstringSearch;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: GdBioinf1.jar pattern.fasta sequence.fasta");
            System.exit(1);
        }

        FastaSequence pattern = new FastaSequence(args[0]);
        FastaSequence sequence = new FastaSequence(args[1]);

        char [] seq = sequence.getSequence(0).toCharArray();

        for (int i=0; i< pattern.getSize(); i++) {
            char [] pat = pattern.getSequence(i).toCharArray();
            BoyerMoore boyerMoore = new BoyerMoore(pat);

            int [] first_positions = new int[10];
            short counter = 0;
            int occurrences = 0;
            int match_index = -1;

            while (true) {
                match_index = boyerMoore.search(seq, match_index+1);
                if (match_index == seq.length)
                    break;
                if (counter < 10) {
                    first_positions[counter++] = match_index+1;
                }
                occurrences++;
            }
            System.out.println(pattern.getSequence(i) + ": " + occurrences);
            System.out.print("[");
            for (int j = 0; j < counter; j++) {
                if (j != counter-1)
                    System.out.print(first_positions[j] + ", ");
                else
                    System.out.println(first_positions[j] + "]");
            }
        }
    }
}
