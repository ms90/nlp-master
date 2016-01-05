import java.io.*;
import java.text.DecimalFormat;

/**
 * WebAnno Extractor for CoNLL 2002 format
 * @author Maxim Serebrianski
 */
class WebAnno {

    /**
     * Creates training data for OpenNLP from WebAnno export files (CoNLL 2002).
     * @param in CoNLL file to be converted
     */
    public static void genOpenNlp(File in) {
        String line;
        String[] splitLine;
        String[] annoSplit;
        boolean annotation = false;
        String fileName = in.getName().replace("%26", "&").replace("%2520", " ").replace(".conll", ".train");
        File out = new File("src/main/resources/training/onlp/annotated/" + fileName);
        StringBuilder sb = new StringBuilder();
        boolean write = false;
        boolean lastLine = false;

        System.out.println("Processing " + "\"" + in.getName() + "\"");

        try (BufferedReader br = new BufferedReader(new FileReader(in)); BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            while ((line = br.readLine()) != null) {
                if (!(line.isEmpty() || lastLine)) {
                    splitLine = line.trim().split(" ");
                    if (splitLine[1].equals("O")) {
                        if (annotation) {
                            sb.append("<END> ");
                            annotation = false;
                        }
                        sb.append(splitLine[0]).append(" ");
                    } else {
                        annoSplit = splitLine[1].split("-");
                        switch (annoSplit[0]) {
                            case "B":
                                switch (annoSplit[1]) {
                                    case "START":
                                        write = true;
                                        sb.setLength(0);
                                        sb.append(splitLine[0]).append(" ");
                                        break;
                                    case "END":
                                        write = false;
                                        lastLine = true;
                                        sb.append(splitLine[0]).append(" .");
                                        break;
                                    default:
                                        if (annotation) {
                                            sb.append("<END> ");
                                        }
                                        annotation = true;
                                        sb.append("<START:").append(annoSplit[1]).append("> ").append(splitLine[0]).append(" ");
                                }
                                break;
                            case "I":
                                annotation = true;
                                sb.append(splitLine[0]).append(" ");
                                break;
                        }
                    }
                } else {
                    if (write || lastLine) {
                        bw.write(sb.toString().trim());
                        if (!lastLine) {
                            bw.newLine();
                        }
                    }
                    sb.setLength(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------------------------------");
    }

    /**
     * Creates tab-separated training data (Penn Treebank) from WebAnno export files (CoNLL 2002).
     * @param in CoNLL file to be converted
     */
    public static void genStanfordNlp(File in) {
        String fileName = in.getName().replace("%26", "&").replace("%2520", " ").replace(".conll", ".tsv");
        File out = new File("src/main/resources/training/snlp/" + fileName);
        String line;
        String[] splitLine;
        String[] annotation;
        StringBuilder sb = new StringBuilder();
        boolean write = false;
//        int lineNumber = 0;

        System.out.println("Processing " + "\"" + in.getName() + "\"");

        try (BufferedReader br = new BufferedReader(new FileReader(in)); BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            while ((line = br.readLine()) != null) {
//                lineNumber++;
                sb.setLength(0);
                boolean lastLine = false;
                if (!line.isEmpty()) {
//                    System.out.println(lineNumber + " " + line);
                    splitLine = line.trim().split(" ");
                    if (splitLine[1].equals("O")) {
                        sb.append(splitLine[0]).append("\t").append(splitLine[1]).append("\n");
                    } else {
                        annotation = splitLine[1].split("-");
                        switch (annotation[1]) {
                            case "START":
                                write = true;
                                sb.append(splitLine[0]).append("\tO\n");
                                break;
                            case "END":
                                write = false;
                                lastLine = true;
                                sb.append(splitLine[0]).append("\tO\n.\tO");
                                break;
                            default:
                                sb.append(splitLine[0]).append("\t").append(annotation[1]).append("\n");
                                break;
                        }
                    }
                    if (write || lastLine) {
                        bw.write(sb.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------------------------------");
    }

    public static void runStatistics(File in) {
        String line;
        int tokens = 0;
        int lines = 0;
        int annotations = 0;
        int goods = 0;
        int assets = 0;
        int services = 0;
        String[] splitLine;
        String[] annoSplit;

        System.out.println("Processing " + in.getName());
        System.out.println();

        try (BufferedReader br = new BufferedReader(new FileReader(in))) {
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    tokens += 1;
                    splitLine = line.split(" ");
                    annoSplit = splitLine[1].split("-");
                    if (annoSplit[0].equals("B")) {
                        annotations += 1;
                        switch (annoSplit[1]) {
                            case "GOODS":
                                goods += 1;
                                break;
                            case "ASSET":
                                assets += 1;
                                break;
                            case "SERVICE":
                                services += 1;
                                break;
                        }
                    }
                } else {
                    lines += 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Tokens: " + tokens);
        System.out.println("Lines: " + lines);
        System.out.println("Annotations (total): " + annotations);
        if (annotations != 0) {
            System.out.println("Goods: " + goods + " (" + Double.parseDouble(new DecimalFormat("##.##").format((double) goods / annotations * 100)) + "%)");
            System.out.println("Assets: " + assets + " (" + Double.parseDouble(new DecimalFormat("##.##").format((double) assets / annotations * 100)) + "%)");
            System.out.println("Services: " + services + " (" + Double.parseDouble(new DecimalFormat("##.##").format((double) services / annotations * 100)) + "%)");
        }
        System.out.println("-----------------------------------------------");
    }
}