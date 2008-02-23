/*
  This file is a part of JOP, the Java Optimized Processor

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

main() {

	int i;
	for (i='0'; i<'0'+80; ++i) {
		printf("%c", i);
	}
	for (i='0'; i<'0'+80; ++i) {
		printf("%c", i+0x80);
	}
	printf("\n");
	for (i='0'; i<256; ++i) {
		printf("%c %d\n", i, i);
	}
}
