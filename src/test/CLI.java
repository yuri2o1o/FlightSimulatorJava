package test;

import java.util.ArrayList;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;
	
	public CLI(DefaultIO dio, int id) {
		this.dio=dio;
		c=new Commands(dio,id); 
		commands=new ArrayList<>();
		commands.add(c.new UploadCSVCommand());
		commands.add(c.new ThresholdCommand());
		commands.add(c.new RunAlgorithmCommand());
		commands.add(c.new PrintReportsCommand());
		commands.add(c.new CommandFive());
	}
	
	private void display()
	{
		dio.write("Welcome to the Anomaly Detection Server.\n");
		dio.write("Please choose an option:\n");
		
		int i = 0;
		for (i = 0; i < commands.size(); i++)
			dio.write((i+1) + ". " + commands.get(i).description + "\n");
		
		dio.write((i+1) + ". exit\n");
	}
	
	public void start() {
		while (true)
		{
			display();
			int input = Integer.parseInt(dio.readText());
			if (input == commands.size()+1)
			{
				dio.write("bye\n");
				break;
			}
			commands.get(input - 1).execute();
		}
	}
}

