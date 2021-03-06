import opennlp.tools.util.eval.FMeasure;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

/**
 * Main class for parsing the report and initializing the NLP pipeline
 * @author Maxim Serebrianski
 */
class Main {
    private static int foldCounter = 1;
    private static final FileFilter directoryFilter = File::isDirectory;

    /**
     * Main point of entry for the application. Choose and enter the appropriate number for processing.
     * @param args
     * @throws IOException
     */
    @SuppressWarnings("JavaDoc")
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        File dir = new File("src/main/resources/training/conll");
        FileFilter fileFilter = new WildcardFileFilter("*.conll");
        File path;
        File[] dirs;

        System.out.println("Enter '1' to generate unannotated training data from 10-K reports.");
        System.out.println("Enter '2' to generate annotated training data for OpenNLP from exported WebAnno files.");
        System.out.println("Enter '3-1' to generate statistics from exported WebAnno files (all documents).");
        System.out.println("Enter '3-2' to generate statistics from exported WebAnno files (per domain).");
        System.out.println("Enter '4' to copy annotation files into stratified folds (for OpenNLP).");
        System.out.println("Enter '5' to evaluate OpenNLP model in global setting.");
        System.out.println("Enter '6' to generate annotated training data for Stanford NLP from exported WebAnno files.");
        System.out.println("Enter '7' to copy annotation files into stratified folds (for Stanford NLP, full).");
        System.out.println("Enter '8' to train Stanford NLP classifier for cross-validation (full).");
        System.out.println("Enter '9' to train Stanford NLP classifier with 50% of training files (full).");
        System.out.println("Enter '10' to generate annotated training data for Stanford NLP from exported WebAnno files and keep only annotated sentences.");
        System.out.println("Enter '11' to copy annotation files into stratified folds (for Stanford NLP, annotated only).");
        System.out.println("Enter '12' to train Stanford NLP classifier for cross-validation (annotated only).");
        System.out.println("Enter '13' to train Stanford NLP classifier with 50% of training files (annotated only).");
        System.out.println("Enter '14' to run domain specific evaluation for OpenNLP.");
        System.out.println("Enter '15' to run domain specific evaluation for Stanford CoreNLP (full).");
        System.out.println("Enter '16' to run domain specific evaluation for Stanford CoreNLP (annotated only).");
        System.out.println("--------------------------------------------------------------------------");

