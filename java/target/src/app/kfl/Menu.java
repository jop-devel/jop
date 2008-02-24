/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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

package kfl;

/**
*	Menu functions.
*/

public class Menu {

	private static final int MNU_CNT = 12;

	public static void doit() {

		int val;
		int sel = 0;

		show(sel);

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.UP) {
					--sel;
				} else if (val==Keyboard.DOWN) {
					++sel;
				} else if (val==Keyboard.C) {
					Display.cls();
					return;
				} else if (val==Keyboard.E) {
					exec(sel);
					return;

				}
				if (sel<0) sel += MNU_CNT;			// geht modulo nicht richtig????
				sel %= MNU_CNT;
				show(sel);
			}

			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static boolean pin() {

		int i, val;
		boolean ret = true;

		Display.cls();
		Display.line1(Texte.pin);
		Display.line2();
		for (i=0; i<8; ++i) {
			Display.data(' ');
		}
		
		for (i=0; i<4;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				++i;
				Display.data('*');
				val = Keyboard.rd();
				if (val==Keyboard.K1) {
					if (i!=1) ret = false;
				} else if (val==Keyboard.K2) {
					if (i!=2) ret = false;
				} else if (val==Keyboard.K3) {
					if (i!=3) ret = false;
				} else if (val==Keyboard.K4) {
					if (i!=4) ret = false;
				} else {
					ret = false;
				}
			}

			Timer.wd();
			Timer.waitForNextInterval();
		}
				

		return ret;
	}

	private static void exec(int sel) {

		if (sel!=9) {			// no pin for logbook
			if(!pin()) return;
		}
		Display.cls();

		if (sel==0) {
			setAuto();
		} else if (sel==1) {
			Debug.download();
		} else if (sel==2) {
			Debug.echo();
		} else if (sel==3) {
			setDate();
		} else if (sel==4) {
			Debug.testStatus();
		} else if (sel==5) {
			setMScnt();
		} else if (sel==6) {
			serviceUp();
		} else if (sel==7) {
			serviceDown();
		} else if (sel==8) {
			setLang();
		} else if (sel==9) {
			logbook();
		} else if (sel==10) {
			backZs();
		} else if (sel==11) {
			restZs();
		}
		Display.cls();
	}

	private static void show(int sel) {

		Display.line1(Texte.menu);
		if (sel==0) {
			Display.line2(Texte.automatik);
		} else if (sel==1) {
			Display.line2(Texte.download);
		} else if (sel==2) {
			Display.line2(Texte.echo1);
		} else if (sel==3) {
			Display.line2(Texte.clock);
		} else if (sel==4) {
			Display.line2(Texte.comTest);
		} else if (sel==5) {
			Display.line2(Texte.anzahl);
		} else if (sel==6) {
			Display.line2(Texte.justageUnten);
		} else if (sel==7) {
			Display.line2(Texte.justageOben);
		} else if (sel==8) {
			Display.line2(Texte.sprache);
		} else if (sel==9) {
			Display.line2(Texte.logbuch);
		} else if (sel==10) {
			Display.line2(Texte.backZs);
		} else if (sel==11) {
			Display.line2(Texte.restZs);
		}
	}

		
/**
*	display two lines and wait for key.
*/
	public static void msg(int[] l1, int[] l2) {

		int val;

		Display.line1(l1);
		Display.line2(l2);

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.E) {
					Display.cls();
					return;
				}
			}
			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	public static void msgMast(int ms, int[] l2) {

		int val;

		Display.line1(Texte.mast, ms);
		Display.line2(l2);

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.E) {
					Display.cls();
					return;
				}
			}
			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static void setDisplayForDate() {

		Display.line1();
		Display.data('2');
		Display.data('0');
		Display.data('_');
		Display.data('_');
		Display.data('-');
		Display.data('_');
		Display.data('_');
		Display.data('-');
		Display.data('_');
		Display.data('_');
		Display.data(' ');
		Display.data('_');
		Display.data('_');
		Display.data(':');
		Display.data('_');
		Display.data('_');
		Display.data(':');
		Display.data('0');
		Display.data('0');
		Display.line1();
		Display.data('2');
		Display.data('0');
	}
	private static void chkDisplayForDate(int cnt) {

		if (cnt==4 || cnt==6) {
			Display.data('-');
		} else if (cnt==8) {
			Display.data('-');
		} else if (cnt==10) {
			Display.data(':');
		}
	}

