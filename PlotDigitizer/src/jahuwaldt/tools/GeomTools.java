/*
 *   GeomTools  -- Utility class containing methods for working with geometry.
 *
 *   Copyright (C) 2003-2014 by Joseph A. Huwaldt.  All rights reserved.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  Or visit:  http://www.gnu.org/licenses/lgpl.html
 */
package jahuwaldt.tools;


import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;


/**
 * A set of generic utilities for working with geometry in Java applications.
 * 
 * <p> * Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: December 23, 2003
 * @version April 14, 2014
 */
public class GeomTools {

	/**
	*  Code used to indicate that two lines do NOT intersect in "linesIntersect()".
	*/
	public static final int kDontIntersect = 0;

	/**
	*  Code used to indicate that two lines intersect in "linesIntersect()".
	*/
	public static final int kDoIntersect = 1;

	/**
	* Code used to indicate that two lines are colinear and intersect at multiple points in "linesIntersect()".
	*/
	public static final int kColinear = 2;


	/**
	*  Prevent anyone from instantiating this utility class.
	*/
	private GeomTools() {}


	/**
	*  Compute the distance from a point to a line or line segment.  If the target point is
	*  between the end points of the line, then the perpendicular distance to
	*  the line is returned.  Otherwise, if the "infinite" flag is false, the distance to the
	*  nearest end point is returned.  If the "infinite" flag is true, then the perpendicular
	*  distance to the infinite extension of a line through p1 and p2 is returned.  If the
	*  points p1 and p2 are coincident (equal), then the distance to that collapsed line (point)
	*  is returned.
	*
	*  @param  p1  One end point of the line segment.
	*  @param  p2  The other end point of the line segment.
	*  @param  p   The target point for computing the distance to the line segment from.
	*  @param  infinite  If false, the distance to the line segment is calculated (which
	*                     may be the distance to one of the end points).  If true, the
	*                     perp. distance to an infinite extension of the line through p1 & p2
	*                     is returned.
	*  @return The minimum signed distance from the point "p" to the line segment p1,p2 or
	*          the minimum signed distance from the point "p" to the infinite line through p1,p2.
	*          A positive value means p3 is to the right of the line, negative to the left.  If
	*          you only care about the distance, and not the sign, just wrap a call to this
	*          method in Math.abs().
	*/
	public static double distPointToLine(Point2D p1, Point2D p2, Point2D p3, boolean infinite) {

		//  Extract coordinates.
		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double x3 = p3.getX();
		double y3 = p3.getY();

		//  For algorithm, see:  Comp.Graphics.Algorithms FAQ, Section 1.02
		//	http://www.faqs.org/faqs/graphics/algorithms-faq/
		double x21 = x2 - x1;
		double y21 = y2 - y1;
		double x31 = x3 - x1;
		double y31 = y3 - y1;
		double L2 = x21*x21 + y21*y21;

		//	Check for a collapsed line case.
		if (x21 == 0 && y21 == 0)
			return Math.sqrt(x31*x31 + y31*y31);

		if (!infinite) {
			double rL2 = (x21*x31 + y21*y31);
			if (rL2 < 0)
				//  Either the point is on the backward extension of p1-p2.
				return Math.sqrt(x31*x31 + y31*y31);

			if (rL2 > L2) {
				//  The point is on the forward extension of p1-p2.
				double x32 = x3 - x2;
				double y32 = y3 - y2;
				return Math.sqrt(x32*x32 + y32*y32);
			}
		}

		//  The point is between p1 and p2 or the line is infinite.
		double s = (y21*x31 - x21*y31) / L2;

		//	Return the signed distance.
		return s*Math.sqrt(L2);
	}


	/**
	*  Method that returns the index of the closest Point2D in a list of
	*  Point2D objects to the input point.
	*
	*  @param  pointLst  A list of Point2D objects to be searched.
	*  @param  aPoint    The point that we want the closest point for.
	*  @return The index to the closest point in the list to the input point.
	*           Returns -1 if there are no points in the input point list.
	*/
	public static int closestPoint2D(List<? extends Point2D> pointLst, Point2D aPoint) {

		int index = -1;
		double minDist2 = Double.MAX_VALUE;
		int size = pointLst.size();

		for (int i=0; i < size; ++i) {
			Point2D point = pointLst.get(i);
			double dist2 = aPoint.distanceSq(point);
			if (dist2 < minDist2) {
				minDist2 = dist2;
				index = i;
			}
		}

		return index;
	}


