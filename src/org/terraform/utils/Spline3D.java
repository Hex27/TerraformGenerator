package org.terraform.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

public class Spline3D extends BasicSpline{
	   private Vector<Vector3f> points;
	   
	   private Vector<Cubic> xCubics;
	   private Vector<Cubic> yCubics;
	   private Vector<Cubic> zCubics;
	   
	   private static final String vector3DgetXMethodName = "getX";
	   private static final String vector3DgetYMethodName = "getY";
	   private static final String vector3DgetZMethodName = "getZ";
	   
	   private Method vector2DgetXMethod;
	   private Method vector2DgetYMethod;
	   private Method vector2DgetZMethod;
	   
	   private static final Object[] EMPTYOBJ = new Object[] { };
	   
	   public Spline3D() {
	      this.points = new Vector<Vector3f>();

	      this.xCubics = new Vector<Cubic>();
	      this.yCubics = new Vector<Cubic>();
	      this.zCubics = new Vector<Cubic>();
	      
	      try {
	         vector2DgetXMethod = Vector3f.class.getDeclaredMethod(vector3DgetXMethodName, new Class[] { });
	         vector2DgetYMethod = Vector3f.class.getDeclaredMethod(vector3DgetYMethodName, new Class[] { });
	         vector2DgetZMethod = Vector3f.class.getDeclaredMethod(vector3DgetZMethodName, new Class[] { });
	      } catch (SecurityException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      } catch (NoSuchMethodException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }      
	   }
	   
	   public void addPoint(Vector3f point) {
	      this.points.add(point);
	   }
	   
	   public Vector<Vector3f> getPoints() {
	      return points;
	   }
	   
	   public void calcSpline() {
	      try {
	            calcNaturalCubic(points, vector2DgetXMethod, xCubics);
	            calcNaturalCubic(points, vector2DgetYMethod, yCubics);
	            calcNaturalCubic(points, vector2DgetZMethod, zCubics);
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
	   
	   public Vector3f getPoint(float position) {
	      position = position * xCubics.size();
	      int      cubicNum = (int) position;
	      float   cubicPos = (position - cubicNum);
	      
	      return new Vector3f(xCubics.get(cubicNum).eval(cubicPos),
	                     yCubics.get(cubicNum).eval(cubicPos),
	                     zCubics.get(cubicNum).eval(cubicPos));
	   }
	}