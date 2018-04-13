import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class Spider
{
    private static final String INDEX_FILE = "index.txt";
    private static final int MAX_PAGES_TO_SEARCH = 100;
    private Set<String> pagesVisited = new HashSet<String>();
    private List<String> pagesToVisit = new LinkedList<String>();
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links = new LinkedList<String>();

    public void crawler(String url) {
        while(this.pagesVisited.size() < MAX_PAGES_TO_SEARCH) {
            String currentUrl;
            if(this.pagesToVisit.isEmpty()) {
                currentUrl = url;
                this.pagesVisited.add(url);
            } else {
                currentUrl = this.nextUrl();
            }
            crawl(currentUrl);
            int i = 0;
            while (this.pagesVisited.size() < MAX_PAGES_TO_SEARCH && i < getLinks().size()) {
                this.pagesToVisit.add(getLinks().get(i));
                i++;
            }
        }
    }

    private void crawl(String url) {
        try {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();

            if(connection.response().statusCode() == 200) {
               getPage(url, Jsoup.parse(htmlDocument.body().html()).text(), pagesVisited.size());
            }
            if(!connection.response().contentType().contains("text/html")) {
                System.out.println("Retrieved something other than HTML");
                return;
            }
            Elements linksOnPage = htmlDocument.select("a[href]");
            for(Element link : linksOnPage)
            {
                this.links.add(link.absUrl("href"));
            }

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private List<String> getLinks()
    {
        return this.links;
    }

    private String nextUrl() {
        String nextUrl;
        do {
            nextUrl = this.pagesToVisit.remove(0);
        } while(this.pagesVisited.contains(nextUrl));

        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }

    private void getPage(String url, String content, int number) {
        try(FileWriter writer = new FileWriter("C:\\Users\\Регина\\Desktop\\Search\\GogolSearch\\visitedPage\\source\\" + number + ".txt", false)) {
            writer.write(content);
            writer.append('\n');
            writer.flush();

            try(FileWriter indexWriter = new FileWriter("C:\\Users\\Регина\\Desktop\\Search\\GogolSearch\\visitedPage\\" + INDEX_FILE, true);
                BufferedWriter bufferWriter = new BufferedWriter(indexWriter)) {
                bufferWriter.write(number + ":" + url);
                bufferWriter.append('\n');
                bufferWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }
}