	/**
	*  Method that returns the index of the Point2D object, in a list of
	*  Point2D objects, at the start of the line segment between adjacent points which
	*  is closest to the input point.
	*
	*  @param  pointLst  A list of Point2D objects assumed to be connected by straight lines to be searched.
	*  @param  aPoint    The point that we want the closest line segment start point for.
	*  @return The index to the point in the list at the start of the closest line segment to the input point.
	*           Returns 0 if there is only a single point in the list.  Returns -1 if there are no points in
	*           the input point list.
	*/
	public static int closestLineSeg2D(List<? extends Point2D> pointLst, Point2D aPoint) {

		int index = -1;
		double minDist2 = Double.MAX_VALUE;
		int size = pointLst.size();
		if (size == 1)  index = 0;
		--size;

		for (int i=0; i < size; ++i) {
			Point2D p = pointLst.get(i);
			Point2D pp = pointLst.get(i+1);
			double dist2 = Math.abs(distPointToLine(p,pp, aPoint, false));
			if (dist2 < minDist2) {
				minDist2 = dist2;
				index = i;
			}
		}

		return index;
	}


	/**
	*  Method that determines whether two line segments intersect.  If the lines intersect,
	*  the coordinates of the output point are set to the coordinates of the point of the
	*  intersection.
	*
	* @param x1 The X-component of the 1st end-point of the 1st line segment.
    * @param y1 The Y-component of the 1st end-point of the 1st line segment.
    * @param x2 The X-component of the 2nd end-point of the 1st line segment.
    * @param y2 The Y-component of the 2nd end-point of the 1st line segment.
	* @param x3 The X-component of the 1st end-point of the 2nd line segment.
    * @param y3 The Y-component of the 1st end-point of the 2nd line segment.
    * @param x4 The X-component of the 2nd end-point of the 2nd line segment.
    * @param y4 The Y-component of the 2nd end-point of the 2nd line segment.
	* @param output        A Point2D object that will be set to the coordinates of the
	*                      intersection of the two lines (to the nearest integer).  Pass null
	*                      to skip this calculation.
	*  @return The value returned is one of the following constants: kDontIntersect, kDoIntersect or
	*           kColinear.  Colinear is treated as a non-intersection (no output point is computed).
	*/
	public static int linesIntersect(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, Point2D output) {

		//  Reference:  Graphics Gems III, Academic Press, "insectc.c", Author:  Franklin Antonio

		int Ax = x2 - x1;
		int Bx = x3 - x4;

		//  X bound box test.
		int x1lo, x1hi;
		if (Ax < 0) {
			x1lo = x2;
			x1hi = x1;
		} else {
			x1hi = x2;
			x1lo = x1;
		}
		if (Bx > 0) {
			if (x1hi < x4 || x3 < x1lo) return kDontIntersect;

		} else {
			if (x1hi < x3 || x4 < x1lo) return kDontIntersect;
		}

		int Ay = y2 - y1;
		int By = y3 - y4;

		//  X bound box test.
		int y1lo, y1hi;
		if (Ay < 0) {
			y1lo = y2;
			y1hi = y1;
		} else {
			y1hi = y2;
			y1lo = y1;
		}
		if (By > 0) {
			if (y1hi < y4 || y3 < y1lo) return kDontIntersect;
		} else {
			if (y1hi < y3 || y4 < y1lo) return kDontIntersect;
		}

		int Cx = x1 - x3;
		int Cy = y1 - y3;
		int d = By*Cx - Bx*Cy;		//  alpha numerator
		int f = Ay*Bx - Ax*By;		//  both denominator
		if (f > 0) {
			if (d < 0 || d > f) return kDontIntersect;
		} else {
			if (d > 0 || d < f) return kDontIntersect;
		}
		if (f == 0) return kColinear;

		if (output != null) {
			//  Compute intersection coordinates.
			int num = d*Ax;								//  numerator
			int offset = sameSigns(num,f) ? f/2 : -f/2; //  round direction
			int x = x1 + (num + offset)/f;				//  intersection x

			num = d*Ay;
			offset = sameSigns(num,f) ? f/2 : -f/2;
			int y = y1 + (num + offset)/f;				//  intersection y

			output.setLocation(x, y);
		}

		return kDoIntersect;
	}