        switch (sc.nextLine()) {
            case "1":   // unannotated training data from 10-K reports
                System.out.println("--------------------------------------------------------------------------");
                processReports();
                System.out.println("Done!");
                break;

            case "2":   // annotated training data for OpenNLP
                System.out.println("--------------------------------------------------------------------------");
                File[] f1 = dir.listFiles(fileFilter);
                for (File f : f1) {
                    WebAnno.genOpenNlp(f);
                }
                System.out.println("Done!");
                break;

            case "3-1":   // statistics on exported WebAnno files (full)
                System.out.println("--------------------------------------------------------------------------");
                File[] f3 = dir.listFiles(fileFilter);
                System.out.println("All Documents");
                System.out.println();
                WebAnno.runStatistics(f3);
                System.out.println("Done!");
                break;

            case "3-2": // statistics on exported WebAnno files (per domain)
                System.out.println("--------------------------------------------------------------------------");
                path = new File("src/main/resources/training/conll/domains");
                dirs = path.listFiles(directoryFilter);

                for (File folder : dirs) {
                    System.out.println(folder.getName());
                    System.out.println();
                    WebAnno.runStatistics(folder.listFiles());
                }
                System.out.println("Done!");
                break;

            case "4":   // generate folds for OpenNLP
                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Copying files to folds...");
                createFolds(true, false);
                System.out.println("Done!");
                break;

            case "5":   // 10-fold cross-validation (OpenNLP)
                evaluateCrossOpenNlp();
                break;

            case "6":   // generate training data for Stanford NLP (full)
                System.out.println("--------------------------------------------------------------------------");
                File[] f4 = dir.listFiles(fileFilter);

                for (File f : f4) {
                    WebAnno.genStanfordNlp(f);
                }
                System.out.println("Done!");
                break;

            case "7":   // generate folds for Stanford NLP (full)
                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Copying files to folds...");
                createFolds(false, false);
                System.out.println("Done!");
                break;

            case "8":   // train Stanford classifier with model 10 for 10-fold cross-validation (full)
                evaluateCrossSnlp(false);
                break;

            case "9":  // train Stanford classifier with different feature sets on 50% data, test on 20% (full)
                evaluateSnlpFeatureSets(false);
                break;

            case "10":  //Generate Stanford NLP training data (annotated only)
                System.out.println("--------------------------------------------------------------------------");
                File[] f5 = dir.listFiles(fileFilter);

                for (File f : f5) {
                    WebAnno.genStanNlpAnnoOnly(f);
                }
                System.out.println("Done!");
                break;

            case "11":   // generate folds for Stanford NLP (annotated only)
                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Copying files to folds...");
                createFolds(false, true);
                System.out.println("Done!");
                break;

            case "12":   // train Stanford classifier with model 10 for 10-fold cross-validation (annotated only)
                evaluateCrossSnlp(true);
                break;

            case "13":  // train Stanford classifier with different feature sets on 50% data, test on 20% (annotated only)
                evaluateSnlpFeatureSets(true);
                break;

            case "14":  // domain specific evaluation (OpenNLP)
                evaluateDomainOpenNlp();
                break;

            case "15":  // domain specific evaluation (Stanford NLP, full)
                evaluateDomainSnlp(false);
                break;

            case "16":  // domain specific evaluation (Stanford NLP, annotations only)
                evaluateDomainSnlp(true);
                break;
        }
    }

    private static void evaluateDomainOpenNlp() {
        System.out.println("--------------------------------------------------------------------------");
        File path = new File("src/main/resources/training/onlp/domain");
        File[] domains = path.listFiles(directoryFilter);
        File[][] files = new File[5][];
        ArrayList<FMeasure> measures;
        ArrayList<File> trainFiles;

        for (File domain : domains) {
            int i = 0;
            Double pre = 0.0;
            Double rec = 0.0;
            Double fm = 0.0;
            measures = new ArrayList<>();
            System.out.println("--------------------------------------------------------------------------");
            for (File fold : domain.listFiles(directoryFilter)) {
                files[i] = fold.listFiles();
                i++;
            }
            for (int j=0; j<5; j++) {
                trainFiles = new ArrayList<>();
                for (File[] f : files) {
                    if (!Arrays.equals(f, files[j])){
                        Collections.addAll(trainFiles, f);
                    }
                }
                try {
                    Collections.addAll(measures, OpenNLP.evaluate(files[j], trainFiles.toArray(new File[trainFiles.size()]), j, false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println();
            }

            for (FMeasure m : measures) {
                if (m.getPrecisionScore() > 0.0 && m.getRecallScore() > 0.0 && m.getFMeasure() > 0.0) {
                    pre += m.getPrecisionScore();
                    rec += m.getRecallScore();
                    fm += m.getFMeasure();
                }
            }

            System.out.println(domain.getName());
            System.out.println();
            System.out.println("Average Precision: " + pre/measures.size());
            System.out.println("Average Recall: " + rec/measures.size());
            System.out.println("Average F1: " + fm/measures.size());
            System.out.println("--------------------------------------------------------------------------");
        }
    }

    private static void evaluateDomainSnlp(boolean annoOnly) {
        System.out.println("--------------------------------------------------------------------------");
        File path;
        if (annoOnly) {
            path = new File("src/main/resources/training/snlp/domain/anno");
        } else {
            path = new File("src/main/resources/training/snlp/domain/full");
        }
        File[] domains = path.listFiles(directoryFilter);
        File[][] files = new File[5][];
        StringBuilder fileList = new StringBuilder();
        Properties props;

        for (File domain : domains) {
            int i = 0;
            System.out.println("--------------------------------------------------------------------------");
            for (File fold : domain.listFiles(directoryFilter)) {
                files[i] = fold.listFiles();
                i++;
            }

            for (int j=0; j<5; j++) {
                fileList.setLength(0);
                for (File[] f : files) {
                    if (!Arrays.equals(f, files[j])) {
                        for (File file : f) {
                            fileList.append(file.getPath()).append(",");
                        }
                    }
                }
                fileList.deleteCharAt(fileList.length() - 1);

                props = StanfordNLP.setProperties(fileList.toString(), 10);

                StanfordNLP.trainClassifier(props, 10, j, true, annoOnly, true, domain.getName());

                fileList.setLength(0);

                if (annoOnly) {
                    path = new File("D:/java/stanford-ner/testfiles/cross/domain/anno/" + domain.getName() + "/" + j);
                } else {
                    path = new File("D:/java/stanford-ner/testfiles/cross/domain/full/" + domain.getName() + "/" + j);
                }
                File[] testfiles = path.listFiles();

                if (testfiles != null) {
                    for (File f : testfiles) {
                        if (annoOnly) {
                            fileList.append("testfiles/cross/domain/anno/").append(domain.getName()).append("/").append(j).append("/").append(f.getName()).append(",");
                        } else {
                            fileList.append("testfiles/cross/domain/full/").append(domain.getName()).append("/").append(j).append("/").append(f.getName()).append(",");
                        }
                    }
                }
                fileList.deleteCharAt(fileList.length() - 1);

                if (annoOnly) {
                    StanfordNLP.createEvalScript(fileList.toString(), "classifiers/cross/domain/anno/" + domain.getName() + "/" + j + "-eval.ser.gz", domain.getName());
                } else {
                    StanfordNLP.createEvalScript(fileList.toString(), "classifiers/cross/domain/full/" + domain.getName() + "/" + j + "-eval.ser.gz", domain.getName());
                }
                System.out.println("--------------------------------------------------------------------------");
            }
            System.out.println("Done!");
        }
    }

    /**
     * Evaluates the OpenNLP classifier via 10-fold cross-validation after generating a model
     */
    private static void evaluateCrossOpenNlp() {
        System.out.println("--------------------------------------------------------------------------");
        File path = new File("src/main/resources/training/onlp/folds");
        File[] dirs = path.listFiles(directoryFilter);
        File[][] files = new File[10][];
        ArrayList<FMeasure> measures = new ArrayList<>();
        int i = 0;
        ArrayList<File> trainFiles;
        Double pre = 0.0;
        Double rec = 0.0;
        Double fm = 0.0;

        for (File folder : dirs) {
            files[i] = folder.listFiles();
            i++;
        }

        for (int j=0; j<10; j++) {
            trainFiles = new ArrayList<>();
            for (File[] f : files) {
                if (!Arrays.equals(f, files[j])){
                    Collections.addAll(trainFiles, f);
                }
            }
            try {
                Collections.addAll(measures, OpenNLP.evaluate(files[j], trainFiles.toArray(new File[trainFiles.size()]), j+1, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("--------------------------------------------------------------------------");
        }

        for (FMeasure m : measures) {
            pre += m.getPrecisionScore();
            rec += m.getRecallScore();
            fm += m.getFMeasure();
        }

        System.out.println("Global Precision: " + pre/measures.size());
        System.out.println("Global Recall: " + rec/measures.size());
        System.out.println("Global F-Measure: " + fm/measures.size());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Done!");
    }

    /**
     * Trains models on several feature sets with 50% of the data
     * @param annoOnly True uses only annotated sentences; false uses full reports
     */
    private static void evaluateSnlpFeatureSets(boolean annoOnly) {
        System.out.println("--------------------------------------------------------------------------");
        File path;
        if (annoOnly) {
            path = new File("src/main/resources/training/snlp/anno/folds");
        } else {
            path = new File("src/main/resources/training/snlp/full/folds");
        }
        File[] dirs = path.listFiles(directoryFilter);
        File[][] files = new File[10][];
        StringBuilder filesList = new StringBuilder();
        Properties pr;
        int i = 0;

        for (File folder : dirs) {
            files[i] = folder.listFiles();
            i++;
        }

        for (int j=0; j<5; j++) {
            for (File file : files[j]) {
                filesList.append(file.getPath()).append(",");
            }
        }
        filesList.deleteCharAt(filesList.length()-1);

        for (int k=0; k<14; k++) {
            pr = StanfordNLP.setProperties(filesList.toString(), k);
            StanfordNLP.trainClassifier(pr, k, 999, false, annoOnly, false, "");
            System.out.println("--------------------------------------------------------------------------");
        }

        filesList.setLength(0);

        if (annoOnly) {
            path = new File("D:/Java/stanford-ner/testfiles/features/anno");
        } else {
            path = new File("D:/Java/stanford-ner/testfiles/features/full");
        }
        File[] testfiles = path.listFiles();

        if (testfiles != null) {
            for (File f : testfiles) {
                if (annoOnly){
                    filesList.append("testfiles/features/anno/").append(f.getName()).append(",");
                } else {
                    filesList.append("testfiles/features/full/").append(f.getName()).append(",");
                }
            }
        }
        filesList.deleteCharAt(filesList.length()-1);

        if (annoOnly) {
            path = new File("D:/Java/stanford-ner/classifiers/features/anno/");
        } else {
            path = new File("D:/Java/stanford-ner/classifiers/features/full/");
        }
        File[] classifiers = path.listFiles();

        if (classifiers != null) {
            for (File f : classifiers) {
                if (annoOnly) {
                    StanfordNLP.createEvalScript(filesList.toString(), "classifiers/features/anno/" + f.getName(), "");
                } else {
                    StanfordNLP.createEvalScript(filesList.toString(), "classifiers/features/full/" + f.getName(), "");
                }
            }
        }

        System.out.println("Done!");
    }

    /**
     * Trains models for 10-fold cross-validation with feature set 10 (best results)
     * @param annoOnly True uses only annotated sentences; false uses full reports
     */
    private static void evaluateCrossSnlp(boolean annoOnly) {
        System.out.println("--------------------------------------------------------------------------");
        File path;
        if (annoOnly) {
            path = new File("src/main/resources/training/snlp/anno/folds");
        } else {
            path = new File("src/main/resources/training/snlp/full/folds");
        }
        File[] dirs = path.listFiles(directoryFilter);
        File[][] files = new File[10][];
        StringBuilder fileList = new StringBuilder();
        Properties props;
        int i = 0;

        for (File folder : dirs) {
            files[i] = folder.listFiles();
            i++;
        }

        for (int j=0; j<10; j++) {
            fileList.setLength(0);
            for (File[] f : files) {
                if (!Arrays.equals(f, files[j])) {
                    for (File file : f) {
                        fileList.append(file.getPath()).append(",");
                    }
                }
            }
            fileList.deleteCharAt(fileList.length()-1);

            props = StanfordNLP.setProperties(fileList.toString(), 10);

            StanfordNLP.trainClassifier(props, 10, j, true, annoOnly, false, "");

            fileList.setLength(0);

            if (annoOnly) {
                path = new File("D:/java/stanford-ner/testfiles/cross/anno/" + j);
            } else {
                path = new File("D:/java/stanford-ner/testfiles/cross/full/" + j);
            }
            File[] testfiles = path.listFiles();

            if (testfiles != null) {
                for (File f : testfiles) {
                    if (annoOnly) {
                        fileList.append("testfiles/cross/anno/").append(j).append("/").append(f.getName()).append(",");
                    } else {
                        fileList.append("testfiles/cross/full/").append(j).append("/").append(f.getName()).append(",");
                    }
                }
            }
            fileList.deleteCharAt(fileList.length()-1);

            if (annoOnly) {
                StanfordNLP.createEvalScript(fileList.toString(), "classifiers/cross/anno/" + j + "-eval.ser.gz", "");
            } else {
                StanfordNLP.createEvalScript(fileList.toString(), "classifiers/cross/full/" + j + "-eval.ser.gz", "");
            }
            System.out.println("--------------------------------------------------------------------------");
        }
        System.out.println("Done!");
    }

    /**
     * Reads the reports.txt file and loads the reports from the SEC website.
     * Uses the OpenNLP sentence detection and tokenizer to process the reports for annotation.
     */
    private static void processReports() {
        String[] split;
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/training/reports.txt"))) {
            while ((line = br.readLine()) != null) {
                split = line.split(" -> ");
                System.out.println("Processing " + split[1]);
                Document document = Jsoup.parse(new URL(split[1]).openStream(), "UTF-8", split[1]);
                String text = document.body().text();
                OpenNLP.preProcess(text, new File("src/main/resources/training/" + split[0] + ".txt"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates folds (as directories) for the cross-validation.
     * @param onlp True = OpenNLP
     *             False = Stanford NLP
     * @param annoOnly  True = Annotated sentences only
     *                  False = Full
     * @throws IOException
     */
    private static void createFolds(boolean onlp, boolean annoOnly) throws IOException {
        File annotations;
        String foldsPath;
        if (onlp) {
            annotations = new File("src/main/resources/training/onlp/annotated");
            foldsPath = "src/main/resources/training/onlp/folds/";
        } else {
            if (annoOnly) {
                annotations = new File("src/main/resources/training/snlp/anno/annotated");
                foldsPath = "src/main/resources/training/snlp/anno/folds/";
            } else {
                annotations = new File("src/main/resources/training/snlp/full/annotated");
                foldsPath = "src/main/resources/training/snlp/full/folds/";
            }
        }
        File[] dirs = annotations.listFiles(directoryFilter);
        File[][] files = new File[10][];
        int i = 0;

        for (File dir : dirs) {
            files[i] = dir.listFiles();
            i++;
        }

        Arrays.sort(files, (left, right) -> Integer.compare(left.length, right.length));

        for (File[] f : files) {
            foldCounter = 0;
            for (File file : f) {
                findNextFold(foldsPath);
                Files.copy(file.toPath(), new File(foldsPath + foldCounter + "/" + file.getName()).toPath());
                if (foldCounter <9) {
                    foldCounter++;
                } else {
                    foldCounter = 0;
                }
            }
        }
    }

    /**
     * Finds the next suitable fold to put the annotation file in.
     * @param foldsPath Path to the folds
     */
    private static void findNextFold(String foldsPath) {
        if (new File(foldsPath + foldCounter).list().length >= 10) {
            if (foldCounter <9) {
                foldCounter++;
            } else {
                foldCounter = 0;
            }
            findNextFold(foldsPath);
        }
    }
}