import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.collection.JavaConversions;

import java.io.*;
import java.util.*;
import java.lang.*;

public class CosineSimilarity extends TFIDFCalculator {


    public void cosineSimilarity(String searchLine) throws MyStemApplicationException {

        MyStemming mystem = new MyStemming();
        String[] searchWords = searchLine.split(" & ");
        ArrayList<String> searchWordsList = new ArrayList<String>();
        ArrayList<Integer> searchWordsCount = new ArrayList<Integer>();

        for (int i = 0; i < searchWords.length; i++) {
            final Iterable<Info> lemm =
                    JavaConversions.asJavaIterable(
                            mystem.mystemAnalyzer
                                    .analyze(Request.apply(searchWords[i].trim()))
                                    .info()
                                    .toIterable());

            for (final Info info : lemm) {
                String lemma = String.valueOf(info.lex());
                lemma = lemma.replace("Some(", "");
                lemma = lemma.replace(")", "");
                searchWords[i] = lemma;
                searchWordsList.add(searchWords[i].trim());
            }
        }

        for (int i = 0; i < searchWordsList.size(); i++) {
            int count = 1;
            searchWordsCount.add(count);
            for (int k = i + 1; k < searchWordsList.size(); k++) {
                if (searchWordsList.get(i).equals(searchWordsList.get(k))) {
                    searchWordsList.remove(k);
                    searchWordsCount.remove(searchWordsCount.size() - 1);
                    count++;
                    searchWordsCount.add(count);
                }
            }
        }

        ArrayList<ArrayList<Double>> queryValueRate = tfIdfCounter(searchWordsList);

        ArrayList<Double> queryRateByDoc = new ArrayList<Double>();

        for (int k = 0; k < queryValueRate.get(0).size(); k++) {
            double d = 0.0;
            for (int i = 0; i < queryValueRate.size(); i++) {
                d += Math.pow(queryValueRate.get(i).get(k), 2);
            }
            queryRateByDoc.add(Math.sqrt(d));
        }

        //расчетные вес запроса
        ArrayList<Double> queryRate = new ArrayList<Double>();
        for (int k = 0; k < queryValueRate.get(0).size(); k++) {
            double d = 0.0;
            for (int i = 0; i < queryValueRate.size(); i++) {
                d += Math.pow(queryValueRate.get(i).get(k), 2);
            }
            queryRate.add(Math.sqrt(d));
        }

        //расчетные веса для всех слов по документам
        ArrayList<ArrayList<Double>> rateByPage = getTfIdf();
        ArrayList<Double> tfIdfRate = new ArrayList<Double>();
        for (ArrayList<Double> page : rateByPage) {
            double d = 0.0;
            for (int i = 0; i < page.size(); i++) {
                d += Math.pow(page.get(i), 2);
            }
            tfIdfRate.add(Math.sqrt(d));
        }

        Map buleff = InvertedList.searchResult(searchLine);

        Map searchResult = new HashMap<String, Double>();

        if (buleff.isEmpty()) {
            System.out.println("Поиск не дал результата!");
        } else {
            Iterator<Map.Entry<Integer, String>> entries = buleff.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer, String> entry = entries.next();
                double result = cosineSimilarity(queryRate, rateByPage.get(entry.getKey()));
                searchResult.put(entry.getValue(), result);

            }
        }

