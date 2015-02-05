import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;

//Download and add this library to the build path.
import org.apache.commons.codec.binary.Base64;

public class BingTest {
	final static int RESULTNUM = 3;

	public static void main(String[] args) throws IOException {
		String query = "XBOX";
		double bar = 0.5;

		startSearching(query, bar);
	}

	private static void startSearching(String query, double bar)
			throws IOException {
		// Provide your account key here.
		String accountKey = "5ZIHb6H/L4XPW0sE8LqFHfGYlyU1su2hafW5KLHjlT4";
		byte[] accountKeyBytes = Base64
				.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		int ret = 0;
		int times = 0;
		double precision = 0;
		ArrayList<ArrayList<String>> docs = new ArrayList<ArrayList<String>>();
		docs.add(new ArrayList<String>());
		docs.add(new ArrayList<String>());
		while (precision < bar) {
			times++;
			String newQuery = reviseQuery(query, docs);
			docs.get(0).clear();
			docs.get(1).clear();
			// The content string is the xml/json output from Bing.
			String content = searchForContent(newQuery, accountKeyEnc);
			System.out.println("Query this time: " + query);
			ret = parseAndDisplay(content, docs);
			if (ret == -2) {
				System.out.println("Invalid content.");
				return;
			}
			if (ret == -1) {
				System.out.println("Zero/Less than 10 search results.");
				return;
			}
			precision = ret * 1.0 / RESULTNUM;
			System.out.println("-----Precision: "
					+ String.format("%1$.3f", precision) + "-----");
			System.out.println("------------------------------\n");
		}
	}

	private static String searchForContent(String query, String accountKeyEnc)
			throws IOException {
		String modifiedQuery = modifyQuery(query);
		String bingUrl = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%27"
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

	private static String modifyQuery(String query) {
		return query.replaceAll(" ", "%20");
	}

	private static void testModifyQuery(String query) {
		System.out.println(modifyQuery(query));
	}

	private static String reviseQuery(String query,
			ArrayList<ArrayList<String>> docs) {
		if (docs.get(0).isEmpty() && docs.get(1).isEmpty()) {
			return query;
		}
		return query;
	}

	private static ArrayList<Integer> splitResults(String content) {
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

	private static void testSplitResults(String content) {
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

	private static String[] splitItem(String item) {
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

	private static void testSplitItem(String item) {
		String[] res = splitItem(item);
		if (res == null) {
			System.out.println("Invalid search result.");
		}
		System.out.println("Title:\n" + res[0]);
		System.out.println("Description:\n" + res[1]);
		System.out.println("URL:\n" + res[2]);
	}

	private static int parseAndDisplay(String content,
			ArrayList<ArrayList<String>> docs) {
		int sum = 0;

		ArrayList<Integer> index = splitResults(content);
		if (index.size() < RESULTNUM * 2 + 1) {
			return -1;
		}
		for (int i = 0; i < index.size() - 2; i += 2) {
			String[] res = splitItem(content.substring(index.get(i),
					index.get(i + 1) + "</m:properties>".length()));
			if (res == null) {
				return -2;
			}
			System.out.println("-----Result " + (i / 2 + 1) + "-----");
			System.out.println("*Title:\n" + res[0]);
			System.out.println("*Description:\n" + res[1]);
			System.out.println("*URL:\n" + res[2] + "\n");
			int fb = getFeedback();
			sum += fb;
			docs.get(fb).add(res[0] + " " + res[1]);
			System.out.println();
		}
		return sum;
	}

	private static int getFeedback() {
		java.util.Scanner input = new java.util.Scanner(System.in);
		System.out.print("Is this document relevant to your query? (y/n) - ");
		String s = input.next();
		if (s.charAt(0) == 'y' || s.charAt(0) == 'Y') {
			return 1;
		} else if (s.charAt(0) == 'n' || s.charAt(0) == 'N') {
			return 0;
		}
		System.out.println("Please check your input. (y/n)");
		return getFeedback();
	}
}