	/**
	*  Method that determines if two numbers have the same sign.
	*/
	private static boolean sameSigns(int a, int b) {
		return (a ^ b) >= 0;
	}


	/**
	*  Method that calculates the intersection of an infinite line through points p1 & p2 and a
	*  circle centered at cp with radius r.
	*
	*  @param  p1   A point on a line.
	*  @param  p2   The other point on a line.
	*  @param  cp   The center of the circle.
	*  @param  r    The radius of the circle.
	*  @param  points  An array of 2 Point2D objects that will be filled in with the intersections.
	*                  If null is passed, a new array is created, if the elements are null, new
	*                  Point2D objects are created.
	*  @return A reference to the input array of Point2D objects is returned.  If there are no intersections,
	*           null is returned and the input array points are not modified.  If there is a single intersection
	*           (line is tangent to circle), then both points in the array will have the same coordinates.
	*/
	public static Point2D[] lineCircleIntersect(Point2D p1, Point2D p2, Point2D cp, double r, Point2D[] points) {
		return lineCircleIntersect(p1.getX(), p1.getY(), p2.getX(), p2.getY(), cp.getX(), cp.getY(), r, points);
	}


	/**
	*  Method that calculates the intersection of an infinite line through points p1 & p2 and a
	*  circle centered at cp with radius r.
	*
	* @param x1 The X-component of the 1st end-point of a line segment.
    * @param y1 The Y-component of the 1st end-point of a line segment.
	* @param x2 The X-component of the 2nd end-point of a line segment.
    * @param y2 The Y-component of the 2nd end-point of a line segment.
	* @param cx The X-component of the center of the circle.
    * @param cy The Y-component of the center of the circle.
	* @param r  The radius of the circle.
	* @param points  An array of 2 Point2D objects that will be filled in with the intersections.
	*                If null is passed, a new array is created, if the elements are null, new
	*                Point2D objects are created.
	*  @return A reference to the input array of Point2D objects is returned.  If there are no intersections,
	*           null is returned and the input array points are not modified.  If there is a single intersection
	*           (line is tangent to circle), then both points in the array will have the same coordinates.
	*/
	public static Point2D[] lineCircleIntersect(double x1, double y1, double x2, double y2, double cx, double cy, double r, Point2D[] points) {
		//  Reference:  Mathematics from: Circle-Line Intersection at MathWorld, Wolfram Research.
		//  http://mathworld.wolfram.com/Circle-LineIntersection.html

		//	Center the circle at the origin.
		x1 -= cx;
		x2 -= cx;
		y1 -= cy;
		y2 -= cy;

		double dx = x2 - x1;
		double dy = y2 - y1;
		double dr2 = dx*dx + dy*dy;
		double D = x1*y2 - x2*y1;

		//  Calculate the discriminant.
		double delta = r*r*dr2 - D*D;

		if (delta < 0)  return null;			//  No intersection.

		//  Check output array.
		if (points == null)
			points = new Point2D[2];
		if (points[0] == null)
			points[0] = new Point2D.Double();
		if (points[1] == null)
			points[1] = new Point2D.Double();

		if (delta == 0) {
			//  Handle tangent case.
			double x = D*dy/dr2 + cx;
			double y = -D*dx/dr2 + cy;

			points[0].setLocation(x,y);
			points[1].setLocation(x,y);

		} else {
			//  Handle two point intersection case.
			x1 = D*dy;
			double radical = Math.sqrt(delta);
			x2 = sgn(dy)*dx*radical;
			double x = (x1 + x2)/dr2 + cx;
			y1 = -D*dx;
			y2 = Math.abs(dy)*radical;
			double y = (y1 + y2)/dr2 + cy;
			points[0].setLocation(x,y);

			x = (x1 - x2)/dr2 + cx;
			y = (y1 - y2)/dr2 + cy;
			points[1].setLocation(x,y);
		}

		return points;
	}

