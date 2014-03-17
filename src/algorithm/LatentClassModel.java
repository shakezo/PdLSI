package algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import util.PropertiesManager;

public class LatentClassModel {

	//ユーザIDとコード内IDの対応
	private HashMap<Integer,String> um = new HashMap<Integer,String>(); //userid
	private HashMap<Integer,String> im = new HashMap<Integer,String>(); //itemid
	private HashMap<Integer,String> lm = new HashMap<Integer,String>(); //lifestyleid

	//ユーザxのアイテムyの購入回数
	int[][] n;

	//潜在変数の数
	int nu; //ユーザ潜在クラス数
	int nv; //アイテム潜在クラス数

	//ユーザ数
	int nx ;
	//アイテム数
	int ny;

	//制約条件定数γ
	double[][] gamma;

	//ライフスタイルスコア
	double[][] lxu; //Luk #ユーザuのライフスタイルkのスコア

	//条件付き確率
	double[][] puv; //p(v|u)
	double[][] pvy; //p(y|v)
	double[][] pux; //p(x|u)
	double[][] pxu; //p(u|x) #制約条件から決定
	double[] px;    //p(x) #制約条件から決定
	double[] py;    //p(y)
	double[] pu;    //p(u) #制約条件から決定
	double[] pv;    //p(v)
	double[][][][] pxyuv; //p(u,v|x,y)

	public LatentClassModel() throws IOException{

		nx = PropertiesManager.N_USER; //ユーザ数設定
		ny = PropertiesManager.N_ITEM; //アイテム数設定
		nu = PropertiesManager.N_LIFESTYLE;//ライフスタイル数設定
		nv = PropertiesManager.N_CLASS; //潜在アイテム
		//pv,puv,py,pvy,pxyuv
		this.n = new int[nx][ny];
		this.pxyuv = new double[nx][ny][nu][nv];
		this.puv = new double[nu][nv];
		this.pvy = new double[nv][ny];
		this.pux = new double[nu][nx];//
		this.pxu = new double[nx][nu];//
		this.px  = new double[nx];//
		this.py  = new double[ny];
		this.pu  = new double[nu];//
		this.pv  = new double[nv];
		this.lxu = new double[nx][nu];
		this.gamma = new double [nx][nu];

		this.readData();
		this.setRand();
		this.setModelRestrict();

		System.out.println("初期値設定完了");
	}

	protected void readData() throws IOException{
		//購買ログデータの読込
		String indir = PropertiesManager.INDIR;
		//System.out.println("indir="+indir);
	    FileReader f = new FileReader(indir+"/purchase_log.csv");
		BufferedReader b = new BufferedReader(f);
		String s;

		//headerを飛ばす
		s=b.readLine();
		String[] item_id_list =s.split(",");
		for(int i =1;i<item_id_list.length;i++){
			im.put(i-1, item_id_list[i]);
		}

		int uid=0;
		int item_id;

		while((s = b.readLine())!=null){
			//購買ログデータの読込
			//配列に追加
			String[] p_cnt_ary = s.split(",");
			um.put(uid,p_cnt_ary[0]); //userIDとコード内idの対応
			for(int i=1;i<p_cnt_ary.length;i++){
				item_id= i-1;
				this.n[uid][item_id] = Integer.parseInt(p_cnt_ary[i]);
			}
			uid++;
		}
		b.close();

		//ライフスタイルデータの読込
	    FileReader f2 = new FileReader(indir+"/lifestyle_score.csv");
		BufferedReader b2 = new BufferedReader(f2);
		s = b2.readLine();
		String[] lifestyle_id_list =s.split(",");
		for(int i =1;i<lifestyle_id_list.length;i++){
			lm.put(i-1, lifestyle_id_list[i]);
		}
		uid = 0;
		int lsid; //lifestyle id
		while((s = b2.readLine())!=null){
			//System.out.println("s="+s);
			String[] l_score_ary = s.split(",");
			//System.out.println("numary="+l_score_ary.length);
			for(int i =1;i<l_score_ary.length;i++){
				lsid =i-1;
				this.lxu[uid][lsid] = Double.parseDouble(l_score_ary[i]);
				//System.out.println("lxu1="+this.lxu[uid][lsid]);
			}
			uid++;
		}
		b2.close();
	}

