package org.terraform.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

public class Spline2D extends BasicSpline{
	   private Vector<Vector2f> points;
	   
	   private Vector<Cubic> xCubics;
	   private Vector<Cubic> yCubics;
	   
	   private static final String vector2DgetXMethodName = "getX";
	   private static final String vector2DgetYMethodName = "getY";
	   
	   private Method vector2DgetXMethod;
	   private Method vector2DgetYMethod;
	   
	   private static final Object[] EMPTYOBJ = new Object[] { };
	   
	   public Spline2D() {
	      this.points = new Vector<Vector2f>();
	   
	      this.xCubics = new Vector<Cubic>();
	      this.yCubics = new Vector<Cubic>();
	      
	      try {
	         vector2DgetXMethod = Vector2f.class.getDeclaredMethod(vector2DgetXMethodName, new Class[] { });
	         vector2DgetYMethod = Vector2f.class.getDeclaredMethod(vector2DgetYMethodName, new Class[] { });
	      } catch (SecurityException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      } catch (NoSuchMethodException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }      
	   }
	   
	   public void addPoint(Vector2f point) {
	      this.points.add(point);
	   }
	   
	   public Vector<Vector2f> getPoints() {
	      return points;
	   }
	   
	   public void calcSpline() {
	      try {
	            calcNaturalCubic(points, vector2DgetXMethod, xCubics);
	            calcNaturalCubic(points, vector2DgetYMethod, yCubics);
	      } catch (IllegalArgumentException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      } catch (IllegalAccessException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      } catch (InvocationTargetException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   
	   public Vector2f getPoint(float position) {
	      position = position * xCubics.size(); // extrapolate to the arraysize
	      int      cubicNum = (int) position;
	      float   cubicPos = (position - cubicNum);
	      
	      return new Vector2f(xCubics.get(cubicNum).eval(cubicPos),
	                     yCubics.get(cubicNum).eval(cubicPos));
	   }
	}