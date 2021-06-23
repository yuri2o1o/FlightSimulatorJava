package plugin;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import application.Utils;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
public class SimpleAnomalyDetector implements AnomalyDetectionAlgorithm {
	private ArrayList<CorrelatedFeatures> correlated;
	public int steps = 0;
	public List<AnomalyReport> reported;
	public float maxThreshold = 0.9f;
	public HashMap<CorrelatedFeatures,Line> map = new HashMap<>();
	private TimeSeries timeS;
	public SimpleAnomalyDetector()
	{
		correlated = new ArrayList<CorrelatedFeatures>();
		reported = new ArrayList<AnomalyReport>();
	}

	public ArrayList<CorrelatedFeatures> getCorrelated()
	{
		return correlated;
	}
	public void learnNormalTs(TimeSeries ts)
	{

		float[] firstVal, secondVal;
		float threshold = (float)0.9;
		int maxPearsonIndex = -1;
		float currPearson = 0, maxPearson = 0;

		for(int i = 0; i < ts.values.size(); i++)
		{
			maxPearsonIndex = -1;
			maxPearson = 0;
			firstVal = new float[ts.values.get(i).size()];
			for(int k = 0; k < firstVal.length; k++)
				firstVal[k] = ts.values.get(i).get(k).floatValue();

			for(int j = i + 1; j < ts.values.size(); j++)
			{
				secondVal = new float[ts.values.get(j).size()];
				for(int l = 0; l < secondVal.length; l++)
					secondVal[l] = ts.values.get(j).get(l).floatValue();
				if(i != j && (Math.abs(currPearson = plugin.StatLib.pearson(firstVal, secondVal)) > threshold))
				{
					if(Math.abs(currPearson) > Math.abs(maxPearson))
					{
						maxPearsonIndex = j;
						maxPearson = currPearson;
					}
				}
			}
			Point[] points = new Point[firstVal.length];
			if(maxPearsonIndex != -1)
			{
				for(int m = 0; m < points.length; m++)
					points[m] = new Point(firstVal[m],ts.values.get(maxPearsonIndex).get(m).floatValue());
				Line le = plugin.StatLib.linear_reg(points);
				CorrelatedFeatures cf = new CorrelatedFeatures(ts.getValueNames().get(i), ts.getValueNames().get(maxPearsonIndex),maxPearson ,le, maxDevFromLine(points, le),MinimalCircle.makeCircle(Arrays.asList(points)));
				correlated.add(cf);
				map.put(cf, le);
			}
		}


	}

	public float maxDevFromLine(Point[] points, Line l)
	{
		float max = 0, curr = 0;
		for(Point p : points)
		{
			if((curr = plugin.StatLib.dev(p, l)) > max)
				max = curr;
		}
		return max;
	}


	public List<AnomalyReport> detectTs(TimeSeries ts) {
		timeS = ts;

		int index1 = -1, index2 = -1;
		List<AnomalyReport> lst = new LinkedList<AnomalyReport>();
		for(int i = 0; i < ts.values.get(0).size(); i++)
		{
			for(int j = 0; j < correlated.size(); j++)
			{
				index1 = ts.getValueNames().indexOf(correlated.get(j).feature1);
				index2 = ts.getValueNames().indexOf(correlated.get(j).feature2);
				if(index1 != -1 && index2 != -1)
				{
					Point p = new Point(ts.values.get(index1).get(i),ts.values.get(index2).get(i));
					if(plugin.StatLib.dev(p, correlated.get(j).lin_reg) > (correlated.get(j).threshold + 0.35))
					{
						String s = String.join("-", correlated.get(j).feature1,correlated.get(j).feature2);
						lst.add(new AnomalyReport(s,i+1,ts.values.get(ts.valueNames.indexOf(correlated.get(j).feature1)).get(i+1),ts.values.get(ts.valueNames.indexOf(correlated.get(j).feature2)).get(i+1)));//remember to fill x,y when you know how to
					}
				}
			}
		}
		steps = ts.values.get(0).size();
		reported = lst;
		return lst;
	}

