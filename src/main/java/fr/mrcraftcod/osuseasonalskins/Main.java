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
import java.util.stream.Collectors;

/**
 * Created by Thomas Couchoud (MrCraftCod - zerderr@gmail.com) on 2018-09-11.
 *
 * @author Thomas Couchoud
 * @since 2018-09-11
 */
public class Main{
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
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
		
		final var contestResultPages = new ArrayList<String>();
		
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
								            .filter(elem -> parameters.winners && elem.text().contains("Fanart Contest Results"))
								            .filter(elem -> elem.hasAttr("href"))
								            .map(elem -> elem.attr("href"))
								            .collect(Collectors.toList());
						LOGGER.info("{} contests results found on page {}", results.size(), page);
						contestResultPages.addAll(results);
					}
				}
				else{
					LOGGER.warn("Response code for page {} was {}", page, requestResult.getStatus());
				}
			}
			catch(final Exception e){
			LOGGER.error("Error loading page {}", page ,e);
			}
			page++;
		}
		
		final var imageLinks = new ArrayList<String>();
		for(final var pageLink : contestResultPages){
			LOGGER.info("Processing page content");
			try{
				final var pageURL =  new URL(pageLink);
				final var requestResult = new StringGetRequestSender(pageURL).getRequestResult();
				if(requestResult.getStatus() == 200){
					final var rootDocument = Jsoup.parse(requestResult.getBody());
					final var result = rootDocument.getElementsByClass("osu-md__link").stream()
					            .map(elem -> elem.attr("href"))
					            .filter(link -> link.endsWith(".png"))
					            .collect(Collectors.toList());
					LOGGER.info("Found {} images", result.size());
					imageLinks.addAll(result);
				}
				else{
					LOGGER.warn("Response code for page {} was {}", pageLink, requestResult.getStatus());
				}
			}
			catch(final Exception e){
				LOGGER.error("Error loading page content" ,e);
			}
		}
		
		for(final var imageLink : imageLinks){
			LOGGER.info("Processing image");
			try{
				final var urlPaths = imageLink.split("/");
				final var fileName = urlPaths[urlPaths.length - 1];
				final var imageURL =  new URL(imageLink);
				final var requestResult = new BinaryGetRequestSender(imageURL).getRequestResult();
				if(requestResult.getStatus() == 200){
					final var finalStream = requestResult.getBody();
					try(finalStream){
						final File file = new File(parameters.getOutFolder(), fileName);
						LOGGER.info("Saving to {}", file.getAbsolutePath());
						try(FileOutputStream fos = new FileOutputStream(file)){
							IOUtils.copy(finalStream, fos);
						}
					}
					catch(final IOException e){
						e.printStackTrace();
					}
				}
				else{
					LOGGER.warn("Response code for page {} was {}", imageLink, requestResult.getStatus());
				}
			}
			catch(final Exception e){
				LOGGER.error("Error downloading image",e);
			}
		}
	}
}
