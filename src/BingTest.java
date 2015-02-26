import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

//Download and add this library to the build path.
import org.apache.commons.codec.binary.Base64;

public class BingTest {
	public final static int RESULTNUM = 10;
	public String accountKey;
	public String query;
	public double bar;
	public String bingUrl;

	BingTest(String accountKey, String query, double bar) {
		this.accountKey = accountKey;
		this.query = query;
		this.bar = bar;
	}

	public void startSearching() throws IOException {
		int ret = 0;
		int times = 0;
		double precision = 0;
		ArrayList<ArrayList<String>> docs = new ArrayList<ArrayList<String>>();
		docs.add(new ArrayList<String>());
		docs.add(new ArrayList<String>());
		while (precision < bar) {
			times++;
			if (times > 1)
				printFb(precision);
			reviseQuery(docs);
			docs.get(0).clear();
			docs.get(1).clear();

			// The content string is the xml/json output from Bing.
			String content = searchForContent();
			ret = parseAndDisplay(content, docs);
			if (ret == -2) {
				System.out.println("Invalid content.");
				return;
			}
			if (ret == -1) {
				System.out.println("Zero/Less than 10 search results.");
				return;
			}
			if (ret == 0) {
				System.out.println("No relevent search results.");
				return;
			}
			precision = ret * 1.0 / RESULTNUM; 
		}
		printFb(precision);
	}

	private String searchForContent() throws IOException {
		byte[] accountKeyBytes = Base64
				.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);
		String modifiedQuery = modifyQuery();
		bingUrl = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%27"
				+ modifiedQuery + "%27&$top=" + RESULTNUM + "&$format=Atom";
		URL url = new URL(bingUrl);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic "
				+ accountKeyEnc);

		InputStream inputStream = (InputStream) urlConnection.getContent();
		byte[] contentRaw = new byte[urlConnection.getContentLength()];
		inputStream.read(contentRaw);
		String content = new String(contentRaw);

		return content;
	}

	private String modifyQuery() {
		return query.replaceAll(" ", "%20");
	}

	private void testModifyQuery() {
		System.out.println(modifyQuery());
	}

	private void reviseQuery(ArrayList<ArrayList<String>> docs) {
		if (docs.get(0).isEmpty() && docs.get(1).isEmpty()) {
			return;
		}
		VectorSpaceModel vs = new VectorSpaceModel(query, docs);
		query = vs.reviseQuery();
	}

	private ArrayList<Integer> splitResults(String content) {
		ArrayList<Integer> index = new ArrayList<Integer>();

		int num = 0;
		int start = 0;
		int cur = content.indexOf("<m:properties>", start);
		while (cur != -1) {
			num++;
			index.add(cur);
			// Find the end of this result
			start = cur + "<m:properties>".length();
			cur = content.indexOf("</m:properties>", start);
			assert cur == -1;
			index.add(cur);
			// Find the next result
			start = cur + "</m:properties>".length();
			cur = content.indexOf("<m:properties>", start);
		}
		index.add(num);

		return index;
	}

	private void testSplitResults(String content) {
		ArrayList<Integer> index = splitResults(content);

		if (index.size() < 3) {
			System.out.println("Invalid index.");
			return;
		}
		if (index.get(index.size() - 1) == 0) {
			System.out.println("Zero search result.");
			return;
		}
		for (int i = 0; i < index.size() - 2; i += 2) {
			System.out.println(content.substring(index.get(i), index.get(i + 1)
					+ "</m:properties>".length()));
		}
		return;
	}

	private String[] splitItem(String item) {
		String[] res = new String[3];
		int start, end;
		start = item.indexOf("<d:Title m:type=\"Edm.String\">", 0);
		if (start == -1)
			return null;
		end = item.indexOf("</d:Title>", start);
		if (end == -1)
			return null;
		res[0] = item.substring(
				start + "<d:Title m:type=\"Edm.String\">".length(), end);

		start = item.indexOf("<d:Description m:type=\"Edm.String\">", end);
		if (start == -1)
			return null;
		end = item.indexOf("</d:Description>", start);
		if (end == -1)
			return null;
		res[1] = item.substring(
				start + "<d:Description m:type=\"Edm.String\">".length(), end);

		start = item.indexOf("<d:Url m:type=\"Edm.String\">", end);
		if (start == -1)
			return null;
		end = item.indexOf("</d:Url>", start);
		if (end == -1)
			return null;
		res[2] = item.substring(
				start + "<d:Url m:type=\"Edm.String\">".length(), end);

		return res;
	}

	private void testSplitItem(String item) {
		String[] res = splitItem(item);
		if (res == null) {
			System.out.println("Invalid search result.");
		}
		System.out.println("Title:\n" + res[0]);
		System.out.println("Description:\n" + res[1]);
		System.out.println("URL:\n" + res[2]);
	}

	private int parseAndDisplay(String content,
			ArrayList<ArrayList<String>> docs) {
		int sum = 0;

		ArrayList<Integer> index = splitResults(content);
		
		printPara((index.size() - 1) / 2);
		
		if (index.size() < RESULTNUM * 2 + 1) {
			return -1;
		}
		
		System.out.println("Bing Search Results:");
		System.out.println("======================");
		for (int i = 0; i < index.size() - 2; i += 2) {
			String[] res = splitItem(content.substring(index.get(i),
					index.get(i + 1) + "</m:properties>".length()));
			if (res == null) {
				return -2;
			}
			System.out.println("Result " + (i / 2 + 1));
			System.out.println("[\nURL: " + res[2]);
			System.out.println("Title: " + res[0]);
			System.out.println("Summary: " + res[1] + "\n]\n");
			int fb = getFeedback();
			sum += fb;
			docs.get(fb).add(res[0] + " " + res[0] + " " + res[1]);
			System.out.println();
		}
		return sum;
	}
	
	private void printFb(double precision) {
		System.out.println("======================");
		System.out.println("FEEDBACK SUMMARY");
		System.out.println("Query " + query);
		System.out.println("Precision " + precision);
		if (precision < bar)
			System.out.println("Still below the desired precision of " + bar);
		else
			System.out.println("Desired precision reached, done");
	}
	
	private void printPara(int num) {
		System.out.println("Parameters:");
		System.out.println("Client key  = " + accountKey);
		System.out.println("Query       = " + query);
		System.out.println("Precision   = " + bar);
		System.out.println("URL: " + bingUrl);
		System.out.println("Total no of results : " + num);
	}

	private int getFeedback() {
		java.util.Scanner input = new java.util.Scanner(System.in);
		System.out.print("Relevant (Y/N)? ");
		String s = input.next();
		if (s.charAt(0) == 'y' || s.charAt(0) == 'Y') {
			return 1;
		} else if (s.charAt(0) == 'n' || s.charAt(0) == 'N') {
			return 0;
		}
		System.out.println("Please check your input. (Y/N)");
		return getFeedback();
	}

	public static void main(String[] args) throws IOException {
		String accountKey = "5ZIHb6H/L4XPW0sE8LqFHfGYlyU1su2hafW5KLHjlT4";
		double bar = 0.9;
		String query = "musk";
//		String accountKey = args[0];
//		double bar = Double.parseDouble(args[1]);
//		String query = args[2];
		
		BingTest bing = new BingTest(accountKey, query, bar);

		bing.startSearching();
	}
}