	public boolean detectOne(Point p)
	{
		for(int j = 0; j < correlated.size(); j++)
			if(plugin.StatLib.dev(p, correlated.get(j).lin_reg) > (correlated.get(j).threshold + 0.35))
				return true;
		return false;
	}
	public List<CorrelatedFeatures> getNormalModel(){
		return correlated;
	}
	public float[] anylizeTimesteps(String ts) {
		String s = "";
		int i = 1;
		while (i < reported.size())
		{
			long j = reported.get(i-1).timeStamp;
			long jj = j;
			while (i < reported.size() && reported.get(i).timeStamp == reported.get(i-1).timeStamp+1 && reported.get(i).description.equals(reported.get(i-1).description))
			{
				i++;
				jj++;
			}
			s += j + "," + jj + "\n";
			i++;
		}

		int FP = 0;
		int TP = 0;
		String[] str1 = ts.split("\n");
		String[] str2 = s.split("\n");
		int P = str1.length;
		int N = steps;

		for (int j = 0; j < str1.length; j++)
		{
			String[] data1 = str1[j].split(",");
			int[] inted1 = new int[data1.length];
			inted1[0] = Integer.parseInt(data1[0]);
			inted1[1] = Integer.parseInt(data1[1]);
			N -= (inted1[1] - inted1[0] + 1);
		}

		for (i = 0; i < str2.length; i++)
		{
			String[] data2 = str2[i].split(",");
			int[] inted2 = new int[data2.length];
			inted2[0] = Integer.parseInt(data2[0]);
			inted2[1] = Integer.parseInt(data2[1]);
			boolean fpflag = false;

			for (int j = 0; j < str1.length; j++)
			{
				String[] data = str1[j].split(",");
				int[] inted = new int[data.length];
				inted[0] = Integer.parseInt(data[0]);
				inted[1] = Integer.parseInt(data[1]);

				if ((inted2[0] <= inted[0] && inted2[1] >= inted[1]) || (inted2[0] >= inted[0] && inted2[0] <= inted[1]) || (inted2[1] >= inted[0] && inted2[1] <= inted[1]))
				{
					TP++;
					fpflag = true;
					break;
				}
			}

			if (!fpflag)
				FP++;
		}

		return new float[] { (float)TP/(float)P, (float)FP/(float)N };
	}

	@Override
	public void learnNormal(File trainFile) {
		// TODO Auto-generated method stub
		TimeSeries ts = new TimeSeries(trainFile.toString());
		learnNormalTs(ts);
	}

	@Override
	public List<AnomalyReport> detect(File testFile) {
		// TODO Auto-generated method stub
		TimeSeries ts = new TimeSeries(testFile.toString());
		return detectTs(ts);
	}

	@Override
	public void drawOnGraph(Canvas canvas, String featureName, Integer timeStamp) {
		// TODO Auto-generated method stub
		Line l = new Line(0,0);
		//draw the points
		ArrayList<Point> points = new ArrayList<Point>();
		int index = -1;
		for(int i = 0; i < correlated.size(); i++)
			if(correlated.get(i).feature1.compareTo(featureName) == 0 || correlated.get(i).feature2.compareTo(featureName) == 0)
				index = i;
		
		if (index == -1)
			return;

		for(int i = 0; i < timeStamp; i++)
			points.add(new Point(timeS.values.get(timeS.valueNames.indexOf(correlated.get(index).feature1)).get(i),timeS.values.get(timeS.valueNames.indexOf(correlated.get(index).feature1)).get(i)));

		for(Entry<CorrelatedFeatures, Line> e : map.entrySet())
		{
			if(e.getKey().feature1.compareTo(featureName) == 0 || e.getKey().feature2.compareTo(featureName) == 0)
				l = e.getValue();
		}

		//now draw line l
		ArrayList<AnomalyReport> list = new ArrayList<>();
		list.addAll(reported);
		for(Iterator<AnomalyReport> iterator = list.iterator(); iterator.hasNext();) {
			AnomalyReport value = iterator.next();
			if(!value.description.contains(featureName) || value.timeStamp > timeStamp)
				iterator.remove();
		}

		//stroke correlation line
		canvas.getGraphicsContext2D().setStroke(Color.BLACK);
		canvas.getGraphicsContext2D().strokeLine(1 % 200, l.f(1) % 200, timeStamp % 200, l.f(timeStamp) % 200);
		
		//stroke anomaly lines (via anomaly points)
		canvas.getGraphicsContext2D().setStroke(Color.RED);
	    for(Point p: points)
			canvas.getGraphicsContext2D().strokeLine(p.x % 200, p.y % 200, p.x % 200, p.y % 200);
	    for(AnomalyReport ar: list)
			canvas.getGraphicsContext2D().strokeLine(ar.x % 200, ar.y % 200, ar.x % 200, ar.y % 200);
	}

	@Override
	public String getCorrelated(String feature) {
		for (CorrelatedFeatures c : correlated) {
			if (c.feature1.equals(feature))
				return c.feature2;
			if (c.feature2.equals(feature))
				return c.feature1;
		}
		return feature;
	}
}
