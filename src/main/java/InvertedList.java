import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.collection.JavaConversions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvertedList {

    public static Map searchResult(String searchLine) throws MyStemApplicationException {
        ArrayList<Integer> result = new ArrayList<Integer>();
        MyStemming mystem = new MyStemming();
        String[] searchWords = searchLine.split(" & ");
        for (int i=0;i<searchWords.length;i++){
            final Iterable<Info> lemm =
                    JavaConversions.asJavaIterable(
                            mystem.mystemAnalyzer
                                    .analyze(Request.apply(searchWords[i]))
                                    .info()
                                    .toIterable());

            for (final Info info : lemm) {
                String lemma = String.valueOf(info.lex());
                lemma = lemma.replace("Some(", "");
                lemma = lemma.replace(")", "");
                searchWords[i] = lemma;
            }

        }
        String path = "C:\\Users\\Регина\\Desktop\\Learning\\Search\\GogolSearch\\visitedPage\\uniqWords.txt";
        Scanner sc = null;
        ArrayList<Inverted> wordReference = new ArrayList<Inverted>();
        ArrayList<Integer> wordIndex = new ArrayList<Integer>();
        int minWordRef = 100;
        boolean emptyIndex = false;
        try {
            for (int i=0; i < searchWords.length;i++) {
                sc = new Scanner(new File(path));
                wordIndex = new ArrayList<Integer>();
                while (sc.hasNextLine()) {
                    String checkStr = sc.nextLine();
                    Pattern p = Pattern.compile("^"+searchWords[i]+"\\s.*");
                    Matcher m = p.matcher(checkStr);
                    if (m.matches()) {
                        checkStr = checkStr.substring(searchWords[i].length()+2, checkStr.length() - 1);
                        String[] strIndex = checkStr.split(", ");
                        minWordRef = minWordRef > strIndex.length ? strIndex.length : minWordRef;
                        for (int k=0;k<strIndex.length;k++) {
                            wordIndex.add(Integer.parseInt(strIndex[k]));
                        }
                        wordReference.add(new Inverted(searchWords[i], wordIndex));
                        break;
                    }
                }
            }
            if (wordReference.size() != searchWords.length){
                for (int l=0;l<(searchWords.length - wordReference.size());l++) {
                    wordReference.add(new Inverted(null, null));
                }
                emptyIndex = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i = 101;
        if(!emptyIndex) {
            while (i > 0) {
                int maxIndexValue = wordReference.get(0).getWordList().get(0);
                boolean allIndexSame = true;
                int checkSameIndex = wordReference.get(0).getWordList().get(0);
                for (int j = 1; j < wordReference.size(); j++) {
                    maxIndexValue = maxIndexValue < wordReference.get(j).getWordList().get(0) ? wordReference.get(j).getWordList().get(0) : maxIndexValue;
                    if (allIndexSame && wordReference.get(j).getWordList().get(0) != checkSameIndex) {
                        allIndexSame = false;
                    }
                }
                if (allIndexSame) {
                    result.add(wordReference.get(0).getWordList().get(0));
                    for (int j = 0; j < wordReference.size(); j++) {
                        wordReference.get(j).getWordList().remove(0);
                        if (i > wordReference.get(j).getWordList().size()) {
                            i = wordReference.get(j).getWordList().size();
                        }
                    }
                } else {
                    for (int j = 0; j < wordReference.size(); j++) {
                        while (wordReference.get(j).getWordList().size() > 0 && maxIndexValue > wordReference.get(j).getWordList().get(0)) {
                            wordReference.get(j).getWordList().remove(0);
                        }
                        if (i > wordReference.get(j).getWordList().size()) {
                            i = wordReference.get(j).getWordList().size();
                        }
                    }
                }
            }
        }

        Map searchResult = new HashMap<Integer, String>();

        try {
            path = "C:\\Users\\Регина\\Desktop\\Learning\\Search\\GogolSearch\\visitedPage\\index.txt";
            String strRefIndex = result.toString();
            String[] refIndex = strRefIndex.substring(1, strRefIndex.length() - 1).split(", ");
            for (i = 0; i < refIndex.length; i++) {
                sc = new Scanner(new File(path));
                while (sc.hasNextLine()) {
                    String checkStr = sc.nextLine();
                    Pattern p = Pattern.compile("^" + refIndex[i] + "\\:.*");
                    Matcher m = p.matcher(checkStr);
                    if (m.matches()) {

                        String need = checkStr.substring(0, checkStr.indexOf(':'));
                        if (!need.equals("0")) {

                            checkStr = checkStr.substring(refIndex[i].length()+1);
                            searchResult.put(Integer.parseInt(need), checkStr);
                        }



                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    public void getInvertedList() {
        int i = 1;
        String path;
        Scanner sc = null;
        ArrayList<String> uniqWord = new ArrayList<String>();
        ArrayList<Inverted> invertedList = new ArrayList<Inverted>();
        while (i <= 100) {
            path = "/Users/aydar/Downloads/GogolSearch v.2/visitedPage/lemma/" + i + ".txt";


            i++;
            try {
                sc = new Scanner(new File(path));
                String checkStr;
                while (sc.hasNextLine()) {
                    checkStr = sc.nextLine();
                    if (uniqWord.indexOf(checkStr) == -1) {
                        uniqWord.add(checkStr);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        i = 1;
        ArrayList<Integer> wordIndex = new ArrayList<Integer>();
        for (int j=0; j<uniqWord.size(); j++) {
            while (i <= 100) {
                path = "/Users/aydar/Downloads/GogolSearch v.2/visitedPage/lemma/" + i + ".txt";
                try {
                    sc = new Scanner(new File(path));
                    String checkStr;
                    while (sc.hasNextLine()) {
                        checkStr = sc.nextLine();
                        String temp = uniqWord.get(j);
                        if (temp.equals(checkStr)) {
                            wordIndex.add(i);
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;
            }

            invertedList.add(new Inverted(uniqWord.get(j), wordIndex));
            wordIndex = new ArrayList<Integer>();
            i = 1;
        }
        try(FileWriter writer = new FileWriter("/Users/aydar/Downloads/GogolSearch v.2/visitedPage/uniqWords.txt", false)) {
            String content = "";
            for(int j=0;j<uniqWord.size();j++) {
                content = invertedList.get(j).getWord() +" "+ invertedList.get(j).getWordList().toString();
                writer.write(content);
                writer.append('\n');
                writer.flush();
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }

    static class Inverted{
        private String  word;
        private ArrayList<Integer> wordList = new ArrayList<Integer>();
        public Inverted(String str, ArrayList list){
            word = str;
            wordList = list;
        }
        public String getWord(){
            return word;
        }
        public ArrayList<Integer> getWordList(){
            return wordList;
        }
    }
}
