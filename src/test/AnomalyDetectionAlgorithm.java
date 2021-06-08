package test;

import java.io.File;
import java.util.List;

public interface AnomalyDetectionAlgorithm {

	public void learnNormal(File trainFile);
	public List<AnomalyReport> detect(File testFile);
	public void drawOnGraph(String graphNodeName,String featureName, int timeStamp);
}