//	private static void setDate() {
public static void setDate() {			// fuer Testbetrieb

		int val;
		int cnt = 2;

		int y = 2000;
		int m = 0;
		int d = 0;
		int h = 0;
		int n = 0;


		setDisplayForDate();

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.C) {
					return;
				} else if (val==Keyboard.E && cnt==12) {
					Clock.setDate(y, m, d);
					Clock.setTime(h, n, 0);
					return;
				} else {
					val = Keyboard.num(val);
					if (val>=0 && cnt<12) {
						++cnt;
						Display.data('0'+val);
						if (cnt==3) {
							y += val*10;
						} else if (cnt==4) {
							y += val;
						} else if (cnt==5) {
							m = val*10;
						} else if (cnt==6) {
							m += val;
						} else if (cnt==7) {
							d = val*10;
						} else if (cnt==8) {
							d += val;
						} else if (cnt==9) {
							h = val*10;
						} else if (cnt==10) {
							h += val;
						} else if (cnt==11) {
							n = val*10;
						} else {
							n += val;
						}
						chkDisplayForDate(cnt);
					}
				}
			}

			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static void setAuto() {

		int val;
		boolean newAuto = Zentrale.auto;

		Display.line1(Texte.automatik);
		if (newAuto) {
			Display.line2(Texte.ein);
		} else {
			Display.line2(Texte.aus);
		}

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.C) {
					return;
				} else if (val==Keyboard.E) {
					Zentrale.auto = newAuto;
					Log.setAuto(newAuto);
					return;
				} else if (val==Keyboard.UP || val==Keyboard.DOWN) {
					newAuto = !newAuto;
					if (newAuto) {
						Display.line2(Texte.ein);
					} else {
						Display.line2(Texte.aus);
					}
				}
			}

			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static void disLang(int sel) {

		Display.line1(Texte.sprache);
		if (sel==0) {
			Display.line2(Texte.deutsch);
		} else if (sel==1) {
			Display.line2(Texte.englisch);
		}
	}


	private static void displayLog(int nr) {

		int i;

		Display.cls();
		Display.intVal(nr);
		int addr = Log.getAddr(nr);
		Display.data(' ');
		i = Log.getSec(addr);
		i = i%3600;
		Display.intVal(i/60);
		Display.data(':');
		Display.intVal(i%60);
		Display.data(' ');

		i = Log.getAction(addr);
		if (i==Log.ERROR) {
			i = Log.getMsnr(addr);
			if (i!=0) {
				Display.data(Texte.msshort, i);
			} else {
				Display.data(Texte.error);
			}
			i = Log.getErrnr(addr);
			Display.line2(Texte.errTxt(i));

		} else {

			if (i==Log.UP_STARTED) {
				Display.line2(Texte.goesUp);
			} else if (i==Log.DOWN_STARTED) {
				Display.line2(Texte.goesDown);
			} else if (i==Log.IS_UP) {
				Display.line2(Texte.isUp);
			} else if (i==Log.IS_DOWN) {
				Display.line2(Texte.isDown);
			} else if (i==Log.STOP) {
				Display.line2(Texte.stop);
			} else if (i==Log.NOTSTOP) {
				Display.line2(Texte.notaus);
			}
		}

	}

	private static void logbook() {

		int val;
		int nr = Log.findLastNr();
		int addr;

		if (nr<0) return;
		addr = Log.getAddr(nr);
		displayLog(nr);

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.C) {
					return;
				} else if (val==Keyboard.UP) {
					if (Log.getAddr(nr+1)>=0) ++nr;
				} else if (val==Keyboard.DOWN) {
					if (Log.getAddr(nr-1)>=0) --nr;
				}
				displayLog(nr);
			}

			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static void setLang() {

		int val;
		int lang = Config.getLang();

		disLang(lang);

		for (;;) {

			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.C) {
					return;
				} else if (val==Keyboard.E) {
					Config.setLang(lang);
					Display.cls();
					Display.line1(Texte.reboot);
					for(;;) ;
				} else if (val==Keyboard.UP) {
					++lang;
				} else if (val==Keyboard.DOWN) {
					--lang;
				}
				lang += 2;
				lang %= 2;

				disLang(lang);
			}

			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static void setMScnt() {

		int val;
		int cnt = Config.getCnt();
		Display.line1(Texte.anzahl);
		Display.line2();
		Display.intVal(cnt);

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.C) {
					return;
				} else if (val==Keyboard.E) {
					Config.setCnt(cnt);
					Display.cls();
					Display.line1(Texte.reboot);
					for(;;) ;
				} else if (val==Keyboard.UP) {
					cnt++;
					if (cnt>15) cnt = 15;
					Display.line2();
					Display.intVal(cnt);
					Display.data(' ');
				} else if (val==Keyboard.DOWN) {
					cnt--;
					if (cnt<0) cnt = 0;
					Display.line2();
					Display.intVal(cnt);
					Display.data(' ');
				}
			}

			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static int getMSNr() {

		int val;
		int max = Config.getCnt();
		int cnt = 1;
		Display.line1(Texte.selMast);
		Display.line2();
		Display.intVal(cnt);

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.E) {
					Display.cls();
					return cnt;
				} else if (val==Keyboard.UP) {
					cnt++;
					if (cnt>max) cnt = max;
					Display.line2();
					Display.intVal(cnt);
					Display.data(' ');
				} else if (val==Keyboard.DOWN) {
					cnt--;
					if (cnt<1) cnt = 1;
					Display.line2();
					Display.intVal(cnt);
					Display.data(' ');
				}
			}

			Timer.wd();
			Timer.waitForNextInterval();
		}
	}

	private static void serviceUp() {

		int i;

		if (!Station.isDown()) {
			msg(Texte.notdown, Texte.empty);
			return;
		}
		int nr = getMSNr()-1;				// zero offset
		Station.serviceUp();
		Display.line1(Texte.waitMast);
		for (i=3; i>0; --i) {
			Display.line2();
			Display.intVal(i);
			Timer.sleepWd(1000);
		}
		msg(Texte.goesUp, Texte.best);
		Station.up();
		Station.servAfterUp(nr);
		Display.cls();
		Display.line1(Texte.reboot);
		for(;;) ;
	}

	private static void serviceDown() {

		int i;

		if (!Station.isUp()) {
			msg(Texte.notup, Texte.empty);
			return;
		}
		int nr = getMSNr()-1;				// zero offset
		Station.serviceDown();
		Display.line1(Texte.waitMast);
		for (i=3; i>0; --i) {
			Display.line2();
			Display.intVal(i);
			Timer.sleepWd(1000);
		}
		msg(Texte.goesDown, Texte.best);
		Station.down();
		Station.servAfterDown(nr);
		Display.cls();
		Display.line1(Texte.reboot);
		for(;;) ;
	}

/**
*	save mast data on mast 1.
*/
	private static void backZs() {

		Display.cls();
		Display.line1(Texte.backZs);
		Timer.sleepWd(1000);
		Station.backZs();
		Display.cls();
		Display.line1(Texte.reboot);
		for(;;) ;
	}

/**
*	restore mast data from mast 1.
*/
	private static void restZs() {

		Display.cls();
		Display.line1(Texte.restZs);
		Timer.sleepWd(1000);
		Station.restZs();
		Display.cls();
		Display.line1(Texte.reboot);
		for(;;) ;
	}

}