	//ライフスタイルスコアに基づく制約条件の設定
	protected void setModelRestrict(){
		System.out.println("p(u|x)の計算開始");
		//p(u|x)の計算
		double normalization_term_pu=0;
		for(int x=0;x<this.nx;x++){
			double normalization_term_pux =0;
			for(int u=0;u<this.nu;u++){
				normalization_term_pux += this.lxu[x][u];
				normalization_term_pu +=this.lxu[x][u];
			}
			//System.out.println("norm_pux="+normalization_term_pux);
			//p(uk|xi)の更新
			for(int u=0;u<this.nu;u++){
				this.pxu[x][u] =this.lxu[x][u]/normalization_term_pux;
				//System.out.println( "  x="+x+"  u="+u+ "  pxu= "+this.pxu[x][u]);
			}
		}
		System.out.println("p(u)の計算開始");
		//p(u)の計算
		for (int u =0; u<this.nu;u++){
			double lxu =0;
			for(int x=0;x<this.nx;x++){
				lxu+=this.lxu[x][u];
			}
			this.pu[u] = lxu/normalization_term_pu;
			//System.out.println("u=" + u+ "  this.pu="+this.pu[u] +"  lxu="+lxu);
		}

		//p(xi)の計算
		double normalization_term_px = 0;
		for(int x=0;x<this.nx;x++){
			for(int y=0; y<this.ny;y++){
				normalization_term_px +=this.n[x][y];
			}
		}
		for(int x=0;x<this.nx;x++){
			double nx =0;
			for(int y=0;y<this.ny;y++){
				nx += this.n[x][y];
			}
			this.px[x] =nx/normalization_term_px;
			//System.out.println("x="+ x +"  this.px="+this.px[x]);
 		}

		//p(x|u)の計算
		//p(x|u) =p(u|x)*p(x)/p(u)
		System.out.println("p(x|u)の計算開始");
		for(int x=0;x<this.nx;x++){
			for(int u=0;u<this.nu;u++){
				this.pux[u][x] =this.pxu[x][u]*this.px[x]/this.pu[u];
				//System.out.println("this.pux="+this.pux[u][x]);
			}
		}
		//γの計算
		System.out.println("γの計算開始");
		for(int u=0;u<this.nu;u++ ){
			for(int x=0;x<this.nx;x++){
				this.gamma[x][u] =this.pu[u]*this.pux[u][x];
				//System.out.println("this.gamma="+this.gamma[x][u]);
			}
		}
	}

	//pv,puv,py,pvy,pxyuv
	protected void setRand(){
		for(int u =0;u<this.nu;u++){
			for(int v=0;v<this.nv;v++){
				this.pv[v] = Math.random();
				this.puv[u][v]=Math.random();
				for(int x=0;x<this.nx;x++){
					for(int y=0;y<this.ny;y++){
						this.py[y]=Math.random();
						this.pvy[v][y]=Math.random();
						this.pxyuv[x][y][u][v] =Math.random();
					}
				}
			}
		}
	}

	public void train() throws IOException{
		double tmp=0;
		int k = PropertiesManager.N_REPEAT;
		double  limit= PropertiesManager.LIMIT;
		boolean writelog = PropertiesManager.WRITELOG;
		String  outdir = PropertiesManager.OUTDIR;
		//System.out.println(PropertiesManager.WRITELOG);
		
		for(int i=0;i<k;i++){
			this.e_step();
			this.m_step();
			double L = this.likelihood();
			if(i%1==0){
				System.out.println("パラメータ更新"+i+"回目 L="+L);
				if (writelog==true){
					FileWriter fw = new FileWriter(outdir+"/log.csv",true);
					fw.write("パラメータ更新"+i+"回目 L="+L+"\n");
					fw.close();
				}
			}
			if( Math.abs(L-tmp)<limit  ){
				break;
			}else{
				tmp = L;
			}
		}
	}

	//尤度計算
	protected double likelihood(){
		double ll = 0;

		for(int x=0;x<this.nx;x++){
			for(int y=0;y<this.ny;y++){
				double uv_ll=0;
				for(int u=0;u<this.nu;u++){
					for(int v=0;v<this.nv;v++){
						//System.out.println("ll="+ll);
						uv_ll+=this.pu[u]*this.pux[u][x]*this.puv[u][v]*this.pvy[v][y];
						//ll+=this.puv[u][v]*this.pvy[v][y] ;
						//ll+=this.puv[u][v]*this.pvy[v][y];
					}
				}
				ll+= this.n[x][y]*Math.log(uv_ll);
			}
		}
		return ll;
	}


