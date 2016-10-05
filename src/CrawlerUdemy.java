import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerUdemy {
	public static int i = 1;
	// public static DB db = new DB();

	// Queue for BFS
	static Queue<String> q = new LinkedList<>();

	// URLs already visited
	static Set<String> markedCourses = new HashSet<>();
	static Set<String> visitedUrls = new HashSet<String>();

	// Start from here
	static String root = "https://www.udemy.com/courses/";

	public static void bfs() throws IOException {

		q.add(root);
		while (!q.isEmpty()) {
			String s = q.poll();
			visitedUrls.add(s);
			// markedCourses.add(s);

			// System.out.println("POLL: " +s);

			// if S is valid course
			// process, write, parse

			if (markedCourses.size() > 40000)
				return;
			// process page
			System.setProperty("java.net.useSystemProxies", "true");
			try {
				Connection con = Jsoup.connect(s).timeout(50000)
						.ignoreHttpErrors(true);
				Document doc = con.maxBodySize(0).get();

				// process page (add urls to queue)
				Elements questions = doc.select("a[href]");
				for (Element link : questions) {
					String url = link.attr("abs:href");
					
					if(url.contains("?dtcode")|| url.contains("?xref=wish")){
						url=url.substring(0,url.lastIndexOf("/"));
						//System.out.println("PROBA: "+url);
					}	
					
					
					if (url.contains("www.udemy.com") && !q.contains(url)
							&& !visitedUrls.contains(url)) {
							q.add(url);
							// System.out.println("ADD TO QUEUE:" + url);
						}
						
				}
				// check if course is valid
				if (validCoruse(s) && !markedCourses.contains(s)) {
					System.out.println(i + " Course: " + s);
					i++;
					writePageToFile(doc, s);
					// ++parse page
					

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean validCoruse(String url) {
		String[] words = { "#", "%", "https://www.udemy.com/courses/", "/?next=", 
				"https://www.udemy.com/course/preview-subscribe",
				"https://www.udemy.com/terms/", "https://www.udemy.com/gift/", "/subscribe", "/popup",
				"https://www.udemy.com/feedback",
				"https://www.udemy.com/payment", "https://www.udemy.com/user",
				"https://www.udemy.com/wishlist/", "https://www.udemy.com/affiliate/", "https://www.udemy.com/topics/",
				"https://www.udemy.com/new-lecture/", "www.udemy.com/support/", "https://www.udemy.com/blog/",
				"https://www.udemy.com/mobile/" , "https://www.udemy.com/careers/" , "https://www.udemy.com/static"};
		for (int i = 0; i < words.length; i++) {
			if (url.contains(words[i])) {
				return false;
			}
		}

		return true;

	}

	public static void writePageToFile(Document page, String url) {

		//String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
		//if (fileName.contains("?")) {
		String tmpName = url.substring(0, url.lastIndexOf("/"));
			// System.out.println("TMP: "+tmpName);
		String	fileName = tmpName.substring(tmpName.lastIndexOf("/") + 1,
					tmpName.length());
		//}
		
		System.out.println("FILE NAME: " + fileName);

		File file = new File("D:\\Dropbox\\Courses\\Udemy\\" + fileName
				+ ".txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("PAGE URL: " + url);
				bw.newLine();
				bw.write("START HTML PAGE:");
				bw.newLine();
				bw.write(page + "/n");
				System.out.println("WRITEN: " + url);
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("FILE EXISTS: " + url);
		}
		markedCourses.add(url);
	}

	public static void main(String[] args) throws IOException, SQLException {
		bfs();

	}

}