	/**
	*  Returns -1 for x < 0, 1 for x >= 0.
	*/
	private static double sgn(double a) {
		return (a < 0 ? -1 : 1);
	}


	/**
	*  <p>
	*  Method that detects corners in a a list of Point2D objects which represent a planar curve.
	*  The input array "sValues" is filled in with the sharpness of a corner detected at each point.
	*  If no corner is detected at a point, that entry in "sValues" is filled in with (float)Math.PI.
	*  The opening angle of a triangle fit to each corner can be calculated as:
	*    alpha[i] = (float)Math.PI - sValue[i];.  </p>
	*
	*  <p>
	*  Uses the IPAN99 algorithm described in: Chetverinkov, D., Szabo, Z.,
	*  "A Simple and Efficient Algorithm for Detection of High Curvature Points
	*  in Planar Curves", Proc. 23rd Workshop of Austrian Pattern Recognition Group,
	*  Steyr, pp. 175-184, 1999.  </p>
	*
	*  @param  points  A list of Point2D objects making up a planar curve.
	*  @param  sValues An existing array of floats of size points.size().  The
	*                  sharpness of a corner applied to each point will be stored here.
	*  @param  dmin2   The minimum distance to consider squared.
	*  @param  dmax2   The maximum distance to consider squared.
	*  @param  amax    The maximum opening angle to consider (in radians).
	*  @return A count of the number of corners detected in the curve.
	*/
	public static int detectCorners(List<? extends Point2D> points, float[] sValues, double dmin2, double dmax2, double amax) {

		//	Loop over all the points in this curve and
		//	calculate the opening angle of the sharpest triangle
		//	that will fit the curve at that point.
		int numPntsm1 = points.size() - 1;
		for (int j=1; j < numPntsm1; ++j) {

			//	Return the sharpness of the sharpest triangle fit
			float sharpness = (float)fitTriangle(points, j, dmin2, dmax2, amax);
			sValues[j] = sharpness;
		}

		//	Remove spurious corners.
		int cornerCount = removeAdjacentCorners(points, sValues, dmin2);

		return cornerCount;
	}

	/**
	*  Method that fits a series of triangles to a list of points
	*  at the specified index and returns the sharpness of the sharpest
	*  triangle.
	*
	*  @param  points  A list of Point2D objects making up a planar curve.
	*  @param  ip      Index to the point for the tip of the triangle.  Can not be
	*                  outside range 1 to points.length-2.
	*  @param  dmin2   The minimum distance to consider squared.
	*  @param  dmax2   The maximum distance to consider squared.
	*  @param  amax    The maximum opening angle to consider (in radians).
	*  @return The sharpness of the sharpest triangle found is returned.
	*/
	private static double fitTriangle(List<? extends Point2D> points, int ip, double dmin2, double dmax2, double amax) {

		double sharpest = Math.PI;

		Point2D p = points.get(ip);
		int ipm = ip - 1;
		int ipp = ip + 1;
		boolean firstPass = true;
		boolean notDone = true;
		while (notDone) {
			//  Get end points of the triangle.
			Point2D pm = points.get(ipm);
			Point2D pp = points.get(ipp);
			double a2 = p.distanceSq(pp);
			double b2 = p.distanceSq(pm);

			//	Check criteria.
			boolean c1 = a2 > dmin2;
			boolean c2 = (a2 < dmax2) || firstPass;
			boolean c3 = b2 > dmin2;
			boolean c4 = (b2 < dmax2) || firstPass;
			if (c1 && c2 && c3 && c4) {
				firstPass = false;

				//  Calculate the opening angle of the triangle.
				double alpha = calcAlpha( a2, b2, pp.distanceSq(pm) );

				if (alpha < amax) {
					//	Calculate sharpness for this triangle.
					double sharpness = Math.PI - Math.abs(alpha);
					if (sharpness < sharpest)
						sharpest = sharpness;

				} else
					notDone = false;

			}

			//  If we are not further away than the tolerance, then try another triangle.
			if (c2 && c4) {
				++ipp;
				--ipm;
				if (ipp > points.size()-1)	ipp = points.size()-1;
				if (ipm < 0)	notDone = false;

			} else
				notDone = false;

		}

		return sharpest;
	}

