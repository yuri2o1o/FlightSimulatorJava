package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ZScoreAlgo implements AnomalyDetectionAlgorithm {

	List<Float> txValues;
	public ZScoreAlgo()
	{
		txValues = new ArrayList<Float>();
	}
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

	public void learnNormal (File trainFile)
	{
		//here we calculate Tx
		TimeSeries ts = new TimeSeries(trainFile.toString());
		learnNormal(ts);
	}

	public void learnNormal (TimeSeries ts)
	{
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
	public List<AnomalyReport> detect (File testFile)
	{
		TimeSeries ts = new TimeSeries(testFile.toString());
		return detect(ts);
	}

	public List<AnomalyReport> detect (TimeSeries ts)
	{
		List<AnomalyReport> anomalies = new ArrayList<AnomalyReport>();
		float current = 0;
		for(int i = 0; i < ts.getValues().size(); i++)
		{
			for(int j = 0; j < ts.getValues().get(i).size(); j++)
			{
				//we check if current zscore is over the "not problematic" limit, calculated as Tx in learnNormal
				current = ZScore(ts,i,j);
				if(current > txValues.get(i))
					anomalies.add(new AnomalyReport(ts.getValueNames().get(i),(long)i+1,i+1,current)); //IMPORTANT!!! CHANGE TIMESTAMP, X,Y WHEN YOU KNOW WHAT TO PUT IN IT
			}
		}
		return anomalies;
	}

}
