import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseUdemy {

	public static int i = 1;
	public static DB db = new DB();

	public static String createCourseID(String text) {
		String id = "UDEMY";
		System.out.println("TEXT: "+text);
		for (String s : text.split(" ")) {
			id += s.charAt(0);
		}
		if(id.length()>45){
			return id.substring(0, 44);
		}
		System.out.println("ID "+ id);
		return id;
	}

	public static void insertIntoDB(String courseName, String courseDesc,
			ArrayList<String> categories, Double price) {
		String id = createCourseID(courseName);
		if (!doesExistCourseInDB(id)) {
			try {
				String sql = "INSERT INTO course(id,title, description, provder,price) "
						+ "VALUES (?, ?, ?, ?, ?)";
				PreparedStatement preparedStatement = db.conn
						.prepareStatement(sql);
				preparedStatement.setString(1, id);
				preparedStatement.setString(2, courseName);
				preparedStatement.setString(3, courseDesc);
				preparedStatement.setString(4, "UDEMY");
				preparedStatement.setDouble(5, price);
				preparedStatement.execute();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}

		for (String s : categories) {
			if (!doesExistKeywordInDB(s, id)) {
				try {
					String sql = "INSERT INTO keyword(value,course) "
							+ "VALUES (?, ?)";
					PreparedStatement preparedStatement = db.conn
							.prepareStatement(sql);
					preparedStatement.setString(1, s);
					preparedStatement.setString(2, id);
					preparedStatement.execute();
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				}
			}
		}

	}

	public static boolean doesExistCourseInDB(String id) {
		String checkSql = "select count(*) from course where id = '" + id
				+ "'";
		boolean flag = true;
		Statement st;
		try {
			st = db.conn.createStatement();
			ResultSet result = st.executeQuery(checkSql);
			result.next();
			if (result.getInt(1) == 0)
				flag = false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		return flag;
	}

	public static boolean doesExistKeywordInDB(String value, String course) {
		String checkSql = "select count(*) from keyword where value = '"
				+ value + "' and course = '" + course + "'";
		boolean flag = true;
		Statement st;
		try {
			st = db.conn.createStatement();
			ResultSet result = st.executeQuery(checkSql);
			result.next();
			if (result.getInt(1) == 0)
				flag = false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		return flag;
	}

	public static void parseCourse(String page, File file) throws IOException {

		Document docTmp = Jsoup.parse(file, "UTF-8");

		Elements courseName = docTmp.select("h1.course-title");
		Elements description = docTmp.select("div[data-more=Full details] >div>p");
		Elements categories = docTmp.select("span.cats>a");
		Elements languages=docTmp.select("span.list-left:contains(Languages)");
		Elements priceTxt=docTmp.select("div.price.fxac>span.current-price");
		
		Element language;
				
		Double price = 0.0;
	
		if (priceTxt.text().length()>1 && !priceTxt.text().equals("Free")){
			price=Double.parseDouble(priceTxt.text().substring(1));
		}
					
		ArrayList<String > categoriesArr=new ArrayList<String>();		

		for (Element e : categories) {
			categoriesArr.add(e.text());
		}

		if(!languages.isEmpty()){
			language = languages.first().nextElementSibling();
			if(language.text().matches("English|Spanish|German|French")){
				insertIntoDB(courseName.text(), description.text(), categoriesArr, price);
				System.out.println(courseName.text());
			}
		}
		

			

		/*	System.out.println("NAME: " + courseName.text());
			System.out.println("DESC: " + description.text());
			System.out.println("CATS: " + categoriesWithDelimiter);
			System.out.println("PRICE: "+price);
			System.out.println("LANGUAGE: "+language.text());
			System.out.println("\n");*/
			

		/*if (courseName.text().length() > 0 && !doesExistInDB(courseName.text())) {
			insertIntoDB(page, courseName.text(), categoriesWithDelimiter,
					description.text());
			System.out.println(i + ") NAME: " + courseName.text());
			i++;
			System.out.println(" ");
		}*/

	}

	public static void listFilesForFolder(final File folder) throws IOException {
		for (final File fileEntry : folder.listFiles()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(fileEntry));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String pagetmp = "";
			try {
				pagetmp = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String[] splits = pagetmp.split(" ");
			String page = splits[splits.length - 1];
			parseCourse(page, fileEntry);
		}
	}

	final static File folder = new File("D:\\Dropbox\\Courses\\UdemyCourses\\");

	public static void main(String[] args) throws SQLException, IOException {
		/*String sql = "INSERT INTO provder(id,name, description)"
				+ "VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = db.conn.prepareStatement(sql);
		preparedStatement.setString(1, "UDEMY");
		preparedStatement.setString(2, "Udemy");
		preparedStatement.setString(3,
				"Own your future by learning new skills online");
		preparedStatement.execute();*/

		listFilesForFolder(folder);
	}

}