	/**
	*  Method that calculates a triangle opening angle (the angle at the tip
	*  of the triangle that has legs of length "a", and "b" and a base of length "c").
	*  Returns the opening angle in radians.
	*/
	private static double calcAlpha(double a2, double b2, double c2) {
		double ab = Math.sqrt(a2*b2);
		double den = 2*ab;
		double num = a2 + b2 - c2;
		return Math.acos(num/den);
	}

	/**
	*  Method that removes adjacent corners.  It searches through the provided array
	*  of corners and removes any that are closer together than dmin2.
	*
	*  @param  points   A list of Point2D objects representing the points in a curve.
	*  @param  sValues  An array of sharpness values for each point in the point list.
	*  @param  tol2     The square of the tolerance to use on determining if corners are
	*                   adjacent.
	*  @return A count of the number of corners remaining in the curve.
	*/
	private static int removeAdjacentCorners(List<? extends Point2D> points, float[] sValues, double tol2) {

		int cornerCount = 0;
		int numPntsm1 = points.size() - 1;

		for (int j=1; j < numPntsm1; ++j) {
			float sharpness = sValues[j];
			if (sharpness < (float)Math.PI) {
				//	Potential corner found.
				++cornerCount;

				//	Does it have any sharper neighbors.
				int pp = j+1;
				Point2D pointj = points.get(j);

				while(pp < numPntsm1) {

					//	Make sure we aren't to far away from "j".
					double d2 = pointj.distanceSq(points.get(pp));
					if (d2 >= tol2)	break;

					if (sValues[pp] < sharpness) {

						//	Sharper neighbor found, discard corner "j".
						sValues[j] = (float)Math.PI;
						--cornerCount;
						break;

					} else
						//	If it is not sharper than "j", then discard it.
						sValues[pp] = (float)Math.PI;

					//	Move on to the next point.
					++pp;
				}
			}
		}

		return cornerCount;
	}


	/**
	*  Method that returns the signed area of an arbitrary simple polygon.  A simple polygon
	*  is one where the lines defining the edges never intersect.
	*
	*  @param  points  An array of 2D ordered points that define the perimeter of the polygon.
	*                  The last point will automatically be connected to the 1st point for this
	*				   calculation.  If the points are ordered counter-clockwise, then the
	*                  area will be positive, otherwise the area will be negative.
    * @return the signed area of the simple polygon.
	*/
	public static double simplePolygonArea(Point2D[] points) {

		//  For algorithm, see:  Comp.Graphics.Algorithms FAQ, Section 2.01
		//	http://www.faqs.org/faqs/graphics/algorithms-faq/
		//  2 A(P) = sum_{i=0}^{n-1} ( x_i  (y_{i+1} - y_{i-1}) )

		double area = 0;

		//  Determine the minimum X coordinate value.
		double xmin = Double.MAX_VALUE;
		int size = points.length;
		for (int i=0; i < size; ++i) {
			double x = points[i].getX();
			if (x < xmin)
				xmin = x;
		}

		double xi, yip1;
		double yim1 = points[size-1].getY();
		Point2D pti = points[0];
		double yi = pti.getY();

		for (int ip1=1; ip1 < size; ++ip1) {
			xi = pti.getX() - xmin;
			Point2D ptip1 = points[ip1];
			yip1 = ptip1.getY();

			area += xi*(yip1 - yim1);

			yim1 = yi;
			yi = yip1;
			pti = ptip1;
		}
		xi = pti.getX() - xmin;
		yip1 = points[0].getY();

		area += xi*(yip1 - yim1);

		return area*0.5;
	}