        Iterator<Map.Entry<Integer, String>> entries = buleff.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Integer, String> entry = entries.next();
            double result = cosineSimilarity(queryRate, rateByPage.get(entry.getKey()));
            searchResult.put(entry.getValue(), result);

        }

        searchResult = sortHashMapByValues((HashMap<String, Double>) searchResult);

        Iterator<Map.Entry<String, Double>> results = searchResult.entrySet().iterator();
        while (results.hasNext()) {
            Map.Entry<String, Double> entry = results.next();
            System.out.println("rate: " + entry.getValue() + " -> " + entry.getKey());

        }
    }

    public ArrayList<ArrayList<Double>> getTfIdf() {
        String csvFile = "tfIdf.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";
        ArrayList<ArrayList<String>> value = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<Double>> doubleValue = new ArrayList<ArrayList<Double>>();

        try {
            //чтение и запись в ArrayList  tfIdf
            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.substring(line.indexOf(';') + 1);
                ArrayList<String> temp = new ArrayList(Arrays.asList(line.split(cvsSplitBy)));
                value.add(temp);
            }
            for (ArrayList<String> toDouble : value) {
                ArrayList<Double> temp = new ArrayList<Double>();
                for (int i = 0; i < toDouble.size(); i++) {
                    double d = Double.parseDouble(toDouble.get(i));
                    d = checkDoubleValue(d);
                    temp.add(d);
                }
                doubleValue.add(temp);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doubleValue;
    }

    public ArrayList<ArrayList<Double>> tfIdfCounter(ArrayList<String> searchWordsList) throws MyStemApplicationException {

        String csvFile = "tfIdf.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";

        ArrayList<ArrayList<String>> value = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<Double>> queryValue = new ArrayList<ArrayList<Double>>();

        try {
            //чтение и запись в ArrayList  tfIdf
            br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "windows-1251"));
            ArrayList<String> header = new ArrayList();

            line = br.readLine();
            if (line != null) {
                header = new ArrayList(Arrays.asList(line.split(cvsSplitBy)));
                header.remove(0);
            }

            while ((line = br.readLine()) != null) {
                line = line.substring(line.indexOf(';') + 1);
                ArrayList<String> temp = new ArrayList(Arrays.asList(line.split(cvsSplitBy)));
                value.add(temp);
            }

            for (int i = 0; i < searchWordsList.size(); i++) {
                //индек исковых слов
                int index = -1;
                MyStemming mystem = new MyStemming();
                final Iterable<Info> lemm =
                        JavaConversions.asJavaIterable(
                                mystem.mystemAnalyzer
                                        .analyze(Request.apply(searchWordsList.get(i)))
                                        .info()
                                        .toIterable());

                for (final Info info : lemm) {
                    String lemma = String.valueOf(info.lex());
                    lemma = lemma.replace("Some(", "");
                    lemma = lemma.replace(")", "");

                    index = header.indexOf(" " + lemma);
                }

                //расчет tfIdf искомых слов
                ArrayList<Double> queryValueLine = new ArrayList<Double>();
                for (ArrayList<String> pageValue : value) {
                    double d = Double.parseDouble(pageValue.get(index));
                    d = checkDoubleValue(d);
                    queryValueLine.add(d);
                }
                queryValue.add(queryValueLine);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryValue;
    }

    public double checkDoubleValue(double doubleValue) {
        if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
            doubleValue = 0.0;
        }
        return doubleValue;
    }

    public double cosineSimilarity(ArrayList<Double> docVector1, ArrayList<Double> docVector2) {
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        double cosineSimilarity = 0.0;

        for (int i = 0; i < docVector1.size(); i++) //docVector1 and docVector2 must be of same length
        {
            dotProduct += docVector1.get(i) * docVector2.get(i);  //a.b
            magnitude1 += Math.pow(docVector1.get(i), 2);  //(a^2)
            magnitude2 += Math.pow(docVector2.get(i), 2); //(b^2)
        }

        magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0;
        }
        return cosineSimilarity;
    }

    public LinkedHashMap<String, Double> sortHashMapByValues(HashMap<String, Double> passedMap) {
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if (o2 > o1) {
                    return 1;
                }
                if (o1 > o2) {
                    return -1;
                }
                return 0;
            }

            public int compare(Long o1, Long o2) {
                return o2.compareTo(o1);
            }
        });

        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();

        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            for (String o : passedMap.keySet()) {
                if (passedMap.get(o).equals(val)) {
                    sortedMap.put(o, val);
                }
            }

        }
        return sortedMap;
    }

}
