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
	
	private File outFolder = new File(".", "images");
	
	public Parameters(){
	}
	
	@Option(name = "-o", aliases = "--out_folder", usage = "The folder where to download images")
	public void setOutFolder(File value){
		this.outFolder = value;
	}
}
