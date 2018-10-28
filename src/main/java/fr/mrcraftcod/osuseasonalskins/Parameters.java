package fr.mrcraftcod.osuseasonalskins;

import org.kohsuke.args4j.Option;
import java.io.File;

/**
 * Created by Thomas Couchoud (MrCraftCod - zerderr@gmail.com) on 01/09/2018.
 *
 * @author Thomas Couchoud
 * @since 2018-09-01
 */
public class Parameters{
	public File getOutFolder(){
		return outFolder;
	}
	
	@Option(name = "-o", aliases = "--out_folder", usage = "The folder where to download images")
	public File outFolder = new File(".", "images");
	
	@Option(name = "-w", aliases = "--winners", usage = "Sets if only the winning contests are downloaded (default: true)")
	public boolean winners = true;
}
