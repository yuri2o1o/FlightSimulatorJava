package plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import application.Utils;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
//anomaly detection algorithm - Z-SCORE
public class ZScoreAlgo implements AnomalyDetectionAlgorithm {
	TimeSeries timeS;
	List<Float> txValues;
	ArrayList<ArrayList<Float>> allZscoreVals = new ArrayList<ArrayList<Float>>();
	public ZScoreAlgo()
	{
		txValues = new ArrayList<Float>();
	}
	//calaculates average of the feature in index col, from 0 to index until(current time in flight)
	public float calcAvg(TimeSeries ts, int col, int until)//calculating the average of a specific feature from start to index "until"
	{
		float avg = 0;
		for(int i = 0; i < until; i++)
		{
			avg += ts.getValues().get(col).get(i);
		}
		return avg / until;
	}

	public float[] cutArray(TimeSeries ts, int col, int until)//returns values in specific feature from index 0 to "until"
	{
		float[] arr = new float[until];
		for(int i = 0; i < until; i++)
		{
			arr[i] = ts.getValues().get(col).get(i);
		}
		return arr;
	}

	public float ZScore(TimeSeries ts, int col, int until)//calculates Z score for specific feature, from start to "until"
	{
		float avg = calcAvg(ts,col, until);
		return Math.abs(ts.values.get(col).get(until) - avg) / (float)Math.sqrt(StatLib.var(cutArray(ts,col,until)));
	}
	
	@Override
	public void learnNormal(File trainFile)
	{
		//here we calculate Tx
		TimeSeries ts = new TimeSeries(trainFile.toString());
		learnNormalTs(ts);
	}

	public void learnNormalTs(TimeSeries ts)
	{
		//we fill the zscore matrix
		for(int j = 0; j < ts.values.size(); j++)
		{
			allZscoreVals.add(new ArrayList<Float>());
			for(int i = 0; i < ts.getValues().get(j).size(); i++)
			{
				allZscoreVals.get(j).add(ZScore(ts,j,i));
			}
		}

		//here we calculate Tx, according to explanation in the manual
		float max = 0, curr = 0;
		for(int i = 0; i <ts.values.size(); i++)
		{
			max = 0; curr = 0;
			for(int j = 0; j < ts.values.get(i).size(); j++)
			{
				if((curr = ZScore(ts,i,j)) > max)
					max = curr;
			}
			txValues.add(max);
		}
	}
	
	//giving user option to detect with both csv file and timeseries object
	@Override
	public List<AnomalyReport> detect (File testFile)
	{
		TimeSeries ts = new TimeSeries(testFile.toString());
		return detectTs(ts);
	}

	public List<AnomalyReport> detectTs(TimeSeries ts)
	{
		timeS = ts;
		List<AnomalyReport> anomalies = new ArrayList<AnomalyReport>();
		float current = 0;
		for(int i = 0; i < ts.getValues().size(); i++)
		{
			for(int j = 0; j < ts.getValues().get(i).size(); j++)
			{
				//we check if current zscore is over the "not problematic" limit, calculated as Tx in learnNormal
				current = ZScore(ts,i,j);
				if(i < txValues.size() && current > txValues.get(i))
					anomalies.add(new AnomalyReport(ts.getValueNames().get(i),i+1,i+1,current));//IMPORTANT!!! CHANGE TIMESTAMP, X,Y WHEN YOU KNOW WHAT TO PUT IN IT
			}
		}
		return anomalies;
	}
	//function to draw on the JavaFX GUI - the graph drawing is a plugin to the program as well.
	@Override
	public void drawOnGraph(Canvas canvas, String featureName, Integer timeStamp) {
		ArrayList<Point> points = new ArrayList<Point>();
		for(int i = 0; i < timeStamp; i++)
			points.add(new Point(allZscoreVals.get(timeS.getValueNames().indexOf(featureName)).get(i), i+1));
		
		//stroke zscore lines (via points)
		canvas.getGraphicsContext2D().setStroke(Color.BLACK);
		for(int i = 0; i < timeStamp-1; i++)
			canvas.getGraphicsContext2D().strokeLine(points.get(i).x % 200, points.get(i).y % 200, points.get(i+1).x % 200, points.get(i+1).y % 200);
	}

	//no "most correlative feature" in Z-SCORE so it returns itself
	@Override
	public String getCorrelated(String feature) {
		return feature;
	}

}
