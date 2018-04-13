import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyStemming {

    public final static MyStem mystemAnalyzer =
            new Factory("-igd --eng-gr --format json --weight")
                    .newMyStem("3.0", Option.<File>empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException {

        indexer();


    }

    public static void indexer() throws MyStemApplicationException {
        int i = 1;
        while (i <= 100) {
            index(i, "C:\\Users\\Регина\\Desktop\\Search\\GogolSearch\\visitedPage\\source\\\\" + i + ".txt");
            i++;
        }

    }

    private static void index(int number, String path) throws MyStemApplicationException {
        Scanner sc2 = null;
        FileWriter indexWriter = null;
        BufferedWriter bufferWriter = null;
        try {
            sc2 = new Scanner(new File(path));
            indexWriter = new FileWriter("C:\\Users\\Регина\\Desktop\\Search\\GogolSearch\\visitedPage\\lemma\\" + number + ".txt", true);
            bufferWriter = new BufferedWriter(indexWriter);
            while (sc2.hasNextLine()) {
                Scanner s2 = new Scanner(sc2.nextLine());
                while (s2.hasNext()) {
                    String s = s2.next();
                    Pattern p = Pattern.compile("[A-Za-zА-Яа-я0-9-]*");
                    Matcher m = p.matcher(s);
                    if (m.matches()) {
                        final Iterable<Info> result =
                                JavaConversions.asJavaIterable(
                                        mystemAnalyzer
                                                .analyze(Request.apply(s))
                                                .info()
                                                .toIterable());

                        for (final Info info : result) {
                            String lemma = String.valueOf(info.lex());
                            lemma = lemma.replace("Some(", "");
                            lemma = lemma.replace(")", "");
                            bufferWriter.write(lemma);
                            bufferWriter.append('\n');
                        }

                    }
                }
            }
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}