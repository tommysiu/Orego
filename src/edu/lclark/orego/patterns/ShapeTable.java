package edu.lclark.orego.patterns;

import java.io.*;
import java.util.Arrays;

/** A class for storing win rates for pattern hashes. */
@SuppressWarnings("serial")
public final class ShapeTable implements Serializable{

	private final float scalingFactor = 0.99f;

	private final float[][] winRateTables;

	public ShapeTable() {
		winRateTables = new float[4][65536];
		for (float[] table : winRateTables) {
			Arrays.fill(table, 0.5f);
		}
	}
	
	public ShapeTable(String filePath){
		float[][] fake = null;
		 try (ObjectInputStream objectInputStream = new ObjectInputStream(
					new FileInputStream(filePath))) {
				fake = (float[][]) objectInputStream.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		 winRateTables = fake;
	}

	public void getRates(){
		try(PrintWriter writer = new PrintWriter(new File("test-books/patterns5x5.csv"))){
			for(float winRate : winRateTables[0]){
				writer.println(winRate + ",");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	float getScalingFactor() {
		return scalingFactor;
	}
	
	/** Get the win rate for a given pattern. */
	public float getWinRate(long hash) {
		float result = 0;
		for (int i = 0; i < 4; i++) {
			int index = (int) (hash >> (16 * i) & 65535);
			result += winRateTables[i][index];
		}
		return result / 4;
	}
	
	public float[][] getWinRateTables(){
		return winRateTables;
	}

	public double testGetRate(int index){
		return winRateTables[1][index];
	}

	/** Update the table with new win data for the given pattern. */
	public void update(long hash, boolean win) {
		for (int i = 0; i < 4; i++) {
			int index = (int) (hash >> (16 * i) & 65535);
			winRateTables[i][index] = win ? scalingFactor * winRateTables[i][index]
					+ (1 - scalingFactor) : scalingFactor * winRateTables[i][index];
		}
	}
}
