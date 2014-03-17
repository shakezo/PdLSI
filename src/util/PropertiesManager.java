package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {

	public static String INDIR;
	public static String OUTDIR;

	public static int N_CLASS;
	public static int N_USER;
	public static int N_ITEM;
	public static int N_LIFESTYLE;
	public static int N_REPEAT;
	public static double LIMIT;
	public static boolean WRITELOG;

	public  PropertiesManager() throws IOException{
		FileInputStream fis = new FileInputStream(new File("parameter.properties"));
		Properties props = new Properties();
		props.load(fis);
		fis.close();
		INDIR = props.getProperty("indir");
		OUTDIR = props.getProperty("outdir");
		N_CLASS = Integer.parseInt(props.getProperty("num_class"));
		N_ITEM  = Integer.parseInt(props.getProperty("num_item"));
		N_USER  = Integer.parseInt(props.getProperty("num_user"));
		N_LIFESTYLE = Integer.parseInt(props.getProperty("num_lifestyle"));
		N_REPEAT = Integer.parseInt(props.getProperty("num_repeat"));
		LIMIT = Double.parseDouble(props.getProperty("limit"));
		WRITELOG = Boolean.parseBoolean(props.getProperty("writelog"));
		FileWriter fw = new FileWriter(OUTDIR+"/log.csv");
		fw.close();
	}
}
