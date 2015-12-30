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
import java.util.ArrayList;
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
    
    private static String[] detectSentences(String text, SentenceModel model) {
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
        return sentenceDetector.sentDetect(text);
    }

    private static String[] tokenizeSentence(String input, TokenizerModel model) {
        Tokenizer tokenizer = new TokenizerME(model);
        return tokenizer.tokenize(input);
    }

    private static File trainModel(File annotations) throws IOException {
        Charset charset = Charset.forName("UTF-8");
        ObjectStream<NameSample> sampleStream;
        TokenNameFinderModel model = null;
        BufferedOutputStream modelOut;
        File out = new File("src/main/resources/models/onlp/custom/" + annotations.getName().replace(".train",".bin"));

        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(annotations), charset);
        sampleStream = new NameSampleDataStream(lineStream);

        try {
            model = NameFinderME.train("en", "products", sampleStream, Collections.<String,Object>emptyMap());
        } finally {
            sampleStream.close();
        }

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

    private static File createTrainingFile(File[] files, int evalFold) {
        System.out.println("Evaluating fold " + evalFold);
        System.out.println();
        String line;
        File trainFile = new File("src/main/resources/training/onlp/" + evalFold + "-eval.train");

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

    public static FMeasure[] evaluateFold(File[] testFiles, File[] trainFiles, int evalFold) throws IOException {
        ObjectStream<String> lineStream;
        NameSampleDataStream testStream;
        TokenNameFinderEvaluator evaluator;
        ArrayList<FMeasure> measures = new ArrayList<>();
        Double pre = 0.0;
        Double rec = 0.0;
        Double fm = 0.0;

        File model = new File("src/main/resources/models/onlp/custom/" + evalFold + "-eval.bin");

        if (!model.exists()) {
            TokenNameFinderModel nameFinderModel = new TokenNameFinderModel(trainModel(createTrainingFile(trainFiles, evalFold)));
            evaluator = new TokenNameFinderEvaluator(new NameFinderME(nameFinderModel));
        } else {
            evaluator = new TokenNameFinderEvaluator(new NameFinderME(new TokenNameFinderModel(model)));
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

        for (FMeasure m : measures) {
            pre += m.getPrecisionScore();
            rec += m.getRecallScore();
            fm += m.getFMeasure();
        }

        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Average Precision for fold " + evalFold + ": " + pre/measures.size());
        System.out.println("Average Recall for fold " + evalFold + ": " + rec/measures.size());
        System.out.println("Average F-Measure for fold " + evalFold + ": " + fm/measures.size());

        return measures.toArray(new FMeasure[measures.size()]);
    }
}