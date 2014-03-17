package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import algorithm.LatentClassModel;

public class DataWriter {

	private LatentClassModel lcm;
	String outdir;

	public DataWriter(LatentClassModel lcm){
		this.lcm = lcm;
		outdir = PropertiesManager.OUTDIR;
	}

	public void write_pvy() throws IOException{
		FileWriter fw = new FileWriter(outdir+"/pvy.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		pw.println("item_class,item_id,p(y|v)");
		double[][] pvy = lcm.getPvy();
		for(int v=0;v<lcm.getNv();v++){
			for(int y=0;y<lcm.getNy();y++){
				String iname = lcm.getIm().get(y);
				pw.write(v+","+iname+","+pvy[v][y]+"\n");
			}
		}
		pw.close();
		bw.close();
		fw.close();
	}

	public void write_puv() throws IOException{
		FileWriter fw = new FileWriter(outdir+"/puv.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		pw.println("life_style,item_class,p(v|u)");
		double[][] puv = lcm.getPuv();
		for(int u=0;u<lcm.getNu();u++){
			for(int v=0;v<lcm.getNv();v++){
				String lname =lcm.getLm().get(u);
				pw.write(lname+","+v+","+puv[u][v]+"\n");
			}
		}
		pw.close();
		bw.close();
		fw.close();
	}
}
