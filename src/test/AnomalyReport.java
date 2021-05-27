package test;

public class AnomalyReport {
	public final String description;
	public final  long timeStamp;
	public int x,y;//coordinates in graph
	public AnomalyReport(String description, long timeStamp, int x, int y){
		this.description=description;
		this.timeStamp=timeStamp;
		this.x = x;
		this.y = y;
	}

}
