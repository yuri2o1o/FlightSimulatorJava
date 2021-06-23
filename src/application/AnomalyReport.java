package application;

public class AnomalyReport {
	public final String description;
	public final  long timeStamp;
	public float
	x,y;//coordinates in graph
	public AnomalyReport(String description, long timeStamp, float x, float current){
		this.description=description;
		this.timeStamp=timeStamp;
		this.x = x;
		this.y = current;
	}



}
