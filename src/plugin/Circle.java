/* 
 * Smallest enclosing circle - Library (Java)
 * 
 * Copyright (c) 2020 Project Nayuki
 * https://www.nayuki.io/page/smallest-enclosing-circle
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (see COPYING.txt and COPYING.LESSER.txt).
 * If not, see <http://www.gnu.org/licenses/>.
 */

package plugin;

import java.util.Collection;

final class Circle {
	private static final double MULTIPLICATIVE_EPSILON = 1 + 1e-14;
	
	
	public final Point c;   // Center
	public final double r;  // Radius
	
	
	public Circle(Point c, double r) {
		this.c = c;
		this.r = r;
	}
	
	
	public boolean contains(Point p) {
		return c.distance(p) <= r * MULTIPLICATIVE_EPSILON;
	}
	
	
	public boolean contains(Collection<Point> ps) {
		for (Point p : ps) {
			if (!contains(p))
				return false;
		}
		return true;
	}
	
	
	public String toString() {
		return String.format("Circle(x=%g, y=%g, r=%g)", c.x, c.y, r);
	}
	
}
