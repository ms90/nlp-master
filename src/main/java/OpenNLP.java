import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;

/**
 * NER Pipeline consisting of sentence detector, tokenizer and named entity recognizer
 * @author Maxim Serebrianski
 */
class OpenNLP {

    public static void runPipeline(String text, File out, boolean trainOnly) throws FileNotFoundException {
        SentenceModel sm = loadSentenceModel();
        TokenizerModel tm = loadTokenizerModel();

        long startTime = System.currentTimeMillis();

        try {
            if (trainOnly) {
                System.out.println("Generating unannotated training file ...");
                generateTrainingFile(text, out, sm, tm);
            } else {
                System.out.println("Running Named Entity Recognition ...");
                runNER(text, sm, tm);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Duration: " + (System.currentTimeMillis() - startTime) + " ms");
        System.out.println("-----------------------------------------------");
    }

    private static void generateTrainingFile(String text, File output, SentenceModel sm, TokenizerModel tm) throws FileNotFoundException {
        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"))) {
            String[] sentences = detectSentences(text, sm);
            for (String s : sentences){
                String[] tokens = tokenizeSentence(s, tm);
                StringBuilder sb = new StringBuilder();
                for (String token : tokens) {
                    sb.append(token).append(" ");
                }
                out.write(sb.toString().trim());
                out.newLine();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runNER(String text, SentenceModel sm, TokenizerModel tm) throws FileNotFoundException {
        String[] sentences = detectSentences(text, sm);
        for (String s : sentences){
            String[] tokens = tokenizeSentence(s, tm);
            runNerOnSentence(tokens);
        }

    }

    private static SentenceModel loadSentenceModel() throws FileNotFoundException {
    	InputStream modelIn = new FileInputStream("src/main/resources/models/en-sent.bin");
        SentenceModel model = null;

        try {
            model = new SentenceModel(modelIn);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                modelIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return model;
    }
    
    private static TokenizerModel loadTokenizerModel() throws FileNotFoundException {
    	InputStream modelIn = new FileInputStream("src/main/resources/models/en-token.bin");
        TokenizerModel model = null;

        try {
            model = new TokenizerModel(modelIn);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                modelIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return model;
    }
    
    private static String[] detectSentences(String text, SentenceModel model) {
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
        return sentenceDetector.sentDetect(text);
    }

    private static String[] tokenizeSentence(String input, TokenizerModel model) {
        Tokenizer tokenizer = new TokenizerME(model);
        return tokenizer.tokenize(input);
    }

    private static void runNerOnSentence(String[] tokens){
        //TODO
    }

    private static void trainModel(File annotations) {
        //TODO
    }
}