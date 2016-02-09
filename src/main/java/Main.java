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
        File[][] files;
        ArrayList<File> trainFiles;
        int i = 0;
        ArrayList<FMeasure> measures;
        Double pre = 0.0;
        Double rec = 0.0;
        Double fm = 0.0;

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
                System.out.println("--------------------------------------------------------------------------");
                path = new File("src/main/resources/training/onlp/folds");
                dirs = path.listFiles(directoryFilter);
                files = new File[10][];
                measures = new ArrayList<>();

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
                    Collections.addAll(measures, OpenNLP.evaluate(files[j], trainFiles.toArray(new File[trainFiles.size()]), j+1, true));
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
                break;

            /*
            case "6":   // sector-specific validation (OpenNLP)
                System.out.println("--------------------------------------------------------------------------");
                path = new File("src/main/resources/training/onlp/annotated");
                dirs = path.listFiles(directoryFilter);
                files = new File[10][];
                measures = new ArrayList<>();
                ArrayList<File> evalFiles;

                for (File folder : dirs) {
                    files[i] = folder.listFiles();
                    i++;
                }

                for (File[] f : files) {
                    trainFiles = new ArrayList<>();
                    evalFiles = new ArrayList<>();
                    if (f.length>=10) {
                        //TODO
                        Collections.addAll(measures, OpenNLP.evaluate(evalFiles.toArray(new File[evalFiles.size()]), trainFiles.toArray(new File[trainFiles.size()]), 0, false));
                    }
                }

                for (FMeasure m : measures) {
                    pre += m.getPrecisionScore();
                    rec += m.getRecallScore();
                    fm += m.getFMeasure();
                }

                System.out.println("Sector Precision: " + pre/measures.size());
                System.out.println("Sector Recall: " + rec/measures.size());
                System.out.println("Sector F-Measure: " + fm/measures.size());
                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Done!");
                break;*/

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
                trainSnlpBestCross(false);
                break;

            case "9":  // train Stanford classifier with different feature sets on 50% data, test on 20% (full)
                trainSnlpFeatureSets(false);
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
                trainSnlpBestCross(true);
                break;

            case "13":  // train Stanford classifier with different feature sets on 50% data, test on 20% (annotated only)
                trainSnlpFeatureSets(true);
                break;
        }
    }

    private static void trainSnlpFeatureSets(boolean annoOnly) {
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
            StanfordNLP.trainClassifier(pr, k, 999, false, annoOnly);
            System.out.println("--------------------------------------------------------------------------");
        }

        filesList.setLength(0);

        path = new File("D:/Java/stanford-ner/testfiles/features");
        File[] testfiles = path.listFiles();

        if (testfiles != null) {
            for (File f : testfiles) {
                filesList.append("testfiles/features/").append(f.getName()).append(",");
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
                    StanfordNLP.createBatchFile(filesList.toString(), "classifiers/features/anno/" + f.getName());
                } else {
                    StanfordNLP.createBatchFile(filesList.toString(), "classifiers/features/full/" + f.getName());
                }
            }
        }

        System.out.println("Done!");
    }

    private static void trainSnlpBestCross(boolean annoOnly) {
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

            StanfordNLP.trainClassifier(props, 10, j, true, annoOnly);

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
                StanfordNLP.createBatchFile(fileList.toString(), "classifiers/cross/anno/" + j + "-eval.ser.gz");
            } else {
                StanfordNLP.createBatchFile(fileList.toString(), "classifiers/cross/full/" + j + "-eval.ser.gz");
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