package fr.mrcraftcod.osuseasonalskins;

import fr.mrcraftcod.utils.http.requestssenders.get.BinaryGetRequestSender;
import fr.mrcraftcod.utils.http.requestssenders.get.StringGetRequestSender;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Thomas Couchoud (MrCraftCod - zerderr@gmail.com) on 2018-09-11.
 *
 * @author Thomas Couchoud
 * @since 2018-09-11
 */
public class Main{
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private static final Pattern TO_DOWNLOAD = Pattern.compile(".*\\.(jpg|png|jpeg)");
	
	public static void main(String[] args){
		final var parameters = new Parameters();
		final var parser = new CmdLineParser(parameters);
		try{
			parser.parseArgument(args);
		}
		catch(final Exception ex){
			parser.printUsage(System.out);
			return;
		}
		
		parameters.getOutFolder().mkdirs();
		
		final var articlePages = new ArrayList<String>();
		
		var pageHasContent = true;
		var page = 1;
		while(pageHasContent){
			LOGGER.info("Loading page {}", page);
			try{
				final var pageURL =  new URL("https://osu.ppy.sh/home/news?page=" + page);
				final var requestResult = new StringGetRequestSender(pageURL).getRequestResult();
				if(requestResult.getStatus() == 200){
					final var rootDocument = Jsoup.parse(requestResult.getBody());
					if(rootDocument.getElementsByClass("news-index-item").isEmpty()){
						pageHasContent = false;
						LOGGER.info("Page is empty");
					}
					else{
						final var results = rootDocument.getElementsByClass("news-index-item__title").stream()
								            .filter(elem -> !parameters.onlyWinners || elem.text().contains("Fanart Contest Results"))
								            .filter(elem -> elem.hasAttr("href"))
								            .map(elem -> elem.absUrl("href"))
								            .collect(Collectors.toList());
						LOGGER.info("{} contests results found on page {}", results.size(), page);
						articlePages.addAll(results);
					}
				}
				else{
					LOGGER.error("Response code for page {} was {}", page, requestResult.getStatus());
				}
			}
			catch(final Exception e){
			LOGGER.error("Error loading page {}", page ,e);
			}
			page++;
		}
		
		LOGGER.info("{} articles found and going to inspect them", articlePages.size());
		
		final var imageLinks = new ArrayList<String>();
		for(final var articleLink : articlePages){
			LOGGER.info("Processing page content: {}", articleLink);
			try{
				final var pageURL =  new URL(articleLink);
				final var requestResult = new StringGetRequestSender(pageURL).getRequestResult();
				if(requestResult.getStatus() == 200){
					final var rootDocument = Jsoup.parse(requestResult.getBody());
					final var result = rootDocument.getElementsByClass("osu-md__link").stream()
					            .map(elem -> elem.absUrl("href"))
					            .filter(link -> TO_DOWNLOAD.matcher(link).matches())
					            .collect(Collectors.toList());
					LOGGER.info("Found {} images", result.size());
					imageLinks.addAll(result);
				}
				else{
					LOGGER.error("Response code for page {} was {}", articleLink, requestResult.getStatus());
				}
			}
			catch(final Exception e){
				LOGGER.error("Error loading page content" ,e);
			}
		}
		
		LOGGER.info("{} images found and will be downloaded", imageLinks.size());
		
		for(final var imageLink : imageLinks){
			LOGGER.info("Processing image {}", imageLink);
			try{
				final var urlPaths = imageLink.split("/");
				final var fileName = urlPaths[urlPaths.length - 1];
				final var imageURL =  new URL(imageLink);
				final var requestResult = new BinaryGetRequestSender(imageURL).getRequestResult();
				if(requestResult.getStatus() == 200){
					final var finalStream = requestResult.getBody();
					try(finalStream){
						final File file = new File(parameters.getOutFolder(), fileName);
						LOGGER.info("Saving to {}, exists: {}", file.getAbsolutePath(), file.exists());
						if(file.exists())
						{
							LOGGER.warn("File {} already exists, skipping", file);
						}
						else{
							try(FileOutputStream fos = new FileOutputStream(file)){
								IOUtils.copy(finalStream, fos);
							}
						}
					}
					catch(final IOException e){
						e.printStackTrace();
					}
				}
				else{
					LOGGER.error("Response code for page {} was {}", imageLink, requestResult.getStatus());
				}
			}
			catch(final Exception e){
				LOGGER.error("Error downloading image",e);
			}
		}
	}
}
