package plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class HybridAlgorithm implements AnomalyDetectionAlgorithm{
	private SimpleAnomalyDetector sad;//for alg 2
	private ZScoreAlgo zsa;//for alg 1
	private List<AnomalyReport> res;//final result
	private TimeSeries tsAlg1, tsAlg2, mainTs;//for each algorithm

	//ArrayLists below are to create a separate TimeSeries object for each algorithm, from the original TimeSeries object created
	//from the file
	public ArrayList<Point> circlePoints = new ArrayList<>();
	public ArrayList<CircleColumnPairing> arr = new ArrayList<CircleColumnPairing>();
	public ArrayList<String> namesForAlg2 = new ArrayList<String>();
	public ArrayList<String> namesForAlg1 = new ArrayList<String>();
	public ArrayList<ArrayList<Float>> forAlg2 = new ArrayList<ArrayList<Float>>();
	public ArrayList<ArrayList<Float>> forAlg1 = new ArrayList<ArrayList<Float>>();
	public ArrayList<String> namesBetween = new ArrayList<String>();
	public ArrayList<ArrayList<Float>> forAlgBetween = new ArrayList<ArrayList<Float>>();
	//parameters for high and low correlation
	public float highCorr, lowCorr;
	public HybridAlgorithm()
	{
		sad = new SimpleAnomalyDetector();
		zsa = new ZScoreAlgo();
		//default values
		highCorr = (float) 0.98;
		lowCorr = (float) 0.5;
	}

	public HybridAlgorithm(float highCorr,float lowCorr)
	{
		sad = new SimpleAnomalyDetector();
		zsa = new ZScoreAlgo();
		this.highCorr = highCorr;
		this.lowCorr = lowCorr;
	}

	public int findTheAlgo(String name) //determines which algorithm should be used on the feature "name"
	{
		float maxCorr = 0;
		for(int i = 0; i < sad.getCorrelated().size(); i++)
		{
			if((sad.getCorrelated().get(i).feature1.compareTo(name) == 0 || sad.getCorrelated().get(i).feature2.compareTo(name) == 0) && Math.abs(sad.getCorrelated().get(i).corrlation) > maxCorr)
				maxCorr = Math.abs(sad.getCorrelated().get(i).corrlation);
		}
		if(maxCorr >= highCorr)
			return 1;
		if(maxCorr < lowCorr)
			return -1;
		for(CorrelatedFeatures corr : sad.getCorrelated())
			if(corr.feature1.equals(name) || corr.feature2.equals(name))
				return 0;
		return -1;
	}

	public void learnNormal (File trainFile)//sends each TimeSeries file to learnNormal in it's specific algorithm
	{
		mainTs = new TimeSeries(trainFile.toString());
		sad.learnNormal(mainTs);
		for(int i = 0; i < mainTs.valueNames.size(); i++)
		{
			int res = findTheAlgo(mainTs.valueNames.get(i));
			if(res == 1)
			{
				namesForAlg2.add(mainTs.getValueNames().get(i));
				forAlg2.add(mainTs.getValues().get(i));
			}
			else if(res == -1)
			{
				namesForAlg1.add(mainTs.getValueNames().get(i));
				forAlg1.add(mainTs.getValues().get(i));
			}
			else
			{
				namesBetween.add(mainTs.getValueNames().get(i));
				forAlgBetween.add(mainTs.getValues().get(i));
			}
		}
		tsAlg1 = new TimeSeries(namesForAlg1,forAlg1);
		tsAlg2 = new TimeSeries(namesForAlg2,forAlg2);
		sad.learnNormal(tsAlg2);
		zsa.learnNormal(tsAlg1);
		//creating the circles for welzl's algorithm
		for(CorrelatedFeatures corr : sad.getCorrelated())
			if(namesBetween.contains(corr.feature1) && namesBetween.contains(corr.feature2))
				arr.add(new CircleColumnPairing(corr.c,corr.feature1,corr.feature2));
	}

	public List<AnomalyReport> detect(File testFile)//sending each TimeSeries created to its specific "Detect" function, on alg1 and 2
	{
		sad.detect(testFile);
		zsa.detect(testFile);
		
		//here we check for welzl's algorithm whether the circle created in learnNormal contains the current point. If not, an anomaly is in order
		res = new ArrayList<>();
		mainTs = new TimeSeries(testFile.toString());
		ArrayList<ArrayList<Float>> allFeatureOnes = new ArrayList<ArrayList<Float>>();
		ArrayList<ArrayList<Float>> allFeatureTwos = new ArrayList<ArrayList<Float>>();
		for(CircleColumnPairing ccp : arr)
		{
			allFeatureOnes.add(mainTs.getValues().get(mainTs.getValueNames().indexOf(ccp.ft1)));
			allFeatureTwos.add(mainTs.getValues().get(mainTs.getValueNames().indexOf(ccp.ft2)));
		}
		for(int i = 0; i < allFeatureOnes.size(); i++)
		{
			for(int j = 0; j < allFeatureOnes.get(i).size(); j++)
			{
				Point p = new Point(allFeatureOnes.get(i).get(j),allFeatureTwos.get(i).get(j));
				circlePoints.add(p);
				if(!arr.get(i).c.contains(p))
					res.add(new AnomalyReport(arr.get(i).ft1 + "-" + arr.get(i).ft2, i+1, allFeatureOnes.get(i).get(j),allFeatureTwos.get(i).get(j)));
			}
		}
		return res;
	}

	//draws on the JavaFX GUI's graph as a plugin to the program
	@Override
	public void drawOnGraph(Canvas canvas, String featureName, int timeStamp) {
		if(findTheAlgo(featureName) == 1)
			sad.drawOnGraph(canvas, featureName, timeStamp);
		else if(findTheAlgo(featureName) == -1)
			zsa.drawOnGraph(canvas, featureName, timeStamp);
		else
		{
			//draw all points in circlePoints until index timeStamp, xValue is p.x and yValue is p.y
			Circle circle = null;
			for(CircleColumnPairing cop : arr)
				if(cop.ft1.compareTo(featureName) == 0 || cop.ft2.compareTo(featureName) == 0)
					circle = cop.c;
			
			//get a Circle shape on a canvas that fits the sizes in circle - circle.c as center and circle.r as radius
			canvas.getGraphicsContext2D().setStroke(Color.BLACK);
			canvas.getGraphicsContext2D().strokeOval(Math.round(Math.abs(circle.c.x-circle.r)) % 200, Math.round(Math.abs(circle.c.y-circle.r)) % 200, (circle.r*2) % 200, (circle.r*2) % 200);
			//draw points
			canvas.getGraphicsContext2D().setStroke(Color.RED);
			for(Point  p : circlePoints)
				canvas.getGraphicsContext2D().strokeLine(p.x % 200, p.y % 200, p.x % 200, p.y % 200);
			
			ArrayList<AnomalyReport> list = new ArrayList<>();
			list.addAll(res);
			for(Iterator<AnomalyReport> iterator = list.iterator(); iterator.hasNext();) {
				AnomalyReport value = iterator.next();
				if(!value.description.contains(featureName) || value.timeStamp > timeStamp)
					iterator.remove();
			}

			//draw anomaly points
			for(AnomalyReport ar : list)
				canvas.getGraphicsContext2D().strokeLine(ar.x % 200, ar.y % 200, ar.x % 200, ar.y % 200);
		}
	}
	//returns the most correlated feature to the one given in the parameter "feature"
	@Override
	public String getCorrelated(String feature) {
		for (CorrelatedFeatures c : sad.getCorrelated()) {
			if (c.feature1.equals(feature))
				return c.feature2;
			if (c.feature2.equals(feature))
				return c.feature1;
		}
		return feature;
	}
}
