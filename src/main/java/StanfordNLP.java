import edu.stanford.nlp.ie.crf.CRFClassifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * NER module from the Stanford NLP library.
 * @author Maxim Serebrianski
 */

class StanfordNLP {

    public static Properties setProperties(String trainFileList, int set) {
        Properties props = new Properties();

        props.setProperty("trainFileList", trainFileList);
        props.setProperty("map", "word=0,answer=1");
        props.setProperty("saveFeatureIndexToDisk", "true");

        switch (set) {
            case 0: // default properties
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
            case 1:
//                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
            case 2:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
//                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
            case 3:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "10");    // 6 --> 10
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
            case 4:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("useNextSequences", "true");  //added
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
            case 5:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "2");  // 1 --> 2
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
            case 6:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeSeqs3", "true");  // added
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
            case 7:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris3useLC");  // changed
                props.setProperty("useDisjunctive", "true");
                break;
            case 8:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "dan2useLC");  // changed
                props.setProperty("useDisjunctive", "true");
                break;
            case 9:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "jenny1useLC");  // changed
                props.setProperty("useDisjunctive", "true");
                break;
            case 10:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "2");  // 1 --> 2
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris3useLC");  // changed
                props.setProperty("useDisjunctive", "true");
                break;
            case 11:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "1");
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                props.setProperty("useTags", "true");   // added
                break;
            case 12:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "3");  // 1 --> 3
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
            case 13:
                props.setProperty("useClassFeature", "true");
                props.setProperty("useWord", "true");
                props.setProperty("useNGrams", "true");
                props.setProperty("noMidNGrams", "true");
                props.setProperty("maxNGramLeng", "6");
                props.setProperty("usePrev", "true");
                props.setProperty("useNext", "true");
                props.setProperty("useSequences", "true");
                props.setProperty("usePrevSequences", "true");
                props.setProperty("maxLeft", "4");  // 1 --> 4
                props.setProperty("useTypeSeqs", "true");
                props.setProperty("useTypeSeqs2", "true");
                props.setProperty("useTypeySequences", "true");
                props.setProperty("wordShape", "chris2useLC");
                props.setProperty("useDisjunctive", "true");
                break;
        }

        return props;
    }

    public static CRFClassifier trainClassifier(Properties props, int featureSet, int evalFold, boolean crossValidation, boolean annoOnly) {
        CRFClassifier classifier = new CRFClassifier(props);
        classifier.train();
        if (crossValidation) {
            if (annoOnly) {
                classifier.serializeClassifier("src/main/resources/models/snlp/cross/anno/" + evalFold + "-eval.ser.gz");
                classifier.serializeClassifier("D:/java/stanford-ner/classifiers/cross/anno/" + evalFold + "-eval.ser.gz");
            } else {
                classifier.serializeClassifier("src/main/resources/models/snlp/cross/full/" + evalFold + "-eval.ser.gz");
                classifier.serializeClassifier("D:/java/stanford-ner/classifiers/cross/full/" + evalFold + "-eval.ser.gz");
            }
        } else {
            if (annoOnly) {
                if (featureSet == 0) {
                    classifier.serializeClassifier("src/main/resources/models/snlp/features/anno/feature-set-default.ser.gz");
                    classifier.serializeClassifier("D:/java/stanford-ner/classifiers/features/anno/feature-set-default.ser.gz");
                } else {
                    classifier.serializeClassifier("src/main/resources/models/snlp/features/anno/feature-set-" + featureSet + ".ser.gz");
                    classifier.serializeClassifier("D:/java/stanford-ner/classifiers/features/anno/feature-set-" + featureSet + ".ser.gz");
                }
            } else {
                if (featureSet == 0) {
                    classifier.serializeClassifier("src/main/resources/models/snlp/features/full/feature-set-default.ser.gz");
                    classifier.serializeClassifier("D:/java/stanford-ner/classifiers/features/full/feature-set-default.ser.gz");
                } else {
                    classifier.serializeClassifier("src/main/resources/models/snlp/features/full/feature-set-" + featureSet + ".ser.gz");
                    classifier.serializeClassifier("D:/java/stanford-ner/classifiers/features/full/feature-set-" + featureSet + ".ser.gz");
                }
            }
        }
        return classifier;
    }

    public static void createBatchFile(String testFiles, String classifier) {
        String[] cl = classifier.split("/");
        File out = new File("D:/Java/stanford-ner/" + cl[cl.length-1] + ".bat");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
//            bw.write("d:");
//            bw.newLine();
//            bw.write("cd java/stanford-ner");
//            bw.newLine();
            bw.write("java -classpath lib/* edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier " + classifier + " -testFiles " + testFiles);
            bw.newLine();
            bw.write("pause");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    public static void evaluate(CRFClassifier classifier, String[] testFile) {
        File out = new File("src/main/resources/training/snlp/test.tsv");
        System.out.println("---");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            for (String str : testFile) {
                bw.write(classifier.classifyToString(str, "tsv", false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void evaluate(CRFClassifier classifier, File testFile) {
        try {
            classifier.classifyAndWriteAnswers(testFile.getPath(), new PlainTextDocumentReaderAndWriter(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void evaluateFile(CRFClassifier classifier, File testFile) {
        System.out.println("---");
        List<List<CoreLabel>> out = classifier.classifyFile(testFile.getPath());
        for (List<CoreLabel> sentence : out) {
            for (CoreLabel word : sentence) {
                System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
            }
            System.out.println();
        }
    }

    private static String[] transformFile(File testFile) {
        String line;
        StringBuilder sb = new StringBuilder();
        String[] splitLine;
        ArrayList<String> sentences = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(testFile))) {
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    splitLine = line.split("\t");
                    if (splitLine[0].equals(".")) {
                        sb.append(splitLine[0]);
                        sentences.add(sb.toString());
                        sb.setLength(0);
                    } else {
                        sb.append(splitLine[0]).append(" ");
                    }
                }
            }
//            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sentences.toArray(new String[sentences.size()]);
    }*/
}
