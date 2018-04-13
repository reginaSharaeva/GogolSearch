import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class TFIDFCalculator {

    public void tfIdfInit() {
        ArrayList<ArrayList<String>> WordsByPage = new ArrayList<ArrayList<String>>();
        for (int i = 1; i < 101; i++) {
            ArrayList<String> bufList = new ArrayList<>();
            Scanner sc = null;
            String path = "C:\\Users\\Регина\\Desktop\\Search\\GogolSearch\\visitedPage\\" + i + ".txt";
            try {
                sc = new Scanner(new File(path));
                while (sc.hasNextLine()) {
                    bufList.add(sc.nextLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            WordsByPage.add(bufList);
        }


        String path = "C:\\Users\\Регина\\Desktop\\Search\\GogolSearch\\visitedPage\\uniqWords.txt";
        Scanner sc = null;
        ArrayList<Integer> wordIndex = new ArrayList<Integer>();
        ArrayList<String> allUniqWords = new ArrayList<String>();
        allUniqWords.add("");
        ArrayList<Reference> wordRef = new ArrayList<Reference>();
        try {
            sc = new Scanner(new File(path));
            String buf = null;
            while (sc.hasNextLine()) {
                wordIndex = new ArrayList<Integer>();
                buf = sc.nextLine();
                String word = buf.substring(0, buf.indexOf("[") - 1);
                buf = buf.substring(buf.indexOf("[")+1, buf.length() - 1);
                String[] strIndex = buf.split(", ");
                for (int k = 0; k < strIndex.length; k++) {
                    wordIndex.add(Integer.parseInt(strIndex[k]));
                }
                wordRef.add(new Reference(word, wordIndex));
                allUniqWords.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        int pageIndex = 0;
        String tf = "tf.csv";
        String idf = "idf.csv";
        String tfIdf = "tfIdf.csv";
        CSVWriter writerTf = null;
        CSVWriter writerIdf = null;
        CSVWriter writerTfIdf = null;

        String[] header = allUniqWords.toArray(new String[allUniqWords.size()]);
        String result = allUniqWords.toString();
        result = result.substring(1,result.length()-1);
        header = result.split(",");
        try {
            Writer writerTfSM = new OutputStreamWriter(new FileOutputStream(tf, true), "UTF-8");
            Writer writerIdfSM = new OutputStreamWriter(new FileOutputStream(idf, true), "UTF-8");
            Writer writerTfIdfSM = new OutputStreamWriter(new FileOutputStream(tfIdf, true), "UTF-8");

            writerTf = new CSVWriter(writerTfSM,';');
            writerIdf = new CSVWriter(writerIdfSM,';');
            writerTfIdf = new CSVWriter(writerTfIdfSM,';');

            writerTf.writeNext(header);
            writerIdf.writeNext(header);
            writerTfIdf.writeNext(header);

            writerTf.close();
            writerIdf.close();
            writerTfIdf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Writer writerTfSM = new OutputStreamWriter(new FileOutputStream(tf, true), "UTF-8");
            Writer writerIdfSM = new OutputStreamWriter(new FileOutputStream(idf, true), "UTF-8");
            Writer writerTfIdfSM = new OutputStreamWriter(new FileOutputStream(tfIdf, true), "UTF-8");
            writerTf = new CSVWriter(writerTfSM, ';');
            writerIdf = new CSVWriter(writerIdfSM, ';');
            writerTfIdf = new CSVWriter(writerTfIdfSM, ';');
            for (ArrayList<String> wordsListByPage : WordsByPage) {
                pageIndex++;
                ArrayList<String> recordTf = new ArrayList<>();
                recordTf.add(String.valueOf(pageIndex));
                ArrayList<String> recordIdf = new ArrayList<>();
                recordIdf.add(String.valueOf(pageIndex));
                ArrayList<String> recordTfIdf = new ArrayList<>();
                recordTfIdf.add(String.valueOf(pageIndex));
                for(int i=0; i<allUniqWords.size();i++){
                    double tfValue = tf(wordsListByPage, allUniqWords.get(i));
                    double idfValue = idf(WordsByPage, allUniqWords.get(i));
                    double tfIdfValue = tfIdf(wordsListByPage, WordsByPage, allUniqWords.get(i));
                    recordTf.add(String.valueOf(tfValue > -1 ? tfValue : ""));
                    recordIdf.add(String.valueOf(idfValue > -1 ? idfValue : ""));
                    if (tfIdfValue == Double.NaN) {
                        recordTfIdf.add("");
                    }else{
                        recordTfIdf.add(String.valueOf(tfIdfValue));
                    }
                }
                writerTf.writeNext(recordTf.toArray(new String[recordTf.size()]));
                if (pageIndex==1) {
                    writerIdf.writeNext(recordIdf.toArray(new String[recordIdf.size()]));
                }
                writerTfIdf.writeNext(recordTfIdf.toArray(new String[recordTfIdf.size()]));
            }
            writerTf.close();
            writerIdf.close();
            writerTf.close();
        }
        catch (IOException e) {
                e.printStackTrace();
        }
    }


    class Reference {
        private String word;
        private ArrayList<Integer> wordList = new ArrayList<Integer>();

        public Reference(String str, ArrayList list) {
            word = str;
            wordList = list;
        }

        public String getWord() {
            return word;
        }

        public ArrayList<Integer> getWordList() {
            return wordList;
        }
    }


    /**
     * @param doc  list of strings
     * @param term String represents a term
     * @return term frequency of term in document
     */
    public double tf(ArrayList<String> doc, String term) {
        double result = 0;
        for (String word : doc) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / doc.size();
    }

    /**
     * @param docs list of list of strings represents the dataset
     * @param term String represents a term
     * @return the inverse term frequency of term in documents
     */
    public double idf(ArrayList<ArrayList<String>> docs, String term) {
        double n = 0;
        for (ArrayList<String> doc : docs) {
            for (String word : doc) {
                if (term.equalsIgnoreCase(word)) {
                    n++;
                    break;
                }
            }
        }
        return Math.log(docs.size() / n);
    }

    /**
     * @param doc  a text document
     * @param docs all documents
     * @param term term
     * @return the TF-IDF of term
     */
    public double tfIdf(ArrayList<String> doc, ArrayList<ArrayList<String>> docs, String term) {
        return tf(doc, term) * idf(docs, term);
    }



}