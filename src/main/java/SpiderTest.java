import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;

public class SpiderTest
{

    public static void main(String[] args) throws MyStemApplicationException {
        CosineSimilarity cs = new CosineSimilarity();
        cs.cosineSimilarity("оспа");
    }
}