    /**
    *  Generates a B-spline curve of the specified order that passes through the input array of 2D points.
    *
    *  <p> Reference:  Rogers, D.F., Adams, J.A.,
    *                  _Mathematical_Elements_For_Computer_Graphics_,
    *                  McGraw-Hill, 1976, pg 144, 226.
    *  </p>
    *
    *  @param  points   The array of inputs points to fit the spline through:  points[0..n-1]
	*  @param  order	The order of the b-spline basis function.
	*  @param  np       The target number of points on the spline to place in the output array.
	*					The actual number of points output will be slightly different.
	*  @return An array of less than np 2D points that lie on the spline.
    */
	public static Point2D[] bspline(Point2D[] points, int order, int np) {
		int A = points.length - 1;				//  Number of polygon verticies minus 1.
		double[][] N = new double[A+order][A+order];
		ArrayList<Point2D> outputArr = new ArrayList();

		//  Generate the knot vectors.
		int B = A - order + 2;
		int[] x = knots(points, B, order);

		for (int w=order-1; w <= order+B; ++w) {
			int end = B+(order-1)*2;
			for (int i=0; i < end; ++i) {				//  Increment knot vector subscript.
				//  Check for a geometric knot.
				if (i != w || x[i] == x[i+1])
					N[i][1] = 0;						//  Calculate values for N[i,1].
				else
					N[i][1] = 1;
			}   //  Next i

			double tend = x[w+1] - x[A+order]/(double)(np-1);
			double step = x[A+order]/(double)(np-1);
			for (double t=x[w]; t <= tend; t += step) {

				double g = 0;
				double h = 0;
				for (int k=2; k <= order; ++k) {			//  Calc. values of N[i,k].

					for (int i=0; i <= A; ++i) {
						double d = 0;
						double e = 0;
						if (N[i][k-1] != 0)
							d = ((t - x[i])*N[i][k-1])/(x[i+k-1] - x[i]);		//  1st term in Eq. 5-78.
						if (N[i+1][k-1] != 0)
							e = ((x[i+k] - t)*N[i+1][k-1])/(x[i+k] - x[i+1]);   //  2nd term.
						N[i][k] = d + e;				//  Equation 5-78.
						g += points[i].getX()*N[i][k];  //  X-component of P(t)
						h += points[i].getY()*N[i][k];  //  Y-component of P(t)
					}   //  Next i

					if (k == order) break;
					g = 0;
					h = 0;
				}   //  Next k

				Point2D R = new Point2D.Double(g,h);
				outputArr.add(R);
			}   //  Next t
		}   //  Next w

		outputArr.add((Point2D)points[A].clone());
		Point2D[] arr = new Point2D[outputArr.size()];
		return outputArr.toArray(arr);
	}


	/**
	*  Method used by "bspline()" to generate the knots of the basis spline.
	*/
	private static int[] knots(Point2D[] V, int B, int C) {
		int end = B + (C-1)*2 + 1;
		int[] x = new int[end];
		for (int i=0; i < end; ++i) {
			if (i <= C-1) {					//  Assure multiplicity of degree C.
				x[i] = 0;					//  Assign multiple end knot vectors.

			} else if (i >= B+C) {			//  Check if end knot vectors reached.
				x[i] = x[i-1];				//  Assign multiple or duplicate knots.

			} else {
				//  Check for repeating verticies.
				if (V[i-C].getX() != V[i-C+1].getX() || V[i-C].getY() != V[i-C+1].getY())
					x[i] = x[i-1] + 1;		//  Assign successive internal vectors.
				else
					x[i] = x[i-1];			//  Assign multiple or duplicate knots.
			}
		}
		return x;
	}



	/**
	*  Some test code.
	*/
    public static void main (String args[]) {

		//  Test bspline.
		System.out.println("Testing bspline():");
		Point2D[] verticies = {new Point2D.Double(1,.1), new Point2D.Double(2.24,.134), new Point2D.Double(3,.374),
			new Point2D.Double(3.5,.314), new Point2D.Double(3.9,0.4),
			new Point2D.Double(4,.36), new Point2D.Double(5,.538), new Point2D.Double(6,.59), new Point2D.Double(7,.567)};

		System.out.println("Input verticies:");
		System.out.println("X\tY");
		for (int i=0; i < verticies.length; ++i) {
			System.out.println((float)(verticies[i].getX()) + "\t" + (float)(verticies[i].getY()));
		}

		Point2D[] output = bspline(verticies, 3, 10);

		System.out.println("Output curve points:");
		System.out.println("X\tY");
		for (int i=0; i < output.length; ++i) {
			System.out.println((float)(output[i].getX()) + "\t" + (float)(output[i].getY()));
		}
	}

}


