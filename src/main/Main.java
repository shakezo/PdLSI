package main;

import java.io.IOException;

import util.DataWriter;
import util.PropertiesManager;

import algorithm.LatentClassModel;

public class Main {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		new PropertiesManager();
		LatentClassModel lca =new LatentClassModel();
		lca.train(); //パラメータ推定

		System.out.println("データ出力開始");
		DataWriter dw = new DataWriter(lca);
		dw.write_puv();  //p(v|u)の出力
		dw.write_pvy();  //p(v|u)の出力
	}
}