	//Estep
	protected void e_step(){
		//System.out.println("Estep");
		//ystem.out.println("puv"+this.puv[0][0]+"  pvy[v][y]=" + pvy[1][1]);

		for(int x=0;x<this.nx;x++){
			for(int y=0;y<this.ny;y++){
				//正規化項
				double normalization_term_pxyuv=0;
				for(int u=0; u<this.nu;u++){
					for(int v=0;v<this.nv;v++){
						normalization_term_pxyuv += this.pu[u]*this.pux[u][x]*this.puv[u][v]*this.pvy[v][y];
					}
				}
				//System.out.println("x="+ x +"y="+ y +"  norm_pxyuv="+normalization_term_pxyuv);
				//p(u,v|x,y)の更新

				for(int u=0; u<this.nu;u++){
					for(int v=0;v<this.nv;v++){
						this.pxyuv[x][y][u][v] = this.pu[u]*this.pux[u][x]*this.puv[u][v]*this.pvy[v][y]/normalization_term_pxyuv;
						//System.out.println("pxyuv="+this.pxyuv[x][y][u][v]);
						//System.out.println("pxyuv="+this.pxyuv[x][y][u][v]);
					}
				}
			}
		}
		//System.out.println("pux="+this.pux[0][0]);
	}

	//Mstep  //p(x|u),p(v|u),p(v|u),p(u)の更新
	protected void m_step(){

		//p(u)は制約条件から固定値なので更新しない
		//p(x|u)は制約条件により固定値のため更新しない
		//*p(u|x)*p(x)=p(x|u)*p(u)からp(x|u) =p(u|x)*p(x)/p(u)

		//p(v|u)の更新ステップ
		//System.out.println("p(v|u)");
		for(int u=0;u<this.nu;u++){
			//正規化項の算出
			double normalization_term_puv=0;
			for(int v=0;v<this.nv;v++){
				for(int x=0;x<this.nx;x++){
					for(int y=0;y<this.ny;y++){
						normalization_term_puv +=this.n[x][y]*this.pxyuv[x][y][u][v];
						//System.out.println("norm_puv="+ normalization_term_puv);
					}
				}
			}
			//System.out.println("u="+ u+"  norm_puv="+ normalization_term_puv);
			//p(v|u)の更新
			for(int v=0;v<this.nv;v++){
				double pxyuv =0;
				for(int x=0;x<this.nx;x++){
					for(int y=0;y<this.ny;y++){
						pxyuv += this.n[x][y]*this.pxyuv[x][y][u][v];
					}
				}
				this.puv[u][v] = pxyuv/normalization_term_puv;
				//System.out.println("v="+v+"  this.puv="+ this.puv[u][v]);
			}
		}
		//p(y|v)
		//System.out.println("p(y|v)");
		for(int v=0;v<this.nv;v++){
			double normalization_term_pvy=0;
			for(int y=0;y<this.ny;y++){
				for(int x=0;x<this.nx;x++){
					for(int u=0; u<this.nu;u++){
						normalization_term_pvy +=this.n[x][y]*this.pxyuv[x][y][u][v];
					}
				}
			}
			//System.out.println("v="+v + "  norm_pvy="+ normalization_term_pvy);
			//p(y|v)の更新
			for(int y =0;y<this.ny;y++){
				double pxyuv=0;
				for (int x=0;x<this.nx;x++){
					for(int u=0;u<this.nu;u++){
						pxyuv +=this.n[x][y]*this.pxyuv[x][y][u][v];
					}
				}
				this.pvy[v][y] =pxyuv/normalization_term_pvy;
				//System.out.println("this.pvy="+"v="+v+"y="+y+"  "+  this.pvy[v][y]);
			}
		}
		//System.out.println("this.pvy="+this.pvy[0][0] +" this.puv=" + this.puv[0][0]);

	}

	public int[][] getN() {
		return n;
	}

	public int getNu() {
		return nu;
	}

	public int getNv() {
		return nv;
	}

	public int getNx() {
		return nx;
	}

	public int getNy() {
		return ny;
	}

	public double[][] getGamma() {
		return gamma;
	}

	public double[][] getLxu() {
		return lxu;
	}

	public double[][] getPuv() {
		return puv;
	}

	public double[][] getPvy() {
		return pvy;
	}

	public double[][] getPux() {
		return pux;
	}

	public double[][] getPxu() {
		return pxu;
	}

	public double[] getPx() {
		return px;
	}

	public double[] getPy() {
		return py;
	}

	public double[] getPu() {
		return pu;
	}

	public double[] getPv() {
		return pv;
	}

	public double[][][][] getPxyuv() {
		return pxyuv;
	}
	public HashMap<Integer, String> getUm() {
		return um;
	}

	public HashMap<Integer, String> getIm() {
		return im;
	}

	public HashMap<Integer, String> getLm() {
		return lm;
	}
}
