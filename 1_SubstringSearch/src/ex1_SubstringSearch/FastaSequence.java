package ex1_SubstringSearch;

import java.lang.*;
import java.io.*;
import java.util.*;
/**
 *  This class reads from a fasta formatted file to memory and provides the data via
 *  getSequence() and getDescription()
 */
final class FastaSequence {

    // Object variables
    private String [] description;
    private String [] sequence;

    /**
     * Reads all the data from the fasta formatted file to the object
     *
     * @param filename the filename of the fasta formatted file
     */
    FastaSequence(String filename) {
        readSequenceFromFile(filename);
    }

    private void readSequenceFromFile(String file)
    {
        List<String> desc = new ArrayList<>();
        List<String> seq = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            StringBuffer buffer = new StringBuffer();
            String line = in.readLine();

            if(line == null)
                throw new IOException( file + " is an empty file" );

            if(line.charAt(0) != '>' )
                throw new IOException("First line of " + file + " should start with '>'" );
            else
                desc.add(line);
            for(line = in.readLine().trim(); line != null; line = in.readLine()) {
                if( line.length() > 0 && line.charAt( 0 ) == '>' ) {
                    seq.add(buffer.toString());
                    buffer = new StringBuffer();
                    desc.add(line);
                } else
                    buffer.append( line.trim() );
            }
            if( buffer.length() != 0 )
                seq.add(buffer.toString());
        }catch(IOException e) {
            System.out.println("Error when reading " + file);
            e.printStackTrace();
        }

        description = new String[desc.size()];
        sequence = new String[seq.size()];
        for (int i=0; i < seq.size(); i++) {
            description[i]=desc.get(i);
            sequence[i]= seq.get(i);
        }

    }

    /**
     * @param i index of the sequence
     * @return Returns the sequence at position i
     */
    String getSequence(int i){
        return sequence[i];
    }

    /**
     * @param i index of the description
     * @return Returns the description at position i
     */
    String getDescription(int i){
        return description[i];
    }

    int getSize(){
        return sequence.length;
    }
}

