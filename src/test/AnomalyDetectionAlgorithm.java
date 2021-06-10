package test;

import java.io.File;
import java.util.List;

import javafx.scene.canvas.Canvas;

public interface AnomalyDetectionAlgorithm {

	public void learnNormal(File trainFile);
	public List<AnomalyReport> detect(File testFile);
	public void drawOnGraph(Canvas canvas,String featureName, int timeStamp);
}
