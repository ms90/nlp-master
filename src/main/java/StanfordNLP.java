import java.io.*;

/**
 * Named Entity Recognizer from the Stanford NLP library.
 * @author Maxim Serebrianski
 */

class StanfordNLP {

    /**
     * Writes the properties file necessary for training the model.
     * @param features Features for the properties file
     * @return Properties file
     */
    private static File writePropFile(String[] features) {
        File propFile = new File("src/main/resources/training/snlp/training.prop");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(propFile))){
            for (String s : features) {
                bw.write(s);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propFile;
    }

    /**
     * Creates the training file for a specific fold of the cross-validation by stitching together all the separate training files.
     * @param files Training files that need to be combined
     * @param evalFold Fold currently being evaluated
     * @return Generated training file
     */
    public static File createTrainingFile(File[] files, int evalFold) {
        if (evalFold != 0) {
            System.out.println("Evaluating fold " + evalFold);
            System.out.println();
        }
        String line;

        File trainFile = new File("src/main/resources/training/snlp/tmp.train");

        for (File f : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(f)); BufferedWriter bw = new BufferedWriter(new FileWriter(trainFile, true))) {
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return trainFile;
    }
}
