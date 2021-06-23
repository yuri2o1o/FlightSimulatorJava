package application;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javafx.scene.canvas.Canvas;

/*
 * This class uses reflection in order to cast a plugin class to an application.AnomalyDetectionAlgorithm, assuming that is implements one.
 * Using this method, we can issue the cast from ANY object, thereby avoiding integration issues when loading plugins from an external source.
 */
public class PluginLoader implements AnomalyDetectionAlgorithm {
	Object plugin;
	
	Method[] methods;
	Method _learnNormal;
	Method _getCorrelated;
	Method _detect;
	Method _drawOnGraph;
	
	public PluginLoader(Object nplugin) {
		plugin = nplugin;
		try {
			methods = plugin.getClass().getMethods();
			_learnNormal = findMethodByName("learnNormal");
			_learnNormal.setAccessible(true);
			_getCorrelated = findMethodByName("getCorrelated");
			_getCorrelated.setAccessible(true);
			_detect = findMethodByName("detect");
			_detect.setAccessible(true);
			_drawOnGraph = findMethodByName("drawOnGraph");
			_drawOnGraph.setAccessible(true);
		} catch (SecurityException e) {}
	}
	
	private Method findMethodByName(String name) {
		for (Method m : methods)
			if (m.getName().equals(name))
				return m;
		return null;
	}
	
	@Override
	public void learnNormal(File trainFile) {
		try { _learnNormal.invoke(plugin, trainFile); } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
	}

	@Override
	public String getCorrelated(String feature) {
		try { return (String)_getCorrelated.invoke(plugin, feature); } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { return null; }
	}

	@Override
	public List<AnomalyReport> detect(File testFile) {
		try { return (List<AnomalyReport>)_detect.invoke(plugin, testFile); } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { return null; }
	}

	@Override
	public void drawOnGraph(Canvas canvas, String featureName, Integer timeStamp) {
		try { _drawOnGraph.invoke(plugin, canvas, featureName, timeStamp); } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { e.printStackTrace(); }
	}
	
}