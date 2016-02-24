import opennlp.tools.namefind.*;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.eval.FMeasure;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;

/**
 * NER Pipeline consisting of sentence detector, tokenizer and named entity recognizer.
 * Exposes the 'evaluate' method which internally performs the necessary steps to create the training file,
 * train the model and evaluate the model against test files.
 * @author Maxim Serebrianski
 */
class OpenNLP {

    /**
     * Performs pre-processing like loading models and recording processing time.
     * @param text Report as plain text
     * @param out Tokenized report
     */
    public static void preProcess(String text, File out) {
        SentenceModel sm = null;
        TokenizerModel tm = null;
        try {
            sm = loadSentenceModel();
            tm = loadTokenizerModel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();

        System.out.println("Generating unannotated training file ...");
        generateTokenizedReport(text, out, sm, tm);

        System.out.println("Duration: " + (System.currentTimeMillis() - startTime) + " ms");
        System.out.println("-----------------------------------------------");
    }

    /**
     * 1. Splits the text into sentences
     * 2. Generates tokenized form of the text separated by a blank space character between each token
     * @param text Text to be tokenized
     * @param output File with tokenized text
     * @param sm Sentence model used to split the text into sentences
     * @param tm Tokenizer model used to tokenize the text
     */
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

    /**
     * Loads the sentence model.
     * @return Sentence model
     * @throws FileNotFoundException
     */
    private static SentenceModel loadSentenceModel() throws FileNotFoundException {
    	InputStream modelIn = new FileInputStream("src/main/resources/models/onlp/opensource/en-sent.bin");
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

    /**
     * Loads the tokenizer model.
     * @return Tokenizer model
     * @throws FileNotFoundException
     */
    private static TokenizerModel loadTokenizerModel() throws FileNotFoundException {
    	InputStream modelIn = new FileInputStream("src/main/resources/models/onlp/opensource/en-token.bin");
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

    /**
     * Splits the text into sentences.
     * @param text Text to be split
     * @param model Sentence model
     * @return Array of sentences
     */
    private static String[] detectSentences(String text, SentenceModel model) {
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
        return sentenceDetector.sentDetect(text);
    }

    /**
     * Converts the text into single tokens.
     * @param input Sentence as string
     * @param model Tokenizer model
     * @return Array of tokens
     */
    private static String[] tokenizeSentence(String input, TokenizerModel model) {
        Tokenizer tokenizer = new TokenizerME(model);
        return tokenizer.tokenize(input);
    }

    /**
     * Trains the model and generates a model file on the hard-drive.
     * @param annotations File with training data
     * @param global True = global cross-validation
     *               False = sector-specific cross-validation
     * @return Trained model
     * @throws IOException
     */
    private static File trainModel(File annotations, boolean global) throws IOException {
        Charset charset = Charset.forName("UTF-8");
        ObjectStream<NameSample> sampleStream;
        TokenNameFinderModel model = null;
        BufferedOutputStream modelOut;
        File out;

        if (global) {
            out = new File("src/main/resources/models/onlp/custom/global-tmp.bin");
        } else {
            out = new File("src/main/resources/models/onlp/custom/sector-tmp.bin");
        }

        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(annotations), charset);
        sampleStream = new NameSampleDataStream(lineStream);

        try {
            model = NameFinderME.train("en", "products", sampleStream, Collections.<String,Object>emptyMap());
        } finally {
            sampleStream.close();
        }

        Files.delete(annotations.toPath());

        modelOut = null;
        try {
            modelOut = new BufferedOutputStream(new FileOutputStream(out));
            model.serialize(modelOut);
        } finally {
            if (modelOut != null)
                modelOut.close();
        }

        return out;
    }

    /**
     * Creates the training file for a specific fold of the cross-validation by stitching together all the separate training files.
     * @param files Training files that need to be combined
     * @param evalFold Fold currently being evaluated
     * @return Generated training file
     */
    private static File createTrainingFile(File[] files, int evalFold) {
        System.out.println("Evaluating fold " + evalFold);
        System.out.println();
        String line;

        File trainFile = new File("src/main/resources/training/onlp/tmp.train");

        for (File f : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(f)); BufferedWriter bw = new BufferedWriter(new FileWriter(trainFile, true))) {
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return trainFile;
    }

    /**
     * Evaluates the trained model against test files and calculates Precision, Recall, F1-Measure.
     * @param testFiles Gold standard
     * @param trainFiles Separate training files
     * @param evalFold Fold currently being evaluated
     * @param global True = global cross-validation
     *               False = sector-specific cross-validation
     * @return Array of results, one for each test file
     * @throws IOException
     */
    public static FMeasure[] evaluate(File[] testFiles, File[] trainFiles, int evalFold, boolean global) throws IOException {
        ObjectStream<String> lineStream;
        NameSampleDataStream testStream;
        TokenNameFinderEvaluator evaluator;
        ArrayList<FMeasure> measures = new ArrayList<>();
//        Double pre = 0.0;
//        Double rec = 0.0;
//        Double fm = 0.0;

        if (global) {
            TokenNameFinderModel nameFinderModel = new TokenNameFinderModel(trainModel(createTrainingFile(trainFiles, evalFold), true));
            evaluator = new TokenNameFinderEvaluator(new NameFinderME(nameFinderModel));
        } else {
            TokenNameFinderModel nameFinderModel = new TokenNameFinderModel(trainModel(createTrainingFile(trainFiles, evalFold), false));
            evaluator = new TokenNameFinderEvaluator(new NameFinderME(nameFinderModel));
        }

        for (File f : testFiles) {
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Evaluating " + f.getName());
            lineStream = new PlainTextByLineStream(new FileInputStream(f), "UTF-8");
            testStream = new NameSampleDataStream(lineStream);
            evaluator.evaluate(testStream);
            measures.add(evaluator.getFMeasure());
            System.out.println();
            System.out.println(evaluator.getFMeasure().toString());
        }

        /*for (FMeasure m : measures) {
            if (m.getPrecisionScore() > 0.0 && m.getRecallScore() > 0.0 && m.getFMeasure() > 0.0) {
                pre += m.getPrecisionScore();
                rec += m.getRecallScore();
                fm += m.getFMeasure();
            }
        }

        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Average Precision for fold " + evalFold + ": " + pre/measures.size());
        System.out.println("Average Recall for fold " + evalFold + ": " + rec/measures.size());
        System.out.println("Average F-Measure for fold " + evalFold + ": " + fm/measures.size());*/

        return measures.toArray(new FMeasure[measures.size()]);
    }
}