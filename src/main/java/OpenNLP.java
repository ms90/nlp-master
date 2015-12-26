import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;

/**
 * NER Pipeline consisting of sentence detector, tokenizer and named entity recognizer
 * @author Maxim Serebrianski
 */
class OpenNLP {

    public static void preProcess(String text, File out) throws FileNotFoundException {
        SentenceModel sm = loadSentenceModel();
        TokenizerModel tm = loadTokenizerModel();

        long startTime = System.currentTimeMillis();

        System.out.println("Generating unannotated training file ...");
        generateTokenizedReport(text, out, sm, tm);

        System.out.println("Duration: " + (System.currentTimeMillis() - startTime) + " ms");
        System.out.println("-----------------------------------------------");
    }

    private static void generateTokenizedReport(String text, File output, SentenceModel sm, TokenizerModel tm) {
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

    public static void trainModel(File annotations) throws IOException {
        Charset charset = Charset.forName("UTF-8");
        ObjectStream<NameSample> sampleStream;
        TokenNameFinderModel model = null;
        BufferedOutputStream modelOut;

        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(annotations), charset);
        sampleStream = new NameSampleDataStream(lineStream);

        try {
            model = NameFinderME.train("en", "products", sampleStream, Collections.<String,Object>emptyMap());
        } finally {
            sampleStream.close();
        }

        modelOut = null;
        try {
            modelOut = new BufferedOutputStream(new FileOutputStream(new File("src/main/resources/models/test.bin")));
            model.serialize(modelOut);
        } finally {
            if (modelOut != null)
                modelOut.close();
        }
    }

    public static void createTrainingFile(File[] files) {
        String line;
        File trainingFile = new File("src/main/resources/training/tf.train");

        for (File f : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(f)); BufferedWriter bw = new BufferedWriter(new FileWriter(trainingFile, true))) {
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                }
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void evaluateNER() {
        //TODO
    }
}