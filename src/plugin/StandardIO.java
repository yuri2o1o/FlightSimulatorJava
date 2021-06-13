package plugin;

import plugin.Commands.DefaultIO;
public class StandardIO implements DefaultIO {

	@Override
	public String readText() 
	{
		
		return null;
	}

	@Override
	public void write(String text) {
		// TODO Auto-generated method stub
		System.out.println(text);
	}

	@Override
	public float readVal() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(float val) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
