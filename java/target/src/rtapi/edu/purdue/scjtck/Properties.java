package edu.purdue.scjtck;

import javax.realtime.PriorityScheduler;
import javax.safetycritical.Terminal;
import javax.safetycritical.annotate.Level;

public class Properties {

//    public Level _level = Level.LEVEL_1;
    public long _duration = 300;
    public long _period = 50;
    public long _iDelay = 0;
    public long _missionMemSize = 5000;
    public long _schedObjBackStoreSize = 5000;
    public long _schedObjScopeSize = 2500;
    public int _threads = 1;
    public int _priority = PriorityScheduler.instance().getNormPriority();
    public int _iterations = 100;
    public int _dropFirstN = 5;

    public long _bgPeriod = 250;
    public long _bgIDelay = 0;
    public int _bgThreads = 0;
	public int _bgPriority = PriorityScheduler.instance().getNormPriority();

	public static String getOptionsInfo() {
		// TODO: refine the usage information of current test case or benchmark
		String info = "";
		info += "\t -L level\n";
		info += "\t -D duration\n";
		info += "\t -P period\n";
		info += "\t -M mission memory size\n";
		info += "\t -m schedulable object memory size\n";
		info += "\t -R prority\n";
		info += "\t -I iteration\n";
		return info;
	}

	public void parseArgs(CharSequence args) {
		int start = -1;
		char option = ' ';

		// -M 5000 -L 1 ...
		for (int i = 0; i < args.length(); i++) {
			if (args.charAt(i) != '-')
				continue;
			if (start != -1) {
				parseArgsHelper(option, args.subSequence(start, i - 1)
						.toString());
			}
			option = args.charAt(i + 1);
			start = i + 3;
		}
		if (start != -1) {
			parseArgsHelper(option, args.subSequence(start, args.length())
					.toString());
		}
	}

	private void parseArgsHelper(char option, String value) {
		value = value.trim();
		switch (option) {
		case 'L':
			//_level = Level.getLevel(value);
			break;
		case 'D':
			_duration = Long.valueOf(value);
			break;
		case 'I':
			_iterations = Integer.valueOf(value);
			break;
		case 'P':
			_period = Long.valueOf(value);
			break;
		case 'p':
			_bgPeriod = Long.valueOf(value);
			break;
		case 'M':
			_missionMemSize = Long.valueOf(value);
			break;
		case 'm':
			_schedObjBackStoreSize = Long.valueOf(value);
			break;
		case 'R':
			_priority = Integer.valueOf(value);
			break;
		case 'r':
			_bgPriority = Integer.valueOf(value);
			break;
		case 'T':
			_threads = Integer.valueOf(value);
		case 't':
			_bgThreads = Integer.valueOf(value);
			break;
		case 'F':
			_dropFirstN = Integer.valueOf(value);
			break;
		default:
			Terminal.getTerminal().writeln("Unknown option: -" + value);
			System.exit(1);
		}
	}
}
